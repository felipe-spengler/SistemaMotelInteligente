package com.motelinteligente.dados;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Frame;
import javazoom.jl.player.Player;

public class playSound {

    private Clip clip;
    private Player player;
    private boolean isPlaying = false;

    public void playSound(String nomeSom) {
        try {
            // Use getResourceAsStream para carregar o som do resources
            InputStream audioInputStream = getClass().getClassLoader().getResourceAsStream(nomeSom);
            if (audioInputStream == null) {
                throw new FileNotFoundException("Arquivo de som não encontrado: " + nomeSom);
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(audioInputStream));
            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception ex) {
            exibirNotificacaoDiscreta("Aviso: Falha ao executar o som.");
            ex.printStackTrace();
        }
    }
    // Função para tocar o som em loop continuamente
    
    public void playSoundLoop(String nomeSom) {
        isPlaying = true;

        new Thread(() -> {
            try {
                while (isPlaying) {
                    // Use getResourceAsStream para carregar o som do resources
                    InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(nomeSom);
                    if (fileInputStream == null) {
                        throw new FileNotFoundException("Arquivo de som não encontrado: " + nomeSom);
                    }
                    player = new Player(fileInputStream);
                    player.play();
                    fileInputStream.close(); // Fecha o stream após tocar o som
                }
            } catch (Exception e) {
                exibirNotificacaoDiscreta("Aviso: Falha ao executar som em loop.");
                e.printStackTrace();
            }
        }).start();
    }

    public void stopSound() {
        if (player != null) {
            isPlaying = false; // Interrompe o loop
            player.close(); // Fecha o player, parando a execução do som
        }
    }

    private static void exibirNotificacaoDiscreta(String msg) {
        // Executa na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                JDialog dialog = new JDialog((Frame) null, false);
                dialog.setUndecorated(true);
                dialog.setAlwaysOnTop(true);
                dialog.setSize(300, 50);
                
                // Canto inferior direito (mesmo padrão de NotificacaoMensalidade)
                Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
                dialog.setLocation(scrSize.width - dialog.getWidth() - 20, scrSize.height - dialog.getHeight() - 60);
                
                JPanel panel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), 0, getHeight(), new Color(15, 23, 42));
                        g2d.setPaint(gp);
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    }
                };
                panel.setOpaque(false);
                panel.setLayout(new java.awt.BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                
                JLabel label = new JLabel(msg);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setForeground(new Color(248, 113, 113)); // Vermelho suave discreto
                panel.add(label, java.awt.BorderLayout.CENTER);
                
                dialog.setContentPane(panel);
                dialog.setVisible(true);
                
                // Auto-fechar após 4 segundos
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        dialog.dispose();
                    }
                }, 4000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
