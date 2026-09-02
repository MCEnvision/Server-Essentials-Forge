package com.enviouse.sef.economy;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CommandCostSchedule {
    private final Map<String, Map<Component, BigDecimal>> costs;
    private final BigDecimal maximumCost;

    private CommandCostSchedule(
            Map<String, Map<Component, BigDecimal>> costs,
            BigDecimal maximumCost
    ) {
        Map<String, Map<Component, BigDecimal>> immutable = new LinkedHashMap<>();
        costs.forEach((action, components) -> immutable.put(action, Map.copyOf(components)));
        this.costs = Map.copyOf(immutable);
        this.maximumCost = Objects.requireNonNull(maximumCost, "maximumCost");
    }

    public static CommandCostSchedule empty() {
        return new CommandCostSchedule(Map.of(), BigDecimal.ZERO);
    }

    public static CommandCostSchedule parse(
            String input,
            int minorUnits,
            long maximumMinorUnits
    ) {
        String source = Objects.requireNonNullElse(input, "").strip();
        if (source.isEmpty()) {
            return empty();
        }
        Map<String, Map<Component, BigDecimal>> costs = new LinkedHashMap<>();
        int componentCount = 0;
        for (String rawEntry : source.split(",", -1)) {
            String entry = rawEntry.strip();
            int separator = entry.indexOf('=');
            if (separator < 1 || separator == entry.length() - 1) {
                throw new IllegalArgumentException("Command cost entry is invalid");
            }
            String key = entry.substring(0, separator).strip().toLowerCase(Locale.ROOT);
            int componentSeparator = key.indexOf('@');
            String action = componentSeparator < 0 ? key : key.substring(0, componentSeparator);
            if (!action.matches("[a-z0-9_.-]+:[a-z0-9_.-]+")) {
                throw new IllegalArgumentException("Command cost action id is invalid");
            }
            Component component = componentSeparator < 0
                    ? Component.FIXED
                    : Component.parse(key.substring(componentSeparator + 1));
            String amount = entry.substring(separator + 1).strip();
            long minor = EconomyMoney.parse(
                    amount,
                    minorUnits,
                    0L,
                    maximumMinorUnits,
                    false);
            BigDecimal major = EconomyMoney.toMajorUnits(minor, minorUnits);
            Map<Component, BigDecimal> components =
                    costs.computeIfAbsent(action, ignored -> new EnumMap<>(Component.class));
            if (components.putIfAbsent(component, major) != null) {
                throw new IllegalArgumentException("Command cost action component is duplicated");
            }
            componentCount++;
            if (componentCount > 1_024) {
                throw new IllegalArgumentException("Command cost entry limit exceeded");
            }
        }
        return new CommandCostSchedule(
                costs,
                EconomyMoney.toMajorUnits(maximumMinorUnits, minorUnits));
    }

    public BigDecimal cost(String actionId) {
        Map<Component, BigDecimal> components = costs.get(normalize(actionId));
        if (components == null) {
            return BigDecimal.ZERO;
        }
        return components.getOrDefault(Component.FIXED, BigDecimal.ZERO)
                .add(components.getOrDefault(Component.PER_USE, BigDecimal.ZERO));
    }

    public BigDecimal quote(
            String actionId,
            Map<String, String> normalizedParameters,
            List<UUID> targetIds
    ) {
        Objects.requireNonNull(normalizedParameters, "normalizedParameters");
        Objects.requireNonNull(targetIds, "targetIds");
        Map<Component, BigDecimal> components = costs.get(normalize(actionId));
        if (components == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = cost(actionId);
        result = addScaled(
                result,
                components.get(Component.PER_TARGET),
                Math.max(1L, targetIds.size()));
        result = addScaled(
                result,
                components.get(Component.PER_DISTANCE),
                metric(normalizedParameters, 0L, "distance", "distance_blocks"));
        result = addScaled(
                result,
                components.get(Component.PER_ITEM),
                metric(normalizedParameters, 1L, "amount", "quantity", "count", "items"));
        if (result.signum() < 0 || result.compareTo(maximumCost) > 0) {
            throw new IllegalArgumentException("Quoted command cost exceeds the configured transaction limit");
        }
        return result;
    }

    public Map<String, Map<Component, BigDecimal>> costs() {
        return costs;
    }

    public boolean configured(String actionId) {
        return costs.containsKey(normalize(actionId));
    }

    public String describe(String actionId, String currencySymbol) {
        Map<Component, BigDecimal> components = costs.get(normalize(actionId));
        if (components == null) {
            return "";
        }
        String symbol = Objects.requireNonNullElse(currencySymbol, "");
        return components.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey().label() + " " + symbol + entry.getValue().toPlainString())
                .collect(Collectors.joining(", "));
    }

    private static BigDecimal addScaled(BigDecimal current, BigDecimal rate, long units) {
        if (rate == null || units == 0L) {
            return current;
        }
        return current.add(rate.multiply(BigDecimal.valueOf(units)));
    }

    private static long metric(
            Map<String, String> parameters,
            long defaultValue,
            String... keys
    ) {
        for (String key : keys) {
            String value = parameters.get(key);
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                BigDecimal decimal = new BigDecimal(value.strip());
                if (decimal.signum() < 0) {
                    throw new IllegalArgumentException("Command cost metric cannot be negative");
                }
                return decimal.setScale(0, java.math.RoundingMode.CEILING).longValueExact();
            } catch (ArithmeticException | NumberFormatException exception) {
                throw new IllegalArgumentException("Command cost metric is invalid", exception);
            }
        }
        return defaultValue;
    }

    private static String normalize(String actionId) {
        return Objects.requireNonNull(actionId, "actionId").strip().toLowerCase(Locale.ROOT);
    }

    public enum Component {
        FIXED,
        PER_TARGET,
        PER_DISTANCE,
        PER_ITEM,
        PER_USE;

        private String label() {
            return switch (this) {
                case FIXED -> "fixed";
                case PER_TARGET -> "per target";
                case PER_DISTANCE -> "per block";
                case PER_ITEM -> "per item";
                case PER_USE -> "per use";
            };
        }

        private static Component parse(String value) {
            return switch (value) {
                case "fixed" -> FIXED;
                case "target", "per_target" -> PER_TARGET;
                case "distance", "per_distance" -> PER_DISTANCE;
                case "item", "per_item" -> PER_ITEM;
                case "use", "per_use" -> PER_USE;
                default -> throw new IllegalArgumentException("Command cost component is invalid");
            };
        }
    }
}
