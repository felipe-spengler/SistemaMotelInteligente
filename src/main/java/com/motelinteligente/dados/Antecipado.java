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
public class Antecipado {

    private String tipo;
    private float valor;
    private Timestamp hora;

    public Antecipado(String tipo, float valor, Timestamp hora) {
        this.tipo = tipo;
        this.valor = valor;
        this.hora = hora;
    }

    // Getters e Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public Timestamp getHora() {
        return hora;
    }

    public void setHora(Timestamp hora) {
        this.hora = hora;
    }

}
