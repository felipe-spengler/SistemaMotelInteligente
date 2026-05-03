package com.motelinteligente.telas.modernas;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JanelaGaleria extends JDialog {

    private String urlSelecionada = null;
    private JPanel pnlFotos;
    private final String API_URL = "https://motelinteligente.com/api/listar_fotos.php";
    private final String BASE_URL = "https://motelinteligente.com/";
    private final String API_KEY = "MotelInteligente_Secret_Key_2024";

    public JanelaGaleria(Window parent) {
        super(parent, "Biblioteca de Imagens", ModalityType.APPLICATION_MODAL);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        EstiloModerno.aplicarEstiloDialog(this);
        
        initUI();
        carregarFotos();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(EstiloModerno.BG_BACKGROUND);

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel lblTitulo = EstiloModerno.criarTitulo("Biblioteca Compartilhada");
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JButton btnNovo = EstiloModerno.criarBotaoPrincipal(" + Enviar Nova Foto ", null);
        btnNovo.addActionListener(e -> fazerNovoUpload());
        pnlHeader.add(btnNovo, BorderLayout.EAST);
        
        add(pnlHeader, BorderLayout.NORTH);

        // Grid de Fotos
        pnlFotos = new JPanel(new GridLayout(0, 4, 15, 15));
        pnlFotos.setOpaque(false);
        pnlFotos.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(pnlFotos);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scroll, BorderLayout.CENTER);
    }

    private void carregarFotos() {
        pnlFotos.removeAll();
        JLabel lblCarregando = new JLabel("Carregando galeria...", SwingConstants.CENTER);
        lblCarregando.setForeground(Color.WHITE);
        pnlFotos.add(lblCarregando);
        
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("X-Api-Key", API_KEY);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) res.append(line);
                in.close();

                JSONObject json = new JSONObject(res.toString());
                if (json.getBoolean("success")) {
                    JSONArray array = json.getJSONArray("fotos");
                    
                    SwingUtilities.invokeLater(() -> {
                        pnlFotos.removeAll();
                        pnlFotos.setLayout(new GridLayout(0, 4, 15, 15));
                    });

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject foto = array.getJSONObject(i);
                        String path = foto.getString("url");
                        String nome = foto.getString("nome");
                        
                        SwingUtilities.invokeLater(() -> {
                            JButton btnFoto = criarCardFoto(nome, path);
                            pnlFotos.add(btnFoto);
                            pnlFotos.revalidate();
                        });
                    }
                    
                    if (array.length() == 0) {
                        SwingUtilities.invokeLater(() -> {
                            pnlFotos.setLayout(new BorderLayout());
                            pnlFotos.add(new JLabel("Nenhuma foto encontrada. Clique em 'Enviar Nova Foto'!", SwingConstants.CENTER));
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JButton criarCardFoto(String nome, String path) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(new Color(30, 41, 59));
        btn.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Label para a imagem (Placeholder enquanto carrega)
        JLabel lblImg = new JLabel("Carregando...", SwingConstants.CENTER);
        lblImg.setForeground(new Color(148, 163, 184));
        btn.add(lblImg, BorderLayout.CENTER);
        
        // Label para o nome
        JLabel lblNome = new JLabel(nome, SwingConstants.CENTER);
        lblNome.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblNome.setForeground(new Color(148, 163, 184));
        lblNome.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btn.add(lblNome, BorderLayout.SOUTH);

        // Carrega miniatura em background
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + path);
                BufferedImage original = ImageIO.read(url);
                if (original != null) {
                    Image escalada = original.getScaledInstance(150, 120, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> {
                        lblImg.setIcon(new ImageIcon(escalada));
                        lblImg.setText("");
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> lblImg.setText("Erro ao carregar"));
            }
        }).start();

        btn.addActionListener(e -> {
            this.urlSelecionada = path;
            dispose();
        });

        return btn;
    }

    private void fazerNovoUpload() {
        // Aproveita a lógica de upload da tela pai ou abre um seletor aqui
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", "jpg", "jpeg", "png", "webp", "jfif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new Thread(() -> {
                try {
                    // Usa o CadastraProdutoModerno para acessar o método estático se necessário, 
                    // ou implementamos um upload rápido aqui.
                    CadastraProdutoModerno pai = (CadastraProdutoModerno) getParent();
                    String res = pai.executarUploadDireto(fc.getSelectedFile());
                    
                    JSONObject json = new JSONObject(res);
                    if (json.optBoolean("success")) {
                        SwingUtilities.invokeLater(() -> {
                            setCursor(Cursor.getDefaultCursor());
                            carregarFotos(); // Recarrega galeria
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            setCursor(Cursor.getDefaultCursor());
                            JOptionPane.showMessageDialog(this, "Erro: " + json.optString("message"));
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> setCursor(Cursor.getDefaultCursor()));
                }
            }).start();
        }
    }

    public String getUrlSelecionada() {
        return urlSelecionada;
    }
}
