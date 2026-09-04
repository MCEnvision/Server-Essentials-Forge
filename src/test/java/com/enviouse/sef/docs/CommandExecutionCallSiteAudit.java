package com.enviouse.sef.docs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds command executor callsites in production sources without treating
 * static source discovery as proof that an action effect was executed.
 */
final class CommandExecutionCallSiteAudit {
    private static final Pattern EXECUTE = Pattern.compile("KernelCommandExecutor\\.execute\\s*\\(");
    private static final Pattern STRING_LITERAL = Pattern.compile("^\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"$");

    private CommandExecutionCallSiteAudit() {
    }

    static Report scan(Path sourceRoot, Set<String> actionIds) {
        Objects.requireNonNull(sourceRoot, "sourceRoot");
        Objects.requireNonNull(actionIds, "actionIds");
        Path resolvedSourceRoot = resolveSourceRoot(sourceRoot);
        if (resolvedSourceRoot == null) {
            return new Report(List.of(), Map.of(), List.of(), List.of());
        }

        List<CallSite> callSites = new ArrayList<>();
        try (var paths = Files.walk(resolvedSourceRoot)) {
            paths.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .sorted()
                    .forEach(path -> scanFile(resolvedSourceRoot, path, actionIds, callSites));
        } catch (IOException exception) {
            throw new IllegalStateException("command executor source audit could not read sources", exception);
        }

        callSites.sort(Comparator.comparing(CallSite::sourceLocation));
        Map<String, List<String>> literalLocations = new LinkedHashMap<>();
        List<String> unknownLiterals = new ArrayList<>();
        List<String> dynamicLocations = new ArrayList<>();
        for (CallSite callSite : callSites) {
            if (callSite.actionId().isBlank()) {
                dynamicLocations.add(callSite.sourceLocation());
            } else if (!actionIds.contains(callSite.actionId())) {
                unknownLiterals.add(callSite.actionId() + " at " + callSite.sourceLocation());
            } else {
                literalLocations.computeIfAbsent(callSite.actionId(), ignored -> new ArrayList<>())
                        .add(callSite.sourceLocation());
            }
        }
        literalLocations.replaceAll((ignored, locations) -> List.copyOf(locations));
        return new Report(
                List.copyOf(callSites),
                Map.copyOf(literalLocations),
                List.copyOf(new LinkedHashSet<>(unknownLiterals)),
                List.copyOf(dynamicLocations));
    }

    private static Path resolveSourceRoot(Path requested) {
        List<Path> candidates = new ArrayList<>();
        candidates.add(requested.toAbsolutePath().normalize());
        Path current = requested.toAbsolutePath().normalize();
        while (current != null) {
            candidates.add(current.resolve("src/main/java").normalize());
            current = current.getParent();
        }
        String projectDirectory = System.getProperty("projectDir", "").trim();
        if (!projectDirectory.isEmpty()) {
            candidates.add(Path.of(projectDirectory).resolve(requested).toAbsolutePath().normalize());
        }
        try {
            Path codeSource = Path.of(CommandExecutionCallSiteAudit.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            Path codeSourceCurrent = Files.isDirectory(codeSource) ? codeSource : codeSource.getParent();
            while (codeSourceCurrent != null) {
                candidates.add(codeSourceCurrent.resolve(requested).toAbsolutePath().normalize());
                codeSourceCurrent = codeSourceCurrent.getParent();
            }
        } catch (Exception ignored) {
            // The source audit remains optional when the test runtime hides its code source.
        }
        return candidates.stream().filter(Files::isDirectory).findFirst().orElse(null);
    }

    private static void scanFile(
            Path sourceRoot,
            Path path,
            Set<String> actionIds,
            List<CallSite> callSites
    ) {
        String source;
        try {
            source = Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("command executor source audit could not read " + path, exception);
        }
        Matcher matcher = EXECUTE.matcher(source);
        while (matcher.find()) {
            String expression = secondArgument(source, matcher.end())
                    .orElseThrow(() -> new IllegalStateException(
                            "command executor call has no action argument at " + location(sourceRoot, path, source, matcher.start())));
            String actionId = literal(expression);
            callSites.add(new CallSite(
                    location(sourceRoot, path, source, matcher.start()),
                    expression,
                    actionId));
        }
    }

    private static java.util.Optional<String> secondArgument(String source, int openEnd) {
        int depth = 1;
        int segmentStart = openEnd;
        int index = openEnd;
        boolean quoted = false;
        boolean escaped = false;
        boolean lineComment = false;
        boolean blockComment = false;
        List<String> arguments = new ArrayList<>();
        while (index < source.length()) {
            char value = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : 0;
            if (lineComment) {
                if (value == '\n' || value == '\r') {
                    lineComment = false;
                }
            } else if (blockComment) {
                if (value == '*' && next == '/') {
                    blockComment = false;
                    index++;
                }
            } else if (quoted) {
                if (escaped) {
                    escaped = false;
                } else if (value == '\\') {
                    escaped = true;
                } else if (value == '"') {
                    quoted = false;
                }
            } else if (value == '/' && next == '/') {
                lineComment = true;
                index++;
            } else if (value == '/' && next == '*') {
                blockComment = true;
                index++;
            } else if (value == '"') {
                quoted = true;
            } else if (value == '(' || value == '[' || value == '{') {
                depth++;
            } else if (value == ')' || value == ']' || value == '}') {
                depth--;
                if (depth == 0) {
                    arguments.add(source.substring(segmentStart, index).strip());
                    break;
                }
            } else if (value == ',' && depth == 1) {
                arguments.add(source.substring(segmentStart, index).strip());
                segmentStart = index + 1;
            }
            index++;
        }
        return arguments.size() > 1
                ? java.util.Optional.of(arguments.get(1))
                : java.util.Optional.empty();
    }

    private static String literal(String expression) {
        Matcher matcher = STRING_LITERAL.matcher(expression);
        if (!matcher.matches()) {
            return "";
        }
        String value = matcher.group(1);
        StringBuilder result = new StringBuilder(value.length());
        boolean escaped = false;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (escaped) {
                result.append(switch (character) {
                    case '"' -> '"';
                    case '\\' -> '\\';
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    default -> character;
                });
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else {
                result.append(character);
            }
        }
        if (escaped) {
            return "";
        }
        return result.toString().trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static String location(Path sourceRoot, Path path, String source, int offset) {
        int line = 1;
        for (int index = 0; index < offset; index++) {
            if (source.charAt(index) == '\n') {
                line++;
            }
        }
        return sourceRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/")
                + ":" + line;
    }

    record CallSite(String sourceLocation, String actionExpression, String actionId) {
        CallSite {
            sourceLocation = Objects.requireNonNull(sourceLocation, "sourceLocation");
            actionExpression = Objects.requireNonNull(actionExpression, "actionExpression");
            actionId = Objects.requireNonNullElse(actionId, "");
        }
    }

    record Report(
            List<CallSite> callSites,
            Map<String, List<String>> literalLocations,
            List<String> unknownLiteralActions,
            List<String> dynamicLocations
    ) {
        Report {
            callSites = List.copyOf(callSites);
            literalLocations = Map.copyOf(literalLocations);
            unknownLiteralActions = List.copyOf(unknownLiteralActions);
            dynamicLocations = List.copyOf(dynamicLocations);
        }
    }
}
