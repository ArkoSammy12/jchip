package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.StrictChip8Emulator;
import io.github.arkosammy12.jchip.memory.StrictChip8Memory;

public final class StrictChip8Display extends Chip8Display<StrictChip8Emulator> {

    public StrictChip8Display(StrictChip8Emulator emulator) {
        super(emulator);
    }


    @Override
    public boolean flipPixel(int column, int row) {
        StrictChip8Memory memory = this.emulator.getMemory();
        memory.drawDisplayPixel(column, row);
        return !memory.getDisplayPixel(column, row);
    }

    @Override
    public void clear() {
        StrictChip8Memory memory = this.emulator.getMemory();
        for (int i = 0; i < 256; i++) {
            memory.writeByte(StrictChip8Memory.DISPLAY_OFFSET + i, 0);
        }
    }

    protected void populateRenderBuffer(int[][] renderBuffer) {
        StrictChip8Memory memory = this.emulator.getMemory();
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                renderBuffer[x][y] = colorPalette.getColorARGB(memory.getDisplayPixel(x, y) ? 1 : 0);
            }
        }
    }

}
