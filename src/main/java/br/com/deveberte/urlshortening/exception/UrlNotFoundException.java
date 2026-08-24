package br.com.deveberte.urlshortening.exception;

public class UrlNotFoundException extends DomainException {
    public UrlNotFoundException(String message) {
        super(message);
    }
}
