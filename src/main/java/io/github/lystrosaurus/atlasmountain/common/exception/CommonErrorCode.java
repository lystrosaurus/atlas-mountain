package io.github.lystrosaurus.atlasmountain.common.exception;

public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST("COMMON_400", "bad request"),
    UNAUTHORIZED("COMMON_401", "unauthorized"),
    FORBIDDEN("COMMON_403", "forbidden"),
    NOT_FOUND("COMMON_404", "not found"),
    CONFLICT("COMMON_409", "conflict"),
    INTERNAL_ERROR("COMMON_500", "internal server error"),
    LOCK_BUSY("LOCK_409", "resource is busy");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
