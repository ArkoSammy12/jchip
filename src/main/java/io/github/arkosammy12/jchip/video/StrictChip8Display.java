package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.memory.StrictChip8Memory;

import java.awt.event.KeyAdapter;
import java.util.List;

public final class StrictChip8Display extends Chip8Display {

    private StrictChip8Memory memory;

    public StrictChip8Display(EmulatorSettings config, List<KeyAdapter> keyAdapters) {
        super(config, keyAdapters);
    }

    public void setMemory(StrictChip8Memory memory) {
        this.memory = memory;
    }

    @Override
    public boolean flipPixel(int column, int row) {
        if (this.memory == null) {
            return false;
        }
        this.memory.drawDisplayPixel(column, row);
        return !this.memory.getDisplayPixel(column, row);
    }

    @Override
    public void clear() {
        if (this.memory == null) {
            return;
        }
        for (int i = 0; i < 256; i++) {
            this.memory.writeByte(StrictChip8Memory.DISPLAY_OFFSET + i, 0);
        }
    }

    protected void populateRenderBuffer(int[][] renderBuffer) {
        if (this.memory == null) {
            return;
        }
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                renderBuffer[x][y] = colorPalette.getColorARGB(this.memory.getDisplayPixel(x, y) ? 1 : 0);
            }
        }
    }

}
