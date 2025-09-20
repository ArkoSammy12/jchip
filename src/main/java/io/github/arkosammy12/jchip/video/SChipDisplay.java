package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;

public class SChipDisplay extends Chip8Display {

    protected boolean isModern;
    protected boolean extendedMode;

    public SChipDisplay(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config, keyAdapter);
        this.isModern = this.chip8Variant == Chip8Variant.SUPER_CHIP_MODERN;
    }

    @Override
    public int getWidth() {
        return 128;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    protected int getPixelScale(DisplayAngle displayAngle) {
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
        this.frameBuffer[column][row] = value;
    }

    public int getPixel(int column, int row) {
        return this.frameBuffer[column][row];
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
        for (int i = this.screenHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = trueScrollAmount + i;
            if (shiftedVerticalPosition >= this.screenHeight) {
                continue;
            }
            for (int j = 0; j < this.screenWidth; j++) {
                this.frameBuffer[j][shiftedVerticalPosition] = this.frameBuffer[j][i];
            }
        }
        // Clear the top scrollOffset rows
        for (int y = 0; y < trueScrollAmount && y < this.screenHeight; y++) {
            for (int x = 0; x < this.screenWidth; x++) {
                this.frameBuffer[x][y] = 0;
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
        for (int i = this.screenWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + scrollAmount;
            if (shiftedHorizontalPosition >= this.screenWidth) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.frameBuffer[shiftedHorizontalPosition][j] = this.frameBuffer[i][j];
            }
        }
        // Clear the leftmost 4 columns
        for (int x = 0; x < scrollAmount && x < this.screenWidth; x++) {
            for (int y = 0; y < this.screenHeight; y++) {
                this.frameBuffer[x][y] = 0;
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
        for (int i = 0; i < this.screenWidth; i++) {
            int shiftedHorizontalPosition = i - scrollAmount;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.frameBuffer[shiftedHorizontalPosition][j] = this.frameBuffer[i][j];
            }
        }
        for (int x = this.screenWidth - scrollAmount; x < this.screenWidth; x++) {
            if (x < 0) {
                continue;
            }
            for (int y = 0; y < this.screenHeight; y++) {
                this.frameBuffer[x][y] = 0;
            }
        }

    }

}
