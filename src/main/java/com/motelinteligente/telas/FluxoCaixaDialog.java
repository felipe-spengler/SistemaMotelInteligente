package com.motelinteligente.telas;

import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fcaixa;
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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.io.FileOutputStream;
import java.io.File;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// OpenPDF imports
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class FluxoCaixaDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(FluxoCaixaDialog.class);

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
        setSize(850, 600);
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

        JButton btnExportar = new JButton("Exportar PDF");
        btnExportar.setBackground(new Color(76, 175, 80));
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setFocusPainted(false);
        btnExportar.addActionListener(e -> exportarParaPdf());
        painelFiltros.add(btnExportar);

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
        modeloDetalhes = new DefaultTableModel(new Object[] {"ID", "Data/Hora", "Tipo", "Descrição", "Valor (R$)", "Forma Pgto", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaDetalhes = new JTable(modeloDetalhes);
        tabelaDetalhes.setRowHeight(24);
        tabelaDetalhes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Esconder a coluna ID
        tabelaDetalhes.getColumnModel().getColumn(0).setMinWidth(0);
        tabelaDetalhes.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelaDetalhes.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollDetalhes = new JScrollPane(tabelaDetalhes);
        scrollDetalhes.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel painelDetalhesTab = new JPanel(new BorderLayout());
        painelDetalhesTab.add(scrollDetalhes, BorderLayout.CENTER);

        JPanel painelBotoesTab = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        painelBotoesTab.setBackground(Color.WHITE);

        JButton btnEditar = new JButton("Editar Despesa");
        btnEditar.setBackground(new Color(255, 152, 0));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarDespesaSelecionada());

        JButton btnExcluir = new JButton("Excluir Lançamento");
        btnExcluir.setBackground(new Color(244, 67, 54));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setFocusPainted(false);
        btnExcluir.addActionListener(e -> excluirLancamentoSelecionado());

        painelBotoesTab.add(btnEditar);
        painelBotoesTab.add(btnExcluir);
        painelDetalhesTab.add(painelBotoesTab, BorderLayout.SOUTH);

        abas.addTab("Lançamentos de Despesas", painelDetalhesTab);

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
                // 1. Consultar Locações (Receita para soma total)
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
                        }
                    }
                }

                // 2. Consultar Vendas Avulsas (Receita para soma total)
                String sqlVendas = "SELECT valortotal, formapagamento FROM vendas_avulsas WHERE horario >= ? AND horario <= ? AND tipo != 'adiantamento'";
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
                        }
                    }
                }

                // 3. Consultar Retiradas/Sangrias (Despesa)
                String sqlRetiradas = "SELECT id, horario, valor, quem, justificativa FROM retiradas_caixa WHERE horario >= ? AND horario <= ? ORDER BY horario";
                try (PreparedStatement stmt = link.prepareStatement(sqlRetiradas)) {
                    stmt.setString(1, tInicio);
                    stmt.setString(2, tFim);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            float total = rs.getFloat("valor");
                            totalDespesasVal += total;

                            despesasCategorias.put("Retiradas/Sangrias", despesasCategorias.getOrDefault("Retiradas/Sangrias", 0f) + total);

                            modeloDetalhes.addRow(new Object[] {
                                id,
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
                String sqlDespesas = "SELECT id, horario, valor, descricao, categoria, formapagamento, status FROM despesas WHERE horario >= ? AND horario <= ? ORDER BY horario";
                try (PreparedStatement stmt = link.prepareStatement(sqlDespesas)) {
                    stmt.setString(1, tInicio);
                    stmt.setString(2, tFim);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            float total = rs.getFloat("valor");
                            String cat = rs.getString("categoria");
                            totalDespesasVal += total;

                            despesasCategorias.put(cat, despesasCategorias.getOrDefault(cat, 0f) + total);

                            modeloDetalhes.addRow(new Object[] {
                                id,
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

    private void editarDespesaSelecionada() {
        int selectedRow = tabelaDetalhes.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa para editar.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tipo = String.valueOf(tabelaDetalhes.getValueAt(selectedRow, 2)); // Tipo
        if (!tipo.startsWith("Despesa")) {
            JOptionPane.showMessageDialog(this, "Apenas lançamentos do tipo 'Despesa' podem ser editados por aqui.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (Integer) tabelaDetalhes.getValueAt(selectedRow, 0);
        String descricao = String.valueOf(tabelaDetalhes.getValueAt(selectedRow, 3));
        String valorStr = String.valueOf(tabelaDetalhes.getValueAt(selectedRow, 4))
                            .replace("- R$ ", "")
                            .replace("+ R$ ", "")
                            .replace(",", ".")
                            .trim();
        float valor = Float.parseFloat(valorStr);
        String pgto = String.valueOf(tabelaDetalhes.getValueAt(selectedRow, 5));
        String status = String.valueOf(tabelaDetalhes.getValueAt(selectedRow, 6));
        
        // Extract category from type: "Despesa (Categoria)"
        String categoria = "Outros";
        if (tipo.contains("(") && tipo.contains(")")) {
            categoria = tipo.substring(tipo.indexOf("(") + 1, tipo.indexOf(")"));
        }
        
        // Check if has a caixa (idcaixa)
        boolean doCaixa = false;
        try (Connection link = new fazconexao().conectar();
             PreparedStatement stmt = link.prepareStatement("SELECT idcaixa FROM despesas WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int cx = rs.getInt("idcaixa");
                    if (!rs.wasNull() && cx > 0) {
                        doCaixa = true;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Erro ao verificar idcaixa: ", ex);
        }
        
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DespesaDialog dlg = new DespesaDialog(parentFrame, id, descricao, categoria, valor, pgto, status, doCaixa);
        dlg.setVisible(true);
        carregarFluxo();
    }

    private void excluirLancamentoSelecionado() {
        int selectedRow = tabelaDetalhes.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um lançamento para excluir.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (Integer) tabelaDetalhes.getValueAt(selectedRow, 0);
        String tipo = String.valueOf(tabelaDetalhes.getValueAt(selectedRow, 2));
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Deseja realmente excluir este lançamento?", 
            "Confirmar Exclusão", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean sucesso = false;
            if (tipo.startsWith("Despesa")) {
                sucesso = new fcaixa().excluirDespesa(id);
            } else if (tipo.startsWith("Retirada")) {
                String sql = "DELETE FROM retiradas_caixa WHERE id = ?";
                try (Connection link = new fazconexao().conectar();
                     PreparedStatement stmt = link.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    sucesso = stmt.executeUpdate() > 0;
                } catch (Exception ex) {
                    logger.error("Erro ao excluir retirada: ", ex);
                }
            }
            
            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Lançamento excluído com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarFluxo();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir lançamento do banco.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportarParaPdf() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar PDF do Fluxo de Caixa");
        fileChooser.setSelectedFile(new File("Fluxo_de_Caixa_" + txtDataInicio.getText().replace("/", "-") + "_a_" + txtDataFim.getText().replace("/", "-") + ".pdf"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }
            
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            try {
                PdfWriter.getInstance(doc, new FileOutputStream(filePath));
                doc.open();
                
                // Styles
                com.lowagie.text.Font fontTitulo = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD, Color.DARK_GRAY);
                com.lowagie.text.Font fontSub = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.ITALIC, Color.GRAY);
                com.lowagie.text.Font fontSecao = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, new Color(33, 150, 243));
                com.lowagie.text.Font fontTabelaHead = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, Color.WHITE);
                com.lowagie.text.Font fontTabelaBody = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL, Color.BLACK);
                com.lowagie.text.Font fontBold = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.BOLD, Color.BLACK);
                
                Paragraph p = new Paragraph("RELATÓRIO DE FLUXO DE CAIXA", fontTitulo);
                p.setAlignment(Element.ALIGN_CENTER);
                doc.add(p);
                
                Paragraph pSub = new Paragraph("Período: " + txtDataInicio.getText() + " até " + txtDataFim.getText(), fontSub);
                pSub.setAlignment(Element.ALIGN_CENTER);
                doc.add(pSub);
                
                doc.add(Chunk.NEWLINE);
                
                // Section 1: DRE
                doc.add(new Paragraph("Resumo Gerencial (DRE)", fontSecao));
                doc.add(Chunk.NEWLINE);
                
                PdfPTable tableResumo = new PdfPTable(3);
                tableResumo.setWidthPercentage(100);
                tableResumo.setWidths(new float[]{60, 20, 20});
                
                PdfPCell h1 = new PdfPCell(new Phrase("Categoria / Centro de Custo", fontTabelaHead));
                h1.setBackgroundColor(new Color(71, 85, 105));
                h1.setPadding(6);
                PdfPCell h2 = new PdfPCell(new Phrase("Valor (R$)", fontTabelaHead));
                h2.setBackgroundColor(new Color(71, 85, 105));
                h2.setPadding(6);
                PdfPCell h3 = new PdfPCell(new Phrase("Participação (%)", fontTabelaHead));
                h3.setBackgroundColor(new Color(71, 85, 105));
                h3.setPadding(6);
                
                tableResumo.addCell(h1);
                tableResumo.addCell(h2);
                tableResumo.addCell(h3);
                
                for (int i = 0; i < modeloResumo.getRowCount(); i++) {
                    String col1 = String.valueOf(modeloResumo.getValueAt(i, 0));
                    String col2 = String.valueOf(modeloResumo.getValueAt(i, 1));
                    String col3 = String.valueOf(modeloResumo.getValueAt(i, 2));
                    
                    com.lowagie.text.Font rowFont = (col1.contains("TOTAL") || col1.contains("FATURAMENTO") || col1.contains("DESPESAS") || col1.contains("RESULTADO")) ? fontBold : fontTabelaBody;
                    
                    PdfPCell c1 = new PdfPCell(new Phrase(col1, rowFont));
                    PdfPCell c2 = new PdfPCell(new Phrase(col2, rowFont));
                    PdfPCell c3 = new PdfPCell(new Phrase(col3, rowFont));
                    
                    c1.setPadding(4);
                    c2.setPadding(4);
                    c3.setPadding(4);
                    
                    tableResumo.addCell(c1);
                    tableResumo.addCell(c2);
                    tableResumo.addCell(c3);
                }
                
                doc.add(tableResumo);
                doc.add(Chunk.NEWLINE);
                doc.add(Chunk.NEWLINE);
                
                // Section 2: Detalhes Despesas
                doc.add(new Paragraph("Detalhamento de Lançamentos (Despesas e Retiradas)", fontSecao));
                doc.add(Chunk.NEWLINE);
                
                PdfPTable tableDesp = new PdfPTable(6);
                tableDesp.setWidthPercentage(100);
                tableDesp.setWidths(new float[]{18, 17, 35, 12, 10, 8});
                
                String[] dHeaders = {"Data/Hora", "Tipo", "Descrição", "Valor", "Pgto", "Status"};
                for (String dh : dHeaders) {
                    PdfPCell h = new PdfPCell(new Phrase(dh, fontTabelaHead));
                    h.setBackgroundColor(new Color(71, 85, 105));
                    h.setPadding(6);
                    tableDesp.addCell(h);
                }
                
                for (int i = 0; i < modeloDetalhes.getRowCount(); i++) {
                    for (int j = 1; j < modeloDetalhes.getColumnCount(); j++) {
                        String val = String.valueOf(modeloDetalhes.getValueAt(i, j));
                        PdfPCell c = new PdfPCell(new Phrase(val, fontTabelaBody));
                        c.setPadding(4);
                        tableDesp.addCell(c);
                    }
                }
                
                doc.add(tableDesp);
                
                JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso em PDF!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                logger.error("Erro ao exportar PDF: ", ex);
                JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } finally {
                doc.close();
            }
        }
    }
}
