/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
        "", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos"
    };

    public static String NumeroPorExtenso(float valor) {
        if (valor < 0 || valor > 9999) {
            return "Valor fora do intervalo válido (0 a 9999)";
        }

        int parteInteira = (int) valor;
        int centavos = Math.round((valor - parteInteira) * 100);

        String extenso = "";

        // Parte inteira
        if (parteInteira == 100) {
            extenso += "cem";
        } else if (parteInteira >= 1000) {
            int milhar = parteInteira / 1000;
            extenso += UNIDADES[milhar] + " mil ";
            parteInteira %= 1000;
        }

        if (parteInteira >= 100) {
            int centena = parteInteira / 100;
            if (centena == 1 && parteInteira % 100 != 0) {
                extenso += "cento e ";
            } else if (centena > 0) {
                extenso += CENTENAS[centena] + " ";
            }
            parteInteira %= 100;
        }

        if (parteInteira >= 20) {
            int dezena = parteInteira / 10;
            extenso += DEZENAS[dezena] + " ";
            parteInteira %= 10;
        }

        if (parteInteira >= 10) {
            extenso += DEZ_A_DEZENOVE[parteInteira - 10] + " ";
        } else if (parteInteira > 0) {
            extenso += "e " + UNIDADES[parteInteira] + " ";
        }

        // Centavos
        if (centavos > 0) {
            extenso += "e " + centavos + " centavos";
        }

        return extenso.trim();
    }
}
