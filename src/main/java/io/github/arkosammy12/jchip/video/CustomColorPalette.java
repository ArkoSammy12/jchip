package io.github.arkosammy12.jchip.video;

public class CustomColorPalette implements ColorPalette {

    private final int[] argbColors = new int[16];

    public CustomColorPalette(ColorPalette base, int[][] customPixelColors) {
        for (int i = 0; i < 16; i++) {
            this.argbColors[i] = base.getColorARGB(i);
        }
        if (customPixelColors == null) {
            return;
        }
        for (int i = 0; i < customPixelColors.length && i < 16; i++) {
            int r = customPixelColors[i][0];
            int g = customPixelColors[i][1];
            int b = customPixelColors[i][2];
            int a = 0xFF;
            this.argbColors[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    @Override
    public int getColorARGB(int colorIndex) {
        return this.argbColors[colorIndex];
    }

}
