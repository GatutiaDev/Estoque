package org.example.validations;

import org.example.dto.EstoqueDTO;
import org.example.exception.ValidacaoException;

public class ValidationDataVencimento implements ValidationsServiceEstoque{
    @Override
    public void validar(EstoqueDTO dados) {
        if (dados.dataVencimento() == null) {
            throw new ValidacaoException("Informe a data de vencimento.");
        }
    }
}
