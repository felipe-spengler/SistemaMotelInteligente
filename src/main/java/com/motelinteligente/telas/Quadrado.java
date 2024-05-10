package com.motelinteligente.telas;


import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/**
 *
 * @author MOTEL
 */
public class Quadrado extends javax.swing.JPanel {
    private QuartoClickListener listener;
    
    public interface QuartoClickListener {
      void quartoClicado(int nomeDoQuarto);
      void clicDireito(int nomeDoQuarto);

    }

    public Quadrado(int numero, String tipo, String data, Color minhacor) {
        initComponents();
        p2.setOpaque(false);
        p2.repaint();
        numeroQuarto.setText(String.valueOf(numero));
        tipoQuarto.setText(tipo);
        manut.setText(data);
        painelTituloQuadrado.setBackground(minhacor);
    
    addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) { // Verifica se foi um clique direito
                    if (listener != null) {
                        listener.clicDireito(numero);
                    }
                } else {
                    if (listener != null) {
                        listener.quartoClicado(numero);
                    }
                }
            }
        });
    }

    public void setQuartoClickListener(QuartoClickListener listener) {
        this.listener = listener;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        p2 = new javax.swing.JPanel();
        painelTituloQuadrado = new javax.swing.JPanel();
        tipoQuarto = new javax.swing.JLabel();
        numeroQuarto = new javax.swing.JLabel();
        manut = new javax.swing.JLabel();

        setBackground(new java.awt.Color(0, 0, 0));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setPreferredSize(new java.awt.Dimension(224, 150));

        p2.setBackground(new java.awt.Color(255, 255, 255));
        p2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p2.setFocusable(false);
        p2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        p2.setPreferredSize(new java.awt.Dimension(100, 100));

        painelTituloQuadrado.setBackground(new java.awt.Color(204, 204, 204));

        tipoQuarto.setBackground(new java.awt.Color(204, 204, 204));
        tipoQuarto.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        tipoQuarto.setForeground(new java.awt.Color(255, 255, 255));
        tipoQuarto.setText("jLabel2");

        numeroQuarto.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        numeroQuarto.setForeground(new java.awt.Color(255, 255, 255));
        numeroQuarto.setText("num");

        javax.swing.GroupLayout painelTituloQuadradoLayout = new javax.swing.GroupLayout(painelTituloQuadrado);
        painelTituloQuadrado.setLayout(painelTituloQuadradoLayout);
        painelTituloQuadradoLayout.setHorizontalGroup(
            painelTituloQuadradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelTituloQuadradoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(numeroQuarto, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tipoQuarto, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );
        painelTituloQuadradoLayout.setVerticalGroup(
            painelTituloQuadradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelTituloQuadradoLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(painelTituloQuadradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numeroQuarto, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tipoQuarto))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        manut.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        manut.setText("jLabel3");
        manut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout p2Layout = new javax.swing.GroupLayout(p2);
        p2.setLayout(p2Layout);
        p2Layout.setHorizontalGroup(
            p2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelTituloQuadrado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(p2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(manut, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        p2Layout.setVerticalGroup(
            p2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(p2Layout.createSequentialGroup()
                .addComponent(painelTituloQuadrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(manut, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(p2, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(p2, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel manut;
    private javax.swing.JLabel numeroQuarto;
    private javax.swing.JPanel p2;
    private javax.swing.JPanel painelTituloQuadrado;
    private javax.swing.JLabel tipoQuarto;
    // End of variables declaration//GEN-END:variables
}
