package org.example.viewmodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.Model.Categoria;
import org.example.Model.Estoque;
import org.example.Model.Marca;
import org.example.dto.EstoqueDTO;
import org.example.service.EstoqueService;

import java.time.LocalDate;

public class EstoqueViewModel {

    private final EstoqueService service;
    private final ObservableList<Estoque> itens = FXCollections.observableArrayList();

    public EstoqueViewModel(EstoqueService service) {
        this.service = service;
        carregarEstoque();
    }

    public ObservableList<Estoque> getItens() {
        return itens;
    }

    public void carregarEstoque() {
        itens.setAll(service.listarEstoque());
    }

    public void cadastrarEstoque(EstoqueDTO dados) {
        service.cadastrarEstoque(dados);
        carregarEstoque();
    }

    public void atualizarEstoque(Long id, EstoqueDTO dados) {
        service.atualizarEstoque(id, dados);
        carregarEstoque();
    }

    public void aumentarQuantidade(Long id) {
        service.aumentarQuantidade(id);
        carregarEstoque();
    }

    public void diminuirQuantidade(Long id) {
        service.diminuirQuantidade(id);
        carregarEstoque();
    }

    public void deletarEstoque(Long id) {
        service.deletarEstoque(id);
        carregarEstoque();
    }
}
