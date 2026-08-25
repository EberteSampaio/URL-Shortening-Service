package br.com.deveberte.urlshortening.config.exceptionhandler;

import br.com.deveberte.urlshortening.exception.DomainException;
import br.com.deveberte.urlshortening.exception.UrlNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalHandlerException extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalHandlerException.class);

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex, HttpServletRequest request){
        log.warn("Regra de negócio violada em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        return this.build(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleUrlNotFound(UrlNotFoundException ex, HttpServletRequest request){
        log.warn("Recurso não encontrado em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        return this.build(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> handleUnexpectedExceptions(Exception ex, HttpServletRequest request){
        String errorId = UUID.randomUUID().toString();

        log.error("errorId={} falha inesperada em {} {}", errorId, request.getMethod(), request.getRequestURI(), ex);

        return this.build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor",
                "Ocorreu um erro inesperado. Informe o código " + errorId + " ao suporte.",
                request.getRequestURI(), null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request){
        List<FieldWithError> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new FieldWithError(erro.getField(), erro.getDefaultMessage()))
                .toList();

        ApiError body = new ApiError(status.value(), "Erro de validação",
                "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.",
                this.path(request), campos);

        return this.handleExceptionInternal(ex, body, headers, status, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode status,
                                                             WebRequest request){
        String path = this.path(request);

        if (body == null || body instanceof ProblemDetail) {
            body = new ApiError(status.value(), this.reasonPhrase(status), this.detail(ex, body), path, null);
        }

        if (status.is5xxServerError()) {
            log.error("Falha ao processar requisição ({}) em {}", status.value(), path, ex);
        } else {
            log.warn("Requisição rejeitada ({}) em {}: {}", status.value(), path, ex.getMessage());
        }

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    private String detail(Exception ex, Object body){
        if (body instanceof ProblemDetail problemDetail && problemDetail.getDetail() != null) {
            return problemDetail.getDetail();
        }

        if (ex instanceof ErrorResponse errorResponse && errorResponse.getBody().getDetail() != null) {
            return errorResponse.getBody().getDetail();
        }

        return "Não foi possível processar a requisição.";
    }

    private String reasonPhrase(HttpStatusCode status){
        return status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "Erro";
    }

    private String path(WebRequest request){
        return request instanceof ServletWebRequest servletWebRequest
                ? servletWebRequest.getRequest().getRequestURI()
                : request.getDescription(false);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message, String path, List<FieldWithError> fields){
        return ResponseEntity.status(status).body(new ApiError(status.value(), error, message, path, fields));
    }
}
