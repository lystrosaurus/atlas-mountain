package io.github.lystrosaurus.atlasmountain.cdc.engine;

import java.io.IOException;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

import io.github.lystrosaurus.atlasmountain.cdc.config.CdcProperties;
import io.github.lystrosaurus.atlasmountain.cdc.dispatcher.BinlogEventDispatcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinlogEngine implements Runnable {

  private BinaryLogClient client;
  private final CdcProperties cdcProperties;
  private final BinlogEventDispatcher dispatcher;

  public BinlogEngine(CdcProperties cdcProperties, BinlogEventDispatcher dispatcher) {
    this.cdcProperties = cdcProperties;
    this.dispatcher = dispatcher;
  }

  @Override
  public void run() {
    log.info(
        "Starting binlog client (serverId={}) connecting to {}:{}",
        cdcProperties.getServerId(),
        cdcProperties.getHost(),
        cdcProperties.getPort());
    client =
        new BinaryLogClient(
            cdcProperties.getHost(),
            cdcProperties.getPort(),
            cdcProperties.getUsername(),
            cdcProperties.getPassword());
    client.registerEventListener(dispatcher);
    client.setKeepAlive(cdcProperties.isKeepAlive());
    client.setKeepAliveInterval(cdcProperties.getKeepAliveInterval());
    client.setHeartbeatInterval(cdcProperties.getHeartbeatInterval());
    client.setConnectTimeout(cdcProperties.getConnectTimeout());
    client.setServerId(cdcProperties.getServerId());
    try {
      client.connect();
    } catch (IOException e) {
      log.error("Binlog connection failed", e);
    }
  }

  public void close() throws IOException {
    log.info("Shutting down binlog engine");
    if (client != null) {
      client.disconnect();
    }
  }
}
