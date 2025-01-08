package com.motelinteligente.telas;

import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.dados.Antecipado;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CacheDados.DadosVendidos;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.NumeroPorExtenso;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.playSound;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
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
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

/**
 *
 * @author MOTEL
 */
public class EncerraQuarto extends javax.swing.JFrame {

    private boolean isFrameOpen = false, salvouLocacao = false;
    private JFrame secondaryFrame;
    int idLocacao = 0;
    private MediaPlayerFactory mediaPlayerFactory;
    private MediaPlayer mediaPlayer;
    private boolean recording = false;  // Controle de gravação
    private boolean jopAberto = false;
    String dataInicio, dataFim, tempoTotalLocado;
    float valorAcrescimo = 0, valorDesconto = 0;
    float valoreRecebido = 0, valorDivida = 0;
    float valorConsumo = 0, valorQuarto = 0, valorAdicionalPeriodo = 0, valorAdicionalPessoa = 0;
    float valD = 0, valP = 0, valC = 0;
    ClienteEncerra outraTela = new ClienteEncerra();
    int numeroDoQuarto;
    //int numeroDePessoas = 2;
    String motivo = null;
    private Timer timer;
    List<Antecipado> antecipados;

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
        // Definir o caminho para as bibliotecas nativas do VLC
        System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC");
        System.setProperty("VLC_PLUGIN_PATH", "C:\\Program Files\\VideoLAN\\VLC\\plugins");

