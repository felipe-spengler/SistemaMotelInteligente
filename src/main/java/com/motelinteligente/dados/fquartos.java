/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.sql.Connection;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class fquartos {

    private static final Logger logger = LoggerFactory.getLogger(fprodutos.class);

    public List<CarregaQuarto> uploadQuartos() {
        List<CarregaQuarto> quartos = new ArrayList<>();
        int numeroQuarto = 0;
        String data;
        String status;
        String tipo;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            String consultaSQL = "select * from quartos order by numeroquarto";
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            ResultSet resultado = statement.executeQuery();

            // Processar o resultado
            while (resultado.next()) {

                numeroQuarto = resultado.getInt("numeroquarto");
                status = new fquartos().getStatus(numeroQuarto);
                data = new fquartos().getDataInicio(numeroQuarto);
                tipo = new fquartos().getTipo(numeroQuarto);

                CarregaQuarto quarto = new CarregaQuarto();
                quarto.setNumeroQuarto(numeroQuarto);
                quarto.setTipoQuarto(tipo);
                quarto.setStatusQuarto(status);
                quarto.setHoraStatus(data);
                quartos.add(quarto);
            }

            // Fechar os recursos
            resultado.close();
            statement.close();
            link.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            logger.error("Erro : fquartos() : ", e);

        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : fquartos() : ", e);
            }
        }
        return quartos;
    }

    public List<vquartos> mostrar() {
        List<vquartos> quartos = new ArrayList<>();
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            String consultaSQL = "select * from quartos order by numeroquarto";
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            ResultSet resultado = statement.executeQuery();

            // Processar o resultado
            while (resultado.next()) {
                //vquartos quarto = new vquartos(0,null, 0, 0 ,0);
                vquartos quarto = new vquartos();
                quarto.setTipoquarto(resultado.getString("tipoquarto"));
                quarto.setNumeroquarto(resultado.getInt("numeroquarto"));
                quarto.setValorquarto(resultado.getFloat("valorquarto"));
                quarto.setPernoitequarto(resultado.getFloat("pernoitequarto"));

                quartos.add(quarto);
            }

            // Fechar os recursos
            resultado.close();
            statement.close();
            link.close();
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return quartos;
    }

    public boolean setStatus(int numero, String status) {
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());

        String consultaSQL = "update status set atualquarto='" + status + "' , horastatus= '" + timestamp
                + "' where numeroquarto= '" + numero + "'";

        int n = 0;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            //imprimir no output
            n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showMessageDialog(null, "Erro setStatus " + e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return false;
    }

    public boolean setStatusHorario(int numero, String status, Timestamp hora) {
        Date dataAtual = new Date();

        String consultaSQL = "update status set atualquarto='" + status + "' , horastatus= '" + hora
                + "' where numeroquarto= '" + numero + "'";

        int n = 0;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            //imprimir no output
            n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showMessageDialog(null, "Erro setStatus " + e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return false;
    }

    public boolean alteraOcupado(int numero, String status) {
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());

        String consultaSQL = "update status set atualquarto='" + status + "' where numeroquarto= '" + numero + "'";

        int n = 0;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return false;
    }

    public boolean insercao(vquartos dados, float adicional, String periodo) {
        String consultaSQL = "INSERT INTO quartos (tipoquarto, numeroquarto, valorquarto, pernoitequarto) VALUES (?, ?, ?, ?)";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            //statement.setInt(1, dados.getIdquartos());
            statement.setString(1, dados.getTipoquarto());
            statement.setInt(2, dados.getNumeroquarto());
            statement.setFloat(3, dados.getValorquarto());
            statement.setFloat(4, dados.getPernoitequarto());
            int n = statement.executeUpdate();
            if (n != 0) {

                // se o quarto foi inserido
                //define o status do quarto
                consultaSQL = "INSERT INTO status (numeroquarto, atualquarto, horastatus,periodo, adicional) VALUES (?, ?, ?, ?, ?)";
                Date dataAtual = new Date();
                Timestamp timestamp = new Timestamp(dataAtual.getTime());
                statement = link.prepareStatement(consultaSQL);

                statement.setInt(1, dados.getNumeroquarto());
                statement.setString(2, "livre");
                statement.setTimestamp(3, timestamp);
                statement.setString(4, periodo);
                statement.setFloat(5, adicional);
                int n2 = statement.executeUpdate();
                if (n2 != 0) {
                    link.close();
                    statement.close();

                    return true;

                } else {
                    link.close();
                    statement.close();
                    return false;
                }
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
            logger.error("Erro : fquartos() : ", e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : fquartos() : ", e);
            }
        }

    }

    public boolean edicao(vquartos dados) {
        String consultaSQL = "update quartos set (tipoquarto=?, numeroquarto=?, valorquarto=?, pernoitequarto=?)"
                + "where numeroquarto = ?";
        Connection link = null;

        try {

            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setString(1, dados.getTipoquarto());
            statement.setInt(2, dados.getNumeroquarto());
            statement.setFloat(3, dados.getValorquarto());
            statement.setFloat(4, dados.getValorquarto());
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public int numeroQuartos() {
        String consultaSQL = "SELECT COUNT(*) FROM quartos";
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                int numero = resultado.getInt(1);
                link.close();
                resultado.close();
                return 5;
            }
        } catch (SQLException e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return 0;
    }

    public boolean exclusao(int idpassado) {
        String consultaSQL = "delete from status "
                + "where numeroquarto = " + idpassado;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            int n = statement.executeUpdate();
            if (n != 0) {
                consultaSQL = "delete from quartos "
                        + "where numeroquarto = " + idpassado;
                statement = link.prepareStatement(consultaSQL);

                int n2 = statement.executeUpdate();
                if (n2 != 0) {
                    link.close();
                    statement.close();

                    return true;

                } else {
                    link.close();
                    statement.close();
                    return false;
                }
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : fquartos() : ", e);
            }
        }
    }

    public boolean verExiste(int numero) {
        String consultaSQL = "SELECT COUNT(*) FROM quartos WHERE numeroquarto = ?";
        Connection link = null;
        try {
            link = new fazconexao().conectar();

            PreparedStatement statement = link.prepareStatement(consultaSQL);

            statement.setInt(1, numero);
            ResultSet resultado = statement.executeQuery();

            if (resultado.next()) {
                int count = resultado.getInt(1);
                if (count > 0) {
                    link.close();
                    statement.close();
                    return true;

                } else {
                    link.close();
                    statement.close();
                    return false;
                }
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
            logger.error("Erro : fquartos() : ", e);
            return true;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }

        return true;
    }

    public int getIdCaixa(int numero) {
        String consultaSQL = "SELECT idcaixaatual FROM registralocado WHERE idlocacao = ?";
        Connection link = null;
        try {
            link = new fazconexao().conectar();

            PreparedStatement statement = link.prepareStatement(consultaSQL);

            statement.setInt(1, numero);
            ResultSet resultado = statement.executeQuery();

            if (resultado.next()) {
                return resultado.getInt("idcaixaatual");
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }

        return 0;
    }

    public boolean adicionaRegistro(int numeroQuarto, String tipoSet) {
        String nomeTabela = null;
        Connection link = null;
        if (tipoSet.equals("manutencao")) {
            nomeTabela = "registramanutencao";
        }
        if (tipoSet.equals("limpeza")) {
            nomeTabela = "registralimpeza";
        }
        if (tipoSet.equals("reservado")) {
            nomeTabela = "registrareserva";
        }
        if (tipoSet.equals("ocupado")) {
            nomeTabela = "registralocacao";
        }
        String consultaSQL = "INSERT INTO " + nomeTabela + " (numquarto, horaentrada) VALUES ( ?, ?)";
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        try {
            link = null;
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, numeroQuarto);
            statement.setTimestamp(2, timestamp);
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, "Erro addRegistro(): " + e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    public void salvaLocacao(int idPassado, Timestamp horaInicio, Timestamp horaFim, float valorDoQuarto, float valorConsumo, float valD, float valP, float valC) {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        String consultaSQL = "UPDATE registralocado SET horafim='" + horaFim
                + "', horainicio='" + horaInicio
                + // Adicione as aspas simples aqui
                "', valorquarto=" + valorDoQuarto
                + ", valorconsumo=" + valorConsumo
                + ", pagodinheiro=" + valD
                + ", pagopix=" + valP
                + ", pagocartao=" + valC
                + ", idcaixaatual=" + idCaixa
                + " WHERE idlocacao=" + idPassado;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public boolean registraLocacao(int numeroQuarto) {
        String consultaSQL = "INSERT INTO registralocado (numquarto, horainicio, numpessoas) VALUES ( ?, ?, ?)";
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        Connection link = null;
        int idLocacao = -1;  // Variável para armazenar o id gerado

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, numeroQuarto);
            statement.setTimestamp(2, timestamp);
            statement.setInt(3, 2);

            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public boolean salvaProduto(int id, int idProduto, int qnt, float valor, float valorTotal) {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        String consultaSQL = "INSERT INTO registravendido ( idlocacao , idproduto, quantidade,valorunidade, valortotal, idcaixaatual) VALUES ("
                + id + " , "
                + idProduto + " , "
                + qnt + " , "
                + valor + " , "
                + valorTotal + ", "
                + idCaixa + " )";
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public void atualizaPessoas(int idPassado, int numPessoas) {
        Connection link = null;
        String consultaSQL = "UPDATE registralocado SET numpessoas=" + numPessoas + "  WHERE numquarto = " + idPassado + " AND horafim IS NULL";
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public int getPessoas(int idPassado) {
        Connection link = null;
        String consultaSQL = "SELECT numpessoas FROM registralocado WHERE numquarto = " + idPassado + " AND horafim IS NULL";
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return resultado.getInt("numpessoas");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return 0;
    }

    public int getIdLocacao(int idPassado) {
        Connection link = null;
        String consultaSQL = "SELECT idlocacao FROM registralocado WHERE numquarto = " + idPassado + " AND horafim IS NULL";
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return resultado.getInt("idlocacao");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return 0;
    }

    public boolean alteraRegistro(int numeroQuarto, String tipoSet) {
        System.out.println("altera reg " + numeroQuarto);
        String nomeTabela = null;
        Connection link = null;
        if (tipoSet.contains("-")) {
            nomeTabela = "registralocacao";
        }
        if (tipoSet.equals("manutencao")) {
            nomeTabela = "registramanutencao";
        }
        if (tipoSet.equals("limpeza")) {
            nomeTabela = "registralimpeza";
        }
        if (tipoSet.equals("reservado")) {
            nomeTabela = "registrareserva";
        }
        if (tipoSet.equals("ocupado")) {
            nomeTabela = "registralocacao";
        }
        Date dataAtual = new Date();
        Timestamp horaAtual = new Timestamp(dataAtual.getTime());
        Timestamp horaBanco = null;
        String consultaSQL = "SELECT horaEntrada FROM " + nomeTabela + " WHERE numquarto = '" + numeroQuarto + "' AND tempoTotal IS NULL";
        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();
            System.out.println(consultaSQL);
            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                horaBanco = resultado.getTimestamp("horaEntrada");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, "Erro altReg() " + e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }

        long diferencaMillis = horaAtual.getTime() - horaBanco.getTime();

        // Calcula a diferença em horas, minutos e segundos
        long minutos = diferencaMillis / (60 * 1000) % 60;
        long horas = diferencaMillis / (60 * 60 * 1000);

        // Formata a diferença no formato hh:mm:ss
        String diferencaFormatada = String.format("%02d:%02d", horas, minutos);
        consultaSQL = "UPDATE " + nomeTabela + " SET tempoTotal = '"
                + diferencaFormatada + "', horaEntrada = '"
                + horaBanco + "' WHERE numquarto = '" + numeroQuarto + "' AND tempoTotal IS NULL";

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            logger.error("Erro : fquartos() : ", e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    public String getStatus(int idPassado) {
        Connection link = null;
        String consultaSQL = "SELECT atualquarto FROM status WHERE numeroquarto = " + idPassado;
        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return (String) resultado.getString("atualquarto");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public String getPeriodo(int idPassado) {
        String consultaSQL = "SELECT periodo FROM status WHERE numeroquarto = " + idPassado;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return (String) resultado.getString("periodo");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public float getAdicional(int idPassado) {
        String consultaSQL = "SELECT adicional FROM status WHERE numeroquarto = " + idPassado;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return resultado.getFloat("adicional");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        }
        return 0;
    }

    public String getDataInicio(int idPassado) {
        String consultaSQL = "SELECT horastatus FROM status WHERE numeroquarto = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                Timestamp valor = resultado.getTimestamp("horastatus");
                return String.valueOf(valor);
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public String getTipo(int idPassado) {
        String consultaSQL = "SELECT tipoquarto FROM quartos WHERE numeroquarto = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return (String) resultado.getString("tipoquarto");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public float getValorQuarto(int numero, String valorPegar) {
        String consultaSQL = null;
        Connection link = null;

        if (valorPegar.equals("pernoite")) {
            consultaSQL = "SELECT pernoitequarto FROM quartos WHERE numeroquarto = " + numero;

        } else {
            consultaSQL = "SELECT valorquarto FROM quartos WHERE numeroquarto = " + numero;
        }
        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                if (valorPegar.equals("pernoite")) {
                    return resultado.getFloat("pernoitequarto");

                } else {
                    return resultado.getFloat("valorquarto");
                }
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return 0;
    }

    public String getData(int idPassado) {
        String consultaSQL = "SELECT horastatus FROM status WHERE numeroquarto = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {

                Timestamp horaBanco = resultado.getTimestamp("horastatus");
                Long datetime = System.currentTimeMillis();
                Timestamp horaAtual = new Timestamp(datetime);

                long diferencaMillis = horaAtual.getTime() - horaBanco.getTime();

                // Calcula a diferença em horas, minutos e segundos
                long minutos = diferencaMillis / (60 * 1000) % 60;
                long horas = diferencaMillis / (60 * 60 * 1000);

                // Formata a diferença no formato hh:mm:ss
                String diferencaFormatada = String.format("%02d:%02d", horas, minutos);
                return diferencaFormatada;
            } else {
                link.close();
                statement.close();
                //return false;
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public vquartos getDadosQuarto(int numero) {
        // falta finalizar essa função
        String consultaSQL = "SELECT * FROM quartos WHERE numeroquarto = " + numero;
        Connection link = null;
        vquartos quarto = null;
        String tipo;
        Float valorQuarto;
        Float pernoiteQuarto;
        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                tipo = resultado.getString("tipoquarto");
                valorQuarto = resultado.getFloat("valorquarto");
                pernoiteQuarto = resultado.getFloat("pernoitequarto");
            } else {
                link.close();
                statement.close();
                //return false;
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public boolean fazOUp(vquartos dados, float hora_adicional, String periodo) {
        // Consulta SQL para atualizar dados na tabela 'quartos'
        String consultaSQL = "UPDATE quartos SET tipoquarto=?, valorquarto=?, pernoitequarto=? WHERE numeroquarto = ?";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setString(1, dados.getTipoquarto());
            statement.setFloat(2, dados.getValorquarto());
            statement.setFloat(3, dados.getPernoitequarto());
            statement.setInt(4, dados.getNumeroquarto());
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        // Chama a função fazOUPStatus para atualizar o status do quarto
        return fazOUPStatus(dados.getNumeroquarto(), hora_adicional, periodo);
    }

    public boolean fazOUPStatus(int numero, float hora_adicional, String periodo) {
        // Consulta SQL para atualizar dados na tabela 'status'
        String consultaSQL = "UPDATE status SET adicional=?, periodo=? WHERE numeroquarto=?";

        int n = 0;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setFloat(1, hora_adicional);
            statement.setString(2, periodo);
            statement.setInt(3, numero);

            n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erro : fquartos() : ", e);
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : fquartos() : ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return false;
    }

}
