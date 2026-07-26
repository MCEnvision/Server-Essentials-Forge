package com.enviouse.sef.vanish;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class VanishListProjection {
    private VanishListProjection() {
    }

    public static <T> List<T> visibleCopy(List<T> entries, Predicate<T> visible) {
        Objects.requireNonNull(entries, "entries");
        Objects.requireNonNull(visible, "visible");
        return entries.stream().filter(visible).toList();
    }
}
