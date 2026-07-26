package com.enviouse.sef.kernel;

import java.util.Objects;
import java.util.Optional;

public record ActionResult<T>(
        boolean successful,
        ReasonCode reason,
        String detail,
        T value
) {
    public ActionResult {
        Objects.requireNonNull(reason, "reason");
        detail = detail == null ? "" : detail;
        if (detail.length() > 512) {
            detail = detail.substring(0, 512);
        }
    }

    public static <T> ActionResult<T> success(T value) {
        return new ActionResult<>(true, ReasonCode.SUCCESS, "", value);
    }

    public static <T> ActionResult<T> failure(ReasonCode reason, String detail) {
        if (reason == ReasonCode.SUCCESS) {
            throw new IllegalArgumentException("A failed result cannot use SUCCESS");
        }
        return new ActionResult<>(false, reason, detail, null);
    }

    public Optional<T> optionalValue() {
        return Optional.ofNullable(value);
    }

    public enum ReasonCode {
        SUCCESS,
        FEATURE_DISABLED,
        SOURCE_NOT_ALLOWED,
        PERMISSION_DENIED,
        TARGET_DENIED,
        TARGET_EXEMPT,
        HIERARCHY_DENIED,
        INVALID_INPUT,
        INVALID_DEFINITION,
        COOLDOWN_ACTIVE,
        WARMUP_ACTIVE,
        WARMUP_CANCELLED,
        CONFIRMATION_REQUIRED,
        CONFIRMATION_INVALID,
        CONFIRMATION_EXPIRED,
        COST_UNAVAILABLE,
        QUOTA_EXCEEDED,
        CONFLICT,
        NOT_FOUND,
        AMBIGUOUS,
        RECOVERY_MODE,
        STORAGE_ERROR,
        POLICY_DENIED,
        RECURSION_DENIED,
        PROVIDER_ERROR
    }
}
