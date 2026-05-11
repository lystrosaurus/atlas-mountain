package io.github.lystrosaurus.atlasmountain.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class RestTemplateIntegrationTest extends IntegrationTestBase {
    @Autowired protected TestRestTemplate restTemplate;
    @LocalServerPort protected int port;
    @Autowired protected JdbcTemplate jdbcTemplate;
}
