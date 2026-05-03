package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.valores;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class ResumoFechamentoDialog extends JDialog {

    private boolean confirmado = false;
    private final DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    // Dados
    private final valores vals; // Dados brutos do sistema (entradas hoje)
    private final float saldoInicial;

    // Vamos interpretar conforme a solicitação:
    // "se tem valor antecipado nesse caixa ou valor desse recebido em outro"
    // Divide em dois:
    // 1. Dados do Fechamento (Sistema - o que foi registrado de consumo/locação
    // finalizada)
    // 2. Extrato Físico (O que deve ter na gaveta)

    // As variáveis vindas do CaixaFrame:
    // antecipadoOutro: "Recebido Antecipado (Outro Caixa)" -> Entrou no sistema
    // como pago, mas o dinheiro tá lá no outro caixa.
    // antecipadoEste: "Neste Caixa (Antecipado)" -> O dinheiro tá aqui, mas talvez
    // a locação não finalizou ou é crédito.

    private final valores antecipadoDetalhado;
    private final float antecipadoOutro;
    private final float antecipadoEste;
    private final float totalJustificativas; // Descontos/Acréscimos aplicados

    public ResumoFechamentoDialog(Window parent, valores vals, float saldoInicial,
            float antecipadoOutro, valores antecipadoDetalhado, float totalJustificativas) {
        super(parent, "Conferência de Fechamento", ModalityType.APPLICATION_MODAL);
        this.vals = vals;
        this.saldoInicial = saldoInicial;
        this.antecipadoOutro = antecipadoOutro;
        this.antecipadoDetalhado = antecipadoDetalhado;
        this.antecipadoEste = antecipadoDetalhado.entradaD + antecipadoDetalhado.entradaC
                + antecipadoDetalhado.entradaP;
        this.totalJustificativas = totalJustificativas;

        initUI();
    }

    private void initUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);

        JPanel main = new JPanel(new MigLayout("fill, insets 20", "[grow][grow]", "[][grow][]"));
        main.setOpaque(false);

        // Header
        main.add(EstiloModerno.criarTitulo("Resumo de Fechamento"), "span 2, center, wrap");
        main.add(new JLabel("Confira os valores antes de confirmar o encerramento do caixa."),
                "span 2, center, wrap, gapbottom 20");

        // === COLUNA 1: DO SISTEMA (Registros de Vendas/Locações) ===
        JPanel pnlSistema = EstiloModerno.criarCard();
        pnlSistema.setLayout(new MigLayout("fillx, wrap 2", "[left]push[right]", "[]5[]5[]5[]10[]"));

        // Título Coluna 1
        JLabel lblSis = EstiloModerno.criarLabel("Valores Lançados (Sistema)");
        lblSis.putClientProperty("FlatLaf.style", "font: bold 14");
        pnlSistema.add(lblSis, "span 2, center, wrap, gapbottom 10");

        // Linhas
        float totalVendas = vals.entradaConsumo;
        float totalLocacoes = vals.entradaQuarto;
        // Total Sistema Bruto
        float totalSistema = totalVendas + totalLocacoes + totalJustificativas;

        addRow(pnlSistema, "Vendas (Produtos)", totalVendas, false);
        addRow(pnlSistema, "Locações", totalLocacoes, false);
        addRow(pnlSistema, "Ajustes/Justificativas", totalJustificativas, totalJustificativas < 0);

        pnlSistema.add(new JSeparator(), "span 2, growx, gaptop 5, gapbottom 5");
        addRow(pnlSistema, "Total Lançado (A)", totalSistema, true);

        main.add(pnlSistema, "grow, top");

        // === COLUNA 2: FÍSICO (O que deve ter na gaveta) ===
        JPanel pnlFisico = EstiloModerno.criarCard();
        pnlFisico.setLayout(new MigLayout("fillx, wrap 2", "[left]push[right]", "[]5[]5[]5[]5[]5[]5[]10[]"));

        // Título Coluna 2
        JLabel lblFis = EstiloModerno.criarLabel("Conferência de Caixa (Físico)");
        lblFis.putClientProperty("FlatLaf.style", "font: bold 14");
        pnlFisico.add(lblFis, "span 2, center, wrap, gapbottom 10");

        // Cálculo do Físico
        // Começa com o Saldo Inicial (Fundo de Troco)
        // (+) Total Lançado Sistema (A)
        // (-) O que foi pago em Cartão/Pix (pois não é dinheiro físico, mas está no
        // total sistema)
        // *Se a conferência for SÓ de dinheiro. O usuário pediu "extrato" geral,
        // mas "o quanto precisa ter no caixa dela pra fechar as contas".
        // Geralmente refere-se ao Dinheiro físico + Comprovantes.
        // Vamos listar tudo que soma/subtrai.

        // Lógica sugerida pelo usuário:
        // "somando no valor do cartao, pix ou dinheiro (ou subtraindo) o valor do
        // antecipado"

        // Vamos construir o "Deve Ter":
        // 1. Saldo Inicial (+)
        // 2. Dinheiro Recebido das Locações/Vendas (+)
        // -> Isso é (TotalSistema - Cartao - Pix) SE assumirmos que o resto é dinheiro.
        // -> Ou usar vals.entradaD diretamente se confiarmos na separação. Vamos usar
        // vals.entradaD.

        // E os antecipados?
        // - "Recebido Antecipado (Outro Caixa)": Foi pago lá, mas a locação finalizou
        // aqui.
        // Isso conta no Total do Sistema (Locação), mas NÃO entrou dinheiro AQUI.
        // Então tem que SUBTRAIR do montante esperado AQUI, se estivermos olhando pro
        // Total Sistema.
        // Mas se olharmos só pro vals.entradaD, isso já deve estar correto (não entrou
        // dinheiro D aqui).
        // Hmmm, o usuário disse: "somando... ou subtraindo o valor do antecipado".

        // Vamos fazer uma abordagem de "Reconstrução do Saldo Final Esperado":

        // Base: Saldo Inicial
        addRow(pnlFisico, "(+) Saldo Inicial", saldoInicial, false);

        // Entradas do Dia (Pelos métodos de pagamento registrados neste caixa)
        // Isso inclui antecipados RECEBIDOS NESTE CAIXA (que viram dinheiro/pix aqui).
        // Se `vals.entradaD` pega tudo que entrou de dinheiro neste caixa ID, então
        // `antecipadoEste` já está incluso nele?
        // Provavelmente sim, se a query do `vals` filtra por `idcaixaatual`.

        // Mas o `antecipadoOutro` (locação fechou aqui, mas pagou lá atrás em outro
        // caixa)
        // Esse valor aparece em `vals.entradaQuarto` deste caixa?
        // Se sim, ele infla o "Total Sistema", mas não gera dinheiro AQUI.

        // Vamos calcular o ESPERADO somando fechados - antecipados de outros caixas
        // Saldo Inicial + Recebidos (que já incluem antecipados em aberto) -
        // Antecipados de Outros Caixas
        // IMPORTANTE: vals já contém TODOS os recebimentos deste caixa (fechados +
        // antecipados em aberto)
        // antecipadoDetalhado é apenas um detalhamento informativo, não deve ser somado
        // novamente
        float esperadoDinheiro = saldoInicial + vals.entradaD - (antecipadoOutro > 0 ? antecipadoOutro : 0);
        float esperadoCartao = vals.entradaC;
        float esperadoPix = vals.entradaP;

        // Mostrando o DEVE TER
        addRow(pnlFisico, "(=) Deve ter em Dinheiro", esperadoDinheiro, true);
        addRow(pnlFisico, "(=) Deve ter em Cartão", esperadoCartao, true);
        addRow(pnlFisico, "(=) Deve ter em Pix", esperadoPix, true);

        pnlFisico.add(new JSeparator(), "span 2, growx, gaptop 10, gapbottom 5");

        // Detalhes da Soma (Explicativo)
        JLabel lblObs = new JLabel(
                "<html><center>Composição dos Totais<br>(Fechados + Antecipados Abertos)</center></html>");
        lblObs.setForeground(Color.GRAY);
        pnlFisico.add(lblObs, "span 2, center, wrap");

        addRow(pnlFisico, "Saldo Inicial (Fundo)", saldoInicial, false);
        addRow(pnlFisico, "Recebido (Fechados)", vals.entradaD + vals.entradaC + vals.entradaP, false);
        // Mostra o valor de antecipados em aberto apenas como informação (já está
        // incluído no "Recebido")
        if (antecipadoEste > 0) {
            JLabel lblInfo = new JLabel(
                    "    • Deste total, R$ " + df.format(antecipadoEste) + " são antecipados em aberto");
            lblInfo.setForeground(new Color(107, 114, 128)); // Cinza
            lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            pnlFisico.add(lblInfo, "span 2, left, wrap");
        }

        // Exibe o que não impacta o físico
        if (antecipadoOutro > 0) {
            addRow(pnlFisico, "Pago em Outro Caixa (Info)", antecipadoOutro, false);
        }

        pnlFisico.add(new JSeparator(), "span 2, growx, gaptop 5, gapbottom 5");

        // Saldo Final Geral
        float saldoFinal = esperadoDinheiro + esperadoCartao + esperadoPix;
        addRow(pnlFisico, "TOTAL GERAL ESPERADO", saldoFinal, true);

        main.add(pnlFisico, "grow, top, wrap");

        // Footer Buttons
        JPanel footer = new JPanel(new MigLayout("insets 20 0 0 0, fillx, center", "[150!][200!]"));
        footer.setOpaque(false);

        JButton btnCancelar = EstiloModerno.criarBotaoSecundario("Voltar", null);
        btnCancelar.addActionListener(e -> dispose());

        JButton btnConfirmar = EstiloModerno.criarBotaoPrincipal("Confirmar Fechamento", null);
        btnConfirmar.setBackground(EstiloModerno.SUCCESS);
        btnConfirmar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });

        footer.add(btnCancelar);
        footer.add(btnConfirmar);

        main.add(footer, "span 2, center");

        add(main);
    }

    private void addRow(JPanel p, String label, float valor, boolean destaqued) {
        JLabel l = new JLabel(label);
        JLabel v = new JLabel(df.format(valor));

        if (destaqued) {
            String style = "font: bold 14";
            if (label.contains("Total Lançado"))
                style += "; foreground: #374151";
            if (label.contains("SALDO FINAL"))
                style += "; foreground: #16A34A"; // Verde

            l.putClientProperty("FlatLaf.style", style);
            v.putClientProperty("FlatLaf.style", style);
        }

        p.add(l);
        p.add(v, "right"); // Alinha valor à direita
    }

    public boolean isConfirmado() {
        return confirmado;
    }
}
