package com.vanopt.logistics.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OptimizationControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void postOptimize_returns200_withSelectedShipments() {
        Map<String, Object> body = Map.of(
                "maxVolume", 15,
                "availableShipments", List.of(
                        Map.of("name", "Parcel A", "volume", 5, "revenue", 120),
                        Map.of("name", "Parcel B", "volume", 10, "revenue", 200),
                        Map.of("name", "Parcel C", "volume", 3, "revenue", 80),
                        Map.of("name", "Parcel D", "volume", 8, "revenue", 160)
                ));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/optimize", new HttpEntity<>(body, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("requestId")).isNotNull();
        assertThat((List<?>) response.getBody().get("selectedShipments")).hasSize(2);
        assertThat(response.getBody().get("totalVolume")).isEqualTo(15);
        assertThat(response.getBody().get("totalRevenue")).isEqualTo(320);
        assertThat(response.getBody().get("createdAt")).isNotNull();
    }

    @Test
    void postOptimize_noValidCombination_returns200_emptySelection() {
        Map<String, Object> body = Map.of(
                "maxVolume", 2,
                "availableShipments", List.of(Map.of("name", "Large", "volume", 10, "revenue", 100)));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/optimize", new HttpEntity<>(body, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("requestId")).isNotNull();
        assertThat((List<?>) response.getBody().get("selectedShipments")).isEmpty();
        assertThat(response.getBody().get("totalVolume")).isEqualTo(0);
        assertThat(response.getBody().get("totalRevenue")).isEqualTo(0);
    }

    @Test
    void postOptimize_invalidInput_returns400() {
        Map<String, Object> body = Map.of("maxVolume", -1, "availableShipments", List.of());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(
                    baseUrl() + "/api/v1/optimize", new HttpEntity<>(body, headers), Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            return;
        }
        throw new AssertionError("Expected 400 BAD_REQUEST");
    }

    @Test
    void getByRequestId_afterPost_returnsSameResult() {
        Map<String, Object> body = Map.of(
                "maxVolume", 15,
                "availableShipments", List.of(
                        Map.of("name", "Parcel A", "volume", 5, "revenue", 120),
                        Map.of("name", "Parcel B", "volume", 10, "revenue", 200)
                ));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> postResponse = restTemplate.postForEntity(
                baseUrl() + "/api/v1/optimize", new HttpEntity<>(body, headers), Map.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String requestId = postResponse.getBody().get("requestId").toString();

        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl() + "/api/v1/optimizations/" + requestId, Map.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().get("requestId")).isEqualTo(requestId);
        assertThat((List<?>) getResponse.getBody().get("selectedShipments")).hasSize(2);
        assertThat(getResponse.getBody().get("totalVolume")).isEqualTo(15);
        assertThat(getResponse.getBody().get("totalRevenue")).isEqualTo(320);
    }

    @Test
    void getByRequestId_notFound_returns404() {
        try {
            restTemplate.getForEntity(
                    baseUrl() + "/api/v1/optimizations/00000000-0000-0000-0000-000000000000", Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            return;
        }
        throw new AssertionError("Expected 404 NOT_FOUND");
    }

    @Test
    void getAll_returnsPaginatedList() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/api/v1/optimizations?page=0&size=5", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
        assertThat(response.getBody().get("totalElements")).isNotNull();
        assertThat(response.getBody().get("totalPages")).isNotNull();
    }
}
