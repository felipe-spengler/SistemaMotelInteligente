/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;
/**
 *
 * @author MOTEL
 */
public class vquartos {
    private int idquartos;
    private String tipoquarto;
    private int numeroquarto;
    private float valorquarto;
    private float pernoitequarto;	
    public float addPessoa;

    public vquartos(int idquartos, String tipoquarto, int numeroquarto, float valorquarto, float pernoitequarto, float adicionalPessoa) {
        this.idquartos = idquartos;
        this.tipoquarto = tipoquarto;
        this.numeroquarto = numeroquarto;
        this.valorquarto = valorquarto;
        this.pernoitequarto = pernoitequarto;
        this.addPessoa = adicionalPessoa;
    }
    public vquartos(){
        
    }

    public int getIdquartos() {
        return idquartos;
    }

    public void setIdquartos(int idquartos) {
        this.idquartos = idquartos;
    }

    public String getTipoquarto() {
        return tipoquarto;
    }
    public float getAddPessoa(){
        return addPessoa;
    }
    public void setAddPessoa(float valor){
        this.addPessoa = valor;
    }
    public void setTipoquarto(String tipoquarto) {
        this.tipoquarto = tipoquarto;
    }

    public int getNumeroquarto() {
        return numeroquarto;
    }

    public void setNumeroquarto(int numeroquarto) {
        this.numeroquarto = numeroquarto;
    }

    public float getValorquarto() {
        return valorquarto;
    }

    public void setValorquarto(float valorquarto) {
        this.valorquarto = valorquarto;
    }

    public float getPernoitequarto() {
        return pernoitequarto;
    }

    public void setPernoitequarto(float pernoitequarto) {
        this.pernoitequarto = pernoitequarto;
    }
    
    
}
