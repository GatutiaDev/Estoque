package org.example.validations;

import java.util.List;

// Lista oficial de validações do estoque: ao criar um validador novo, registre-o aqui.
// A ordem da lista é a ordem em que as mensagens de erro aparecem.
public final class ValidacoesEstoque {

    private ValidacoesEstoque() {
    }

    public static List<ValidationsServiceEstoque> todas() {
        return List.of(
                new ValidationNomeProduto(),
                new ValidationCategoria(),
                new ValidationMarca(),
                new ValidationQuantidade(),
                new ValidationCaixa(),
                new ValidationDataVencimento()
        );
    }
}
