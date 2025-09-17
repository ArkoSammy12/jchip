package io.github.arkosammy12.jchip.util;

import picocli.CommandLine;

public enum DisplayAngle {
    DEG_0("0"),
    DEG_90("90"),
    DEG_180("180"),
    DEG_270("270");

    private final String identifier;

    DisplayAngle(String identifier) {
        this.identifier = identifier;
    }

    public static DisplayAngle getDisplayAngleForIdentifier(String identifier) {
        for (DisplayAngle displayAngle : DisplayAngle.values()) {
            if (displayAngle.identifier.equals(identifier)) {
                return displayAngle;
            }
        }
        throw new IllegalArgumentException("Invalid display angle value: " + identifier + "!");
    }

    public static class Converter implements CommandLine.ITypeConverter<DisplayAngle> {

        @Override
        public DisplayAngle convert(String value) {
            return getDisplayAngleForIdentifier(value);
        }
    }

}
