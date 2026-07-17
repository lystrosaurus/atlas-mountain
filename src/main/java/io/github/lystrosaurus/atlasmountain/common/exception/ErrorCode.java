package io.github.lystrosaurus.atlasmountain.common.exception;

import java.io.Serializable;

public interface ErrorCode extends Serializable {

  String code();

  String message();

  /**
   * HTTP status code that should be returned when this error is surfaced via the API. Embedding it
   * in the error code avoids a fragile string-switch in the global exception handler and forces
   * every new error code to declare its HTTP semantics explicitly.
   */
  int httpStatus();
}
