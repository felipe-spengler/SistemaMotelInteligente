/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.security.Timestamp;

/**
 *
 * @author MOTEL
 */
public class CarregaQuarto {

    int numeroQuarto;
    String tipoQuarto;
    String statusQuarto;
    String horaStatus;

    public int getNumeroQuarto() {
        return numeroQuarto;
    }

    public void setNumeroQuarto(int numeroQuarto) {
        this.numeroQuarto = numeroQuarto;
    }

    public String getTipoQuarto() {
        return tipoQuarto;
    }

    public void setTipoQuarto(String tipoQuarto) {
        this.tipoQuarto = tipoQuarto;
    }

    public String getStatusQuarto() {
        return statusQuarto;
    }

    public void setStatusQuarto(String statusQuarto) {
        this.statusQuarto = statusQuarto;
    }

    public String getHoraStatus() {
        return horaStatus;
    }

    public void setHoraStatus(String horaStatus) {
        this.horaStatus = horaStatus;
    }

    public CarregaQuarto(int numeroQuarto, String tipoQuarto, String statusQuarto, String horaStatus) {
        this.numeroQuarto = numeroQuarto;
        this.tipoQuarto = tipoQuarto;
        this.statusQuarto = statusQuarto;
        this.horaStatus = horaStatus;
    }

    

    CarregaQuarto() {
    }

}
