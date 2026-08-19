package com.teamflow.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapResourceNotFoundTo404() {
        ProblemDetail pd = handler.handleNotFound(new ResourceNotFoundException("Project", UUID.randomUUID()));
        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
        assertNotNull(pd.getProperties().get("timestamp"));
    }

    @Test
    void shouldMapConflictTo409() {
        ProblemDetail pd = handler.handleConflict(new ConflictException("Username already taken"));
        assertEquals(HttpStatus.CONFLICT.value(), pd.getStatus());
        assertEquals("Username already taken", pd.getDetail());
    }

    @Test
    void shouldMapAppAccessDeniedTo403() {
        ProblemDetail pd = handler.handleForbidden(new AccessDeniedException("Not a member"));
        assertEquals(HttpStatus.FORBIDDEN.value(), pd.getStatus());
    }

    @Test
    void shouldMapSpringAccessDeniedTo403() {
        ProblemDetail pd = handler.handleSpringForbidden(
            new org.springframework.security.access.AccessDeniedException("denied"));
        assertEquals(HttpStatus.FORBIDDEN.value(), pd.getStatus());
        assertEquals("Access denied", pd.getDetail());
    }

    @Test
    void shouldMapBadCredentialsTo401() {
        ProblemDetail pd = handler.handleBadCredentials(new BadCredentialsException("nope"));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), pd.getStatus());
    }

    @Test
    void shouldMapValidationErrorsTo400WithFieldMap() throws NoSuchMethodException {
        Method method = Object.class.getMethod("toString");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors())
            .thenReturn(List.of(new FieldError("request", "username", "must not be blank")));

        var ex = new MethodArgumentNotValidException(new MethodParameter(method, -1), bindingResult);

        ProblemDetail pd = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        @SuppressWarnings("unchecked")
        var errors = (java.util.Map<String, String>) pd.getProperties().get("errors");
        assertEquals("must not be blank", errors.get("username"));
    }

    @Test
    void shouldMapUnexpectedExceptionTo500() {
        ProblemDetail pd = handler.handleGeneral(new IllegalStateException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertEquals("An unexpected error occurred", pd.getDetail());
    }
}
