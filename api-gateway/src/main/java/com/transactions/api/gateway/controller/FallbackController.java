package com.transactions.api.gateway.controller;

import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import com.transactions.api.gateway.dto.commons.StatusModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static com.transactions.api.gateway.constant.Constants.SERVICE_UNAVAILABLE;

@RestController
public class FallbackController {

    @RequestMapping("/contactSupport")
    public Mono<ResponseEntity<APIResponseDTO>> contactSupport() {
        return Mono.just(
                    ResponseEntity.status(503)
                            .body(
                            APIResponseDTO.builder()
                                    .responseMsg("An error occurred due to unavailability of an upstream service. Please try after some time or contact support team!!!")
                                    .statusModel(new StatusModel(503, SERVICE_UNAVAILABLE))
                                    .build()

        ));
    }

}