        // Inicializa a fábrica e o media player
        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();
        startRecording(numeroQuarto);
        tabela.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 1) {
                    int row = e.getFirstRow();
                    int novaQuantidade = (int) tabela.getValueAt(row, 1);
                    float valorUnd = (float) tabela.getValueAt(row, 3);
                    float novoTotal = novaQuantidade * valorUnd;
                    tabela.setValueAt(novoTotal, row, 4); // Atualize a coluna "Total"

                    atualizaConsumo();
                }
            }

        });

        setValorDivida();

        setupKeyboardShortcuts();

        txtIdProduto.grabFocus();

        boolean found = new NativeDiscovery().discover();
        if (!found) {
            System.err.println("Não foi possível localizar as bibliotecas VLC. Certifique-se de que o VLC está instalado.");
            return;  // Sai do construtor se as bibliotecas não forem encontradas
        }

    }

    private void setupKeyboardShortcuts() {
        // Obtém o InputMap e ActionMap da janela principal (RootPane)
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        // Mapeia a tecla ESC para a ação de voltar
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btVoltar.doClick();  // Simula o clique no botão "Voltar"
            }
        });

        // Mapeia a tecla F9 para salvar
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "F9");
        actionMap.put("F9", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!jopAberto) {
                    jopAberto = true;
                    btSalvar.doClick();  // Simula o clique no botão "Salvar"
                    jopAberto = false;    // Reseta o estado após a ação
                }
            }
        });

        // Mapeia a tecla F2 para conferência
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");
        actionMap.put("F2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btConferencia.doClick();  // Simula o clique no botão "Conferência"
            }
        });

        // Mapeia a tecla F4 para débito
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "F4");
        actionMap.put("F4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btDebito.doClick();  // Simula o clique no botão "Débito"
            }
        });

        // Mapeia a tecla F6 para desistência
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "F6");
        actionMap.put("F6", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btDesistencia.doClick();  // Simula o clique no botão "Desistência"
            }
        });
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

        //setar valor quarto e adicionalPeriodo começa agora
        CacheDados cache = CacheDados.getInstancia();
        DadosOcupados ocupado = cache.getCacheOcupado().get(numeroQuarto);
        CarregaQuarto quarto = cache.getCacheQuarto().get(numeroQuarto);
        String status = quarto.getStatusQuarto();
        String horarioQuarto = quarto.getHoraStatus();
        String[] partes = status.split("-");
        
        txtPessoas.setText(String.valueOf(ocupado.getNumeroPessoas()));
        valorAdicionalPessoa = calculaAdicionalPessoa(ocupado.getNumeroPessoas());
        if (partes[1].equals("pernoite")) {
            valorQuarto = ocupado.getValorPernoite();
            int numeroAdicionais = subtrairHora(numeroQuarto, horarioQuarto, "pernoite");
            valorAdicionalPeriodo = Float.valueOf(numeroAdicionais) * ocupado.getValorAdicional();
            lblHoraAdicional.setText("R$" + String.valueOf(valorAdicionalPeriodo));
        } else if (partes[1].equals("periodo")) {
            valorQuarto = ocupado.getValorPeriodo();
            int numeroAdicionais = subtrairHora(numeroQuarto, horarioQuarto, ocupado.getTempoPeriodo());
            valorAdicionalPeriodo = Float.valueOf(numeroAdicionais) * ocupado.getValorAdicional();
            lblHoraAdicional.setText("R$" + String.valueOf(valorAdicionalPeriodo));
        }
        if (idLocacao == 0) {
            idLocacao = cache.getCacheOcupado().get(numeroQuarto).getIdLoca();
            if (idLocacao == 0) {
                DadosOcupados quartoOcupado = cache.getCacheOcupado().get(numeroQuarto);
                int novoID = new fquartos().getIdLocacao(numeroQuarto);
                idLocacao = novoID;

            }
        }

        adicionaPreVendidos(idLocacao);
        antecipados = verAntecipado(idLocacao);
        setValorDivida();

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
            atualizaConsumo();
        }
    }

    public List<Antecipado> verAntecipado(int locacao) {
        List<Antecipado> antecipados = new ArrayList<>();
        Connection link = null;
        float jaRecebeu = 0;
        try {
            link = new fazconexao().conectar();
            // Consulta SQL para buscar registros da tabela "antecipado" para a locação especificada
            String selectSQL = "SELECT tipo, valor, hora FROM antecipado WHERE idlocacao = ?";
            PreparedStatement preparedStatement = link.prepareStatement(selectSQL);
            preparedStatement.setInt(1, locacao);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Itera sobre os resultados da consulta
            while (resultSet.next()) {

                String tipo = resultSet.getString("tipo");
                float valor = resultSet.getFloat("valor");
                Timestamp hora = resultSet.getTimestamp("hora");

                // Cria o objeto Antecipado para cada registro retornado
                if (tipo.equals("desconto")) {
                    this.valorDesconto = valor;
                    txtDesconto.setText("" + valorDesconto);

                    txtJustifica.setText("negociado antecipado");
                } else {
                    System.out.println("setando valor");
                    jaRecebeu += valor;
                    Antecipado antecipado = new Antecipado(tipo, valor, hora);
                    antecipados.add(antecipado);
                }

            }
            valoreRecebido = jaRecebeu;
            txtRecebidoAntecipado.setText("" + jaRecebeu);
            // Fecha os recursos
            resultSet.close();
            preparedStatement.close();
            link.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar dados antecipados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Retorna a lista de pagamentos antecipados
        return antecipados;

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
        super.dispose();
        outraTela.dispose();
        stopRecording();

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
        btWifi = new javax.swing.JButton();
        painelProdutos = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtIdProduto = new javax.swing.JTextField();
        lblNomeProduto = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        btApagar = new javax.swing.JButton();
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
        txtValorDivida = new javax.swing.JLabel();
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
        txtRecebidoAntecipado = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtDesconto = new javax.swing.JTextField();
        txtDescontoPorcento = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtAcrescimo = new javax.swing.JTextField();
        labelEncerramento = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
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
        btDebito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/financeiro.png"))); // NOI18N
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

        btWifi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btWifi.setForeground(new java.awt.Color(204, 51, 0));
        btWifi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/wifi.png"))); // NOI18N
        btWifi.setText("Wi-Fi");
        btWifi.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btWifi.setIconTextGap(1);
        btWifi.setMaximumSize(new java.awt.Dimension(130, 79));
        btWifi.setMinimumSize(new java.awt.Dimension(130, 79));
        btWifi.setPreferredSize(new java.awt.Dimension(130, 79));
        btWifi.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btWifi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btWifiActionPerformed(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btWifi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        barraCimaLayout.setVerticalGroup(
            barraCimaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, barraCimaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(barraCimaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btWifi, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        jLabel7.setText("Cod Produto:");

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

        btApagar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btApagar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png"))); // NOI18N
        btApagar.setText("Apagar");
        btApagar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btApagarActionPerformed(evt);
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
                .addComponent(btApagar, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(btApagar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        txtValorDivida.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtValorDivida.setText("0,00");

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
        jPanel2.setForeground(new java.awt.Color(204, 204, 204));

        jLabel13.setBackground(new java.awt.Color(255, 51, 51));
        jLabel13.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(51, 51, 51));
        jLabel13.setText("        RECEBIMENTO");
        jLabel13.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel18.setText("Valor Pendente a Receber:");

        lblAReceber.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblAReceber.setForeground(new java.awt.Color(255, 51, 51));
        lblAReceber.setText("jLabel17");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel19.setText("Valor Recebido:");

        txtRecebidoAntecipado.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtRecebidoAntecipado.setMinimumSize(new java.awt.Dimension(80, 22));
        txtRecebidoAntecipado.setPreferredSize(new java.awt.Dimension(80, 22));
        txtRecebidoAntecipado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRecebidoAntecipadoActionPerformed(evt);
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
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 17, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addGap(18, 18, 18)
                .addComponent(txtRecebidoAntecipado, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(txtRecebidoAntecipado, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGroup(painelRecebimentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelRecebimentoLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtJustifica, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
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
                        .addComponent(txtDesconto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(painelInfoLayout.createSequentialGroup()
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                    .addComponent(lblValorConsumo2)))))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(txtValorDivida)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(txtValorDivida))
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
            .addComponent(barraCima, javax.swing.GroupLayout.DEFAULT_SIZE, 1137, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addComponent(labelEncerramento, javax.swing.GroupLayout.PREFERRED_SIZE, 606, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

                    //inseriu o produto
                    //agora atualiza os valores
                    atualizaConsumo();

                } else {
                    JOptionPane.showMessageDialog(rootPane, "Quantidade inválida!");

                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Código inserido invalido!");
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Digite um valor válido!");
        }

        setValorDivida();
        txtQuantidade.setText("");
        txtIdProduto.setText("");

    }//GEN-LAST:event_btInserirActionPerformed
    public void atualizaConsumo() {
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
            setValorDivida();

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
                valorAdicionalPessoa = novoVal;
            } catch (Exception e) {
                e.printStackTrace();
            }
            setValorDivida();

        }

    }//GEN-LAST:event_txtPessoasActionPerformed
    private void setValorDivida() {
        float valorSomar = valorAcrescimo + valorQuarto + valorConsumo + valorAdicionalPeriodo + valorAdicionalPessoa;
        valorDivida = valorSomar - valorDesconto;
        txtValorDivida.setText(String.valueOf(valorDivida));
        txtRecebidoAntecipado.setText(String.valueOf(valoreRecebido));
        lblValorQuarto.setText(String.valueOf(valorAdicionalPessoa + valorQuarto));
        lblValorConsumo.setText(String.valueOf(valorConsumo));
        lblAReceber.setText(String.valueOf(valorDivida - valoreRecebido));
        outraTela.setarValores(valorAdicionalPessoa + valorQuarto, valorAdicionalPeriodo);
        outraTela.setValorTotal(valorDivida);
        outraTela.setConsumo(valorConsumo);
    }
    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed
        if (motivo != null) {
            // foi dado desistencia
            salvaDesistencia();
        } else {
            if (valorDesconto > 0 || valorAcrescimo > 0) {
                // precisa de justificativa
                if (txtJustifica.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Precisa Justificativa!");
                } else {
                    if (chamaJOP()) {
                        if (valorDesconto > 0) {
                            salvaJustifica("desconto", valorDesconto);
                        }
                        if (valorAcrescimo > 0) {
                            salvaJustifica("acrescimo", valorAcrescimo);
                        }
                        salvaVendidos(numeroDoQuarto);
                    }
                }

            } else {
                // nao precisa justificativa
                if (chamaJOP()) {
                    salvaVendidos(numeroDoQuarto);

                }
            }
        }
        jopAberto = false;

    }//GEN-LAST:event_btSalvarActionPerformed
    public void salvaJustifica(String tipoValor, float valorSalvar) {
        Connection link = null;
        CacheDados cache = CacheDados.getInstancia();
        if (idLocacao == 0) {
            idLocacao = cache.getCacheOcupado().get(numeroDoQuarto).getIdLoca();
            if (idLocacao == 0) {
                DadosOcupados quartoOcupado = cache.getCacheOcupado().get(numeroDoQuarto);
                int novoID = new fquartos().getIdLocacao(numeroDoQuarto);
                idLocacao = novoID;

            }
        }
        try {
            link = new fazconexao().conectar();
            String consultaSQL = "INSERT INTO justificativa (idlocacao, valor, tipo, justificativa) VALUES (?, ?, ?, ?)";
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
        if (idLocacao == 0) {
            idLocacao = cache.getCacheOcupado().get(numeroDoQuarto).getIdLoca();
            if (idLocacao == 0) {
                DadosOcupados quartoOcupado = cache.getCacheOcupado().get(numeroDoQuarto);
                int novoID = new fquartos().getIdLocacao(numeroDoQuarto);
                idLocacao = novoID;

            }
        }
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
                JOptionPane.showMessageDialog(null, "Locação Excluida!");
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
        // Criação dos campos e botões
        JTextField valorField = new JTextField(10);
        JTextArea valoresRecebidosArea = new JTextArea(5, 20);
        valoresRecebidosArea.setEditable(false); // Não permitir edição
        JScrollPane scrollPane = new JScrollPane(valoresRecebidosArea); // Adicionando barra de rolagem

        // Ajuste visual para o JTextArea parecer desabilitado
        valoresRecebidosArea.setOpaque(false); // Deixa o fundo transparente (igual ao painel)
        valoresRecebidosArea.setBackground(new Color(240, 240, 240)); // Cor de fundo igual ao painel
        valoresRecebidosArea.setForeground(Color.BLACK); // Cor do texto
        valoresRecebidosArea.setBorder(BorderFactory.createEmptyBorder()); // Remove a borda

        Font biggerFont = new Font(valorField.getFont().getName(), valorField.getFont().getStyle(), 16);
        valorField.setFont(biggerFont);

        // Botões de pagamento
        JButton botaoCredito = new JButton("Crédito (C)");
        JButton botaoDebito = new JButton("Débito (D)");
        JButton botaoDinheiro = new JButton("Dinheiro (O)");
        JButton botaoPix = new JButton("Pix (P)");

        // Botões de ação
        JButton botaoAcertouDinheiro = new JButton("Acertou no Dinheiro (F6)");
        JButton botaoAcertouDebito = new JButton("Acertou no Débito (F7)");
        JButton botaoAcertouCredito = new JButton("Acertou no Crédito (F8)");
        JButton botaoAcertouPix = new JButton("Acertou no Pix (F9)");

        JButton botaoSalvar = new JButton("Salvar (S)");

        // Painel principal com layout nulo
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(700, 400)); // Defina um tamanho adequado para o painel
        panel.setSize(700, 400); // Definindo o tamanho físico do painel
        panel.setLayout(null); // Para posicionamento manual dos componentes

        // Definindo posição e tamanho dos componentes
        // Dispor os botões de pagamento em 2 colunas por 2 linhas
        botaoCredito.setBounds(50, 20, 150, 50);
        botaoDebito.setBounds(220, 20, 150, 50);
        botaoDinheiro.setBounds(50, 80, 150, 50);
        botaoPix.setBounds(220, 80, 150, 50);

        // Campo de valor e botão ENTER na mesma linha que os botões de pagamento
        JLabel labelValor = new JLabel("Valor:");
        labelValor.setBounds(390, 20, 60, 30); // Alinhado ao lado dos botões
        valorField.setBounds(450, 20, 150, 30);
        JButton botaoEnter = new JButton("ENTER");
        botaoEnter.setForeground(Color.RED); // Cor do texto vermelha
        botaoEnter.setBounds(610, 20, 80, 30); // Alinhado ao lado do valorField

        // Botões inferiores organizados em uma única linha
        botaoAcertouDinheiro.setBounds(50, 150, 170, 40);
        botaoAcertouDebito.setBounds(230, 150, 170, 40);
        botaoAcertouCredito.setBounds(410, 150, 170, 40);
        botaoAcertouPix.setBounds(590, 150, 170, 40);

        JLabel labelRecebidos = new JLabel("Valores Recebidos:");
        labelRecebidos.setBounds(50, 210, 200, 30);
        scrollPane.setBounds(50, 240, 350, 100);

        botaoSalvar.setBounds(420, 300, 100, 40);

        // Adicionando os componentes ao painel
        panel.add(botaoCredito);
        panel.add(botaoDebito);
        panel.add(botaoDinheiro);
        panel.add(botaoPix);
        panel.add(labelValor);
        panel.add(valorField);
        panel.add(botaoEnter);
        panel.add(botaoAcertouDinheiro);
        panel.add(botaoAcertouDebito);
        panel.add(botaoAcertouCredito);
        panel.add(botaoAcertouPix);
        panel.add(labelRecebidos);
        panel.add(scrollPane);
        panel.add(botaoSalvar);
        // Resetando a aparência dos botões para o estado não selecionado
        resetarBotoes(botaoCredito, botaoDebito, botaoDinheiro, botaoPix);

        // Variáveis de controle
        final String[] tipoPagamento = {""}; // Armazena o tipo de pagamento selecionado
        final float[] recebidoDin = {0}, recebidoPix = {0}, recebidoCredito = {0}, recebidoDebito = {0};
        boolean[] sucesso = {false}; // Variável de controle para indicar sucesso do pagamento

        // Listener comum para selecionar o tipo de pagamento e focar no campo de valor
        ActionListener selecionarMetodo = e -> {
            JButton source = (JButton) e.getSource();
            tipoPagamento[0] = source.getText().substring(source.getText().indexOf("(") + 1, source.getText().indexOf(")"));
            System.out.println(tipoPagamento[0]);

            // Marca o botão como selecionado visualmente
            resetarBotoes(botaoCredito, botaoDebito, botaoDinheiro, botaoPix);
            source.setBackground(Color.GRAY);
            source.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

            valorField.requestFocus();
        };

        // Adicionando o listener aos botões
        botaoCredito.addActionListener(selecionarMetodo);
        botaoDebito.addActionListener(selecionarMetodo);
        botaoDinheiro.addActionListener(selecionarMetodo);
        botaoPix.addActionListener(selecionarMetodo);

        // Função para atualizar a área de texto com os valores recebidos
        Runnable atualizarValoresRecebidos = () -> {
            valoresRecebidosArea.setText(""); // Limpa a área de texto
            if (recebidoCredito[0] > 0) {
                valoresRecebidosArea.append(String.format("%.2f Crédito\n", recebidoCredito[0]));
            }
            if (recebidoDebito[0] > 0) {
                valoresRecebidosArea.append(String.format("%.2f Débito\n", recebidoDebito[0]));
            }
            if (recebidoDin[0] > 0) {
                valoresRecebidosArea.append(String.format("%.2f Dinheiro\n", recebidoDin[0]));
            }
            if (recebidoPix[0] > 0) {
                valoresRecebidosArea.append(String.format("%.2f Pix\n", recebidoPix[0]));
            }
        };
        for (Antecipado antecipado : antecipados) {
            String tipo = antecipado.getTipo();
            float valor = antecipado.getValor();

            // Se quiser somar o total já pago de todos os tipos de pagamento
            switch (tipo) {
                case "credito":
                    recebidoCredito[0] += valor;

                    break;
                case "debito":
                    recebidoDebito[0] += valor;

                    break;
                case "dinheiro":
                    recebidoDin[0] += valor;

                    break;
                case "pix":
                    recebidoPix[0] += valor;
                    break;

            }
            atualizarValoresRecebidos.run(); // Atualiza os valores recebidos na área de texto

        }
        // Validação do campo de valor (apenas números, ponto e vírgula permitidos)
        valorField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Permitir números, ponto, vírgula, e teclas de controle como backspace
                if (!Character.isDigit(c) && c != '.' && c != ',' && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume(); // Ignorar o caractere
                }
            }
        });

        // Ação para quando o cliente pressionar Enter no campo de texto
        valorField.addActionListener(e -> {
            String valorInserido = valorField.getText().replace(",", ".");
            try {
                float valor = Float.parseFloat(valorInserido);

                switch (tipoPagamento[0]) {
                    case "C":
                        recebidoCredito[0] += valor;

                        break;
                    case "D":
                        recebidoDebito[0] += valor;

                        break;
                    case "O":
                        recebidoDin[0] += valor;

                        break;
                    case "P":
                        recebidoPix[0] += valor;

                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Selecione um tipo de pagamento antes de inserir o valor.");
                        break;
                }
                // Resetando a aparência dos botões para o estado não selecionado
                resetarBotoes(botaoCredito, botaoDebito, botaoDinheiro, botaoPix);
                valorField.setText(""); // Limpa o campo de valor após registrar
                atualizarValoresRecebidos.run(); // Atualiza os valores recebidos na área de texto
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Valor inválido. Insira um número válido.");
            }
        });
        // Definindo a ação para o botão Enter
        botaoEnter.addActionListener(e -> {
            valorField.requestFocus(); // Define o foco no campo de texto
            valorField.postActionEvent(); // Simula o pressionamento da tecla Enter
        });
        // Exibição do JOptionPane

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        JDialog dialog = optionPane.createDialog("Digite os valores recebidos");
        dialog.setSize(800, 500);
        // Adicionando suporte para atalhos de teclado (teclas C, D, O, P, S) no JRootPane do diálogo
        JRootPane rootPane = dialog.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("C"), "credito");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "debito");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("O"), "dinheiro");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("P"), "pix");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "salvar");

        rootPane.getActionMap().put("credito", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoCredito.doClick(); // Simula o clique do botão
            }
        });
        rootPane.getActionMap().put("debito", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoDebito.doClick();
            }
        });
        rootPane.getActionMap().put("dinheiro", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoDinheiro.doClick();
            }
        });
        rootPane.getActionMap().put("pix", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoPix.doClick();
            }
        });
        rootPane.getActionMap().put("salvar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoSalvar.doClick(); // Simula o clique do botão Salvar
            }
        });
        // Listener para acertar o valor no dinheiro
        botaoAcertouDinheiro.addActionListener(e -> {
            recebidoDin[0] = valorDivida - valoreRecebido;
            atualizarValoresRecebidos.run(); // Atualiza a área de texto com os valores recebidos
            resetarBotoes(botaoCredito, botaoDebito, botaoDinheiro, botaoPix);
        });

