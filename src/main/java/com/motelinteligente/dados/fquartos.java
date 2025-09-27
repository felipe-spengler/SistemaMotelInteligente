/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class fquartos {

    private static final Logger logger = LoggerFactory.getLogger(fquartos.class);

    private final fazconexao conexao = new fazconexao();

    public List<CarregaQuarto> uploadQuartos() {
        List<CarregaQuarto> quartos = new ArrayList<>();
        String consultaSQL = "SELECT q.numeroquarto, s.atualquarto AS status, s.horastatus AS data, q.tipoquarto AS tipo FROM quartos q JOIN status s ON q.numeroquarto = s.numeroquarto ORDER BY q.numeroquarto";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL); ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                CarregaQuarto quarto = new CarregaQuarto();
                quarto.setNumeroQuarto(resultado.getInt("numeroquarto"));
                quarto.setTipoQuarto(resultado.getString("tipo"));
                quarto.setStatusQuarto(resultado.getString("status"));
                quarto.setHoraStatus(resultado.getTimestamp("data").toString());
                quartos.add(quarto);
            }
        } catch (SQLException e) {
            logger.error("Erro ao carregar quartos: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao carregar quartos: " + e.getMessage());
        }
        return quartos;
    }

    public List<vquartos> mostrar() {
        List<vquartos> quartos = new ArrayList<>();
        String consultaSQL = "SELECT * FROM quartos ORDER BY numeroquarto";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL); ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                vquartos quarto = new vquartos();
                quarto.setTipoquarto(resultado.getString("tipoquarto"));
                quarto.setNumeroquarto(resultado.getInt("numeroquarto"));
                quarto.setValorquarto(resultado.getFloat("valorquarto"));
                quarto.setPernoitequarto(resultado.getFloat("pernoitequarto"));
                quartos.add(quarto);
            }
        } catch (SQLException e) {
            logger.error("Erro ao mostrar quartos: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao mostrar quartos: " + e.getMessage());
        }
        return quartos;
    }

    // Dentro da sua classe fquartos.java
    public boolean setStatus(int numero, String status) {
        // Este método usa o horário atual, como já havíamos refatorado
        return setStatus(numero, status, new Timestamp(new Date().getTime()));
    }

    public boolean setStatus(int numero, String status, Timestamp horario) {
        String consultaSQL = "UPDATE status SET atualquarto = ?, horastatus = ? WHERE numeroquarto = ?";
        try (Connection link = new fazconexao().conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setString(1, status);
            statement.setTimestamp(2, horario);
            statement.setInt(3, numero);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao definir status: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao definir status: " + e.getMessage());
            return false;
        }
    }

    public boolean alteraOcupado(int numero, String status) {
        String consultaSQL = "UPDATE status SET atualquarto = ? WHERE numeroquarto = ?";

        try (Connection link = new fazconexao().conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            statement.setString(1, status);
            statement.setInt(2, numero);

            // Retorna true se pelo menos uma linha foi atualizada
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            // Loga o erro para depuração
            logger.error("Erro ao alterar status (alteraOcupado): ", e);
            // Exibe uma mensagem amigável para o usuário
            JOptionPane.showMessageDialog(null, "Erro ao alterar o status do quarto: " + e.getMessage());
            return false;
        }
    }

    public boolean insercao(vquartos dados, float adicional, String periodo) {
        String insertQuartosSQL = "INSERT INTO quartos (tipoquarto, numeroquarto, valorquarto, pernoitequarto) VALUES (?, ?, ?, ?)";
        String insertStatusSQL = "INSERT INTO status (numeroquarto, atualquarto, horastatus, periodo, adicional) VALUES (?, ?, ?, ?, ?)";
        try (Connection link = conexao.conectar()) {
            link.setAutoCommit(false); // Inicia a transação
            try (PreparedStatement statementQuartos = link.prepareStatement(insertQuartosSQL); PreparedStatement statementStatus = link.prepareStatement(insertStatusSQL)) {

                // Insere na tabela 'quartos'
                statementQuartos.setString(1, dados.getTipoquarto());
                statementQuartos.setInt(2, dados.getNumeroquarto());
                statementQuartos.setFloat(3, dados.getValorquarto());
                statementQuartos.setFloat(4, dados.getPernoitequarto());
                if (statementQuartos.executeUpdate() == 0) {
                    link.rollback();
                    return false;
                }

                // Insere na tabela 'status'
                statementStatus.setInt(1, dados.getNumeroquarto());
                statementStatus.setString(2, "livre");
                statementStatus.setTimestamp(3, new Timestamp(new Date().getTime()));
                statementStatus.setString(4, periodo);
                statementStatus.setFloat(5, adicional);
                if (statementStatus.executeUpdate() == 0) {
                    link.rollback();
                    return false;
                }

                link.commit(); // Confirma a transação
                return true;
            } catch (SQLException e) {
                link.rollback(); // Desfaz a transação em caso de erro
                throw e; // Lança a exceção para ser capturada pelo bloco principal
            }
        } catch (SQLException e) {
            logger.error("Erro na inserção de quarto: ", e);
            JOptionPane.showMessageDialog(null, "Erro na inserção de quarto: " + e.getMessage());
            return false;
        }
    }

    public boolean fazOUp(vquartos dados, float hora_adicional, String periodo) {
        // SQL to update data in 'quartos' table
        String consultaQuarto = "UPDATE quartos SET tipoquarto = ?, valorquarto = ?, pernoitequarto = ? WHERE numeroquarto = ?";

        // SQL to update data in 'status' table
        String consultaStatus = "UPDATE status SET adicional = ?, periodo = ? WHERE numeroquarto = ?";

        try (Connection link = new fazconexao().conectar()) {
            link.setAutoCommit(false); // Begin the transaction

            // Update the 'quartos' table
            try (PreparedStatement statementQuarto = link.prepareStatement(consultaQuarto)) {
                statementQuarto.setString(1, dados.getTipoquarto());
                statementQuarto.setFloat(2, dados.getValorquarto());
                statementQuarto.setFloat(3, dados.getPernoitequarto());
                statementQuarto.setInt(4, dados.getNumeroquarto());
                statementQuarto.executeUpdate();
            }

            // Update the 'status' table
            try (PreparedStatement statementStatus = link.prepareStatement(consultaStatus)) {
                statementStatus.setFloat(1, hora_adicional);
                statementStatus.setString(2, periodo);
                statementStatus.setInt(3, dados.getNumeroquarto());
                statementStatus.executeUpdate();
            }

            link.commit(); // Commit changes if both updates were successful
            return true;

        } catch (SQLException e) {
            // If an error occurs, the transaction is rolled back automatically by the `try-with-resources` block (if not already committed)
            logger.error("Erro ao fazer UPDATE no quarto e locação: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao atualizar os dados: " + e.getMessage());
            return false;
        }

    }

    public boolean edicao(vquartos dados) {
        String consultaSQL = "UPDATE quartos SET tipoquarto = ?, valorquarto = ?, pernoitequarto = ? WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setString(1, dados.getTipoquarto());
            statement.setFloat(2, dados.getValorquarto());
            statement.setFloat(3, dados.getPernoitequarto());
            statement.setInt(4, dados.getNumeroquarto());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro na edição de quarto: ", e);
            JOptionPane.showMessageDialog(null, "Erro na edição de quarto: " + e.getMessage());
            return false;
        }
    }

    public int numeroQuartos() {
        String consultaSQL = "SELECT COUNT(*) FROM quartos";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL); ResultSet resultado = statement.executeQuery()) {
            if (resultado.next()) {
                return resultado.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erro ao contar quartos: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao contar quartos: " + e.getMessage());
        }
        return 0;
    }

    public boolean exclusao(int idQuarto) {
        String deleteStatusSQL = "DELETE FROM status WHERE numeroquarto = ?";
        String deleteQuartosSQL = "DELETE FROM quartos WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar()) {
            link.setAutoCommit(false); // Inicia a transação
            try (PreparedStatement statementStatus = link.prepareStatement(deleteStatusSQL); PreparedStatement statementQuartos = link.prepareStatement(deleteQuartosSQL)) {

                // Exclui da tabela 'status'
                statementStatus.setInt(1, idQuarto);
                statementStatus.executeUpdate();

                // Exclui da tabela 'quartos'
                statementQuartos.setInt(1, idQuarto);
                if (statementQuartos.executeUpdate() == 0) {
                    link.rollback();
                    return false;
                }

                link.commit(); // Confirma a transação
                return true;
            } catch (SQLException e) {
                link.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Erro na exclusão de quarto: ", e);
            JOptionPane.showMessageDialog(null, "Erro na exclusão de quarto: " + e.getMessage());
            return false;
        }
    }

    public boolean verExiste(int numero) {
        String consultaSQL = "SELECT COUNT(*) FROM quartos WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numero);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar se quarto existe: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao verificar se quarto existe: " + e.getMessage());
            return true; // Retornar true por segurança para evitar inserção duplicada em caso de erro
        }
    }

    public boolean adicionaRegistro(int numeroQuarto, String tipoSet) {
        String nomeTabela = switch (tipoSet) {
            case "manutencao" ->
                "registramanutencao";
            case "limpeza" ->
                "registralimpeza";
            case "reservado" ->
                "registrareserva";
            default -> {
                logger.error("Tipo de registro inválido: {}", tipoSet);
                JOptionPane.showMessageDialog(null, "Tipo de registro inválido.");
                throw new IllegalArgumentException("Tipo de registro inválido: " + tipoSet);
            }
        };

        String consultaSQL = "INSERT INTO " + nomeTabela + " (numquarto, horaentrada) VALUES (?, ?)";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numeroQuarto);
            statement.setTimestamp(2, new Timestamp(new Date().getTime()));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao adicionar registro para " + tipoSet + ": ", e);
            JOptionPane.showMessageDialog(null, "Erro ao adicionar registro: " + e.getMessage());
            return false;
        }
    }
    // Dentro da sua classe fquartos.java

    public boolean insercao(vquartos dados) {
        // A consulta de inserção do quarto
        String consultaQuarto = "INSERT INTO quartos (numquarto, tpquarto, valorperiodo, valorpernoite) VALUES (?, ?, ?, ?)";
        // A consulta de inserção do status inicial
        String consultaStatus = "INSERT INTO status (numquarto, atualquarto, horastatus) VALUES (?, ?, ?)";

        try (Connection link = new fazconexao().conectar()) {
            link.setAutoCommit(false); // Inicia a transação

            // Insere o quarto
            try (PreparedStatement statementQuarto = link.prepareStatement(consultaQuarto)) {
                statementQuarto.setInt(1, dados.getIdquartos());
                statementQuarto.setString(2, dados.getTipoquarto());
                statementQuarto.setFloat(3, dados.getValorquarto());
                statementQuarto.setFloat(4, dados.getPernoitequarto());
                statementQuarto.executeUpdate();
            }

            // Insere o status inicial (Livre)
            try (PreparedStatement statementStatus = link.prepareStatement(consultaStatus)) {
                statementStatus.setInt(1, dados.getIdquartos());
                statementStatus.setString(2, "livre");
                statementStatus.setTimestamp(3, new Timestamp(new Date().getTime()));
                statementStatus.executeUpdate();
            }

            link.commit(); // Confirma a transação
            return true;

        } catch (SQLException e) {
            // Em caso de erro, desfaz a transação para evitar dados incompletos
            try (Connection link = new fazconexao().conectar()) {
                link.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Erro ao fazer rollback: ", rollbackEx);
            }
            logger.error("Erro ao inserir novo quarto: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar novo quarto: " + e.getMessage());
            return false;
        }
    }

    public void salvaLocacao(int idPassado, Timestamp horaInicio, Timestamp horaFim, float valorDoQuarto, float valorConsumo, float valD, float valP, float valC) {
        int idCaixa = configGlobal.getInstance().getCaixa();
        String consultaSQL = "UPDATE registralocado SET horafim=?, horainicio=?, valorquarto=?, valorconsumo=?, pagodinheiro=?, pagopix=?, pagocartao=?, idcaixaatual=? WHERE idlocacao=?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setTimestamp(1, horaFim);
            statement.setTimestamp(2, horaInicio);
            statement.setFloat(3, valorDoQuarto);
            statement.setFloat(4, valorConsumo);
            statement.setFloat(5, valD);
            statement.setFloat(6, valP);
            statement.setFloat(7, valC);
            statement.setInt(8, idCaixa);
            statement.setInt(9, idPassado);
            if (statement.executeUpdate() == 0) {
                logger.error("Nenhuma linha foi atualizada para idlocacao: " + idPassado);
                JOptionPane.showMessageDialog(null, "Contacte o suporte: Erro locação " + idPassado);
            }
        } catch (SQLException e) {
            logger.error("Erro ao salvar locação: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao salvar locação: " + e.getMessage());
        }
    }

    public boolean registraLocacao(int numeroQuarto) {
        String verificaDuplicidadeSQL = "SELECT COUNT(*) FROM registralocado WHERE numquarto = ? AND horainicio = ?";
        String consultaSQL = "INSERT INTO registralocado (numquarto, horainicio, numpessoas) VALUES (?, ?, ?)";
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        Connection link = null;
        PreparedStatement statement = null;

        try {
            link = new fazconexao().conectar();

            // Verificar duplicidade
            statement = link.prepareStatement(verificaDuplicidadeSQL);
            statement.setInt(1, numeroQuarto);
            statement.setTimestamp(2, timestamp);
            ResultSet rs = statement.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Registro duplicado encontrado
                String mensagemErro = "registro duplicado: Quarto = " + numeroQuarto + ", Horário = " + timestamp;
                logger.error( "Erro: Registro duplicado encontrado.\n" + mensagemErro);
                JOptionPane.showMessageDialog(null, "Erro: Registro duplicado encontrado.\n" + mensagemErro);
                return false;
            }

            // Fechar o statement de verificação antes de criar o próximo
            statement.close();

            // Inserir registro
            statement = link.prepareStatement(consultaSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, numeroQuarto);
            statement.setTimestamp(2, timestamp);
            statement.setInt(3, 2);

            int n = statement.executeUpdate();
            if (n != 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int idLocacaoGerado = generatedKeys.getInt(1);
                    //logger.info("Registro inserido com sucesso: ID = " + idLocacaoGerado + ", Quarto = " + numeroQuarto);

                    //insere na cache ocupado
                    float valPeriodo = 0, valPernoite = 0, valAdicional = 0;
                    int pessoas = 0;
                    fquartos quartodao = new fquartos();
                    valPernoite = quartodao.getValorQuarto(numeroQuarto, "pernoite");
                    valPeriodo = quartodao.getValorQuarto(numeroQuarto, "periodo");
                    valAdicional = quartodao.getAdicional(numeroQuarto);
                    pessoas = quartodao.getPessoas(numeroQuarto);
                    int idLoca = new fquartos().getIdLocacao(numeroQuarto);
                    String tempo = quartodao.getPeriodo(numeroQuarto);
                    DadosOcupados ocupado = new DadosOcupados(timestamp, idLocacaoGerado, valPeriodo, valPernoite, pessoas, valAdicional, tempo);
                    CacheDados cache = CacheDados.getInstancia();
                    cache.getCacheOcupado().put(numeroQuarto, ocupado);

                    generatedKeys.close();
                } else {
                    logger.warn("Registro inserido, mas nenhum ID foi retornado. Quarto = " + numeroQuarto + ", Horário = " + timestamp);
                }
                return true;
            } else {
                logger.warn("Falha ao inserir registro. Nenhuma linha afetada. SQL: " + consultaSQL);
            }
        } catch (SQLException e) {
            logger.error("Erro ao executar registraLocacao: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao registrar locação: " + e.getMessage());
        } finally {
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro ao fechar recursos: ", e);
            }
        }
        return false;
    }

    public boolean salvaProduto(int idLocacao, int idProduto, int qnt, float valorUnidade, float valorTotal) {
        int idCaixa = configGlobal.getInstance().getCaixa();
        String consultaSQL = "INSERT INTO registravendido (idlocacao, idproduto, quantidade, valorunidade, valortotal, idcaixaatual) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idLocacao);
            statement.setInt(2, idProduto);
            statement.setInt(3, qnt);
            statement.setFloat(4, valorUnidade);
            statement.setFloat(5, valorTotal);
            statement.setInt(6, idCaixa);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao salvar produto: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao salvar produto: " + e.getMessage());
            return false;
        }
    }

    public void atualizaPessoas(int idQuarto, int numPessoas) {
        String consultaSQL = "UPDATE registralocado SET numpessoas = ? WHERE numquarto = ? AND horafim IS NULL";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numPessoas);
            statement.setInt(2, idQuarto);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao atualizar número de pessoas: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao atualizar número de pessoas: " + e.getMessage());
        }
    }

    public int getPessoas(int idQuarto) {
        String consultaSQL = "SELECT numpessoas FROM registralocado WHERE numquarto = ? AND horafim IS NULL";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("numpessoas");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter número de pessoas: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter número de pessoas: " + e.getMessage());
        }
        return 0;
    }

    public Timestamp getHoraInicio(int idLocacao) {
        String consultaSQL = "SELECT horainicio FROM registralocado WHERE idlocacao = ? AND horafim IS NULL";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idLocacao);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getTimestamp("horainicio");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter hora de início: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter hora de início: " + e.getMessage());
        }
        return null;
    }

    public int getIdLocacao(int idQuarto) {
        String consultaSQL = "SELECT idlocacao FROM registralocado WHERE numquarto = ? AND horafim IS NULL";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("idlocacao");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter ID de locação: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter ID de locação: " + e.getMessage());
        }
        return 0;
    }

    public boolean alteraRegistro(int numeroQuarto, String tipoSet) {
        String nomeTabela = null;

        if (tipoSet.contains("-")) {
            nomeTabela = "registralocado";
        } else if (tipoSet.equals("manutencao")) {
            nomeTabela = "registramanutencao";
        } else if (tipoSet.equals("limpeza")) {
            nomeTabela = "registralimpeza";
        } else if (tipoSet.equals("reservado")) {
            nomeTabela = "registrareserva";
        } else if (tipoSet.equals("ocupado")) {
            nomeTabela = "registralocacao";
        }

        if (nomeTabela == null) {
            return false;
        }

        String consultaSQL = "SELECT horaEntrada FROM " + nomeTabela + " WHERE numquarto = ? AND tempoTotal IS NULL";
        Timestamp horaBanco = null;

        try (Connection link = new fazconexao().conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            statement.setInt(1, numeroQuarto);
            System.out.println("Consulta SQL: " + consultaSQL);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    horaBanco = resultado.getTimestamp("horaEntrada");
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao consultar horaEntrada. SQL: " + consultaSQL, e);
            JOptionPane.showMessageDialog(null, "Erro ao consultar horaEntrada: " + e.getMessage());
            return false;
        }

        if (horaBanco == null) {
            return false;
        }

        Timestamp horaAtual = new Timestamp(System.currentTimeMillis());
        long diferencaMillis = horaAtual.getTime() - horaBanco.getTime();

        long minutos = diferencaMillis / (60 * 1000) % 60;
        long horas = diferencaMillis / (60 * 60 * 1000);
        String diferencaFormatada = String.format("%02d:%02d", horas, minutos);

        consultaSQL = "UPDATE " + nomeTabela + " SET tempoTotal = ?, horaEntrada = ? WHERE numquarto = ? AND tempoTotal IS NULL";

        try (Connection link = new fazconexao().conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            statement.setString(1, diferencaFormatada);
            statement.setTimestamp(2, horaBanco);
            statement.setInt(3, numeroQuarto);       
            int n = statement.executeUpdate();
            return n != 0;

        } catch (Exception e) {
            logger.error("Erro ao atualizar registro. SQL: " + consultaSQL, e);
            JOptionPane.showMessageDialog(null, "Erro ao atualizar registro: " + e.getMessage());
            return false;
        }
    }

    // Métodos utilitários adicionados para completar a funcionalidade do código original
    public String getStatus(int numeroQuarto) {
        String consultaSQL = "SELECT atualquarto FROM status WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numeroQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getString("atualquarto");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter status do quarto: ", e);
        }
        return "desconhecido";
    }

    public String getDataInicio(int numeroQuarto) {
        String consultaSQL = "SELECT horastatus FROM status WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numeroQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getTimestamp("horastatus").toString();
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter data de início: ", e);
        }
        return "N/A";
    }

    public String getTipo(int numeroQuarto) {
        String consultaSQL = "SELECT tipoquarto FROM quartos WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numeroQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getString("tipoquarto");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter tipo de quarto: ", e);
        }
        return "desconhecido";
    }

    public float getValorQuarto(int numeroQuarto, String tipoValor) {
        String coluna = "valorquarto";
        if ("pernoite".equals(tipoValor)) {
            coluna = "pernoitequarto";
        }
        String consultaSQL = "SELECT " + coluna + " FROM quartos WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numeroQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getFloat(coluna);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter valor do quarto: ", e);
        }
        return 0;
    }

    public float getAdicional(int numeroQuarto) {
        String consultaSQL = "SELECT adicional FROM status WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numeroQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getFloat("adicional");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter valor adicional: ", e);
        }
        return 0;
    }

    public String getPeriodo(int numeroQuarto) {
        String consultaSQL = "SELECT periodo FROM status WHERE numeroquarto = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, numeroQuarto);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getString("periodo");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter período: ", e);
        }
        return null;
    }

    public int getIdCaixa(int idLocacao) {
        String consultaSQL = "SELECT idcaixaatual FROM registralocado WHERE idlocacao = ?";
        try (Connection link = conexao.conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idLocacao);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("idcaixaatual");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter ID do caixa: ", e);
        }
        return 0;
    }
    public String getData(int idPassado) {
    String consultaSQL = "SELECT horastatus FROM status WHERE numeroquarto = ?";
    
    try (Connection link = new fazconexao().conectar();
         PreparedStatement statement = link.prepareStatement(consultaSQL)) {
        
        statement.setInt(1, idPassado);
        
        try (ResultSet resultado = statement.executeQuery()) {
            if (resultado.next()) {
                Timestamp horaBanco = resultado.getTimestamp("horastatus");
                long diferencaMillis = System.currentTimeMillis() - horaBanco.getTime();
                
                long horas = diferencaMillis / (60 * 60 * 1000);
                long minutos = (diferencaMillis % (60 * 60 * 1000)) / (60 * 1000);
                
                return String.format("%02d:%02d", horas, minutos);
            }
        }
    } catch (SQLException e) {
        logger.error("Erro ao obter a diferença de tempo: ", e);
        JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage());
    }
   
    return null;
}
}
