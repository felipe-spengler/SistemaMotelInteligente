package com.motelinteligente.telas;

import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.dados.BackupExecutor;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CacheDados.DadosVendidos;
import com.motelinteligente.dados.CacheDados.Negociados;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.CheckSincronia;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import static com.motelinteligente.telas.EncerraQuarto.isInteger;
import com.motelinteligente.telas.Quadrado.QuartoClickListener;
import java.awt.BorderLayout;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import javax.swing.Timer;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                CacheDados cache = CacheDados.getInstancia();
                cache.alteraRunning(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        // Inicializa o BackupExecutor
        new BackupExecutor().start();
        CheckSincronia checkSincronia = new CheckSincronia();
        checkSincronia.start();

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
            //JOptionPane.showMessageDialog(null, "Precisa Abrir o Caixa!");
        } else {
            configGlobal configuracao = configGlobal.getInstance();
            configuracao.setCaixa(idCaixaAtual);
        }

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
                quartoEmFoco = 1;
            }
            alteraPainel();
        });
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
            jTabbedPane1.setEnabledAt(1, false);
            this.painelSecundario.setBackground(Color.green);
            // troca os botoes
            iniciar();

        }
        if (status.equals("manutencao")) {
            botaoStatus.setEnabled(true);
            jTabbedPane1.setEnabledAt(1, false);
            this.painelSecundario.setBackground(Color.gray);
            iniciar();
        }
        if (status.equals("reservado")) {
            botaoStatus.setEnabled(true);
            jTabbedPane1.setEnabledAt(1, false);
            this.painelSecundario.setBackground(Color.cyan);
            iniciar();
        }
        if (status.contains("ocupado")) {
            finalizar();
            botaoTroca.setVisible(true);

            jTabbedPane1.setEnabledAt(1, true);
            botaoStatus.setEnabled(true);
            txtPessoas.setEnabled(true);
            txtPessoas.setText(String.valueOf(cache.getCacheOcupado().get(quartoEmFoco).getNumeroPessoas()));
            String[] partes = status.split("-");
            int idLoca = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();
            if (idLoca == 0) {
                System.out.println("tinha dado idlocacao = 0 na cache");
                DadosOcupados quartoOcupado = cache.getCacheOcupado().get(quartoEmFoco);
                int novoID = new fquartos().getIdLocacao(quartoEmFoco);
                quartoOcupado.setIdLoca(novoID);
                cache.getCacheOcupado().put(quartoEmFoco, quartoOcupado);
                System.out.println("resolvido novo id é " + novoID);
                cache.carregaProdutosNegociadosCache(novoID);
            }
            //insere prevendidos tabela
            populaPrevendidos(idLoca, modelo);
            //popula os txtAntecipado
            populaAntecipado(idLoca);
            if (partes[1].equals("pernoite")) {
                this.painelSecundario.setBackground(Color.MAGENTA);
            } else if (partes[1].equals("periodo")) {
                this.painelSecundario.setBackground(Color.red);
            }
            setValorQuarto();
        }
        if (status.equals("limpeza")) {
            botaoStatus.setEnabled(true);
            jTabbedPane1.setEnabledAt(1, false);
            this.painelSecundario.setBackground(Color.yellow);
            iniciar();
        }

        lblNumero.setText(String.valueOf(quartoEmFoco));
        painelSecundario.repaint();
    }

    public void populaAntecipado(int idLocacao) {
        CacheDados cache = CacheDados.getInstancia();
        // Verifica se a cache contém produtos vendidos para essa locação
        if (cache.cacheNegociado.containsKey(idLocacao)) {
            // Obtém a lista de produtos vendidos associada a essa locação
            List<Negociados> negociados = cache.cacheNegociado.get(idLocacao);

            for (Negociados negociado : negociados) {
                float valor = negociado.valor;
                String tipo = negociado.tipo;
                if (tipo.equals("recebido")) {
                    txtAntecipado.setText(String.valueOf(valor));
                }
                if (tipo.equals("negociado")) {
                    txtDescontoNegociado.setText(String.valueOf(valor));
                }
                System.out.println("Negociado setado - locacao " + idLocacao + " tipo " + tipo + " valor " + valor);

            }
        } else {
            System.out.println("Não há antecipados para a locação " + idLocacao + " na cache.");
        }

    }

    public void populaPrevendidos(int locacao, DefaultTableModel modelo) {
        CacheDados cache = CacheDados.getInstancia();
        float totalVendido = 0;
        // Verifica se a cache contém produtos vendidos para essa locação
        if (cache.cacheProdutosVendidos.containsKey(locacao)) {
            // Obtém a lista de produtos vendidos associada a essa locação
            List<DadosVendidos> produtosVendidos = cache.cacheProdutosVendidos.get(locacao);

            // Popula a tabela com os prevendidos
            fprodutos produtoDao = new fprodutos();
            for (DadosVendidos produtoVendido : produtosVendidos) {
                int idProduto = produtoVendido.idProduto;
                int quantidade = produtoVendido.quantidadeVendida;
                String desc = produtoDao.getDescicao(String.valueOf(idProduto));
                float valor = produtoDao.getValorProduto(idProduto);
                float total = valor * quantidade;
                totalVendido += total;
                // Adiciona uma nova linha ao modelo da tabela
                modelo.addRow(new Object[]{
                    quantidade,
                    desc,
                    valor,
                    total
                });
            }
        } else {
            System.out.println("Não há produtos vendidos para a locação " + locacao + " na cache.");
        }
        lblValorConsumo.setText("R$ " + totalVendido);
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
            JOptionPane.showMessageDialog(null, "Erro na função FORMATARDATA: " + e.getMessage());
            e.printStackTrace();
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
            //adiciona o valor do adicional de pessoas
            valor += calculaAdicionalPessoa(ocupado.getNumeroPessoas());
            lblValorQuarto.setText(String.valueOf(valor));
            int numeroAdicionais = subtrairHora(quartoEmFoco, horarioQuarto, "pernoite");
            Float valorAdicional = Float.valueOf(numeroAdicionais) * ocupado.getValorAdicional();
            lblHoraAdicional.setText(String.valueOf(valorAdicional));
        } else if (partes[1].equals("periodo")) {
            float valor = ocupado.getValorPeriodo();
            //adiciona o valor do adicional de pessoas
            valor += calculaAdicionalPessoa(ocupado.getNumeroPessoas());
            lblValorQuarto.setText(String.valueOf(valor));
            int numeroAdicionais = subtrairHora(quartoEmFoco, horarioQuarto, ocupado.getTempoPeriodo());
            System.out.println("num add" + numeroAdicionais);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        menuOcupado = new javax.swing.JPopupMenu();
        radioPernoite = new javax.swing.JRadioButtonMenuItem();
        radioPeriodo = new javax.swing.JRadioButtonMenuItem();
        jTextField2 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        labelData = new javax.swing.JLabel();
        labelHora = new javax.swing.JLabel();
        lblAlarmeAtivo = new javax.swing.JLabel();
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
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
        jPanel4 = new javax.swing.JPanel();
        pnAdicionarAlarme = new javax.swing.JPanel();
        lblAdicionarAlarme = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        painelQuartos = new javax.swing.JPanel();
        tabela = new javax.swing.JPanel();
        srPane = new javax.swing.JDesktopPane();
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
        jMenu6 = new javax.swing.JMenu();
        menuFazBackup = new javax.swing.JMenuItem();
        menuResBackup = new javax.swing.JMenuItem();
        btFerramentas = new javax.swing.JMenu();
        menuConfigAd = new javax.swing.JMenu();
        menuSobSistema = new javax.swing.JMenuItem();
        menuSair = new javax.swing.JMenu();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame3Layout = new javax.swing.GroupLayout(jFrame3.getContentPane());
        jFrame3.getContentPane().setLayout(jFrame3Layout);
        jFrame3Layout.setHorizontalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame3Layout.setVerticalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame4Layout = new javax.swing.GroupLayout(jFrame4.getContentPane());
        jFrame4.getContentPane().setLayout(jFrame4Layout);
        jFrame4Layout.setHorizontalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame4Layout.setVerticalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

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
        limpezaDisponivel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sign-check-icon_34365.png"))); // NOI18N
        limpezaDisponivel.setText("Disponibilizar Quarto");
        limpezaDisponivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpezaDisponivelActionPerformed(evt);
            }
        });
        menuLimpeza.add(limpezaDisponivel);

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

        labelData.setText("data data data");

        labelHora.setText("hora hora hora");

        lblAlarmeAtivo.setText("jLabel4");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(269, 269, 269)
                .addComponent(labelData)
                .addGap(40, 40, 40)
                .addComponent(labelHora)
                .addGap(374, 374, 374)
                .addComponent(lblAlarmeAtivo, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelData)
                    .addComponent(labelHora)
                    .addComponent(lblAlarmeAtivo))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/logo_peq.png"))); // NOI18N

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

        jTabbedPane1.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Horas Adicionais");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("R$");

        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setOpaque(false);

        lblNumero.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblNumero.setText("1");

        botaoStatus.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        botaoStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/flat-style-circle-edit_icon-icons.com_66939.png"))); // NOI18N
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
                .addComponent(botaoStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNumero)
                    .addComponent(botaoStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

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
            .addComponent(botaoEncerrar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(painelSecundarioLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelSecundarioLayout.createSequentialGroup()
                        .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10))
                            .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel8)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblValorConsumo)
                            .addComponent(lblValorQuarto))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblHoraAdicional))
                            .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPessoas, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(33, 33, 33))
                    .addGroup(painelSecundarioLayout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(painelSecundarioLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(painelSecundarioLayout.createSequentialGroup()
                .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botaoIniciar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(botaoTroca, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelSecundarioLayout.setVerticalGroup(
            painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelSecundarioLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton4)
                    .addComponent(lblEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelSecundarioLayout.createSequentialGroup()
                        .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(16, 16, 16)
                        .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)))
                    .addGroup(painelSecundarioLayout.createSequentialGroup()
                        .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblValorQuarto)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(lblHoraAdicional))
                        .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(painelSecundarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblValorConsumo)
                                    .addComponent(jLabel13)))
                            .addGroup(painelSecundarioLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtPessoas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(2, 2, 2)))
                .addGap(12, 12, 12)
                .addComponent(botaoEncerrar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botaoIniciar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botaoTroca)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Principal", painelSecundario);

        tabela1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabela1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Quantidade", "Descrição", "Valor und", "Valor Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabela1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane2.setViewportView(tabela1);

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
        jLabel1.setText("Recebido Antecipado:");

        jLabel3.setFont(new java.awt.Font("Palatino Linotype", 0, 14)); // NOI18N
        jLabel3.setText("Desconto Negociado:");

        txtAntecipado.setFont(new java.awt.Font("Palatino Linotype", 0, 14)); // NOI18N
        txtAntecipado.setText("0.00");
        txtAntecipado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAntecipadoActionPerformed(evt);
            }
        });

        txtDescontoNegociado.setFont(new java.awt.Font("Palatino Linotype", 0, 14)); // NOI18N
        txtDescontoNegociado.setText("0,00");
        txtDescontoNegociado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescontoNegociadoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDescontoNegociado, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAntecipado, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtAntecipado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDescontoNegociado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(87, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bt_inserirProduto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bt_apagarProduto)
                .addGap(15, 15, 15))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_inserirProduto)
                    .addComponent(bt_apagarProduto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Antecipado", jPanel5);

        pnAdicionarAlarme.setBackground(new java.awt.Color(204, 204, 204));

        lblAdicionarAlarme.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        lblAdicionarAlarme.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAdicionarAlarme.setText("Clique Para Adicionar Alarme");
        lblAdicionarAlarme.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAdicionarAlarmeMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnAdicionarAlarmeLayout = new javax.swing.GroupLayout(pnAdicionarAlarme);
        pnAdicionarAlarme.setLayout(pnAdicionarAlarmeLayout);
        pnAdicionarAlarmeLayout.setHorizontalGroup(
            pnAdicionarAlarmeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnAdicionarAlarmeLayout.createSequentialGroup()
                .addContainerGap(96, Short.MAX_VALUE)
                .addComponent(lblAdicionarAlarme, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(91, 91, 91))
        );
        pnAdicionarAlarmeLayout.setVerticalGroup(
            pnAdicionarAlarmeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnAdicionarAlarmeLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(lblAdicionarAlarme, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jButton2.setText("Excluir Alarme");

        jButton5.setText("Modificar Alarme");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(pnAdicionarAlarme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(pnAdicionarAlarme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(94, 94, 94))
        );

        jTabbedPane1.addTab("Alarme", jPanel4);

        painelSecundario2.setLayer(jTabbedPane1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout painelSecundario2Layout = new javax.swing.GroupLayout(painelSecundario2);
        painelSecundario2.setLayout(painelSecundario2Layout);
        painelSecundario2Layout.setHorizontalGroup(
            painelSecundario2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelSecundario2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1))
        );
        painelSecundario2Layout.setVerticalGroup(
            painelSecundario2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelSecundario2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jTabbedPane1))
        );

        jButton1.setFont(new java.awt.Font("Stencil", 0, 14)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/bt_entrada_icone.png"))); // NOI18N
        jButton1.setText("POrtão Entrada");
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Stencil", 0, 14)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/bt_saida_icone.png"))); // NOI18N
        jButton3.setText("POrtão saída");
        jButton3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel2)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(jSeparator3)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                        .addGap(132, 132, 132)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel5)
                                            .addComponent(lblPermissao))
                                        .addGap(32, 32, 32)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblCargo)
                                            .addComponent(lblUsuario))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(35, 35, 35)))
                        .addGap(17, 17, 17))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(painelSecundario2))
                        .addGap(35, 35, 35))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(10, 10, 10)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPermissao)
                    .addComponent(lblCargo))
                .addGap(0, 0, 0)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton3))
                .addGap(1, 1, 1)
                .addComponent(painelSecundario2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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
            .addGap(0, 925, Short.MAX_VALUE)
        );
        srPaneLayout.setVerticalGroup(
            srPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 588, Short.MAX_VALUE)
        );

        btCadastros.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/cadastro.png"))); // NOI18N
        btCadastros.setText("Cadastros    |");
        btCadastros.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btQuartos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_quarto.png"))); // NOI18N
        btQuartos.setText("Quartos");
        btQuartos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
        btFuncionario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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

        jMenu2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/businesspackage_additionalpackage_box_add_insert_negoci_2335.png"))); // NOI18N
        jMenu2.setText("Produtos");
        jMenu2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        menuCadastraProduto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_editar.png"))); // NOI18N
        menuCadastraProduto.setText("Cadastro Produtos");
        menuCadastraProduto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuCadastraProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCadastraProdutoActionPerformed(evt);
            }
        });
        jMenu2.add(menuCadastraProduto);

        menuVerProdutos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_produtos.png"))); // NOI18N
        menuVerProdutos.setText("Ver Produtos");
        menuVerProdutos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuVerProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuVerProdutosActionPerformed(evt);
            }
        });
        jMenu2.add(menuVerProdutos);

        jMenuBar1.add(jMenu2);

        menuCaixaBt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/financeiro.png"))); // NOI18N
        menuCaixaBt.setText("Caixa");
        menuCaixaBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
        jMenu5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jMenu16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_rela_financeiro.png"))); // NOI18N
        jMenu16.setText("Financeiro");
        jMenu16.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btConfereCaixa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_rela_financeiro.png"))); // NOI18N
        btConfereCaixa.setText("Conferencia de Caixas");
        btConfereCaixa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btConfereCaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConfereCaixaActionPerformed(evt);
            }
        });
        jMenu16.add(btConfereCaixa);

        btConfereLocacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_hospede.png"))); // NOI18N
        btConfereLocacao.setText("Relatório de Locações");
        btConfereLocacao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btConfereLocacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConfereLocacaoActionPerformed(evt);
            }
        });
        jMenu16.add(btConfereLocacao);

        jMenu5.add(jMenu16);

        jMenu18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_vender.png"))); // NOI18N
        jMenu18.setText("Produtos");
        jMenu18.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        menuRelaVenProdutos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_rela_produto.png"))); // NOI18N
        menuRelaVenProdutos.setText("Relatório Produtos Vendidos");
        menuRelaVenProdutos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuRelaVenProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuRelaVenProdutosActionPerformed(evt);
            }
        });
        jMenu18.add(menuRelaVenProdutos);

        jMenu5.add(jMenu18);

        jMenuBar1.add(jMenu5);

        jMenu6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/seguranca.png"))); // NOI18N
        jMenu6.setText("Segurança   |");
        jMenu6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jMenu6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu6ActionPerformed(evt);
            }
        });

        menuFazBackup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_backup.png"))); // NOI18N
        menuFazBackup.setText("Mostra Cache ProdutosV");
        menuFazBackup.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuFazBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFazBackupActionPerformed(evt);
            }
        });
        jMenu6.add(menuFazBackup);

        menuResBackup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_restaura.png"))); // NOI18N
        menuResBackup.setText("Testa Som");
        menuResBackup.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuResBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuResBackupActionPerformed(evt);
            }
        });
        jMenu6.add(menuResBackup);

        jMenuBar1.add(jMenu6);

        btFerramentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/ferramentas.png"))); // NOI18N
        btFerramentas.setText("Ferramentas   |");
        btFerramentas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        menuConfigAd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_editar.png"))); // NOI18N
        menuConfigAd.setText("Configurações Adicionais");
        menuConfigAd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
        menuSobSistema.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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

        menuSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sair.png"))); // NOI18N
        menuSair.setText("Sair");
        menuSair.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuSair.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuSairMouseClicked(evt);
            }
        });
        menuSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSairActionPerformed(evt);
            }
        });
        jMenuBar1.add(menuSair);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(srPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(painelQuartos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(tabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(139, 139, 139)
                                .addComponent(painelQuartos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(srPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void menuSairMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuSairMouseClicked
        // TODO add your handling code here:
        this.dispose();
        try {
            new TelaLogin().setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_menuSairMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        configGlobal config = configGlobal.getInstance();
        setLabel(config.getUsuario(), config.getCargoUsuario());
        if (config.getCaixa() == 0) {
            JOptionPane.showMessageDialog(null, "Precisa abrir o caixa");
            new CaixaFrame().setVisible(true);
        }
        if (config.getCargoUsuario().equals("comum")) {
            btFerramentas.setEnabled(false);
            btCadastros.setEnabled(false);
            menuCadastraProduto.setEnabled(false);
        }
        Date dataSistema = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        labelData.setText(formato.format(dataSistema));

        Timer timer = new Timer(1000, new hora());
        Timer outroTimer = new Timer(30000, new horaQuarto());
        timer.start();
        mostraQuartos();
        outroTimer.start();

    }

    private void mostraQuartos() {
        String status = null, data = null;
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
                srPane.revalidate();
                srPane.repaint();
            }
            if (status.equals("manutencao")) {
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), "MANUTENÇÃO", outroCinza);
                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setBackground(meuCinza);
                srPane.add(quadrado);
                srPane.revalidate();
                srPane.repaint();

            }
            if (status.equals("limpeza")) {
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), "LIMPEZA", outroAmarelo);
                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setBackground(meuAmarelo);
                srPane.add(quadrado);
                srPane.revalidate();
                srPane.repaint();

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
                srPane.revalidate();
                srPane.repaint();
            }
            if (status.equals("reservado")) {
                Quadrado quadrado = new Quadrado(q.getNumeroQuarto(), q.getTipoQuarto(), "RESERVADO", outroAzul);
                quadrado.setBackground(Color.cyan);
                quadrado.setQuartoClickListener(this); // Registre a TelaPrincipal como ouvinte
                quadrado.setVisible(true);
                srPane.add(quadrado);
                srPane.revalidate();
                srPane.repaint();
            }
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
    }

    class horaQuarto implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            mostraQuartos();
        }

    }

    class hora implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Calendar now = Calendar.getInstance();
            labelHora.setText(String.format("%1$tH:%1$tM:%1$tS", now));
            configGlobal config = configGlobal.getInstance();

            if (config.getMudanca()) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("algo mudou");
                    mostraQuartos();
                    focoQuarto();
                    config.setMudanca(false);
                    painelSecundario.repaint();
                });

            }

        }
    }//GEN-LAST:event_formWindowOpened
    public void fecharTela() throws IOException {
        this.dispose();
        new TelaLogin().setVisible(true);
    }
    private void menuSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSairActionPerformed
        try {
            CacheDados cacheDados = CacheDados.getInstancia();
            cacheDados.limparCaches();
            fecharTela();
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_menuSairActionPerformed

    private void lblUsuarioPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_lblUsuarioPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_lblUsuarioPropertyChange

    private void botaoStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoStatusActionPerformed
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

    }//GEN-LAST:event_botaoStatusActionPerformed

    private void itemReservaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemReservaActionPerformed
        // TODO add your handling code here:
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "reservado");
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
    }//GEN-LAST:event_itemReservaActionPerformed

    private void itemManutencaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemManutencaoActionPerformed
        // setar quarto em manutencao
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "manutencao");
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

    }//GEN-LAST:event_itemManutencaoActionPerformed

    private void btFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFuncionarioActionPerformed
        //cadastrar funcionario
        if (cargo != null) {
            if (cargo.equals("admin")) {
                new TelaCadFuncionario();
            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");

        }
    }//GEN-LAST:event_btFuncionarioActionPerformed

    private void botaoEncerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoEncerrarActionPerformed
        if (isClickable) {
            isClickable = false;

            executarFinalizar();
            // Iniciar uma thread para desbloquear o botão após um segundo
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Esperar um segundo
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    isClickable = true; // Desbloquear o botão
                }
            }).start();
        }
    }//GEN-LAST:event_botaoEncerrarActionPerformed
    public void executarFinalizar() {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        if (idCaixa == 0) {
            new CaixaFrame().setVisible(true);
            JOptionPane.showMessageDialog(null, "Precisa abrir o caixa!");

        } else {
            // Abre uma caixa de diálogo de confirmação
            int confirmacao = JOptionPane.showConfirmDialog(null, "Deseja encerrar o quarto " + quartoEmFoco + "?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                new EncerraQuarto(quartoEmFoco);
            }
        }
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
        if (statusColocar.contains("ocupado")) {
            dados.carregarOcupado(quartoMudar);
        }
        if (statusColocar.equals("limpeza")) {
            dados.getCacheOcupado().remove(quartoMudar);
        }
        return true;

    }
    private void botaoIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoIniciarActionPerformed
        //lidar com problema de duplo clique
        if (isClickable) {
            isClickable = false;

            executaIniciar();
            // Iniciar uma thread para desbloquear o botão após um segundo
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Esperar um segundo
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    isClickable = true; // Desbloquear o botão
                }
            }).start();
        }


    }//GEN-LAST:event_botaoIniciarActionPerformed
    public void executaIniciar() {
        // setar quarto como locado

        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "ocupado-periodo");
            mostraQuartos();
            if (new fquartos().registraLocacao(quartoEmFoco)) {
                if (!new fquartos().setStatus(quartoEmFoco, "ocupado-periodo")) {
                    JOptionPane.showMessageDialog(null, "Falha ao iniciar locação no banco!");
                } else {
                    focoQuarto();
                    //abreportao
                    new Thread(() -> {
                        try {
                            Thread.sleep(350); // Pausa por 0,3s
                            new ConectaArduino(quartoEmFoco);
                            Thread.sleep(600); // Pausa por 0,6s
                        } catch (InterruptedException ex) {
                            JOptionPane.showMessageDialog(null, ex);
                        }
                        new ConectaArduino(888);
                    }).start(); // Inicia a thread
                }
            } else {
                JOptionPane.showMessageDialog(null, "Falha ao iniciar locação!");

            }
        }
    }
    private void menuVerProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuVerProdutosActionPerformed
        new Produto().setVisible(true);
    }//GEN-LAST:event_menuVerProdutosActionPerformed

    private void menuCadastraProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCadastraProdutoActionPerformed
        new CadastraProduto().setVisible(true);
    }//GEN-LAST:event_menuCadastraProdutoActionPerformed

    private void limpezaDisponivelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpezaDisponivelActionPerformed
        // TODO add your handling code here:
        if (quartoEmFoco != 0) {

            mudaStatusNaCache(quartoEmFoco, "livre");
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
    }//GEN-LAST:event_limpezaDisponivelActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void menuCaixaBtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCaixaBtActionPerformed
        new CaixaFrame().setVisible(true);
        boolean sair = false;

        if (sair) {
            this.dispose(); // Fecha a TelaPrincipal (ou realiza outras ações para sair do aplicativo)
        }

    }//GEN-LAST:event_menuCaixaBtActionPerformed
    public void insereIcone(JFrame frame) {
        try {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/imagens/iconeMotel.png")));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    private void menuCaixaBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuCaixaBtMouseClicked
        new CaixaFrame().setVisible(true);
    }//GEN-LAST:event_menuCaixaBtMouseClicked

    private void txtPessoasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPessoasActionPerformed
        try {
            int numeroPessoas = Integer.valueOf(txtPessoas.getText());
            new fquartos().atualizaPessoas(quartoEmFoco, numeroPessoas);
            if (numeroPessoas < 2) {
                numeroPessoas = 2;
            }
            //atualizar na cache também
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
            e.printStackTrace();
        }
    }//GEN-LAST:event_txtPessoasActionPerformed

    private void btQuartosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btQuartosActionPerformed
        //alterar quartos
        if (cargo != null) {
            if (cargo.equals("admin")) {
                new CadastraQuarto();
            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");

        }
    }//GEN-LAST:event_btQuartosActionPerformed

    private void btConfereCaixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btConfereCaixaActionPerformed
        //abre a conferencia de caixas
        new ConferenciaCaixa().setVisible(true);
    }//GEN-LAST:event_btConfereCaixaActionPerformed

    private void btQuartosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btQuartosMouseClicked
        // TODO add your handling code here:
        if (cargo != null) {
            if (cargo.equals("admin")) {
                CadastraQuarto cadastra = new CadastraQuarto();
                cadastra.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");

        }
    }//GEN-LAST:event_btQuartosMouseClicked

    private void btFuncionarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btFuncionarioMouseClicked
        // TODO add your handling code here:
        if (cargo != null) {
            if (cargo.equals("admin")) {
                TelaCadFuncionario telaC = new TelaCadFuncionario();
                telaC.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente admin");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Erro Grave no sistema! Nenhum Cargo");

        }
    }//GEN-LAST:event_btFuncionarioMouseClicked

    private void btConfereLocacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btConfereLocacaoActionPerformed
        if (cargo != null) {
            if (cargo.equals("admin") || cargo.equals("gerente")) {
                new ConferenciaLocacoes().setVisible(true);

            } else {
                JOptionPane.showMessageDialog(null, "Você não tem permissão! Somente gerencia ou acima");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Erro Grave no sistema! Nenhum Cargo");

        }
    }//GEN-LAST:event_btConfereLocacaoActionPerformed

    private void menuRelaVenProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRelaVenProdutosActionPerformed
        new ConferenciaProdutos().setVisible(true);
    }//GEN-LAST:event_menuRelaVenProdutosActionPerformed

    private void menuConfigAdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuConfigAdActionPerformed

    }//GEN-LAST:event_menuConfigAdActionPerformed

    private void menuConfigAdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuConfigAdMouseClicked
        new ConfiguracoesAdicionais().setVisible(true);
    }//GEN-LAST:event_menuConfigAdMouseClicked

    private void menuSobSistemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSobSistemaActionPerformed
        new ConfigAutoAtend().setVisible(true);
    }//GEN-LAST:event_menuSobSistemaActionPerformed

    private void menuSobSistemaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuSobSistemaMouseClicked
        new ConfigAutoAtend().setVisible(true);
    }//GEN-LAST:event_menuSobSistemaMouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // botaosaida enviar numero 888 arduino
        SwingUtilities.invokeLater(() -> {
            new ConectaArduino(888);
        });
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        // botaosaida enviar numero 999 arduino
        SwingUtilities.invokeLater(() -> {
            new ConectaArduino(999);
        });
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // abre o portão do quarto
        SwingUtilities.invokeLater(() -> {
            new ConectaArduino(quartoEmFoco);
        });
    }//GEN-LAST:event_jButton4ActionPerformed

    private void radioPernoiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPernoiteActionPerformed
        // clicou no radiopernoite - altera status para pernoite
        if (quartoEmFoco != 0) {
            alteraOcupadoCache(quartoEmFoco, "ocupado-pernoite");
            mostraQuartos();

            SwingWorker<Void, Void> worker;
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new fquartos().alteraOcupado(quartoEmFoco, "ocupado-pernoite");
                    //quarto.setStatus(quartoEmFoco, "ocupado-periodo");
                    //quarto.alteraRegistro(quartoEmFoco, status);
                    focoQuarto();
                    return null;
                }

            };
            worker.execute();
        }
    }//GEN-LAST:event_radioPernoiteActionPerformed

    private void radioPeriodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPeriodoActionPerformed
        if (quartoEmFoco != 0) {
            alteraOcupadoCache(quartoEmFoco, "ocupado-periodo");
            mostraQuartos();

            SwingWorker<Void, Void> worker;
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new fquartos().alteraOcupado(quartoEmFoco, "ocupado-periodo");
                    //quarto.setStatus(quartoEmFoco, "ocupado-periodo");
                    //quarto.alteraRegistro(quartoEmFoco, status);
                    focoQuarto();
                    return null;
                }

            };
            worker.execute();
        }
    }//GEN-LAST:event_radioPeriodoActionPerformed

    private void jMenu6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu6ActionPerformed
        CacheDados cache = CacheDados.getInstancia();
        cache.mostrarCacheProdutosVendidos();
    }//GEN-LAST:event_jMenu6ActionPerformed

    private void bt_apagarProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_apagarProdutoActionPerformed
        DefaultTableModel modelo = (DefaultTableModel) tabela1.getModel();
        int linhaSelecionada = tabela1.getSelectedRow();
        if (linhaSelecionada != -1) {
            String desc = modelo.getValueAt(linhaSelecionada, 1).toString();
            int qntVendida = Integer.valueOf(modelo.getValueAt(linhaSelecionada, 0).toString());
            modelo.removeRow(linhaSelecionada);
            new fprodutos().removePreVendido(quartoEmFoco, desc);
            JOptionPane.showMessageDialog(null, "Excluido com sucesso");

            //remove da cache tbm
            CacheDados cache = CacheDados.getInstancia();
            int idLoca = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();
            int idProduto = new fprodutos().getIdProduto(desc);
            if (idLoca == 0) {
                DadosOcupados quartoOcupado = cache.getCacheOcupado().get(quartoEmFoco);
                int novoID = new fquartos().getIdLocacao(quartoEmFoco);
                quartoOcupado.setIdLoca(novoID);
                cache.getCacheOcupado().put(quartoEmFoco, quartoOcupado);

                cache.carregaProdutosNegociadosCache(novoID);
            }
            List<DadosVendidos> produtosVendidos = new ArrayList<>();
            int limitador = 0;
            if (cache.cacheProdutosVendidos.containsKey(idLoca)) {
                // Itera sobre a lista usando um índice para poder remover um item
                for (int i = 0; i < produtosVendidos.size(); i++) {
                    DadosVendidos dados = produtosVendidos.get(i);
                    if (dados.idProduto == idProduto && dados.quantidadeVendida == qntVendida) {
                        produtosVendidos.remove(i); // Remove o item da lista
                        limitador++;
                        break; // Sai do loop após remover o item desejado
                    }
                }

                // Verifica se a lista ficou vazia
                if (produtosVendidos.isEmpty()) {
                    // Remove a entrada correspondente da cache
                    cache.cacheProdutosVendidos.remove(idLoca);
                } else {
                    // Atualiza a lista na cache
                    cache.cacheProdutosVendidos.put(idLoca, produtosVendidos);
                }

            }
        } else {
            JOptionPane.showMessageDialog(null, "Nenhum produto selecionado");
        }
    }//GEN-LAST:event_bt_apagarProdutoActionPerformed

    private void bt_inserirProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_inserirProdutoActionPerformed
        obterProduto();
    }//GEN-LAST:event_bt_inserirProdutoActionPerformed
    class CentralizarCelulasRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // Configurando a centralização do texto
            if (cellComponent instanceof JLabel) {
                ((JLabel) cellComponent).setHorizontalAlignment(JLabel.CENTER);
            }
            return cellComponent;
        }
    }

    private void obterProduto() {
        // Criação do campo de texto para o ID do produto
        JTextField txtIdProduto = new JTextField();
        txtIdProduto.setColumns(10);

        // Criação do campo de texto para a quantidade
        JTextField txtQuantidade = new JTextField();
        txtQuantidade.setColumns(10);

        // Label para exibir a descrição do produto
        JLabel lblDescricaoProduto = new JLabel();
        lblDescricaoProduto.setPreferredSize(new Dimension(200, 20));

        // Adiciona um DocumentListener ao campo de texto para monitorar as mudanças no texto
        txtIdProduto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarDescricaoProduto();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isInteger(txtIdProduto.getText())) {
                    atualizarDescricaoProduto();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            // Método para atualizar a descrição do produto com base no ID do produto
            private void atualizarDescricaoProduto() {
                String descricao = new fprodutos().getDescicao(txtIdProduto.getText());
                lblDescricaoProduto.setText(descricao);
            }
        });
        txtIdProduto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtQuantidade.requestFocusInWindow(); // Move o foco para txtQuantidade ao pressionar Enter em txtIdProduto
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5); // Margens

