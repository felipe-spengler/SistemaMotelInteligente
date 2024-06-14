package com.motelinteligente.telas;

import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CacheDados.DadosVendidos;
import com.motelinteligente.dados.CacheDados.Negociados;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.NumeroPorExtenso;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.playSound;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author MOTEL
 */
public class EncerraQuarto extends javax.swing.JFrame {

    String dataInicio, dataFim, tempoTotalLocado;
    float valorAcrescimo = 0;
    float valorConsumo = 0, valorQuarto = 0, valorAdicionalPeriodo = 0, valorAdicionalPessoa = 0;
    float valD = 0, valP = 0, valC = 0, valorTotalFinal = 0;
    ClienteEncerra outraTela = new ClienteEncerra();
    float valorTotalRecebido = 0;
    int numeroDoQuarto;
    int finalizar = 0, numeroDePessoas = 2;
    String motivo = null;
    private KeyEventDispatcher yourKeyEventDispatcher;
    private Timer timer;

    class numOnly extends PlainDocument {

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str != null) {
                // Verifique se a string inserida contém apenas números (0 a 9)
                for (char c : str.toCharArray()) {
                    if (!Character.isDigit(c)) {
                        return; // Não insira caracteres não numéricos
                    }
                }

                // Se a string contém apenas números, insira-a normalmente
                super.insertString(offs, str, a);
            }
        }
    }

    /**
     * Creates new form EncerraQuarto
     */
    public EncerraQuarto(int numeroQuarto) {
        initComponents();
        numeroDoQuarto = numeroQuarto;
        txtIdProduto.setDocument(new numOnly());
        txtQuantidade.setDocument(new numOnly());
        txtPessoas.setDocument(new numOnly());
        this.setVisible(true);
        setaLabelGeral(numeroQuarto);
        // fim setar valor quarto e adicional

        //setar valor total 
        setValorTotal();

        tabela.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 1) {
                    int row = e.getFirstRow();
                    int novaQuantidade = (int) tabela.getValueAt(row, 1);
                    float valorUnd = (float) tabela.getValueAt(row, 3);
                    float novoTotal = novaQuantidade * valorUnd;
                    tabela.setValueAt(novoTotal, row, 4); // Atualize a coluna "Total"

                    atualizaLabel();
                }
            }

        });
        // Inicialize yourKeyEventDispatcher
        yourKeyEventDispatcher = new KeyEventDispatcher() {
            boolean eventConsumed = false;

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (eventConsumed) {
                    // Se o evento já foi consumido, retorne false para indicar que não foi tratado novamente
                    return false;
                }

                int keyCode = e.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_ESCAPE:
                        btVoltar.doClick();
                        eventConsumed = true;
                        break;
                    case KeyEvent.VK_F9:
                        btSalvar.doClick();
                        eventConsumed = true;
                        break;
                    case KeyEvent.VK_F2:
                        btConferencia.doClick();
                        eventConsumed = true;
                        break;
                    case KeyEvent.VK_F4:
                        btDebito.doClick();
                        eventConsumed = true;
                        break;
                    case KeyEvent.VK_F6:
                        btDesistencia.doClick();
                        eventConsumed = true;
                        break;
                    default:
                        break;
                }

                // Inicie um temporizador para redefinir eventConsumed após um curto período de tempo
                if (timer != null) {
                    timer.stop();
                }
                timer = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        eventConsumed = false;
                        timer.stop();
                    }
                });
                timer.start();

                return false; // Indica se o evento foi consumido ou não
            }
        };
        // Adicione yourKeyEventDispatcher ao KeyboardFocusManager
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(yourKeyEventDispatcher);
        txtIdProduto.grabFocus();
    }

    public void setaLabelGeral(int numeroQuarto) {
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();

        modelo.setNumRows(0);
        txtIdProduto.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                String texto = new fprodutos().getDescicao(txtIdProduto.getText());
                lblNomeProduto.setText(texto);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isInteger(txtIdProduto.getText())) {
                    String texto = new fprodutos().getDescicao(txtIdProduto.getText());
                    lblNomeProduto.setText(texto);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        txtIdProduto.addActionListener(e -> {
            txtQuantidade.requestFocusInWindow();
        });
        txtQuantidade.addActionListener(e -> {
            btInserir.doClick();
            txtIdProduto.grabFocus();
        });

        // seta a data de inicio
        fquartos quartodao = new fquartos();
        dataInicio = quartodao.getDataInicio(numeroQuarto);
        lblInicioLocacao.setText(dataInicio);
        //setar a data final
        Date dataAtual = new Date();
        Timestamp horaAtual = new Timestamp(dataAtual.getTime());

        dataFim = String.valueOf(horaAtual);
        lblFimLocacao.setText(dataFim);

        tempoTotalLocado = quartodao.getData(numeroQuarto);

        labelEncerramento.setText("Encerramento Quarto " + numeroDoQuarto);
        //setar tempo locado
        lblTempoLocado.setText(tempoTotalLocado);

        //setar numero pessoas
        numeroDePessoas = quartodao.getPessoas(numeroQuarto);
        if (numeroDePessoas == 2) {
            txtPessoas.setText(String.valueOf(numeroDePessoas));
        } else {
            txtPessoas.setText(String.valueOf(numeroDePessoas));
        }
        //setar valor quarto e adicionalPeriodo começa agora
        CacheDados cache = CacheDados.getInstancia();
        DadosOcupados ocupado = cache.getCacheOcupado().get(numeroQuarto);
        CarregaQuarto quarto = cache.getCacheQuarto().get(numeroQuarto);
        String status = quarto.getStatusQuarto();
        String horarioQuarto = quarto.getHoraStatus();
        String[] partes = status.split("-");
        if (partes[1].equals("pernoite")) {
            float valor = ocupado.getValorPernoite();
            //adiciona o valor do adicional de pessoas
            valor += calculaAdicionalPessoa(ocupado.getNumeroPessoas());
            lblValorQuarto.setText("R$" + String.valueOf(valor));
            valorQuarto = (valor);
            int numeroAdicionais = subtrairHora(numeroQuarto, horarioQuarto, "pernoite");
            valorAdicionalPeriodo = Float.valueOf(numeroAdicionais) * ocupado.getValorAdicional();
            lblHoraAdicional.setText("R$" + String.valueOf(valorAdicionalPeriodo));
        } else if (partes[1].equals("periodo")) {
            float valor = ocupado.getValorPeriodo();
            //adiciona o valor do adicional de pessoas
            valor += calculaAdicionalPessoa(ocupado.getNumeroPessoas());
            lblValorQuarto.setText("R$" + String.valueOf(valor));
            int numeroAdicionais = subtrairHora(numeroQuarto, horarioQuarto, ocupado.getTempoPeriodo());
            valorAdicionalPeriodo = Float.valueOf(numeroAdicionais) * ocupado.getValorAdicional();
            lblHoraAdicional.setText("R$" + String.valueOf(valorAdicionalPeriodo));
        }

        // ve se tem itens no prevenda
        int idLocacao = quartodao.getIdLocacao(numeroQuarto);
        if (idLocacao == 0) {
            int novoID = new fquartos().getIdLocacao(numeroQuarto);
            idLocacao = novoID;
        }
        adicionaPreVendidos(idLocacao);
        verAntecipado(idLocacao);
        setValorTotal();

        //setar as datas da tela do cliente
        outraTela.setaDatas(dataInicio, dataFim, tempoTotalLocado);
        outraTela.setTitulo(numeroQuarto);

    }

    public void adicionaPreVendidos(int locacao) {
        // Verifica se a cache de produtos vendidos contém a locação
        CacheDados cache = CacheDados.getInstancia();

        if (cache.cacheProdutosVendidos.containsKey(locacao)) {
            // Obtém a lista de produtos vendidos para esta locação da cache
            List<DadosVendidos> produtosVendidos = cache.cacheProdutosVendidos.get(locacao);

            // Percorre a lista de produtos vendidos
            for (DadosVendidos produto : produtosVendidos) {
                // Preenche os campos com os dados de prevendidos
                int id = produto.idProduto;
                int quantidade = produto.quantidadeVendida;
                txtIdProduto.setText(Integer.toString(id));
                txtQuantidade.setText(Integer.toString(quantidade));

                // Simula um clique no botão btInserir
                btInserir.doClick();
            }

            // Atualiza o label (se necessário)
            atualizaLabel();
        }
    }

    public void verAntecipado(int locacao) {
        // Verifica se a cache de negociações antecipadas contém a locação
        CacheDados cache = CacheDados.getInstancia();
        if (cache.cacheNegociado.containsKey(locacao)) {
            // Obtém a lista de negociações antecipadas associada a essa locação
            List<Negociados> negociacoes = cache.cacheNegociado.get(locacao);

            // Itera sobre a lista de negociações antecipadas
            for (Negociados negociado : negociacoes) {
                String tipo = negociado.tipo;
                float valor = negociado.valor;
                if (valor > 0) {
                    if (tipo.equals("recebido")) {
                        JOptionPane.showMessageDialog(null, valor + " recebidos antecipadamente!", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                    if (tipo.equals("negociado")) {
                        JOptionPane.showMessageDialog(null, valor + " desconto negociado!", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                }

            }
        } else {
            // Se a cache não contiver a locação, exibe uma mensagem informando que não há dados antecipados para essa locação
            System.out.println("Não há dados antecipados para a locação " + locacao + " na cache.");
        }
    }

    public float calculaAdicionalPessoa(int numeroPessoas) {
        float adicionalPessoas = (numeroPessoas - 2) * 30;
        if (adicionalPessoas <= 0) {
            adicionalPessoas = 0;
        }
        valorAdicionalPessoa = adicionalPessoas;
        return adicionalPessoas;
    }

    @Override
    public void dispose() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(yourKeyEventDispatcher);

        outraTela.dispose();
        super.dispose();
    }

    public void setValorTotal() {
        try {
            int indiceQuarto = lblValorQuarto.getText().indexOf("R$");
            int indiceConsumo = lblValorConsumo2.getText().indexOf("R$");
            int indiceAdicional = lblHoraAdicional.getText().indexOf("R$");
            float valQuarto = 0;
            float valConsumo = 0;
            float valAdicional = 0;

            if (indiceQuarto != -1) {
                String valorAtual = lblValorQuarto.getText().substring(indiceQuarto + 2);
                valQuarto = Float.valueOf(valorAtual);
            }

            if (indiceConsumo != -1) {
                String valorAtual = lblValorConsumo2.getText().substring(indiceConsumo + 2);
                valConsumo = Float.valueOf(valorAtual);
            }
            if (indiceAdicional != -1) {
                String valorAtual = lblHoraAdicional.getText().substring(indiceAdicional + 2);
                valAdicional = Float.valueOf(valorAtual);
            }

            float valorTotal = valQuarto + valConsumo + valAdicional;
            valorTotalFinal = valorTotal;
            SwingUtilities.invokeLater(() -> {
                lblValorTotal.setText("R$" + String.valueOf(valorTotal));
                outraTela.setPessoas(numeroDePessoas);
                outraTela.setarValores(lblValorQuarto.getText(), lblHoraAdicional.getText());
                outraTela.setValorTotal(valorTotal);
                lblAReceber.setText("R$" + String.valueOf(valorTotalFinal - valorTotalRecebido));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String calculaData(String dataBanco) {
        Timestamp horaBanco = Timestamp.valueOf(dataBanco);
        Long datetime = System.currentTimeMillis();
        Timestamp horaAtual = new Timestamp(datetime);

        long diferencaMillis = horaAtual.getTime() - horaBanco.getTime();

        // Calcula a diferença em horas, minutos e segundos
        long minutos = diferencaMillis / (60 * 1000) % 60;
        long horas = diferencaMillis / (60 * 60 * 1000);

        // Formata a diferença no formato hh:mm:ss
        String diferencaFormatada = String.format("%02d:%02d", horas, minutos);
        return diferencaFormatada;
    }

    public int subtrairHora(int numeroQuarto, String horarioQuarto, String ondeEncaixa) {
        String diferenca = calculaData(horarioQuarto);
        String[] partes = diferenca.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        int totalMinutos = (horas * 60) + minutos;
        int add = 0, minPernoite = 729;

        if (ondeEncaixa.equals("pernoite")) {
            // Verifica se a diferença é maior que 12 horas e 9 minutos
            if (totalMinutos <= minPernoite) {
                return 0;
            } else {
                // Calcula as horas adicionais além do período de pernoite
                while (totalMinutos > minPernoite) {
                    add++;
                    minPernoite += 60; // Adiciona mais 1 hora
                }
                return add;
            }
        } else {
            CacheDados cache = CacheDados.getInstancia();
            DadosOcupados ocupado = cache.getCacheOcupado().get(numeroQuarto);
            String periodoQuarto = ocupado.getTempoPeriodo();
            String[] partesPeriodo = periodoQuarto.split(":");
            int horasPeriodo = Integer.parseInt(partesPeriodo[0]);
            int minutosPeriodo = Integer.parseInt(partesPeriodo[1]);
            int totalPeriodo = (horasPeriodo * 60) + minutosPeriodo + 9;

            // Verifica se o tempo atual está dentro do período especificado
            if (totalMinutos <= totalPeriodo) {
                return 0;
            } else {
                // Calcula as horas adicionais além do período especificado
                while (totalMinutos > totalPeriodo) {
                    totalPeriodo += 60; // Adiciona mais 1 hora
                    add++;
                }
                return add;
            }
        }
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barraCima = new javax.swing.JPanel();
        btSalvar = new javax.swing.JButton();
        btVoltar = new javax.swing.JButton();
        btDebito = new javax.swing.JButton();
        btConferencia = new javax.swing.JButton();
        btDesistencia = new javax.swing.JButton();
        painelProdutos = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtIdProduto = new javax.swing.JTextField();
        lblNomeProduto = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        btInserir = new javax.swing.JButton();
        txtQuantidade = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblValorConsumo = new javax.swing.JLabel();
        painelInfo = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblInicioLocacao = new javax.swing.JLabel();
        lblFimLocacao = new javax.swing.JLabel();
        lblValorQuarto = new javax.swing.JLabel();
        lblHoraAdicional = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblTempoLocado = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtPessoas = new javax.swing.JTextField();
        lblValorConsumo2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblValorTotal = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        painelRecebimento = new javax.swing.JPanel();
        txtJustifica = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        lblAReceber = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txtValorRecebido = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtDesconto = new javax.swing.JTextField();
        txtDescontoPorcento = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtAcrescimo = new javax.swing.JTextField();
        labelEncerramento = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        barraCima.setPreferredSize(new java.awt.Dimension(701, 95));

        btSalvar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btSalvar.setForeground(new java.awt.Color(204, 51, 0));
        btSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/Save_37110.png"))); // NOI18N
        btSalvar.setText("Salvar (F9)");
        btSalvar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btSalvar.setMaximumSize(new java.awt.Dimension(130, 79));
        btSalvar.setMinimumSize(new java.awt.Dimension(130, 79));
        btSalvar.setPreferredSize(new java.awt.Dimension(130, 79));
        btSalvar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarActionPerformed(evt);
            }
        });

        btVoltar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btVoltar.setForeground(new java.awt.Color(204, 51, 0));
        btVoltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/Go-back-icon.png"))); // NOI18N
        btVoltar.setText("Voltar (ESC)");
        btVoltar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btVoltar.setMaximumSize(new java.awt.Dimension(130, 79));
        btVoltar.setMinimumSize(new java.awt.Dimension(130, 79));
        btVoltar.setPreferredSize(new java.awt.Dimension(130, 79));
        btVoltar.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        btVoltar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVoltarActionPerformed(evt);
            }
        });

        btDebito.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btDebito.setForeground(new java.awt.Color(204, 51, 0));
        btDebito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/technicalsupport_support_representative_person_people_man_1641.png"))); // NOI18N
        btDebito.setText("Débito (F4)");
        btDebito.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btDebito.setMaximumSize(new java.awt.Dimension(130, 79));
        btDebito.setMinimumSize(new java.awt.Dimension(130, 79));
        btDebito.setPreferredSize(new java.awt.Dimension(130, 79));
        btDebito.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btDebito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDebitoActionPerformed(evt);
            }
        });

        btConferencia.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btConferencia.setForeground(new java.awt.Color(204, 51, 0));
        btConferencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/technicalsupport_support_representative_person_people_man_1641.png"))); // NOI18N
        btConferencia.setText("Conferencia (F2)");
        btConferencia.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btConferencia.setMaximumSize(new java.awt.Dimension(130, 79));
        btConferencia.setMinimumSize(new java.awt.Dimension(130, 79));
        btConferencia.setPreferredSize(new java.awt.Dimension(130, 79));
        btConferencia.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btConferencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConferenciaActionPerformed(evt);
            }
        });

        btDesistencia.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btDesistencia.setForeground(new java.awt.Color(204, 51, 0));
        btDesistencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/cancel_icon-icons.com_52401.png"))); // NOI18N
        btDesistencia.setText("Desistencia (F6)");
        btDesistencia.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btDesistencia.setMaximumSize(new java.awt.Dimension(130, 79));
        btDesistencia.setMinimumSize(new java.awt.Dimension(130, 79));
        btDesistencia.setPreferredSize(new java.awt.Dimension(130, 79));
        btDesistencia.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btDesistencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDesistenciaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout barraCimaLayout = new javax.swing.GroupLayout(barraCima);
        barraCima.setLayout(barraCimaLayout);
        barraCimaLayout.setHorizontalGroup(
            barraCimaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barraCimaLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(btVoltar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btConferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btDebito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btDesistencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        barraCimaLayout.setVerticalGroup(
            barraCimaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, barraCimaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(barraCimaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btDesistencia, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btConferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btDebito, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(barraCimaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(barraCimaLayout.createSequentialGroup()
                            .addComponent(btSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, barraCimaLayout.createSequentialGroup()
                            .addComponent(btVoltar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(16, 16, 16)))))
        );

        painelProdutos.setBackground(new java.awt.Color(204, 204, 204));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Adicionar Consumo");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel7.setText("id do Produto:");

        txtIdProduto.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtIdProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdProdutoActionPerformed(evt);
            }
        });

        lblNomeProduto.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblNomeProduto.setText("<>");

        tabela.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "id", "Quantidade", "Descrição", "Valor und", "Valor Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabela.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane1.setViewportView(tabela);

        jButton4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png"))); // NOI18N
        jButton4.setText("Apagar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        btInserir.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btInserir.setText("Inserir");
        btInserir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInserirActionPerformed(evt);
            }
        });

        txtQuantidade.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtQuantidade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantidadeActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setText("Quantidade");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Valor do Consumo:");

        lblValorConsumo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblValorConsumo.setForeground(new java.awt.Color(0, 51, 255));
        lblValorConsumo.setText("R$0.00");

        javax.swing.GroupLayout painelProdutosLayout = new javax.swing.GroupLayout(painelProdutos);
        painelProdutos.setLayout(painelProdutosLayout);
        painelProdutosLayout.setHorizontalGroup(
            painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(28, 28, 28)
                .addComponent(lblValorConsumo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(12, 12, 12)
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addComponent(txtQuantidade, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btInserir))
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addComponent(txtIdProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblNomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 652, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        painelProdutosLayout.setVerticalGroup(
            painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtIdProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNomeProduto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtQuantidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btInserir))
                .addGap(21, 21, 21)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel11)
                        .addComponent(lblValorConsumo))
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelInfo.setBackground(new java.awt.Color(204, 204, 204));
        painelInfo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Inicio da Locação:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("Fim da Locação:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel5.setText("Valor Quarto:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("Hora Adicional:");

        lblInicioLocacao.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblInicioLocacao.setText("jLabel9");

        lblFimLocacao.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblFimLocacao.setText("jLabel9");

        lblValorQuarto.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblValorQuarto.setForeground(new java.awt.Color(0, 51, 255));
        lblValorQuarto.setText("jLabel9");

        lblHoraAdicional.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblHoraAdicional.setForeground(new java.awt.Color(0, 51, 255));
        lblHoraAdicional.setText("label");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel9.setText("Tempo Locado:");

        lblTempoLocado.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTempoLocado.setText("jLabel9");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel10.setText("Pessoas no Quarto:");

        txtPessoas.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtPessoas.setText("2");
        txtPessoas.setMinimumSize(new java.awt.Dimension(80, 22));
        txtPessoas.setPreferredSize(new java.awt.Dimension(80, 22));
        txtPessoas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPessoasActionPerformed(evt);
            }
        });

        lblValorConsumo2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblValorConsumo2.setForeground(new java.awt.Color(0, 51, 255));
        lblValorConsumo2.setText("R$0.00");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setText("Valor do Consumo:");

        lblValorTotal.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblValorTotal.setForeground(new java.awt.Color(255, 51, 51));
        lblValorTotal.setText("0,00");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setText("Valor Total:");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        painelRecebimento.setBackground(new java.awt.Color(255, 255, 255));

        txtJustifica.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtJustifica.setMinimumSize(new java.awt.Dimension(80, 22));
        txtJustifica.setPreferredSize(new java.awt.Dimension(80, 22));
        txtJustifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtJustificaActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setText("Justificativa:");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel13.setBackground(new java.awt.Color(255, 51, 51));
        jLabel13.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(51, 51, 51));
        jLabel13.setText("        RECEBIMENTO");
        jLabel13.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Valor Pendente a Receber:");

        lblAReceber.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblAReceber.setForeground(new java.awt.Color(255, 51, 51));
        lblAReceber.setText("jLabel17");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("Valor Recebido:");

        txtValorRecebido.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtValorRecebido.setMinimumSize(new java.awt.Dimension(80, 22));
        txtValorRecebido.setPreferredSize(new java.awt.Dimension(80, 22));
        txtValorRecebido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorRecebidoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(76, 76, 76)
                                .addComponent(jLabel18))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(101, 101, 101)
                                .addComponent(lblAReceber)))
                        .addGap(0, 134, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addGap(18, 18, 18)
                .addComponent(txtValorRecebido, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(txtValorRecebido, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAReceber)
                .addGap(32, 32, 32))
        );

        javax.swing.GroupLayout painelRecebimentoLayout = new javax.swing.GroupLayout(painelRecebimento);
        painelRecebimento.setLayout(painelRecebimentoLayout);
        painelRecebimentoLayout.setHorizontalGroup(
            painelRecebimentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelRecebimentoLayout.createSequentialGroup()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJustifica, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(painelRecebimentoLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelRecebimentoLayout.setVerticalGroup(
            painelRecebimentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelRecebimentoLayout.createSequentialGroup()
                .addGroup(painelRecebimentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(txtJustifica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Desconto:");

        txtDesconto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDesconto.setText("0");
        txtDesconto.setMinimumSize(new java.awt.Dimension(80, 22));
        txtDesconto.setPreferredSize(new java.awt.Dimension(80, 22));
        txtDesconto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescontoActionPerformed(evt);
            }
        });

        txtDescontoPorcento.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDescontoPorcento.setText("0%");
        txtDescontoPorcento.setMinimumSize(new java.awt.Dimension(80, 22));
        txtDescontoPorcento.setPreferredSize(new java.awt.Dimension(80, 22));
        txtDescontoPorcento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescontoPorcentoActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Acréscimo:");

        txtAcrescimo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtAcrescimo.setText("0");
        txtAcrescimo.setMinimumSize(new java.awt.Dimension(80, 22));
        txtAcrescimo.setPreferredSize(new java.awt.Dimension(80, 22));
        txtAcrescimo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAcrescimoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelRecebimento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDescontoPorcento, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDesconto, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAcrescimo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDescontoPorcento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(txtAcrescimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelRecebimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout painelInfoLayout = new javax.swing.GroupLayout(painelInfo);
        painelInfo.setLayout(painelInfoLayout);
        painelInfoLayout.setHorizontalGroup(
            painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelInfoLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addGap(46, 46, 46)
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblInicioLocacao)
                            .addComponent(lblFimLocacao)))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(63, 63, 63)
                        .addComponent(lblTempoLocado))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel10)
                            .addComponent(jLabel4))
                        .addGap(32, 32, 32)
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblValorQuarto)
                            .addComponent(txtPessoas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHoraAdicional)
                            .addComponent(lblValorConsumo2)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelInfoLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(lblValorTotal)
                        .addGap(23, 23, 23)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        painelInfoLayout.setVerticalGroup(
            painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelInfoLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblInicioLocacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblFimLocacao))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTempoLocado)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addComponent(txtPessoas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorQuarto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHoraAdicional))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(lblValorConsumo2))))
                .addGap(18, 18, 18)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(lblValorTotal))
                .addGap(28, 28, 28)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        labelEncerramento.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        labelEncerramento.setText("Encerramento Quarto xxx");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barraCima, javax.swing.GroupLayout.DEFAULT_SIZE, 1140, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(labelEncerramento, javax.swing.GroupLayout.PREFERRED_SIZE, 606, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(116, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(painelProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(barraCima, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(painelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelEncerramento, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(painelProdutos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdProdutoActionPerformed

    }//GEN-LAST:event_txtIdProdutoActionPerformed

    private void txtQuantidadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantidadeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQuantidadeActionPerformed

    private void btInserirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInserirActionPerformed
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();

        if (isInteger(txtIdProduto.getText())) {
            fprodutos produtodao = new fprodutos();
            String texto = produtodao.getDescicao(txtIdProduto.getText());
            if (texto != null) {
                if (isInteger(txtQuantidade.getText())) {
                    float valor = produtodao.getValorProduto(Integer.parseInt(txtIdProduto.getText()));
                    float valorSoma = valor * Integer.parseInt(txtQuantidade.getText());
                    modelo.addRow(new Object[]{
                        txtIdProduto.getText(),
                        txtQuantidade.getText(),
                        texto,
                        valor,
                        valorSoma
                    });
                    outraTela.adicionaTabela(txtIdProduto.getText(), txtQuantidade.getText(), texto, valor, valorSoma);
                    String valorAtual = "0";
                    try {
                        int indiceDoR = lblValorQuarto.getText().indexOf("R$");
                        if (indiceDoR != -1) {
                            valorAtual = lblValorConsumo.getText().substring(indiceDoR + 2);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e);
                    }

                    float novoVal = Float.parseFloat(valorAtual) + valorSoma;
                    atualizaLabel();
                    valorConsumo = novoVal;
                    setValorTotal();

                } else {
                    JOptionPane.showMessageDialog(rootPane, "Quantidade inválida!");

                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Código inserido invalido!");
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Digite um valor válido!");
        }

        if (!(txtDesconto.getText().isEmpty())) {
            verificaDesconto(2);
            System.out.println("realizar ");
        }
        if (!(txtDesconto.getText().isEmpty())) {
            realizaAcrescimo();
        }

        txtQuantidade.setText("");
        txtIdProduto.setText("");

    }//GEN-LAST:event_btInserirActionPerformed
    public void atualizaLabel() {
        valorConsumo = 0;
        DefaultTableModel model = (DefaultTableModel) tabela.getModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {

            valorConsumo += (float) model.getValueAt(i, 4);

        }

        SwingUtilities.invokeLater(() -> {
            lblValorConsumo.setText(String.valueOf("R$" + valorConsumo));
            lblValorConsumo2.setText(String.valueOf("R$" + valorConsumo));
            outraTela.setConsumo(valorConsumo);
            setValorTotal();

        });
    }
    private void txtPessoasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPessoasActionPerformed
        if (txtPessoas.getText() != null) {
            try {
                int qnt = Integer.valueOf(txtPessoas.getText());
                outraTela.setPessoas(qnt);
                if (qnt < 2) {
                    qnt = 2;
                }

                float novoVal = calculaAdicionalPessoa(qnt);
                lblValorQuarto.setText("R$" + String.valueOf(novoVal + valorQuarto));
                System.out.println("aqui o valor do quarto é " + (novoVal + valorQuarto));
                outraTela.setValorQuarto("R$" + String.valueOf(novoVal + valorQuarto));

            } catch (Exception e) {
                e.printStackTrace();
            }
            setValorTotal();

        }

    }//GEN-LAST:event_txtPessoasActionPerformed

    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed

        if (motivo != null) {
            // foi dado desistencia
            salvaDesistencia();
        } else {
            String descontar = txtDesconto.getText();
            String acrescentar = txtAcrescimo.getText();
            if ((!descontar.equals("0") && !descontar.isEmpty()) || (!acrescentar.equals("0") && !acrescentar.isEmpty())) {
                // precisa de justificativa
                if (txtJustifica.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Precisa Justificativa!");
                } else {
                    if (chamaJOP()) {
                        salvaVendidos(numeroDoQuarto);
                        salvaJustifica();
                        System.out.println("justificativa salva");
                    }
                }

            } else {
                // nao precisa justificativa
                if (chamaJOP()) {
                    salvaVendidos(numeroDoQuarto);
                    System.out.println("não precisou justificar");

                }
            }
        }

        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(yourKeyEventDispatcher);

    }//GEN-LAST:event_btSalvarActionPerformed
    public void salvaJustifica() {
        System.out.println("está executando aqui");
        int idLocacao = 0;
        Connection link = null;
        String consultaSQL = "SELECT MAX(idlocacao) as max_id FROM registralocado WHERE numquarto = ?";
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, numeroDoQuarto);
            ResultSet resultado = statement.executeQuery();
            if (resultado.next()) {
                idLocacao = resultado.getInt("max_id");
                System.out.println("id é " + idLocacao);
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error salvajustifica:" + e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "error salvajustifica:" + e);
            }
        }
        float valorSalvar = 0;
        String tipoValor = null;
        if (txtDesconto.getText() != null && txtDesconto.getText() != "0") {
            tipoValor = "desconto";
            valorSalvar = Float.valueOf(txtDesconto.getText());
        } else {
            if (txtAcrescimo.getText() != null) {
                tipoValor = "acrescimo";
                valorSalvar = Float.valueOf(txtAcrescimo.getText());
            }
        }
        try {
            link = new fazconexao().conectar();
            consultaSQL = "INSERT INTO justificativa (idlocacao, valor, tipo, justificativa) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            System.out.println("");
            statement.setInt(1, idLocacao);
            statement.setFloat(2, valorSalvar);
            statement.setString(3, tipoValor);
            statement.setString(4, txtJustifica.getText());

            int n = statement.executeUpdate();

            if (n != 0) {
                link.close();
                statement.close();
                JOptionPane.showMessageDialog(null, "Desconto/Acrescimo salvo! ");

            } else {
                link.close();
                statement.close();
                JOptionPane.showMessageDialog(null, "Erro ao salvar Justificativa! Infome ao Suporte do Sistema!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "SEVERAL ERROR: Justificativa! Infome ao Suporte do Sistema!");

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

    public void salvaDesistencia() {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        fquartos quartodao = new fquartos();
        CacheDados cache = CacheDados.getInstancia();
        int idLocacao = cache.getCacheOcupado().get(numeroDoQuarto).getIdLoca();
        String horaFim = lblFimLocacao.getText();
        String horaInicio = lblInicioLocacao.getText();
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            String consultaSQL = "INSERT INTO desistencia (numquarto, horainicio, horafim, motivo, idcaixaatual) VALUES (?, ?, ? , ? , ?)";
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, numeroDoQuarto);
            statement.setTimestamp(2, Timestamp.valueOf(horaInicio));
            statement.setTimestamp(3, Timestamp.valueOf(horaFim));
            statement.setString(4, motivo);
            statement.setInt(5, idCaixa);

            int n = statement.executeUpdate();

            if (n != 0) {
                link.close();
                statement.close();
                JOptionPane.showMessageDialog(null, "Desistencia Salva! ");
                excluiDaRegistraLocado(idLocacao);
                // altera o status do quarto para limpeza
                mudaStatusNaCache(numeroDoQuarto, "limpeza");
                //retira da cache ocupados
                cache.getCacheOcupado().remove(numeroDoQuarto);

                //se tiver prevendidos ou negociados retira tambem
                if (cache.cacheNegociado.containsKey(idLocacao)) {
                    cache.cacheNegociado.remove(idLocacao);
                }
                if (cache.cacheProdutosVendidos.containsKey(idLocacao)) {
                    cache.cacheProdutosVendidos.remove(idLocacao);
                }
                quartodao.setStatus(numeroDoQuarto, "limpeza");
                quartodao.adicionaRegistro(numeroDoQuarto, "limpeza");
                config.setMudanca(true);
                outraTela.dispose();
                this.dispose();
            } else {
                link.close();
                statement.close();
                JOptionPane.showMessageDialog(null, "Erro ao salvar Desistencia! Infome ao Suporte do Sistema!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "SEVERAL ERROR: Desistencia! Infome ao Suporte do Sistema!");

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

    public void excluiDaRegistraLocado(int idLocacao) {
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            String consultaSQL = "DELETE FROM registralocado WHERE idlocacao = ?";
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, idLocacao);
            int n = statement.executeUpdate();

            if (n != 0) {
                JOptionPane.showMessageDialog(null, "Registro excluído da tabela registralocado!");
            } else {
                JOptionPane.showMessageDialog(null, "Nenhum registro encontrado para exclusão na tabela registralocado!");
            }
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir registro de locação na tabela registralocado! Informe ao Suporte do Sistema!");
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

    public boolean chamaJOP() {
        JTextField pixField = new JTextField(10);
        JTextField cartaoField = new JTextField(10);
        JTextField dinheiroField = new JTextField(10);
        Font biggerFont = new Font(pixField.getFont().getName(), pixField.getFont().getStyle(), 16);
        pixField.setFont(biggerFont);
        cartaoField.setFont(biggerFont);
        dinheiroField.setFont(biggerFont);

        float valorTotal = 0;
        int indiceDoR = lblValorTotal.getText().indexOf("R$");
        if (indiceDoR != -1) {
            String valorPegar = lblValorTotal.getText().substring(indiceDoR + 2);
            valorPegar = valorPegar.replace(",", ".");
            valorTotal = Float.parseFloat(valorPegar);
        }
        dinheiroField.setText(String.valueOf(valorTotal));
        cartaoField.setText(String.valueOf("0"));
        pixField.setText(String.valueOf("0"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Digite o valor recebido em Dinheiro:"));
        panel.add(dinheiroField);
        panel.add(new JLabel("Digite o valor recebido em Pix:"));
        panel.add(pixField);
        panel.add(new JLabel("Digite o valor recebido em Cartão:"));
        panel.add(cartaoField);

        // Exiba o JOptionPane personalizado
        int result = JOptionPane.showConfirmDialog(null, panel, "Digite os valores recebidos",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String valorPix = pixField.getText().replace(",", ".");;
            String valorCartao = cartaoField.getText().replace(",", ".");;
            String valorDinheiro = dinheiroField.getText().replace(",", ".");;

            try {
                float valPix = 0, valCartao = 0, valDinheiro = 0;
                if (valorPix != null) {
                    valPix = Float.parseFloat(valorPix);
                }

                if (valorCartao != null) {
                    valCartao = Float.parseFloat(valorCartao);
                }

                if (valorDinheiro != null) {
                    valDinheiro = Float.parseFloat(valorDinheiro);
                }

                float soma = valPix + valCartao + valDinheiro;
                // vê se fecha com o valor da conta

                if (soma == valorTotal) {
                    valD = valDinheiro;
                    valC = valCartao;
                    valP = valPix;
                    JOptionPane.getRootFrame().dispose();

                    return true;
                } else {
                    System.out.println("");
                    JOptionPane.showMessageDialog(null, "valores divergentes!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Valores inválidos. Certifique-se de inserir números válidos.");
                return false;
            }
        }
        return false;
    }

    public int objectToInt(int x, int y) {
        DefaultTableModel model = (DefaultTableModel) tabela.getModel();

        Object value = model.getValueAt(x, y);

        if (value instanceof Integer) {
            int numero = (Integer) value;
            return numero;
        } else if (value instanceof String) {
            try {
                int numero = Integer.parseInt((String) value);
                return numero;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        System.out.println("deu ruim");
        return -1;
    }

    public void salvaVendidos(int numero) {
        fquartos quartodao = new fquartos();
        CacheDados cache = CacheDados.getInstancia();
        int idLocacao = cache.getCacheOcupado().get(numeroDoQuarto).getIdLoca();
        DefaultTableModel model = (DefaultTableModel) tabela.getModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int idProduto = objectToInt(i, 0);
            int quantidade = objectToInt(i, 1);
            float valUnd = (Float) model.getValueAt(i, 3);
            float valtotal = (Float) model.getValueAt(i, 4);

            quartodao.salvaProduto(idLocacao, idProduto, quantidade, valUnd, valtotal);
            new com.motelinteligente.dados.fprodutos().diminuiEstoque(idProduto, quantidade);
        }
        String consumoPegar, totalPegar;
        float valorPegouConsumo = 0, valorPegouTotal = 0;
        int indiceC = lblValorConsumo.getText().indexOf("R$");
        if (indiceC != -1) {
            consumoPegar = lblValorConsumo.getText().substring(indiceC + 2);
            consumoPegar = consumoPegar.replace(",", ".");
            valorPegouConsumo = Float.parseFloat(consumoPegar);
        }
        int indiceT = lblValorTotal.getText().indexOf("R$");
        if (indiceT != -1) {
            totalPegar = lblValorTotal.getText().substring(indiceT + 2);
            totalPegar = totalPegar.replace(",", ".");
            valorPegouTotal = Float.parseFloat(totalPegar);
        }
        float valorDoQuarto = valorPegouTotal - valorPegouConsumo;
        String horaFim = lblFimLocacao.getText();
        String horaInicio = lblInicioLocacao.getText();
        String valorQuartoPassar = lblValorTotal.getText();
        quartodao.salvaLocacao(idLocacao, Timestamp.valueOf(horaInicio), Timestamp.valueOf(horaFim), valorDoQuarto, valorConsumo, valD, valP, valC);
        new playSound().playSound("som/agradecemosPreferencia.wav");
        new ConectaArduino(999);
        JOptionPane.showMessageDialog(null, "Registro Salvo com sucesso!");

        // altera o status do quarto para limpeza
        mudaStatusNaCache(numeroDoQuarto, "limpeza");
        //retira da cache ocupados
        cache.getCacheOcupado().remove(numeroDoQuarto);

        //se tiver prevendidos ou negociados retira tambem
        if (cache.cacheNegociado.containsKey(idLocacao)) {
            cache.cacheNegociado.remove(idLocacao);
        }
        if (cache.cacheProdutosVendidos.containsKey(idLocacao)) {
            cache.cacheProdutosVendidos.remove(idLocacao);
        }
        quartodao.setStatus(numeroDoQuarto, "limpeza");
        quartodao.adicionaRegistro(numeroDoQuarto, "limpeza");
        configGlobal config = configGlobal.getInstance();
        config.setMudanca(true);
        outraTela.dispose();
        this.dispose();
    }

    public boolean mudaStatusNaCache(int quartoMudar, String statusColocar) {
        CacheDados dados = CacheDados.getInstancia();
        // Obtém o quarto da cache
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        // Atualiza o status e a data do quarto
        quarto.setStatusQuarto(statusColocar);
        quarto.setHoraStatus(String.valueOf(timestamp));
        // Atualiza o quarto na cache
        dados.getCacheQuarto().put(quartoMudar, quarto);
        return true;

    }
    private void btVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVoltarActionPerformed
        // TODO add your handling code here:
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(yourKeyEventDispatcher);

        outraTela.dispose();
        this.dispose();
    }//GEN-LAST:event_btVoltarActionPerformed

    private void btDebitoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDebitoActionPerformed
        // clicou falar débito
        String valorDebito = "0";
        int indice = lblValorQuarto.getText().indexOf("R$");
        if (indice != -1) {
            valorDebito = lblValorTotal.getText().substring(indice + 2);
            float valor = Float.parseFloat(valorDebito);
            String falar = "SuaConta " + NumeroPorExtenso.NumeroPorExtenso(valor) + " reais";
            String[] palavras = falar.split(" ");

            // Itere pelo array de palavras e imprima cada uma
            reproduzirSonsEmSequencia(palavras, 0);
        }

    }//GEN-LAST:event_btDebitoActionPerformed
    public static void reproduzirSonsEmSequencia(String[] palavras, int indice) {
        if (indice < palavras.length) {
            String palavraAtual = palavras[indice];
            String caminhoSom = "som/" + palavraAtual + ".wav";

            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(caminhoSom));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                // Adiciona um listener para detectar o término da reprodução
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        // Quando o som atual parar, inicie o próximo som
                        reproduzirSonsEmSequencia(palavras, indice + 1);
                    }
                });

                // Inicia a reprodução do som
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }


    private void btConferenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btConferenciaActionPerformed
        // TODO add your handling code here:
        new playSound().playSound("som/mensagem conferencia.wav");
    }//GEN-LAST:event_btConferenciaActionPerformed

    private void btDesistenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDesistenciaActionPerformed
        //verifica o tempo que está locado

        String[] partes = tempoTotalLocado.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        configGlobal config = configGlobal.getInstance();
        String cargo = config.getCargoUsuario();

        if (horas == 0 && minutos <= 10) {
            //da tempo de fazer desistencia
            fazDesistencia();
        } else {
            if (cargo.equals("gerente") || cargo.equals("admin")) {
                fazDesistencia();
                lblAReceber.setText("0.00");
            } else {
                JOptionPane.showMessageDialog(null, "Seu cargo não possui permissão para essa desistência! Contacte um gerente.");
            }
        }

    }//GEN-LAST:event_btDesistenciaActionPerformed
    public void fazDesistencia() {
        motivo = JOptionPane.showInputDialog(null, "Digite o motivo da Desistencia:");
        if (motivo != null) {
            lblValorTotal.setText("R$0.00");
            lblValorQuarto.setText("R$0.00");
            lblAReceber.setText("R$0.00");
            valorQuarto = valorTotalFinal = 0;
        } else {
            JOptionPane.showMessageDialog(null, "Precisa digitar o motivo!!");
        }
    }
    private void txtDescontoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescontoActionPerformed
        verificaDesconto(2);
    }//GEN-LAST:event_txtDescontoActionPerformed

    private void txtDescontoPorcentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescontoPorcentoActionPerformed
        verificaDesconto(1);

    }//GEN-LAST:event_txtDescontoPorcentoActionPerformed
    public void verificaDesconto(int numero) {
        String textoPrimeiro = txtDescontoPorcento.getText();
        if (textoPrimeiro == null) {
            textoPrimeiro = "0";
        }
        String textoSemPercentagem = textoPrimeiro.replace("%", "");
        textoSemPercentagem = textoSemPercentagem.replace(",", ".");

        String textoSegundo = txtDesconto.getText().replace(",", ".");;
        float descontoPorcento = -1;
        float desconto = -1;
        try {
            descontoPorcento = Float.valueOf(textoSemPercentagem);
            desconto = Float.valueOf(textoSegundo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        configGlobal config = configGlobal.getInstance();
        int limiteDesconto = config.getLimiteDesconto();
        if (numero == 1) {
            if (descontoPorcento >= 0) {
                if (config.getCargoUsuario().equals("comum")) {
                    if (descontoPorcento < limiteDesconto) {
                        realizaDescontoPorCento();
                    }else{
                        JOptionPane.showMessageDialog(null, "Desconto Excede o Permitido");
                    }
                } else {
                    realizaDescontoPorCento();
                }
            }

        }
        if (numero == 2) {
            if (desconto >= 0) {
                float porcentoDesconto = (desconto * 100) / valorTotalFinal;
                if (config.getCargoUsuario().equals("comum")) {
                    if (porcentoDesconto < limiteDesconto) {
                        realizaDesconto();
                    }else{
                        JOptionPane.showMessageDialog(null, "Desconto Excede o Permitido");
                    }
                } else {
                    realizaDesconto();
                }
            }

        }

    }

    public void realizaDesconto() {
        float valorTotal = 0;
        try {
            float valorDesconto = Float.parseFloat(txtDesconto.getText());
            float porcentoDesconto = (valorDesconto * 100) / valorTotalFinal;

            DecimalFormat formato = new DecimalFormat("#.##");
            String numeroFormatado = formato.format(porcentoDesconto);
            System.out.println("val tt era " + valorTotalFinal);
            System.out.println("val desc  " + valorDesconto);
            txtDescontoPorcento.setText(numeroFormatado + "%");
            valorTotal = valorTotalFinal - valorDesconto;

            final String mostrar = String.valueOf(valorTotal);
            SwingUtilities.invokeLater(() -> {
                lblValorTotal.setText("R$" + String.valueOf(mostrar));

            });
            outraTela.setValorTotal(valorTotal);
            lblAReceber.setText(mostrar);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "RealizaDesconto(): "+ e);
        }
    }

    public void realizaDescontoPorCento() {
        float valorTotal = 0;
        try {
            String textoPrimeiro = txtDescontoPorcento.getText();
            String textoSemPercentagem = textoPrimeiro.replace("%", "");
            textoSemPercentagem = textoSemPercentagem.replace(",", ".");
            float porcentoDesconto = Float.valueOf(textoSemPercentagem);
            float valorDesconto = (porcentoDesconto / 100) * valorTotalFinal;
            txtDesconto.setText(String.valueOf(valorDesconto));
            valorTotal = valorTotalFinal - valorDesconto;
            final String mostrar = String.valueOf(valorTotal);
            SwingUtilities.invokeLater(() -> {
                lblValorTotal.setText("R$" + String.valueOf(mostrar));
            });
            outraTela.setValorTotal(valorTotal);
            lblAReceber.setText(mostrar);

            //outraTela.setDesconto(porcentoDesconto, valorDesconto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void realizaAcrescimo() {
        float valorTotal = 0;
        try {
            float acrescimo = Float.valueOf(txtAcrescimo.getText());

            valorTotal = valorTotalFinal + acrescimo;
            final String mostrar = String.valueOf(valorTotal);
            SwingUtilities.invokeLater(() -> {
                lblValorTotal.setText("R$" + String.valueOf(mostrar));

            });
            outraTela.setValorTotal(valorTotal);
            lblAReceber.setText(mostrar);
            //outraTela.setAcrescimo(acrescimo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void txtAcrescimoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAcrescimoActionPerformed
        if (txtAcrescimo.getText() != null) {
            realizaAcrescimo();

        }

    }//GEN-LAST:event_txtAcrescimoActionPerformed

    private void txtJustificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtJustificaActionPerformed
        // setar justificativa
    }//GEN-LAST:event_txtJustificaActionPerformed

    private void txtValorRecebidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorRecebidoActionPerformed
        // TODO add your handling code here:
        try {
            String recebido = txtValorRecebido.getText();
            recebido = recebido.replace(",", ".");
            if (recebido != null) {
                float valorRecebido = Float.valueOf(recebido);
                valorTotalRecebido += valorRecebido;
            }
            String valorDebito;
            int indice = lblAReceber.getText().indexOf("R$");
            if (indice != -1) {
                valorDebito = lblAReceber.getText().substring(indice + 2);
                float valor = Float.valueOf(valorDebito);

                lblAReceber.setText("R$" + String.valueOf(valor - valorTotalRecebido));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_txtValorRecebidoActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();

        int selectedRow;
        selectedRow = tabela.getSelectedRow();
        if (selectedRow != -1) {
            float coluna5 = (float) modelo.getValueAt(selectedRow, 4);
            modelo.removeRow(selectedRow);

            atualizaLabel();
            setValorTotal();

        } else {
            JOptionPane.showMessageDialog(null, "Nenhum produto selecionado!");
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowOpened

    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel barraCima;
    private javax.swing.JButton btConferencia;
    private javax.swing.JButton btDebito;
    private javax.swing.JButton btDesistencia;
    private javax.swing.JButton btInserir;
    private javax.swing.JButton btSalvar;
    private javax.swing.JButton btVoltar;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelEncerramento;
    private javax.swing.JLabel lblAReceber;
    private javax.swing.JLabel lblFimLocacao;
    private javax.swing.JLabel lblHoraAdicional;
    private javax.swing.JLabel lblInicioLocacao;
    private javax.swing.JLabel lblNomeProduto;
    private javax.swing.JLabel lblTempoLocado;
    private javax.swing.JLabel lblValorConsumo;
    private javax.swing.JLabel lblValorConsumo2;
    private javax.swing.JLabel lblValorQuarto;
    private javax.swing.JLabel lblValorTotal;
    private javax.swing.JPanel painelInfo;
    private javax.swing.JPanel painelProdutos;
    private javax.swing.JPanel painelRecebimento;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtAcrescimo;
    private javax.swing.JTextField txtDesconto;
    private javax.swing.JTextField txtDescontoPorcento;
    private javax.swing.JTextField txtIdProduto;
    private javax.swing.JTextField txtJustifica;
    private javax.swing.JTextField txtPessoas;
    private javax.swing.JTextField txtQuantidade;
    private javax.swing.JTextField txtValorRecebido;
    // End of variables declaration//GEN-END:variables
}
