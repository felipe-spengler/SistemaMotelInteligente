/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;


/**
 *
 * @author MOTEL
 */
public class vfuncionario {
    private int idfuncionario;
    private String nomefuncionario;
    private String loginfuncionario;
    private String senhafuncionario;
    private String cargofuncionario;

    public vfuncionario(int idfuncionario, String nomefuncionario, String cargofuncionario, String loginfuncionario, String senhafuncionario) {
        this.idfuncionario = idfuncionario;
        this.nomefuncionario = nomefuncionario;
        this.loginfuncionario = loginfuncionario;
        this.senhafuncionario = senhafuncionario;
        this.cargofuncionario = cargofuncionario;
    }
    public vfuncionario(){
        
    }

    public int getIdfuncionario() {
        return idfuncionario;
    }

    public void setIdfuncionario(int idfuncionario) {
        this.idfuncionario = idfuncionario;
    }

    public String getNomefuncionario() {
        return nomefuncionario;
    }

    public void setNomefuncionario(String nomefuncionario) {
        this.nomefuncionario = nomefuncionario;
    }

    public String getLoginfuncionario() {
        return loginfuncionario;
    }

    public void setLoginfuncionario(String loginfuncionario) {
        this.loginfuncionario = loginfuncionario;
    }

    public String getSenhafuncionario() {
        return senhafuncionario;
    }

    public void setSenhafuncionario(String senhafuncionario) {
        this.senhafuncionario = senhafuncionario;
    }

    public String getCargofuncionario() {
        return cargofuncionario;
    }

    public void setCargofuncionario(String cargofuncionario) {
        this.cargofuncionario = cargofuncionario;
    }

    
    
}