// Label e campo de texto para o ID do produto
        JLabel labelIdProduto = new JLabel("ID do Produto:");
        labelIdProduto.setFont(new Font("Arial", Font.PLAIN, 14)); // Define a fonte
        panel.add(labelIdProduto, gbc);

        gbc.gridx++;
        panel.add(txtIdProduto, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Expande o componente horizontalmente
        panel.add(lblDescricaoProduto, gbc);

// Reinicializa os GridBagConstraints para a próxima linha
        gbc.gridx = 0;
        gbc.gridy++;

// Label e campo de texto para a quantidade
        JLabel labelQuantidade = new JLabel("Quantidade:");
        labelQuantidade.setFont(new Font("Arial", Font.PLAIN, 14)); // Define a fonte
        panel.add(labelQuantidade, gbc);

        gbc.gridx++;
        panel.add(txtQuantidade, gbc);

        class CentralizarCelulasRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Configurando a centralização do texto
                if (cellComponent instanceof JLabel) {
                    ((JLabel) cellComponent).setHorizontalAlignment(JLabel.CENTER);
                }

                return cellComponent;
            }

        }

        // Exibe um JOptionPane com o painel contendo os componentes
        int result = JOptionPane.showConfirmDialog(null, panel, "Obter Produto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        txtIdProduto.grabFocus();
        if (result == JOptionPane.OK_OPTION) {
            // Aqui você pode usar os valores inseridos pelo usuário, por exemplo:
            String idProdutoStr = txtIdProduto.getText();
            String quantidadeStr = txtQuantidade.getText();
            DefaultTableModel modelo = (DefaultTableModel) tabela1.getModel();

            if (isInteger(idProdutoStr)) {
                fprodutos produtodao = new fprodutos();
                String texto = produtodao.getDescicao(idProdutoStr);
                if (texto != null) {
                    if (quantidadeStr != null) {
                        float valor = produtodao.getValorProduto(Integer.parseInt(idProdutoStr));
                        float valorSoma = valor * Integer.parseInt(quantidadeStr);
                        tabela1.setDefaultRenderer(Object.class, new CentralizarCelulasRenderer());
                        modelo.addRow(new Object[]{
                            quantidadeStr,
                            texto,
                            valor,
                            valorSoma
                        });
                        // insere nos produtosvendidos
                        CacheDados cache = CacheDados.getInstancia();
                        int idLoca = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();
                        if (idLoca == 0) {
                            DadosOcupados quartoOcupado = cache.getCacheOcupado().get(quartoEmFoco);
                            int novoID = new fquartos().getIdLocacao(quartoEmFoco);
                            quartoOcupado.setIdLoca(novoID);
                            cache.getCacheOcupado().put(quartoEmFoco, quartoOcupado);

                            cache.carregaProdutosNegociadosCache(novoID);
                        }
                        List<DadosVendidos> produtosVendidos = new ArrayList<>();
                        if (cache.cacheProdutosVendidos.containsKey(idLoca)) {
                            produtosVendidos = cache.cacheProdutosVendidos.get(idLoca);

                        }
                        produtosVendidos.add(new DadosVendidos(Integer.valueOf(idProdutoStr), Integer.valueOf(quantidadeStr)));
                        cache.cacheProdutosVendidos.put(idLoca, produtosVendidos);

                        produtodao.inserirPrevendido(idLoca, Integer.valueOf(idProdutoStr), Integer.valueOf(quantidadeStr));
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Quantidade inválida!");

                    }
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Código inserido invalido!");
                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Digite um valor válido!");
            }
        }
        focoQuarto();
    }
    private void txtDescontoNegociadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescontoNegociadoActionPerformed
        float valorRecebido = 0;
        try {
            valorRecebido = Float.parseFloat(txtDescontoNegociado.getText().replace(',', '.'));
            CacheDados cache = CacheDados.getInstancia();
            int idLocacao = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();

            salvaAntecipado(idLocacao, "negociado", valorRecebido);
            JOptionPane.showMessageDialog(null, "Desconto Antecipado adicionado com Sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Valor digitado não é monetário");
        }
    }//GEN-LAST:event_txtDescontoNegociadoActionPerformed

    private void txtAntecipadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAntecipadoActionPerformed
        float valorRecebido = 0;
        try {
            valorRecebido = Float.parseFloat(txtAntecipado.getText().replace(',', '.'));
            CacheDados cache = CacheDados.getInstancia();
            int idLocacao = cache.getCacheOcupado().get(quartoEmFoco).getIdLoca();

            salvaAntecipado(idLocacao, "recebido", valorRecebido);
            JOptionPane.showMessageDialog(null, "Recebimento Antecipado adicionado com Sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Valor digitado não é monetário");
        }


    }//GEN-LAST:event_txtAntecipadoActionPerformed

    private void menuResBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuResBackupActionPerformed
        CacheDados cache = CacheDados.getInstancia();
        cache.mostrarCacheQuarto();

    }//GEN-LAST:event_menuResBackupActionPerformed

    private void menuFazBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFazBackupActionPerformed
        CacheDados cache = CacheDados.getInstancia();
        cache.mostrarCacheProdutosVendidos();
    }//GEN-LAST:event_menuFazBackupActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Você tem certeza que deseja encerrar a aplicação?", "Confirmação de Encerramento",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            CacheDados cache = CacheDados.getInstancia();
            cache.alteraRunning(false);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dispose();  // Fecha a janela e chama windowClosed
            System.exit(0);
        } else {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }//GEN-LAST:event_formWindowClosing

    private void lblAdicionarAlarmeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAdicionarAlarmeMouseClicked
        //EventroAbrirAlarme

    }//GEN-LAST:event_lblAdicionarAlarmeMouseClicked

    private void botaoTrocaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoTrocaActionPerformed
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
                model.addRow(new Object[]{quarto.getNumeroQuarto(), quarto.getTipoQuarto()});

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
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Selecionar linha inteira ao clicar
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        // Adiciona o listener de clique na tabela para selecionar a linha inteira
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();  // Obtém a linha selecionada
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
        dialog.setLocationRelativeTo(null);  // Centraliza na tela
        // Configura o botão "Voltar" para fechar o JDialog
        botaoVoltar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();  // Fecha o JDialog
            }
        });

        // Configura o botão "Trocar" para trocar o quarto selecionado
        botaoTrocar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();  // Obtém a linha selecionada
                if (row != -1) {
                    int numeroQuarto = (int) table.getValueAt(row, 0);  // Obtém o número do quarto da linha selecionada

                    // Abre o JOptionPane de confirmação
                    int resposta = JOptionPane.showConfirmDialog(
                            dialog,
                            "Deseja prosseguir a troca de quarto?",
                            "Confirmação",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (resposta == JOptionPane.YES_OPTION) {
                        trocaQuarto(idLocacao, numeroQuarto);  // Chama o método de troca de quarto
                        dialog.dispose();  // Fecha o JDialog após a troca
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Por favor, selecione um quarto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        // Exibe o JDialog
        dialog.setVisible(true);

    }//GEN-LAST:event_botaoTrocaActionPerformed
    private void trocaQuarto(int idLocacao, int numeroNovoQuarto) {
        mudaStatusNaCache(quartoEmFoco, "limpeza");
        mudaStatusNaCache(numeroNovoQuarto, "ocupado-periodo");
        fquartos quarto = new fquartos();
        quarto.adicionaRegistro(quartoEmFoco, "limpeza");
        quarto.setStatus(quartoEmFoco, "limpeza");
        quarto.setStatus(numeroNovoQuarto, "ocupado-periodo");
        String SQL = "UPDATE table registralocado set numquarto=" + numeroNovoQuarto + " where idlocacao = " + idLocacao;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(SQL);
            int linhasAfetadas = statement.executeUpdate();
            if (linhasAfetadas == 1) {
                JOptionPane.showMessageDialog(null, "Troca de quarto realizada com sucesso!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao trocar o quarto: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (link != null) {
                try {
                    link.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        focoQuarto();
        mostraQuartos();

    }

    public void salvaAntecipado(int idLocacao, String tipo, float valor) {
        CacheDados cache = CacheDados.getInstancia();
        if (idLocacao == 0) {
            DadosOcupados quartoOcupado = cache.getCacheOcupado().get(quartoEmFoco);
            int novoID = new fquartos().getIdLocacao(quartoEmFoco);
            quartoOcupado.setIdLoca(novoID);
            cache.getCacheOcupado().put(quartoEmFoco, quartoOcupado);
        }

        List<Negociados> negociacoes = new ArrayList<>();
        if (cache.cacheNegociado.containsKey(idLocacao)) {
            negociacoes = cache.cacheNegociado.get(idLocacao);

            //preciso iterar em negociações ver se tem algum negociado onde tipo = tipoNegociado
            for (Negociados negociado : negociacoes) {
                if (negociado.tipo.equals(tipo)) {
                    negociado.valor = valor;
                }
            }

        } else {
            Negociados negociado = new Negociados(tipo, valor);
            negociacoes.add(negociado);
            cache.cacheNegociado.put(idLocacao, negociacoes);
        }

        Connection link = null;
        try {
            link = new fazconexao().conectar();
            // Primeiro, tenta atualizar um registro existente
            String updateSQL = "UPDATE antecipado SET valor = ? WHERE idlocacao = ? AND tipo = ?";
            PreparedStatement updateStatement = link.prepareStatement(updateSQL);
            updateStatement.setFloat(1, valor);
            updateStatement.setInt(2, idLocacao);
            updateStatement.setString(3, tipo);

            int rowsUpdated = updateStatement.executeUpdate();
            updateStatement.close();

            // Se nenhum registro foi atualizado, significa que não existe registro para o idLocacao e tipo, então faz a inserção
            if (rowsUpdated == 0) {
                // Consulta SQL para inserção de dados na tabela antecipado
                String insertSQL = "INSERT INTO antecipado (idlocacao, tipo, valor) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = link.prepareStatement(insertSQL);
                insertStatement.setInt(1, idLocacao);
                insertStatement.setString(2, tipo);
                insertStatement.setFloat(3, valor);
                insertStatement.executeUpdate();
                insertStatement.close();
            }

            // Fecha os recursos
            link.close();
        } catch (SQLException e) {
            // Tratamento de erro
            JOptionPane.showMessageDialog(null, "Erro ao inserir/atualizar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Isso pode ser removido em versões finais para evitar vazamento de informações sensíveis
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
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */

        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        new TelaPrincipal();

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botaoEncerrar;
    private javax.swing.JButton botaoIniciar;
    private javax.swing.JButton botaoStatus;
    private javax.swing.JButton botaoTroca;
    private javax.swing.JMenu btCadastros;
    private javax.swing.JMenuItem btConfereCaixa;
    private javax.swing.JMenuItem btConfereLocacao;
    private javax.swing.JMenu btFerramentas;
    private javax.swing.JMenu btFuncionario;
    private javax.swing.JMenu btQuartos;
    private javax.swing.JButton bt_apagarProduto;
    private javax.swing.JButton bt_inserirProduto;
    private javax.swing.JMenuItem itemManutencao;
    private javax.swing.JMenuItem itemReserva;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
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
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel labelData;
    private javax.swing.JLabel labelHora;
    private javax.swing.JLabel lblAdicionarAlarme;
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
    private javax.swing.JMenuItem menuCadastraProduto;
    private javax.swing.JMenu menuCaixaBt;
    private javax.swing.JMenu menuConfigAd;
    private javax.swing.JMenuItem menuFazBackup;
    private javax.swing.JPopupMenu menuLimpeza;
    private javax.swing.JPopupMenu menuOcupado;
    private javax.swing.JMenuItem menuRelaVenProdutos;
    private javax.swing.JMenuItem menuResBackup;
    private javax.swing.JMenu menuSair;
    private javax.swing.JMenuItem menuSobSistema;
    private javax.swing.JPopupMenu menuStatus;
    private javax.swing.JMenuItem menuVerProdutos;
    private javax.swing.JPanel painelQuartos;
    private javax.swing.JPanel painelSecundario;
    private javax.swing.JDesktopPane painelSecundario2;
    private javax.swing.JPanel pnAdicionarAlarme;
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
