package io.github.arkosammy12.jchip.util;

import picocli.CommandLine;

public enum DisplayAngle {
    DEG_0("No rotation", 0, "0"),
    DEG_90("90 degrees", 90, "90"),
    DEG_180("180 degrees", 180, "180"),
    DEG_270("270 degrees", 270, "270");

    private final String displayName;
    private final String identifier;
    private final int intValue;

    DisplayAngle(String displayName, int intValue, String identifier) {
        this.displayName = displayName;
        this.intValue = intValue;
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

    public static DisplayAngle getDisplayAngleForIntValue(int intValue) {
        for (DisplayAngle displayAngle : DisplayAngle.values()) {
            if (intValue == displayAngle.intValue) {
                return displayAngle;
            }
        }
        throw new IllegalArgumentException("Invalid display angle value: " + intValue + "!");
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static class Converter implements CommandLine.ITypeConverter<DisplayAngle> {

        @Override
        public DisplayAngle convert(String value) {
            return getDisplayAngleForIdentifier(value);
        }

    }

}
