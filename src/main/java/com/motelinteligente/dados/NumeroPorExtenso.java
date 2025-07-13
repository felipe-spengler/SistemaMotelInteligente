package com.motelinteligente.dados;

public class NumeroPorExtenso {

    private static final String[] UNIDADES = {
            "", "um", "dois", "tres", "quatro", "cinco", "seis", "sete", "oito", "nove"
    };

    private static final String[] DEZ_A_DEZENOVE = {
            "dez", "onze", "doze", "treze", "catorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove"
    };

    private static final String[] DEZENAS = {
            "", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa"
    };

    private static final String[] CENTENAS = {
            "", "cento e", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos"
    };

    public static String NumeroPorExtenso(float valor) {
        if (valor < 0 || valor > 9999.99f) {
            return "Valor fora do intervalo válido (0 a 9999.99)";
        }

        int parteInteira = (int) valor;
        int centavos = Math.round((valor - parteInteira) * 100);
        StringBuilder extenso = new StringBuilder();

        // Parte inteira
        if (parteInteira >= 1000) {
            int milhar = parteInteira / 1000;
            extenso.append(UNIDADES[milhar]).append(" mil");
            parteInteira %= 1000;
        }

        if (parteInteira > 100) {
            int centena = parteInteira / 100;
            extenso.append(" ").append(CENTENAS[centena]);
            parteInteira %= 100;
        }

        if (parteInteira == 100) {
            extenso.append(" cem");
            parteInteira = 0;
        } else if (parteInteira >= 20) {
            int dezena = parteInteira / 10;
            extenso.append(" ").append(DEZENAS[dezena]);
            parteInteira %= 10;
        }

        if (parteInteira >= 10) {
            extenso.append(" ").append(DEZ_A_DEZENOVE[parteInteira - 10]);
        } else if (parteInteira > 0) {
            if (extenso.length() > 0) {
                extenso.append(" e ");
            }
            extenso.append(UNIDADES[parteInteira]);
        }

        // Adiciona "reais"
        String valorExtenso = extenso.toString().trim();
        if (!valorExtenso.isEmpty()) {
            valorExtenso += (valorExtenso.equals("um") ? " real" : " reais");
        } else {
            valorExtenso = "zero reais";
        }

        // Parte dos centavos
        if (centavos > 0) {
            if (!valorExtenso.isEmpty()) {
                valorExtenso += " e ";
            }
            if (centavos < 10) {
                valorExtenso += UNIDADES[centavos];
            } else if (centavos < 20) {
                valorExtenso += DEZ_A_DEZENOVE[centavos - 10];
            } else {
                int dezena = centavos / 10;
                valorExtenso += DEZENAS[dezena];
                if (centavos % 10 != 0) {
                    valorExtenso += " e " + UNIDADES[centavos % 10];
                }
            }
            valorExtenso += " centavos";
        }

        return valorExtenso.trim();
    }

    public static void main(String[] args) {
        float[] valores = {0.00f, 1.00f, 3.00f, 10.00f, 11.00f, 12.00f, 15.00f, 20.00f, 21.00f, 71.00f, 30.00f, 100.00f, 101.00f, 110.00f, 111.00f, 999.99f, 1000.00f, 1001.00f, 1100.00f, 1111.00f, 9999.99f};
        for (float valor : valores) {
            System.out.println(valor + " = " + NumeroPorExtenso(valor));
        }
    }
}