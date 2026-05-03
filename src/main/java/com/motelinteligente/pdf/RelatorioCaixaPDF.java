package com.motelinteligente.pdf;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;
import javax.swing.GroupLayout.Alignment;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JFileChooser;

public class RelatorioCaixaPDF implements Relatorio {

    int count = 0;
    float somaTotal = 0;
    private Document documentoPDF;

    public RelatorioCaixaPDF() {
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
        } else if (userSelection == JFileChooser.CANCEL_OPTION) {
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
        System.out.println("Corpo do relatorio ok");
    }

    @Override
    public void gerarRodape() {
        this.adicionarQuebraDeSessao();
        this.pularLinha();
        this.adicionarRodaPe();
        System.out.println("redape do relatorio ok");
    }

    @Override
    public void imprimir() {
        if (this.documentoPDF != null && this.documentoPDF.isOpen()) {
            documentoPDF.close();
        }

        JOptionPane.showMessageDialog(null, "Documento gerado com sucesso");
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
        Chunk cTitulo = new Chunk("RELATÓRIO DE CAIXAS");
        cTitulo.setFont(new Font(Font.COURIER, 24));
        cTitulo.setBackground(Color.lightGray, 2, 2, 2, 2);
        paragrafoTitulo.add(cTitulo);
        documentoPDF.add(paragrafoTitulo);
    }

    private void adicionarImagem(String caminhoImagem) {
        if (this.documentoPDF != null) { // Verifica se o documento foi inicializado
            Image imgTitulo = null;
            try {
                imgTitulo = Image.getInstance(caminhoImagem);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "IMG Não encontrada", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            if (imgTitulo != null) {
                imgTitulo.setAlignment(Element.ALIGN_CENTER);
                try {
                    this.documentoPDF.add(imgTitulo);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void pularLinha() {
        this.documentoPDF.add(new Paragraph(" "));
    }

    private PdfPTable criarTabelaComCabecalho() {
        // tabela com 4 colunas
        PdfPTable tableProdutos = new PdfPTable(7);
        tableProdutos.setWidthPercentage(98);
        tableProdutos.setWidths(new float[]{1f, 2f, 1f, 1f,  2f, 1f, 1f});

        PdfPCell celulaTitulo = new PdfPCell(new Phrase("ID"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("ABERTURA"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("USER ABRIU"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("SALDO INICIAL"));
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("FECHAMENTO"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("USER FECHOU"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        celulaTitulo = new PdfPCell(new Phrase("SALDO FECHA"));
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setBackgroundColor(Color.LIGHT_GRAY);
        tableProdutos.addCell(celulaTitulo);

        return tableProdutos;
    }

    private void adicionarProdutosATabela(PdfPTable tableProdutos, List<List<Object>> dadosDasColunas) {
    int contador = 1;

    for (List<Object> linha : dadosDasColunas) {

        PdfPCell celulaID = new PdfPCell(new Phrase(String.valueOf(linha.get(0))));
        PdfPCell celulaAbertura = new PdfPCell(new Phrase(String.valueOf(linha.get(1))));
        PdfPCell celulaUserAbriu = new PdfPCell(new Phrase(String.valueOf(linha.get(2))));
        PdfPCell celulaSaldoInicial = new PdfPCell(new Phrase("R$ " + String.valueOf(linha.get(3))));
        PdfPCell celulaFechamento = new PdfPCell(new Phrase(String.valueOf(linha.get(4))));
        PdfPCell celulaUserFechou = new PdfPCell(new Phrase(String.valueOf(linha.get(5))));
        PdfPCell celulaSaldoFecha = new PdfPCell(new Phrase("R$ " + String.valueOf(linha.get(6))));

        // Cores de linha alternadas
        if (contador % 2 == 0) {
            Color cor = Color.LIGHT_GRAY;
            celulaID.setBackgroundColor(cor);
            celulaAbertura.setBackgroundColor(cor);
            celulaUserAbriu.setBackgroundColor(cor);
            celulaSaldoInicial.setBackgroundColor(cor);
            celulaFechamento.setBackgroundColor(cor);
            celulaUserFechou.setBackgroundColor(cor);
            celulaSaldoFecha.setBackgroundColor(cor);
        }

        // Adiciona as células na ordem correta
        tableProdutos.addCell(celulaID);
        tableProdutos.addCell(celulaAbertura);
        tableProdutos.addCell(celulaUserAbriu);
        tableProdutos.addCell(celulaSaldoInicial);
        tableProdutos.addCell(celulaFechamento);
        tableProdutos.addCell(celulaUserFechou);
        tableProdutos.addCell(celulaSaldoFecha);

        // Cálculo total (presumindo que você declarou somaTotal em outro lugar)
        float saldoInicial = parseFloatSafe(linha.get(3));
        float saldoFechamento = parseFloatSafe(linha.get(6));
        somaTotal += saldoFechamento - saldoInicial;

        contador++;
    }
    count = contador-1;
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
        pTotal.add(new Chunk(count + " caixas.                          Valor Total: R$ " + somaTotal,
                new Font(Font.TIMES_ROMAN, 16)));
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
