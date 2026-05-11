package io.github.lystrosaurus.atlasmountain.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

public abstract class RestTemplateIntegrationTest extends IntegrationTestBase {
    protected final RestTemplate restTemplate = new RestTemplate();
    @LocalServerPort protected int port;
    @Autowired protected JdbcTemplate jdbcTemplate;

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }
}
