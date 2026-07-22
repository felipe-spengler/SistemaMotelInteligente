package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class fazconexao {

    // 🔑 Usar um único DataSource (pool) em toda a aplicação
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(CarregarVariaveis.getLocalDbUrl());
            config.setUsername(CarregarVariaveis.getUser());
            config.setPassword(CarregarVariaveis.getPassword());

            // Configurações recomendadas
            config.setMaximumPoolSize(15);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 min
            config.setConnectionTimeout(30000);
            config.setLeakDetectionThreshold(60000);
            // Previne conexões zumbis
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(3000); // 3s pra testar
            config.setMaxLifetime(600000); // recicla a conexão a cada 10 min
            config.setKeepaliveTime(60000); // manda ping a cada 1 min
            dataSource = new HikariDataSource(config);
            inicializarBancoDeDados();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao inicializar pool de conexões: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void inicializarBancoDeDados() {
        String sqlVendasAvulsas = "CREATE TABLE IF NOT EXISTS vendas_avulsas (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idcaixa INT NOT NULL," +
                "  idproduto INT NOT NULL," +
                "  descricao VARCHAR(255) NOT NULL," +
                "  quantidade INT NOT NULL," +
                "  valorunidade FLOAT NOT NULL," +
                "  valortotal FLOAT NOT NULL," +
                "  tipo VARCHAR(50) NOT NULL," +
                "  formapagamento VARCHAR(50) NOT NULL," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_vendas_avulsas_idcaixa (idcaixa)," +
                "  KEY idx_vendas_avulsas_idproduto (idproduto)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        String sqlDespesas = "CREATE TABLE IF NOT EXISTS despesas (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idcaixa INT DEFAULT NULL," +
                "  descricao VARCHAR(255) NOT NULL," +
                "  categoria VARCHAR(100) NOT NULL," +
                "  valor FLOAT NOT NULL," +
                "  formapagamento VARCHAR(50) NOT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'pago'," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_despesas_idcaixa (idcaixa)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        String sqlRetiradas = "CREATE TABLE IF NOT EXISTS retiradas_caixa (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idcaixa INT NOT NULL," +
                "  valor FLOAT NOT NULL," +
                "  quem VARCHAR(100) NOT NULL," +
                "  justificativa VARCHAR(255) NOT NULL," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_retiradas_caixa_idcaixa (idcaixa)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        String sqlAuditoria = "CREATE TABLE IF NOT EXISTS auditoria_locacoes (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idlocacao INT NOT NULL," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  campo_alterado VARCHAR(100) NOT NULL," +
                "  valor_antigo VARCHAR(255) DEFAULT NULL," +
                "  valor_novo VARCHAR(255) DEFAULT NULL," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_auditoria_locacoes_idlocacao (idlocacao)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        try (Connection link = dataSource.getConnection();
             java.sql.Statement stmt = link.createStatement()) {
            stmt.executeUpdate(sqlVendasAvulsas);
            stmt.executeUpdate(sqlDespesas);
            stmt.executeUpdate(sqlRetiradas);
            stmt.executeUpdate(sqlAuditoria);

            // Migrar categorias de despesas antigas para a nova nomenclatura alinhada com o site
            try {
                stmt.executeUpdate("UPDATE despesas SET categoria = 'Gastos Fixos' WHERE categoria IN ('Água/Luz/Internet', 'Água / Luz / Internet')");
                stmt.executeUpdate("UPDATE despesas SET categoria = 'Limpeza / Lavanderia' WHERE categoria IN ('Limpeza', 'Lavanderia')");
                stmt.executeUpdate("UPDATE despesas SET categoria = 'Salários / Comissões' WHERE categoria = 'Salários'");
                stmt.executeUpdate("UPDATE despesas SET categoria = 'Frigobar / Reposição' WHERE categoria IN ('Reposição Mercadoria', 'Cozinha')");
            } catch (Exception ex) {
                // Silencia se houver qualquer problema ou se a tabela não tiver registros
            }

            // Criar triggers para vendas_avulsas
            criarTriggerSeNaoExistir(stmt, "trg_vendas_avulsas_insert", "vendas_avulsas", "INSERT", "id");
            criarTriggerSeNaoExistir(stmt, "trg_vendas_avulsas_update", "vendas_avulsas", "UPDATE", "id");
            criarTriggerSeNaoExistir(stmt, "trg_vendas_avulsas_delete", "vendas_avulsas", "DELETE", "id");

            // Criar triggers para despesas
            criarTriggerSeNaoExistir(stmt, "trg_despesas_insert", "despesas", "INSERT", "id");
            criarTriggerSeNaoExistir(stmt, "trg_despesas_update", "despesas", "UPDATE", "id");
            criarTriggerSeNaoExistir(stmt, "trg_despesas_delete", "despesas", "DELETE", "id");

            // Criar triggers para retiradas_caixa
            criarTriggerSeNaoExistir(stmt, "trg_retiradas_caixa_insert", "retiradas_caixa", "INSERT", "id");
            criarTriggerSeNaoExistir(stmt, "trg_retiradas_caixa_update", "retiradas_caixa", "UPDATE", "id");
            criarTriggerSeNaoExistir(stmt, "trg_retiradas_caixa_delete", "retiradas_caixa", "DELETE", "id");

            // Criar trigger para auditoria_locacoes
            criarTriggerSeNaoExistir(stmt, "trg_auditoria_locacoes_insert", "auditoria_locacoes", "INSERT", "id");

        } catch (Exception e) {
            System.err.println("Erro ao inicializar tabelas no banco de dados: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao criar tabelas no banco de dados. " +
                    "Verifique se o usuário tem privilégios de CREATE.\nErro: " + e.getMessage(), 
                    "Erro de Inicialização de Banco", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void criarTriggerSeNaoExistir(java.sql.Statement stmt, String triggerNome, String tabelaNome, String operacao, String campoId) {
        try {
            stmt.executeUpdate("DROP TRIGGER IF EXISTS " + triggerNome);
            String sqlTrigger = "CREATE TRIGGER " + triggerNome + " " +
                    "AFTER " + operacao + " ON " + tabelaNome + " " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "  INSERT INTO log_sincronizacao (tabela_nome, registro_id) " +
                    "  VALUES ('" + tabelaNome + "', " + (operacao.equalsIgnoreCase("DELETE") ? "OLD" : "NEW") + "." + campoId + "); " +
                    "END;";
            stmt.executeUpdate(sqlTrigger);
        } catch (Exception e) {
            System.err.println("Erro ao criar trigger " + triggerNome + ": " + e.getMessage());
        }
    }

    public Connection conectar() throws SQLException {
        // Agora, cada chamada pega uma conexão do pool
        Connection conn = dataSource.getConnection();
        return createConnectionProxy(conn);
    }

    private Connection createConnectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                new Class[] { Connection.class },
                new BackupProxyHandler(connection));
    }

    public int verificaCaixa() {
        String consultaSQL = "SELECT id FROM caixa WHERE saldofecha IS NULL";
        try (Connection link = conectar();
                 PreparedStatement statement = link.prepareStatement(consultaSQL);
                 ResultSet resultado = statement.executeQuery()) {

            if (resultado.next()) {
                return resultado.getInt("id");
            }
            return 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
            return 0;
        }
    }

    public static void registrarAuditoria(int idLocacao, String campo, String valorAntigo, String valorNovo) {
        String usuario = configGlobal.getInstance().getUsuario();
        if (usuario == null || usuario.trim().isEmpty()) {
            usuario = "Sistema/Desconhecido";
        }
        String sql = "INSERT INTO auditoria_locacoes (idlocacao, usuario, campo_alterado, valor_antigo, valor_novo) VALUES (?, ?, ?, ?, ?)";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement stmt = link.prepareStatement(sql)) {
            stmt.setInt(1, idLocacao);
            stmt.setString(2, usuario);
            stmt.setString(3, campo);
            stmt.setString(4, valorAntigo);
            stmt.setString(5, valorNovo);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao registrar auditoria de locação: " + e.getMessage());
        }
    }

    // Método opcional para fechar o pool ao encerrar o sistema
    public static void fecharPool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
