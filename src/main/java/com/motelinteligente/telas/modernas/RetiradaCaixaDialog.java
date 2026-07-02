package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fcaixa;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class RetiradaCaixaDialog extends JDialog {

    private boolean confirmado = false;

    public RetiradaCaixaDialog(Window parent) {
        super(parent, "Retirada do Caixa", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(430, 300);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);

        JPanel main = new JPanel(new MigLayout("fill, insets 20", "[right][grow]", "[]12[]12[]12[]20[]"));
        main.setOpaque(false);

        main.add(EstiloModerno.criarTitulo("Retirada do Caixa"), "span 2, center, wrap, gapbottom 10");

        main.add(EstiloModerno.criarLabel("Valor (R$):"));
        JTextField txtValor = EstiloModerno.criarInput();
        txtValor.setText("0.00");
        main.add(txtValor, "growx, wrap");

        main.add(EstiloModerno.criarLabel("Quem retirou:"));
        JTextField txtQuem = EstiloModerno.criarInput();
        txtQuem.setText(configGlobal.getInstance().getUsuario());
        main.add(txtQuem, "growx, wrap");

        main.add(EstiloModerno.criarLabel("Justificativa:"));
        JTextField txtJustificativa = EstiloModerno.criarInput();
        main.add(txtJustificativa, "growx, wrap");

        JPanel footer = new JPanel(new MigLayout("insets 0, fillx, center", "[150!][150!]"));
        footer.setOpaque(false);

        JButton btnCancelar = EstiloModerno.criarBotaoSecundario("Cancelar", null);
        btnCancelar.addActionListener(e -> dispose());

        JButton btnConfirmar = EstiloModerno.criarBotaoPrincipal("Confirmar Retirada", null);
        btnConfirmar.setBackground(EstiloModerno.DANGER);
        btnConfirmar.addActionListener(e -> {
            try {
                float valor = Float.parseFloat(txtValor.getText().replace(",", "."));
                String quem = txtQuem.getText().trim();
                String just = txtJustificativa.getText().trim();
                if (valor <= 0) { JOptionPane.showMessageDialog(this, "Valor deve ser maior que zero."); return; }
                if (quem.isEmpty()) { JOptionPane.showMessageDialog(this, "Informe quem esta retirando."); return; }
                if (just.isEmpty()) { JOptionPane.showMessageDialog(this, "Informe a justificativa."); return; }
                int idCaixa = configGlobal.getInstance().getCaixa();
                if (idCaixa == 0) { JOptionPane.showMessageDialog(this, "Nenhum caixa aberto!"); return; }
                boolean ok = new fcaixa().salvarRetirada(idCaixa, valor, quem, just);
                if (ok) {
                    JOptionPane.showMessageDialog(this, String.format("Retirada de R$ %.2f registrada!", valor), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    confirmado = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar retirada.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor invalido. Use ponto como separador decimal.");
            }
        });

        footer.add(btnCancelar);
        footer.add(btnConfirmar);
        main.add(footer, "span 2, center");
        add(main);
    }

    public boolean isConfirmado() { return confirmado; }
}
