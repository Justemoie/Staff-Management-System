package com.example.sms.service.implementation;

import com.example.sms.service.VisitCounterService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterServiceImpl implements VisitCounterService {

    private final Map<String, AtomicLong> visitCounters = new ConcurrentHashMap<>();

    @Override
    public void incrementVisit(String url) {
        AtomicLong counter = visitCounters.computeIfAbsent(url, k -> new AtomicLong(0));

        counter.incrementAndGet();
    }

    @Override
    public long getVisitCount(String url) {
        AtomicLong counter = visitCounters.get(url);
        return counter != null ? counter.get() : 0;
    }

    @Override
    public long getTotalVisitCount() {
        return visitCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }
}