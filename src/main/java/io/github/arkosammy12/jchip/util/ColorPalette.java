package io.github.arkosammy12.jchip.util;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

import java.awt.*;

public class ColorPalette {


    private static final int[] CADMIUM = {
            0x1a1c2cff, 0xf4f4f4ff, 0x94b0c2ff, 0x333c57ff,
            0xb13e53ff, 0xa7f070ff, 0x3b5dc9ff, 0xffcd75ff,
            0x5d275dff, 0x38b764ff, 0x29366fff, 0x566c86ff,
            0xef7d57ff, 0x73eff7ff, 0x41a6f6ff, 0x257179ff
    };

    private static final int[] SILICON8 = {
            0x000000ff, 0xffffffff, 0xaaaaaaff, 0x555555ff,
            0xff0000ff, 0x00ff00ff, 0x0000ffff, 0xffff00ff,
            0x880000ff, 0x008800ff, 0x000088ff, 0x888800ff,
            0xff00ffff, 0x00ffffff, 0x880088ff, 0x008888ff
    };

    private static final int[] PICO8 = {
            0x000000ff, 0xfff1e8ff, 0xc2c3c7ff, 0x5f574fff,
            0xef7d57ff, 0x00e436ff, 0x29adffff, 0xffec27ff,
            0xab5236ff, 0x008751ff, 0x1d2b53ff, 0xffa300ff,
            0xff77a8ff, 0xffccaaff, 0x7e2553ff, 0x83769cff
    };

    private static final int[] OCTO_CLASSIC = {
            0x996600ff, 0xFFCC00ff, 0xFF6600ff, 0x662200ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff
    };

    private static final int[] LCD = {
            0xf2fff2ff, 0x5b8c7cff, 0xadd9bcff, 0x0d1a1aff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff
    };

    private static final int[] C64 = {
            0x000000ff, 0xffffffff, 0xadadadff, 0x626262ff,
            0xa1683cff, 0x9ae29bff, 0x887ecbff, 0xc9d487ff,
            0x9f4e44ff, 0x5cab5eff, 0x50459bff, 0x6d5412ff,
            0xcb7e75ff, 0x6abfc6ff, 0xa057a3ff, 0x898989ff
    };

    private static final int[] INTELLIVISION = {
            0x0c0005ff, 0xfffcffff, 0xa7a8a8ff, 0x3c5800ff,
            0xff3e00ff, 0x6ccd30ff, 0x002dffff, 0xfaea27ff,
            0xffa600ff, 0x00a720ff, 0xbd95ffff, 0xc9d464ff,
            0xff3276ff, 0x5acbffff, 0xc81a7dff, 0x00780fff
    };

    private static final int[] CGA = {
            0x000000ff, 0xffffffff, 0xaaaaaaff, 0x555555ff,
            0xff5555ff, 0x55ff55ff, 0x5555ffff, 0xffff55ff,
            0xaa0000ff, 0x00aa00ff, 0x0000aaff, 0xaa5500ff,
            0xff55ffff, 0x55ffffff, 0xaa00aaff, 0x00aaaaff
    };

    private final TextCharacter[] textCharacterColors = new TextCharacter[16];
    private final Color[] colorMap = new Color[16];
    private final int[][] colors = new int[16][3];
    private final int[] rgbColors = new int[16];

    public ColorPalette(String colorPalette) {
        int[] chosenPalette = switch (colorPalette) {
            case "cadmium" -> CADMIUM;
            case "silicon8" -> SILICON8;
            case "pico8" -> PICO8;
            case "octoclassic" -> OCTO_CLASSIC;
            case "lcd" -> LCD;
            case "c64" -> C64;
            case "intellivision" -> INTELLIVISION;
            case "cga" -> CGA;
            default -> throw new IllegalArgumentException("Unknown color palette: " + colorPalette + "!");
        };
        for (int i = 0; i < chosenPalette.length; i++) {
            int color = chosenPalette[i];
            int r = (color >> 24) & 0xFF;
            int g = (color >> 16) & 0xFF;
            int b = (color >> 8) & 0xFF;

            this.colors[i][0] = r;
            this.colors[i][1] = g;
            this.colors[i][2] = b;

            this.colorMap[i] = new Color(r, g, b);
            this.rgbColors[i] = (r << 16) | (g << 8) | b;
            this.textCharacterColors[i] = TextCharacter.fromCharacter('█')[0].withForegroundColor(new TextColor.RGB(r, g, b));
        }

    }

    public TextCharacter getPixel(int colorIndex) {
        return this.textCharacterColors[colorIndex];
    }

    public Color getColor(int colorIndex) {
        return this.colorMap[colorIndex];
    }

    public int getRGB(int colorIndex) {
        return this.rgbColors[colorIndex];
    }

}
