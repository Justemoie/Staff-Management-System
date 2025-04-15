package com.example.sms.service.implementation;

import com.example.sms.dto.response.LogCreationResponse;
import com.example.sms.model.LogCreationStatus;
import com.example.sms.service.LogService;
import com.example.sms.exception.LogCreationException;  // Importing the custom exception
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private final Path logDir;
    private final Map<String, LogCreationStatus> statusMap = new ConcurrentHashMap<>();

    public LogServiceImpl() {
        this.logDir = Paths.get("logs");
    }

    @Autowired
    public LogServiceImpl(Path logDir) {
        this.logDir = logDir;
    }

    @Bean
    public Path logDir() {
        return Paths.get("logs");
    }

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            Path invalidDateFile = logDir.resolve("invalid-date.txt");
            if (!Files.exists(invalidDateFile)) {
                Files.writeString(invalidDateFile,
                        "Invalid date format. Please use yyyy-MM-dd (e.g., 2025-03-31).");
            }
        } catch (IOException e) {
            throw new LogCreationException("Failed to initialize invalid-date.txt", e);  // Use custom exception here
        }
    }

    @Override
    public LogCreationResponse createLogFileAsync() {
        String id = UUID.randomUUID().toString();
        LogCreationStatus status = new LogCreationStatus(id, "PENDING");
        statusMap.put(id, status);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(8000);

                LocalDate logDate = LocalDate.now();
                String fileName = "app.log";
                Path filePath = logDir.resolve(fileName);

                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                }

                Files.writeString(filePath,
                        "Лог для текущей даты: "
                                + logDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

                status.setStatus("COMPLETED");
                status.setFilePath(filePath.toString());
            } catch (IOException e) {
                status.setStatus("FAILED");
                throw new LogCreationException("Не удалось создать лог-файл", e);  // Use custom exception here
            } catch (InterruptedException e) {
                status.setStatus("FAILED");
                Thread.currentThread().interrupt();
                throw new LogCreationException("Операция была прервана во время задержки", e);  // Use custom exception here
            }
        });

        return new LogCreationResponse(id, "PENDING");
    }

    @Override
    public LogCreationStatus getLogCreationStatus(String id) {
        return statusMap.getOrDefault(id, new LogCreationStatus(id, "NOT_FOUND"));
    }

    @Override
    public Resource getLogFileById(String id) {
        LogCreationStatus status = statusMap.get(id);
        if (status == null || status.getFilePath() == null) {
            return null;
        }
        if ("PENDING".equals(status.getStatus())) {
            throw new LogCreationException("Файл ещё обрабатывается, попробуйте позже");  // Use custom exception here
        }
        if (!"COMPLETED".equals(status.getStatus())) {
            return null;
        }
        File file = new File(status.getFilePath());
        return file.exists() ? new FileSystemResource(file) : null;
    }

    @Override
    public Resource getLogFileResource(String date) throws DateTimeParseException {
        LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);

        if (logDate.isAfter(LocalDate.now())) {
            throw new LogCreationException("Нельзя запрашивать логи для будущих дат");
        }

        File logFile;
        if (logDate.equals(LocalDate.now())) {
            logFile = logDir.resolve("app.log").toFile();
        } else {
            String logFileName = "app." + logDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";
            logFile = logDir.resolve(logFileName).toFile();
        }

        return logFile.exists() ? new FileSystemResource(logFile) : null;
    }

    @Override
    public Resource getInvalidDateResource() {
        return new FileSystemResource(logDir.resolve("invalid-date.txt").toFile());
    }
}
