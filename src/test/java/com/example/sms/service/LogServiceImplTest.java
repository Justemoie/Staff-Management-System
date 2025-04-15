package com.example.sms.service;

import com.example.sms.service.implementation.LogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class LogServiceImplTest {

    private LogServiceImpl logService;

    @TempDir
    Path tempDir;

    private Path logsDir;
    private Path invalidDateFile;

    @BeforeEach
    void setUp() {
        // Создаём временную директорию logs
        logsDir = tempDir.resolve("logs");
        invalidDateFile = logsDir.resolve("invalid-date.txt");

        // Инициализируем LogServiceImpl с временной директорией
        logService = new LogServiceImpl(logsDir);
    }

    // Тесты для метода init
    @Test
    void init_WhenLogsDirDoesNotExist_ShouldCreateDirAndFile() throws IOException {
        // Act
        logService.init();

        // Assert
        assertTrue(Files.exists(logsDir), "Logs directory should be created");
        assertTrue(Files.exists(invalidDateFile), "invalid-date.txt should be created");
        String content = Files.readString(invalidDateFile);
        assertEquals("Invalid date format. Please use yyyy-MM-dd (e.g., 2025-03-31).", content);
    }

    @Test
    void init_WhenLogsDirExists_ShouldNotThrowException() throws IOException {
        // Arrange
        Files.createDirectories(logsDir);

        // Act
        logService.init();

        // Assert
        assertTrue(Files.exists(logsDir), "Logs directory should still exist");
        assertTrue(Files.exists(invalidDateFile), "invalid-date.txt should be created");
        String content = Files.readString(invalidDateFile);
        assertEquals("Invalid date format. Please use yyyy-MM-dd (e.g., 2025-03-31).", content);
    }

    @Test
    void getLogFileResource_WhenCurrentDateAndLogExists_ShouldReturnResource() throws IOException {
        // Arrange
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        File logFile = logsDir.resolve("app.log").toFile();
        Files.createDirectories(logsDir);
        Files.createFile(logFile.toPath());

        // Act
        Resource result = logService.getLogFileResource(date);

        // Assert
        assertNotNull(result, "Resource should not be null");
        assertTrue(result.exists(), "Log file should exist");
        assertEquals(new FileSystemResource(logFile), result);
    }

    @Test
    void getLogFileResource_WhenCurrentDateAndLogDoesNotExist_ShouldReturnNull() {
        // Arrange
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Act
        Resource result = logService.getLogFileResource(date);

        // Assert
        assertNull(result, "Resource should be null if log file does not exist");
    }

    @Test
    void getLogFileResource_WhenPastDateAndLogExists_ShouldReturnResource() throws IOException {
        // Arrange
        String date = "2025-03-31";
        String logFileName = "app." + date + ".log";
        File logFile = logsDir.resolve(logFileName).toFile();
        Files.createDirectories(logsDir);
        Files.createFile(logFile.toPath());

        // Act
        Resource result = logService.getLogFileResource(date);

        // Assert
        assertNotNull(result, "Resource should not be null");
        assertTrue(result.exists(), "Log file should exist");
        assertEquals(new FileSystemResource(logFile), result);
    }

    @Test
    void getLogFileResource_WhenPastDateAndLogDoesNotExist_ShouldReturnNull() {
        // Arrange
        String date = "2025-03-31";

        // Act
        Resource result = logService.getLogFileResource(date);

        // Assert
        assertNull(result, "Resource should be null if log file does not exist");
    }

    @Test
    void getLogFileResource_WhenInvalidDateFormat_ShouldThrowDateTimeParseException() {
        // Arrange
        String invalidDate = "2025-13-31"; // Неверный месяц

        // Act & Assert
        assertThrows(DateTimeParseException.class, () -> logService.getLogFileResource(invalidDate),
                "Should throw DateTimeParseException for invalid date format");
    }

    // Тесты для метода getInvalidDateResource
    @Test
    void getInvalidDateResource_WhenFileExists_ShouldReturnResource() throws IOException {
        // Arrange
        Files.createDirectories(logsDir);
        Files.writeString(invalidDateFile, "Invalid date format. Please use yyyy-MM-dd (e.g., 2025-03-31).");

        // Act
        Resource result = logService.getInvalidDateResource();

        // Assert
        assertNotNull(result, "Resource should not be null");
        assertTrue(result.exists(), "invalid-date.txt should exist");
        assertEquals(new FileSystemResource(invalidDateFile.toFile()), result);
    }

    @Test
    void getInvalidDateResource_WhenFileDoesNotExist_ShouldReturnResourceForNonExistentFile() {
        // Arrange
        // Не создаём файл invalid-date.txt

        // Act
        Resource result = logService.getInvalidDateResource();

        // Assert
        assertNotNull(result, "Resource should not be null");
        assertFalse(result.exists(), "invalid-date.txt should not exist");
        assertEquals(new FileSystemResource(logsDir.resolve("invalid-date.txt").toFile()), result);
    }
}