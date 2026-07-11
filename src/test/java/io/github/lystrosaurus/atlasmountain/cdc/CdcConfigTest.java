package io.github.lystrosaurus.atlasmountain.cdc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.github.lystrosaurus.atlasmountain.cdc.engine.BinlogEngine;
import io.github.lystrosaurus.atlasmountain.cdc.engine.EmbeddedEngineExecutorService;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@MockitoBean(types = EmbeddedEngineExecutorService.class)
@TestPropertySource(
    properties = {
      "cdc.enabled=true",
      "cdc.host=localhost",
      "cdc.port=3306",
      "cdc.username=test",
      "cdc.password=test",
      "cdc.server-id=9999"
    })
class CdcConfigTest {

  private final BinlogEngine binlogEngine;
  private final EmbeddedEngineExecutorService executorService;

  @Autowired
  CdcConfigTest(BinlogEngine binlogEngine, EmbeddedEngineExecutorService executorService) {
    this.binlogEngine = binlogEngine;
    this.executorService = executorService;
  }

  @Test
  void beansAreRegisteredWhenCdcEnabled() {
    assertThat(binlogEngine).isNotNull();
    assertThat(executorService).isNotNull();
  }
}
