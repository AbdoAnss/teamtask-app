package com.teamflow.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow over MockMvc with a real H2 database: exercises the security
 * chain, controllers, services, mappers and exception handler together.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String aliceToken;
    private String aliceRefreshToken;
    private String bobToken;
    private String bobId;
    private String projectId;
    private String taskId;

    private ObjectNode body(String... pairs) {
        ObjectNode node = objectMapper.createObjectNode();
        for (int i = 0; i < pairs.length; i += 2) {
            node.put(pairs[i], pairs[i + 1]);
        }
        return node;
    }

    private MvcResult register(String username) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("username", username,
                    "email", username + "@teamflow.test",
                    "password", "password123",
                    "firstName", username.toUpperCase()).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.user.username").value(username))
            .andReturn();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    @Order(0)
    void apiDocsShouldBePubliclyAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isOk());
    }

    @Test
    @Order(1)
    void authFlowShouldRegisterLoginAndRejectBadInput() throws Exception {
        MvcResult registered = register("alice");
        aliceToken = json(registered).get("accessToken").asText();
        aliceRefreshToken = json(registered).get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("username", "alice", "password", "password123").toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.username").value("alice"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("username", "alice", "password", "wrong-password").toString()))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("username", "alice",
                    "email", "alice2@teamflow.test",
                    "password", "password123").toString()))
            .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("username", "x", "email", "not-an-email", "password", "short").toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());

        mockMvc.perform(get("/api/projects"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(2)
    void projectAndTaskFlowShouldCrudThroughHttp() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("name", "Website Redesign", "description", "New marketing site").toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Website Redesign"))
            .andExpect(jsonPath("$.owner.username").value("alice"))
            .andExpect(jsonPath("$.members.length()").value(1))
            .andReturn();
        projectId = json(created).get("id").asText();

        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(put("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("name", "Website Redesign v2").toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Website Redesign v2"));

        MvcResult taskCreated = mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                .header("Authorization", "Bearer " + aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("title", "Design homepage", "description", "Wireframes first").toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Design homepage"))
            .andExpect(jsonPath("$.status").value("TODO"))
            .andExpect(jsonPath("$.projectName").value("Website Redesign v2"))
            .andReturn();
        taskId = json(taskCreated).get("id").asText();

        mockMvc.perform(get("/api/projects/" + projectId + "/tasks?title=homepage")
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Design homepage"));

        mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("status", "DONE").toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @Order(3)
    void dashboardAndAccessControlShouldEnforceMembership() throws Exception {
        mockMvc.perform(get("/api/dashboard/projects/" + projectId)
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTasks").value(1))
            .andExpect(jsonPath("$.completionPercentage").value(100.0));

        MvcResult bob = register("bob");
        bobToken = json(bob).get("accessToken").asText();
        bobId = json(bob).get("user").get("id").asText();

        mockMvc.perform(get("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/projects/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void userManagementAndTokenLifecycleShouldWork() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(put("/api/users/" + bobId)
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("firstName", "Bobby").toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Bobby"));

        mockMvc.perform(delete("/api/users/" + bobId)
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isForbidden());

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("refreshToken", aliceRefreshToken).toString()))
            .andExpect(status().isOk())
            .andReturn();
        assertNotEquals(aliceRefreshToken, json(refreshed).get("refreshToken").asText());

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("refreshToken", aliceRefreshToken).toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    void ownerShouldBeAbleToDeleteProject() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("name", "Temp project").toString()))
            .andExpect(status().isOk())
            .andReturn();
        String tempId = json(created).get("id").asText();

        mockMvc.perform(delete("/api/projects/" + tempId)
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/" + tempId)
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isNotFound());
    }
}
