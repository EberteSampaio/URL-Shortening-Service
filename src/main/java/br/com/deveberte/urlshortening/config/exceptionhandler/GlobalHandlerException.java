package br.com.deveberte.urlshortening.config.exceptionhandler;

import br.com.deveberte.urlshortening.exception.DomainException;
import br.com.deveberte.urlshortening.exception.UrlNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex, HttpServletRequest request){
        var badRequestStatus = HttpStatus.BAD_REQUEST;
        var error = new ApiError(
                badRequestStatus.value(),
                "Dados inválidos",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(badRequestStatus).body(error);
    }
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ApiError> handleUrlNotFound(UrlNotFoundException ex, HttpServletRequest request){
        var notFoundStatus = HttpStatus.NOT_FOUND;
        var error = new ApiError(
                notFoundStatus.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(notFoundStatus).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedExceptions(Exception ex, HttpServletRequest request){

        HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
        var error = new ApiError(
                internalServerError.value(),
                "Erro interno no servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(internalServerError).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request){
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        List<FieldWithError> campos = fieldErrors.stream()
                .map(erro -> new FieldWithError(erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.toList());

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        var erro = new ApiError(
                badRequest.value(),
                "Erro de Validação",
                "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.",
                request.getRequestURI(),
                campos
        );

        return ResponseEntity.status(badRequest).body(erro);
    }
}
