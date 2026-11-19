package com.kitloop.controller;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentRequest;
import com.kitloop.exception.FileStorageException;
import com.kitloop.exception.RequestFlowException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.EquipmentRequestService;
import com.kitloop.service.PaymentScreenshotStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PaymentProofController {

    private final AppUserRepository appUserRepository;

    private final EquipmentRequestService
            equipmentRequestService;

    private final PaymentScreenshotStorageService
            paymentScreenshotStorageService;

    public PaymentProofController(
            AppUserRepository appUserRepository,
            EquipmentRequestService equipmentRequestService,
            PaymentScreenshotStorageService paymentScreenshotStorageService
    ) {
        this.appUserRepository = appUserRepository;
        this.equipmentRequestService = equipmentRequestService;
        this.paymentScreenshotStorageService = paymentScreenshotStorageService;
    }

    @GetMapping(
            "/media/payments/{filename:.+}"
    )
    public ResponseEntity<Resource> paymentProof(
            @PathVariable String filename,
            Authentication authentication
    ) {

        AppUser currentUser =
                appUserRepository
                        .findByEmailIgnoreCase(
                                authentication.getName()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        FORBIDDEN
                                )
                        );

        EquipmentRequest request;

        try {

            request =
                    equipmentRequestService
                            .getRequestByScreenshotFilename(
                                    filename
                            );

        } catch (
                RequestFlowException exception
        ) {

            throw new ResponseStatusException(
                    NOT_FOUND
            );
        }

        if (!equipmentRequestService
                .canViewPaymentProof(
                        request,
                        currentUser
                )) {

            throw new ResponseStatusException(
                    FORBIDDEN
            );
        }

        try {

            Resource resource =
                    paymentScreenshotStorageService
                            .load(
                                    filename
                            );

            String type =
                    paymentScreenshotStorageService
                            .determineContentType(
                                    filename
                            );

            return ResponseEntity
                    .ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    type
                            )
                    )
                    .cacheControl(
                            CacheControl
                                    .maxAge(
                                            Duration.ofMinutes(10)
                                    )
                                    .cachePrivate()
                    )
                    .body(resource);

        } catch (
                FileStorageException exception
        ) {

            throw new ResponseStatusException(
                    NOT_FOUND
            );
        }
    }
}