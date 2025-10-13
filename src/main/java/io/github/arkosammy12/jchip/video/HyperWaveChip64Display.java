package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.config.EmulatorConfig;

import java.awt.event.KeyAdapter;
import java.util.Arrays;
import java.util.List;

public class HyperWaveChip64Display extends XOChipDisplay {

    private static final int FULL_OPAQUE_MASK = 0xFF << 24;

    private final int[] colorPalette = new int[16];
    private DrawingMode drawingMode = DrawingMode.XOR;

    public HyperWaveChip64Display(EmulatorConfig config, List<KeyAdapter> keyAdapters) {
        super(config, keyAdapters);
        Arrays.fill(this.colorPalette, 0xFF000000);
    }

    @Override
    public synchronized void reset() {
        super.reset();
        Arrays.fill(this.colorPalette, 0xFF000000);
        this.drawingMode = DrawingMode.XOR;
    }

    public synchronized void setDrawingMode(DrawingMode drawingMode) {
        this.drawingMode = drawingMode;
    }

    public synchronized void setPaletteEntry(int index, int value) {
        this.colorPalette[index] = FULL_OPAQUE_MASK | value;
    }

    @Override
    public synchronized boolean togglePixelAtBitPlanes(int column, int row, int bitPlaneMask) {
        boolean collision = (this.bitplaneBuffer[column][row] & bitPlaneMask) != 0;
        switch (this.drawingMode) {
            case OR -> this.bitplaneBuffer[column][row] |= bitPlaneMask;
            case SUBTRACT -> this.bitplaneBuffer[column][row] &= ~bitPlaneMask;
            case XOR -> this.bitplaneBuffer[column][row] ^= bitPlaneMask;
        }
        return collision;
    }

    public synchronized void invert() {
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

    @Override
    protected synchronized void fillImageBuffer(int[] buffer) {
        for (int y = 0; y < displayHeight; y++) {
            int base = y * displayWidth;
            for (int x = 0; x < displayWidth; x++) {
                buffer[base + x] = this.colorPalette[bitplaneBuffer[x][y] & 0xF];
            }
        }
    }

    public enum DrawingMode {
        OR,
        SUBTRACT,
        XOR
    }

}
