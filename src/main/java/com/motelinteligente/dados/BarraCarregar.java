package com.motelinteligente.dados;

import com.motelinteligente.telas.TelaPrincipal;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class BarraCarregar {

    private final JProgressBar progressBar;
    private final JFrame frame;

    public BarraCarregar() {
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        frame = new JFrame("Carregando dados...");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(300, 100);
        frame.setLocationRelativeTo(null);
        frame.add(progressBar);
    }

    public JFrame getFrame() {
        return frame;
    }

    private void atualizarBarraProgresso(int progresso) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progresso));
    }

    public void carregarDados() {
        frame.setVisible(true); // Exibe a barra de carregamento

        Thread thread = new Thread(() -> {
            CacheDados carregamento = CacheDados.getInstancia();
            carregamento.carregarDadosQuarto();

            // Quando o carregamento estiver completo (assumindo que 100% = carregamento concluído)
            SwingUtilities.invokeLater(() -> {
                frame.dispose(); // Fecha a barra de carregamento
                iniciarTelaPrincipal(); // Inicia a tela principal após o carregamento completo
            });
        });

        thread.start(); // Inicia a thread para carregar os dados
    }

    private void iniciarTelaPrincipal() {
        TelaPrincipal tela = new TelaPrincipal();
        tela.setVisible(true);
    }
}
