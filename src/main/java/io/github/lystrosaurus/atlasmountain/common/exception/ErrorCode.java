package io.github.lystrosaurus.atlasmountain.common.exception;

import java.io.Serializable;

public interface ErrorCode extends Serializable {

  String code();

  String message();
}
