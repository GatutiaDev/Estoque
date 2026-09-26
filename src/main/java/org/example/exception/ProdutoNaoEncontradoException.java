package org.example.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {
    public ProdutoNaoEncontradoException(String texto){
        super(texto);
    }
}
