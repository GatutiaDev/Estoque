package org.example.validations;

import org.example.dto.EstoqueDTO;
import org.example.exception.ValidacaoException;

public class ValidationNomeProduto implements ValidationsServiceEstoque{

    @Override
    public void validar(EstoqueDTO dados){
        if (dados.produto() == null || dados.produto().isBlank()) {
            throw new ValidacaoException("Informe o nome do produto.");
        }
    }


}

