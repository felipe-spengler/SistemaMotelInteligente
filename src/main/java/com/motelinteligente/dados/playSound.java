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
        isPlaying = true;

        new Thread(() -> {
            try {
                while (isPlaying) {
                    // Cria um novo FileInputStream e Player a cada loop
                    FileInputStream fileInputStream = new FileInputStream(nomeSom);
                    player = new Player(fileInputStream);
                    player.play();
                    fileInputStream.close(); // Fecha o stream após tocar o som
                }
            } catch (Exception e) {
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
}
