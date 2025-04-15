package com.example.sms.aspect;

import com.example.sms.service.VisitCounterService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class VisitCounterAspect {

    private final VisitCounterService visitCounterService;

    @Autowired
    public VisitCounterAspect(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Before("execution(* com.example.sms.controller..*.*(..)) && "
            + "(@annotation(org.springframework.web.bind.annotation.GetMapping) || "
           + "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.RequestMapping))")
    public void incrementVisitCount(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String url = request.getRequestURI();
            visitCounterService.incrementVisit(url);
        }
    }
}