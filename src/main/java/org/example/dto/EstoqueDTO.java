package org.example.dto;

import org.example.Model.Categoria;
import org.example.Model.Marca;

import java.time.LocalDate;

public record EstoqueDTO(String produto, Categoria categoria, int quantidade, int caixa, LocalDate dataVencimento, Marca marca) {
}
