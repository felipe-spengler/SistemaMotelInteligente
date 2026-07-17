package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class configGlobal {
    private static final Logger logger = LoggerFactory.getLogger(configGlobal.class);
    private static configGlobal instancia;
    private String usuario;
    private String senhaTemporaria;
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
    private String portoesRF;
    private int limiteDesconto;
    private BackupQueueManager backupQueueManager;
    private static int contadorConexoes = 0;
    private int alarmesAtivos = 0;
    private String caminhoAudio;
    private boolean clienteSeleciona;
    private boolean subtelaAtiva;
    private boolean pedidosOnlineAtivo;
    private boolean impressoraAtiva;
    private String impressoraNome;
    private boolean luzAtiva;
    private float taxaCredito;
    private float taxaDebito;
    private float taxaPix;

    // Construtor privado para evitar a criação de múltiplas instâncias
    public configGlobal() {
        usuario = null;
        cargoUsuario = "Visitante";
        caixaAberto = 0;
        portoesRF = "BOTOEIRA";
        mudanca = false;
        logoffecharcaixa = false;
        controlaEstoque = false;
        flagSistemaSpring = flagArduino = false;
        telaMostrar = null;
        impressoraAtiva = false;
        impressoraNome = null;
        luzAtiva = false;
        taxaCredito = 0f;
        taxaDebito = 0f;
        taxaPix = 0f;
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

    public String getPortoesRF() {
        return portoesRF;
    }

    public void setPortoesRF(String portoes) {
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
        logger.info("[CONFIG] carregarInformacoes iniciado para usuario='{}' cargo='{}'", login, cargo);
        this.setCargoUsuario(cargo);
        this.setUsuario(login);

        carregarConfiguracoesAdicionais();
        logger.info("[CONFIG] carregarInformacoes concluído para usuario='{}' cargo='{}'", login, cargo);
    }

    private void verificarColunasImpressora(Connection link) {
        String[] colunas = {
            "impressora_ativa TINYINT(1) DEFAULT 0",
            "impressora_nome VARCHAR(255) DEFAULT NULL",
            "luz_ativa TINYINT(1) DEFAULT 0"
        };
        for (String col : colunas) {
            String nomeColuna = col.split(" ")[0];
            try (PreparedStatement testStmt = link.prepareStatement("SELECT " + nomeColuna + " FROM configuracoes LIMIT 1")) {
                testStmt.executeQuery();
            } catch (SQLException ex) {
                try (Statement alterStmt = link.createStatement()) {
                    alterStmt.executeUpdate("ALTER TABLE configuracoes ADD COLUMN " + col);
                    logger.info("Coluna " + nomeColuna + " adicionada com sucesso na tabela configuracoes.");
                } catch (SQLException alterEx) {
                    logger.error("Erro ao adicionar coluna " + nomeColuna + " em configuracoes: ", alterEx);
                }
            }
        }
    }

    private void verificarColunasTaxas(Connection link) {
        String[] colunas = {
            "taxa_credito FLOAT DEFAULT 0",
            "taxa_debito FLOAT DEFAULT 0",
            "taxa_pix FLOAT DEFAULT 0"
        };
        for (String col : colunas) {
            String nomeColuna = col.split(" ")[0];
            try (PreparedStatement testStmt = link.prepareStatement("SELECT " + nomeColuna + " FROM configuracoes LIMIT 1")) {
                testStmt.executeQuery();
            } catch (SQLException ex) {
                try (Statement alterStmt = link.createStatement()) {
                    alterStmt.executeUpdate("ALTER TABLE configuracoes ADD COLUMN " + col);
                    logger.info("Coluna " + nomeColuna + " adicionada com sucesso na tabela configuracoes.");
                } catch (SQLException alterEx) {
                    logger.error("Erro ao adicionar coluna " + nomeColuna + " em configuracoes: ", alterEx);
                }
            }
        }
    }

    private void verificarColunaPortoesRF(Connection link) {
        try {
            boolean tipoIncorreto = false;
            String typeStr = "";
            try (PreparedStatement stmt = link.prepareStatement("SHOW COLUMNS FROM configuracoes LIKE 'portoesrf'")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        typeStr = rs.getString("Type").toLowerCase();
                        if (typeStr.contains("tinyint") || typeStr.contains("bit") || typeStr.contains("bool")) {
                            tipoIncorreto = true;
                        }
                    }
                }
            }
            if (tipoIncorreto) {
                logger.info("[CONFIG] Coluna portoesrf com tipo antigo ({}). Migrando para VARCHAR(50)...", typeStr);
                
                // 1. Obter o valor atual
                boolean valorAtual = true;
                try (Statement stmt = link.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT portoesrf FROM configuracoes LIMIT 1")) {
                    if (rs.next()) {
                        valorAtual = rs.getBoolean("portoesrf");
                    }
                }
                
                // 2. Alterar o tipo da coluna
                try (Statement stmt = link.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE configuracoes MODIFY COLUMN portoesrf VARCHAR(50) DEFAULT 'BOTOEIRA'");
                }
                
                // 3. Atualizar com o valor mapeado
                String novoValor = valorAtual ? "RF" : "BOTOEIRA";
                try (PreparedStatement stmt = link.prepareStatement("UPDATE configuracoes SET portoesrf = ?")) {
                    stmt.setString(1, novoValor);
                    stmt.executeUpdate();
                }
                logger.info("[CONFIG] Migração da coluna portoesrf concluída. Novo valor: {}", novoValor);
            }
        } catch (SQLException e) {
            logger.error("[CONFIG] Erro ao verificar/migrar coluna portoesrf: ", e);
        }
    }

    public void carregarConfiguracoesAdicionais() {
        logger.info("[CONFIG] carregarConfiguracoesAdicionais iniciado");
        String consultaSQL = "SELECT * FROM configuracoes";

        try (Connection link = new fazconexao().conectar()) {
            verificarColunaPortoesRF(link);
            verificarColunasImpressora(link);
            verificarColunasTaxas(link);

            try (Statement statement = link.createStatement();
                    ResultSet resultado = statement.executeQuery(consultaSQL)) {

                if (resultado.next()) {
                    // Carrega as configurações do banco de dados
                    this.logoffecharcaixa = resultado.getBoolean("logoffcaixa");
                    this.controlaEstoque = resultado.getBoolean("estoque");
                    this.flagMesmoUserCaixa = resultado.getBoolean("flagMesmoUserCaixa");
                    this.limiteDesconto = resultado.getInt("limitadesconto");
                    this.telaMostrar = resultado.getString("telaMostrar");
                    this.portoesRF = resultado.getString("portoesrf");
                    if (this.portoesRF == null) {
                        this.portoesRF = "BOTOEIRA";
                    }
                    try {
                        this.caminhoAudio = resultado.getString("caminhoAudio");
                        this.clienteSeleciona = resultado.getBoolean("clienteSeleciona");
                        this.subtelaAtiva = resultado.getBoolean("subtelaAtiva");
                        this.pedidosOnlineAtivo = resultado.getBoolean("pedidos_online");
                    } catch (Exception ex) {
                        // Colunas podem não existir ainda
                    }
                    try {
                        this.impressoraAtiva = resultado.getBoolean("impressora_ativa");
                        this.impressoraNome = resultado.getString("impressora_nome");
                    } catch (Exception ex) {
                        // Colunas podem não existir ainda
                    }
                    try {
                        this.luzAtiva = resultado.getBoolean("luz_ativa");
                    } catch (Exception ex) {
                        // Coluna pode não existir ainda
                    }
                    try {
                        this.taxaCredito = resultado.getFloat("taxa_credito");
                        this.taxaDebito = resultado.getFloat("taxa_debito");
                        this.taxaPix = resultado.getFloat("taxa_pix");
                    } catch (Exception ex) {
                        // Colunas podem não existir ainda
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Erro ao carregar Informações Adicionais. Nenhuma configuração encontrada.");
                }
            }
            logger.info("[CONFIG] carregarConfiguracoesAdicionais concluído");

        } catch (Exception e) {
            logger.error("Erro ao carregar configurações adicionais", e);
            JOptionPane.showMessageDialog(null, "Erro ao conectar e carregar informações: " + e.toString(),
                    "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    public float getTaxaCredito() {
        return taxaCredito;
    }

    public void setTaxaCredito(float taxaCredito) {
        this.taxaCredito = taxaCredito;
    }

    public float getTaxaDebito() {
        return taxaDebito;
    }

    public void setTaxaDebito(float taxaDebito) {
        this.taxaDebito = taxaDebito;
    }

    public float getTaxaPix() {
        return taxaPix;
    }

    public void setTaxaPix(float taxaPix) {
        this.taxaPix = taxaPix;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenhaTemporaria() {
        return senhaTemporaria;
    }

    public void setSenhaTemporaria(String senhaTemporaria) {
        this.senhaTemporaria = senhaTemporaria;
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

    public boolean isPedidosOnlineAtivo() {
        return pedidosOnlineAtivo;
    }

    public void setPedidosOnlineAtivo(boolean pedidosOnlineAtivo) {
        this.pedidosOnlineAtivo = pedidosOnlineAtivo;
    }

    public boolean isImpressoraAtiva() {
        return impressoraAtiva;
    }

    public void setImpressoraAtiva(boolean impressoraAtiva) {
        this.impressoraAtiva = impressoraAtiva;
    }

    public String getImpressoraNome() {
        return impressoraNome;
    }

    public void setImpressoraNome(String impressoraNome) {
        this.impressoraNome = impressoraNome;
    }

    public boolean isLuzAtiva() {
        return luzAtiva;
    }

    public void setLuzAtiva(boolean luzAtiva) {
        this.luzAtiva = luzAtiva;
    }
}
