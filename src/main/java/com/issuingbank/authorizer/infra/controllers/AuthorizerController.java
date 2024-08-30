package com.issuingbank.authorizer.infra.controllers;

import com.issuingbank.authorizer.application.authorizer.AuthorizationRequest;
import com.issuingbank.authorizer.application.authorizer.AuthorizationResponse;
import com.issuingbank.authorizer.application.authorizer.AuthorizationResponseType;
import com.issuingbank.authorizer.application.authorizer.TransactionAuthorizerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
@AllArgsConstructor
public class AuthorizerController {
    private TransactionAuthorizerService authorizerService;

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorize(@RequestHeader(value = "Idempotency-Key") final UUID idempotencyKey,
                                                           @RequestBody final AuthorizationRequest authorizationRequest) {
        try {
            return ResponseEntity.ok(authorizerService.execute(idempotencyKey, authorizationRequest));
        } catch (Exception e) {
            return ResponseEntity.ok(AuthorizationResponse.from(AuthorizationResponseType.UNEXPECTED_ERROR.getCode()));
        }
    }

}
