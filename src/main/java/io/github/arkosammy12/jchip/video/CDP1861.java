package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.ui.IODevice;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import org.tinylog.Logger;

public class CDP1861<E extends CosmacVipEmulator> extends Display<E> implements IODevice {

    private static final int SCANLINES_PER_FRAME = 262;
    private static final int MACHINE_CYCLES_PER_SCANLINE = 14;

    private static final int INTERRUPT_BEGIN = 78;
    private static final int INTERRUPT_END = 80;

    private static final int FIRST_EFX_BEGIN = 76;
    private static final int FIRST_EFX_END = 80;

    private static final int DISPLAY_AREA_BEGIN = 80;
    private static final int DISPLAY_AREA_END = 208;

    private static final int SECOND_EFX_BEGIN = 204;
    private static final int SECOND_EFX_END = 208;

    private static final int DMAO_BEGIN = 4;
    private static final int DMAO_END = 12;


    private final int[][] displayBuffer;
    private long machineCycleCounter;
    private int scanlineIndex;

    private DmaStatus dmaStatus = DmaStatus.NONE;
    private boolean triggerInterrupt = false;
    private boolean enabled = false;

    public CDP1861(E emulator) {
        super(emulator);
        this.displayBuffer = new int[this.getWidth()][this.getHeight()];
    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 128;
    }

    @Override
    protected int getImageWidth() {
        return 128;
    }

    @Override
    protected int getImageHeight() {
        return 128;
    }

    @Override
    protected int getImageScale(DisplayAngle displayAngle) {
        return 11;
    }

    @Override
    public DmaStatus getDmaStatus() {
        return this.dmaStatus;
    }

    @Override
    public boolean isInterrupting() {
        return this.triggerInterrupt;
    }


    @Override
    public void cycle() {
        this.emulator.getProcessor().setEF(0, (this.scanlineIndex >= FIRST_EFX_BEGIN && this.scanlineIndex < FIRST_EFX_END) || (this.scanlineIndex >= SECOND_EFX_BEGIN && this.scanlineIndex < SECOND_EFX_END));
        if (this.enabled) {
            this.triggerInterrupt = this.scanlineIndex >= INTERRUPT_BEGIN && this.scanlineIndex < INTERRUPT_END;
            if (this.scanlineIndex >= DISPLAY_AREA_BEGIN && this.scanlineIndex < DISPLAY_AREA_END) {
                long relativeCycles = this.machineCycleCounter % MACHINE_CYCLES_PER_SCANLINE;
                if (relativeCycles >= DMAO_BEGIN && relativeCycles < DMAO_END) {
                    this.dmaStatus = DmaStatus.OUT;
                } else {
                    this.dmaStatus = DmaStatus.NONE;
                }
            }
        }
        if ((this.machineCycleCounter + 1) % MACHINE_CYCLES_PER_SCANLINE == 0) {
            this.scanlineIndex = (this.scanlineIndex + 1) % SCANLINES_PER_FRAME;
        }
        machineCycleCounter++;
    }

    @Override
    public void doDmaOut(int value) {
        int row = this.scanlineIndex - DISPLAY_AREA_BEGIN;
        if (row < 0 || row >= this.getHeight()) {
            return;
        }

        int dmaIndex = (int) ((this.machineCycleCounter % MACHINE_CYCLES_PER_SCANLINE) - DMAO_BEGIN) - 1;
        if (dmaIndex < 0 || dmaIndex >= 8) {
            return;
        }
        int colStart = dmaIndex * 8;
        for (int i = 0, mask = 0x80; i < 8; i++, mask >>>= 1) {
            int col = colStart + i;
            this.displayBuffer[col][row] = ((value & mask) != 0) ? 1 : 0;
        }
    }

    @Override
    public int doDmaIn() {
        return 0;
    }

    @Override
    public void onOutput(int value) {
        this.enabled = false;
    }

    @Override
    public int onInput() {
        this.enabled = true;
        return 0;
    }

    @Override
    protected void populateRenderBuffer(int[][] renderBuffer) {
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < getWidth(); x++) {
                renderBuffer[x * 2][y] = BuiltInColorPalette.CADMIUM.getColorARGB(this.displayBuffer[x][y] & 0xF);
                renderBuffer[(x * 2) + 1][y] = BuiltInColorPalette.CADMIUM.getColorARGB(this.displayBuffer[x][y] & 0xF);
            }
        }
    }

    @Override
    protected void clear() {

    }

}
