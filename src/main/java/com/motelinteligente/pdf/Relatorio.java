package com.motelinteligente.pdf;


import java.util.List;

public interface Relatorio {

    public void gerarCabecalho(String data);

    public void gerarCorpo(List<List<Object>> dadosDasColunas);

    public void gerarRodape();

    public void imprimir();
}
