package com.enviouse.sef.message;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.kernel.ActionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageService {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-z][a-z0-9_.-]{0,63})}");
    private static final int MAXIMUM_TEMPLATE_LENGTH = 4096;
    private static final int MAXIMUM_RENDERED_LENGTH = 16_384;

    public ActionResult<Template> compile(String source, Set<String> allowedPlaceholders) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(allowedPlaceholders, "allowedPlaceholders");
        if (allowedPlaceholders.size() > 64) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "too many message placeholders");
        }
        Set<String> allowed = allowedPlaceholders.stream()
                .map(MessageService::normalize)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (source.length() > MAXIMUM_TEMPLATE_LENGTH) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "message template exceeds size limit");
        }

        List<Segment> segments = new ArrayList<>();
        Set<String> used = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(source);
        int cursor = 0;
        while (matcher.find()) {
            if (matcher.start() > cursor) {
                segments.add(new Literal(source.substring(cursor, matcher.start())));
            }
            String key = normalize(matcher.group(1));
            if (!allowed.contains(key)) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "unknown placeholder " + key);
            }
            segments.add(new Placeholder(key));
            used.add(key);
            cursor = matcher.end();
        }
        if (cursor < source.length()) {
            segments.add(new Literal(source.substring(cursor)));
        }
        return ActionResult.success(new Template(source, List.copyOf(segments), Set.copyOf(used)));
    }

    public ActionResult<Component> render(Template template, Map<String, Component> values) {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(values, "values");
        if (!values.keySet().stream().map(MessageService::normalize).collect(java.util.stream.Collectors.toSet())
                .containsAll(template.placeholders())) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "message value is missing");
        }

        MutableComponent rendered = Component.empty();
        int estimatedLength = 0;
        for (Segment segment : template.segments()) {
            if (segment instanceof Literal literal) {
                estimatedLength += literal.value().length();
                rendered.append(TextFormatter.stringToFormattedText(literal.value()));
            } else if (segment instanceof Placeholder placeholder) {
                Component value = values.entrySet().stream()
                        .filter(entry -> normalize(entry.getKey()).equals(placeholder.key()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(Component.empty());
                estimatedLength += value.getString().length();
                rendered.append(value.copy());
            }
            if (estimatedLength > MAXIMUM_RENDERED_LENGTH) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "rendered message exceeds size limit");
            }
        }
        return ActionResult.success(rendered);
    }

    public record Template(String source, List<Segment> segments, Set<String> placeholders) {
        public Template {
            source = Objects.requireNonNull(source, "source");
            segments = List.copyOf(Objects.requireNonNull(segments, "segments"));
            placeholders = Set.copyOf(Objects.requireNonNull(placeholders, "placeholders"));
        }
    }

    public sealed interface Segment permits Literal, Placeholder {
    }

    public record Literal(String value) implements Segment {
        public Literal {
            value = Objects.requireNonNull(value, "value");
        }
    }

    public record Placeholder(String key) implements Segment {
        public Placeholder {
            key = normalize(key);
        }
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }
}
