package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;

public class SChipDisplay extends Chip8Display {

    private final boolean isModern;
    protected boolean extendedMode;

    public SChipDisplay(EmulatorConfig config, KeyAdapter keyAdapter, boolean isModern) {
        super(config, keyAdapter);
        this.isModern = isModern;
    }

    @Override
    public int getWidth() {
        return this.extendedMode ? 128 : 64;
    }

    @Override
    public int getHeight() {
        return this.extendedMode ? 64 : 32;
    }

    @Override
    protected int getRenderWidth() {
        return 128;
    }

    @Override
    protected int getRenderHeight() {
        return 64;
    }

    @Override
    protected int getPixelRenderScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 6;
            default -> 10;
        };
    }

    public void setExtendedMode(boolean extendedMode) {
        this.extendedMode = extendedMode;
    }

    public boolean isExtendedMode() {
        return this.extendedMode;
    }

    public void setPixel(int column, int row, int value) {
        this.bitplaneBuffer[column][row] = value;
    }

    public int getPixel(int column, int row) {
        return this.bitplaneBuffer[column][row];
    }

    public void scrollDown(int scrollAmount) {
        int trueScrollAmount;
        if (!this.isModern) {
            trueScrollAmount = scrollAmount;
        } else {
            if (this.extendedMode) {
                trueScrollAmount = scrollAmount;
            } else {
                trueScrollAmount = scrollAmount * 2;
            }
        }
        for (int i = this.displayHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = trueScrollAmount + i;
            if (shiftedVerticalPosition >= this.displayHeight) {
                continue;
            }
            for (int j = 0; j < this.displayWidth; j++) {
                this.bitplaneBuffer[j][shiftedVerticalPosition] = this.bitplaneBuffer[j][i];
            }
        }
        for (int y = 0; y < trueScrollAmount && y < this.displayHeight; y++) {
            for (int x = 0; x < this.displayWidth; x++) {
                this.bitplaneBuffer[x][y] = 0;
            }
        }

    }

    public void scrollRight() {
        int scrollAmount;
        if (!this.isModern) {
            scrollAmount = 4;
        } else {
            if (this.extendedMode) {
                scrollAmount = 4;
            } else {
                scrollAmount = 8;
            }
        }
        for (int i = this.displayWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + scrollAmount;
            if (shiftedHorizontalPosition >= this.displayWidth) {
                continue;
            }
            if (this.displayHeight >= 0) {
                System.arraycopy(this.bitplaneBuffer[i], 0, this.bitplaneBuffer[shiftedHorizontalPosition], 0, this.displayHeight);
            }
        }
        for (int x = 0; x < scrollAmount && x < this.displayWidth; x++) {
            for (int y = 0; y < this.displayHeight; y++) {
                this.bitplaneBuffer[x][y] = 0;
            }
        }

    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollLeft() {
        int scrollAmount;
        if (!this.isModern) {
            scrollAmount = 4;
        } else {
            if (this.extendedMode) {
                scrollAmount = 4;
            } else {
                scrollAmount = 8;
            }
        }
        for (int i = 0; i < this.displayWidth; i++) {
            int shiftedHorizontalPosition = i - scrollAmount;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            if (this.displayHeight >= 0) {
                System.arraycopy(this.bitplaneBuffer[i], 0, this.bitplaneBuffer[shiftedHorizontalPosition], 0, this.displayHeight);
            }
        }
        for (int x = this.displayWidth - scrollAmount; x < this.displayWidth; x++) {
            if (x < 0) {
                continue;
            }
            for (int y = 0; y < this.displayHeight; y++) {
                this.bitplaneBuffer[x][y] = 0;
            }
        }
    }

}
