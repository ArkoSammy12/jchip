package io.github.arkosammy12.jchip.util;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

import java.util.HashMap;
import java.util.Map;

public class ColorPalette {


    private static final int[] rausPalette = {
            0x181C2000, 0xE4DCD400, 0x8C888400, 0x403C3800,
            0xD8201000, 0x40D02000, 0x1040D000, 0xE0C81800,
            0x50101000, 0x10501000, 0x50B0C000, 0xF0801000,
            0xE0609000, 0xE0F09000, 0xB050F000, 0x70402000,
    };

    private static final int[] silicon8Palette = {
            0xFF000000,
            0xFFFFFFFF,
            0xFFAAAAAA,
            0xFF555555,
            0xFFFF0000,
            0xFF00FF00,
            0xFF0000FF,
            0xFFFFFF00,
            0xFF880000,
            0xFF008800,
            0xFF000088,
            0xFF888800,
            0xFFFF00FF,
            0xFF00FFFF,
            0xFF880088,
            0xFF008888
    };

    private static final int[] cgaPalette = {
            0x000000FF, 0x0000AAFF, 0x00AA00FF, 0x00AAAAFF,
            0xAA0000FF, 0xAA00AAFF, 0xAA5500FF, 0xAAAAAAFF,
            0x555555FF, 0x5555FFFF, 0x55FF55FF, 0x55FFFFFF,
            0xFF5555FF, 0xFF55FFFF, 0xFFFF55FF, 0xFFFFFFFF
    };

    private static final int[] octoPalette = {
            0x996600FF, 0xFFCC00FF, 0xFF6600FF, 0x662200FF,
            0xFF0000FF, 0x00FF00FF, 0x0000FFFF, 0xFFFF00FF,
            0x880000FF, 0x008800FF, 0x000088FF, 0x888800FF,
            0xFF00FFFF, 0x00FFFFFF, 0x880088FF, 0x008888FF
    };

    private static final int[] cadmiumPalette = new int[] {
            0x1a1c2cff, 0xf4f4f4ff, 0x94b0c2ff, 0x333c57ff,
            0xb13e53ff, 0xa7f070ff, 0x3b5dc9ff, 0xffcd75ff,
            0x5d275dff, 0x38b764ff, 0x29366fff, 0x566c86ff,
            0xef7d57ff, 0x73eff7ff, 0x41a6f6ff, 0x257179ff
    };

    private final TextCharacter[] characterMap = new TextCharacter[16];

    public ColorPalette(String colorPalette) {
        int[] chosenPalette = switch (colorPalette) {
            case "octo" -> octoPalette;
            case "silicon8" -> silicon8Palette;
            case "raus" -> rausPalette;
            case "cadmium" -> cadmiumPalette;
            case "cga" -> cgaPalette;
            default -> throw new IllegalArgumentException("Unknown color palette: " + colorPalette + "!");
        };
        for (int i = 0; i < chosenPalette.length; i++) {
            int color = chosenPalette[i];
            int r = (color >> 24) & 0xFF;
            int g = (color >> 16) & 0xFF;
            int b = (color >> 8) & 0xFF;
            this.characterMap[i] = TextCharacter.fromCharacter('█')[0].withForegroundColor(new TextColor.RGB(r, g, b));
        }

    }

    public TextCharacter getPixel(int colorIndex) {
        return this.characterMap[colorIndex];
    }

}
