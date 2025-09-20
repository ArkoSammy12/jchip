package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;
import java.util.Arrays;

public class Chip8Display extends AbstractDisplay {

    protected final ColorPalette colorPalette;

    protected final int[][] frameBuffer;

    public Chip8Display(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config, keyAdapter);
        this.frameBuffer = new int[getWidth()][getHeight()];
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

    public boolean togglePixel(int column, int row) {
        if (column >= this.screenWidth || column < 0 || row >= this.screenHeight || row < 0) {
            return false;
        }
        int current = this.frameBuffer[column][row];
        this.frameBuffer[column][row] ^= 1;
        return current != 0;
    }

    @Override
    public void clear() {
        for (int[] ints : this.frameBuffer) {
            Arrays.fill(ints, 0);
        }
    }

    @Override
    protected int getPixelScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 11;
            default -> 20;
        };
    }

    @Override
    protected void populateDataBuffer(int[] buffer) {
        for (int y = 0; y < screenHeight; y++) {
            int base = y * screenWidth;
            for (int x = 0; x < screenWidth; x++) {
                buffer[base + x] = colorPalette.getColorARGB(frameBuffer[x][y] & 0xF);
            }
        }
    }
}
