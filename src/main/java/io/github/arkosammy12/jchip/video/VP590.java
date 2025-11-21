package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;

import java.util.Arrays;

public class VP590<E extends CosmacVipEmulator> extends CDP1861<E> {

    private static final int[] BACKGROUND_COLORS = {
            0xFF000080,
            0xFF000000,
            0xFF008000,
            0XFF800000
    };

    private final int[] colorRam = new int[256];
    private int backgroundColorIndex = 0;
    private boolean extendedColorDraw = false;
    private boolean colorRamModified = false;

    public VP590(E emulator) {
        super(emulator);
        Arrays.fill(this.colorRam, 0xF0);
        for (int[] ints : this.displayBuffer) {
            Arrays.fill(ints, 0xFF000000);
        }
    }

    public void writeColorRam(int address, int value) {
        this.colorRamModified = true;
        if ((address >= 0xC000 && address <= 0xCFFF) || (address >= 0xE000 && address <= 0xEFFF)) {
            extendedColorDraw = false;
        } else if ((address >= 0xD000 && address <= 0xDFFF) || (address >= 0xF000 && address <= 0xFFFF)) {
            extendedColorDraw = true;
        }
        int colorValue = 0xF0 | (value & 7);
        if (this.extendedColorDraw) {
            this.colorRam[address & 0xFF] = colorValue;
        } else {
            this.colorRam[address & 0xE7] = colorValue;
        }
    }

    public int readColorRam(int address) {
        if (this.extendedColorDraw) {
            return this.colorRam[address & 0xFF];
        } else {
            return this.colorRam[address & 0xE7];
        }
    }

    @Override
    public boolean isOutputPort(int port) {
        return port == 5 || super.isOutputPort(port);
    }

    @Override
    public void onOutput(int port, int value) {
        if (super.isOutputPort(port)) {
            super.onOutput(port, value);
            return;
        }
        this.backgroundColorIndex = (backgroundColorIndex + 1) % BACKGROUND_COLORS.length;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void doDmaOut(int dmaOutAddress, int value) {
        if (!this.emulator.getProcessor().getCurrentState().isS2Dma()) {
            return;
        }
        int row = this.scanlineIndex - DISPLAY_AREA_BEGIN;
        if (row < 0 || row >= this.getHeight()) {
            return;
        }
        int dmaIndex = (int) ((this.emulator.getProcessor().getMachineCycles() % MACHINE_CYCLES_PER_SCANLINE) - DMAO_BEGIN);
        int colStart = dmaIndex * 8;
        int color = 0xFF000000;
        int backgroundColor;
        if (this.colorRamModified) {
            backgroundColor = BACKGROUND_COLORS[backgroundColorIndex];
            int colorByte = this.readColorRam(dmaOutAddress);
            if ((colorByte & 1) != 0) {
                color |= 0xFF0000;
            }
            if ((colorByte & 0b100) != 0) {
                color |= 0x00FF00;
            }
            if ((colorByte & 0b10) != 0) {
                color |= 0x0000FF;
            }
        } else {
            backgroundColor = 0xFF000080;
            color |= 0xFFFFFF;
        }
        for (int i = 0, mask = 0x80; i < 8; i++, mask >>>= 1) {
            int col = colStart + i;
            if (col < 0 || col >= this.getWidth()) {
                break;
            }
            this.displayBuffer[col][row] = (value & mask) != 0 ? color : backgroundColor;
        }
    }

}
