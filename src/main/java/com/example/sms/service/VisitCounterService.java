package com.example.sms.service;

public interface VisitCounterService {
    void incrementVisit(String url);

    long getVisitCount(String url);

    long getTotalVisitCount();
}
