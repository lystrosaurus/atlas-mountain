package io.github.lystrosaurus.atlasmountain.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public abstract class MockMvcIntegrationTest extends IntegrationTestBase {
    protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbcTemplate;
    @Autowired private WebApplicationContext context;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
}
