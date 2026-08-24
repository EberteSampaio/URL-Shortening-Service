package br.com.deveberte.urlshortening.config.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

public class ApiError{
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FieldWithError> fields;

    public ApiError( Integer status, String error, String message, String path, List<FieldWithError> fields) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fields = fields;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<FieldWithError> getFields() {
        return fields;
    }
}
