package com.motelinteligente.telas;

import com.motelinteligente.dados.fazconexao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

public class CadastroPortoes extends JFrame {

    private JTable tabelaPortoes;
    private DefaultTableModel modelo;

    public CadastroPortoes() {
        setTitle("Cadastro de Portões");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15)); // Margem de 15px

        modelo = new DefaultTableModel();
        modelo.addColumn("Portão");
        modelo.addColumn("Código");
        modelo.addColumn("Bit Length");

        // Carregar dados do banco
        carregarDados();

        tabelaPortoes = new JTable(modelo);
        tabelaPortoes.setFont(new Font("Arial", Font.PLAIN, 14)); // Tamanho da fonte
        tabelaPortoes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tabelaPortoes.getTableHeader().setBackground(Color.BLACK);
        tabelaPortoes.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tabelaPortoes);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnSalvar = new JButton("Salvar");
        JButton btnVoltar = new JButton("Voltar");

        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarDados();
            }
        });

        btnVoltar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fecha a janela
            }
        });

        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnVoltar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void carregarDados() {
        String consultaPortoes = "SELECT portao, codigo, bitLength FROM portoes";
        String consultaQuartos = "SELECT numeroquarto FROM quartos";
        Connection link = null;

        try {
            link = new fazconexao().conectar();

            // Carregar dados dos portões
            PreparedStatement statementPortoes = link.prepareStatement(consultaPortoes);
            ResultSet resultSetPortoes = statementPortoes.executeQuery();

            boolean entradaExists = false;
            boolean saidaExists = false;
            boolean cortinaExists = false;

            // Verifica se já existem registros para entrada, saída e cortina
            while (resultSetPortoes.next()) {
                int portao = resultSetPortoes.getInt("portao");
                int codigo = resultSetPortoes.getInt("codigo");
                int bitLength = resultSetPortoes.getInt("bitLength");

                switch (portao) {
                    case 888:
                        entradaExists = true;
                        modelo.addRow(new Object[]{"Entrada", codigo, bitLength});
                        break;
                    case 999:
                        saidaExists = true;
                        modelo.addRow(new Object[]{"Saída", codigo, bitLength});
                        break;
                    case 777:
                        cortinaExists = true;
                        modelo.addRow(new Object[]{"Cortina", codigo, bitLength});
                        break;
                }
            }

            // Adiciona linhas para entrada, saída e cortina se não existirem
            if (!entradaExists) {
                modelo.addRow(new Object[]{"Entrada", "", ""});
            }
            if (!saidaExists) {
                modelo.addRow(new Object[]{"Saída", "", ""});
            }
            if (!cortinaExists) {
                modelo.addRow(new Object[]{"Cortina", "", ""});
            }

            // Carregar números dos quartos
            ArrayList<Integer> numerosQuartos = new ArrayList<>();
            PreparedStatement statementQuartos = link.prepareStatement(consultaQuartos);
            ResultSet resultSetQuartos = statementQuartos.executeQuery();
            while (resultSetQuartos.next()) {
                int numeroQuarto = resultSetQuartos.getInt("numeroquarto");
                numerosQuartos.add(numeroQuarto);
            }

            // Ordenar os números dos quartos
            Collections.sort(numerosQuartos);
            for (int numeroQuarto : numerosQuartos) {
                modelo.addRow(new Object[]{"Quarto " + numeroQuarto, "", ""});
            }

            resultSetPortoes.close();
            resultSetQuartos.close();
            statementPortoes.close();
            statementQuartos.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar dados: " + e.getMessage());
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void salvarDados() {
        try {
            for (int i = 0; i < modelo.getRowCount(); i++) {
                String tipo = (String) modelo.getValueAt(i, 0);
                String codigo = (String) modelo.getValueAt(i, 1);
                String bitLength = (String) modelo.getValueAt(i, 2);

                // Verifica se todos os campos estão preenchidos
                if (!codigo.isEmpty() && !bitLength.isEmpty()) {
                    salvarNoBanco(tipo, codigo, bitLength);
                }
            }
            JOptionPane.showMessageDialog(this, "Dados salvos com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar dados: " + e.getMessage());
        }
    }

    private void salvarNoBanco(String tipo, String codigo, String bitLength) {
    String updateSQL = "UPDATE portoes SET codigo = ?, bitLength = ? WHERE portao = ?";
    String insertSQL = "INSERT INTO portoes (portao, codigo, bitLength) VALUES (?, ?, ?)";
    Connection link = null;
    
    try {
        link = new fazconexao().conectar();
        PreparedStatement statement = link.prepareStatement(updateSQL);
        
        // Definindo os parâmetros para o UPDATE
        statement.setInt(1, Integer.parseInt(codigo));
        statement.setInt(2, Integer.parseInt(bitLength));
        statement.setInt(3, getPortaoTipo(tipo));

        // Mostrar a consulta SQL e os parâmetros
        System.out.println("Executando SQL: " + updateSQL);
        System.out.println("Valores: " + codigo + ", " + bitLength + ", " + getPortaoTipo(tipo));

        // Executar a atualização
        int n = statement.executeUpdate();
        
        // Se nenhum registro foi atualizado, tenta inserir
        if (n == 0) {
            System.out.println("Nenhum registro atualizado, realizando INSERT.");
            statement.close(); // Fecha o statement anterior

            // Preparar o INSERT
            statement = link.prepareStatement(insertSQL);
            statement.setInt(1, getPortaoTipo(tipo));
            statement.setInt(2, Integer.parseInt(codigo));
            statement.setInt(3, Integer.parseInt(bitLength));

            System.out.println("Executando SQL: " + insertSQL);
            System.out.println("Valores: " + getPortaoTipo(tipo) + ", " + codigo + ", " + bitLength);

            // Executar o INSERT
            statement.executeUpdate();
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, e);
    } finally {
        try {
            if (link != null && !link.isClosed()) {
                link.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
    private int getPortaoTipo(String tipo) {
        switch (tipo) {
            case "Entrada":
                return 888; // ID para entrada
            case "Saída":
                return 999; // ID para saída
            case "Cortina":
                return 777; // ID para cortina
            default:
                // Para números de quartos, retorna o número do quarto
                return Integer.parseInt(tipo.split(" ")[1]); // Extrai o número do quarto
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CadastroPortoes cadastro = new CadastroPortoes();
            cadastro.setVisible(true);
        });
    }
}
