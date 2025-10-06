package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;
import java.util.Arrays;

public class Chip8Display extends Display {

    protected final ColorPalette colorPalette;

    protected final int[][] bitplaneBuffer;

    public Chip8Display(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config, keyAdapter);
        this.bitplaneBuffer = new int[this.getImageWidth()][this.getImageHeight()];
        this.colorPalette = config.getColorPalette();
    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    protected int getImageWidth() {
        return 64;
    }

    @Override
    protected int getImageHeight() {
        return 32;
    }

    public boolean togglePixel(int column, int row) {
        int current = this.bitplaneBuffer[column][row];
        this.bitplaneBuffer[column][row] ^= 1;
        return current != 0;
    }

    @Override
    public void clear() {
        for (int[] ints : this.bitplaneBuffer) {
            Arrays.fill(ints, 0);
        }
    }

    @Override
    protected int getImageScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 11;
            default -> 20;
        };
    }

    @Override
    protected void fillImageBuffer(int[] buffer) {
        for (int y = 0; y < displayHeight; y++) {
            int base = y * displayWidth;
            for (int x = 0; x < displayWidth; x++) {
                buffer[base + x] = colorPalette.getColorARGB(bitplaneBuffer[x][y] & 0xF);
            }
        }
    }
}
