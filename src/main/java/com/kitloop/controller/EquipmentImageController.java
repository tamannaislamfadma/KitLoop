package com.kitloop.controller;

import com.kitloop.exception.FileStorageException;
import com.kitloop.service.EquipmentImageStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class EquipmentImageController {

    private final EquipmentImageStorageService
            equipmentImageStorageService;

    public EquipmentImageController(
            EquipmentImageStorageService equipmentImageStorageService
    ) {
        this.equipmentImageStorageService =
                equipmentImageStorageService;
    }

    @GetMapping(
            "/media/equipment/{filename:.+}"
    )
    public ResponseEntity<Resource>
    equipmentImage(
            @PathVariable
            String filename
    ) {

        try {

            Resource resource =
                    equipmentImageStorageService
                            .loadEquipmentImage(
                                    filename
                            );

            String contentType =
                    equipmentImageStorageService
                            .determineContentType(
                                    filename
                            );

            return ResponseEntity
                    .ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    contentType
                            )
                    )
                    .cacheControl(
                            CacheControl
                                    .maxAge(
                                            Duration.ofDays(7)
                                    )
                                    .cachePrivate()
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\""
                                    + filename
                                    + "\""
                    )
                    .body(resource);

        } catch (
                FileStorageException exception
        ) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }
}