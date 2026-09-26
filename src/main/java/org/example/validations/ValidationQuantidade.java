package org.example.validations;

import org.example.dto.EstoqueDTO;
import org.example.exception.ValidacaoException;

public class ValidationQuantidade implements ValidationsServiceEstoque{

    @Override
    public void validar(EstoqueDTO dados) {
        if (dados.quantidade() < 0) {
            throw new ValidacaoException("A quantidade não pode ser negativa.");
        }
    }
}
