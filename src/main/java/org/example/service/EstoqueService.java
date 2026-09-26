package org.example.service;

import org.example.Model.Estoque;
import org.example.dto.EstoqueDTO;
import org.example.exception.ProdutoNaoEncontradoException;
import org.example.repository.EstoqueRepository;
import org.example.validations.ValidationsServiceEstoque;

import java.util.List;

public class EstoqueService {

    private final EstoqueRepository repository;

    private final List<ValidationsServiceEstoque> validations;

    public EstoqueService(EstoqueRepository repository, List<ValidationsServiceEstoque> validations) {
        this.repository = repository;
        this.validations = validations;
    }


    public void cadastrarEstoque(EstoqueDTO dados) {

        validations.forEach(v -> v.validar(dados));

        Estoque novo = new Estoque(dados.produto().trim(), dados.categoria(), dados.quantidade(), dados.caixa(), dados.dataVencimento(), dados.marca());

        repository.salvar(novo);
    }

    public void atualizarEstoque(Long id, EstoqueDTO dados) {

        validations.forEach(v -> v.validar(dados));

        Estoque item = buscarOuFalhar(id);
        item.setProduto(dados.produto().trim());
        item.setCategoria(dados.categoria());
        item.setQuantidade(dados.quantidade());
        item.setCaixa(dados.caixa());
        item.setDataVencimento(dados.dataVencimento());
        item.setMarca(dados.marca());

        repository.salvar(item);
    }

    public List<Estoque> listarEstoque() {

        return repository.listarTodas();

    }

    public void deletarEstoque(Long id) {
        repository.deletar(id);
    }

    public void aumentarQuantidade(Long id) {
        Estoque item = buscarOuFalhar(id);
        item.adicionarQuantidade();
        repository.salvar(item);
    }

    public void diminuirQuantidade(Long id) {
        Estoque item = buscarOuFalhar(id);
        item.removerQuantidade();
        repository.salvar(item);
    }

    private Estoque buscarOuFalhar(Long id) {
        return repository.buscaPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Não foi possível encontrar o produto."));
    }

}
