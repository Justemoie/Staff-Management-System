package com.example.sms.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeParseException;

@Service
public interface LogService {
    Resource getLogFileResource(String date) throws DateTimeParseException;

    Resource getInvalidDateResource();

}
