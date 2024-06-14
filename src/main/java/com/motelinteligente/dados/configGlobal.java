package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class configGlobal {

    private static configGlobal instancia;
    private String usuario;
    private String cargoUsuario;
    private int caixaAberto;
    private boolean mudanca;
    private boolean logoffecharcaixa;
    private boolean controlaEstoque;
    private boolean flagFechar;
    public float addPessoa = 30;
    private boolean flagSistemaSpring;
    private boolean flagArduino;
    private boolean flagMesmoUserCaixa;
    private int limiteDesconto;
    public int contador;
    

    // Construtor privado para evitar a criação de múltiplas instâncias
    public configGlobal() {
        usuario = null;
        cargoUsuario = "Visitante";
        caixaAberto = 0;
        mudanca = false;
        logoffecharcaixa = false;
        controlaEstoque = false;
        flagSistemaSpring = flagArduino = false;
        contador =0;
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
    public void carregarConfiguracoesAdicionais(){
 
        String consultaSQL = "SELECT * FROM configuracoes";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                //carrega as configurações do banco de dados
                this.logoffecharcaixa = resultado.getBoolean("logoffcaixa");
                this.controlaEstoque = resultado.getBoolean("estoque");
                this.flagMesmoUserCaixa = resultado.getBoolean("flagMesmoUserCaixa");
                this.limiteDesconto = resultado.getInt("limitadesconto");
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao carregar Informações Adicionais.");
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
    }

    public int getContador(){
        return contador;
    }
    public void aumentaContador(){
        contador++;
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

}
