package com.transactions.api.gateway.controller;

import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import com.transactions.api.gateway.dto.commons.StatusModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

class FallbackControllerTests {

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        FallbackController fallbackController = new FallbackController();
        webTestClient = WebTestClient
                .bindToController(fallbackController)
                .build();
    }

    @Test
    void testContactSupport_ReturnsCorrectErrorMessage() {

        APIResponseDTO expectedResponse = APIResponseDTO.builder()
                .responseMsg("An error occurred due to unavailability of an upstream service. Please try after some time or contact support team!!!")
                .statusModel(new StatusModel(503, "SERVICE_UNAVAILABLE"))
                .build();
        // Act & Assert
        webTestClient.get()
                .uri("/contactSupport")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody(APIResponseDTO.class).consumeWith(
                        apiResponseDTOEntityExchangeResult -> {
                            assertThat(apiResponseDTOEntityExchangeResult.getResponseBody()).isNotNull();
                            assertThat(apiResponseDTOEntityExchangeResult.getResponseBody().getResponseMsg()).isEqualTo(expectedResponse.getResponseMsg());
                            assertThat(apiResponseDTOEntityExchangeResult.getResponseBody().getStatusModel().getStatusCode()).isEqualTo(expectedResponse.getStatusModel().getStatusCode());
                            assertThat(apiResponseDTOEntityExchangeResult.getResponseBody().getStatusModel().getStatusMsg()).isEqualTo(expectedResponse.getStatusModel().getStatusMsg());
                        }
                );
    }

}