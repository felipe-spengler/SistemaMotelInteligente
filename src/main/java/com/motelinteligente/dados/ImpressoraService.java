package com.motelinteligente.dados;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.table.DefaultTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpressoraService {

    private static final Logger logger = LoggerFactory.getLogger(ImpressoraService.class);

    /**
     * Lista todas as impressoras instaladas no sistema operacional.
     */
    public static List<String> listarImpressoras() {
        List<String> impressoras = new ArrayList<>();
        try {
            PrintService[] printServices = PrinterJob.lookupPrintServices();
            for (PrintService service : printServices) {
                impressoras.add(service.getName());
            }
        } catch (Exception e) {
            logger.error("Erro ao listar impressoras: ", e);
        }
        return impressoras;
    }

    /**
     * Imprime um texto genérico em formato cupom na impressora configurada.
     */
    public static void imprimirTexto(String texto) {
        configGlobal config = configGlobal.getInstance();
        if (!config.isImpressoraAtiva()) {
            logger.info("Impressão desativada nas configurações globais.");
            return;
        }

        String printerName = config.getImpressoraNome();
        PrintService selectedService = null;

        try {
            if (printerName != null && !printerName.trim().isEmpty()) {
                PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
                for (PrintService service : printServices) {
                    if (service.getName().equalsIgnoreCase(printerName.trim())) {
                        selectedService = service;
                        break;
                    }
                }
            }

            if (selectedService == null) {
                selectedService = PrintServiceLookup.lookupDefaultPrintService();
            }

            if (selectedService == null) {
                logger.error("Nenhuma impressora padrão ou selecionada foi encontrada no sistema!");
                return;
            }

            logger.info("Tentando imprimir via modo RAW (Texto Direto) na impressora: {}", selectedService.getName());
            
            try {
                // Tenta impressão direta por bytes (modo RAW) - Altamente compatível com bobinas térmicas e "Generic/Text Only"
                javax.print.DocFlavor flavor = javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE;
                javax.print.DocPrintJob docJob = selectedService.createPrintJob();
                
                // Converter para bytes usando encoding Cp850 (acentuação compatível com a maioria das impressoras térmicas brasileiras)
                byte[] textBytes = texto.getBytes("Cp850");
                
                // Comandos ESC/POS básicos:
                // ESC @ (0x1B, 0x40) - Inicializa a impressora
                byte[] escInit = new byte[]{0x1B, 0x40};
                // Avanço de papel (5 linhas) e corte parcial (GS V 66 0 -> 0x1D, 0x56, 0x42, 0x00)
                byte[] escCut = new byte[]{0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x1D, 0x56, 0x42, 0x00};
                
                byte[] finalBytes = new byte[escInit.length + textBytes.length + escCut.length];
                System.arraycopy(escInit, 0, finalBytes, 0, escInit.length);
                System.arraycopy(textBytes, 0, finalBytes, escInit.length, textBytes.length);
                System.arraycopy(escCut, 0, finalBytes, escInit.length + textBytes.length, escCut.length);
                
                javax.print.Doc doc = new javax.print.SimpleDoc(finalBytes, flavor, null);
                docJob.print(doc, null);
                logger.info("Cupom impresso via modo RAW com sucesso!");
                return; // Impressão concluída com sucesso, encerra o método
            } catch (Exception rawException) {
                logger.warn("Falha ao imprimir em modo RAW. Tentando fallback para modo AWT Graphics...", rawException);
            }

            // FALLBACK: Modo Gráfico AWT (caso o modo RAW falhe por restrição de Driver)
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(selectedService);

            final String textToPrint = texto;
            String[] lines = textToPrint.split("\n");
            double calculatedHeight = (lines.length + 6) * 12;

            PageFormat pf = job.defaultPage();
            Paper paper = new Paper();
            paper.setSize(220, calculatedHeight);
            paper.setImageableArea(10, 10, 200, calculatedHeight - 20);
            pf.setPaper(paper);
            pf.setOrientation(PageFormat.PORTRAIT);

            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));
                    g2d.setColor(Color.BLACK);

                    FontMetrics metrics = g2d.getFontMetrics();
                    int lineHeight = metrics.getHeight();
                    int y = lineHeight;

                    for (String line : lines) {
                        g2d.drawString(line, 0, y);
                        y += lineHeight;
                    }

                    return PAGE_EXISTS;
                }
            }, pf);

            job.print();
            logger.info("Cupom enviado via modo Gráfico AWT com sucesso para a impressora: {}", selectedService.getName());
        } catch (Exception e) {
            logger.error("Erro durante o processo de impressão do cupom: ", e);
        }
    }

    /**
     * Imprime os dados gerais do caixa (Abertura, fechamento e resumo de entradas).
     */
    public static void imprimirFechamentoCaixa(int idCaixa, valores v, float saldoIni, float antecipadoOutro, valores antecipadoDetalhado, float totalJustificativas) {
        if (!configGlobal.getInstance().isImpressoraAtiva()) return;

        fcaixa dao = new fcaixa();
        String usuarioAbre = dao.getUsuarioAbriu(idCaixa);
        Timestamp dataAbre = dao.getDataAbriu(idCaixa);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String strAbertura = dataAbre != null ? sdf.format(dataAbre) : "N/A";
        String strFechamento = sdf.format(new Date());

        float esperadoDinheiro = saldoIni + v.entradaD - (antecipadoOutro > 0 ? antecipadoOutro : 0);
        float esperadoCartao = v.entradaC;
        float esperadoPix = v.entradaP;
        float saldoFinal = esperadoDinheiro + esperadoCartao + esperadoPix;

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("          FECHAMENTO DE CAIXA             \n");
        sb.append("==========================================\n");
        sb.append(String.format("Caixa ID: %d\n", idCaixa));
        sb.append(String.format("Abertura: %s\n", strAbertura));
        sb.append(String.format("Aberto por: %s\n", usuarioAbre != null ? usuarioAbre : "N/A"));
        sb.append(String.format("Fechamento: %s\n", strFechamento));
        sb.append(String.format("Fechado por: %s\n", configGlobal.getInstance().getUsuario()));
        sb.append("------------------------------------------\n");
        sb.append(String.format("Saldo Inicial (Fundo):     R$ %,.2f\n", saldoIni));
        sb.append(String.format("Vendas (Produtos):         R$ %,.2f\n", v.entradaConsumo));
        sb.append(String.format("Locacoes (Suites):         R$ %,.2f\n", v.entradaQuarto));
        sb.append(String.format("Ajustes/Justificativas:    R$ %,.2f\n", totalJustificativas));
        sb.append("------------------------------------------\n");
        sb.append("VALORES ESPERADOS EM GAVETA:\n");
        sb.append(String.format("Dinheiro Fisico:           R$ %,.2f\n", esperadoDinheiro));
        sb.append(String.format("Cartao Comprovantes:       R$ %,.2f\n", esperadoCartao));
        sb.append(String.format("Pix Recebidos:             R$ %,.2f\n", esperadoPix));
        if (antecipadoOutro > 0) {
            sb.append(String.format("Pago em Outro Caixa (Info):R$ %,.2f\n", antecipadoOutro));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format("TOTAL GERAL ESPERADO:      R$ %,.2f\n", saldoFinal));
        sb.append("==========================================\n\n\n\n\n");

        imprimirTexto(sb.toString());
    }

    /**
     * Imprime a listagem de todos os produtos vendidos durante o período do caixa.
     */
    public static void imprimirProdutosVendidos(int idCaixa) {
        if (!configGlobal.getInstance().isImpressoraAtiva()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("       PRODUTOS VENDIDOS NO CAIXA         \n");
        sb.append("==========================================\n");
        sb.append(String.format("Caixa ID: %d\n", idCaixa));
        sb.append("------------------------------------------\n");
        sb.append(String.format("Qtd  Produto              V.Unit  V.Total\n"));
        sb.append("------------------------------------------\n");

        String sql = "SELECT p.descricao, SUM(rv.quantidade) as qtd, rv.valorunidade, SUM(rv.valortotal) as total " +
                     "FROM registravendido rv " +
                     "INNER JOIN produtos p ON rv.idproduto = p.idproduto " +
                     "WHERE rv.idcaixaatual = ? " +
                     "GROUP BY p.descricao, rv.valorunidade";

        float totalGeral = 0;

        try (Connection link = new fazconexao().conectar();
             PreparedStatement stmt = link.prepareStatement(sql)) {
            stmt.setInt(1, idCaixa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String desc = rs.getString("descricao");
                    if (desc != null && desc.length() > 20) {
                        desc = desc.substring(0, 18) + "..";
                    }
                    int qtd = rs.getInt("qtd");
                    float vUnit = rs.getFloat("valorunidade");
                    float total = rs.getFloat("total");
                    totalGeral += total;

                    sb.append(String.format("%3d  %-20s %6.2f %8.2f\n", qtd, desc != null ? desc : "N/A", vUnit, total));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar produtos vendidos do caixa: ", e);
            return;
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("TOTAL VENDAS PRODUTOS:     R$ %,.2f\n", totalGeral));
        sb.append("==========================================\n\n\n\n\n");

        imprimirTexto(sb.toString());
    }

    /**
     * Imprime o extrato de produtos vendidos detalhado por quarto (para reposição de estoque).
     */
    public static void imprimirProdutosVendidosPorQuarto(int idCaixa) {
        if (!configGlobal.getInstance().isImpressoraAtiva()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("    PRODUTOS VENDIDOS - POR QUARTO        \n");
        sb.append("==========================================\n");
        sb.append(String.format("Caixa ID: %d\n", idCaixa));
        sb.append("------------------------------------------\n");

        // Consulta agrupada por quarto + produto para facilitar reposição
        String sql =
    "SELECT rl.numquarto, p.descricao, SUM(rv.quantidade) as qtd, rv.valorunidade, SUM(rv.valortotal) as total " +
    "FROM registravendido rv " +
    "INNER JOIN produtos p ON rv.idproduto = p.idproduto " +
    "LEFT JOIN registralocado rl ON rv.idlocacao = rl.idlocacao " +
    "WHERE rv.idcaixaatual = ? " +
    "GROUP BY rl.numquarto, p.descricao, rv.valorunidade " + // <-- Adicionado isso aqui
    "ORDER BY rl.numquarto, p.descricao";

        float totalGeral = 0;
        int quartoAtual = -1;
        float totalQuarto = 0;

        try (Connection link = new fazconexao().conectar();
             PreparedStatement stmt = link.prepareStatement(sql)) {
            stmt.setInt(1, idCaixa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int quarto = rs.getInt("numquarto");
                    String desc = rs.getString("descricao");
                    if (desc != null && desc.length() > 18) desc = desc.substring(0, 16) + "..";
                    int qtd = rs.getInt("qtd");
                    float vUnit = rs.getFloat("valorunidade");
                    float total = rs.getFloat("total");

                    // Cabeçalho do quarto quando muda
                    if (quarto != quartoAtual) {
                        if (quartoAtual != -1) {
                            sb.append(String.format("  Subtotal Qto %02d:       R$ %,.2f\n", quartoAtual, totalQuarto));
                        }
                        quartoAtual = quarto;
                        totalQuarto = 0;
                        sb.append(String.format("--- Quarto %02d ---\n", quarto > 0 ? quarto : 0));
                        sb.append(String.format("Qtd  Produto              V.Unit  V.Total\n"));
                    }

                    totalQuarto += total;
                    totalGeral += total;
                    sb.append(String.format("%3d  %-18s %6.2f %8.2f\n", qtd, desc != null ? desc : "N/A", vUnit, total));
                }
                // Fecha último quarto
                if (quartoAtual != -1) {
                    sb.append(String.format("  Subtotal Qto %02d:       R$ %,.2f\n", quartoAtual, totalQuarto));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar produtos por quarto: ", e);
            return;
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("TOTAL GERAL PRODUTOS:      R$ %,.2f\n", totalGeral));
        sb.append("==========================================\n\n\n\n\n");

        imprimirTexto(sb.toString());
    }

    /**
     * Imprime o cupom do extrato de locação para entrega ao cliente.
     */

    public static void imprimirExtratoLocacao(
            int numeroQuarto, int idLocacao, String dataInicio, String dataFim, String tempoTotalLocado,
            float valorQuarto, float valorAdicionalPessoa, float valorAdicionalPeriodo, float valorConsumo,
            float valorAcrescimo, float valorDesconto, float valoreRecebido, float valorRecebidoAgora,
            DefaultTableModel modelTabela) {

        if (!configGlobal.getInstance().isImpressoraAtiva()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("            EXTRATO DE LOCACAO            \n");
        sb.append("==========================================\n");
        sb.append(String.format("Quarto: %02d            Locacao ID: %d\n", numeroQuarto, idLocacao));

        SimpleDateFormat inSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String entFmt = dataInicio != null ? dataInicio : "N/A";
        String saiFmt = dataFim != null ? dataFim : "N/A";
        try {
            if (dataInicio != null && !dataInicio.equals("N/A")) {
                String clean = dataInicio.contains(".") ? dataInicio.substring(0, dataInicio.indexOf('.')) : dataInicio;
                entFmt = outSdf.format(inSdf.parse(clean));
            }
            if (dataFim != null && !dataFim.equals("N/A")) {
                String clean = dataFim.contains(".") ? dataFim.substring(0, dataFim.indexOf('.')) : dataFim;
                saiFmt = outSdf.format(inSdf.parse(clean));
            }
        } catch (Exception e) {
            // Mantém a string original em caso de erro no parse
        }

        sb.append(String.format("Entrada: %s\n", entFmt));
        sb.append(String.format("Saida:   %s\n", saiFmt));
        sb.append(String.format("Duracao: %s\n", tempoTotalLocado != null ? tempoTotalLocado : "N/A"));
        sb.append("------------------------------------------\n");
        sb.append("HOSPEDAGEM:\n");
        sb.append(String.format("Valor Quarto:              R$ %,.2f\n", valorQuarto));
        if (valorAdicionalPessoa > 0) {
            sb.append(String.format("Adic. Pessoas:             R$ %,.2f\n", valorAdicionalPessoa));
        }
        if (valorAdicionalPeriodo > 0) {
            sb.append(String.format("Adic. Periodo:             R$ %,.2f\n", valorAdicionalPeriodo));
        }

        if (modelTabela != null && modelTabela.getRowCount() > 0) {
            boolean temProdutos = false;
            for (int i = 0; i < modelTabela.getRowCount(); i++) {
                if (modelTabela.getValueAt(i, 0) != null) {
                    temProdutos = true;
                    break;
                }
            }

            if (temProdutos) {
                sb.append("------------------------------------------\n");
                sb.append("PRODUTOS CONSUMIDOS:\n");
                sb.append(String.format("Qtd  Produto              V.Unit  V.Total\n"));
                sb.append("------------------------------------------\n");
                for (int i = 0; i < modelTabela.getRowCount(); i++) {
                    Object idVal = modelTabela.getValueAt(i, 0);
                    if (idVal == null) continue;

                    int qtd = Integer.parseInt(String.valueOf(modelTabela.getValueAt(i, 1)));
                    String desc = (String) modelTabela.getValueAt(i, 2);
                    if (desc != null && desc.length() > 20) {
                        desc = desc.substring(0, 18) + "..";
                    }
                    float vUnit = Float.parseFloat(String.valueOf(modelTabela.getValueAt(i, 3)));
                    float total = Float.parseFloat(String.valueOf(modelTabela.getValueAt(i, 4)));

                    sb.append(String.format("%3d  %-20s %6.2f %8.2f\n", qtd, desc != null ? desc : "N/A", vUnit, total));
                }
            }
        }

        float subtotal = valorQuarto + valorAdicionalPessoa + valorAdicionalPeriodo + valorConsumo + valorAcrescimo;
        float totalGeral = subtotal - valorDesconto;

        sb.append("------------------------------------------\n");
        sb.append(String.format("Subtotal Quarto + Prod:    R$ %,.2f\n", subtotal));
        if (valorDesconto > 0) {
            sb.append(String.format("Desconto:                 -R$ %,.2f\n", valorDesconto));
        }
        sb.append(String.format("TOTAL GERAL:               R$ %,.2f\n", totalGeral));
        if (valoreRecebido > 0) {
            sb.append(String.format("Pago Antecipado:           R$ %,.2f\n", valoreRecebido));
        }
        if (valorRecebidoAgora > 0) {
            sb.append(String.format("Pago no Fechamento:        R$ %,.2f\n", valorRecebidoAgora));
        }
        float aReceber = totalGeral - valoreRecebido - valorRecebidoAgora;
        sb.append(String.format("SALDO RESTANTE:            R$ %,.2f\n", aReceber > 0 ? aReceber : 0));
        sb.append("==========================================\n\n\n\n\n");

        imprimirTexto(sb.toString());
    }

    /**
     * Carrega todos os dados de uma locação do banco de dados e realiza a impressão do extrato.
     */
    public static void imprimirExtratoLocacaoPorId(int idLocacao) {
        if (!configGlobal.getInstance().isImpressoraAtiva()) return;

        try (Connection link = new fazconexao().conectar()) {
            // 1. Obter dados da locação e quarto
            String sqlLoc = "SELECT * FROM registralocado WHERE idlocacao = ?";
            PreparedStatement stmtLoc = link.prepareStatement(sqlLoc);
            stmtLoc.setInt(1, idLocacao);
            ResultSet rsLoc = stmtLoc.executeQuery();

            if (!rsLoc.next()) {
                logger.error("Locacao ID " + idLocacao + " nao encontrada para impressao.");
                return;
            }

            int numeroQuarto = rsLoc.getInt("numquarto");
            Timestamp dataInicioTs = rsLoc.getTimestamp("horainicio");
            Timestamp dataFimTs = rsLoc.getTimestamp("horafim");
            float valorQuarto = rsLoc.getFloat("valorquarto");
            float valorConsumo = rsLoc.getFloat("valorconsumo");
            float valD = rsLoc.getFloat("pagodinheiro");
            float valP = rsLoc.getFloat("pagopix");
            float valC = rsLoc.getFloat("pagocartao");
            String periodoFinalStr = rsLoc.getString("periodo_locado");

            String dataInicio = dataInicioTs != null ? String.valueOf(dataInicioTs) : "N/A";
            String dataFim = dataFimTs != null ? String.valueOf(dataFimTs) : "N/A";

            // 2. Buscar adicionais por pessoa e período da tabela justificativa/antecipado
            float valorAdicionalPessoa = 0;
            float valorAdicionalPeriodo = 0;
            float valorDesconto = 0;
            float valorAcrescimo = 0;
            float valoreRecebido = 0;

            // Busca na tabela justificativa (descontos e acrescimos da locacao)
            String sqlJust = "SELECT tipo, valor FROM justificativa WHERE idlocacao = ?";
            try (PreparedStatement stmtJust = link.prepareStatement(sqlJust)) {
                stmtJust.setInt(1, idLocacao);
                try (ResultSet rsJust = stmtJust.executeQuery()) {
                    while (rsJust.next()) {
                        String tipo = rsJust.getString("tipo");
                        float val = rsJust.getFloat("valor");
                        if ("desconto".equalsIgnoreCase(tipo)) {
                            valorDesconto += val;
                        } else if ("acrescimo".equalsIgnoreCase(tipo)) {
                            valorAcrescimo += val;
                        }
                    }
                }
            }

            // Busca na tabela antecipado (valores pagos antecipados ou descontos inseridos anteriormente)
            String sqlAnt = "SELECT tipo, valor FROM antecipado WHERE idlocacao = ?";
            try (PreparedStatement stmtAnt = link.prepareStatement(sqlAnt)) {
                stmtAnt.setInt(1, idLocacao);
                try (ResultSet rsAnt = stmtAnt.executeQuery()) {
                    while (rsAnt.next()) {
                        String tipo = rsAnt.getString("tipo");
                        float val = rsAnt.getFloat("valor");
                        if ("desconto".equalsIgnoreCase(tipo)) {
                            valorDesconto += val;
                        } else if ("acrescimo".equalsIgnoreCase(tipo)) {
                            valorAcrescimo += val;
                        } else {
                            valoreRecebido += val;
                        }
                    }
                }
            }

            // Calcula tempo total locado
            String tempoTotalLocado = "N/A";
            if (dataInicioTs != null) {
                long endTime = dataFimTs != null ? dataFimTs.getTime() : System.currentTimeMillis();
                long diff = endTime - dataInicioTs.getTime();
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000) % 24;
                long diffDays = diff / (24 * 60 * 60 * 1000);
                String tagPrevia = dataFimTs == null ? " (Pre-via)" : "";
                if (diffDays > 0) {
                    tempoTotalLocado = String.format("%d d, %d h, %d m%s", diffDays, diffHours, diffMinutes, tagPrevia);
                } else if (diffHours > 0) {
                    tempoTotalLocado = String.format("%d h, %d m%s", diffHours, diffMinutes, tagPrevia);
                } else {
                    tempoTotalLocado = String.format("%d min%s", diffMinutes, tagPrevia);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("==========================================\n");
            sb.append("            EXTRATO DE LOCACAO            \n");
            sb.append("==========================================\n");
            sb.append(String.format("Quarto: %02d            Locacao ID: %d\n", numeroQuarto, idLocacao));

            SimpleDateFormat inSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            String entFmt = dataInicio;
            String saiFmt = dataFim;
            try {
                if (dataInicioTs != null) {
                    entFmt = outSdf.format(dataInicioTs);
                }
                if (dataFimTs != null) {
                    saiFmt = outSdf.format(dataFimTs);
                }
            } catch (Exception e) {
                // Fallback
            }

            sb.append(String.format("Entrada: %s\n", entFmt));
            sb.append(String.format("Saida:   %s\n", saiFmt));
            sb.append(String.format("Duracao: %s\n", tempoTotalLocado));
            sb.append("------------------------------------------\n");
            sb.append("HOSPEDAGEM:\n");
            sb.append(String.format("Valor Quarto:              R$ %,.2f\n", valorQuarto));

            // 3. Obter produtos (da tabela prevendidos se for pré-via, ou registravendido se finalizado)
            String sqlProd;
            if (dataFimTs == null) {
                sqlProd = "SELECT p.descricao AS nomeproduto, pv.quantidade, p.valorproduto AS valorunidade, (pv.quantidade * p.valorproduto) AS valortotal " +
                          "FROM prevendidos pv JOIN produtos p ON pv.idproduto = p.idproduto WHERE pv.idlocacao = ?";
            } else {
                sqlProd = "SELECT p.descricao AS nomeproduto, v.quantidade, v.valorunidade, v.valortotal " +
                          "FROM registravendido v JOIN produtos p ON v.idproduto = p.idproduto WHERE v.idlocacao = ?";
            }
            PreparedStatement stmtProd = link.prepareStatement(sqlProd);
            stmtProd.setInt(1, idLocacao);
            ResultSet rsProd = stmtProd.executeQuery();

            boolean temProdutos = false;
            StringBuilder sbProd = new StringBuilder();
            while (rsProd.next()) {
                if (!temProdutos) {
                    sbProd.append("------------------------------------------\n");
                    sbProd.append("PRODUTOS CONSUMIDOS:\n");
                    sbProd.append(String.format("Qtd  Produto              V.Unit  V.Total\n"));
                    sbProd.append("------------------------------------------\n");
                    temProdutos = true;
                }
                int qtd = rsProd.getInt("quantidade");
                String desc = rsProd.getString("nomeproduto");
                if (desc != null && desc.length() > 20) {
                    desc = desc.substring(0, 18) + "..";
                }
                float vUnit = rsProd.getFloat("valorunidade");
                float total = rsProd.getFloat("valortotal");

                sbProd.append(String.format("%3d  %-20s %6.2f %8.2f\n", qtd, desc != null ? desc : "N/A", vUnit, total));
            }
            if (temProdutos) {
                sb.append(sbProd.toString());
            }

            float subtotal = valorQuarto + valorAdicionalPessoa + valorAdicionalPeriodo + valorConsumo + valorAcrescimo;
            float totalGeral = subtotal - valorDesconto;

            sb.append("------------------------------------------\n");
            sb.append(String.format("Subtotal Quarto + Prod:    R$ %,.2f\n", subtotal));
            if (valorDesconto > 0) {
                sb.append(String.format("Desconto:                 -R$ %,.2f\n", valorDesconto));
            }
            sb.append(String.format("TOTAL GERAL:               R$ %,.2f\n", totalGeral));
            
            // Pagamentos realizados
            float totalPago = valD + valP + valC;
            if (valD > 0) {
                sb.append(String.format("Pago em Dinheiro:          R$ %,.2f\n", valD));
            }
            if (valP > 0) {
                sb.append(String.format("Pago em Pix:               R$ %,.2f\n", valP));
            }
            if (valC > 0) {
                sb.append(String.format("Pago em Cartao:            R$ %,.2f\n", valC));
            }

            float aReceber = totalGeral - valoreRecebido - totalPago;
            sb.append(String.format("SALDO RESTANTE:            R$ %,.2f\n", aReceber > 0 ? aReceber : 0));
            sb.append("==========================================\n\n\n\n\n");

            imprimirTexto(sb.toString());

        } catch (Exception e) {
            logger.error("Erro ao carregar dados da locacao ID " + idLocacao + " para impressao", e);
        }
    }
}
