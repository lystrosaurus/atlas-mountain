package io.github.lystrosaurus.atlasmountain.cdc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import io.github.lystrosaurus.atlasmountain.cdc.engine.BinlogEngine;
import io.github.lystrosaurus.atlasmountain.cdc.engine.EmbeddedEngineExecutorService;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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

  @Autowired(required = false)
  private BinlogEngine binlogEngine;

  @Autowired(required = false)
  private EmbeddedEngineExecutorService executorService;

  @Test
  void beansAreRegisteredWhenCdcEnabled() {
    assertThat(binlogEngine).isNotNull();
    assertThat(executorService).isNotNull();
  }
}
