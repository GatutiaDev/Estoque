package org.example.repository;

import org.example.Model.Estoque;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository {

    void salvar(Estoque estoque);

    List<Estoque> listarTodas();

    void deletar(Long id);

    Optional<Estoque> buscaPorId(Long id);

}
