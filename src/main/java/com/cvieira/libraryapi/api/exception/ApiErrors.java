package com.cvieira.libraryapi.api.exception;

import com.cvieira.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult) {

        errors = new ArrayList<>();

        for(ObjectError error : bindingResult.getAllErrors()) {
            errors.add(error.getDefaultMessage());
        }
    }

    public ApiErrors(BusinessException businessException) {

        errors = Arrays.asList(businessException.getMessage());
    }

    public ApiErrors(ResponseStatusException ex) {

        errors = Arrays.asList(ex.getReason());
    }

    public List<String> getErrors() {
        return errors;
    }
}
