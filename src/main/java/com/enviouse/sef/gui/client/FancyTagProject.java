package com.enviouse.sef.gui.client;

import com.enviouse.sef.fancytags.FancyTagObjectStore;
import com.enviouse.sef.fancytags.FancyTagProjectArchive;
import com.mojang.blaze3d.platform.NativeImage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class FancyTagProject {
    public static final int MAXIMUM_LAYERS = FancyTagProjectArchive.HARD_MAXIMUM_LAYERS;
    public static final int MAXIMUM_PALETTE_COLORS = 32;
    public static final int MAXIMUM_HISTORY_BYTES = 16 * 1024 * 1024;

    private final UUID id;
    private String name;
    private final int width;
    private final int height;
    private final List<Layer> layers = new ArrayList<>();
    private final List<Integer> palette = new ArrayList<>();
    private final Deque<Edit> undo = new ArrayDeque<>();
    private final Deque<Edit> redo = new ArrayDeque<>();
    private int activeLayer;
    private List<PixelChange> pending;
    private int historyBytes;
    private long revision = 1L;
    private boolean dirty = true;

    public FancyTagProject(UUID id, String name, int width, int height) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = boundedName(name);
        if (width < 1
                || height < 1
                || width > FancyTagObjectStore.HARD_MAXIMUM_WIDTH
                || height > FancyTagObjectStore.HARD_MAXIMUM_HEIGHT
                || (long) width * height > FancyTagObjectStore.HARD_MAXIMUM_PIXELS) {
            throw new IllegalArgumentException("tag project dimensions are outside bounds");
        }
        this.width = width;
        this.height = height;
        layers.add(new Layer("base", "Base", true, 255, new int[width * height]));
        palette.addAll(List.of(
                0xffffffff,
                0xff000000,
                0xffff5555,
                0xff55ff55,
                0xff5555ff,
                0xffffff55,
                0xffff55ff,
                0xff55ffff));
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void rename(String value) {
        String replacement = boundedName(value);
        if (!replacement.equals(name)) {
            name = replacement;
            changed();
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public long revision() {
        return revision;
    }

    public boolean dirty() {
        return dirty;
    }

    public void markSaved() {
        dirty = false;
    }

    public List<LayerView> layers() {
        List<LayerView> result = new ArrayList<>();
        for (int index = 0; index < layers.size(); index++) {
            Layer layer = layers.get(index);
            result.add(new LayerView(index, layer.id(), layer.name(), layer.visible(), layer.opacity()));
        }
        return List.copyOf(result);
    }

    public int activeLayer() {
        return activeLayer;
    }

    public void setActiveLayer(int index) {
        if (index < 0 || index >= layers.size()) {
            throw new IllegalArgumentException("tag project layer index is invalid");
        }
        activeLayer = index;
    }

    public boolean addLayer(String requestedName) {
        if (layers.size() >= MAXIMUM_LAYERS) {
            return false;
        }
        String displayName = boundedLayerName(requestedName);
        String base = displayName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isBlank()) {
            base = "layer";
        }
        String id = base.substring(0, Math.min(48, base.length()));
        int suffix = 2;
        while (containsLayer(id)) {
            id = base.substring(0, Math.min(48, base.length())) + "_" + suffix++;
        }
        layers.add(new Layer(id, displayName, true, 255, new int[width * height]));
        activeLayer = layers.size() - 1;
        clearHistory();
        changed();
        return true;
    }

    public boolean removeActiveLayer() {
        if (layers.size() <= 1) {
            return false;
        }
        layers.remove(activeLayer);
        activeLayer = Math.min(activeLayer, layers.size() - 1);
        clearHistory();
        changed();
        return true;
    }

    public void setLayerVisible(int index, boolean visible) {
        Layer current = layer(index);
        layers.set(index, current.withVisible(visible));
        changed();
    }

    public void setLayerOpacity(int index, int opacity) {
        if (opacity < 0 || opacity > 255) {
            throw new IllegalArgumentException("tag layer opacity is outside bounds");
        }
        Layer current = layer(index);
        layers.set(index, current.withOpacity(opacity));
        changed();
    }

    public List<Integer> palette() {
        return List.copyOf(palette);
    }

    public void setPaletteColor(int index, int color) {
        if (index < 0 || index >= MAXIMUM_PALETTE_COLORS) {
            throw new IllegalArgumentException("tag palette index is invalid");
        }
        while (palette.size() <= index) {
            palette.add(0x00000000);
        }
        palette.set(index, color);
        changed();
    }

    public void beginEdit() {
        if (pending == null) {
            pending = new ArrayList<>();
        }
    }

    public void setPixel(int x, int y, int color) {
        requirePoint(x, y);
        if (pending == null) {
            beginEdit();
        }
        Layer layer = layers.get(activeLayer);
        int index = y * width + x;
        int previous = layer.pixels()[index];
        if (previous == color) {
            return;
        }
        PixelChange existing = pending.stream()
                .filter(change -> change.layer() == activeLayer && change.index() == index)
                .findFirst()
                .orElse(null);
        if (existing == null) {
            pending.add(new PixelChange(activeLayer, index, previous, color));
        } else {
            pending.set(pending.indexOf(existing), new PixelChange(
                    existing.layer(),
                    existing.index(),
                    existing.before(),
                    color));
        }
        layer.pixels()[index] = color;
        dirty = true;
    }

    public void fill(int x, int y, int color) {
        requirePoint(x, y);
        beginEdit();
        Layer layer = layers.get(activeLayer);
        int target = layer.pixels()[y * width + x];
        if (target == color) {
            return;
        }
        boolean[] visited = new boolean[width * height];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(y * width + x);
        while (!queue.isEmpty()) {
            int index = queue.removeFirst();
            if (visited[index] || layer.pixels()[index] != target) {
                continue;
            }
            visited[index] = true;
            int pixelX = index % width;
            int pixelY = index / width;
            setPixel(pixelX, pixelY, color);
            if (pixelX > 0) {
                queue.add(index - 1);
            }
            if (pixelX + 1 < width) {
                queue.add(index + 1);
            }
            if (pixelY > 0) {
                queue.add(index - width);
            }
            if (pixelY + 1 < height) {
                queue.add(index + width);
            }
        }
        endEdit();
    }

    public void line(int fromX, int fromY, int toX, int toY, int color) {
        requirePoint(fromX, fromY);
        requirePoint(toX, toY);
        beginEdit();
        int x = fromX;
        int y = fromY;
        int dx = Math.abs(toX - fromX);
        int sx = fromX < toX ? 1 : -1;
        int dy = -Math.abs(toY - fromY);
        int sy = fromY < toY ? 1 : -1;
        int error = dx + dy;
        while (true) {
            setPixel(x, y, color);
            if (x == toX && y == toY) {
                break;
            }
            int doubled = error * 2;
            if (doubled >= dy) {
                error += dy;
                x += sx;
            }
            if (doubled <= dx) {
                error += dx;
                y += sy;
            }
        }
        endEdit();
    }

    public void rectangle(int fromX, int fromY, int toX, int toY, int color, boolean filled) {
        requirePoint(fromX, fromY);
        requirePoint(toX, toY);
        beginEdit();
        int minimumX = Math.min(fromX, toX);
        int maximumX = Math.max(fromX, toX);
        int minimumY = Math.min(fromY, toY);
        int maximumY = Math.max(fromY, toY);
        for (int y = minimumY; y <= maximumY; y++) {
            for (int x = minimumX; x <= maximumX; x++) {
                if (filled || x == minimumX || x == maximumX || y == minimumY || y == maximumY) {
                    setPixel(x, y, color);
                }
            }
        }
        endEdit();
    }

    public void rasterizeText(int x, int y, String text, int color) {
        Objects.requireNonNull(text, "text");
        if (text.length() > 64) {
            throw new IllegalArgumentException("tag project text is too long");
        }
        beginEdit();
        int cursor = x;
        for (int codePoint : text.toUpperCase(Locale.ROOT).codePoints().toArray()) {
            if (cursor + 4 >= width) {
                break;
            }
            int[] rows = glyph(codePoint);
            for (int row = 0; row < rows.length; row++) {
                for (int column = 0; column < 5; column++) {
                    if ((rows[row] & 1 << 4 - column) != 0
                            && cursor + column >= 0
                            && y + row >= 0
                            && cursor + column < width
                            && y + row < height) {
                        setPixel(cursor + column, y + row, color);
                    }
                }
            }
            cursor += 6;
        }
        endEdit();
    }

    public void endEdit() {
        if (pending == null) {
            return;
        }
        if (!pending.isEmpty()) {
            Edit edit = new Edit(List.copyOf(pending));
            undo.addLast(edit);
            historyBytes = Math.addExact(historyBytes, edit.bytes());
            redo.clear();
            while (undo.size() > FancyTagProjectArchive.HARD_MAXIMUM_HISTORY
                    || historyBytes > MAXIMUM_HISTORY_BYTES) {
                Edit removed = undo.removeFirst();
                historyBytes -= removed.bytes();
            }
            changed();
        }
        pending = null;
    }

    public boolean undo() {
        endEdit();
        Edit edit = undo.pollLast();
        if (edit == null) {
            return false;
        }
        for (PixelChange change : edit.changes()) {
            layers.get(change.layer()).pixels()[change.index()] = change.before();
        }
        redo.addLast(edit);
        historyBytes -= edit.bytes();
        changed();
        return true;
    }

    public boolean redo() {
        endEdit();
        Edit edit = redo.pollLast();
        if (edit == null) {
            return false;
        }
        for (PixelChange change : edit.changes()) {
            layers.get(change.layer()).pixels()[change.index()] = change.after();
        }
        undo.addLast(edit);
        historyBytes += edit.bytes();
        changed();
        return true;
    }

    public NativeImage flatten() {
        NativeImage image = new NativeImage(width, height, true);
        int[] flattened = flattenedPixels();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setPixelRGBA(x, y, flattened[y * width + x]);
            }
        }
        return image;
    }

    public int[] flattenedPixels() {
        int[] result = new int[width * height];
        for (Layer layer : layers) {
            if (!layer.visible() || layer.opacity() == 0) {
                continue;
            }
            for (int index = 0; index < result.length; index++) {
                result[index] = blend(result[index], layer.pixels()[index], layer.opacity());
            }
        }
        return result;
    }

    int[] layerPixels(int index) {
        return Arrays.copyOf(layer(index).pixels(), width * height);
    }

    void replaceLayers(List<LoadedLayer> replacements, int selectedLayer) {
        if (replacements.isEmpty() || replacements.size() > MAXIMUM_LAYERS) {
            throw new IllegalArgumentException("tag project layers are outside bounds");
        }
        layers.clear();
        for (LoadedLayer replacement : replacements) {
            if (replacement.pixels().length != width * height || containsLayer(replacement.id())) {
                throw new IllegalArgumentException("tag project layer data is invalid");
            }
            layers.add(new Layer(
                    replacement.id(),
                    boundedLayerName(replacement.name()),
                    replacement.visible(),
                    replacement.opacity(),
                    Arrays.copyOf(replacement.pixels(), replacement.pixels().length)));
        }
        activeLayer = Math.clamp(selectedLayer, 0, layers.size() - 1);
        clearHistory();
        changed();
    }

    private void clearHistory() {
        undo.clear();
        redo.clear();
        pending = null;
        historyBytes = 0;
    }

    private Layer layer(int index) {
        if (index < 0 || index >= layers.size()) {
            throw new IllegalArgumentException("tag project layer index is invalid");
        }
        return layers.get(index);
    }

    private boolean containsLayer(String id) {
        return layers.stream().anyMatch(layer -> layer.id().equals(id));
    }

    private void requirePoint(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException("tag project pixel is outside the canvas");
        }
    }

    private void changed() {
        revision = Math.addExact(revision, 1L);
        dirty = true;
    }

    private static int blend(int below, int above, int opacity) {
        int sourceAlpha = (above >>> 24) * opacity / 255;
        if (sourceAlpha == 0) {
            return below;
        }
        int destinationAlpha = below >>> 24;
        int outputAlpha = sourceAlpha + destinationAlpha * (255 - sourceAlpha) / 255;
        if (outputAlpha == 0) {
            return 0;
        }
        int sourceRed = above >>> 16 & 0xff;
        int sourceGreen = above >>> 8 & 0xff;
        int sourceBlue = above & 0xff;
        int destinationRed = below >>> 16 & 0xff;
        int destinationGreen = below >>> 8 & 0xff;
        int destinationBlue = below & 0xff;
        int red = (sourceRed * sourceAlpha
                + destinationRed * destinationAlpha * (255 - sourceAlpha) / 255) / outputAlpha;
        int green = (sourceGreen * sourceAlpha
                + destinationGreen * destinationAlpha * (255 - sourceAlpha) / 255) / outputAlpha;
        int blue = (sourceBlue * sourceAlpha
                + destinationBlue * destinationAlpha * (255 - sourceAlpha) / 255) / outputAlpha;
        return outputAlpha << 24 | red << 16 | green << 8 | blue;
    }

    private static String boundedName(String value) {
        String result = Objects.requireNonNullElse(value, "").strip();
        if (result.isBlank() || result.length() > 64 || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("tag project name is invalid");
        }
        return result;
    }

    private static String boundedLayerName(String value) {
        String result = Objects.requireNonNullElse(value, "Layer").strip();
        if (result.isBlank() || result.length() > 32 || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("tag project layer name is invalid");
        }
        return result;
    }

    private static int[] glyph(int codePoint) {
        return switch (codePoint) {
            case 'A' -> new int[]{14, 17, 17, 31, 17, 17, 17};
            case 'B' -> new int[]{30, 17, 17, 30, 17, 17, 30};
            case 'C' -> new int[]{14, 17, 16, 16, 16, 17, 14};
            case 'D' -> new int[]{30, 17, 17, 17, 17, 17, 30};
            case 'E' -> new int[]{31, 16, 16, 30, 16, 16, 31};
            case 'F' -> new int[]{31, 16, 16, 30, 16, 16, 16};
            case 'G' -> new int[]{14, 17, 16, 23, 17, 17, 14};
            case 'H' -> new int[]{17, 17, 17, 31, 17, 17, 17};
            case 'I' -> new int[]{14, 4, 4, 4, 4, 4, 14};
            case 'J' -> new int[]{7, 2, 2, 2, 18, 18, 12};
            case 'K' -> new int[]{17, 18, 20, 24, 20, 18, 17};
            case 'L' -> new int[]{16, 16, 16, 16, 16, 16, 31};
            case 'M' -> new int[]{17, 27, 21, 21, 17, 17, 17};
            case 'N' -> new int[]{17, 25, 21, 19, 17, 17, 17};
            case 'O' -> new int[]{14, 17, 17, 17, 17, 17, 14};
            case 'P' -> new int[]{30, 17, 17, 30, 16, 16, 16};
            case 'Q' -> new int[]{14, 17, 17, 17, 21, 18, 13};
            case 'R' -> new int[]{30, 17, 17, 30, 20, 18, 17};
            case 'S' -> new int[]{15, 16, 16, 14, 1, 1, 30};
            case 'T' -> new int[]{31, 4, 4, 4, 4, 4, 4};
            case 'U' -> new int[]{17, 17, 17, 17, 17, 17, 14};
            case 'V' -> new int[]{17, 17, 17, 17, 17, 10, 4};
            case 'W' -> new int[]{17, 17, 17, 21, 21, 21, 10};
            case 'X' -> new int[]{17, 17, 10, 4, 10, 17, 17};
            case 'Y' -> new int[]{17, 17, 10, 4, 4, 4, 4};
            case 'Z' -> new int[]{31, 1, 2, 4, 8, 16, 31};
            case '0' -> new int[]{14, 17, 19, 21, 25, 17, 14};
            case '1' -> new int[]{4, 12, 4, 4, 4, 4, 14};
            case '2' -> new int[]{14, 17, 1, 2, 4, 8, 31};
            case '3' -> new int[]{30, 1, 1, 14, 1, 1, 30};
            case '4' -> new int[]{2, 6, 10, 18, 31, 2, 2};
            case '5' -> new int[]{31, 16, 16, 30, 1, 1, 30};
            case '6' -> new int[]{14, 16, 16, 30, 17, 17, 14};
            case '7' -> new int[]{31, 1, 2, 4, 8, 8, 8};
            case '8' -> new int[]{14, 17, 17, 14, 17, 17, 14};
            case '9' -> new int[]{14, 17, 17, 15, 1, 1, 14};
            case ' ' -> new int[]{0, 0, 0, 0, 0, 0, 0};
            default -> new int[]{31, 17, 5, 4, 0, 4, 0};
        };
    }

    public record LayerView(int index, String id, String name, boolean visible, int opacity) {
    }

    record LoadedLayer(String id, String name, boolean visible, int opacity, int[] pixels) {
    }

    private record Layer(String id, String name, boolean visible, int opacity, int[] pixels) {
        private Layer withVisible(boolean replacement) {
            return new Layer(id, name, replacement, opacity, pixels);
        }

        private Layer withOpacity(int replacement) {
            return new Layer(id, name, visible, replacement, pixels);
        }
    }

    private record PixelChange(int layer, int index, int before, int after) {
    }

    private record Edit(List<PixelChange> changes) {
        private int bytes() {
            return Math.multiplyExact(changes.size(), 16);
        }
    }
}
