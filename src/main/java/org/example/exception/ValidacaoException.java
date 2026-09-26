package org.example.exception;

public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String texto){
        super(texto);
    }
}
