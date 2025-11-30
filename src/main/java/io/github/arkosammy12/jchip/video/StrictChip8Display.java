package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.StrictChip8Emulator;
import io.github.arkosammy12.jchip.memory.StrictChip8Bus;

public final class StrictChip8Display extends Chip8Display<StrictChip8Emulator> {

    public StrictChip8Display(StrictChip8Emulator emulator) {
        super(emulator);
    }

    @Override
    public boolean flipPixel(int column, int row) {
        StrictChip8Bus bus = this.emulator.getBus();
        bus.drawDisplayPixel(column, row);
        return !bus.getDisplayPixel(column, row);
    }

    @Override
    public void clear() {
        StrictChip8Bus bus = this.emulator.getBus();
        for (int i = 0; i < 256; i++) {
            bus.writeByte(StrictChip8Bus.DISPLAY_OFFSET + i, 0);
        }
    }

    protected void populateRenderBuffer(int[][] renderBuffer) {
        StrictChip8Bus bus = this.emulator.getBus();
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                renderBuffer[x][y] = colorPalette.getColorARGB(bus.getDisplayPixel(x, y) ? 1 : 0);
            }
        }
    }

}
