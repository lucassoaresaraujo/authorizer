package com.issuingbank.authorizer.infra.controllers;

import com.issuingbank.authorizer.application.merchant.CreateMerchantRequest;
import com.issuingbank.authorizer.application.merchant.CreateMerchantService;
import com.issuingbank.authorizer.domain.merchant.Merchant;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@AllArgsConstructor
public class MerchantController {
    private final CreateMerchantService createMerchantService;

    @PostMapping("/merchant")
    public ResponseEntity<Merchant> create(CreateMerchantRequest request) {
        return ResponseEntity.ok(createMerchantService.execute(request));
    }

}
