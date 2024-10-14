/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
            JOptionPane.showMessageDialog(null, "Erro ao executar som!");
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
