package com.atlashub.app.config;

import com.atlashub.shared.security.PublicEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class PublicEndpointScanner {

    private final ApplicationContext applicationContext;

    public PublicEndpointScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public record EndpointConfig(HttpMethod method, String path) {}

    public List<EndpointConfig> getPublicEndpoints() {
        List<EndpointConfig> endpoints = new ArrayList<>();
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object controller : controllers.values()) {
            Class<?> clazz = controller.getClass();
            // Unwrap CGLIB proxies
            if (clazz.getName().contains("$$")) {
                clazz = clazz.getSuperclass();
            }

            String basePath = "";
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                String[] classPaths = clazz.getAnnotation(RequestMapping.class).value();
                if (classPaths.length > 0) {
                    basePath = classPaths[0];
                }
            }

            // If class has @PublicEndpoint, all methods are public (simplify by not supporting class-level for now, or just iterate all methods)
            boolean classIsPublic = clazz.isAnnotationPresent(PublicEndpoint.class);

            for (Method method : clazz.getDeclaredMethods()) {
                if (classIsPublic || method.isAnnotationPresent(PublicEndpoint.class)) {
                    endpoints.addAll(extractEndpoints(method, basePath));
                }
            }
        }
        return endpoints;
    }

    private List<EndpointConfig> extractEndpoints(Method method, String basePath) {
        List<EndpointConfig> configs = new ArrayList<>();
        String[] methodPaths = new String[]{""};
        HttpMethod httpMethod = null; // null means all methods

        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            if (mapping.value().length > 0) methodPaths = mapping.value();
            if (mapping.method().length > 0) httpMethod = HttpMethod.valueOf(mapping.method()[0].name());
        } else if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            if (mapping.value().length > 0) methodPaths = mapping.value();
            httpMethod = HttpMethod.GET;
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            if (mapping.value().length > 0) methodPaths = mapping.value();
            httpMethod = HttpMethod.POST;
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            if (mapping.value().length > 0) methodPaths = mapping.value();
            httpMethod = HttpMethod.PUT;
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            if (mapping.value().length > 0) methodPaths = mapping.value();
            httpMethod = HttpMethod.DELETE;
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            PatchMapping mapping = method.getAnnotation(PatchMapping.class);
            if (mapping.value().length > 0) methodPaths = mapping.value();
            httpMethod = HttpMethod.PATCH;
        } else {
            // Not an endpoint method
            return configs;
        }

        for (String path : methodPaths) {
            String fullPath = basePath;
            if (!path.isEmpty()) {
                fullPath = basePath + (path.startsWith("/") ? path : "/" + path);
            }
            if (fullPath.isEmpty()) {
                fullPath = "/";
            }
            
            // Convert Spring MVC path variables {id} to Spring Security ant matchers * or ** if needed, 
            // but Spring Security 6 handles path variables natively if we use MvcRequestMatcher! 
            // However, with requestMatchers(path), Spring Security 6 uses AntPathRequestMatcher by default. 
            // So we need to convert {param} to *
            fullPath = fullPath.replaceAll("\\{[^/]+\\}", "*");

            configs.add(new EndpointConfig(httpMethod, fullPath));
        }

        return configs;
    }
}
