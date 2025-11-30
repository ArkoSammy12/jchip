package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.SChipEmulator;
import io.github.arkosammy12.jchip.util.DisplayAngle;

public class SChipDisplay<E extends SChipEmulator> extends Chip8Display<E> {

    private final boolean isModern;
    protected boolean hiresMode;

    public SChipDisplay(E emulator, boolean isModern) {
        super(emulator);
        this.isModern = isModern;
    }

    @Override
    public int getWidth() {
        return this.hiresMode ? 128 : 64;
    }

    @Override
    public int getHeight() {
        return this.hiresMode ? 64 : 32;
    }

    @Override
    public int getImageWidth() {
        return 128;
    }

    @Override
    public int getImageHeight() {
        return 64;
    }

    @Override
    public int getImageScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 6;
            default -> 10;
        };
    }

    public void setHiresMode(boolean hiresMode) {
        this.hiresMode = hiresMode;
    }

    public boolean isHiresMode() {
        return this.hiresMode;
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
            if (this.hiresMode) {
                trueScrollAmount = scrollAmount;
            } else {
                trueScrollAmount = scrollAmount * 2;
            }
        }
        for (int i = this.imageHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = trueScrollAmount + i;
            if (shiftedVerticalPosition >= this.imageHeight) {
                continue;
            }
            for (int j = 0; j < this.imageWidth; j++) {
                this.bitplaneBuffer[j][shiftedVerticalPosition] = this.bitplaneBuffer[j][i];
            }
        }
        for (int y = 0; y < trueScrollAmount && y < this.imageHeight; y++) {
            for (int x = 0; x < this.imageWidth; x++) {
                this.bitplaneBuffer[x][y] = 0;
            }
        }

    }

    public void scrollRight() {
        int scrollAmount;
        if (!this.isModern) {
            scrollAmount = 4;
        } else {
            if (this.hiresMode) {
                scrollAmount = 4;
            } else {
                scrollAmount = 8;
            }
        }
        for (int i = this.imageWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + scrollAmount;
            if (shiftedHorizontalPosition >= this.imageWidth) {
                continue;
            }
            if (this.imageHeight >= 0) {
                System.arraycopy(this.bitplaneBuffer[i], 0, this.bitplaneBuffer[shiftedHorizontalPosition], 0, this.imageHeight);
            }
        }
        for (int x = 0; x < scrollAmount && x < this.imageWidth; x++) {
            for (int y = 0; y < this.imageHeight; y++) {
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
            if (this.hiresMode) {
                scrollAmount = 4;
            } else {
                scrollAmount = 8;
            }
        }
        for (int i = 0; i < this.imageWidth; i++) {
            int shiftedHorizontalPosition = i - scrollAmount;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            if (this.imageHeight >= 0) {
                System.arraycopy(this.bitplaneBuffer[i], 0, this.bitplaneBuffer[shiftedHorizontalPosition], 0, this.imageHeight);
            }
        }
        for (int x = this.imageWidth - scrollAmount; x < this.imageWidth; x++) {
            if (x < 0) {
                continue;
            }
            for (int y = 0; y < this.imageHeight; y++) {
                this.bitplaneBuffer[x][y] = 0;
            }
        }
    }

}
