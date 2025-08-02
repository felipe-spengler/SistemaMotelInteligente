/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.telas;

import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.pdf.Relatorio;
import com.motelinteligente.pdf.RelatorioCaixaPDF;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;

public class ConfereCaixa extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JDateChooser dateChooserInicio;
    private JDateChooser dateChooserFim;
    private JLabel labelMostraResultado;
    private JButton buscarButton, btnGerarPDF, btnMaisDetalhes;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ConfereCaixa() {
        setTitle("Consulta de Caixas");
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
        tableModel = new DefaultTableModel(new Object[]{"ID", "Data Abertura", "Usuário Abertura", "Valor Abertura", "Data Fechamento", "Usuário Fechamento", "Valor Fechamento"}, 0);
        table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(255, 255, 255));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        // Adicionando componentes à janela
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Painel inferior para botões
        JPanel bottomPanel = new JPanel(new FlowLayout());

        // Botão Gerar PDF
        btnGerarPDF = new JButton("Gerar PDF");
        btnGerarPDF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/pdf_icon.png")));
        bottomPanel.add(btnGerarPDF);

        // Botão Mais Detalhes
        btnMaisDetalhes = new JButton("Mais Detalhes");
        btnMaisDetalhes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/search_locate_find_6278.png")));
        bottomPanel.add(btnMaisDetalhes);

        add(bottomPanel, BorderLayout.SOUTH);

        // Ações dos botões
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarCaixas();
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
                    int idSelecionado = Integer.valueOf(id.toString());
                    mostraDetalhes(idSelecionado);
                } else {
                    JOptionPane.showMessageDialog(null, "Nenhum CAIXA Selecionado!");
                }
            }
        });
    }

    // Método para carregar os caixas abertos entre as datas fornecidas
    private void carregarCaixas() {
        // Verifica se os campos estão vazios ou nulos
        String textoInicio = ((JTextField) dateChooserInicio.getDateEditor().getUiComponent()).getText().trim();
        String textoFim = ((JTextField) dateChooserFim.getDateEditor().getUiComponent()).getText().trim();

        if (textoInicio.isEmpty() && textoFim.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione pelo menos uma das datas (início ou fim)", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date dataInicio = null;
        Date dataFim = null;

        if (!textoInicio.isEmpty()) {
            dataInicio = dateChooserInicio.getDate();
        }
        if (!textoFim.isEmpty()) {
            dataFim = dateChooserFim.getDate();

            // Ajusta dataFim para 23:59:59.999
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataFim);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            dataFim = cal.getTime();
        }

        Connection link = new fazconexao().conectar();
        try {
            tableModel.setRowCount(0); // Limpar tabela antes de carregar novos dados

            // Monta a query dinamicamente
            StringBuilder query = new StringBuilder("SELECT * FROM caixa WHERE 1=1 ");
            if (dataInicio != null) {
                query.append("AND horaabre >= ? ");
            }
            if (dataFim != null) {
                query.append("AND horaabre <= ? ");
            }

            PreparedStatement statement = link.prepareStatement(query.toString());

            // Define os parâmetros dinamicamente
            int paramIndex = 1;
            if (dataInicio != null) {
                statement.setTimestamp(paramIndex++, new Timestamp(dataInicio.getTime()));
            }
            if (dataFim != null) {
                statement.setTimestamp(paramIndex++, new Timestamp(dataFim.getTime()));
            }

            System.out.println("QUERY => " + statement.toString());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                Timestamp horaAbre = resultSet.getTimestamp("horaabre");
                String usuarioAbre = resultSet.getString("usuarioabre");
                float saldoAbre = resultSet.getFloat("saldoabre");
                Timestamp horaFecha = resultSet.getTimestamp("horafecha");
                String usuarioFecha = resultSet.getString("usuariofecha");
                float saldoFecha = resultSet.getFloat("saldofecha");

                // Formatar as datas
                String horaAbreFormatted = dateFormat.format(horaAbre);
                String horaFechaFormatted = (horaFecha != null) ? dateFormat.format(horaFecha) : "";

                tableModel.addRow(new Object[]{id, horaAbreFormatted, usuarioAbre, saldoAbre, horaFechaFormatted, usuarioFecha, saldoFecha});
            }

            // Centralizar colunas
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados dos caixas", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void prepararPDF() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // Obtém as datas do JDateChooser
        Date dataInicio = dateChooserInicio.getDate();
        Date dataFim = dateChooserFim.getDate();

        // Formata as datas
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        String dataA1 = (dataInicio != null) ? formatador.format(dataInicio) : "Data de Início Não Selecionada";
        String data2 = (dataFim != null) ? formatador.format(dataFim) : "Data de Fim Não Selecionada";

        // Cria o texto do cabeçalho com as datas
        String dataPasssar = "Caixas Abertos de " + dataA1 + " até " + data2;

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
            Object dadoColuna7 = model.getValueAt(i, 6); // 7ª coluna

            // Adiciona os dados da linha à lista
            dadosLinha.add(dadoColuna1);
            dadosLinha.add(dadoColuna2);
            dadosLinha.add(dadoColuna3);
            dadosLinha.add(dadoColuna4);
            dadosLinha.add(dadoColuna5);
            dadosLinha.add(dadoColuna6);
            dadosLinha.add(dadoColuna7);

            // Adiciona os dados da linha à lista principal
            dadosDasColunas.add(dadosLinha);
        }

        // Gera o relatório PDF
        Relatorio relatorioPdfSimples = new RelatorioCaixaPDF();
        relatorioPdfSimples.gerarCabecalho(dataPasssar);
        relatorioPdfSimples.gerarCorpo(dadosDasColunas);
        relatorioPdfSimples.gerarRodape();
        relatorioPdfSimples.imprimir();
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
        SwingUtilities.invokeLater(() -> new ConfereCaixa().setVisible(true));
    }
}
