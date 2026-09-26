package org.example.Model;

import org.example.exception.ValidacaoException;

import java.time.LocalDate;

public class Estoque {

    private Long id;

    private String produto;

    private Categoria categoria;

    private int quantidade;

    private int caixa;

    private LocalDate dataVencimento;

    private Marca marca;

    public Estoque(Long id, String produto, Categoria categoria, int quantidade, int caixa, LocalDate dataVencimento, Marca marca) {
        this.id = id;
        this.produto = produto;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.caixa = caixa;
        this.dataVencimento = dataVencimento;
        this.marca = marca;
    }

    public Estoque(String produto, Categoria categoria, int quantidade, int caixa, LocalDate dataVencimento, Marca marca) {
        this.produto = produto;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.caixa = caixa;
        this.dataVencimento = dataVencimento;
        this.marca = marca;
    }

    public String getProduto() {
        return produto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Long getId() {
        return id;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getCaixa() {
        return caixa;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public void setCaixa(int caixa) {
        this.caixa = caixa;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public void adicionarQuantidade(){
        this.quantidade++;
    }

    public void removerQuantidade(){
        if(this.quantidade ==0){
            throw new ValidacaoException("A quantidade já está em 0.");
        }
        else {
            this.quantidade--;
        }
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }
}
