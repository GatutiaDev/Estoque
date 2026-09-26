package org.example.validations;

import org.example.dto.EstoqueDTO;
import org.example.exception.ValidacaoException;

public class ValidationCategoria implements ValidationsServiceEstoque{

    @Override
    public void validar(EstoqueDTO dados) {
        if (dados.categoria() == null) {
            throw new ValidacaoException("Selecione uma categoria.");
        }
    }
}
