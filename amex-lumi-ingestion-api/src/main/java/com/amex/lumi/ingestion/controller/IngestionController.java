package com.amex.lumi.ingestion.controller;

import com.amex.lumi.ingestion.dto.IngestionRequest;
import com.amex.lumi.ingestion.dto.IngestionResponse;
import com.amex.lumi.ingestion.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingestions")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(consumes= "multipart/form-data")
    public ResponseEntity<IngestionResponse> triggerIngestion(
            @Valid @ModelAttribute IngestionRequest request) {

        IngestionResponse response =
                ingestionService.triggerIngestion(request);

        return ResponseEntity.ok(response);
    }
}