package br.com.toolschallenge.handler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.toolschallenge.exception.DuplicateTransactionIdException;
import br.com.toolschallenge.exception.PaymentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PATH = "path";
	private static final String MESSAGE = "message";
	private static final String TIMESTAMP = "timestamp";

	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<Map<String, String>> fieldErrors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            Map<String, String> fe = new HashMap<>();
            fe.put("field", error.getField());
            fe.put(MESSAGE, error.getDefaultMessage());
            fieldErrors.add(fe);
        });

        Map<String, Object> body = createBaseBody(
                "Validation failed for one or more fields.",
                request
        );
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        List<Map<String, String>> fieldErrors = new ArrayList<>();

        ex.getConstraintViolations().forEach(violation -> {
            Map<String, String> fe = new HashMap<>();
            fe.put("field", violation.getPropertyPath().toString());
            fe.put(MESSAGE, violation.getMessage());
            fieldErrors.add(fe);
        });

        Map<String, Object> body = createBaseBody(
                "Constraint violation for one or more parameters.",
                request
        );
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        Throwable root = ex.getMostSpecificCause();
        String message = "Error reading request body.";

        if (root != null && root.getMessage() != null) {
            String rootMsg = root.getMessage();

            if (rootMsg.contains("Invalid payment method type")) {
                message = rootMsg
                        + " Allowed values: AVISTA, PARCELADO LOJA, PARCELADO EMISSOR.";
            } else {
                message = rootMsg;
            }
        }

        Map<String, Object> body = createBaseBody(
                message,
                request
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Required request parameter '%s' of type %s is missing",
                ex.getParameterName(),
                ex.getParameterType()
        );

        Map<String, Object> body = createBaseBody(
                message,
                request
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String message = String.format(
                "Parameter '%s' has invalid value '%s'. Expected type: %s",
                ex.getName(),
                ex.getValue(),
                requiredType
        );

        Map<String, Object> body = createBaseBody(
                message,
                request
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DuplicateTransactionIdException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateTransactionId(
            DuplicateTransactionIdException ex,
            HttpServletRequest request) {

        Map<String, Object> body = Map.of(
                TIMESTAMP, Instant.now().toString(),
                MESSAGE, ex.getMessage(),
                PATH, request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> body = Map.of(
                TIMESTAMP, Instant.now().toString(),
                MESSAGE, ex.getMessage(),
                PATH, request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUncaughtException(
            Exception ex,
            HttpServletRequest request) {

        Map<String, Object> body = createBaseBody(
                "An unexpected error occurred. Please contact support if the problem persists.",
                request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    
    private Map<String, Object> createBaseBody(
            String message,
            HttpServletRequest request
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put(TIMESTAMP, Instant.now().toString());
        body.put(MESSAGE, message);
        body.put(PATH, request.getRequestURI());
        return body;
    }
}
