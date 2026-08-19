package com.teamflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void openApiBeanShouldExposeBearerSchemeAndInfo() {
        OpenAPI openAPI = new OpenApiConfig().teamflowOpenAPI();

        assertEquals("TeamFlow API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getInfo().getContact());
        assertNotNull(openAPI.getSecurity());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("bearerAuth"));
        assertEquals("JWT", openAPI.getComponents().getSecuritySchemes().get("bearerAuth").getBearerFormat());
    }
}
