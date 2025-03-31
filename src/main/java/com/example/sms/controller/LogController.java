package com.example.sms.controller;

import com.example.sms.service.LogService;
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
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    public ResponseEntity<Resource> getLogFile(@RequestParam("date") String date) {
        try {
            Resource resource = logService.getLogFileResource(date);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // Определяем имя файла на основе даты
            LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            String fileName = logDate.equals(LocalDate.now())
                    ? "app.log"
                    : "app." + logDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(logService.getInvalidDateResource());
        }
    }
}