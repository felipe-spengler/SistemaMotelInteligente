package com.motelinteligente.dados;

public class PeriodoQuarto {
    private int id;
    private int numeroQuarto;
    private String descricao;
    private int tempoMinutos;
    private float valor;
    private boolean isPernoite;
    private int ordem;

    public PeriodoQuarto() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNumeroQuarto() { return numeroQuarto; }
    public void setNumeroQuarto(int numeroQuarto) { this.numeroQuarto = numeroQuarto; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getTempoMinutos() { return tempoMinutos; }
    public void setTempoMinutos(int tempoMinutos) { this.tempoMinutos = tempoMinutos; }
    public float getValor() { return valor; }
    public void setValor(float valor) { this.valor = valor; }
    public boolean isPernoite() { return isPernoite; }
    public void setIsPernoite(boolean isPernoite) { this.isPernoite = isPernoite; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}
