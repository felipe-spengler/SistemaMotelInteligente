package com.motelinteligente.dados;
import java.sql.Timestamp;

public class vprodutos {
    int idProduto;
    String descricao;
    float valor;
    String estoque;
    Timestamp dataCompra;
    String categoria;
    String imagem;
    String detalhes;

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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public vprodutos(int idProduto, String descricao, float valor, String estoque, Timestamp dataCompra, String categoria, String imagem, String detalhes) {
        this.idProduto = idProduto;
        this.descricao = descricao;
        this.valor = valor;
        this.estoque = estoque;
        this.dataCompra = dataCompra;
        this.categoria = categoria;
        this.imagem = imagem;
        this.detalhes = detalhes;
    }

    public vprodutos(int idProduto, String descricao, float valor, String estoque, Timestamp dataCompra) {
        this(idProduto, descricao, valor, estoque, dataCompra, "Diversos", null, null);
    }
}
