package com.motelinteligente.telas;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CacheDados.DadosVendidos;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class ObterProdutoFrame extends JFrame {

    private JTextField txtCodProduto;
    private JLabel lblDigitado;
    private JSpinner spinnerQuantidade;
    private DefaultTableModel modelo;
    private int numeroQuarto;

    public ObterProdutoFrame(DefaultTableModel modelo, int numeroQuarto) {
        this.modelo = modelo;
        this.numeroQuarto = numeroQuarto;

        setTitle("Inserir Produto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 150);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new GridLayout(3, 3, 10, 10)); // 3 colunas, espaçamento de 10px

        // Primeira linha
        JLabel lblCodProduto = new JLabel("Cód Produto:");
        lblCodProduto.setForeground(Color.WHITE);
        lblCodProduto.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtCodProduto = new JTextField(8);
        txtCodProduto.setFont(new Font("Arial", Font.PLAIN, 14));
        txtCodProduto.setMargin(new Insets(2, 2, 2, 2)); // Reduzir as margens internas
        txtCodProduto.setPreferredSize(new Dimension(txtCodProduto.getPreferredSize().width, 25)); // Definir a altura
        
        lblDigitado = new JLabel();
        lblDigitado.setForeground(Color.WHITE);
        lblDigitado.setFont(new Font("Arial", Font.PLAIN, 14));

        txtCodProduto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarDescricaoProduto();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarDescricaoProduto();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}

            private void atualizarDescricaoProduto() {
                String descricao = new fprodutos().getDescicao(txtCodProduto.getText());
                lblDigitado.setText(descricao);
            }
        });

        // Adicionando os componentes à tela
        add(lblCodProduto);
        add(txtCodProduto);
        add(lblDigitado);

        // Segunda linha
        JLabel lblQuantidade = new JLabel("Quantidade:");
        lblQuantidade.setForeground(Color.WHITE);
        lblQuantidade.setFont(new Font("Arial", Font.PLAIN, 14));
        
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spinnerQuantidade.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) spinnerQuantidade.getEditor();
        spinnerEditor.getTextField().setMargin(new Insets(2, 2, 2, 2)); // Reduzir as margens internas do campo de texto
        spinnerQuantidade.setPreferredSize(new Dimension(spinnerQuantidade.getPreferredSize().width, 25)); // Definir a altura
        
        add(lblQuantidade);
        add(spinnerQuantidade);

        // Preencher o espaço na segunda linha
        add(new JLabel()); // Espaço vazio para o layout

        // Terceira linha
        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.setFont(new Font("Arial", Font.PLAIN, 14));
        btnVoltar.addActionListener(e -> dispose());

        JButton btnInserir = new JButton("Inserir");
        btnInserir.setFont(new Font("Arial", Font.PLAIN, 14));
        btnInserir.addActionListener(e -> inserirProduto());

        add(btnVoltar);
        add(btnInserir);
        
        txtCodProduto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    spinnerQuantidade.requestFocus(); // Mover o foco para o campo de quantidade
                }
            }
        });
        spinnerQuantidade.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnInserir.requestFocus(); // Mover o foco para o botão "Inserir"
                }
            }
        });

        
        
        setLocationRelativeTo(null); // Centralizar a tela
        setVisible(true);
    }

    private void inserirProduto() {
        String idProdutoStr = txtCodProduto.getText();
        int quantidade = (Integer) spinnerQuantidade.getValue();

        if (isInteger(idProdutoStr)) {
            fprodutos produtodao = new fprodutos();
            String texto = produtodao.getDescicao(idProdutoStr);
            if (texto != null) {
                if (quantidade != 0) {
                    float valor = produtodao.getValorProduto(Integer.parseInt(idProdutoStr));
                    float valorSoma = valor * quantidade;
                    CacheDados cache = CacheDados.getInstancia();
                    int idLoca = cache.getCacheOcupado().get(numeroQuarto).getIdLoca();
                    if (idLoca == 0) {
                        DadosOcupados quartoOcupado = cache.getCacheOcupado().get(numeroQuarto);
                        int novoID = new fquartos().getIdLocacao(numeroQuarto);
                        quartoOcupado.setIdLoca(novoID);
                        cache.getCacheOcupado().put(numeroQuarto, quartoOcupado);

                        cache.carregaProdutosNegociadosCache(novoID);
                    }
                    modelo.addRow(new Object[]{
                        quantidade,
                        texto,
                        valor,
                        valorSoma
                    });
                    List<DadosVendidos> produtosVendidos = new ArrayList<>();
                    if (cache.cacheProdutosVendidos.containsKey(idLoca)) {
                        produtosVendidos = cache.cacheProdutosVendidos.get(idLoca);
                    }
                    produtosVendidos.add(new DadosVendidos(Integer.valueOf(idProdutoStr), Integer.valueOf(quantidade)));
                    cache.cacheProdutosVendidos.put(idLoca, produtosVendidos);

                    produtodao.inserirPrevendido(idLoca, Integer.valueOf(idProdutoStr), Integer.valueOf(quantidade));
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Quantidade inválida!");
                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Código inserido inválido!");
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Digite um valor válido!");
        }
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static void main(String args[]){
        DefaultTableModel modelo = null;
        new ObterProdutoFrame( modelo, 2);
    }
}
