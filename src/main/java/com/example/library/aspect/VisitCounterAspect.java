package com.example.library.aspect;

import com.example.library.annotation.CountVisit;
import com.example.library.exception.VisitCounterException;
import com.example.library.service.VisitCounterService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class VisitCounterAspect {
    private final VisitCounterService visitCounterService;

    public VisitCounterAspect(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Around("@annotation(countVisit)")
    public Object countVisit(ProceedingJoinPoint joinPoint, CountVisit countVisit)
            throws VisitCounterException {
        String url = countVisit.value().isEmpty()
                ? resolveUrlFromRequest()
                : countVisit.value();

        try {
            visitCounterService.incrementCounter(url);
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new VisitCounterException("Failed to increment visit counter for URL: " + url, e);
        }
    }

    private String resolveUrlFromRequest() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();

        return request.getRequestURI();
    }
}