package com.atlashub.main.interceptor;

import com.atlashub.shared.domain.valueobject.CorrelationId;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        // Prefer X-Correlation-Id, fall back to X-Request-Id, generate if absent
        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = request.getHeader(REQUEST_ID_HEADER);
        }

        if (correlationId != null && !correlationId.isBlank()) {
            CorrelationId.set(correlationId);
        } else {
            correlationId = CorrelationId.getOrCreate();
        }

        // Echo the correlation ID back in the response for traceability
        response.setHeader(CORRELATION_HEADER, correlationId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        CorrelationId.clear();
    }
}
