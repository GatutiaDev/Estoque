package org.example.view;

import org.example.Model.Categoria;
import org.example.Model.Estoque;
import org.example.Model.Marca;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Uma linha da tabela em árvore: ou um lote, ou um produto que resume vários lotes com o mesmo nome e marca
public class LinhaEstoque {

    private final String chaveProduto;
    private final Estoque lote;
    private final List<Estoque> lotes;
    private final boolean dentroDeProduto;

    private LinhaEstoque(String chaveProduto, Estoque lote, List<Estoque> lotes, boolean dentroDeProduto) {
        this.chaveProduto = chaveProduto;
        this.lote = lote;
        this.lotes = lotes;
        this.dentroDeProduto = dentroDeProduto;
    }

    // Lote que aparece sozinho, porque é o único lote do produto
    public static LinhaEstoque loteUnico(String chaveProduto, Estoque lote) {
        return new LinhaEstoque(chaveProduto, lote, List.of(lote), false);
    }

    // Lote que aparece dentro de um produto agrupado
    public static LinhaEstoque loteDoProduto(String chaveProduto, Estoque lote) {
        return new LinhaEstoque(chaveProduto, lote, List.of(lote), true);
    }

    public static LinhaEstoque produto(String chaveProduto, List<Estoque> lotes) {
        return new LinhaEstoque(chaveProduto, null, List.copyOf(lotes), false);
    }

    public boolean isProduto() {
        return lote == null;
    }

    public boolean isDentroDeProduto() {
        return dentroDeProduto;
    }

    public String getChaveProduto() {
        return chaveProduto;
    }

    public Estoque getLote() {
        return lote;
    }

    public List<Estoque> getLotes() {
        return lotes;
    }

    public String getProduto() {
        return principal().getProduto();
    }

    public Categoria getCategoria() {
        return principal().getCategoria();
    }

    public Marca getMarca() {
        return principal().getMarca();
    }

    // Os lotes podem ter o nome digitado de formas diferentes ("Kaiak" e "kaiak "):
    // o produto usa o nome, a categoria e a marca do lote cadastrado primeiro
    private Estoque principal() {
        return lotes.stream().min(Comparator.comparing(Estoque::getId)).orElseThrow();
    }

    public int getQuantidade() {
        return lotes.stream().mapToInt(Estoque::getQuantidade).sum();
    }

    public String getCaixas() {
        return lotes.stream()
                .map(Estoque::getCaixa)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    // No produto, é o vencimento mais próximo entre os lotes que ainda têm unidades
    // (se todos estiverem zerados, considera todos). Assim a cor do produto é a do pior lote.
    public LocalDate getVencimento() {
        if (!isProduto()) {
            return lote.getDataVencimento();
        }
        List<Estoque> comUnidades = lotes.stream().filter(l -> l.getQuantidade() > 0).toList();
        List<Estoque> considerados = comUnidades.isEmpty() ? lotes : comUnidades;
        return considerados.stream()
                .map(Estoque::getDataVencimento)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    // FEFO (first expired, first out): o lote com unidades que vence primeiro
    public Estoque loteQueVencePrimeiro() {
        return lotes.stream()
                .filter(l -> l.getQuantidade() > 0)
                .min(Comparator.comparing(Estoque::getDataVencimento).thenComparing(Estoque::getId))
                .orElse(null);
    }
}
