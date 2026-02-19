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
public class vprodutos {
    int idProduto;
    String descricao;
    float valor;
    String estoque;
    Timestamp dataCompra;

    public vprodutos() {
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public String getEstoque() {
        return estoque;
    }

    public void setEstoque(String estoque) {
        this.estoque = estoque;
    }

    public Timestamp getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(Timestamp dataCompra) {
        this.dataCompra = dataCompra;
    }

    public vprodutos(int idProduto, String descricao, float valor, String estoque, Timestamp dataCompra) {
        this.idProduto = idProduto;
        this.descricao = descricao;
        this.valor = valor;
        this.estoque = estoque;
        this.dataCompra = dataCompra;
    }

}
