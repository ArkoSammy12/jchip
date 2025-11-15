package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.ui.IODevice;
import io.github.arkosammy12.jchip.util.DisplayAngle;

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

    private static final int DMAO_BEGIN = 4 - 1; // Shift begin and end indices back by one, since the actual
    private static final int DMAO_END = 12 - 1;  // dmao cycle will be acknowledged on the next cycle

    private final int[][] displayBuffer;
    private int scanlineIndex;

    private DmaStatus dmaStatus = DmaStatus.NONE;
    private boolean interrupting = false;
    private boolean enabled = false;
    private boolean displayEnableLatch;

    public CDP1861(E emulator) {
        super(emulator);
        this.displayBuffer = new int[this.getWidth()][this.getHeight()];
    }

    @Override
    public int getWidth() {
        return 256;
    }

    @Override
    public int getHeight() {
        return 128;
    }

    @Override
    protected int getImageWidth() {
        return 256;
    }

    @Override
    protected int getImageHeight() {
        return 128;
    }

    @Override
    protected int getImageScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 5;
            default -> 7;
        };
    }

    @Override
    public DmaStatus getDmaStatus() {
        return this.dmaStatus;
    }

    @Override
    public boolean isInterrupting() {
        return this.interrupting;
    }

    @Override
    public void cycle() {
        long cpuMachineCycles = this.emulator.getProcessor().getMachineCycles();
        if (cpuMachineCycles % CosmacVipEmulator.CYCLES_PER_FRAME == 0) {
            this.enabled = this.displayEnableLatch;
        }
        if (this.enabled) {
            this.emulator.getProcessor().setEF(0, (this.scanlineIndex >= FIRST_EFX_BEGIN && this.scanlineIndex < FIRST_EFX_END) || (this.scanlineIndex >= SECOND_EFX_BEGIN && this.scanlineIndex < SECOND_EFX_END));
            this.interrupting = this.scanlineIndex >= INTERRUPT_BEGIN && this.scanlineIndex < INTERRUPT_END;
            if (this.scanlineIndex >= DISPLAY_AREA_BEGIN && this.scanlineIndex < DISPLAY_AREA_END) {
                long scanLineCycles = cpuMachineCycles % MACHINE_CYCLES_PER_SCANLINE;
                if (scanLineCycles >= DMAO_BEGIN && scanLineCycles < DMAO_END) {
                    this.dmaStatus = DmaStatus.OUT;
                } else {
                    this.dmaStatus = DmaStatus.NONE;
                }
            }
        } else {
            this.interrupting = false;
            this.dmaStatus = DmaStatus.NONE;
            this.emulator.getProcessor().setEF(0, false);
        }
        if (cpuMachineCycles % MACHINE_CYCLES_PER_SCANLINE == 0) {
            this.scanlineIndex = (this.scanlineIndex + 1) % SCANLINES_PER_FRAME;
        }
    }

    @Override
    public void doDmaOut(int value) {
        if (!this.emulator.getProcessor().getCurrentState().isS2Dma()) {
            return;
        }
        int row = this.scanlineIndex - DISPLAY_AREA_BEGIN;
        if (row < 0 || row >= this.getHeight()) {
            return;
        }
        int dmaIndex = (int) ((this.emulator.getProcessor().getMachineCycles() % MACHINE_CYCLES_PER_SCANLINE) - DMAO_BEGIN);
        int colStart = dmaIndex * 8;
        for (int i = 0, mask = 0x80; i < 8; i++, mask >>>= 1) {
            int col = (colStart + i) * 4;
            for (int j = 0; j < 4; j++) {
                int colOffset = col + j;
                if (colOffset < 0 || colOffset >= this.getWidth()) {
                    break;
                }
                this.displayBuffer[colOffset][row] = ((value & mask) != 0) ? 1 : 0;
            }
        }
    }

    @Override
    public int doDmaIn() {
        return 0;
    }

    @Override
    public void onOutput(int value) {
        this.displayEnableLatch = false;
    }

    @Override
    public int onInput() {
        this.displayEnableLatch = true;
        return 0;
    }

    @Override
    protected void populateRenderBuffer(int[][] renderBuffer) {
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                renderBuffer[x][y] = (this.displayBuffer[x][y] & 0xF) != 0 ? 0xFFFFFFFF : 0xFF000000;
            }
        }
    }

}