// Listener para acertar o valor no débito
        botaoAcertouDebito.addActionListener(e -> {
            recebidoDebito[0] = valorDivida - valoreRecebido;
            atualizarValoresRecebidos.run();
            resetarBotoes(botaoCredito, botaoDebito, botaoDinheiro, botaoPix);
        });

// Listener para acertar o valor no crédito
        botaoAcertouCredito.addActionListener(e -> {
            recebidoCredito[0] = valorDivida - valoreRecebido;
            atualizarValoresRecebidos.run();
            resetarBotoes(botaoCredito, botaoDebito, botaoDinheiro, botaoPix);
        });

// Listener para acertar o valor no pix
        botaoAcertouPix.addActionListener(e -> {
            recebidoPix[0] = valorDivida - valoreRecebido;
            atualizarValoresRecebidos.run();
            resetarBotoes(botaoCredito, botaoDebito, botaoDinheiro, botaoPix);
        });

// Adicionando atalhos de teclado F6, F7, F8, F9 no JRootPane
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F6"), "acertouDinheiro");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F7"), "acertouDebito");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F8"), "acertouCredito");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F9"), "acertouPix");

        rootPane.getActionMap().put("acertouDinheiro", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoAcertouDinheiro.doClick(); // Simula o clique do botão "Acertou no Dinheiro"
            }
        });
        rootPane.getActionMap().put("acertouDebito", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoAcertouDebito.doClick(); // Simula o clique do botão "Acertou no Débito"
            }
        });
        rootPane.getActionMap().put("acertouCredito", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoAcertouCredito.doClick(); // Simula o clique do botão "Acertou no Crédito"
            }
        });
        rootPane.getActionMap().put("acertouPix", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botaoAcertouPix.doClick(); // Simula o clique do botão "Acertou no Pix"
            }
        });
        // Ação do botão "Salvar"
        botaoSalvar.addActionListener(e -> {
            float totalRecebido = recebidoDin[0] + recebidoPix[0] + recebidoCredito[0] + recebidoDebito[0];

            // Verifica se o valor total recebido fecha com o valor da dívida
            if (totalRecebido == valorDivida) {
                valD = recebidoDin[0];
                valC = recebidoCredito[0] + recebidoDebito[0];
                valP = recebidoPix[0];
                sucesso[0] = true; // Indica que o pagamento foi bem-sucedido
                dialog.dispose(); // Fecha o JDialog
            } else {
                JOptionPane.showMessageDialog(null, "Valores divergentes! Total recebido: " + totalRecebido);
            }
        });
        dialog.setVisible(true);

        return sucesso[0]; // Retorna o status de sucesso do pagamento
    }

    private void resetarBotoes(JButton... botoes) {
        Dimension buttonSize = new Dimension(120, 40);
        for (JButton botao : botoes) {
            botao.setBackground(new Color(200, 200, 200)); // Fundo Light Gray quando não selecionado
            botao.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Borda padrão fina

            botao.setPreferredSize(buttonSize);
            botao.setMaximumSize(buttonSize); // Define o tamanho máximo
            botao.setMinimumSize(buttonSize); // Define o tamanho mínimo
        }
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
        return -1;
    }

    public void salvaVendidos(int numero) {
        salvouLocacao = true;
        fquartos quartodao = new fquartos();
        CacheDados cache = CacheDados.getInstancia();
        if (idLocacao == 0) {
            idLocacao = cache.getCacheOcupado().get(numeroDoQuarto).getIdLoca();
            if (idLocacao == 0) {
                DadosOcupados quartoOcupado = cache.getCacheOcupado().get(numeroDoQuarto);
                int novoID = new fquartos().getIdLocacao(numeroDoQuarto);
                idLocacao = novoID;

            }
        }
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
        //salvalocacao
        float valorDoQuarto = valorQuarto + valorAdicionalPeriodo + valorAdicionalPessoa;
        String horaFim = lblFimLocacao.getText();
        String horaInicio = lblInicioLocacao.getText();
        quartodao.salvaLocacao(idLocacao, Timestamp.valueOf(horaInicio), Timestamp.valueOf(horaFim), valorDoQuarto, valorConsumo, valD, valP, valC);
        new playSound().playSound("som/agradecemosPreferencia.wav");
        new ConectaArduino(999);

        // altera o status do quarto para limpeza
        mudaStatusNaCache(numeroDoQuarto, "limpeza");
        //retira da cache ocupados
        cache.getCacheOcupado().remove(numeroDoQuarto);

        //se tiver prevendidos ou negociados retira tambem
        if (cache.cacheProdutosVendidos.containsKey(idLocacao)) {
            cache.cacheProdutosVendidos.remove(idLocacao);
        }
        quartodao.setStatus(numeroDoQuarto, "limpeza");
        quartodao.adicionaRegistro(numeroDoQuarto, "limpeza");
        configGlobal config = configGlobal.getInstance();
        config.setMudanca(true);
        outraTela.dispose();
        this.dispose();
        JOptionPane.showMessageDialog(null, "Registro Salvo com sucesso!");
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

        outraTela.dispose();
        this.dispose();
    }//GEN-LAST:event_btVoltarActionPerformed

    private void btDebitoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDebitoActionPerformed
        // clicou falar débito
        String valorDebito;
        float valor = (valorDivida - valoreRecebido);
        String falar = "SuaConta " + NumeroPorExtenso.NumeroPorExtenso(valor) + " reais";
        String[] palavras = falar.split(" ");

        // Itere pelo array de palavras e imprima cada uma
        reproduzirSonsEmSequencia(palavras, 0);

    }//GEN-LAST:event_btDebitoActionPerformed
    public static void reproduzirSonsEmSequencia(String[] palavras, int indice) {
        if (indice < palavras.length) {
            String palavraAtual = palavras[indice];
            String caminhoSom = "/som/" + palavraAtual + ".wav";  // Caminho relativo dentro do .jar

            try (InputStream audioSrc = EncerraQuarto.class.getResourceAsStream(caminhoSom)) {
                if (audioSrc == null) {
                    System.out.println("Som não encontrado: " + caminhoSom);
                    return;
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc));
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
            txtValorDivida.setText("R$0.00");
            lblValorQuarto.setText("R$0.00");
            lblAReceber.setText("R$0.00");
            valorDivida = 0;
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
        float valorDesconto = 0, valorPorcento = 0;

        configGlobal config = configGlobal.getInstance();
        int limiteDesconto = config.getLimiteDesconto();
        if (numero == 1) {
            try {
                String semPorcentagem = txtDescontoPorcento.getText().replace("%", "");
                valorPorcento = Float.valueOf(semPorcentagem);
                valorDesconto = (valorPorcento / 100) * valorDivida;
                txtDesconto.setText("" + valorDesconto);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Digite um valor válido");
            }

        } else {
            valorDesconto = Float.valueOf(txtDesconto.getText());
            valorPorcento = (valorDesconto / valorDivida) * 100;
            txtDescontoPorcento.setText(valorPorcento + "%");
        }

        if (valorDesconto >= 0) {
            if (config.getCargoUsuario().equals("comum")) {
                if (valorPorcento < limiteDesconto) {
                    this.valorDesconto = valorDesconto;
                } else {
                    JOptionPane.showMessageDialog(null, "Desconto Excede o Permitido");
                }
            } else {
                this.valorDesconto = valorDesconto;
            }
        }
        setValorDivida();
    }


    private void txtAcrescimoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAcrescimoActionPerformed
        if (txtAcrescimo.getText() != null) {
            try {
                float valorAcrescimo = Float.valueOf(txtAcrescimo.getText());
                this.valorAcrescimo = valorAcrescimo;
                setValorDivida();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Valor de acréscimo incorreto");
                e.printStackTrace();
            }

        }

    }//GEN-LAST:event_txtAcrescimoActionPerformed

    private void txtJustificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtJustificaActionPerformed
        // setar justificativa
    }//GEN-LAST:event_txtJustificaActionPerformed

    private void txtRecebidoAntecipadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRecebidoAntecipadoActionPerformed
        // TODO add your handling code here:
        try {
            String recebido = txtRecebidoAntecipado.getText();
            recebido = recebido.replace(",", ".");
            if (recebido != null) {
                float valorRecebido = Float.valueOf(recebido);
                valoreRecebido = valorRecebido;
                setValorDivida();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_txtRecebidoAntecipadoActionPerformed

    private void btApagarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btApagarActionPerformed
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();

        int selectedRow;
        selectedRow = tabela.getSelectedRow();
        if (selectedRow != -1) {
            float coluna5 = (float) modelo.getValueAt(selectedRow, 4);
            modelo.removeRow(selectedRow);

            atualizaConsumo();
            setValorDivida();

        } else {
            JOptionPane.showMessageDialog(null, "Nenhum produto selecionado!");
        }
    }//GEN-LAST:event_btApagarActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
    }//GEN-LAST:event_formWindowOpened
    // Iniciar gravação

    private void startRecording(int numeroQuarto) {
        if (!recording) {
            CacheDados cache = CacheDados.getInstancia();
            if (idLocacao == 0) {
                idLocacao = cache.getCacheOcupado().get(numeroDoQuarto).getIdLoca();
                if (idLocacao == 0) {
                    DadosOcupados quartoOcupado = cache.getCacheOcupado().get(numeroDoQuarto);
                    int novoID = new fquartos().getIdLocacao(numeroDoQuarto);
                    idLocacao = novoID;

                }
            }
            // Caminho onde o vídeo será salvo (alterar conforme necessário)
            String outputFilePath = new File("src/main/resources/videos/temp", idLocacao + ".mp4").getAbsolutePath();

            // URL da câmera (RTSP)
            String mediaUrl = "rtsp://admin:Felipe0110@192.168.100.135:554/cam/realmonitor?channel=1&subtype=0";

            String[] mediaOptions = {
                ":sout=#file{dst=" + outputFilePath + "}",
                ":network-caching=1000", // Cache de rede para suavizar
                ":no-sout-all",
                ":sout-keep"
            };

            // Inicia a gravação diretamente, sem exibir vídeo
            mediaPlayer.media().play(mediaUrl, mediaOptions);
            recording = true;  // Marca que a gravação está em andamento
            System.out.println("Gravação iniciada.");
        }
    }

    // Parar gravação
    private void stopRecording() {
        if (recording) {
            // Para a gravação
            mediaPlayer.controls().stop();
            recording = false;  // Marca que a gravação foi interrompida
            String infoFilePath = new File("src/main/resources/videos/temp", "locacao_" + idLocacao + ".txt").getAbsolutePath();
            String videoFilePath = new File("src/main/resources/videos/temp", idLocacao + ".mp4").getAbsolutePath();

            if (salvouLocacao) {
                float valorDoQuarto = valorQuarto + valorAdicionalPeriodo + valorAdicionalPessoa;

                // Se a locação foi salva, move para a pasta definitivo
                saveInfoToFile(infoFilePath, idLocacao, valorDoQuarto);

                System.out.println("Gravação salva com sucesso.");
                Thread thread = new Thread(() -> {
                    try {
                        // Pausa de 2 segundos
                        Thread.sleep(2000);

                        // Chama o método para mover os arquivos
                        moveFilesToFinalFolder(videoFilePath, infoFilePath);
                        Thread.sleep(2000);
                        VideoProcessorTest processor = new VideoProcessorTest();
                        processor.checkVideoFolder();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // A thread termina automaticamente após isso.
                });
                thread.start();

            } else {
                // Se a locação não foi salva, exclui os arquivos temporários
                deleteTempFiles(videoFilePath, infoFilePath);
            }
        }

    }

    private void moveFilesToFinalFolder(String videoFilePath, String infoFilePath) {
        int maxRetries = 5; // Número máximo de tentativas
        int waitTime = 2000; // Tempo de espera entre as tentativas (em milissegundos)
        int retries = 0;

        while (retries < maxRetries) {
            try {
                // Tenta mover os arquivos
                Files.move(Path.of(videoFilePath), Path.of("src/main/resources/videos/definitivo", new File(videoFilePath).getName()), StandardCopyOption.REPLACE_EXISTING);
                Files.move(Path.of(infoFilePath), Path.of("src/main/resources/videos/definitivo", new File(infoFilePath).getName()), StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Arquivos movidos com sucesso.");
                return; // Se mover os arquivos com sucesso, sai do método

            } catch (IOException e) {
                // Se o arquivo estiver em uso, aguarda 2 segundos e tenta novamente
                if (e instanceof java.nio.file.FileSystemException) {
                    System.out.println("Arquivo em uso, aguardando " + waitTime + "ms antes de tentar novamente...");
                    try {
                        Thread.sleep(waitTime); // Aguarda antes de tentar novamente
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    retries++; // Incrementa o número de tentativas
                } else {
                    e.printStackTrace(); // Caso seja outro erro, imprime a exceção
                    return; // Caso contrário, sai do método
                }
            }
        }

        // Se não conseguiu mover os arquivos após várias tentativas
        System.out.println("Falha ao mover os arquivos após " + maxRetries + " tentativas.");
    }

    // Método para excluir arquivos temporários
    private void deleteTempFiles(String videoFilePath, String infoFilePath) {
        try {
            File tempVideoFile = new File(videoFilePath);
            File tempInfoFile = new File(infoFilePath);

            if (tempVideoFile.exists()) {
                boolean videoDeleted = tempVideoFile.delete();
                if (videoDeleted) {
                    System.out.println("Vídeo temporário excluído.");
                }
            }

            if (tempInfoFile.exists()) {
                boolean infoDeleted = tempInfoFile.delete();
                if (infoDeleted) {
                    System.out.println("Arquivo de informações temporário excluído.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveInfoToFile(String filePath, int idLocacao, float valorQuarto) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Locação: " + idLocacao);
            writer.newLine();
            writer.write("Número do Quarto: " + numeroDoQuarto);
            writer.newLine();
            writer.write("Entrada: " + (dataInicio));
            writer.newLine();
            writer.write("Saída: " + (dataFim));
            writer.newLine();
            writer.write(String.format("Valor do Quarto: R$ %.2f", valorQuarto));
            writer.newLine();
            writer.write(String.format("Consumo: R$ %.2f", valorConsumo));
            writer.newLine();
            writer.write(String.format("Desconto: R$ %.2f", valorDesconto));
            writer.newLine();
            writer.write(String.format("Acréscimo: R$ %.2f", valorAcrescimo));
            writer.newLine();
            writer.write(String.format("Pagou em Dinheiro: R$ %.2f", valD));
            writer.newLine();
            writer.write(String.format("Pagou com Cartão: R$ %.2f", valC));
            writer.newLine();
            writer.write(String.format("Pagou com Pix: R$ %.2f", valP));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkVideoFolder() {
        File folder = new File("src/main/resources/videos/definitivo");

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("A pasta de vídeos não foi encontrada!");
            return;
        }

        // Lista arquivos de vídeo na pasta
        File[] videoFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
        if (videoFiles == null) {
            System.out.println("Erro ao acessar a pasta de vídeos.");
            return;
        }

        System.out.println("Número de vídeos encontrados: " + videoFiles.length);

        if (videoFiles.length >= 5) {
            System.out.println("Temos 5 ou mais vídeos, hora de concatenar!");
            try {
                createBlackScreensForVideos();
                List<String> videoPaths = Arrays.stream(videoFiles)
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toList());
                concatenateVideos(videoPaths, "src/main/resources/videos/definitivo/concatenated_video.mp4");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Ainda não há vídeos suficientes para concatenar.");
        }
    }

// Lê os arquivos de texto e cria as telas pretas correspondentes
    public void createBlackScreensForVideos() throws IOException, InterruptedException {
        File folder = new File("src/main/resources/videos/definitivo");
        File[] textFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (textFiles == null) {
            System.out.println("Nenhum arquivo de texto encontrado.");
            return;
        }

        for (File textFile : textFiles) {
            String infoText = readTextFromFile(textFile);
            String idLocacao = textFile.getName().replace("locacao_", "").replace(".txt", "");
            String outputFilePath = new File("src/main/resources/videos/definitivo", "black_" + idLocacao + ".mp4").getAbsolutePath();
            createBlackScreen(infoText, outputFilePath);
        }
    }

// Lê o conteúdo do arquivo de texto
    private static String readTextFromFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\\n"); // \\n para quebra de linha no FFmpeg
            }
        }
        return content.toString();
    }

// Gera a tela preta com texto usando FFmpeg
    private void createBlackScreen(String text, String outputFilePath) throws IOException, InterruptedException {
        // Substitua "/path/to/font.ttf" pelo caminho completo de uma fonte TTF no seu sistema
        String fontFilePath = "C:/Windows/Fonts/Arial.ttf"; // Certifique-se de que o caminho para a fonte está correto
        String command = String.format("ffmpeg -f lavfi -i color=c=black:s=1280x720:d=5 -vf \"drawtext=fontfile='%s': "
                + "text='%s': fontcolor=white: fontsize=36: box=1: boxcolor=black@0.5: boxborderw=5: x=(w-text_w)/2: y=(h-text_h)/2\" "
                + "-codec:a copy \"%s\"",
                fontFilePath, text.replace("'", "\\'"), outputFilePath);

        executeCommand(command);
    }

    private void executeCommand(String command) {
        try {
            // Use "cmd" no Windows
            ProcessBuilder builder = new ProcessBuilder("cmd", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Lê a saída do processo
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

// Concatena os vídeos usando FFmpeg
    private void concatenateVideos(List<String> videoPaths, String outputVideoPath) {
        try {
            // Cria a string de comando para o FFMPEG
            StringBuilder command = new StringBuilder("ffmpeg -y -f concat -safe 0 -i ");
            String listFilePath = "src/main/resources/videos/temp/video_list.txt";

            // Cria o arquivo temporário com a lista de vídeos
            File tempFolder = new File("src/main/resources/videos/temp");
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }

            try (PrintWriter writer = new PrintWriter(listFilePath)) {
                for (String videoPath : videoPaths) {
                    writer.println("file '" + videoPath.replace("\\", "/") + "'");
                }
            }

            // Adiciona o caminho do arquivo de lista de vídeos ao comando
            command.append("\"").append(listFilePath).append("\"").append(" -c copy \"").append(outputVideoPath).append("\"");

            // Executa o comando usando o CMD no Windows
            executeCommand(command.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void btWifiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btWifiActionPerformed
        // mostra a img de conexao de wi-fi na 2a Tela
        if (!isFrameOpen) {
            // Obtém a tela a mostrar da instância global
            String telaMostrar = configGlobal.getInstance().getTelaMostrar();

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            // Verifica se há pelo menos uma tela
            if (screens.length > 0) {
                // Cria o frame secundário
                secondaryFrame = new JFrame("Conexão Wi-Fi");
                secondaryFrame.setSize(600, 600);
                secondaryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                // Carrega a imagem do Wi-Fi
                ImageIcon wifiImage = new ImageIcon(getClass().getResource("/imagens/conexaoWifi.jpg"));

                // Verifica se a imagem foi carregada corretamente
                if (wifiImage.getIconWidth() == -1) {
                    JOptionPane.showMessageDialog(this, "Imagem não encontrada!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JLabel imgLabel = new JLabel(wifiImage);
                secondaryFrame.add(imgLabel);

                // Define a localização do frame com base na tela selecionada
                boolean telaEncontrada = false;
                for (GraphicsDevice screen : screens) {
                    if (screen.getIDstring().equals(telaMostrar)) {
                        Rectangle bounds = screen.getDefaultConfiguration().getBounds();
                        secondaryFrame.setLocation(bounds.x, bounds.y);
                        telaEncontrada = true;
                        break; // Sai do loop após encontrar a tela correspondente
                    }
                }

                // Se não encontrar a tela, usa a primeira tela disponível
                if (!telaEncontrada) {
                    Rectangle bounds = screens[0].getDefaultConfiguration().getBounds();
                    secondaryFrame.setLocation(bounds.x, bounds.y);
                }

                secondaryFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                secondaryFrame.setVisible(true);

                // Altera a cor do botão para cinza escuro
                btWifi.setBackground(Color.DARK_GRAY);
                isFrameOpen = true;
            } else {
                JOptionPane.showMessageDialog(this, "Não há uma tela disponível.");
            }
        } else {
            // Fecha a tela secundária se já estiver aberta
            if (secondaryFrame != null) {
                secondaryFrame.dispose();
                secondaryFrame = null;
            }
            btWifi.setBackground(null);  // Restaura a cor original do botão
            isFrameOpen = false;
        }
    }//GEN-LAST:event_btWifiActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowActivated

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
    }//GEN-LAST:event_formWindowGainedFocus

    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel barraCima;
    private javax.swing.JButton btApagar;
    private javax.swing.JButton btConferencia;
    private javax.swing.JButton btDebito;
    private javax.swing.JButton btDesistencia;
    private javax.swing.JButton btInserir;
    private javax.swing.JButton btSalvar;
    private javax.swing.JButton btVoltar;
    private javax.swing.JButton btWifi;
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
    private javax.swing.JTextField txtRecebidoAntecipado;
    private javax.swing.JLabel txtValorDivida;
    // End of variables declaration//GEN-END:variables
}
