package br.com.toolschallenge.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.toolschallenge.exception.DuplicateTransactionIdException;
import br.com.toolschallenge.exception.InvalidInstallmentsForPaymentTypeException;
import br.com.toolschallenge.exception.PaymentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    // keys
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_PATH = "path";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_FIELD_ERRORS = "fieldErrors";
    private static final String KEY_FIELD = "field";

    private static final String REQUEST_URI = "/pagamentos";
    private static final String OBJECT_NAME_PAYMENT = "payment";
    private static final String FIELD_CARD_NUMBER = "cardNumber";
    private static final String FIELD_AMOUNT = "amount";
    private static final String PARAM_ID = "id";
    private static final String TYPE_STRING = "String";
    private static final String VALUE_ABC = "abc";
    private static final String TRANSACTION_ID = "1000000000001";
    private static final String ROOT_BAD_BODY = "bad_body";
    private static final String ROOT_INVALID_PAYMENT_METHOD_TYPE =
            "Invalid payment method type: XYZ";
    private static final String ROOT_EXCEPTION_MESSAGE = "boom";

    private static final String MSG_MUST_NOT_BE_BLANK = "must not be blank";
    private static final String MSG_VALIDATION_FAILED =
            "Validation failed for one or more fields.";
    private static final String MSG_AMOUNT_MUST_BE_GREATER_THAN_ZERO =
            "must be greater than 0";
    private static final String MSG_CONSTRAINT_VIOLATION =
            "Constraint violation for one or more parameters.";
    private static final String MSG_ALLOWED_PAYMENT_TYPES_SUFFIX =
            " Allowed values: AVISTA, PARCELADO LOJA, PARCELADO EMISSOR.";
    private static final String MSG_REQUIRED_PARAM_ID =
            "Required request parameter 'id' of type String is missing";
    private static final String MSG_TYPE_MISMATCH =
            "Parameter 'id' has invalid value 'abc'. Expected type: Integer";
    private static final String MSG_DUPLICATE_TRANSACTION_PREFIX =
            "Transaction with id '";
    private static final String MSG_DUPLICATE_TRANSACTION_SUFFIX =
            "' already exists";
    private static final String MSG_PAYMENT_NOT_FOUND_PREFIX =
            "Transaction not found for id: ";
    private static final String MSG_UNCAUGHT_EXCEPTION =
            "An unexpected error occurred. Please contact support if the problem persists.";
    private static final String DISPLAY_METHOD_ARGUMENT_NOT_VALID =
            "handleMethodArgumentNotValid should return 400 with fieldErrors list";
    private static final String DISPLAY_CONSTRAINT_VIOLATION =
            "handleConstraintViolation should return 400 with fieldErrors list";
    private static final String DISPLAY_HTTP_MESSAGE_NOT_READABLE =
            "handleHttpMessageNotReadable should append allowed values when payment method invalid";
    private static final String DISPLAY_MISSING_REQUEST_PARAM =
            "handleMissingServletRequestParameter should return 400 with formatted message";
    private static final String DISPLAY_TYPE_MISMATCH =
            "handleMethodArgumentTypeMismatch should return 400 with type info";
    private static final String DISPLAY_DUPLICATE_TRANSACTION =
            "handleDuplicateTransactionId should return 409 with formatted message";
    private static final String DISPLAY_PAYMENT_NOT_FOUND =
            "handlePaymentNotFound should return 404 with formatted message";
    private static final String DISPLAY_UNCAUGHT_EXCEPTION =
            "handleUncaughtException should return 500 with generic message";
    private static final String UNEXPECTED_ERROR_MESSAGE =
            "An unexpected error occurred. Please contact support if the problem persists.";
    private static final String DISPLAY_INVALID_INSTALLMENTS_FOR_PAYMENT_TYPE =
            "handleInvalidInstallmentsForPaymentType should return 400 with exception message";
    private static final String ERROR_MSG_INVALID_INSTALLMENTS =
            "Installments quantity must be lower than 2 when payment type is AVISTA";
    private static final String DISPLAY_METHOD_ARGUMENT_TYPE_MISMATCH_UNKNOWN =
            "handleMethodArgumentTypeMismatch should use 'unknown' when required type is null";
    private static final String ERROR_MSG_TYPE_MISMATCH_UNKNOWN =
            "Parameter 'id' has invalid value 'abc'. Expected type: unknown";
    private static final String DISPLAY_HTTP_MESSAGE_NOT_READABLE_GENERIC =
            "handleHttpMessageNotReadable should return root message when not payment type error";
    private static final String ERROR_MSG_HTTP_BODY_GENERIC =
            "Some generic body parsing error";


    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
    }

    @Test
    @DisplayName(DISPLAY_METHOD_ARGUMENT_NOT_VALID)
    void handleMethodArgumentNotValid() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), OBJECT_NAME_PAYMENT);
        bindingResult.addError(
                new FieldError(OBJECT_NAME_PAYMENT, FIELD_CARD_NUMBER, MSG_MUST_NOT_BE_BLANK)
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, MSG_VALIDATION_FAILED)
                .containsEntry(KEY_PATH, REQUEST_URI);
        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fieldErrors =
                (List<Map<String, String>>) body.get(KEY_FIELD_ERRORS);

        assertThat(fieldErrors).hasSize(1);
        assertThat(fieldErrors.get(0))
                .containsEntry(KEY_FIELD, FIELD_CARD_NUMBER)
                .containsEntry(KEY_MESSAGE, MSG_MUST_NOT_BE_BLANK);
    }

    @Test
    @DisplayName(DISPLAY_CONSTRAINT_VIOLATION)
    void handleConstraintViolation() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation =
                (ConstraintViolation<Object>) org.mockito.Mockito.mock(ConstraintViolation.class);
        Path path = org.mockito.Mockito.mock(Path.class);

        when(path.toString()).thenReturn(FIELD_AMOUNT);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(MSG_AMOUNT_MUST_BE_GREATER_THAN_ZERO);

        ConstraintViolationException ex =
                new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Map<String, Object>> response =
                handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, MSG_CONSTRAINT_VIOLATION)
                .containsEntry(KEY_PATH, REQUEST_URI);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fieldErrors =
                (List<Map<String, String>>) body.get(KEY_FIELD_ERRORS);

        assertThat(fieldErrors).hasSize(1);
        assertThat(fieldErrors.get(0))
                .containsEntry(KEY_FIELD, FIELD_AMOUNT)
                .containsEntry(KEY_MESSAGE, MSG_AMOUNT_MUST_BE_GREATER_THAN_ZERO);
    }

    @Test
    @DisplayName(DISPLAY_HTTP_MESSAGE_NOT_READABLE)
    void handleHttpMessageNotReadable_invalidPaymentType() {
        Throwable root = new RuntimeException(ROOT_INVALID_PAYMENT_METHOD_TYPE);

        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException(ROOT_BAD_BODY, root, null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleHttpMessageNotReadable(ex, request);

        Map<String, Object> body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isNotNull()
                .containsEntry(
                        KEY_MESSAGE,
                        ROOT_INVALID_PAYMENT_METHOD_TYPE + MSG_ALLOWED_PAYMENT_TYPES_SUFFIX
                )
                .containsEntry(KEY_PATH, REQUEST_URI);
    }

    @Test
    @DisplayName(DISPLAY_MISSING_REQUEST_PARAM)
    void handleMissingServletRequestParameter() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException(PARAM_ID, TYPE_STRING);

        ResponseEntity<Map<String, Object>> response =
                handler.handleMissingServletRequestParameter(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, MSG_REQUIRED_PARAM_ID)
                .containsEntry(KEY_PATH, REQUEST_URI);
    }

    @Test
    @DisplayName(DISPLAY_TYPE_MISMATCH)
    void handleMethodArgumentTypeMismatch() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException(
                        VALUE_ABC, Integer.class, PARAM_ID, null, new IllegalArgumentException("bad")
                );

        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodArgumentTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, MSG_TYPE_MISMATCH)
                .containsEntry(KEY_PATH, REQUEST_URI);
    }

    @Test
    @DisplayName(DISPLAY_DUPLICATE_TRANSACTION)
    void handleDuplicateTransactionId() {

        DuplicateTransactionIdException ex =
                new DuplicateTransactionIdException(TRANSACTION_ID);

        ResponseEntity<Map<String, Object>> response =
                handler.handleDuplicateTransactionId(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(
                        KEY_MESSAGE,
                        MSG_DUPLICATE_TRANSACTION_PREFIX + TRANSACTION_ID + MSG_DUPLICATE_TRANSACTION_SUFFIX
                )
                .containsEntry(KEY_PATH, REQUEST_URI);
        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();
    }

    @Test
    @DisplayName(DISPLAY_PAYMENT_NOT_FOUND)
    void handlePaymentNotFound() {

        PaymentNotFoundException ex =
                new PaymentNotFoundException(TRANSACTION_ID);

        ResponseEntity<Map<String, Object>> response =
                handler.handlePaymentNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(
                        KEY_MESSAGE,
                        MSG_PAYMENT_NOT_FOUND_PREFIX + TRANSACTION_ID
                )
                .containsEntry(KEY_PATH, REQUEST_URI);
        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();
    }

    @Test
    @DisplayName(DISPLAY_UNCAUGHT_EXCEPTION)
    void handleUncaughtException() {
        Exception ex = new RuntimeException(ROOT_EXCEPTION_MESSAGE);

        ResponseEntity<Map<String, Object>> response =
                handler.handleUncaughtException(ex, request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, MSG_UNCAUGHT_EXCEPTION)
                .containsEntry(KEY_PATH, REQUEST_URI);
        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();
    }
    
    @Test
    @DisplayName(DISPLAY_UNCAUGHT_EXCEPTION)
    void handleUncaughtException_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("boom");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUncaughtException(ex, request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, UNEXPECTED_ERROR_MESSAGE)
                .containsEntry(KEY_PATH, REQUEST_URI);

        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();
    }

    @Test
    @DisplayName(DISPLAY_INVALID_INSTALLMENTS_FOR_PAYMENT_TYPE)
    void handleInvalidInstallmentsForPaymentType_shouldReturnBadRequestWithMessage() {
        InvalidInstallmentsForPaymentTypeException ex =
                new InvalidInstallmentsForPaymentTypeException();

        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidInstallmentsForPaymentType(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, ERROR_MSG_INVALID_INSTALLMENTS)
                .containsEntry(KEY_PATH, REQUEST_URI);
        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();
    }

    @Test
    @DisplayName(DISPLAY_METHOD_ARGUMENT_TYPE_MISMATCH_UNKNOWN)
    void handleMethodArgumentTypeMismatch_shouldUseUnknownWhenRequiredTypeIsNull() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException(
                        "abc",
                        null,
                        "id",
                        null,
                        new IllegalArgumentException("bad")
                );

        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodArgumentTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, ERROR_MSG_TYPE_MISMATCH_UNKNOWN)
                .containsEntry(KEY_PATH, REQUEST_URI);
        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();
    }
    
    @Test
    @DisplayName(DISPLAY_HTTP_MESSAGE_NOT_READABLE_GENERIC)
    void handleHttpMessageNotReadable_shouldReturnRootMessage_whenNotPaymentTypeError() {
        Throwable root = new RuntimeException(ERROR_MSG_HTTP_BODY_GENERIC);

        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("bad_body", root, null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleHttpMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull()
                .containsEntry(KEY_MESSAGE, ERROR_MSG_HTTP_BODY_GENERIC)
                .containsEntry(KEY_PATH, REQUEST_URI);
        assertThat(body.get(KEY_TIMESTAMP)).isNotNull();
    }
}
