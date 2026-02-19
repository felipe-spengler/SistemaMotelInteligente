package com.motelinteligente.pdf;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JFileChooser;

public class RelatorioLocacoesPDF implements Relatorio {

    private Document documentoPDF;
    int count = 0;
    float totalQuarto = 0, totalConsumo = 0, totalTotal = 0;
    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yy HH:mm");

    public RelatorioLocacoesPDF() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar PDF"); // Título da caixa de diálogo

        int userSelection = fileChooser.showSaveDialog(null); // Exibe a caixa de diálogo de salvamento

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Garante que o nome do arquivo termine com ".pdf"
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            this.documentoPDF = new Document(PageSize.A4, 50, 50, 50, 50);
            try {
                PdfWriter.getInstance(this.documentoPDF, new FileOutputStream(filePath));
                this.adicionarPaginacao();
                this.documentoPDF.open();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            // Se o usuário cancelou a operação de salvamento
            System.out.println("Operação de salvamento cancelada.");
        }

    }

    @Override
    public void gerarCabecalho(String data) {
        this.adicionarImagem("src/main/resources/imagens/iconeMI.jpg");
        this.pularLinha();
        this.adicionarParagrafoTitulo();
        this.pularLinha();
        this.pularLinha();
        this.pularLinha();
        this.datar(data);
        this.pularLinha();
        this.adicionarQuebraDeSessao();
        System.out.println("cabeçalho do relatorio ok");
    }

    @Override
    public void gerarCorpo(List<List<Object>> dadosDasColunas) {
        this.pularLinha();
        this.adicionarParagrafoItensVendidosTitulo();
        this.pularLinha();
        PdfPTable tableProdutos = this.criarTabelaComCabecalho();
        this.adicionarProdutosATabela(tableProdutos, dadosDasColunas);
        this.documentoPDF.add(tableProdutos);
        this.pularLinha();
        this.pularLinha();
        this.adicionarTotalDaVenda();
        System.out.println("corpo do relatorio ok");
    }

    @Override
    public void gerarRodape() {
        this.adicionarQuebraDeSessao();
        this.pularLinha();
        this.adicionarRodaPe();
        System.out.println("rodape do relatorio ok");
    }

    @Override
    public void imprimir() {
        if (this.documentoPDF != null && this.documentoPDF.isOpen()) {
            documentoPDF.close();
        }
        JOptionPane.showMessageDialog(null, "Relatorio gerado com sucesso!");
    }

    private void adicionarPaginacao() {
        HeaderFooter paginacao = new HeaderFooter(new Phrase("Pág.", new Font(Font.BOLD)), true);
        paginacao.setAlignment(Element.ALIGN_RIGHT);
        paginacao.setBorder(Rectangle.NO_BORDER);
        documentoPDF.setHeader(paginacao);
    }

    private void datar(String data) {
        Chunk chunkDataCliente = new Chunk();
        chunkDataCliente.append(data);

        Paragraph paragrafoDataCliente = new Paragraph();
        paragrafoDataCliente.add(chunkDataCliente);
        this.documentoPDF.add(paragrafoDataCliente);
    }

    private void adicionarQuebraDeSessao() {
        Paragraph paragrafoSessao = new Paragraph("__________________________________________________________");
        paragrafoSessao.setAlignment(Element.ALIGN_CENTER);
        this.documentoPDF.add(paragrafoSessao);
    }

    private void adicionarParagrafoTitulo() {
        Paragraph paragrafoTitulo = new Paragraph();
        paragrafoTitulo.setAlignment(Element.ALIGN_CENTER);
        Chunk cTitulo = new Chunk("RELATÓRIO DE LOCAÇÕES");
        cTitulo.setFont(new Font(Font.COURIER, 24));
        cTitulo.setBackground(Color.lightGray, 2, 2, 2, 2);
        paragrafoTitulo.add(cTitulo);
        documentoPDF.add(paragrafoTitulo);
    }

    private void adicionarImagem(String caminhoImagem) {
        Image imgTitulo = null;
        try {
            imgTitulo = Image.getInstance(caminhoImagem);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "IMG Não encontrada", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        if (imgTitulo != null) {
            imgTitulo.setAlignment(Element.ALIGN_CENTER);
            this.documentoPDF.add(imgTitulo);
        }
    }

    private void pularLinha() {
        this.documentoPDF.add(new Paragraph(" "));
    }

    private PdfPTable criarTabelaComCabecalho() {
        // tabela com 4 colunas
        PdfPTable tableProdutos = new PdfPTable(6);
        tableProdutos.setWidthPercentage(98);
        tableProdutos.setWidths(new float[]{2f, 2f, 1f, 1f, 1f, 1f});

        PdfPCell celulaTitulo = new PdfPCell(new Phrase("INÍCIO"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("TÉRMINO"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("Nº QUARTO"));
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("VALOR QUARTO."));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("VALOR CONSUMO"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("VALOR TOTAL"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        return tableProdutos;
    }

    private void adicionarProdutosATabela(PdfPTable tableProdutos, List<List<Object>> dadosDasColunas) {
        int contador = 1;

        for (List<Object> linha : dadosDasColunas) {

            // Se for uma linha de "Caixa", tratar separadamente
            if (String.valueOf(linha.get(0)).startsWith("Caixa")) {
                System.out.println("Deu if aqui");
                Font fonteCaixa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED);
                PdfPCell celulaCaixa = new PdfPCell(new Phrase(String.valueOf(linha.get(0)), fonteCaixa));
                celulaCaixa.setColspan(6);
                celulaCaixa.setHorizontalAlignment(Element.ALIGN_CENTER);
                celulaCaixa.setBackgroundColor(Color.YELLOW);
                celulaCaixa.setBorder(Rectangle.NO_BORDER); // opcional: sem borda
                tableProdutos.addCell(celulaCaixa);
                contador = 0; // zera para recomeçar a alternância de cor, se quiser
                continue;
            } else {
                count++;
            }

            // Processamento normal das linhas com dados
            Object objDataInicio = linha.get(0);
            Object objDataFim = linha.get(1);

            String dataInicioFormatada = objDataInicio instanceof Date
                    ? formato.format((Date) objDataInicio)
                    : String.valueOf(objDataInicio);

            String dataFimFormatada = objDataFim instanceof Date
                    ? formato.format((Date) objDataFim)
                    : String.valueOf(objDataFim);

            PdfPCell celulaInicio = new PdfPCell(new Phrase(dataInicioFormatada));
            PdfPCell celulaFim = new PdfPCell(new Phrase(dataFimFormatada));
            PdfPCell celulaQuarto = new PdfPCell(new Phrase(String.valueOf(linha.get(2))));
            PdfPCell celulaValorQ = new PdfPCell(new Phrase("R$ " + String.valueOf(linha.get(3))));
            PdfPCell celulaValorC = new PdfPCell(new Phrase("R$ " + String.valueOf(linha.get(4))));
            PdfPCell celulaValorT = new PdfPCell(new Phrase("R$ " + String.valueOf(linha.get(5))));

            // Alterna a cor de fundo
            if (contador % 2 == 0) {
                celulaInicio.setBackgroundColor(Color.LIGHT_GRAY);
                celulaFim.setBackgroundColor(Color.LIGHT_GRAY);
                celulaQuarto.setBackgroundColor(Color.LIGHT_GRAY);
                celulaValorQ.setBackgroundColor(Color.LIGHT_GRAY);
                celulaValorC.setBackgroundColor(Color.LIGHT_GRAY);
                celulaValorT.setBackgroundColor(Color.LIGHT_GRAY);
            }

            tableProdutos.addCell(celulaInicio);
            tableProdutos.addCell(celulaFim);
            tableProdutos.addCell(celulaQuarto);
            tableProdutos.addCell(celulaValorQ);
            tableProdutos.addCell(celulaValorC);
            tableProdutos.addCell(celulaValorT);

            totalQuarto += parseFloatSafe(linha.get(3));
            totalConsumo += parseFloatSafe(linha.get(4));
            totalTotal += parseFloatSafe(linha.get(5));

            contador++;
        }

    }

    private float parseFloatSafe(Object valor) {
        try {
            if (valor == null || String.valueOf(valor).equalsIgnoreCase("null")) {
                return 0f;
            }
            return Float.parseFloat(String.valueOf(valor));
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    private void adicionarTotalDaVenda() {

        Paragraph pTotal = new Paragraph();
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valorFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        
        pTotal.setSpacingBefore(10f);  // Espaçamento antes do parágrafo
        pTotal.setSpacingAfter(10f);   // Espaçamento depois

// Título centralizado
        Paragraph titulo = new Paragraph("Resumo do Caixa", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        this.documentoPDF.add(titulo);

// Locações
        pTotal.add(new Chunk("Locações: ", labelFont));
        pTotal.add(new Chunk(count + "\n", valorFont));

// Valor Consumo
        pTotal.add(new Chunk("Valor Consumo: ", labelFont));
        pTotal.add(new Chunk("R$ " + totalConsumo + "\n", valorFont));

// Valor Quarto
        pTotal.add(new Chunk("Valor Quarto: ", labelFont));
        pTotal.add(new Chunk("R$ " + totalQuarto + "\n", valorFont));

// Soma Total
        pTotal.add(new Chunk("Soma Total: ", labelFont));
        pTotal.add(new Chunk("R$ " + totalTotal + "\n", valorFont));

// Adiciona ao documento
        this.documentoPDF.add(pTotal);
    }

    private void adicionarParagrafoItensVendidosTitulo() {
        Paragraph pItensVendidos = new Paragraph();
        pItensVendidos.setAlignment(Element.ALIGN_CENTER);
        pItensVendidos.add(new Chunk("Caixas: ", new Font(Font.TIMES_ROMAN, 16)));
        documentoPDF.add(new Paragraph(pItensVendidos));
        documentoPDF.add(new Paragraph(" "));
    }

    private void adicionarRodaPe() {
        Date dataAtual = new Date();
        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = formatoData.format(dataAtual);

        // Cria a string para o relatório
        String relatorio = "Relatório gerado em: " + dataFormatada;
        Paragraph pRodape = new Paragraph();
        pRodape.setAlignment(Element.ALIGN_RIGHT);
        pRodape.add(new Chunk(relatorio, new Font(Font.TIMES_ROMAN, 14)));
        this.documentoPDF.add(pRodape);
    }

}
