package com.motelinteligente.telas;

import com.motelinteligente.dados.fazconexao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class FluxoCaixaDialog extends JDialog {

    private final JTextField txtDataInicio;
    private final JTextField txtDataFim;
    private final javax.swing.JComboBox<String> comboPeriodo;
    
    private final JTable tabelaResumo;
    private final DefaultTableModel modeloResumo;
    
    private final JTable tabelaDetalhes;
    private final DefaultTableModel modeloDetalhes;

    private final JLabel lblTotalReceitas;
    private final JLabel lblTotalDespesas;
    private final JLabel lblSaldoLiquido;

    public FluxoCaixaDialog(JFrame parent) {
        super(parent, "Fluxo de Caixa Mensal", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(800, 580);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Painel de Filtros (Norte)
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        painelFiltros.setBackground(new Color(248, 250, 252));
        painelFiltros.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        painelFiltros.add(new JLabel("Filtro:"));
        comboPeriodo = new javax.swing.JComboBox<>(new String[] {
            "Este Mês", "Mês Passado", "Últimos 30 Dias", "Últimos 90 Dias", "Personalizado"
        });
        comboPeriodo.addActionListener(e -> ajustarPeriodoCombo());
        painelFiltros.add(comboPeriodo);

        painelFiltros.add(new JLabel("Período de:"));
        txtDataInicio = new JTextField(10);
        painelFiltros.add(txtDataInicio);

        painelFiltros.add(new JLabel("Até:"));
        txtDataFim = new JTextField(10);
        painelFiltros.add(txtDataFim);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setBackground(new Color(33, 150, 243));
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.addActionListener(e -> carregarFluxo());
        painelFiltros.add(btnFiltrar);

        // Preencher datas padrão (Mês Atual)
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy");
        String dataFimStr = sdfData.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String dataInicioStr = sdfData.format(cal.getTime());
        txtDataInicio.setText(dataInicioStr);
        txtDataFim.setText(dataFimStr);
        txtDataInicio.setEditable(false);
        txtDataFim.setEditable(false);

        // Painel de Resumos (Sul)
        JPanel painelResumo = new JPanel(new GridLayout(1, 3, 15, 0));
        painelResumo.setBorder(new EmptyBorder(10, 15, 15, 15));
        painelResumo.setBackground(Color.WHITE);

        JPanel cardReceitas = criarCard("Total Faturamento", new Color(240, 253, 244));
        lblTotalReceitas = (JLabel) cardReceitas.getClientProperty("lblValor");
        lblTotalReceitas.setForeground(new Color(22, 163, 74));
        painelResumo.add(cardReceitas);

        JPanel cardDespesas = criarCard("Total Despesas", new Color(254, 242, 242));
        lblTotalDespesas = (JLabel) cardDespesas.getClientProperty("lblValor");
        lblTotalDespesas.setForeground(new Color(220, 38, 38));
        painelResumo.add(cardDespesas);

        JPanel cardSaldo = criarCard("Resultado Líquido", new Color(240, 249, 255));
        lblSaldoLiquido = (JLabel) cardSaldo.getClientProperty("lblValor");
        lblSaldoLiquido.setForeground(new Color(2, 132, 199));
        painelResumo.add(cardSaldo);

        // Abas Centralizadoras
        JTabbedPane abas = new JTabbedPane();

        // Aba 1: Resumo Gerencial (DRE)
        modeloResumo = new DefaultTableModel(new Object[] {"Categoria / Centro de Custo", "Valor (R$)", "Participação (%)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaResumo = new JTable(modeloResumo);
        tabelaResumo.setRowHeight(26);
        tabelaResumo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaResumo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollResumo = new JScrollPane(tabelaResumo);
        scrollResumo.setBorder(BorderFactory.createEmptyBorder());
        abas.addTab("Resumo Gerencial (DRE)", scrollResumo);

        // Aba 2: Lançamentos Detalhados
        modeloDetalhes = new DefaultTableModel(new Object[] {"Data/Hora", "Tipo", "Descrição", "Valor (R$)", "Forma Pgto", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaDetalhes = new JTable(modeloDetalhes);
        tabelaDetalhes.setRowHeight(24);
        tabelaDetalhes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollDetalhes = new JScrollPane(tabelaDetalhes);
        scrollDetalhes.setBorder(BorderFactory.createEmptyBorder());
        abas.addTab("Lançamentos Detalhados", scrollDetalhes);

        add(painelFiltros, BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(painelResumo, BorderLayout.SOUTH);

        carregarFluxo();
    }

    private JPanel criarCard(String titulo, Color fundo) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(fundo);
        card.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        card.setPreferredSize(new Dimension(200, 70));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitulo.setForeground(Color.GRAY);
        card.add(lblTitulo, gbc);

        gbc.gridy = 1;
        JLabel lblValor = new JLabel("R$ 0,00");
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(lblValor, gbc);

        card.putClientProperty("lblValor", lblValor);
        return card;
    }

    private void carregarFluxo() {
        modeloResumo.setRowCount(0);
        modeloDetalhes.setRowCount(0);

        String dataIniStr = txtDataInicio.getText().trim();
        String dataFimStr = txtDataFim.getText().trim();

        SimpleDateFormat parserEntrada = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat parserBanco = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date dataIni = parserEntrada.parse(dataIniStr);
            Date dataFim = parserEntrada.parse(dataFimStr);

            String tInicio = parserBanco.format(dataIni) + " 00:00:00";
            String tFim = parserBanco.format(dataFim) + " 23:59:59";

            float faturamentoTotal = 0f;
            float totalDespesasVal = 0f;

            // Mapas para agrupar Faturamento por forma de pagamento e Despesas por Categoria
            Map<String, Float> faturamentoMeios = new HashMap<>();
            faturamentoMeios.put("Dinheiro", 0f);
            faturamentoMeios.put("Pix", 0f);
            faturamentoMeios.put("Cartão", 0f);
            faturamentoMeios.put("Outro", 0f);

            Map<String, Float> despesasCategorias = new HashMap<>();

            try (Connection link = new fazconexao().conectar()) {
                // 1. Consultar Locações (Receita)
                String sqlLocacoes = "SELECT horafim, pagodinheiro, pagopix, pagocartao, numquarto FROM registralocado WHERE horafim >= ? AND horafim <= ? AND valorquarto IS NOT NULL ORDER BY horafim";
                try (PreparedStatement stmt = link.prepareStatement(sqlLocacoes)) {
                    stmt.setString(1, tInicio);
                    stmt.setString(2, tFim);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            float pgDinheiro = rs.getFloat("pagodinheiro");
                            float pgPix = rs.getFloat("pagopix");
                            float pgCartao = rs.getFloat("pagocartao");
                            float total = pgDinheiro + pgPix + pgCartao;

                            faturamentoTotal += total;
                            faturamentoMeios.put("Dinheiro", faturamentoMeios.get("Dinheiro") + pgDinheiro);
                            faturamentoMeios.put("Pix", faturamentoMeios.get("Pix") + pgPix);
                            faturamentoMeios.put("Cartão", faturamentoMeios.get("Cartão") + pgCartao);

                            modeloDetalhes.addRow(new Object[] {
                                rs.getTimestamp("horafim"),
                                "Locação",
                                "Quarto #" + rs.getInt("numquarto"),
                                String.format("+ R$ %.2f", total),
                                "Múltiplo",
                                "Concluído"
                            });
                        }
                    }
                }

                // 2. Consultar Vendas Avulsas (Receita)
                String sqlVendas = "SELECT horario, valortotal, descricao, formapagamento FROM vendas_avulsas WHERE horario >= ? AND horario <= ? AND tipo != 'adiantamento' ORDER BY horario";
                try (PreparedStatement stmt = link.prepareStatement(sqlVendas)) {
                    stmt.setString(1, tInicio);
                    stmt.setString(2, tFim);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            float total = rs.getFloat("valortotal");
                            String pgto = rs.getString("formapagamento");

                            faturamentoTotal += total;
                            if ("dinheiro".equalsIgnoreCase(pgto)) {
                                faturamentoMeios.put("Dinheiro", faturamentoMeios.get("Dinheiro") + total);
                            } else if ("pix".equalsIgnoreCase(pgto)) {
                                faturamentoMeios.put("Pix", faturamentoMeios.get("Pix") + total);
                            } else if ("credito".equalsIgnoreCase(pgto) || "debito".equalsIgnoreCase(pgto) || "cartao".equalsIgnoreCase(pgto)) {
                                faturamentoMeios.put("Cartão", faturamentoMeios.get("Cartão") + total);
                            } else {
                                faturamentoMeios.put("Outro", faturamentoMeios.get("Outro") + total);
                            }

                            modeloDetalhes.addRow(new Object[] {
                                rs.getTimestamp("horario"),
                                "Venda Avulsa",
                                rs.getString("descricao"),
                                String.format("+ R$ %.2f", total),
                                pgto.toUpperCase(),
                                "Concluído"
                            });
                        }
                    }
                }

                // 3. Consultar Retiradas/Sangrias (Despesa)
                String sqlRetiradas = "SELECT horario, valor, quem, justificativa FROM retiradas_caixa WHERE horario >= ? AND horario <= ? ORDER BY horario";
                try (PreparedStatement stmt = link.prepareStatement(sqlRetiradas)) {
                    stmt.setString(1, tInicio);
                    stmt.setString(2, tFim);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            float total = rs.getFloat("valor");
                            totalDespesasVal += total;

                            despesasCategorias.put("Retiradas/Sangrias", despesasCategorias.getOrDefault("Retiradas/Sangrias", 0f) + total);

                            modeloDetalhes.addRow(new Object[] {
                                rs.getTimestamp("horario"),
                                "Retirada Caixa",
                                "Quem: " + rs.getString("quem") + " - " + rs.getString("justificativa"),
                                String.format("- R$ %.2f", total),
                                "DINHEIRO",
                                "Pago"
                            });
                        }
                    }
                }

                // 4. Consultar Despesas (Despesa)
                String sqlDespesas = "SELECT horario, valor, descricao, categoria, formapagamento, status FROM despesas WHERE horario >= ? AND horario <= ? ORDER BY horario";
                try (PreparedStatement stmt = link.prepareStatement(sqlDespesas)) {
                    stmt.setString(1, tInicio);
                    stmt.setString(2, tFim);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            float total = rs.getFloat("valor");
                            String cat = rs.getString("categoria");
                            totalDespesasVal += total;

                            despesasCategorias.put(cat, despesasCategorias.getOrDefault(cat, 0f) + total);

                            modeloDetalhes.addRow(new Object[] {
                                rs.getTimestamp("horario"),
                                "Despesa (" + cat + ")",
                                rs.getString("descricao"),
                                String.format("- R$ %.2f", total),
                                rs.getString("formapagamento").toUpperCase(),
                                rs.getString("status").toUpperCase()
                            });
                        }
                    }
                }
            }

            // --- POPULAR ABA 1: RESUMO GERENCIAL (DRE STYLE) ---
            modeloResumo.addRow(new Object[] {"FATURAMENTO (RECEITAS)", "", ""});
            for (Map.Entry<String, Float> entry : faturamentoMeios.entrySet()) {
                if (entry.getValue() > 0) {
                    float perc = faturamentoTotal > 0 ? (entry.getValue() / faturamentoTotal) * 100f : 0f;
                    modeloResumo.addRow(new Object[] {
                        "  " + entry.getKey().toUpperCase(),
                        String.format("%,.2f", entry.getValue()),
                        String.format("%.1f %%", perc)
                    });
                }
            }
            modeloResumo.addRow(new Object[] {"TOTAL FATURAMENTO", String.format("%,.2f", faturamentoTotal), "100.0 %"});
            modeloResumo.addRow(new Object[] {"", "", ""}); // Linha em branco

            modeloResumo.addRow(new Object[] {"DESPESAS POR CENTRO DE CUSTO", "", ""});
            for (Map.Entry<String, Float> entry : despesasCategorias.entrySet()) {
                float perc = faturamentoTotal > 0 ? (entry.getValue() / faturamentoTotal) * 100f : 0f;
                modeloResumo.addRow(new Object[] {
                    "  " + entry.getKey().toUpperCase(),
                    String.format("%,.2f", entry.getValue()),
                    String.format("%.1f %%", perc)
                });
            }
            float percDesp = faturamentoTotal > 0 ? (totalDespesasVal / faturamentoTotal) * 100f : 0f;
            modeloResumo.addRow(new Object[] {"TOTAL DESPESAS", String.format("%,.2f", totalDespesasVal), String.format("%.1f %%", percDesp)});
            modeloResumo.addRow(new Object[] {"", "", ""}); // Linha em branco

            float resultadoGeral = faturamentoTotal - totalDespesasVal;
            float percRes = faturamentoTotal > 0 ? (resultadoGeral / faturamentoTotal) * 100f : 0f;
            modeloResumo.addRow(new Object[] {"RESULTADO GERAL DO PERÍODO", String.format("%,.2f", resultadoGeral), String.format("%.1f %%", percRes)});

            // Atualizar os cartões inferiores
            lblTotalReceitas.setText(String.format("R$ %,.2f", faturamentoTotal));
            lblTotalDespesas.setText(String.format("R$ %,.2f", totalDespesasVal));
            lblSaldoLiquido.setText(String.format("R$ %,.2f", resultadoGeral));

            if (resultadoGeral >= 0) {
                lblSaldoLiquido.setForeground(new Color(22, 163, 74));
            } else {
                lblSaldoLiquido.setForeground(new Color(220, 38, 38));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do fluxo de caixa: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajustarPeriodoCombo() {
        String selecionado = comboPeriodo.getSelectedItem().toString();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if ("Este Mês".equals(selecionado)) {
            txtDataInicio.setEditable(false);
            txtDataFim.setEditable(false);
            txtDataFim.setText(sdf.format(cal.getTime()));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            txtDataInicio.setText(sdf.format(cal.getTime()));
        } else if ("Mês Passado".equals(selecionado)) {
            txtDataInicio.setEditable(false);
            txtDataFim.setEditable(false);
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            txtDataInicio.setText(sdf.format(cal.getTime()));
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            txtDataFim.setText(sdf.format(cal.getTime()));
        } else if ("Últimos 30 Dias".equals(selecionado)) {
            txtDataInicio.setEditable(false);
            txtDataFim.setEditable(false);
            txtDataFim.setText(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, -30);
            txtDataInicio.setText(sdf.format(cal.getTime()));
        } else if ("Últimos 90 Dias".equals(selecionado)) {
            txtDataInicio.setEditable(false);
            txtDataFim.setEditable(false);
            txtDataFim.setText(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, -90);
            txtDataInicio.setText(sdf.format(cal.getTime()));
        } else {
            // Personalizado
            txtDataInicio.setEditable(true);
            txtDataFim.setEditable(true);
        }

        carregarFluxo();
    }
}
