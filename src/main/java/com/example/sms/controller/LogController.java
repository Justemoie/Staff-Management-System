package com.example.sms.controller;

import com.example.sms.dto.response.ErrorResponse;
import com.example.sms.dto.response.LogCreationResponse;
import com.example.sms.model.LogCreationStatus;
import com.example.sms.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Log Management", description = "Endpoints for managing log files")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/logs/async")
    @Operation(
            summary = "Create log file asynchronously for current date",
            description = "Initiates async log file "
                    + "creation for the current date and returns an ID to track status"
    )
    @ApiResponses(value = {@ApiResponse
            (responseCode = "200", description = "Log file creation initiated"), @ApiResponse
            (responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LogCreationResponse> createLogFileAsync() {
        try {
            LogCreationResponse response = logService.createLogFileAsync();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new LogCreationResponse(null, "FAILED: " + e.getMessage()));
        }
    }

    @GetMapping("/logs/status/{id}")
    @Operation(
            summary = "Get log creation status",
            description = "Retrieves the status of a log file creation by ID"
    )
    @ApiResponses(value = { @ApiResponse
            (responseCode = "200", description = "Status retrieved successfully"), @ApiResponse
            (responseCode = "404", description = "Status not found")
    })
    public ResponseEntity<LogCreationStatus> getLogCreationStatus(
            @Parameter(description = "Log creation ID", required = true)
            @PathVariable String id) {
        LogCreationStatus status = logService.getLogCreationStatus(id);
        return "NOT_FOUND".equals(status.getStatus())
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(status);
    }

    @GetMapping("/logs/download/{id}")
    @Operation(
            summary = "Download log file by ID",
            description =
                    "Downloads the log file associated with the creation ID (always current date)"
    )
    @ApiResponses(value = {@ApiResponse
            (responseCode = "200", description = "Log file retrieved successfully"), @ApiResponse
            (responseCode = "400", description = "File is still being processed"), @ApiResponse
            (responseCode = "404", description = "Log file not found")
    })
    public ResponseEntity<?> getLogFileById(
            @Parameter(description = "Log creation ID", required = true)
            @PathVariable String id) {
        try {
            Resource resource = logService.getLogFileById(id);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app.log\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", e.getMessage(), 400));
        }
    }

    @GetMapping("/logs")
    @Operation(
            summary = "Get log file by date",
            description = "Retrieves the log file for the "
                    + "specified date (current or past) in the format yyyy-MM-dd"
    )
    @ApiResponses(value = { @ApiResponse
            (responseCode = "200", description = "Log file retrieved successfully"), @ApiResponse
            (responseCode = "400", description = "Invalid date format or future date"), @ApiResponse
            (responseCode = "404", description = "Log file not found")
    })
    public ResponseEntity<Resource> getLogFile(
            @Parameter(description = "Date in yyyy-MM-dd format "
                    + "(e.g., 2025-04-13), must be current or past", required = true)
            @RequestParam("date") String date) {
        try {
            Resource resource = logService.getLogFileResource(date);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            String fileName = logDate.equals(LocalDate.now())
                    ? "app.log"
                    : "app." + logDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (DateTimeParseException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(logService.getInvalidDateResource());
        }
    }
}