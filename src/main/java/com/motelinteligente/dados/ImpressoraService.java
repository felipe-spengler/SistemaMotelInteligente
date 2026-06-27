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
}
