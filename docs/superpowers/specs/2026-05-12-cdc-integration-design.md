# CDC Integration Design

## Overview

Integrate the CDC (Change Data Capture) capability from the `cerberus` project into `atlas-mountain` as an independent feature package. This allows atlas-mountain to listen to MySQL binlog events for cache invalidation, audit logging, and data synchronization.

## Architecture

CDC is an independent feature package, not part of the layered architecture (Controller -> Service -> DAO -> Mapper). It does not depend on business services and is triggered by database-level events.

```
io.github.lystrosaurus.atlasmountain.cdc
  config/          # CdcProperties, CdcConfig
  engine/          # BinlogEngine (BinaryLogClient wrapper)
  dispatcher/      # BinlogEventDispatcher (event parsing & routing)
  handler/         # BinlogEventHandler (interface), LoggingBinlogHandler (default)
  event/           # InsertEvent, UpdateEvent, DeleteEvent
```

## Components

### CdcProperties

Configuration properties for MySQL binlog connection:
- `cdc.enabled` — default `false`, must be explicitly enabled
- `cdc.host`, `cdc.port`, `cdc.username`, `cdc.password`
- `cdc.server-id` — unique replica ID to avoid replication conflicts
- `cdc.keep-alive`, `cdc.keep-alive-interval`, `cdc.heartbeat-interval`, `cdc.connect-timeout`

### BinlogEngine

Wraps `BinaryLogClient` from `mysql-binlog-connector-java`. Implements `Runnable`, managed by `EmbeddedEngineExecutorService` (`SmartLifecycle`).

### BinlogEventDispatcher

Implements `BinaryLogClient.EventListener`. Parses `TABLE_MAP`, `WRITE_ROWS`, `UPDATE_ROWS`, `DELETE_ROWS` events. Routes structured events to registered handlers.

### BinlogEventHandler (Interface)

```java
public interface BinlogEventHandler {
    void onInsert(InsertEvent event);
    void onUpdate(UpdateEvent event);
    void onDelete(DeleteEvent event);
    boolean supports(String database, String table);
}
```

Business code registers handlers as Spring beans. The dispatcher routes events to all matching handlers.

### Event Objects

Structured event objects carrying database, table, row data, and timestamp:
- `InsertEvent` — list of inserted rows
- `UpdateEvent` — list of before/after row pairs
- `DeleteEvent` — list of deleted rows

## Error Handling

- Binlog connection failures are logged and retried via `keep-alive`.
- Event parsing exceptions are caught per-event to avoid crashing the listener.
- Handler exceptions are caught per-handler so one failing handler does not block others.

## Testing

- Unit test for `BinlogEventDispatcher` with mock events.
- Spring Boot integration test verifying `CdcConfig` beans are registered when `cdc.enabled=true`.

## Dependencies

Add to `pom.xml`:
```xml
<dependency>
    <groupId>io.debezium</groupId>
    <artifactId>mysql-binlog-connector-java</artifactId>
    <version>0.40.3</version>
</dependency>
```
