/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.io.File;
import java.io.FileInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.swing.JOptionPane;
import javazoom.jl.player.Player;

public class playSound {

    private Clip clip;
    private Player player;
    private boolean isPlaying = false;

    public void playSound(String nomeSom) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(nomeSom).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro ao executar som!.");
            ex.printStackTrace();
        }
    }
    // Função para tocar o som em loop continuamente

    public void playSoundLoop(String nomeSom) {
        try {
            FileInputStream fileInputStream = new FileInputStream(nomeSom);
            player = new Player(fileInputStream);
            isPlaying = true;

            new Thread(() -> {
                try {
                    while (isPlaying) {
                        player.play();
                        fileInputStream.getChannel().position(0); // Reset the stream to loop
                        player = new Player(fileInputStream);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro ao executar som!");
            ex.printStackTrace();
        }
    }

    // Função para parar o som que está sendo reproduzido
    public void stopSound() {
        if (clip != null && clip.isRunning()) {
            clip.stop(); // Para o som
        }
    }
}
