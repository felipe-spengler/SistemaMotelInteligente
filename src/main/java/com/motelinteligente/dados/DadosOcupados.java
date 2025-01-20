/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;
import java.sql.Timestamp;

/**
 *
 * @author MOTEL
 */
public class DadosOcupados {
    private int idLoca;
    private float valorPeriodo;
    private float valorPernoite;
    private int numeroPessoas;
    private float valorAdicional;
    private String periodoQuarto;
    private java.sql.Timestamp horaEntrada;

    
    public float getValorPeriodo() {
        return valorPeriodo;
    }
    public Timestamp getHoraEntrada() {
        return horaEntrada;
    }
    public void setHoraEntrada(Timestamp recebido) {
        this.horaEntrada = recebido;
    }
    public void setValorPeriodo(float valorPeriodo) {
        this.valorPeriodo = valorPeriodo;
    }
    public String getTempoPeriodo() {
        return periodoQuarto;
    }

    public void setTempoPeriodo(String tempo) {
        this.periodoQuarto = tempo;
    }

    public float getValorPernoite() {
        return valorPernoite;
    }

    public void setValorPernoite(float valorPernoite) {
        this.valorPernoite = valorPernoite;
    }

    public int getNumeroPessoas() {
        return numeroPessoas;
    }

    public void setNumeroPessoas(int numeroPessoas) {
        this.numeroPessoas = numeroPessoas;
    }

    public float getValorAdicional() {
        return valorAdicional;
    }

    public void setValorAdicional(float valorAdicional) {
        this.valorAdicional = valorAdicional;
    }
    public int getIdLoca() {
        return idLoca;
    }

    public void setIdLoca(int idLoca) {
        this.idLoca = idLoca;
    }
    public DadosOcupados(java.sql.Timestamp entrada, int idLocacao, float valorPeriodo, float valorPernoite, int numeroPessoas, float valorAdicional, String tempo) {
        this.horaEntrada = entrada;
        this.idLoca = idLocacao;
        this.valorPeriodo = valorPeriodo;
        this.valorPernoite = valorPernoite;
        this.numeroPessoas = numeroPessoas;
        this.valorAdicional = valorAdicional;
        this.periodoQuarto = tempo;
    }
}
