package br.com.toolschallenge.controller;

import static br.com.toolschallenge.data.PaymentTestDataFactory.DEFAULT_TRANSACTION_ID;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createAuthorizedPaymentResponse;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createCanceledPaymentResponse;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createValidPaymentRequest;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.toolschallenge.dto.PagamentoRequestDTO;
import br.com.toolschallenge.dto.PagamentoResponseDTO;
import br.com.toolschallenge.service.PaymentService;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private static final String BASE_URL = "/pagamentos";
    private static final String LIST_ALL_URL = "/pagamentos/listAllPayments";
    private static final String ESTORNO_URL_TEMPLATE = "/pagamentos/{id}/estorno";
    private static final String ID_PATH_TEMPLATE = "/{id}";

    private static final String STATUS_AUTORIZADO = "AUTORIZADO";
    private static final String STATUS_CANCELADO = "CANCELADO";

    private static final String JSON_ROOT = "$";
    private static final String JSON_TRANSACTION_ID_PATH = "$.transacao.id";
    private static final String JSON_TRANSACTION_STATUS_PATH = "$.transacao.descricao.status";
    private static final String JSON_LIST_FIRST_STATUS_PATH = "$[0].transacao.descricao.status";
    private static final String JSON_LIST_SECOND_STATUS_PATH = "$[1].transacao.descricao.status";

    private static final String DISPLAY_CREATE_PAYMENT =
            "POST /pagamentos should return 201 Created with payment response body";
    private static final String DISPLAY_GET_BY_ID =
            "GET /pagamentos/{id} should return 200 OK with payment";
    private static final String DISPLAY_LIST_ALL =
            "GET /pagamentos should return list of payments";
    private static final String DISPLAY_CANCEL_PAYMENT =
            "POST /pagamentos/{id}/estorno should return 200 OK with canceled payment";

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(paymentController)
                .build();
    }

    @Test
    @DisplayName(DISPLAY_CREATE_PAYMENT)
    void createPayment_endpoint() throws Exception {
        PagamentoRequestDTO request = createValidPaymentRequest();
        PagamentoResponseDTO response = createAuthorizedPaymentResponse();

        when(paymentService.createPayment(any(PagamentoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(JSON_TRANSACTION_ID_PATH, is(DEFAULT_TRANSACTION_ID)))
                .andExpect(jsonPath(JSON_TRANSACTION_STATUS_PATH, is(STATUS_AUTORIZADO)));
    }

    @Test
    @DisplayName(DISPLAY_GET_BY_ID)
    void getPaymentById_endpoint() throws Exception {
        PagamentoResponseDTO response = createAuthorizedPaymentResponse();
        when(paymentService.findPaymentById(DEFAULT_TRANSACTION_ID)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + ID_PATH_TEMPLATE, DEFAULT_TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_TRANSACTION_ID_PATH, is(DEFAULT_TRANSACTION_ID)));
    }

    @Test
    @DisplayName(DISPLAY_LIST_ALL)
    void listAllPayments_endpoint() throws Exception {
        PagamentoResponseDTO response1 = createAuthorizedPaymentResponse();
        PagamentoResponseDTO response2 = createCanceledPaymentResponse();

        when(paymentService.listAllPayments()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get(LIST_ALL_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_ROOT, hasSize(2)))
                .andExpect(jsonPath(JSON_LIST_FIRST_STATUS_PATH, is(STATUS_AUTORIZADO)))
                .andExpect(jsonPath(JSON_LIST_SECOND_STATUS_PATH, is(STATUS_CANCELADO)));
    }

    @Test
    @DisplayName(DISPLAY_CANCEL_PAYMENT)
    void cancelPayment_endpoint() throws Exception {
        PagamentoResponseDTO response = createCanceledPaymentResponse();
        when(paymentService.cancelPayment(DEFAULT_TRANSACTION_ID)).thenReturn(response);

        mockMvc.perform(post(ESTORNO_URL_TEMPLATE, DEFAULT_TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_TRANSACTION_STATUS_PATH, is(STATUS_CANCELADO)));
    }
}