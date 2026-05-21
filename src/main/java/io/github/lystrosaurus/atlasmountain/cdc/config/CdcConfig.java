package io.github.lystrosaurus.atlasmountain.cdc.config;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.lystrosaurus.atlasmountain.cdc.dispatcher.BinlogEventDispatcher;
import io.github.lystrosaurus.atlasmountain.cdc.engine.BinlogEngine;
import io.github.lystrosaurus.atlasmountain.cdc.engine.EmbeddedEngineExecutorService;
import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;

@Configuration
@EnableConfigurationProperties(CdcProperties.class)
@ConditionalOnProperty(prefix = "cdc", name = "enabled", havingValue = "true")
public class CdcConfig {

  @Bean
  public AtomicBoolean cdcRunning() {
    return new AtomicBoolean(false);
  }

  @Bean
  public BinlogEngine binlogEngine(
      CdcProperties cdcProperties, BinlogEventDispatcher dispatcher, AtomicBoolean cdcRunning) {
    return new BinlogEngine(cdcProperties, dispatcher, cdcRunning);
  }

  @Bean
  public EmbeddedEngineExecutorService embeddedEngineExecutorService(
      BinlogEngine engine, AtomicBoolean cdcRunning) {
    return new EmbeddedEngineExecutorService(engine, cdcRunning);
  }

  @Bean
  public BinlogEventDispatcher binlogEventDispatcher(List<BinlogEventHandler> handlers) {
    return new BinlogEventDispatcher(handlers);
  }
}
