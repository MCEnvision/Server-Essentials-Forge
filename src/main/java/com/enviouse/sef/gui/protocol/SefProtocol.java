package com.enviouse.sef.gui.protocol;

import java.util.EnumSet;
import java.util.Set;

public final class SefProtocol {
    public static final String CHANNEL_VERSION = "1";
    public static final int MAJOR = 1;
    public static final int MINOR = 0;
    public static final int MAXIMUM_PANEL_ENTRIES = 100;
    public static final int MAXIMUM_HUD_TILES = 16;
    public static final int MAXIMUM_IDENTITY_PROJECTIONS = 256;
    public static final int MAXIMUM_IDENTITY_COMPONENT_BYTES = 8_192;
    public static final int MAXIMUM_TAG_BYTES = 1_048_576;
    public static final long SERVER_FEATURES = Feature.mask(EnumSet.allOf(Feature.class));

    private SefProtocol() {
    }

    public static boolean compatible(int major) {
        return major == MAJOR;
    }

    public enum Feature {
        DASHBOARD(0),
        HOMES(1),
        WARPS(2),
        TELEPORT_REQUESTS(3),
        HELP_DIAGNOSTICS(4),
        STAFF_OVERVIEW(5),
        HUD(6),
        PAUSE_BUTTON(7),
        FANCY_TAGS_STATIC(8),
        IDENTITY_PROJECTION(9),
        UNIVERSAL_GUI(10),
        PANEL_EDITOR(11);

        private final int bit;

        Feature(int bit) {
            this.bit = bit;
        }

        public long flag() {
            return 1L << bit;
        }

        public boolean present(long mask) {
            return (mask & flag()) != 0L;
        }

        public static long mask(Set<Feature> features) {
            long result = 0L;
            for (Feature feature : features) {
                result |= feature.flag();
            }
            return result;
        }
    }
}
