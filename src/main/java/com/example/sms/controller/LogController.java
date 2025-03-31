package com.example.sms.controller;

import com.example.sms.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@Tag(name = "Log Management", description = "Endpoints for managing log files")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    @Operation(
            summary = "Get log file by date",
            description = "Retrieves the log file for the specified date in the format yyyy-MM-dd. Returns the log file as a downloadable resource."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log file retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format. Date must be in yyyy-MM-dd format"),
            @ApiResponse(responseCode = "404", description = "Log file not found for the specified date")
    })
    public ResponseEntity<Resource> getLogFile(
            @Parameter(description = "Date of the log file in yyyy-MM-dd format (e.g., 2025-03-31)", required = true)
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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(logService.getInvalidDateResource());
        }
    }
}