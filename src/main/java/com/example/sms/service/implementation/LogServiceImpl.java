package com.example.sms.service.implementation;

import com.example.sms.service.LogService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class LogServiceImpl implements LogService {

    @PostConstruct
    public void init() {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            Path invalidDateFile = Paths.get("logs/invalid-date.txt");
            if (!Files.exists(invalidDateFile)) {
                Files.writeString(invalidDateFile, "Invalid date format. Please use yyyy-MM-dd (e.g., 2025-03-31).");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize invalid-date.txt", e);
        }
    }

    @Override
    public Resource getLogFileResource(String date) throws DateTimeParseException {
        LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        File logFile;

        // Если запрашивается текущий день, используем logs/app.log
        if (logDate.equals(LocalDate.now())) {
            logFile = new File("logs/app.log");
        } else {
            // Для прошлых дней используем архивный файл
            String logFileName = "logs/app." + logDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";
            logFile = new File(logFileName);
        }

        if (!logFile.exists()) {
            return null;
        }

        return new FileSystemResource(logFile);
    }

    @Override
    public Resource getInvalidDateResource() {
        return new FileSystemResource(new File("logs/invalid-date.txt"));
    }
}