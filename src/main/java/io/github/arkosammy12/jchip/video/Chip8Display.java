package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;

import java.util.Arrays;

public class Chip8Display<E extends Chip8Emulator> extends Display<E> {

    protected final ColorPalette colorPalette;
    protected final int[][] bitplaneBuffer;

    public Chip8Display(E emulator) {
        super(emulator);
        this.bitplaneBuffer = new int[this.getImageWidth()][this.getImageHeight()];
        this.colorPalette = emulator.getEmulatorSettings().getColorPalette();
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

    public void clear() {
        for (int[] ints : this.bitplaneBuffer) {
            Arrays.fill(ints, 0);
        }
    }

    protected void populateRenderBuffer(int[][] renderBuffer) {
        for (int y = 0; y < this.imageHeight; y++) {
            for (int x = 0; x < this.imageWidth; x++) {
                renderBuffer[x][y] = this.colorPalette.getColorARGB(this.bitplaneBuffer[x][y] & 0xF);
            }
        }
    }

}
