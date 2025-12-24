package com.motelinteligente.telas;

import com.motelinteligente.dados.TelaSistema;
import com.motelinteligente.alarme.AlarmApp;
import com.motelinteligente.alarme.FAlarmes;
import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.dados.Antecipado;
import com.motelinteligente.dados.BackupExecutor;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CacheDados.DadosVendidos;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.CheckSincronia;
import com.motelinteligente.dados.ConexaoRemota;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.playSound;
import com.motelinteligente.telas.Quadrado.QuartoClickListener;
import java.awt.BorderLayout;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.Timestamp;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TelaPrincipal extends javax.swing.JFrame implements QuartoClickListener {

    private String user = null;
    private String cargo = null;
    private int quartoEmFoco = 0;
    private int numeroQuartos = 0;
    private JPopupMenu popupMenu;
    private boolean isClickable = true;
    private Timer alarmTimer; // Timer para verificar os alarmes
    private long lastUpdate = 0;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TelaPrincipal.class);
    private EncerraQuarto encerraQuarto;
    private CaixaFrame caixaFrame;
    private static final long UPDATE_INTERVAL = 1000; // 1 segundo

    private class NumOnly extends PlainDocument {

        @Override
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
     * Creates new form TelaPrincipal
     */
    public TelaPrincipal() {
        initComponents();
        inicializarPopupMenu();

        // Inicializa o BackupExecutor
        new BackupExecutor().start();
        CheckSincronia.start();

        setExtendedState(MAXIMIZED_BOTH);
        iniciar();
        insereIcone(this);
        tabela1.getColumn(tabela1.getColumnName(0)).setPreferredWidth(35);
        tabela1.getColumn(tabela1.getColumnName(1)).setPreferredWidth(80);
        tabela1.getColumn(tabela1.getColumnName(2)).setPreferredWidth(35);
        tabela1.getColumn(tabela1.getColumnName(3)).setPreferredWidth(70);
        numeroQuartos = new fquartos().numeroQuartos();
        txtPessoas.setDocument(new NumOnly());

        this.setVisible(true);
        int idCaixaAtual = new fazconexao().verificaCaixa();

        if (idCaixaAtual == 0) {
            JOptionPane.showMessageDialog(null, "Precisa Abrir o Caixa!");
        } else {
            configGlobal configuracao = configGlobal.getInstance();
            configuracao.setCaixa(idCaixaAtual);
        }
        // Configurando o Key Binding para F2 diretamente no JFrame (this)
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        // Mapeia a tecla F2
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "playSound");

        // Define a ação quando F2 for pressionada
        actionMap.put("playSound", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Executa o código para tocar o som
                new playSound().playSound("som/mensagem conferencia.wav");
            }
        });

    }

    public void setLabel(String ini_user, String ini_cargo) {
        cargo = ini_cargo;
        user = ini_user;

        lblUsuario.setText(user);
        lblCargo.setText(cargo);
    }

    public void focoQuarto() {
        SwingUtilities.invokeLater(() -> {
            if (quartoEmFoco == 0) {
                quartoEmFoco = 0;
            }
            alteraPainel();
        });
    }

    private void checkAlarmsToRing() {
        configGlobal config = configGlobal.getInstance();

        if (config.getAlarmesAtivos() > 0) {
            try (Connection conn = new fazconexao().conectar();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt
                            .executeQuery("SELECT id, hora_despertar, descricao FROM alarmes WHERE ativo = TRUE")) {

                while (rs.next()) {
                    int idAlarme = rs.getInt("id");
                    Timestamp horaDespertar = rs.getTimestamp("hora_despertar");
                    String descricao = rs.getString("descricao");
                    // Obtém a data e hora atuais
                    Calendar agora = Calendar.getInstance();
                    int anoAtual = agora.get(Calendar.YEAR);
                    int mesAtual = agora.get(Calendar.MONTH) + 1; // Meses começam de 0, por isso somamos 1
                    int diaAtual = agora.get(Calendar.DAY_OF_MONTH);
                    int horaAtual = agora.get(Calendar.HOUR_OF_DAY);
                    int minutoAtual = agora.get(Calendar.MINUTE);

                    // Obtém a data e a hora do alarme
                    Calendar alarmeCalendar = Calendar.getInstance();
                    alarmeCalendar.setTime(horaDespertar);
                    int anoAlarme = alarmeCalendar.get(Calendar.YEAR);
                    int mesAlarme = alarmeCalendar.get(Calendar.MONTH) + 1;
                    int diaAlarme = alarmeCalendar.get(Calendar.DAY_OF_MONTH);
                    int horaAlarme = alarmeCalendar.get(Calendar.HOUR_OF_DAY);
                    int minutoAlarme = alarmeCalendar.get(Calendar.MINUTE);

                    // Saídas detalhadas para depuração
                    // Comparar data (ano, mês e dia)
                    if (anoAtual == anoAlarme && mesAtual == mesAlarme && diaAtual == diaAlarme) {

                        // Comparar hora e minuto
                        if (horaAtual == horaAlarme && minutoAtual == minutoAlarme) {
                            logger.warn("Alarme ID: " + idAlarme + " disparando agora. "
                                    + " Horário atual: " + alarmeCalendar);
                            showAlarmAlert(idAlarme, descricao); // Chama a função para mostrar o alerta
                        }

                    } else {
                        // Comparar se o alarme já passou da data atual

                        // Verificar se o alarme está no passado
                        if (anoAtual > anoAlarme
                                || (anoAtual == anoAlarme && mesAtual > mesAlarme)
                                || (anoAtual == anoAlarme && mesAtual == mesAlarme && diaAtual > diaAlarme)
                                || (anoAtual == anoAlarme && mesAtual == mesAlarme && diaAtual == diaAlarme
                                        && horaAtual > horaAlarme)
                                || (anoAtual == anoAlarme && mesAtual == mesAlarme && diaAtual == diaAlarme
                                        && horaAtual == horaAlarme && minutoAtual > minutoAlarme)) {

                            // Verificar se passou mais de 5 minutos
                            long diferencaMillis = agora.getTimeInMillis() - alarmeCalendar.getTimeInMillis();
                            long quinzeMinutosMillis = 5 * 60 * 1000; // 15 minutos em milissegundos
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String horarioAtual = sdf.format(agora.getTime());
                            String horarioAlarme = sdf.format(alarmeCalendar.getTime());
                            if (!(diferencaMillis <= quinzeMinutosMillis)) {
                                logger.warn("Alarme ID: " + idAlarme
                                        + " já passou mais de 5 minutos - deve remover. "
                                        + "Horário atual: " + horarioAtual
                                        + ", Horário do alarme: " + horarioAlarme);
                                new FAlarmes().removeAlarmFromDatabase(idAlarme);
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                logger.error("Erro ao verificar alarmes ativos", ex);
            }
        }
    }

    private boolean isTimeToRing(Time alarmTime) {
        Time currentTime = new Time(System.currentTimeMillis());
        return currentTime.equals(alarmTime); // Verifica se a hora atual é igual à do alarme
    }

    private void showAlarmAlert(int idAlarme, String description) {
        playSound soundPlayer = new playSound();
        soundPlayer.playSoundLoop("som/despertador.mp3"); // Caminho relativo

        JOptionPane.showMessageDialog(
                null,
                "<html><span style='font-size:32px;'>  " + description + " </span></html>",
                "Atenção!",
                JOptionPane.WARNING_MESSAGE);

        soundPlayer.stopSound(); // Para o som quando o alerta é fechado
        new FAlarmes().removeAlarmFromDatabase(idAlarme);
    }

    public void setaLabel() {
        lblEntrada.setText("Informações");

        if (lblValorConsumo.getText() != "-") {
            lblValorConsumo.setText("-");
        }
        if (lblValorQuarto.getText() != "-") {
            lblValorQuarto.setText("-");
        }
        if (lblHoraAdicional.getText() != "-") {
            lblHoraAdicional.setText("-");
        }
    }

    public void alteraPainel() {

        txtPessoas.setText("2");
        CacheDados cache = CacheDados.getInstancia();
        CarregaQuarto quarto = cache.getCacheQuarto().get(quartoEmFoco);
        if (quartoEmFoco > 0) {

            String status = quarto.getStatusQuarto();
            DefaultTableModel modelo = (DefaultTableModel) tabela1.getModel();
            modelo.setNumRows(0);
            txtDescontoNegociado.setText("0,00");
            txtAntecipado.setText("0,00");
            fquartos quartodao = new fquartos();
            if (!status.equals("ocupado")) {
                setaLabel();
                botaoTroca.setVisible(false);
            }

            txtPessoas.setEnabled(false);
            if (status.equals("livre")) {
                botaoStatus.setEnabled(true);
                alteradorPaineis.setEnabledAt(1, false);
                this.painelSecundario.setBackground(new Color(46, 204, 113)); // Emerald (Livre)
                // troca os botoes
                iniciar();

            }
            if (status.equals("manutencao")) {
                botaoStatus.setEnabled(true);
                alteradorPaineis.setEnabledAt(1, false);
                this.painelSecundario.setBackground(new Color(149, 165, 166)); // Concrete (Manutenção)
                iniciar();
            }
            if (status.equals("reservado")) {
                botaoStatus.setEnabled(true);
                alteradorPaineis.setEnabledAt(1, false);
                this.painelSecundario.setBackground(new Color(52, 152, 219)); // Sky Blue (Reservado)
                iniciar();
            }
            if (status.contains("ocupado")) {
                finalizar();
                botaoTroca.setVisible(true);

                alteradorPaineis.setEnabledAt(1, true);
                botaoStatus.setEnabled(true);
                txtPessoas.setEnabled(true);
                if (cache.getCacheOcupado().get(quartoEmFoco).getNumeroPessoas() == 0) {
                    cache.getCacheOcupado().get(quartoEmFoco).setNumeroPessoas(2);
                    txtPessoas.setText(String.valueOf(2));

                } else {
                    txtPessoas.setText(String.valueOf(cache.getCacheOcupado().get(quartoEmFoco).getNumeroPessoas()));

                }
                String[] partes = status.split("-");
                int idLoca = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();
                if (idLoca == 0) {
                    DadosOcupados quartoOcupado = cache.getCacheOcupado().get(quartoEmFoco);
                    int novoID = new fquartos().getIdLocacao(quartoEmFoco);
                    quartoOcupado.setIdLoca(novoID);
                    cache.getCacheOcupado().put(quartoEmFoco, quartoOcupado);
                }
                // insere prevendidos tabela
                populaPrevendidos();
                atualizaAntecipado(idLoca);
                if (partes[1].equals("pernoite")) {
                    this.painelSecundario.setBackground(new Color(155, 89, 182)); // Amethyst (Pernoite)
                } else if (partes[1].equals("periodo")) {
                    this.painelSecundario.setBackground(new Color(231, 76, 60)); // Alizarin (Período/Ocupado)
                }
                setValorQuarto();
            }
            if (status.equals("limpeza")) {
                botaoStatus.setEnabled(true);
                alteradorPaineis.setEnabledAt(1, false);
                this.painelSecundario.setBackground(new Color(241, 196, 15)); // Sunflower (Limpeza)
                iniciar();
            }

            lblNumero.setText(String.valueOf(quartoEmFoco));
            painelSecundario.repaint();
        }
    }

    public void populaPrevendidos() {

        // A conexão agora também está em try-with-resources
        try (Connection link = new fazconexao().conectar()) {

            DefaultTableModel modelo = (DefaultTableModel) tabela1.getModel();
            CacheDados cache = CacheDados.getInstancia();
            modelo.setNumRows(0);
            int locacao = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();
            float totalVendido = 0;
            String consultaProdutosSQL = "SELECT idproduto, quantidade FROM prevendidos WHERE idlocacao = ?";
            try (PreparedStatement statementProdutos = link.prepareStatement(consultaProdutosSQL)) {
                statementProdutos.setInt(1, locacao);
                try (ResultSet resultadoProdutos = statementProdutos.executeQuery()) {
                    fprodutos produtoDao = new fprodutos();
                    while (resultadoProdutos.next()) {
                        int idProduto = resultadoProdutos.getInt("idproduto");
                        int quantidade = resultadoProdutos.getInt("quantidade");
                        String desc = produtoDao.getDescicao(String.valueOf(idProduto));
                        float valor = produtoDao.getValorProduto(idProduto);
                        float total = valor * quantidade;
                        totalVendido += total;

                        modelo.addRow(new Object[] {
                                quantidade,
                                desc,
                                valor,
                                total
                        });
                    }

                    lblValorConsumo.setText("R$ " + totalVendido);
                    lblValorConsumo.repaint();
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(this.getClass().getName())
                    .log(Level.SEVERE, "Erro em populaPrevendidos: ", e);
        }
    }

    public static String formatarData(String dataOriginal) {
        // Definindo o formato da string de data original
        SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

        try {
            // Fazendo o parsing da string de data original para um objeto Date
            Date data = formatoOriginal.parse(dataOriginal);

            // Definindo o formato desejado
            SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM HH:mm");

            // Formatando a data para o formato desejado
            return formatoDesejado.format(data);
        } catch (ParseException e) {
            logger.error("Erro na função formatarData", e);
            JOptionPane.showMessageDialog(null, "Erro na função FORMATARDATA: " + e.getMessage());
            return null;
        }
    }

    public void setValorQuarto() {
        CacheDados cache = CacheDados.getInstancia();
        DadosOcupados ocupado = cache.getCacheOcupado().get(quartoEmFoco);
        CarregaQuarto quarto = cache.getCacheQuarto().get(quartoEmFoco);
        String status = quarto.getStatusQuarto();
        String horarioQuarto = quarto.getHoraStatus();
        lblEntrada.setText(formatarData(horarioQuarto));

        String[] partes = status.split("-");
        if (partes[1].equals("pernoite")) {
            float valor = ocupado.getValorPernoite();
            // adiciona o valor do adicional de pessoas
            valor += calculaAdicionalPessoa(ocupado.getNumeroPessoas());
            lblValorQuarto.setText(String.valueOf(valor));
            int numeroAdicionais = subtrairHora(quartoEmFoco, horarioQuarto, "pernoite");
            Float valorAdicional = Float.valueOf(numeroAdicionais) * ocupado.getValorAdicional();
            lblHoraAdicional.setText(String.valueOf(valorAdicional));
        } else if (partes[1].equals("periodo")) {
            float valor = ocupado.getValorPeriodo();
            // adiciona o valor do adicional de pessoas
            valor += calculaAdicionalPessoa(ocupado.getNumeroPessoas());
            lblValorQuarto.setText(String.valueOf(valor));
            int numeroAdicionais = subtrairHora(quartoEmFoco, horarioQuarto, ocupado.getTempoPeriodo());
            Float valorAdicional = Float.valueOf(numeroAdicionais) * ocupado.getValorAdicional();
            lblHoraAdicional.setText(String.valueOf(valorAdicional));
        }

    }

    public float calculaAdicionalPessoa(int numeroPessoas) {
        int add = (numeroPessoas - 2) * 30;
        if (add >= 0) {
            return add;
        } else {
            return 0;
        }
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
            DadosOcupados ocupado = cache.getCacheOcupado().get(quartoEmFoco);
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

    public void iniciar() {
        SwingUtilities.invokeLater(() -> {
            botaoEncerrar.setVisible(false);
            botaoIniciar.setVisible(true);

        });
    }

    public void finalizar() {
        SwingUtilities.invokeLater(() -> {
            botaoEncerrar.setVisible(true);
            botaoIniciar.setVisible(false);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator2 = new javax.swing.JSeparator();
        jFrame1 = new javax.swing.JFrame();
        jFrame2 = new javax.swing.JFrame();
        jFrame3 = new javax.swing.JFrame();
        jFrame4 = new javax.swing.JFrame();
        jMenuItem2 = new javax.swing.JMenuItem();
        menuStatus = new javax.swing.JPopupMenu();
        itemReserva = new javax.swing.JMenuItem();
        itemManutencao = new javax.swing.JMenuItem();
        menuLimpeza = new javax.swing.JPopupMenu();
        limpezaDisponivel = new javax.swing.JMenuItem();
        limpezaManutencao = new javax.swing.JMenuItem();
        limpezaReserva = new javax.swing.JMenuItem();
        menuOcupado = new javax.swing.JPopupMenu();
        radioPernoite = new javax.swing.JRadioButtonMenuItem();
        radioPeriodo = new javax.swing.JRadioButtonMenuItem();
        jTextField2 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        lblPermissao = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblCargo = new javax.swing.JLabel();
        painelSecundario2 = new javax.swing.JDesktopPane();
        alteradorPaineis = new javax.swing.JTabbedPane();
        painelSecundario = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblNumero = new javax.swing.JLabel();
        botaoStatus = new javax.swing.JButton();
        lblEntrada = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        botaoEncerrar = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        botaoIniciar = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblValorQuarto = new javax.swing.JLabel();
        txtPessoas = new javax.swing.JTextField();
        lblValorConsumo = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        lblHoraAdicional = new javax.swing.JLabel();
        botaoTroca = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabela1 = new javax.swing.JTable();
        bt_apagarProduto = new javax.swing.JButton();
        bt_inserirProduto = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtAntecipado = new javax.swing.JTextField();
        txtDescontoNegociado = new javax.swing.JTextField();
        bt_Antecipado = new javax.swing.JButton();
        btNegociar = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        painelQuartos = new javax.swing.JPanel();
        tabela = new javax.swing.JPanel();
        srPane = new javax.swing.JDesktopPane();
        painelBotton = new javax.swing.JPanel();
        labelHora = new javax.swing.JLabel();
        labelData = new javax.swing.JLabel();
        lblAlarmeAtivo = new javax.swing.JLabel();
        painelReservasProximas = new javax.swing.JPanel();
        labelReservas = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        btCadastros = new javax.swing.JMenu();
        btQuartos = new javax.swing.JMenu();
        btFuncionario = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        menuCadastraProduto = new javax.swing.JMenuItem();
        menuVerProdutos = new javax.swing.JMenuItem();
        menuCaixaBt = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jMenu16 = new javax.swing.JMenu();
        btConfereCaixa = new javax.swing.JMenuItem();
        btConfereLocacao = new javax.swing.JMenuItem();
        jMenu18 = new javax.swing.JMenu();
        menuRelaVenProdutos = new javax.swing.JMenuItem();
        btConferencia = new javax.swing.JMenu();
        btFerramentas = new javax.swing.JMenu();
        menuConfigAd = new javax.swing.JMenu();
        menuSobSistema = new javax.swing.JMenuItem();
        btDespertador = new javax.swing.JMenu();
        menuReservas = new javax.swing.JMenu();
        menuSistema = new javax.swing.JMenu();
        menuResBackup = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        btMenuSair = new javax.swing.JMenu();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        jFrame1Layout.setVerticalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
                jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        jFrame2Layout.setVerticalGroup(
                jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        javax.swing.GroupLayout jFrame3Layout = new javax.swing.GroupLayout(jFrame3.getContentPane());
        jFrame3.getContentPane().setLayout(jFrame3Layout);
        jFrame3Layout.setHorizontalGroup(
                jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        jFrame3Layout.setVerticalGroup(
                jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        javax.swing.GroupLayout jFrame4Layout = new javax.swing.GroupLayout(jFrame4.getContentPane());
        jFrame4.getContentPane().setLayout(jFrame4Layout);
        jFrame4Layout.setHorizontalGroup(
                jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        jFrame4Layout.setVerticalGroup(
                jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        jMenuItem2.setText("jMenuItem2");

        itemReserva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/reservas.png"))); // NOI18N
        itemReserva.setText("Reservar Quarto");
        itemReserva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemReservaActionPerformed(evt);
            }
        });
        menuStatus.add(itemReserva);

        itemManutencao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/ferramentas.png"))); // NOI18N
        itemManutencao.setText("Iniciar Manutenção");
        itemManutencao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemManutencaoActionPerformed(evt);
            }
        });
        menuStatus.add(itemManutencao);

        limpezaDisponivel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        limpezaDisponivel
                .setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sign-check-icon_34365.png"))); // NOI18N
        limpezaDisponivel.setText("Disponibilizar Quarto");
        limpezaDisponivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpezaDisponivelActionPerformed(evt);
            }
        });
        menuLimpeza.add(limpezaDisponivel);

        limpezaManutencao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        limpezaManutencao.setIcon(
                new javax.swing.ImageIcon(getClass().getResource("/imagens/bill_payment_note_icon_143263.png"))); // NOI18N
        limpezaManutencao.setText("Iniciar Manutencao");
        limpezaManutencao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpezaManutencaoActionPerformed(evt);
            }
        });
        menuLimpeza.add(limpezaManutencao);

        limpezaReserva.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        limpezaReserva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/calendar_30000.png"))); // NOI18N
        limpezaReserva.setText("Reservar Quarto");
        limpezaReserva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpezaReservaActionPerformed(evt);
            }
        });
        menuLimpeza.add(limpezaReserva);

        radioPernoite.setSelected(true);
        radioPernoite.setText("Pernoite");
        radioPernoite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPernoiteActionPerformed(evt);
            }
        });
        menuOcupado.add(radioPernoite);

        radioPeriodo.setSelected(true);
        radioPeriodo.setText("Período");
        radioPeriodo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPeriodoActionPerformed(evt);
            }
        });
        menuOcupado.add(radioPeriodo);

        jTextField2.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Motel Intensy - Principal");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }

            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setMaximumSize(new java.awt.Dimension(473, 643));
        jPanel2.setRequestFocusEnabled(false);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/iconeMI.jpg"))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel5.setText("Usuário:");

        lblPermissao.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        lblPermissao.setText("Permissão:");

        lblUsuario.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        lblUsuario.setForeground(new java.awt.Color(51, 51, 51));
        lblUsuario.setText("Nome");
        lblUsuario.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                lblUsuarioPropertyChange(evt);
            }
        });

        lblCargo.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        lblCargo.setForeground(new java.awt.Color(51, 51, 51));
        lblCargo.setText("texto");

        painelSecundario2.setBackground(new java.awt.Color(204, 204, 204));

        alteradorPaineis.setBackground(new java.awt.Color(255, 255, 255));
        alteradorPaineis.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Horas Adicionais");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("R$");

        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setOpaque(false);

        lblNumero.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblNumero.setText(" ");

        botaoStatus.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        botaoStatus.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/imagens/flat-style-circle-edit_icon-icons.com_66939.png"))); // NOI18N
        botaoStatus.setText("Alterar STATUS do Quarto");
        botaoStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoStatusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(lblNumero)
                                .addGap(18, 18, 18)
                                .addComponent(botaoStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 337,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblNumero)
                                        .addComponent(botaoStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))));

        lblEntrada.setFont(new java.awt.Font("Nunito", 1, 18)); // NOI18N
        lblEntrada.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEntrada.setText("Informações");
        lblEntrada.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Valor Quarto:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("R$");

        botaoEncerrar.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        botaoEncerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/Save_37110.png"))); // NOI18N
        botaoEncerrar.setText("   Encerrar");
        botaoEncerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoEncerrarActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Valor Consumo:");

        botaoIniciar.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        botaoIniciar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/ic_hotel_128_28520.png"))); // NOI18N
        botaoIniciar.setText("   Iniciar");
        botaoIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoIniciarActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("R$");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Pessoas no Quarto:");

        lblValorQuarto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblValorQuarto.setText("0,00");

        txtPessoas.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPessoas.setText("2");
        txtPessoas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPessoasActionPerformed(evt);
            }
        });

        lblValorConsumo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblValorConsumo.setText("0,00");

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/portao_fechado.png"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        lblHoraAdicional.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHoraAdicional.setText("0,00");

        botaoTroca.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        botaoTroca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_quarto.png"))); // NOI18N
        botaoTroca.setText("Trocar Quarto");
        botaoTroca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoTrocaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout painelSecundarioLayout = new javax.swing.GroupLayout(painelSecundario);
        painelSecundario.setLayout(painelSecundarioLayout);
        painelSecundarioLayout.setHorizontalGroup(
                painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(botaoEncerrar, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addGroup(painelSecundarioLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                .addGroup(painelSecundarioLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                                .addComponent(jLabel9)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel10))
                                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                                .addComponent(jLabel7)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jLabel8)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(painelSecundarioLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblValorConsumo)
                                                        .addComponent(lblValorQuarto))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(painelSecundarioLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                                .addComponent(jLabel11)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jLabel12)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(lblHoraAdicional))
                                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                                .addComponent(jLabel13)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(txtPessoas,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 43,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(33, 33, 33))
                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                .addComponent(jButton4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 306,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE))))
                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addGroup(painelSecundarioLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(botaoIniciar, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(botaoTroca, javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap()));
        painelSecundarioLayout.setVerticalGroup(
                painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelSecundarioLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jButton4)
                                        .addComponent(lblEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 39,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(painelSecundarioLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                .addGroup(painelSecundarioLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel7)
                                                        .addComponent(jLabel8))
                                                .addGap(16, 16, 16)
                                                .addGroup(painelSecundarioLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel9)
                                                        .addComponent(jLabel10)))
                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                .addGroup(painelSecundarioLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblValorQuarto)
                                                        .addComponent(jLabel11)
                                                        .addComponent(jLabel12)
                                                        .addComponent(lblHoraAdicional))
                                                .addGroup(painelSecundarioLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                                .addGap(16, 16, 16)
                                                                .addGroup(painelSecundarioLayout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(lblValorConsumo)
                                                                        .addComponent(jLabel13)))
                                                        .addGroup(painelSecundarioLayout.createSequentialGroup()
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(txtPessoas,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(2, 2, 2)))
                                .addGap(12, 12, 12)
                                .addComponent(botaoEncerrar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botaoIniciar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botaoTroca)
                                .addGap(5, 5, 5)));

        alteradorPaineis.addTab("Principal", painelSecundario);

        tabela1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabela1.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Qnt", "Descrição", "Valor und", "Valor Total"
                }) {
            Class[] types = new Class[] {
                    java.lang.Integer.class, java.lang.String.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean[] {
                    false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabela1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane2.setViewportView(tabela1);
        if (tabela1.getColumnModel().getColumnCount() > 0) {
            tabela1.getColumnModel().getColumn(0).setPreferredWidth(15);
            tabela1.getColumnModel().getColumn(2).setResizable(false);
            tabela1.getColumnModel().getColumn(2).setPreferredWidth(35);
            tabela1.getColumnModel().getColumn(3).setResizable(false);
            tabela1.getColumnModel().getColumn(3).setPreferredWidth(35);
        }

        bt_apagarProduto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png"))); // NOI18N
        bt_apagarProduto.setText("Apagar Produto");
        bt_apagarProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_apagarProdutoActionPerformed(evt);
            }
        });

        bt_inserirProduto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_editar.png"))); // NOI18N
        bt_inserirProduto.setText("Inserir Produto");
        bt_inserirProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_inserirProdutoActionPerformed(evt);
            }
        });

        jPanel6.setBackground(new java.awt.Color(204, 204, 204));

        jLabel1.setFont(new java.awt.Font("Palatino Linotype", 0, 14)); // NOI18N
        jLabel1.setText("Já recebeu:");

        jLabel3.setFont(new java.awt.Font("Palatino Linotype", 0, 14)); // NOI18N
        jLabel3.setText("Desconto Negociado:");

        txtAntecipado.setFont(new java.awt.Font("Palatino Linotype", 0, 14)); // NOI18N
        txtAntecipado.setText("0.00");
        txtAntecipado.setFocusable(false);
        txtAntecipado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAntecipadoActionPerformed(evt);
            }
        });

        txtDescontoNegociado.setFont(new java.awt.Font("Palatino Linotype", 0, 14)); // NOI18N
        txtDescontoNegociado.setText("0,00");
        txtDescontoNegociado.setFocusable(false);
        txtDescontoNegociado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescontoNegociadoActionPerformed(evt);
            }
        });

        bt_Antecipado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_conta_recebe.png"))); // NOI18N
        bt_Antecipado.setText("Receber Antecipado");
        bt_Antecipado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_AntecipadoActionPerformed(evt);
            }
        });

        btNegociar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_conta_paga.png"))); // NOI18N
        btNegociar.setText("Negociar Valor");
        btNegociar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btNegociarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel6Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(txtDescontoNegociado,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 157,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(jPanel6Layout.createSequentialGroup()
                                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(txtAntecipado,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 157,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(bt_Antecipado, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btNegociar, javax.swing.GroupLayout.PREFERRED_SIZE, 175,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(45, Short.MAX_VALUE)));
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addContainerGap(24, Short.MAX_VALUE)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(bt_Antecipado)
                                        .addComponent(btNegociar))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(txtAntecipado, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(txtDescontoNegociado, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(36, 36, 36)));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(10, 10, 10))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(bt_inserirProduto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bt_apagarProduto)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 386,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 135,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(bt_inserirProduto)
                                        .addComponent(bt_apagarProduto))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        alteradorPaineis.addTab("Antecipado", jPanel5);

        painelSecundario2.setLayer(alteradorPaineis, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout painelSecundario2Layout = new javax.swing.GroupLayout(painelSecundario2);
        painelSecundario2.setLayout(painelSecundario2Layout);
        painelSecundario2Layout.setHorizontalGroup(
                painelSecundario2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelSecundario2Layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(alteradorPaineis)));
        painelSecundario2Layout.setVerticalGroup(
                painelSecundario2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                painelSecundario2Layout.createSequentialGroup()
                                        .addGap(5, 5, 5)
                                        .addComponent(alteradorPaineis)));

        jButton1.setFont(new java.awt.Font("Tw Cen MT", 3, 18)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/bt_entrada_icone.png"))); // NOI18N
        jButton1.setText("ENTRADA");
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.setFocusable(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tw Cen MT", 3, 18)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/bt_saida_icone.png"))); // NOI18N
        jButton3.setText("SAÍDA");
        jButton3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(102, 102, 102));
        jButton2.setFont(new java.awt.Font("Tw Cen MT", 3, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("CORTINA");
        jButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton2.setFocusable(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout
                                                .createSequentialGroup()
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jSeparator5,
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addGroup(jPanel2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jSeparator3,
                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                                .addGap(132, 132, 132)
                                                                                .addGroup(jPanel2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(jLabel5)
                                                                                        .addComponent(lblPermissao))
                                                                                .addGap(32, 32, 32)
                                                                                .addGroup(jPanel2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(lblCargo)
                                                                                        .addComponent(lblUsuario))
                                                                                .addGap(0, 0, Short.MAX_VALUE)))
                                                                .addGap(35, 35, 35)))
                                                .addGap(17, 17, 17))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout
                                                .createSequentialGroup()
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addGap(6, 6, 6)
                                                                .addComponent(jButton1,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 153,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jButton2,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 72,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jButton3,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 154,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(painelSecundario2))
                                                .addGap(35, 35, 35))))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(195, 195, 195)
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE)));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5)
                                        .addComponent(lblUsuario))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblPermissao)
                                        .addComponent(lblCargo))
                                .addGap(0, 0, 0)
                                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton3)
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 52,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(1, 1, 1)
                                .addComponent(painelSecundario2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));

        jScrollPane1.setViewportView(jPanel2);

        painelQuartos.setBackground(new java.awt.Color(0, 0, 0));
        painelQuartos.setLayout(new java.awt.GridLayout(1, 0));

        tabela.setLayout(new java.awt.GridLayout(1, 0));

        srPane.setMaximumSize(new java.awt.Dimension(1500, 1500));
        srPane.setMinimumSize(new java.awt.Dimension(500, 500));

        javax.swing.GroupLayout srPaneLayout = new javax.swing.GroupLayout(srPane);
        srPane.setLayout(srPaneLayout);
        srPaneLayout.setHorizontalGroup(
                srPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 981, Short.MAX_VALUE));
        srPaneLayout.setVerticalGroup(
                srPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 556, Short.MAX_VALUE));

        painelBotton.setBackground(new java.awt.Color(255, 255, 255));
        painelBotton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        painelBotton.setMaximumSize(new java.awt.Dimension(990, 80));
        painelBotton.setMinimumSize(new java.awt.Dimension(990, 80));

        labelHora.setText("hora hora hora");

        labelData.setText("data data data");

        lblAlarmeAtivo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/bell.png"))); // NOI18N

        painelReservasProximas.setBackground(new java.awt.Color(255, 255, 255));
        painelReservasProximas.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        painelReservasProximas.setMaximumSize(new java.awt.Dimension(600, 80));
        painelReservasProximas.setMinimumSize(new java.awt.Dimension(500, 66));
        painelReservasProximas.setPreferredSize(new java.awt.Dimension(568, 80));

        labelReservas.setText("txt");

        javax.swing.GroupLayout painelReservasProximasLayout = new javax.swing.GroupLayout(painelReservasProximas);
        painelReservasProximas.setLayout(painelReservasProximasLayout);
        painelReservasProximasLayout.setHorizontalGroup(
                painelReservasProximasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelReservasProximasLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labelReservas, javax.swing.GroupLayout.PREFERRED_SIZE, 554,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));
        painelReservasProximasLayout.setVerticalGroup(
                painelReservasProximasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelReservasProximasLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labelReservas, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap()));

        javax.swing.GroupLayout painelBottonLayout = new javax.swing.GroupLayout(painelBotton);
        painelBotton.setLayout(painelBottonLayout);
        painelBottonLayout.setHorizontalGroup(
                painelBottonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelBottonLayout.createSequentialGroup()
                                .addGap(185, 185, 185)
                                .addComponent(painelReservasProximas, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 242,
                                        Short.MAX_VALUE)
                                .addComponent(lblAlarmeAtivo, javax.swing.GroupLayout.PREFERRED_SIZE, 51,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(labelData)
                                .addGap(18, 18, 18)
                                .addComponent(labelHora)
                                .addGap(236, 236, 236)));
        painelBottonLayout.setVerticalGroup(
                painelBottonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelBottonLayout.createSequentialGroup()
                                .addContainerGap(61, Short.MAX_VALUE)
                                .addGroup(painelBottonLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblAlarmeAtivo)
                                        .addGroup(painelBottonLayout.createSequentialGroup()
                                                .addGap(8, 8, 8)
                                                .addGroup(painelBottonLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(labelData)
                                                        .addComponent(labelHora))))
                                .addGap(20, 20, 20))
                        .addComponent(painelReservasProximas, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE));

        btCadastros.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/cadastro.png"))); // NOI18N
        btCadastros.setText("Cadastros    |");
        btCadastros.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        btQuartos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_quarto.png"))); // NOI18N
        btQuartos.setText("Quartos");
        btQuartos.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btQuartos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btQuartosMouseClicked(evt);
            }
        });
        btQuartos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btQuartosActionPerformed(evt);
            }
        });
        btCadastros.add(btQuartos);

        btFuncionario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_funcionario.png"))); // NOI18N
        btFuncionario.setText("Funcionários");
        btFuncionario.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btFuncionario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btFuncionarioMouseClicked(evt);
            }
        });
        btFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFuncionarioActionPerformed(evt);
            }
        });
        btCadastros.add(btFuncionario);

        jMenuBar1.add(btCadastros);

        jMenu2.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/imagens/businesspackage_additionalpackage_box_add_insert_negoci_2335.png"))); // NOI18N
        jMenu2.setText("Produtos");
        jMenu2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        menuCadastraProduto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_editar.png"))); // NOI18N
        menuCadastraProduto.setText("Cadastro Produtos");
        menuCadastraProduto.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuCadastraProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCadastraProdutoActionPerformed(evt);
            }
        });
        jMenu2.add(menuCadastraProduto);

        menuVerProdutos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_produtos.png"))); // NOI18N
        menuVerProdutos.setText("Ver Produtos");
        menuVerProdutos.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuVerProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuVerProdutosActionPerformed(evt);
            }
        });
        jMenu2.add(menuVerProdutos);

        jMenuBar1.add(jMenu2);

        menuCaixaBt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/financeiro.png"))); // NOI18N
        menuCaixaBt.setText("Caixa");
        menuCaixaBt.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuCaixaBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuCaixaBtMouseClicked(evt);
            }
        });
        menuCaixaBt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCaixaBtActionPerformed(evt);
            }
        });
        jMenuBar1.add(menuCaixaBt);

        jMenu5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/relatorios.png"))); // NOI18N
        jMenu5.setText("Relatórios   |");
        jMenu5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jMenu16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_rela_financeiro.png"))); // NOI18N
        jMenu16.setText("Financeiro");
        jMenu16.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        btConfereCaixa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_rela_financeiro.png"))); // NOI18N
        btConfereCaixa.setText("Conferencia de Caixas");
        btConfereCaixa.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btConfereCaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConfereCaixaActionPerformed(evt);
            }
        });
        jMenu16.add(btConfereCaixa);

        btConfereLocacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_hospede.png"))); // NOI18N
        btConfereLocacao.setText("Relatório de Locações");
        btConfereLocacao.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btConfereLocacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConfereLocacaoActionPerformed(evt);
            }
        });
        jMenu16.add(btConfereLocacao);

        jMenu5.add(jMenu16);

        jMenu18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_vender.png"))); // NOI18N
        jMenu18.setText("Produtos");
        jMenu18.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        menuRelaVenProdutos
                .setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_rela_produto.png"))); // NOI18N
        menuRelaVenProdutos.setText("Relatório Produtos Vendidos");
        menuRelaVenProdutos.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuRelaVenProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuRelaVenProdutosActionPerformed(evt);
            }
        });
        jMenu18.add(menuRelaVenProdutos);

        jMenu5.add(jMenu18);

        jMenuBar1.add(jMenu5);

        btConferencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/musica.png"))); // NOI18N
        btConferencia.setText("Conferência (F2)");
        btConferencia.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btConferencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btConferenciaMouseClicked(evt);
            }
        });
        jMenuBar1.add(btConferencia);

        btFerramentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/ferramentas.png"))); // NOI18N
        btFerramentas.setText("Ferramentas   |");
        btFerramentas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        menuConfigAd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_editar.png"))); // NOI18N
        menuConfigAd.setText("Configurações Adicionais");
        menuConfigAd.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuConfigAd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuConfigAdMouseClicked(evt);
            }
        });
        menuConfigAd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuConfigAdActionPerformed(evt);
            }
        });
        btFerramentas.add(menuConfigAd);

        menuSobSistema.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_entrar.png"))); // NOI18N
        menuSobSistema.setText("Auto Atendimento");
        menuSobSistema.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuSobSistema.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuSobSistemaMouseClicked(evt);
            }
        });
        menuSobSistema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSobSistemaActionPerformed(evt);
            }
        });
        btFerramentas.add(menuSobSistema);

        jMenuBar1.add(btFerramentas);

        btDespertador.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/alarme.png"))); // NOI18N
        btDespertador.setText("Despertador");
        btDespertador.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btDespertador.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btDespertadorMouseClicked(evt);
            }
        });
        jMenuBar1.add(btDespertador);

        menuReservas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/reservas.png"))); // NOI18N
        menuReservas.setText("Reservas");
        menuReservas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuReservas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuReservasMouseClicked(evt);
            }
        });
        menuReservas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuReservasActionPerformed(evt);
            }
        });
        jMenuBar1.add(menuReservas);

        menuSistema.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/seguranca.png"))); // NOI18N
        menuSistema.setText("Sistema");
        menuSistema.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuSistema.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuSistemaMouseClicked(evt);
            }
        });
        menuSistema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSistemaActionPerformed(evt);
            }
        });

        menuResBackup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/relatorios.png"))); // NOI18N
        menuResBackup.setText("Mostra Cache Atual");
        menuResBackup.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuResBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuResBackupActionPerformed(evt);
            }
        });
        menuSistema.add(menuResBackup);

        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/consumo.png"))); // NOI18N
        jMenuItem1.setText("Atualizar & Sincronizar");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        menuSistema.add(jMenuItem1);

        jMenuBar1.add(menuSistema);

        btMenuSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sair.png"))); // NOI18N
        btMenuSair.setText("Sair");
        btMenuSair.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btMenuSair.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btMenuSairMouseClicked(evt);
            }
        });
        btMenuSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMenuSairActionPerformed(evt);
            }
        });
        jMenuBar1.add(btMenuSair);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(srPane, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(painelQuartos, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, 0)
                                                .addComponent(tabela, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 468,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(painelBotton, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tabela, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(139, 139, 139)
                                                .addComponent(painelQuartos, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(srPane, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(painelBotton, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap()));

        painelBotton.getAccessibleContext().setAccessibleName("");

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void menuReservasMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_menuReservasMouseClicked
        new GerenciarReservas().setVisible(true);
    }// GEN-LAST:event_menuReservasMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowOpened
        configGlobal config = configGlobal.getInstance();
        setLabel(config.getUsuario(), config.getCargoUsuario());
        // Mostra os quartos inicialmente
        mostraQuartos();
        if (config.getCaixa() == 0) {
            JOptionPane.showMessageDialog(null, "Precisa abrir o caixa");
            new CaixaFrame().setVisible(true);
        }

        if (config.getCargoUsuario().equals("comum")) {
            btFerramentas.setEnabled(false);
            btCadastros.setEnabled(false);
            menuCadastraProduto.setEnabled(false);
        }

        // carrega o numero de alarmes ativos ao iniciar o sistema
        try (Connection conn = new fazconexao().conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(" SELECT COUNT(*) AS total FROM alarmes ")) {
            if (rs.next()) {
                int alarmesAtivos = rs.getInt("total");
                config.setAlarmesAtivos(alarmesAtivos);
            }
        } catch (SQLException ex) {
            logger.error("Erro ao carregar número de alarmes ativos", ex);
        }

        // Atualiza a data na interface
        Date dataSistema = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        labelData.setText(formato.format(dataSistema));

        // Utiliza java.util.Timer para atualizar a hora a cada 1 segundo
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // A ação para atualizar a hora, equivalente à classe `hora`
                Calendar now = Calendar.getInstance();
                SwingUtilities.invokeLater(() -> {
                    labelHora.setText(String.format("%1$tH:%1$tM:%1$tS", now));
                });
                configGlobal config = configGlobal.getInstance();

                if (config.getMudanca()) {
                    SwingUtilities.invokeLater(() -> {
                        mostraQuartos();
                        focoQuarto();
                        config.setMudanca(false);
                        painelSecundario.repaint();
                        focoQuarto();
                    });
                }
            }
        }, 0, 750); // Atualiza a cada 1/2 segundo

        Timer alarmeTimer = new Timer();
        alarmeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                configGlobal config = configGlobal.getInstance();
                if (config.getAlarmesAtivos() > 0) {
                    lblAlarmeAtivo.setVisible(true);
                    checkAlarmsToRing();
                } else {
                    lblAlarmeAtivo.setVisible(false);
                }
            }
        }, 0, 60 * 1000); // Verifica a cada 1 minuto

        // Utiliza outro java.util.Timer para verificar os quartos a cada 20 segundos
        Timer outroTimer = new Timer();
        outroTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                srPane.removeAll();
                mostraQuartos();
                focoQuarto();
            }
        }, 0, 20000); // Atualiza a cada 20 segundos
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            verificarReservasProximas();
        }, 0, 1, TimeUnit.HOURS);
        // Tarefa 2: Roda a cada 3 horas (a que você pediu)
        scheduler.scheduleAtFixedRate(() -> {
            verificarMensalidade();
        }, 0, 3, TimeUnit.HOURS);

    }

    private String getSistemaProperty() {
        Properties props = new Properties();

        // CORREÇÃO CRÍTICA: Corrigido o erro de digitação de "Documetos" para
        // "Documents"
        String userHome = System.getProperty("user.home");
        String path = userHome + File.separator + "Documents"
                + File.separator + "logs" + File.separator + "application.properties";

        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);

            // 1. Pega o valor da propriedade "SISTEMA"
            String sistema = props.getProperty("SISTEMA", "SistemaDesconhecido");

            // 2. Transforma para minúsculo (necessário para o switch/case no PHP)
            String sistemaLower = sistema.toLowerCase();

            // 3. (Sugestão de segurança) Codifica para URL, caso tenha caracteres especiais
            return URLEncoder.encode(sistemaLower, StandardCharsets.UTF_8.toString());

        } catch (Exception e) {
            logger.error("Erro ao ler application.properties em {}", path, e);
            return "SistemaDesconhecido";
        }
    }

    public void verificarMensalidade() {
        String sistemaNome = getSistemaProperty();
        LocalDate hoje = LocalDate.now();
        int diaDoMes = hoje.getDayOfMonth();

        // Conexão com o banco de dados
        try (Connection conn = ConexaoRemota.getConnection()) {
            String sql = "SELECT referente FROM mensalidade WHERE status = 'approved' ORDER BY referente DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

                LocalDate ultimoPagamento = null;
                if (rs.next()) {
                    // Pega a data de referência da última mensalidade paga
                    ultimoPagamento = rs.getDate("referente").toLocalDate();
                }

                // --- Lógica de Verificação ---
                // 1. Verifica se a última mensalidade paga é a deste mês (ou já paga)
                if (ultimoPagamento != null && !ultimoPagamento.isBefore(hoje.withDayOfMonth(1))) {
                    // A última mensalidade paga é a deste mês ou já está adiantada.
                    return;
                }

                // --- Se chegou aqui, a mensalidade está em aberto ou atrasada ---
                // URL de pagamento (O valor final será definido em mensalidade.php)
                final String redirectUrl = "http://motelinteligente.com/api/mensalidade.php?sistema=" + sistemaNome;

                String titulo = "Aviso de Mensalidade";
                String mensagem;
                int tipoMensagem; // Tipo de ícone: WARNING ou ERROR

                if (diaDoMes <= 10) {
                    // Até o dia 10: Vencimento próximo (Aviso Amarelo)
                    mensagem = "O vencimento da Mensalidade está próximo (dia 10).\nDeseja pagar agora?";
                    tipoMensagem = JOptionPane.WARNING_MESSAGE;
                } else {
                    // Após o dia 10: Mensalidade atrasada (Erro Vermelho)
                    mensagem = "Sua mensalidade está em atraso.\nPor favor, regularize o pagamento para evitar interrupções.\nDeseja pagar agora?";
                    tipoMensagem = JOptionPane.ERROR_MESSAGE;
                }

                // Define os botões personalizados
                Object[] options = { "Pagar Agora", "Pagar Depois" };

                // Exibe a janela modal
                int escolha = JOptionPane.showOptionDialog(
                        null, // Componente pai (null para centralizar na tela)
                        mensagem,
                        titulo,
                        JOptionPane.YES_NO_OPTION, // Opções de sim/não
                        tipoMensagem,
                        null, // Ícone customizado (null usa o padrão)
                        options, // Os botões 'Pagar Agora' e 'Pagar Depois'
                        options[0] // Botão padrão (focado)
                );

                // Ação baseada na escolha do usuário
                if (escolha == JOptionPane.YES_OPTION) { // Pagar Agora
                    // Abre o navegador com o link
                    abrirURL(redirectUrl);
                }
                // Se o usuário clicar em "Pagar Depois" (NO_OPTION) ou fechar no X
                // (CANCEL_OPTION), a função apenas sai (ignora).

            }
        } catch (Exception e) {
            logger.error("Erro ao verificar mensalidade: " + e);
        }
    }

    public void abrirURL(String url) {
        if (Objects.requireNonNull(url, "A URL não pode ser nula").isEmpty()) {
            return;
        }

        // Verifica se o Desktop API é suportado (requer Java 1.6+)
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            try {
                desktop.browse(new java.net.URI(url));
            } catch (Exception e) {
                // Log de erro se o navegador não puder ser aberto
                logger.error("Não foi possível abrir o navegador: " + e);
                JOptionPane.showMessageDialog(null,
                        "Não foi possível abrir o navegador. Copie e cole este link:\n" + url, "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Solução de fallback para ambientes sem suporte a Desktop API
            String os = System.getProperty("os.name").toLowerCase();
            try {
                if (os.contains("win")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec("open " + url);
                } else { // Linux/Unix
                    Runtime.getRuntime().exec("xdg-open " + url);
                }
            } catch (Exception e) {
                logger.error("Não foi possível executar o comando de abertura de URL: " + e);
                JOptionPane.showMessageDialog(null,
                        "Não foi possível abrir o navegador. Copie e cole este link:\n" + url, "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void verificarReservasProximas() {

        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM reservas "
                                + "WHERE TIMESTAMP(data_entrada, horario_entrada) BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 12 HOUR)");
                ResultSet rs = stmt.executeQuery()) {

            CacheDados cache = CacheDados.getInstancia();
            StringBuilder textoHTML = new StringBuilder("<html>");
            boolean temReservas = false;

            while (rs.next()) {
                temReservas = true; // Indica que há pelo menos uma reserva

                String numero = rs.getString("numero_quarto");
                String hora = rs.getString("horario_entrada");
                String data = rs.getString("data_entrada");
                String valorPago = rs.getString("valor_pago");
                String nome = rs.getString("observacao");

                // verifica qual o status do quarto > se possível reserva ele
                CarregaQuarto quarto = cache.getCacheQuarto().get(Integer.parseInt(numero));
                System.out.println("quarto de num " + numero + " está reservado");
                if (!quarto.getStatusQuarto().contains("ocupado") || quarto.getStatusQuarto().equals("limpeza")) {
                    mudaStatusNaCache(Integer.parseInt(numero), "reservado", null);

                    // a seguir acontece em background
                    SwingWorker<Void, Void> worker;
                    worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            new fquartos().setStatus(Integer.parseInt(numero), "reservado");
                            return null;
                        }

                    };
                    worker.execute();

                }

                // Formatação da data e horário
                String dataHora = hora + "/" + data;

                // Adicionando a bolinha azul e o texto formatado
                textoHTML.append("<div style='font-size: 14px;'>")
                        .append("<span style='color: blue;'>&#9679;</span> Reserva: Nº <b style='color: red;'>")
                        .append(numero)
                        .append("</b> - <b style='color: red;'>")
                        .append(dataHora)
                        .append("</b> - pago <b style='color: red;'>")
                        .append(valorPago)
                        .append("</b> - <span style='color: black;'>")
                        .append(nome)
                        .append("</span></div><br>");

            }

            // Se não houver reservas, deve deixar o texto vazio
            if (!temReservas) {
                textoHTML.append("Nenhuma reserva nas proximas 24h.");
            }

            textoHTML.append("</html>");

            SwingUtilities.invokeLater(() -> {
                labelReservas.setText(textoHTML.toString());

            });

        } catch (Exception e) {
            logger.error("Erro ao verificar reservas próximas", e);
        }
    }

    private void mostraQuartos() {
        String status = null, data = null;
        long currentTime = System.currentTimeMillis();
        // Se a última atualização foi há menos de UPDATE_INTERVAL, cancela esta
        // execução
        if (currentTime - lastUpdate < UPDATE_INTERVAL) {
            try {
                Thread.sleep(1000); // espera 1 segundo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restaura o estado de interrupção
            }
        }

        // Atualiza o timestamp da última execução
        lastUpdate = currentTime;
        srPane.removeAll();
        srPane.setLayout(new GridLayout(3, 3));

        Color outroVerde = new Color(0, 200, 0);
        Color meuVerde = new Color(30, 255, 50);

        Color meuAmarelo = new Color(238, 243, 96);
        Color outroAmarelo = new Color(238, 213, 0);

        Color meuCinza = new Color(204, 204, 204);
        Color outroCinza = new Color(155, 155, 155);

        Color meuVermelho = new Color(255, 70, 72);
        Color outroVermelho = new Color(213, 0, 30);

        Color meuAzul = new Color(38, 58, 155);
        Color outroAzul = new Color(0, 21, 111);
        CacheDados cacheDados = CacheDados.getInstancia();

        // Percorre os elementos da cache
        for (Map.Entry<Integer, CarregaQuarto> entry : cacheDados.getCacheQuarto().entrySet()) {
            int numeroQuarto = entry.getKey();
            CarregaQuarto q = entry.getValue();
            status = q.getStatusQuarto();
            if (status.equals("livre")) {
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), null, outroVerde);
                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setBackground(meuVerde);

                srPane.add(quadrado);

            }
            if (status.equals("manutencao")) {
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), "MANUTENÇÃO", outroCinza);
                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setBackground(meuCinza);
                srPane.add(quadrado);

            }
            if (status.equals("limpeza")) {
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), "LIMPEZA", outroAmarelo);
                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setBackground(meuAmarelo);
                srPane.add(quadrado);

            }
            if (status.contains("-")) {
                data = calculaData(q.getHoraStatus());
                String[] partes = status.split("-");
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), data, outroVermelho);
                if (partes[1].equals("pernoite")) {
                    quadrado.setBackground(new Color(153, 51, 153));
                } else if (partes[1].equals("periodo")) {
                    quadrado.setBackground(meuVermelho);
                }

                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setVisible(true);
                srPane.add(quadrado);

            }
            if (status.equals("reservado")) {
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), "RESERVADO", outroAzul);
                quadrado.setBackground(Color.cyan);
                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setVisible(true);
                srPane.add(quadrado);
            }
        }
        srPane.revalidate();
        srPane.repaint();

        painelSecundario.repaint();
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

    @Override
    public void quartoClicado(int numeroQuartoSelecionado) {
        quartoEmFoco = numeroQuartoSelecionado;
        focoQuarto();

    }

    private boolean isCargoAdmin() {
        if (cargo.equals("admin")) {
            return true;
        } // Supondo que seja admin por padrão
        return false;
    }

    public void clicDireito(int numeroQuartoSelecionado) {
        // Lógica para quando um quarto é clicado com o botão direito do mouse
        if (isCargoAdmin()) {
            popupMenu.getComponent(1).setEnabled(true); // Ativa a opção de editar apenas se for admin
        } else {
            popupMenu.getComponent(1).setEnabled(false); // Desativa a opção de editar se não for admin
        }
        popupMenu.show(this, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        quartoEmFoco = numeroQuartoSelecionado;

    }

    private void inicializarPopupMenu() {
        popupMenu = new JPopupMenu();

        JMenuItem verLocacaoItem = new JMenuItem("Ver última locação");
        verLocacaoItem.addActionListener((e) -> {
            new UltimaLocacao(false, quartoEmFoco).setVisible(true);
        });
        popupMenu.add(verLocacaoItem);

        JMenuItem editarLocacaoItem = new JMenuItem("Editar última locação");
        editarLocacaoItem.addActionListener((e) -> {

            if (isCargoAdmin()) {
                new UltimaLocacao(true, quartoEmFoco).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Não possui permissão! Somente admin");
            }
        });
        verLocacaoItem.setFont(new Font("Arial", Font.PLAIN, 16));
        editarLocacaoItem.setFont(new Font("Arial", Font.PLAIN, 16));
        popupMenu.add(editarLocacaoItem);

    }// GEN-LAST:event_formWindowOpened

    public void fecharTela() throws IOException {
        this.dispose();
        new TelaLogin().setVisible(true);
    }

    private void menuReservasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuReservasActionPerformed
        // DEPOIS IMPLEMENTAR A PARTE DE RESERVAS AQUI

    }// GEN-LAST:event_menuReservasActionPerformed

    private void lblUsuarioPropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_lblUsuarioPropertyChange
        // TODO add your handling code here:
    }// GEN-LAST:event_lblUsuarioPropertyChange

    private void itemReservaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_itemReservaActionPerformed
        // TODO add your handling code here:
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "reservado", null);
            mostraQuartos();
            // a seguir acontece em background
            SwingWorker<Void, Void> worker;
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    quarto.setStatus(quartoEmFoco, "reservado");
                    quarto.adicionaRegistro(quartoEmFoco, "reservado");
                    focoQuarto();
                    return null;
                }

            };
            worker.execute();
        }
    }// GEN-LAST:event_itemReservaActionPerformed

    private void itemManutencaoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_itemManutencaoActionPerformed
        // setar quarto em manutencao
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "manutencao", null);
            mostraQuartos();
            // a seguir acontece em background
            SwingWorker<Void, Void> worker;
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    quarto.adicionaRegistro(quartoEmFoco, "manutencao");
                    quarto.setStatus(quartoEmFoco, "manutencao");
                    focoQuarto();
                    return null;
                }

            };
            worker.execute();
        }

    }// GEN-LAST:event_itemManutencaoActionPerformed

    private void btFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btFuncionarioActionPerformed

    }// GEN-LAST:event_btFuncionarioActionPerformed

    public void executarFinalizar() {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        if (idCaixa == 0) {
            new CaixaFrame().setVisible(true);
            JOptionPane.showMessageDialog(null, "Precisa abrir o caixa!");

        } else {
            // Abre uma caixa de diálogo de confirmação
            int confirmacao = JOptionPane.showConfirmDialog(null, "Deseja encerrar o quarto " + quartoEmFoco + "?",
                    "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                abrirEncerraQuarto(quartoEmFoco);

            }
        }
    }

    private void abrirEncerraQuarto(int quartoEmFoco) {
        if (encerraQuarto == null || !encerraQuarto.isVisible()) {
            // Criar nova instância se não houver tela aberta
            encerraQuarto = new EncerraQuarto(this, quartoEmFoco);
            encerraQuarto.setVisible(true);
        } else {
            Icon iconeAlerta = UIManager.getIcon("OptionPane.warningIcon");

            // Mensagem formatada com HTML
            String mensagem = "<html><body style='text-align: center; font-size: 12px;'>"
                    + "<b>Uma tela de encerramento já está aberta!</b><br>"
                    + "Se precisar, finalize a tela atual antes de abrir outra.</body></html>";

            // Exibe a JOptionPane com ícone e mensagem estilizada
            JOptionPane.showMessageDialog(this, mensagem, "Atenção!", JOptionPane.WARNING_MESSAGE, iconeAlerta);
            encerraQuarto.setExtendedState(JFrame.NORMAL); // Caso esteja minimizada, restaurar
            encerraQuarto.setAlwaysOnTop(true); // Coloca na frente de todas as janelas
            encerraQuarto.toFront(); // Traz para frente
            encerraQuarto.requestFocus(); // Dá foco na janela
            encerraQuarto.setAlwaysOnTop(false); // Remove o "always on top" depois

        }
    }

    public boolean mudaStatusNaCache(int quartoMudar, String statusColocar, Timestamp hora) {
        CacheDados dados = CacheDados.getInstancia();
        // Obtém o quarto da cache
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        // Atualiza o status e a data do quarto
        quarto.setStatusQuarto(statusColocar);
        if (hora != null) {
            quarto.setHoraStatus(String.valueOf(hora));
        } else {
            quarto.setHoraStatus(String.valueOf(timestamp));
        }

        // Atualiza o quarto na cache
        dados.getCacheQuarto().put(quartoMudar, quarto);
        if (statusColocar.contains("ocupado")) {
            dados.carregarOcupado(quartoMudar);
        }
        if (statusColocar.equals("limpeza")) {
            dados.getCacheOcupado().remove(quartoMudar);
        }
        return true;

    }

    public void executaIniciar() {
        // setar quarto como locado

        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "ocupado-periodo", null);
            mostraQuartos();
            if (new fquartos().registraLocacao(quartoEmFoco)) {
                if (!new fquartos().setStatus(quartoEmFoco, "ocupado-periodo")) {
                    JOptionPane.showMessageDialog(null, "Falha ao iniciar locação no banco!");
                } else {
                    focoQuarto();
                    // abreportao
                    new Thread(() -> {
                        try {
                            Thread.sleep(300); // Pausa por 0,3s
                            new ConectaArduino(quartoEmFoco);
                            Thread.sleep(800); // Pausa por 0,3s
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            logger.error("Thread interrompida ao acionar Arduino no início da locação", ex);
                        }
                        new ConectaArduino(888);
                    }).start(); // Inicia a thread
                }
            } else {
                JOptionPane.showMessageDialog(null, "Falha ao iniciar locação!");

            }
        }
        System.out.println("finalizou inicialização do quarto " + quartoEmFoco);
    }

    private void menuVerProdutosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuVerProdutosActionPerformed
        new Produto().setVisible(true);
    }// GEN-LAST:event_menuVerProdutosActionPerformed

    private void menuCadastraProdutoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuCadastraProdutoActionPerformed
        new CadastraProduto(null, 0).setVisible(true);
    }// GEN-LAST:event_menuCadastraProdutoActionPerformed

    private void limpezaDisponivelActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_limpezaDisponivelActionPerformed
        // TODO add your handling code here:
        if (quartoEmFoco != 0) {

            mudaStatusNaCache(quartoEmFoco, "livre", null);
            mostraQuartos();

            SwingWorker<Void, Void> worker;
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    String status = quarto.getStatus(quartoEmFoco);
                    quarto.setStatus(quartoEmFoco, "livre");
                    quarto.alteraRegistro(quartoEmFoco, status);
                    focoQuarto();
                    return null;
                }

            };
            worker.execute();
        }
    }// GEN-LAST:event_limpezaDisponivelActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenu2ActionPerformed
    }// GEN-LAST:event_jMenu2ActionPerformed

    public void insereIcone(JFrame frame) {
        try {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/imagens/iconeMotel.png")));
        } catch (Exception e) {
            logger.warn("Não foi possível definir o ícone da janela", e);
        }
    }

    private void btQuartosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btQuartosActionPerformed

    }// GEN-LAST:event_btQuartosActionPerformed

    private void btConfereCaixaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btConfereCaixaActionPerformed
        // abre a conferencia de caixas
        new ConfereCaixa().setVisible(true);

    }// GEN-LAST:event_btConfereCaixaActionPerformed

    private void btConfereLocacaoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btConfereLocacaoActionPerformed
        if (cargo != null) {
            if (cargo.equals("admin") || cargo.equals("gerente")) {
                new ConfereLocacoes().setVisible(true);

            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente gerencia ou acima");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Erro Grave no sistema! Nenhum Cargo");

        }
    }// GEN-LAST:event_btConfereLocacaoActionPerformed

    private void menuRelaVenProdutosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuRelaVenProdutosActionPerformed
        new ConfereProdutos().setVisible(true);
    }// GEN-LAST:event_menuRelaVenProdutosActionPerformed

    private void menuConfigAdActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuConfigAdActionPerformed

    }// GEN-LAST:event_menuConfigAdActionPerformed

    private void menuConfigAdMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_menuConfigAdMouseClicked
        new ConfiguracoesAdicionais().setVisible(true);
    }// GEN-LAST:event_menuConfigAdMouseClicked

    private void menuSobSistemaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuSobSistemaActionPerformed
        new ConfigAutoAtend().setVisible(true);
    }// GEN-LAST:event_menuSobSistemaActionPerformed

    private void menuSobSistemaMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_menuSobSistemaMouseClicked
        new ConfigAutoAtend().setVisible(true);
    }// GEN-LAST:event_menuSobSistemaMouseClicked

    private void radioPernoiteActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_radioPernoiteActionPerformed
        // clicou no radiopernoite - altera status para pernoite
        if (quartoEmFoco != 0) {
            alteraOcupadoCache(quartoEmFoco, "ocupado-pernoite");
            mostraQuartos();

            SwingWorker<Void, Void> worker;
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new fquartos().alteraOcupado(quartoEmFoco, "ocupado-pernoite");
                    // quarto.setStatus(quartoEmFoco, "ocupado-periodo");
                    // quarto.alteraRegistro(quartoEmFoco, status);
                    focoQuarto();
                    return null;
                }

            };
            worker.execute();
        }
    }// GEN-LAST:event_radioPernoiteActionPerformed

    private void radioPeriodoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_radioPeriodoActionPerformed
        if (quartoEmFoco != 0) {
            alteraOcupadoCache(quartoEmFoco, "ocupado-periodo");
            mostraQuartos();

            SwingWorker<Void, Void> worker;
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new fquartos().setStatus(quartoEmFoco, "ocupado-periodo");
                    // quarto.setStatus(quartoEmFoco, "ocupado-periodo");
                    // quarto.alteraRegistro(quartoEmFoco, status);
                    focoQuarto();
                    return null;
                }

            };
            worker.execute();
        }
    }// GEN-LAST:event_radioPeriodoActionPerformed

    class CentralizarCelulasRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            // Configurando a centralização do texto
            if (cellComponent instanceof JLabel) {
                ((JLabel) cellComponent).setHorizontalAlignment(JLabel.CENTER);
            }
            return cellComponent;
        }
    }

    private void menuResBackupActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuResBackupActionPerformed
        CacheDados cache = CacheDados.getInstancia();
        cache.mostrarCacheQuarto();

    }// GEN-LAST:event_menuResBackupActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }// GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Você tem certeza que deseja encerrar a aplicação?", "Confirmação de Encerramento",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fazconexao.fecharPool();
            dispose(); // Fecha a janela e chama windowClosed
            System.exit(0);
        } else {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }// GEN-LAST:event_formWindowClosing

    private void txtDescontoNegociadoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtDescontoNegociadoActionPerformed

    }// GEN-LAST:event_txtDescontoNegociadoActionPerformed

    private void txtAntecipadoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtAntecipadoActionPerformed
        float valorRecebido = 0;
        try {
            valorRecebido = Float.parseFloat(txtAntecipado.getText().replace(',', '.'));
            CacheDados cache = CacheDados.getInstancia();
            int idLocacao = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();

            salvaAntecipado(idLocacao, "recebido", valorRecebido);
            JOptionPane.showMessageDialog(null, "Recebimento Antecipado adicionado com Sucesso!");
        } catch (NumberFormatException e) {
            logger.warn("Valor inválido em txtAntecipadoActionPerformed: {}", txtAntecipado.getText(), e);
            JOptionPane.showMessageDialog(null, "Valor digitado não é monetário");
        }

    }// GEN-LAST:event_txtAntecipadoActionPerformed

    private void bt_inserirProdutoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_bt_inserirProdutoActionPerformed
        new ObterProdutoFrame((DefaultTableModel) tabela1.getModel(), quartoEmFoco, this::populaPrevendidos);
    }// GEN-LAST:event_bt_inserirProdutoActionPerformed

    private void bt_apagarProdutoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_bt_apagarProdutoActionPerformed
        DefaultTableModel modelo = (DefaultTableModel) tabela1.getModel();
        int linhaSelecionada = tabela1.getSelectedRow();

        if (linhaSelecionada != -1) {
            // 1. Coleta dos dados para exclusão no banco
            String desc = modelo.getValueAt(linhaSelecionada, 1).toString();
            int qntVendida = Integer.valueOf(modelo.getValueAt(linhaSelecionada, 0).toString());

            // 2. Remove o item visualmente da tabela
            modelo.removeRow(linhaSelecionada);

            // 3. Remove o registro do produto no banco de dados
            // Assumindo que 'fprodutos().removePreVendido' já cuida da lógica SQL
            new fprodutos().removePreVendido(quartoEmFoco, desc, qntVendida);

            JOptionPane.showMessageDialog(null, "Excluído com sucesso");

            // 4. Recalcula e atualiza o total na interface gráfica
            // (Você pode reusar a lógica de atualização que removeu do outro método)
            atualizaTotalConsumo(); // Método sugerido para recalcular e atualizar o lblValorConsumo

        } else {
            JOptionPane.showMessageDialog(null, "Nenhum produto selecionado");
        }
    }// GEN-LAST:event_bt_apagarProdutoActionPerformed

    public void atualizaTotalConsumo() {
        float totalVendido = 0;
        DefaultTableModel modelo = (DefaultTableModel) tabela1.getModel();

        // Itera sobre as linhas remanescentes da tabela
        for (int i = 0; i < modelo.getRowCount(); i++) {
            // Assume que a coluna 3 (índice 3) contém o valor TOTAL da linha
            // Ajuste o índice se o TOTAL estiver em outra coluna (0=QNT, 1=DESC, 2=VALOR,
            // 3=TOTAL)
            float totalLinha = Float.parseFloat(modelo.getValueAt(i, 3).toString());
            totalVendido += totalLinha;
        }

        lblValorConsumo.setText("R$ " + totalVendido);
        lblValorConsumo.repaint();
    }

    private void botaoTrocaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botaoTrocaActionPerformed
        // Obtém a instância da cache de dados
        CacheDados cache = CacheDados.getInstancia();
        int idLocacao = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();
        if (idLocacao == 0) {
            DadosOcupados quartoOcupado = cache.getCacheOcupado().get(quartoEmFoco);
            int novoID = new fquartos().getIdLocacao(quartoEmFoco);
            quartoOcupado.setIdLoca(novoID);
            cache.getCacheOcupado().put(quartoEmFoco, quartoOcupado);
        }
        // Cria o modelo da tabela
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Número do Quarto");
        model.addColumn("Tipo do Quarto");

        // Itera sobre os quartos livres e adiciona ao modelo da tabela
        for (Map.Entry<Integer, CarregaQuarto> entry : cache.getCacheQuarto().entrySet()) {
            CarregaQuarto quarto = entry.getValue();
            if (quarto.getStatusQuarto().equals("livre")) {
                model.addRow(new Object[] { quarto.getNumeroQuarto(), quarto.getTipoQuarto() });

            }
        }

        // Cria a tabela
        JTable table = new JTable(model);

        // Configura a fonte da tabela
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(20);

        // Configura o cabeçalho da tabela
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));

        // Configura o renderizador para o cabeçalho
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader()
                .getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Selecionar linha inteira ao clicar
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        // Adiciona o listener de clique na tabela para selecionar a linha inteira
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow(); // Obtém a linha selecionada
                // Apenas seleciona a linha, sem ação adicional ao clicar
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // Cria os botões "Voltar" e "Trocar"
        JButton botaoVoltar = new JButton("Voltar");
        JButton botaoTrocar = new JButton("Trocar");

        // Cria um painel para os botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(botaoVoltar);
        buttonPanel.add(botaoTrocar);

        // Configura o JDialog
        JDialog dialog = new JDialog((Frame) null, "Quartos Livres", true);
        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null); // Centraliza na tela
        // Configura o botão "Voltar" para fechar o JDialog
        botaoVoltar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose(); // Fecha o JDialog
            }
        });

        // Configura o botão "Trocar" para trocar o quarto selecionado
        botaoTrocar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow(); // Obtém a linha selecionada
                if (row != -1) {
                    Object value = table.getValueAt(row, 0); // Obtém o valor da célula
                    String numeroQuartoStr = value.toString(); // Converte o valor para String
                    int numeroQuarto = Integer.parseInt(numeroQuartoStr); // Obtém o número do quarto da linha
                                                                          // selecionada

                    // Abre o JOptionPane de confirmação
                    int resposta = JOptionPane.showConfirmDialog(
                            dialog,
                            "Deseja prosseguir a troca de quarto?",
                            "Confirmação",
                            JOptionPane.YES_NO_OPTION);

                    if (resposta == JOptionPane.YES_OPTION) {
                        trocaQuarto(idLocacao, numeroQuarto); // Chama o método de troca de quarto
                        dialog.dispose(); // Fecha o JDialog após a troca
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Por favor, selecione um quarto.", "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        // Exibe o JDialog
        dialog.setVisible(true);
    }// GEN-LAST:event_botaoTrocaActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton4ActionPerformed
        // abre o portão do quarto
        SwingUtilities.invokeLater(() -> {
            new ConectaArduino(quartoEmFoco);
        });
    }// GEN-LAST:event_jButton4ActionPerformed

    private void txtPessoasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtPessoasActionPerformed
        try {
            int numeroPessoas = Integer.valueOf(txtPessoas.getText());
            new fquartos().atualizaPessoas(quartoEmFoco, numeroPessoas);
            if (numeroPessoas < 2) {
                numeroPessoas = 2;
            }
            // atualizar na cache também
            CacheDados cache = CacheDados.getInstancia();
            Map<Integer, DadosOcupados> dadosOcupados = cache.getCacheOcupado();
            if (dadosOcupados.containsKey(quartoEmFoco)) {
                DadosOcupados quartoOcupado = dadosOcupados.get(quartoEmFoco);
                quartoOcupado.setNumeroPessoas(numeroPessoas);
                dadosOcupados.put(quartoEmFoco, quartoOcupado);
            } else {
                JOptionPane.showMessageDialog(null, "Quarto não está na Cache Ocupado!");
            }
            setValorQuarto();
        } catch (Exception e) {
            logger.error("Erro ao atualizar número de pessoas do quarto {}", quartoEmFoco, e);
        }
    }// GEN-LAST:event_txtPessoasActionPerformed

    private void botaoIniciarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botaoIniciarActionPerformed
        // lidar com problema de duplo clique
        if (isClickable) {
            isClickable = false;
            System.out.println("está clicavel");
            executaIniciar();
            // Iniciar uma thread para desbloquear o botão após um segundo
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Esperar meio segundo
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.warn("Thread interrompida ao reabilitar botão Iniciar", ex);
                } finally {
                    isClickable = true; // Desbloquear o botão
                }
            }).start();
        } else {
            isClickable = true;
        }

    }// GEN-LAST:event_botaoIniciarActionPerformed

    private void botaoEncerrarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botaoEncerrarActionPerformed
        if (isClickable) {
            isClickable = false;

            executarFinalizar();
            // Iniciar uma thread para desbloquear o botão após um segundo
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Esperar um segundo
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.warn("Thread interrompida ao reabilitar botão Encerrar", ex);
                } finally {
                    isClickable = true; // Desbloquear o botão
                    System.out.println("setou is clickable true de novo");
                }
            }).start();
        } else {
            isClickable = true;
            System.out.println("setou is clickable true de novo");
        }
    }// GEN-LAST:event_botaoEncerrarActionPerformed

    private void botaoStatusActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botaoStatusActionPerformed
        // TODO add your handling code here:
        if (quartoEmFoco != 0) {
            CacheDados cacheDados = CacheDados.getInstancia();
            CarregaQuarto q = cacheDados.getCacheQuarto().get(quartoEmFoco);
            String status = q.getStatusQuarto();
            if (status.equals("limpeza") || status.equals("manutencao") || status.equals("reservado")) {
                menuLimpeza.show(botaoStatus, 20, 20);

            } else if (status.contains("ocupado")) {
                menuOcupado.show(botaoStatus, 20, 20);
                String[] partes = status.split("-");
                if (partes[1].equals("pernoite")) {
                    radioPeriodo.setSelected(false);
                    radioPernoite.setSelected(true);
                } else if (partes[1].equals("periodo")) {
                    radioPeriodo.setSelected(true);
                    radioPernoite.setSelected(false);
                }
                radioPeriodo.setFont(new Font("Arial", Font.PLAIN, 16));
                radioPernoite.setFont(new Font("Arial", Font.PLAIN, 16));
            } else {
                menuStatus.show(botaoStatus, 20, 20);
            }
        }
        quartoClicado(quartoEmFoco);
        mostraQuartos();
    }// GEN-LAST:event_botaoStatusActionPerformed

    private void btDespertadorMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btDespertadorMouseClicked
        new AlarmApp();
    }// GEN-LAST:event_btDespertadorMouseClicked

    private void btConferenciaMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btConferenciaMouseClicked
        new playSound().playSound("som/mensagem conferencia.wav");
    }// GEN-LAST:event_btConferenciaMouseClicked

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

    private void btNegociarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btNegociarActionPerformed
        // Exibe um JOptionPane para o cliente inserir o valor de desconto
        String descontoInput = JOptionPane.showInputDialog(null, "Digite o valor do desconto:", "Desconto",
                JOptionPane.PLAIN_MESSAGE);

        // Verifica se o cliente inseriu algum valor
        if (descontoInput != null && !descontoInput.isEmpty()) {
            try {
                // Converte o valor de desconto inserido para float
                float valorDesconto = Float.parseFloat(descontoInput.replace(",", "."));

                // Agora você pode usar o valorDesconto no seu código
                CacheDados cache = CacheDados.getInstancia();
                int idLocacao = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();

                // Chama a função salvaAntecipado ou outra lógica com o valor de desconto
                salvaAntecipado(idLocacao, "desconto", valorDesconto);
                JOptionPane.showMessageDialog(null, "Valor de desconto salvo");
                txtDescontoNegociado.setText("R$" + valorDesconto);
            } catch (NumberFormatException ex) {
                logger.warn("Valor de desconto inválido informado: {}", descontoInput, ex);
                JOptionPane.showMessageDialog(null, "Valor inválido! Insira um número válido.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_btNegociarActionPerformed

    private void bt_AntecipadoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_bt_AntecipadoActionPerformed
        System.out.println("toooooooo no antecipadoooooooooooooooo");

        CacheDados cache = CacheDados.getInstancia();
        int idLocacao = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();
        criarTelaSelecaoPagamento(idLocacao);
        // aqui atualiza o valor do txtAntecipado
        atualizaAntecipado(idLocacao);

    }// GEN-LAST:event_bt_AntecipadoActionPerformed

    public void atualizaAntecipado(int locacao) {
        List<Antecipado> antecipados = new ArrayList<>();
        float jaRecebeu = 0;
        float valorDesconto = 0;
        try (Connection link = new fazconexao().conectar()) {

            // Consulta SQL para buscar registros da tabela "antecipado" para a locação
            // especificada
            String selectSQL = "SELECT tipo, valor, hora FROM antecipado WHERE idlocacao = ?";
            PreparedStatement preparedStatement = link.prepareStatement(selectSQL);
            preparedStatement.setInt(1, locacao);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Itera sobre os resultados da consulta
            while (resultSet.next()) {
                String tipo = resultSet.getString("tipo");
                float valor = resultSet.getFloat("valor");
                // Cria o objeto Antecipado para cada registro retornado
                if (tipo.equals("desconto")) {
                    valorDesconto += valor;
                } else {
                    jaRecebeu += valor;
                }
            }
            txtAntecipado.setText(String.format("R$ %.2f", jaRecebeu));
            txtDescontoNegociado.setText(String.format("R$ %.2f", valorDesconto));
            // Fecha os recursos
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            logger.error("Erro ao buscar dados antecipados para locacao {}", locacao, e);
            JOptionPane.showMessageDialog(null, "Erro ao buscar dados antecipados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void criarTelaSelecaoPagamento(int idLocacao) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Selecione o Tipo de Pagamento");
        dialog.setModal(true);
        dialog.setSize(300, 300);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBackground(Color.DARK_GRAY);

        JButton botaoCredito = criarBotao("Crédito (C)", 'C');
        JButton botaoDebito = criarBotao("Débito (D)", 'D');
        JButton botaoDinheiro = criarBotao("Dinheiro (O)", 'O');
        JButton botaoPix = criarBotao("Pix (P)", 'P');

        panel.add(botaoCredito);
        panel.add(botaoDebito);
        panel.add(botaoDinheiro);
        panel.add(botaoPix);

        ActionListener selecionarMetodo = e -> {
            JButton source = (JButton) e.getSource();
            String tipoPagamentoEscolhido = source.getText().substring(source.getText().indexOf("(") + 1,
                    source.getText().indexOf(")"));
            float valorRecebido = carregarValorRecebido(tipoPagamentoEscolhido, idLocacao);
            criarTelaEntradaValor(valorRecebido, tipoPagamentoEscolhido, idLocacao);
            dialog.dispose(); // Fecha a tela após a seleção
        };

        botaoCredito.addActionListener(selecionarMetodo);
        botaoDebito.addActionListener(selecionarMetodo);
        botaoDinheiro.addActionListener(selecionarMetodo);
        botaoPix.addActionListener(selecionarMetodo);

        // Adicionar atalhos de teclado
        configurarAtalho(panel, "O", botaoDinheiro);
        configurarAtalho(panel, "P", botaoPix);
        configurarAtalho(panel, "C", botaoCredito);
        configurarAtalho(panel, "D", botaoDebito);

        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "fecharTela");
        actionMap.put("fecharTela", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose(); // Fecha a tela quando ESC é pressionado
            }
        });
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JButton criarBotao(String texto, char mnemonic) {
        JButton botao = new JButton(texto);
        botao.setMnemonic(mnemonic);
        botao.setPreferredSize(new Dimension(80, 40)); // Largura 80, altura 40
        botao.setBackground(Color.BLACK); // Fundo preto
        botao.setForeground(Color.WHITE); // Texto branco
        botao.setFocusPainted(false);
        return botao;
    }

    private void configurarAtalho(JComponent component, String tecla, JButton botao) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(tecla), tecla);
        actionMap.put(tecla, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botao.doClick(); // Simula o clique no botão
            }
        });
    }

    private void criarTelaEntradaValor(float valorRecebido, String tipoPago, int idLocacao) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Entrada de Valor");
        dialog.setModal(true);
        dialog.setSize(500, 350);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        String recebido = switch (tipoPago) {
            case "C" ->
                "credito";
            case "D" ->
                "debito";
            case "O" ->
                "dinheiro";
            default ->
                "pix";
        };

        JLabel label = new JLabel(
                "<html><center><b>Valor Atual Recebido:</b> R$ " + String.format("%.2f", valorRecebido)
                        + " em " + recebido + "<br><br>Digite o valor a ser usado na operação:</center></html>");
        label.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField valorField = new JTextField(10);
        valorField.setFont(new Font("Arial", Font.BOLD, 16));

        // --- BOTÃO 2 AGORA É O PRIMEIRO (SOMAR) ---
        JButton botaoSomar = new JButton(" Somar ao Recebido ");
        botaoSomar.setPreferredSize(new Dimension(200, 40));
        botaoSomar.setBackground(new Color(30, 144, 255)); // Azul forte
        botaoSomar.setForeground(Color.WHITE);
        botaoSomar.setFocusPainted(false);
        botaoSomar.setFont(new Font("Arial", Font.BOLD, 14));
        botaoSomar.addActionListener(e -> salvarValor(valorField, tipoPago, idLocacao, dialog, "SOMAR", valorRecebido));

        // --- BOTÃO 1 AGORA É O SEGUNDO (SUBSTITUIR/SETAR) ---
        JButton botaoSubstituir = new JButton(" Setar Valor Total ");
        botaoSubstituir.setPreferredSize(new Dimension(200, 40));
        botaoSubstituir.setBackground(new Color(255, 140, 0)); // Laranja
        botaoSubstituir.setForeground(Color.WHITE);
        botaoSubstituir.setFocusPainted(false);
        botaoSubstituir.setFont(new Font("Arial", Font.BOLD, 14));
        botaoSubstituir.addActionListener(
                e -> salvarValor(valorField, tipoPago, idLocacao, dialog, "SUBSTITUIR", valorRecebido));

        // --- Mapeamento do teclado (ENTER e ESC) ---
        InputMap inputMap = valorField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = valorField.getActionMap();

        // Mapeando ESC
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "fechar");
        actionMap.put("fechar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        // Mapeando ENTER para clicar no botão SOMAR
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "somarValor");
        actionMap.put("somarValor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarValor(valorField, tipoPago, idLocacao, dialog, "SOMAR", valorRecebido);
            }
        });

        // Adiciona os componentes ao painel
        panel.add(label, gbc);
        gbc.gridy++;
        panel.add(valorField, gbc);
        gbc.gridy++;

        // Adicionar os botões lado a lado - Ordem alterada aqui!
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoesPanel.add(botaoSomar); // ⬅️ Somar primeiro
        botoesPanel.add(botaoSubstituir); // ⬅️ Substituir segundo

        panel.add(botoesPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);

        SwingUtilities.invokeLater(valorField::requestFocusInWindow);
        valorField.setText(null);
    }

    private void salvarValor(JTextField valorField, String tipoPagamento, int idLocacao, JDialog dialog,
            String operacao, float valorRecebidoAnterior) {
        String valorInserido = valorField.getText().replace(",", ".");
        try {
            float valor = Float.parseFloat(valorInserido);
            float novoValor = 0;
            String mensagem;

            if ("SUBSTITUIR".equals(operacao)) {
                // O valor digitado é o novo total
                novoValor = valor;
                mensagem = "Valor total substituído com sucesso!";
            } else if ("SOMAR".equals(operacao)) {
                // Soma o valor digitado ao valor anterior
                novoValor = valorRecebidoAnterior + valor;
                mensagem = "Valor somado! Novo total recebido: R$ " + String.format("%.2f", novoValor);
            } else {
                // Caso de erro, embora não deva acontecer com a implementação acima
                JOptionPane.showMessageDialog(dialog, "Operação inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            salvaAntecipado(idLocacao, tipoPagamento, novoValor);

            // Exibe o feedback
            JOptionPane.showMessageDialog(dialog, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            dialog.dispose();

            // Opcional: Chama a atualização da tela principal se necessário
            atualizaAntecipado(idLocacao);
        } catch (NumberFormatException ex) {
            logger.warn("Valor inválido informado em salvarValor: {}", valorInserido, ex);
            JOptionPane.showMessageDialog(dialog, "Valor inválido. Insira um número válido.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static float carregarValorRecebido(String tipoPagamento, int idLocacao) {
        String recebido = null;

        // Determina o tipo de pagamento correspondente
        switch (tipoPagamento) {
            case "C" ->
                recebido = "credito";
            case "D" ->
                recebido = "debito";
            case "O" ->
                recebido = "dinheiro";
            case "P" ->
                recebido = "pix";
        }

        String consultaSQL = "SELECT * FROM antecipado WHERE idlocacao = ? AND tipo = ?";

        try (Connection link = new fazconexao().conectar()) {

            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, idLocacao);
            statement.setString(2, recebido);

            ResultSet resultado = statement.executeQuery(); // Chame executeQuery() sem o SQL
            if (resultado.next()) {
                float valor = resultado.getFloat("valor"); // Supondo que a coluna se chame "valor"
                return valor;
            }
        } catch (Exception e) {
            logger.error("Erro ao carregar valor recebido. tipo={}, idLocacao={}", tipoPagamento, idLocacao, e);
            JOptionPane.showMessageDialog(null, e);
            return 0;
        }
        return 0;
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
        SwingUtilities.invokeLater(() -> {
            new ConectaArduino(888);
        });
    }// GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
        SwingUtilities.invokeLater(() -> {
            new ConectaArduino(999);
        });
    }// GEN-LAST:event_jButton3ActionPerformed

    private void btFuncionarioMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btFuncionarioMouseClicked
        // cadastrar funcionario
        System.out.println("clicou func");
        if (cargo != null) {
            if (cargo.equals("admin")) {
                new TelaCadFuncionario().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");

        }

    }// GEN-LAST:event_btFuncionarioMouseClicked

    private void btQuartosMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btQuartosMouseClicked
        // alterar quartos
        System.out.println("clicou abrir quartos");
        if (cargo != null) {
            if (cargo.equals("admin")) {
                new CadastraQuarto().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");

        }
    }// GEN-LAST:event_btQuartosMouseClicked

    private void menuCaixaBtActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuCaixaBtActionPerformed
        abrirCaixaFrame();
        // new CaixaFrame().setVisible(true);

    }// GEN-LAST:event_menuCaixaBtActionPerformed

    private void abrirCaixaFrame() {
        if (caixaFrame == null || !caixaFrame.isVisible()) {
            // Criar nova instância se não houver tela aberta
            caixaFrame = new CaixaFrame();
            caixaFrame.setVisible(true);
        } else {
            Icon iconeAlerta = UIManager.getIcon("OptionPane.warningIcon");

            // Mensagem formatada com HTML
            String mensagem = "<html><body style='text-align: center; font-size: 12px;'>"
                    + "<b>Uma tela de caixa já está aberta!</b><br>"
                    + "</body></html>";

            // Exibe a JOptionPane com ícone e mensagem estilizada
            JOptionPane.showMessageDialog(this, mensagem, "Atenção!", JOptionPane.WARNING_MESSAGE, iconeAlerta);
            caixaFrame.setExtendedState(JFrame.NORMAL); // Caso esteja minimizada, restaurar
            caixaFrame.setAlwaysOnTop(true); // Coloca na frente de todas as janelas
            caixaFrame.toFront(); // Traz para frente
            caixaFrame.requestFocus(); // Dá foco na janela
            caixaFrame.setAlwaysOnTop(false); // Remove o "always on top" depois

        }
    }

    private void menuCaixaBtMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_menuCaixaBtMouseClicked
        abrirCaixaFrame();
    }// GEN-LAST:event_menuCaixaBtMouseClicked

    private void btMenuSairMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btMenuSairMouseClicked
        // CODIGO PARA LOGGOF
        try {
            CacheDados cacheDados = CacheDados.getInstancia();
            cacheDados.limparCaches();
            fecharTela();
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_btMenuSairMouseClicked

    private void btMenuSairActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btMenuSairActionPerformed

    }// GEN-LAST:event_btMenuSairActionPerformed

    private void menuSistemaMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_menuSistemaMouseClicked

    }// GEN-LAST:event_menuSistemaMouseClicked

    private void menuSistemaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuSistemaActionPerformed
        // Cria a nova janela

    }// GEN-LAST:event_menuSistemaActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        SwingUtilities.invokeLater(() -> {
            new ConectaArduino(777);
        });
    }// GEN-LAST:event_jButton2ActionPerformed

    private void limpezaManutencaoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_limpezaManutencaoActionPerformed
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "manutencao", null);
            configGlobal config = configGlobal.getInstance();
            config.setMudanca(true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    String statusAntes = quarto.getStatus(quartoEmFoco);
                    quarto.setStatus(quartoEmFoco, "manutencao");
                    if (!(statusAntes.equals("livre"))) {
                        quarto.alteraRegistro(quartoEmFoco, statusAntes);
                    }
                    quarto.adicionaRegistro(quartoEmFoco, "manutencao");
                    return null;
                }
            };
            worker.execute();
        }
    }// GEN-LAST:event_limpezaManutencaoActionPerformed

    private void limpezaReservaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_limpezaReservaActionPerformed
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "reservado", null);
            configGlobal config = configGlobal.getInstance();
            config.setMudanca(true);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    String statusAntes = quarto.getStatus(quartoEmFoco);
                    quarto.setStatus(quartoEmFoco, "reservado");
                    if (!(statusAntes.equals("livre"))) {
                        quarto.alteraRegistro(quartoEmFoco, statusAntes);
                    }
                    quarto.adicionaRegistro(quartoEmFoco, "reservado");
                    return null;
                }
            };
            worker.execute();
        }
    }// GEN-LAST:event_limpezaReservaActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem1ActionPerformed
        TelaSistema telaSistema = new TelaSistema();
        // Torna a janela visível
        telaSistema.setVisible(true);
    }// GEN-LAST:event_jMenuItem1ActionPerformed

    private void trocaQuarto(int idLocacao, int numeroNovoQuarto) {
        fquartos quarto = new fquartos();
        String horaStatus = quarto.getDataInicio(quartoEmFoco);
        Timestamp hStatus = null;
        try {
            hStatus = Timestamp.valueOf(horaStatus);
        } catch (Exception e) {
            logger.error("Erro ao converter horaStatus para Timestamp. horaStatus={}", horaStatus, e);
        }

        mudaStatusNaCache(quartoEmFoco, "limpeza", hStatus);
        mudaStatusNaCache(numeroNovoQuarto, "ocupado-periodo", hStatus);

        quarto.adicionaRegistro(quartoEmFoco, "limpeza");
        quarto.setStatus(quartoEmFoco, "limpeza", hStatus);
        quarto.setStatus(numeroNovoQuarto, "ocupado-periodo", hStatus);
        String SQL = "UPDATE registralocado set numquarto=" + numeroNovoQuarto + " where idlocacao = " + idLocacao;
        try (Connection link = new fazconexao().conectar()) {
            PreparedStatement statement = link.prepareStatement(SQL);
            int linhasAfetadas = statement.executeUpdate();
            if (linhasAfetadas == 1) {
                JOptionPane.showMessageDialog(null, "Troca de quarto realizada com sucesso!");
            }
        } catch (SQLException e) {
            logger.error("Erro ao trocar o quarto de {} para {}", quartoEmFoco, numeroNovoQuarto, e);
            JOptionPane.showMessageDialog(null, "Erro ao trocar o quarto: " + e.getMessage());
        }
        focoQuarto();
        mostraQuartos();

    }

    public void salvaAntecipado(int idLocacao, String tipo, float valor) {
        // Esta parte transforma o código (C, D, O, P) no nome do tipo de BD (credito,
        // debito, etc.)
        tipo = switch (tipo) {
            case "C" ->
                "credito";
            case "D" ->
                "debito";
            case "O" ->
                "dinheiro";
            case "P" ->
                "pix";
            default ->
                "desconto";
        };
        configGlobal config = configGlobal.getInstance();
        int idCaixaatual = config.getCaixa();

        try (Connection link = new fazconexao().conectar()) {
            String currentTime = new java.sql.Timestamp(System.currentTimeMillis()).toString();

            // 1. Tenta atualizar (UPSERT: Update)
            String updateSQL = "UPDATE antecipado SET valor = ?, hora = ?, idcaixaatual = ? WHERE idlocacao = ? AND tipo = ?";
            PreparedStatement updateStatement = link.prepareStatement(updateSQL);
            updateStatement.setFloat(1, valor); // Este 'valor' é o novo total calculado em salvarValor
            updateStatement.setString(2, currentTime);
            updateStatement.setInt(3, idCaixaatual);
            updateStatement.setInt(4, idLocacao);
            updateStatement.setString(5, tipo);

            int rowsUpdated = updateStatement.executeUpdate();
            updateStatement.close();

            // 2. Se não atualizou, insere (UPSERT: Insert)
            if (rowsUpdated == 0) {
                String insertSQL = "INSERT INTO antecipado (idlocacao, tipo, valor, hora , idcaixaatual) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStatement = link.prepareStatement(insertSQL);
                insertStatement.setInt(1, idLocacao);
                insertStatement.setString(2, tipo);
                insertStatement.setFloat(3, valor); // Este 'valor' é o novo total calculado em salvarValor
                insertStatement.setString(4, currentTime);
                insertStatement.setInt(5, idCaixaatual);
                insertStatement.executeUpdate();
                insertStatement.close();
            }

        } catch (SQLException e) {
            logger.error("Erro ao inserir/atualizar antecipado. locacao={}, tipo={}", idLocacao, tipo, e);
            JOptionPane.showMessageDialog(null, "Erro ao inserir/atualizar: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean alteraOcupadoCache(int quartoMudar, String statusColocar) {
        CacheDados dados = CacheDados.getInstancia();
        // Obtém o quarto da cache
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        // Atualiza o status
        quarto.setStatusQuarto(statusColocar);
        // Atualiza o quarto na cache
        dados.getCacheQuarto().put(quartoMudar, quarto);

        return true;

    }

    // Aplica o DefaultListSelectionModel personalizado à tabela
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        new TelaPrincipal();

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane alteradorPaineis;
    private javax.swing.JButton botaoEncerrar;
    private javax.swing.JButton botaoIniciar;
    private javax.swing.JButton botaoStatus;
    private javax.swing.JButton botaoTroca;
    private javax.swing.JMenu btCadastros;
    private javax.swing.JMenuItem btConfereCaixa;
    private javax.swing.JMenuItem btConfereLocacao;
    private javax.swing.JMenu btConferencia;
    private javax.swing.JMenu btDespertador;
    private javax.swing.JMenu btFerramentas;
    private javax.swing.JMenu btFuncionario;
    private javax.swing.JMenu btMenuSair;
    private javax.swing.JButton btNegociar;
    private javax.swing.JMenu btQuartos;
    private javax.swing.JButton bt_Antecipado;
    private javax.swing.JButton bt_apagarProduto;
    private javax.swing.JButton bt_inserirProduto;
    private javax.swing.JMenuItem itemManutencao;
    private javax.swing.JMenuItem itemReserva;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    private javax.swing.JFrame jFrame3;
    private javax.swing.JFrame jFrame4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu16;
    private javax.swing.JMenu jMenu18;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel labelData;
    private javax.swing.JLabel labelHora;
    private javax.swing.JLabel labelReservas;
    private javax.swing.JLabel lblAlarmeAtivo;
    private javax.swing.JLabel lblCargo;
    private javax.swing.JLabel lblEntrada;
    private javax.swing.JLabel lblHoraAdicional;
    private javax.swing.JLabel lblNumero;
    private javax.swing.JLabel lblPermissao;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorConsumo;
    private javax.swing.JLabel lblValorQuarto;
    private javax.swing.JMenuItem limpezaDisponivel;
    private javax.swing.JMenuItem limpezaManutencao;
    private javax.swing.JMenuItem limpezaReserva;
    private javax.swing.JMenuItem menuCadastraProduto;
    private javax.swing.JMenu menuCaixaBt;
    private javax.swing.JMenu menuConfigAd;
    private javax.swing.JPopupMenu menuLimpeza;
    private javax.swing.JPopupMenu menuOcupado;
    private javax.swing.JMenuItem menuRelaVenProdutos;
    private javax.swing.JMenuItem menuResBackup;
    private javax.swing.JMenu menuReservas;
    private javax.swing.JMenu menuSistema;
    private javax.swing.JMenuItem menuSobSistema;
    private javax.swing.JPopupMenu menuStatus;
    private javax.swing.JMenuItem menuVerProdutos;
    private javax.swing.JPanel painelBotton;
    private javax.swing.JPanel painelQuartos;
    private javax.swing.JPanel painelReservasProximas;
    private javax.swing.JPanel painelSecundario;
    private javax.swing.JDesktopPane painelSecundario2;
    private javax.swing.JRadioButtonMenuItem radioPeriodo;
    private javax.swing.JRadioButtonMenuItem radioPernoite;
    private javax.swing.JDesktopPane srPane;
    private javax.swing.JPanel tabela;
    private javax.swing.JTable tabela1;
    private javax.swing.JTextField txtAntecipado;
    private javax.swing.JTextField txtDescontoNegociado;
    private javax.swing.JTextField txtPessoas;
    // End of variables declaration//GEN-END:variables
}
