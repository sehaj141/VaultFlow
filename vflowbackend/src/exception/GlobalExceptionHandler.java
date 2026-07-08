package com.vaultflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserExists(UserAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(RuntimeException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<Map<String, Object>> handleTokenRefresh(TokenRefreshException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }
    @ExceptionHandler(FolderNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleFolderNotFound(FolderNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
}

@ExceptionHandler(DuplicateFolderNameException.class)
public ResponseEntity<Map<String, Object>> handleDuplicateFolderName(DuplicateFolderNameException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
}

@ExceptionHandler(InvalidFolderOperationException.class)
public ResponseEntity<Map<String, Object>> handleInvalidFolderOperation(InvalidFolderOperationException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
}
@ExceptionHandler(FileNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleFileNotFound(FileNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
}

@ExceptionHandler(UnsupportedFileTypeException.class)
public ResponseEntity<Map<String, Object>> handleUnsupportedFileType(UnsupportedFileTypeException ex) {
    return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
}

@ExceptionHandler(FileSizeLimitExceededException.class)
public ResponseEntity<Map<String, Object>> handleFileSizeLimit(FileSizeLimitExceededException ex) {
    return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage());
}

@ExceptionHandler(StorageQuotaExceededException.class)
public ResponseEntity<Map<String, Object>> handleStorageQuota(StorageQuotaExceededException ex) {
    return buildResponse(HttpStatus.INSUFFICIENT_STORAGE, ex.getMessage());
}
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}