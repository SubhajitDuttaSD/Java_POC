package com.example.campaignapi.security;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String MISSING_OR_INVALID_KEY_MESSAGE =
            "Missing or invalid API key. Send it in the X-API-Key header.";
    private static final Set<String> READ_ONLY_METHODS = Set.of("GET", "HEAD");

    private final ApiKeyRepository apiKeyRepository;
    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository, RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.apiKeyRepository = apiKeyRepository;
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String rawKey = request.getHeader(API_KEY_HEADER);
        if (rawKey == null || rawKey.isBlank()) {
            writeError(response, 401, MISSING_OR_INVALID_KEY_MESSAGE);
            return;
        }

        Optional<ApiKey> apiKey = apiKeyRepository.findByKeyHashAndRevokedFalse(ApiKeyHasher.hash(rawKey));
        if (apiKey.isEmpty()) {
            writeError(response, 401, MISSING_OR_INVALID_KEY_MESSAGE);
            return;
        }

        RateLimitResult rateLimitResult = rateLimiter.tryConsume(apiKey.get().getId());
        response.setHeader("X-RateLimit-Limit", String.valueOf(RateLimiter.CAPACITY));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));
        if (!rateLimitResult.allowed()) {
            response.setHeader("Retry-After", String.valueOf(rateLimitResult.retryAfterSeconds()));
            writeError(response, 429, "Too many requests. Retry after "
                    + rateLimitResult.retryAfterSeconds() + " second(s).");
            return;
        }

        String requiredScope = READ_ONLY_METHODS.contains(request.getMethod())
                ? ApiKeyScopes.READ
                : ApiKeyScopes.WRITE;

        if (!apiKey.get().hasScope(requiredScope)) {
            writeError(response, 403, "This API key does not have permission for this action.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "status", status,
                "message", message)));
    }
}
