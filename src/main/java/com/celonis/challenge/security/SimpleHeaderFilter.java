package com.celonis.challenge.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SimpleHeaderFilter extends OncePerRequestFilter {
    private final String authHeader;
    private final String secretApiKey;

    public SimpleHeaderFilter(@Value("${celonis.api.auth.header}") String authHeader,
                              @Value("${celonis.api.auth.key}") String secretKey) {
        this.authHeader = authHeader;
        this.secretApiKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // OPTIONS should always work
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        String val = request.getHeader(authHeader);
        if (val == null || !val.equals(secretApiKey)) {
            response.setStatus(401);
            response.getWriter().append("Not authorized to execute, Make sure you provide secret header");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
