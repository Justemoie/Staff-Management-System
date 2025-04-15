package com.example.sms.service;

import com.example.sms.dto.response.LogCreationResponse;
import com.example.sms.model.LogCreationStatus;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.io.Resource;

public interface LogService {
    LogCreationResponse createLogFileAsync();

    LogCreationStatus getLogCreationStatus(String id);

    Resource getLogFileById(String id);

    Resource getLogFileResource(String date) throws DateTimeParseException;

    Resource getInvalidDateResource();
}