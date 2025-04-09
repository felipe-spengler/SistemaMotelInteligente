/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.telas;

import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.pdf.Relatorio;
import com.motelinteligente.pdf.RelatorioCaixaPDF;
import com.motelinteligente.pdf.RelatorioLocacoesPDF;
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

public class ConfereLocacoes extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JDateChooser dateChooserInicio;
    private JDateChooser dateChooserFim;
    private JButton buscarButton, btnGerarPDF, btnMaisDetalhes;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private int contaResultados;
    private JLabel contaLabel;

    public ConfereLocacoes() {
        setTitle("Consulta de Locações");
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
        tableModel = new DefaultTableModel(new Object[]{"Inicio", "Fim", "N° Quarto", "Valor Quarto", "Valor Consumo", "Valor Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // nenhuma célula será editável
            }
        };
        table = new JTable(tableModel);
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
        btnMaisDetalhes = new JButton("Mais Detalhes");
        btnMaisDetalhes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/search_locate_find_6278.png")));

        leftPanel.add(btnGerarPDF);
        leftPanel.add(btnMaisDetalhes);

// Painel para o label à direita
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        contaLabel = new JLabel("Nenhum resultado");
        rightPanel.add(contaLabel);

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

        btnMaisDetalhes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int linhaSelecionada = table.getSelectedRow();
                if (linhaSelecionada != -1) {
                    Object id = table.getValueAt(linhaSelecionada, 0);
                    Timestamp horaInicio = Timestamp.valueOf(id.toString());
                    String consultaSQL = "SELECT idlocacao FROM registralocado WHERE horainicio = ?";

                    try (
                            java.sql.Connection link = new fazconexao().conectar(); PreparedStatement stmt = link.prepareStatement(consultaSQL)) {
                        stmt.setTimestamp(1, horaInicio);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                int idSelecionado = resultSet.getInt("idlocacao");
                                new VerDadosLocacao(idSelecionado).setVisible(true);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Erro ao consultar locação: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Nenhuma locação selecionada!");
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

        table.setRowHeight(30);
        table.getColumn(table.getColumnName(0)).setPreferredWidth(200);
        table.getColumn(table.getColumnName(1)).setPreferredWidth(200);
        table.getColumn(table.getColumnName(2)).setPreferredWidth(80);
        table.getColumn(table.getColumnName(3)).setPreferredWidth(100);
        table.getColumn(table.getColumnName(4)).setPreferredWidth(100);
        table.getColumn(table.getColumnName(5)).setPreferredWidth(120);

        int caixaAtual = 0;
        model.setNumRows(0);
        fquartos quartosdao = new fquartos();
        //carregar os dados
        contaResultados = 0;
        for (int id : idList) {
            if (quartosdao.getIdCaixa(id) != caixaAtual) {
                model.addRow(new Object[]{
                    "Caixa " + quartosdao.getIdCaixa(id)});
                caixaAtual = quartosdao.getIdCaixa(id);
            }
            String consultaSQL = "SELECT * FROM registralocado WHERE idlocacao = " + id + " AND valorquarto IS NOT NULL";
            Connection link = null;
            float valQ = 0, valC = 0;
            try {
                link = new fazconexao().conectar();
                Statement statement = link.createStatement();
                ResultSet resultado = statement.executeQuery(consultaSQL);
                if (resultado.next()) {
                    contaResultados++;
                    int idlocacao = resultado.getInt("idlocacao");
                    valQ = resultado.getFloat("valorquarto");
                    valC = resultado.getFloat("valorconsumo");
                    model.addRow(new Object[]{
                        resultado.getTimestamp("horainicio"),
                        resultado.getTimestamp("horafim"),
                        resultado.getInt("numquarto"),
                        resultado.getFloat("valorquarto"),
                        resultado.getFloat("valorconsumo"),
                        resultado.getFloat("valorquarto") + resultado.getFloat("valorconsumo"),});
                }
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(null, e);
            } finally {
                try {
                    if (link != null && !link.isClosed()) {
                        link.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            contaLabel.setText(String.format("Mostrando %d resultados", contaResultados));
        }

        //mostraResultados
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Verifica se foi um clique duplo
                    int row = table.getSelectedRow(); // Obtém a linha selecionada
                    // Faça algo com a linha selecionada, por exemplo:
                    System.out.println("Clique duplo na linha: " + row);
                }
            }
        });

// Associe o renderizador personalizado à coluna que você deseja
        int columnIndex = 0; // Índice da coluna que deseja aplicar o renderizador (por exemplo, a primeira coluna)

        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new ConfereLocacoes.CustomCellRenderer());

    }

    public void prepararPDF() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // Obtém as datas do JDateChooser
        Date dataInicio = dateChooserInicio.getDate();
        Date dataFim = dateChooserFim.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataFim);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        dataFim = cal.getTime();
        // Formata as datas
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        String dataA1 = (dataInicio != null) ? formatador.format(dataInicio) : "Data de Início Não Selecionada";
        String data2 = (dataFim != null) ? formatador.format(dataFim) : "Data de Fim Não Selecionada";

        // Cria o texto do cabeçalho com as datas
        String dataPasssar = "Locações de " + dataA1 + " até " + data2;

        // Coleta os dados das colunas da tabela
        List<List<Object>> dadosDasColunas = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            List<Object> dadosLinha = new ArrayList<>();

            Object dadoColuna1 = model.getValueAt(i, 0); // 1ª coluna
            Object dadoColuna2 = model.getValueAt(i, 1); // 2ª coluna
            Object dadoColuna3 = model.getValueAt(i, 2); // 3ª coluna
            Object dadoColuna4 = model.getValueAt(i, 3); // 4ª coluna
            Object dadoColuna5 = model.getValueAt(i, 4); // 5ª coluna
            Object dadoColuna6 = model.getValueAt(i, 5); // 6ª coluna
            

            // Adiciona os dados da linha à lista
            dadosLinha.add(dadoColuna1);
            dadosLinha.add(dadoColuna2);
            dadosLinha.add(dadoColuna3);
            dadosLinha.add(dadoColuna4);
            dadosLinha.add(dadoColuna5);
            dadosLinha.add(dadoColuna6);

            // Adiciona os dados da linha à lista principal
            dadosDasColunas.add(dadosLinha);
        }

        // Gera o relatório PDF
        Relatorio relatorioPdfSimples = new RelatorioLocacoesPDF();
        relatorioPdfSimples.gerarCabecalho(dataPasssar);
        relatorioPdfSimples.gerarCorpo(dadosDasColunas);
        relatorioPdfSimples.gerarRodape();
        relatorioPdfSimples.imprimir();
    }

    class CustomCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

        public CustomCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null && value.toString().startsWith("Caixa")) {
                setText("<html><b style='font-size: 14px; background-color: yellow;'>" + value + "</b></html>");
            }

            return c;
        }
    }

    public void mostraDetalhes(int idCaixa) {
        JFrame detalhesFrame = new JFrame("Detalhes do Caixa" + idCaixa);
        detalhesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detalhesFrame.setSize(1200, 800);

        String query = "SELECT * FROM registralocado WHERE idcaixaatual = " + idCaixa;
        Connection link = new fazconexao().conectar();
        try (PreparedStatement preparedStatement = link.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            // Cria uma lista para armazenar os resultados
            List<Object[]> dados = new ArrayList<>();

            // Itera pelos resultados e adiciona os dados à lista
            while (resultSet.next()) {
                // Obtém os valores das colunas do ResultSet
                int idLocacao = resultSet.getInt("idlocacao");
                int numeroQuarto = resultSet.getInt("numquarto");
                String horaInicio = resultSet.getString("horainicio");
                String horaFim = resultSet.getString("horafim");
                float valorQuarto = resultSet.getFloat("valorquarto");
                float valorConsumo = resultSet.getFloat("Valorconsumo");
                float recebidoDinheiro = resultSet.getFloat("pagodinheiro");
                float recebidoPix = resultSet.getFloat("pagopix");
                float recebidoCartao = resultSet.getFloat("pagocartao");

                // Adiciona os valores em um array de objetos
                Object[] linha = {idLocacao, numeroQuarto, horaInicio, horaFim, valorQuarto, valorConsumo, recebidoDinheiro, recebidoPix, recebidoCartao};
                dados.add(linha);
            }
            String[] colunas = {"ID Locação", "Número Quarto", "Hora Início", "Hora Fim", "Valor Quarto", "Valor Consumo", "Recebido Dinheiro", "Recebido Pix", "Recebido Cartão"};

            Object[][] data = dados.toArray(new Object[0][]);

            JTable tabelaDetalhes = new JTable(data, colunas);
            // Define a altura das linhas
            tabelaDetalhes.setRowHeight(30);

// Define a largura das colunas
            tabelaDetalhes.getColumnModel().getColumn(0).setPreferredWidth(60);
            tabelaDetalhes.getColumnModel().getColumn(1).setPreferredWidth(60);
            tabelaDetalhes.getColumnModel().getColumn(2).setPreferredWidth(200);
            tabelaDetalhes.getColumnModel().getColumn(3).setPreferredWidth(200);
            tabelaDetalhes.getColumnModel().getColumn(4).setPreferredWidth(120);
            tabelaDetalhes.getColumnModel().getColumn(5).setPreferredWidth(120);
            tabelaDetalhes.getColumnModel().getColumn(6).setPreferredWidth(120);
            tabelaDetalhes.getColumnModel().getColumn(7).setPreferredWidth(120);
            tabelaDetalhes.getColumnModel().getColumn(8).setPreferredWidth(120);

            JScrollPane scrollPane = new JScrollPane(tabelaDetalhes);
            detalhesFrame.add(scrollPane);
            detalhesFrame.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConfereLocacoes().setVisible(true));
    }
}
