package com.example.sms.controller;

import com.example.sms.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Visit Counter", description = "Endpoints for tracking website visits")
public class VisitCounterController {

    private final VisitCounterService visitCounterService;

    public VisitCounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/visits")
    @Operation(
            summary = "Get visit count for a URL",
            description = "Returns the number of visits to the specified URL"
    )
    @ApiResponses(value = {@ApiResponse
            (responseCode = "200", description = "Visit count retrieved successfully"), @ApiResponse
            (responseCode = "400", description = "Invalid URL provided")
    })
    public ResponseEntity<Long> getVisitCount(
            @Parameter(description =
                    "URL to get visit count for (e.g., /logs/async)", required = true)
            @RequestParam("url") String url) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(0L);
        }
        long count = visitCounterService.getVisitCount(url);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/visits/total")
    @Operation(
            summary = "Get total visit count for all URLs",
            description = "Returns the total number of visits across all URLs"
    )
    @ApiResponses(value = {@ApiResponse
            (responseCode = "200", description = "Total visit count retrieved successfully")
    })
    public ResponseEntity<Long> getTotalVisitCount() {
        long totalCount = visitCounterService.getTotalVisitCount();
        return ResponseEntity.ok(totalCount);
    }
}