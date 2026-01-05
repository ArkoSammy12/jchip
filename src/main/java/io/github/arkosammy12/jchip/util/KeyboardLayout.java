package io.github.arkosammy12.jchip.util;

import picocli.CommandLine;

import java.awt.event.KeyEvent;
import java.util.function.IntUnaryOperator;

public enum KeyboardLayout implements DisplayNameProvider {
    QWERTY("Qwerty", "qwerty", keyCode -> switch (keyCode) {
        case KeyEvent.VK_X -> 0x0;
        case KeyEvent.VK_1 -> 0x1;
        case KeyEvent.VK_2 -> 0x2;
        case KeyEvent.VK_3 -> 0x3;
        case KeyEvent.VK_Q -> 0x4;
        case KeyEvent.VK_W -> 0x5;
        case KeyEvent.VK_E -> 0x6;
        case KeyEvent.VK_A -> 0x7;
        case KeyEvent.VK_S -> 0x8;
        case KeyEvent.VK_D -> 0x9;
        case KeyEvent.VK_Z -> 0xA;
        case KeyEvent.VK_C -> 0xB;
        case KeyEvent.VK_4 -> 0xC;
        case KeyEvent.VK_R -> 0xD;
        case KeyEvent.VK_F -> 0xE;
        case KeyEvent.VK_V -> 0xF;
        default -> -1;
    }),
    DVORAK("Dvorak", "dvorak", keyCode -> switch (keyCode) {
        case KeyEvent.VK_Q -> 0x0;
        case KeyEvent.VK_1 -> 0x1;
        case KeyEvent.VK_2 -> 0x2;
        case KeyEvent.VK_3 -> 0x3;
        case KeyEvent.VK_QUOTE -> 0x4;
        case KeyEvent.VK_COMMA -> 0x5;
        case KeyEvent.VK_PERIOD -> 0x6;
        case KeyEvent.VK_A -> 0x7;
        case KeyEvent.VK_O -> 0x8;
        case KeyEvent.VK_E -> 0x9;
        case KeyEvent.VK_SEMICOLON -> 0xA;
        case KeyEvent.VK_J -> 0xB;
        case KeyEvent.VK_4 -> 0xC;
        case KeyEvent.VK_P -> 0xD;
        case KeyEvent.VK_U -> 0xE;
        case KeyEvent.VK_K -> 0xF;
        default -> -1;
    }),
    AZERTY("Azerty", "azerty", keyCode -> switch (keyCode) {
        case KeyEvent.VK_X -> 0x0;
        case KeyEvent.VK_1 -> 0x1;
        case KeyEvent.VK_2 -> 0x2;
        case KeyEvent.VK_3 -> 0x3;
        case KeyEvent.VK_A -> 0x4;
        case KeyEvent.VK_Z -> 0x5;
        case KeyEvent.VK_E -> 0x6;
        case KeyEvent.VK_Q -> 0x7;
        case KeyEvent.VK_S -> 0x8;
        case KeyEvent.VK_D -> 0x9;
        case KeyEvent.VK_W -> 0xA;
        case KeyEvent.VK_C -> 0xB;
        case KeyEvent.VK_4 -> 0xC;
        case KeyEvent.VK_R -> 0xD;
        case KeyEvent.VK_F -> 0xE;
        case KeyEvent.VK_V -> 0xF;
        default -> -1;
    }),
    COLEMAK("Colemak", "colemak", keyCode -> switch (keyCode) {
        case KeyEvent.VK_X -> 0x0;
        case KeyEvent.VK_1 -> 0x1;
        case KeyEvent.VK_2 -> 0x2;
        case KeyEvent.VK_3 -> 0x3;
        case KeyEvent.VK_Q -> 0x4;
        case KeyEvent.VK_W -> 0x5;
        case KeyEvent.VK_F -> 0x6;
        case KeyEvent.VK_A -> 0x7;
        case KeyEvent.VK_R -> 0x8;
        case KeyEvent.VK_S -> 0x9;
        case KeyEvent.VK_Z -> 0xA;
        case KeyEvent.VK_C -> 0xB;
        case KeyEvent.VK_4 -> 0xC;
        case KeyEvent.VK_P -> 0xD;
        case KeyEvent.VK_T -> 0xE;
        case KeyEvent.VK_V -> 0xF;
        default -> -1;
    });

    private final String displayName;
    private final String identifier;
    private final IntUnaryOperator keypadKeyCodeMapper;

    KeyboardLayout(String displayName, String identifier, IntUnaryOperator keypadKeyCodeMapper) {
        this.displayName = displayName;
        this.identifier = identifier;
        this.keypadKeyCodeMapper = keypadKeyCodeMapper;
    }

    public int getKeypadHexForKeyCode(int keyCode) {
        return this.keypadKeyCodeMapper.applyAsInt(keyCode);
    }

    public static KeyboardLayout getKeyboardLayoutForIdentifier(String identifier) {
        for (KeyboardLayout layout : values()) {
            if (layout.identifier.equals(identifier)) {
                return layout;
            }
        }
        throw new IllegalArgumentException("Unknown keyboard layout identifier \"" + identifier + "\"!");
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    public static class Converter implements CommandLine.ITypeConverter<KeyboardLayout> {

        @Override
        public KeyboardLayout convert(String value) {
            return getKeyboardLayoutForIdentifier(value);
        }

    }
}
