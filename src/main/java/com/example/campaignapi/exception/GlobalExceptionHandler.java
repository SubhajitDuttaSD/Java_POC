package com.example.campaignapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.campaignapi.dto.ValidationErrorResponse;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CampaignNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleCampaignNotFound(
            CampaignNotFoundException exception) {

        return Map.of(
                "status", 404,
                "message", exception.getMessage());
    }

    @ExceptionHandler(DuplicateCampaignNameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDuplicateCampaignName(
            DuplicateCampaignNameException exception) {

        return Map.of(
                "status", 409,
                "message", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage()));

        return new ValidationErrorResponse(
                400,
                "Validation failed",
                errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMalformedRequest(
            HttpMessageNotReadableException exception) {

        return Map.of(
                "status", 400,
                "message", "Request body is missing or is not valid JSON");
    }
}