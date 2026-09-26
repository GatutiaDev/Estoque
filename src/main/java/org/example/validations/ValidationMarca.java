package org.example.validations;

import org.example.dto.EstoqueDTO;
import org.example.exception.ValidacaoException;

public class ValidationMarca implements ValidationsServiceEstoque{
    @Override
    public void validar(EstoqueDTO dados) {
        if (dados.marca() == null) {
            throw new ValidacaoException("Selecione uma marca.");
        }
    }
}
