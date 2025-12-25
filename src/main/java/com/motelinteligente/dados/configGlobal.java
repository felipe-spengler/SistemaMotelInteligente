package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class configGlobal {
    private static final Logger logger = LoggerFactory.getLogger(fquartos.class);
    private static configGlobal instancia;
    private String usuario;
    private String cargoUsuario;
    private int caixaAberto;
    private String telaMostrar;
    private boolean mudanca;
    private boolean logoffecharcaixa;
    private boolean controlaEstoque;
    private boolean flagFechar;
    public float addPessoa = 30;
    private boolean flagSistemaSpring;
    private boolean flagArduino;
    private boolean flagMesmoUserCaixa;
    private boolean portoesRF;
    private int limiteDesconto;
    private BackupQueueManager backupQueueManager;
    private static int contadorConexoes = 0;
    private int alarmesAtivos = 0;
    private String caminhoAudio;
    private boolean clienteSeleciona;
    private boolean subtelaAtiva;

    // Construtor privado para evitar a criação de múltiplas instâncias
    public configGlobal() {
        usuario = null;
        cargoUsuario = "Visitante";
        caixaAberto = 0;
        portoesRF = true;
        mudanca = false;
        logoffecharcaixa = false;
        controlaEstoque = false;
        flagSistemaSpring = flagArduino = false;
        telaMostrar = null;

    }

    public int getAlarmesAtivos() {
        return alarmesAtivos;
    }

    public void setAlarmesAtivos(int alarmesAtivos) {
        this.alarmesAtivos = alarmesAtivos;
    }

    public void incrementarAlarme() {
        this.alarmesAtivos++;
    }

    public void decrementarAlarme() {
        if (this.alarmesAtivos > 0) {
            this.alarmesAtivos--;
        }
    }

    public static void incrementarContadorExecucoes() {
        contadorConexoes++;
        logger.warn(" Total de conexoes remotas estabelecidas: " + contadorConexoes);
    }

    public static int getContadorExecucoes() {
        return contadorConexoes;
    }

    public BackupQueueManager getBackupQueueManager() {
        return backupQueueManager;
    }

    public void initializeBackupQueueManager(BackupQueueManager backupQueueManager) {
        this.backupQueueManager = backupQueueManager;
    }

    public int getLimiteDesconto() {
        return limiteDesconto;
    }

    public void setLimiteDesconto(int limiteDesconto) {
        this.limiteDesconto = limiteDesconto;
    }

    public boolean isFlagMesmoUserCaixa() {
        return flagMesmoUserCaixa;
    }

    public boolean getPortoesRF() {
        return portoesRF;
    }

    public void setPortoesRF(boolean portoes) {
        this.portoesRF = portoes;
    }

    public void setFlagMesmoUserCaixa(boolean flagMesmoUserCaixa) {
        this.flagMesmoUserCaixa = flagMesmoUserCaixa;
    }

    public boolean isFlagSistemaSpring() {
        return flagSistemaSpring;
    }

    public void setFlagSistemaSpring(boolean flagSistemaSpring) {
        this.flagSistemaSpring = flagSistemaSpring;
    }

    public boolean isFlagArduino() {
        return flagArduino;
    }

    public void setFlagArduino(boolean flagArduino) {
        this.flagArduino = flagArduino;
    }

    public void carregarInformacoes(String cargo, String login) {
        this.setCargoUsuario(cargo);
        this.setUsuario(login);

        carregarConfiguracoesAdicionais();
    }

    public void carregarConfiguracoesAdicionais() {
        String consultaSQL = "SELECT * FROM configuracoes";

        // O try-with-resources garante que todos os recursos entre parênteses
        // (Connection, Statement, e ResultSet) serão fechados automaticamente.
        try (Connection link = new fazconexao().conectar();
                Statement statement = link.createStatement();
                ResultSet resultado = statement.executeQuery(consultaSQL)) {

            if (resultado.next()) {
                // Carrega as configurações do banco de dados
                this.logoffecharcaixa = resultado.getBoolean("logoffcaixa");
                this.controlaEstoque = resultado.getBoolean("estoque");
                this.flagMesmoUserCaixa = resultado.getBoolean("flagMesmoUserCaixa");
                this.limiteDesconto = resultado.getInt("limitadesconto");
                this.telaMostrar = resultado.getString("telaMostrar");
                this.telaMostrar = resultado.getString("telaMostrar");
                this.portoesRF = resultado.getBoolean("portoesrf");
                try {
                    this.caminhoAudio = resultado.getString("caminhoAudio");
                    this.clienteSeleciona = resultado.getBoolean("clienteSeleciona");
                    this.subtelaAtiva = resultado.getBoolean("subtelaAtiva");
                } catch (Exception ex) {
                    // Colunas podem não existir ainda
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Erro ao carregar Informações Adicionais. Nenhuma configuração encontrada.");
            }

        } catch (Exception e) {
            logger.error("Erro ao carregar configurações adicionais", e);
            JOptionPane.showMessageDialog(null, "Erro ao conectar e carregar informações: " + e.getMessage(),
                    "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCargoUsuario() {
        return cargoUsuario;
    }

    public void setCargoUsuario(String cargoUsuario) {
        this.cargoUsuario = cargoUsuario;
    }

    public int getCaixa() {
        return caixaAberto;
    }

    public void setCaixa(int caixaAberto) {
        this.caixaAberto = caixaAberto;
    }

    public boolean getMudanca() {
        return mudanca;
    }

    public boolean getFlagFechar() {
        return flagFechar;
    }

    public void setFlagFechar(boolean flag) {
        this.flagFechar = flag;
    }

    public void setMudanca(boolean mudanca) {
        this.mudanca = mudanca;
    }

    public boolean getLogoffecharcaixa() {
        return logoffecharcaixa;
    }

    public void setLogoffecharcaixa(boolean logoffecharcaixa) {
        this.logoffecharcaixa = logoffecharcaixa;
    }

    public boolean getControlaEstoque() {
        return controlaEstoque;
    }

    public void setControlaEstoque(boolean controlaEstoque) {
        this.controlaEstoque = controlaEstoque;
    }

    public static synchronized configGlobal getInstance() {
        if (instancia == null) {
            instancia = new configGlobal();
        }
        return instancia;
    }

    public String getTelaMostrar() {
        return telaMostrar;
    }

    public void setTelaMostrar(String usuario) {
        this.telaMostrar = usuario;
    }

    public String getCaminhoAudio() {
        return caminhoAudio;
    }

    public void setCaminhoAudio(String caminhoAudio) {
        this.caminhoAudio = caminhoAudio;
    }

    public boolean isClienteSeleciona() {
        return clienteSeleciona;
    }

    public void setClienteSeleciona(boolean clienteSeleciona) {
        this.clienteSeleciona = clienteSeleciona;
    }

    public boolean isSubtelaAtiva() {
        return subtelaAtiva;
    }

    public void setSubtelaAtiva(boolean subtelaAtiva) {
        this.subtelaAtiva = subtelaAtiva;
    }
}
