package com.vaultflow.exception;

public class DuplicateFolderNameException extends RuntimeException {
    public DuplicateFolderNameException(String message) {
        super(message);
    }
}