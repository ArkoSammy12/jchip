package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.SChip10Emulator;

public class SChip10Display<E extends SChip10Emulator> extends Chip8Display<E> {

    protected boolean hiresMode;

    public SChip10Display(E emulator) {
        super(emulator);
    }

    @Override
    public int getWidth() {
        return this.hiresMode ? 128 : 64;
    }

    @Override
    public int getHeight() {
        return this.hiresMode ? 64 : 32;
    }

    @Override
    public int getImageWidth() {
        return 128;
    }

    @Override
    public int getImageHeight() {
        return 64;
    }

    public void setHiresMode(boolean hiresMode) {
        this.hiresMode = hiresMode;
    }

    public boolean isHiresMode() {
        return this.hiresMode;
    }

    public void setPixel(int column, int row, int value) {
        this.bitplaneBuffer[column][row] = value;
    }

    public int getPixel(int column, int row) {
        return this.bitplaneBuffer[column][row];
    }

}
