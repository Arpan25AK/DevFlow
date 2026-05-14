package com.dev.api_gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new HashMap<>();

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public void putHeader(String name, String value) {
        customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        if (customHeaders.containsKey(name)) return customHeaders.get(name);
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (customHeaders.containsKey(name))
            return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(customHeaders.keySet());
        return Collections.enumeration(names);
    }
}