package com.example.motiday_api.exception;

public class RoutineNotFoundException extends RuntimeException {
    public RoutineNotFoundException(String message) {
        super(message);
    }
}