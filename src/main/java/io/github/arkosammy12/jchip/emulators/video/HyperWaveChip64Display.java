package io.github.arkosammy12.jchip.emulators.video;

import io.github.arkosammy12.jchip.emulators.HyperWaveChip64Emulator;

import java.util.Arrays;

public class HyperWaveChip64Display<E extends HyperWaveChip64Emulator> extends XOChipDisplay<E> {

    private static final int FULL_OPAQUE_MASK = 0xFF << 24;

    private final int[] colorPalette = new int[16];
    private DrawingMode drawingMode = DrawingMode.XOR;

    public HyperWaveChip64Display(E emulator) {
        super(emulator);
        Arrays.fill(this.colorPalette, 0xFF000000);
    }

    public void setDrawingMode(DrawingMode drawingMode) {
        this.drawingMode = drawingMode;
    }

    public void setPaletteEntry(int index, int value) {
        this.colorPalette[index] = FULL_OPAQUE_MASK | value;
    }

    @Override
    public boolean flipPixelAtBitPlanes(int column, int row, int bitPlaneMask) {
        boolean collision = (this.bitplaneBuffer[column][row] & bitPlaneMask) != 0;
        switch (this.drawingMode) {
            case OR -> this.bitplaneBuffer[column][row] |= bitPlaneMask;
            case SUBTRACT -> this.bitplaneBuffer[column][row] &= ~bitPlaneMask;
            case XOR -> this.bitplaneBuffer[column][row] ^= bitPlaneMask;
        }
        return collision;
    }

    public void invert() {
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.getSelectedBitPlanes()) == 0) {
                continue;
            }
            for (int i = 0; i < this.bitplaneBuffer.length; i++) {
                for (int j = 0; j < this.bitplaneBuffer[i].length; j++) {
                    this.bitplaneBuffer[i][j] ^= mask;
                }
            }
        }
    }

    public void populateRenderBuffer(int[][] renderBuffer) {
        for (int y = 0; y < this.imageHeight; y++) {
            for (int x = 0; x < this.imageWidth; x++) {
                renderBuffer[x][y] = this.colorPalette[this.bitplaneBuffer[x][y] & 0xF];
            }
        }
    }

    public enum DrawingMode {
        OR,
        SUBTRACT,
        XOR
    }

}
