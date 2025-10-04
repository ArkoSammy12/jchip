package io.github.arkosammy12.jchip.util;

import picocli.CommandLine;

public enum KeyboardLayout {
    QWERTY("qwerty"),
    DVORAK("dvorak"),
    AZERTY("azerty"),
    COLEMAK("colemak");

    private final String identifier;

    KeyboardLayout(String identifier) {
        this.identifier = identifier;
    }

    public static KeyboardLayout getKeyboardLayoutForIdentifier(String identifier) {
        for (KeyboardLayout keyboardLayout : KeyboardLayout.values()) {
            if (keyboardLayout.identifier.equals(identifier)) {
                return keyboardLayout;
            }
        }
        throw new IllegalArgumentException("Unknown keyboard layout: " + identifier + "!");
    }

    public int getKeypadHexForChar(char c) {
        char pressed = Character.toLowerCase(c);
        return switch (this) {
            case QWERTY -> getKeypadHexForCharQWERTY(pressed);
            case DVORAK -> getKeypadHexForCharDVORAK(pressed);
            case AZERTY -> getKeypadHexForCharAZERTY(pressed);
            case COLEMAK -> getKeypadHexForCharCOLEMAK(pressed);
        };
    }

    private static int getKeypadHexForCharQWERTY(char c) {
        return switch (c) {
            case 'x' -> 0x0;
            case '1' -> 0x1;
            case '2' -> 0x2;
            case '3' -> 0x3;
            case 'q' -> 0x4;
            case 'w' -> 0x5;
            case 'e' -> 0x6;
            case 'a' -> 0x7;
            case 's' -> 0x8;
            case 'd' -> 0x9;
            case 'z' -> 0xA;
            case 'c' -> 0xB;
            case '4' -> 0xC;
            case 'r' -> 0xD;
            case 'f' -> 0xE;
            case 'v' -> 0xF;
            default -> -1;
        };
    }

    private static int getKeypadHexForCharDVORAK(char c) {
        return switch (c) {
            case 'x' -> 0x0;
            case '1' -> 0x1;
            case '2' -> 0x2;
            case '3' -> 0x3;
            case '\'' -> 0x4;
            case ',' -> 0x5;
            case '.' -> 0x6;
            case 'a' -> 0x7;
            case 'o' -> 0x8;
            case 'e' -> 0x9;
            case ';' -> 0xA;
            case 'q' -> 0xB;
            case '4' -> 0xC;
            case 'p' -> 0xD;
            case 'u' -> 0xE;
            case 'k' -> 0xF;
            default -> -1;
        };
    }

    private static int getKeypadHexForCharAZERTY(char c) {
        return switch (c) {
            case 'x' -> 0x0;
            case '1' -> 0x1;
            case '2' -> 0x2;
            case '3' -> 0x3;
            case 'a' -> 0x4;
            case 'z' -> 0x5;
            case 'e' -> 0x6;
            case 'q' -> 0x7;
            case 's' -> 0x8;
            case 'd' -> 0x9;
            case 'w' -> 0xA;
            case 'c' -> 0xB;
            case '4' -> 0xC;
            case 'r' -> 0xD;
            case 'f' -> 0xE;
            case 'v' -> 0xF;
            default -> -1;
        };
    }

    private static int getKeypadHexForCharCOLEMAK(char c) {
        return switch (c) {
            case 'x' -> 0x0;
            case '1' -> 0x1;
            case '2' -> 0x2;
            case '3' -> 0x3;
            case 'q' -> 0x4;
            case 'w' -> 0x5;
            case 'f' -> 0x6;
            case 'a' -> 0x7;
            case 'r' -> 0x8;
            case 's' -> 0x9;
            case 'z' -> 0xA;
            case 'c' -> 0xB;
            case '4' -> 0xC;
            case 'p' -> 0xD;
            case 't' -> 0xE;
            case 'v' -> 0xF;
            default -> -1;
        };
    }

    public static class Converter implements CommandLine.ITypeConverter<KeyboardLayout> {

        @Override
        public KeyboardLayout convert(String value) {
            return getKeyboardLayoutForIdentifier(value);
        }

    }

}
