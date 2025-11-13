/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.telas;

import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.vendaProdutos;
import com.motelinteligente.pdf.Relatorio;
import com.motelinteligente.pdf.RelatorioCaixaPDF;
import com.motelinteligente.pdf.RelatorioLocacoesPDF;
import com.motelinteligente.pdf.RelatorioProdutosPDF;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class ConfereProdutos extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JDateChooser dateChooserInicio;
    private JDateChooser dateChooserFim;
    private JButton buscarButton, btnGerarPDF;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private JLabel  quantiaVendida, valorVendido;
    

    public ConfereProdutos() {
        setTitle(" Consulta de Produtos Vendidos ");
        setSize(1050, 730);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        Font labelFont = new Font("Arial", Font.BOLD, 14);
// Painel superior para filtros
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(new Color(60, 60, 60)); // Define o background do painel superior

// Inicializa os JDateChoosers
        dateChooserInicio = new JDateChooser();
        dateChooserFim = new JDateChooser();
        dateChooserInicio.setPreferredSize(new Dimension(200, 30));
        dateChooserFim.setPreferredSize(new Dimension(200, 30));

// Define a cor de fundo dos campos dos JDateChoosers
        dateChooserInicio.getDateEditor().getUiComponent().setBackground(new Color(255, 255, 255));
        dateChooserFim.getDateEditor().getUiComponent().setBackground(new Color(255, 255, 255));

// Acessa o JTextField interno do JDateChooser e define a cor e a fonte
        JTextField startDateField = (JTextField) dateChooserInicio.getDateEditor().getUiComponent();
        JTextField endDateField = (JTextField) dateChooserFim.getDateEditor().getUiComponent();

        startDateField.setForeground(Color.WHITE);
        endDateField.setForeground(Color.WHITE);

// Define a fonte para 14px para os campos de texto dos JDateChoosers
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        dateChooserInicio.setFont(font);
        dateChooserFim.setFont(font);
        startDateField.setFont(font);
        endDateField.setFont(font);

// Criação dos rótulos
        JLabel labelInicio = new JLabel("Data Início:");
        JLabel labelFim = new JLabel("Data Fim:");

// Define a cor da fonte das labels
        labelInicio.setForeground(Color.WHITE);
        labelFim.setForeground(Color.WHITE);
        labelInicio.setFont(labelFont);
        labelFim.setFont(labelFont);
        
        
// Adiciona os componentes ao painel
        topPanel.add(labelInicio);
        topPanel.add(dateChooserInicio);
        topPanel.add(labelFim);
        topPanel.add(dateChooserFim);

        // Botão para buscar
        buscarButton = new JButton("Buscar");
        topPanel.add(buscarButton);

        // Tabela para exibir os resultados
        tableModel = new DefaultTableModel(new Object[]{"Id", "Descricao", "Quantidade", "Valor Und", "Valor Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // nenhuma célula será editável
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getColumn(table.getColumnName(0)).setPreferredWidth(100);
        table.getColumn(table.getColumnName(1)).setPreferredWidth(200);
        table.getColumn(table.getColumnName(2)).setPreferredWidth(100);
        table.getColumn(table.getColumnName(3)).setPreferredWidth(200);
        table.getColumn(table.getColumnName(4)).setPreferredWidth(200);
        table.setCellSelectionEnabled(false);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(255, 255, 255));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // apenas uma linha de cada vez
        table.setRowSelectionAllowed(true); // permite seleção de linhas
        table.setColumnSelectionAllowed(false); // impede seleção de colunas
        // Adicionando componentes à janela
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Painel inferior com BorderLayout para separar esquerda e direita
        JPanel bottomPanel = new JPanel(new BorderLayout());

// Painel para os botões à esquerda
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnGerarPDF = new JButton("Gerar PDF");
        btnGerarPDF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/pdf_icon.png")));

        leftPanel.add(btnGerarPDF);

// Painel para o label à direita
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        valorVendido = new JLabel("00");
        quantiaVendida = new JLabel("00");
        rightPanel.add(valorVendido);
        rightPanel.add(quantiaVendida);
        quantiaVendida.setFont(labelFont);
        valorVendido.setFont(labelFont);
// Adiciona os dois subpainéis no painel principal
        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Ações dos botões
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarLocacoes();
            }
        });

        btnGerarPDF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                if (model.getRowCount() > 0) {
                    prepararPDF();
                } else {
                    JOptionPane.showMessageDialog(null, "Nenhum dado na tabela.");
                }
            }
        });

        
    }

    private void carregarLocacoes() {
        // pegar os textos dos campos
        ArrayList<Integer> idList = new ArrayList<>();
        Date dataInicio = dateChooserInicio.getDate();
        Date dataFim = dateChooserFim.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataFim);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        dataFim = cal.getTime();
        
        try {

            String consultaSQL = "SELECT idlocacao FROM registralocado WHERE horainicio >= ? AND horainicio <= ?";
            consultaSQL += " AND valorquarto IS NOT NULL order by idcaixaatual";
            java.sql.Connection link = new fazconexao().conectar();
            PreparedStatement stmt = link.prepareStatement(consultaSQL);
            stmt.setTimestamp(1, new Timestamp(dataInicio.getTime()));
            stmt.setTimestamp(2, new Timestamp(dataFim.getTime()));
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("idlocacao");
                idList.add(id);
            }

            // chama a funcao para carregar a tabela
            carregaTabela(idList);
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        }
    }

    public void carregaTabela(ArrayList<Integer> idList) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setNumRows(0);
        vendaProdutos venda = new vendaProdutos();
        vendaProdutos.gerenciaVenda gerenciaVenda = venda.new gerenciaVenda();
        List<vendaProdutos> vendas = gerenciaVenda.listaPorLocacao(idList);
        int quantidadeVendida = 0;
        float valVendido = 0;
        DefaultTableModel modelo = (DefaultTableModel) table.getModel();
        modelo.setNumRows(0);
        //JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        for (vendaProdutos v : vendas) {
            String desc = new fprodutos().getDescicao(String.valueOf(v.idProduto));
            modelo.addRow(new Object[]{
                v.idProduto,
                desc,
                v.quantidade,
                v.valorUnd,
                v.quantidade * v.valorUnd
            });
            quantidadeVendida += v.quantidade;
            valVendido += (v.quantidade * v.valorUnd);
        }

        this.valorVendido.setText("R$" + String.valueOf(valVendido) + " - ");
        quantiaVendida.setText(String.valueOf(quantidadeVendida) + " Produtos vendidos");

    }

    
    public void prepararPDF() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Date dataInicio = dateChooserInicio.getDate();
        Date dataFim = dateChooserFim.getDate();
        // Formata as datas
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        String dataA1 = (dataInicio != null) ? formatador.format(dataInicio) : "Data de Início Não Selecionada";
        String data2 = (dataFim != null) ? formatador.format(dataFim) : "Data de Fim Não Selecionada";

        // Cria o texto do cabeçalho com as datas
        String dataPasssar = "Produtos vendidos de " + dataA1 + " até " + data2;
        
        List<List<Object>> dadosDasColunas = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            List<Object> dadosLinha = new ArrayList<>();

            Object dadoColuna1 = model.getValueAt(i, 0); // 2ª coluna
            Object dadoColuna2 = model.getValueAt(i, 1); // 2ª coluna
            Object dadoColuna3 = model.getValueAt(i, 2); // 3ª coluna
            Object dadoColuna4 = model.getValueAt(i, 3); // 4ª coluna
            Object dadoColuna5 = model.getValueAt(i, 4); // 5ª coluna

            // Adiciona os dados da linha à lista
            dadosLinha.add(dadoColuna1);
            dadosLinha.add(dadoColuna2);
            dadosLinha.add(dadoColuna3);
            dadosLinha.add(dadoColuna4);
            dadosLinha.add(dadoColuna5);

            // Adiciona os dados da linha à lista principal
            dadosDasColunas.add(dadosLinha);
        }

        Relatorio relatorioPdfSimples = new RelatorioProdutosPDF();
        relatorioPdfSimples.gerarCabecalho(dataPasssar);
        relatorioPdfSimples.gerarCorpo(dadosDasColunas);
        relatorioPdfSimples.gerarRodape();
        relatorioPdfSimples.imprimir();
    }
    
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConfereProdutos().setVisible(true));
    }
}
