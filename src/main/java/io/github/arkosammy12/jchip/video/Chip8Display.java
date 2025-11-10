package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.util.DisplayAngle;

import java.awt.event.KeyAdapter;
import java.util.Arrays;
import java.util.List;

public class Chip8Display extends Display {

    protected final ColorPalette colorPalette;
    protected final int[][] bitplaneBuffer;

    public Chip8Display(EmulatorSettings config, List<KeyAdapter> keyAdapters) {
        super(config, keyAdapters);
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
    public int getImageWidth() {
        return 64;
    }

    @Override
    public int getImageHeight() {
        return 32;
    }

    public boolean flipPixel(int column, int row) {
        this.bitplaneBuffer[column][row] ^= 1;
        return this.bitplaneBuffer[column][row] == 0;
    }

    @Override
    public void clear() {
        for (int[] ints : this.bitplaneBuffer) {
            Arrays.fill(ints, 0);
        }
    }

    @Override
    public int getImageScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 11;
            default -> 20;
        };
    }

    protected void populateRenderBuffer(int[][] renderBuffer) {
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                renderBuffer[x][y] = colorPalette.getColorARGB(bitplaneBuffer[x][y] & 0xF);
            }
        }
    }

}
