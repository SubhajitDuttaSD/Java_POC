package com.example.campaignapi.security;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthFilterTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    private RateLimiter rateLimiter;
    private ApiKeyAuthFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
        filter = new ApiKeyAuthFilter(apiKeyRepository, rateLimiter, new ObjectMapper());
        chain = org.mockito.Mockito.mock(FilterChain.class);
    }

    private ApiKey apiKeyWithScopes(long id, String scopes) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(id);
        apiKey.setScopes(scopes);
        return apiKey;
    }

    @Test
    void missingKey_returns401_andDoesNotCallChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/campaigns");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void unknownKey_returns401() throws Exception {
        when(apiKeyRepository.findByKeyHashAndRevokedFalse(any())).thenReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/campaigns");
        request.addHeader("X-API-Key", "bogus");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void readOnlyKey_onGet_isAllowed() throws Exception {
        when(apiKeyRepository.findByKeyHashAndRevokedFalse(any()))
                .thenReturn(Optional.of(apiKeyWithScopes(1L, ApiKeyScopes.READ)));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/campaigns");
        request.addHeader("X-API-Key", "valid-read-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo(String.valueOf(RateLimiter.CAPACITY));
    }

    @Test
    void readOnlyKey_onPost_isForbidden() throws Exception {
        when(apiKeyRepository.findByKeyHashAndRevokedFalse(any()))
                .thenReturn(Optional.of(apiKeyWithScopes(1L, ApiKeyScopes.READ)));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/campaigns");
        request.addHeader("X-API-Key", "valid-read-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void fullAccessKey_onPost_isAllowed() throws Exception {
        when(apiKeyRepository.findByKeyHashAndRevokedFalse(any()))
                .thenReturn(Optional.of(apiKeyWithScopes(1L, ApiKeyScopes.READ + "," + ApiKeyScopes.WRITE)));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/campaigns");
        request.addHeader("X-API-Key", "valid-write-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void exceedingRateLimit_returns429_withRetryAfterHeader_andDoesNotCallChain() throws Exception {
        when(apiKeyRepository.findByKeyHashAndRevokedFalse(any()))
                .thenReturn(Optional.of(apiKeyWithScopes(1L, ApiKeyScopes.READ)));

        for (int i = 0; i < RateLimiter.CAPACITY; i++) {
            rateLimiter.tryConsume(1L);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/campaigns");
        request.addHeader("X-API-Key", "valid-read-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isNotNull();
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void pathOutsideApiV1_isNotFiltered() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
