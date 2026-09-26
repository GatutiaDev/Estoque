package org.example.validations;

import org.example.dto.EstoqueDTO;
import org.example.exception.ValidacaoException;

public class ValidationCaixa implements ValidationsServiceEstoque{
    @Override
    public void validar(EstoqueDTO dados) {
        if (dados.caixa() < 0) {
            throw new ValidacaoException("A caixa não pode ser negativa.");
        }
    }
}
