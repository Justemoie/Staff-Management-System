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

    private final Path logDir;

    public LogServiceImpl() {
        this.logDir = Paths.get("logs");
    }

    // Конструктор для тестов
    public LogServiceImpl(Path logDir) {
        this.logDir = logDir;
    }

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            Path invalidDateFile = logDir.resolve("invalid-date.txt");
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

        if (logDate.equals(LocalDate.now())) {
            logFile = logDir.resolve("app.log").toFile();
        } else {
            String logFileName = "app." + logDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";
            logFile = logDir.resolve(logFileName).toFile();
        }

        if (!logFile.exists()) {
            return null;
        }

        return new FileSystemResource(logFile);
    }

    @Override
    public Resource getInvalidDateResource() {
        return new FileSystemResource(logDir.resolve("invalid-date.txt").toFile());
    }
}