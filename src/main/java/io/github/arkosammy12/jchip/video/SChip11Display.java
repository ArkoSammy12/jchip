package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.SChip11Emulator;

public class SChip11Display<E extends SChip11Emulator> extends SChip10Display<E> {

    public SChip11Display(E emulator) {
        super(emulator);
    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollDown(int scrollAmount) {
        for (int i = this.imageHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = scrollAmount + i;
            if (shiftedVerticalPosition >= this.imageHeight) {
                continue;
            }
            for (int j = 0; j < this.imageWidth; j++) {
                this.bitplaneBuffer[j][shiftedVerticalPosition] = this.bitplaneBuffer[j][i];
            }
        }
        for (int y = 0; y < scrollAmount && y < this.imageHeight; y++) {
            for (int x = 0; x < this.imageWidth; x++) {
                this.bitplaneBuffer[x][y] = 0;
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollRight() {
        int scrollAmount = 4;
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
        int scrollAmount = 4;
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
