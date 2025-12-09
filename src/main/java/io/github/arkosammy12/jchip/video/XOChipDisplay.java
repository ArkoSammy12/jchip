package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.XOChipEmulator;

public class XOChipDisplay<E extends XOChipEmulator> extends SChipModernDisplay<E> {

    public static final int BITPLANE_BASE_MASK = 1 << 3;

    private int selectedBitPlanes = 1;

    public XOChipDisplay(E emulator) {
        super(emulator);
    }

    public void setSelectedBitPlanes(int selectedBitPlanes) {
        this.selectedBitPlanes = selectedBitPlanes;
    }

    public int getSelectedBitPlanes() {
        return this.selectedBitPlanes;
    }

    public boolean flipPixelAtBitPlanes(int column, int row, int bitPlaneMask) {
        boolean collision = (this.bitplaneBuffer[column][row] & bitPlaneMask) != 0;
        this.bitplaneBuffer[column][row] ^= bitPlaneMask;
        return collision;
    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollUp(int scrollAmount) {
        if (!this.hiresMode) {
            scrollAmount *= 2;
        }
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < this.imageHeight; i++) {
                int shiftedVerticalPosition = i - scrollAmount;
                if (shiftedVerticalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.imageWidth; j++) {
                    int val = this.bitplaneBuffer[j][i] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] |= mask;
                    } else {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] &= ~mask;
                    }
                }
            }
            for (int y = this.imageHeight - scrollAmount; y < this.imageHeight; y++) {
                if (y < 0) {
                    continue;
                }
                for (int x = 0; x < this.imageWidth; x++) {
                    this.bitplaneBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollDown(int scrollAmount) {
        if (!this.hiresMode) {
            scrollAmount *= 2;
        }
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = this.imageHeight - 1; i >= 0; i--) {
                int shiftedVerticalPosition = scrollAmount + i;
                if (shiftedVerticalPosition >= this.imageHeight) {
                    continue;
                }
                for (int j = 0; j < this.imageWidth; j++) {
                    int val = this.bitplaneBuffer[j][i] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] |= mask;
                    } else {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] &= ~mask;
                    }
                }
            }
            for (int y = 0; y < scrollAmount && y < this.imageHeight; y++) {
                for (int x = 0; x < this.imageWidth; x++) {
                    this.bitplaneBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollRight() {
        int scrollAmount = this.hiresMode ? 4 : 8;
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = this.imageWidth - 1; i >= 0; i--) {
                int shiftedHorizontalPosition = i + scrollAmount;
                if (shiftedHorizontalPosition >= this.imageWidth) {
                    continue;
                }
                for (int j = 0; j < this.imageHeight; j++) {
                    int val = this.bitplaneBuffer[i][j] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] |= mask;
                    } else {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] &= ~mask;
                    }
                }
            }
            for (int x = 0; x < scrollAmount && x < this.imageWidth; x++) {
                for (int y = 0; y < this.imageHeight; y++) {
                    this.bitplaneBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollLeft() {
        int scrollAmount = this.hiresMode ? 4 : 8;
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < this.imageWidth; i++) {
                int shiftedHorizontalPosition = i - scrollAmount;
                if (shiftedHorizontalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.imageHeight; j++) {
                    int val = this.bitplaneBuffer[i][j] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] |= mask;
                    } else {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] &= ~mask;
                    }
                }
            }
            for (int x = this.imageWidth - scrollAmount; x < this.imageWidth; x++) {
                if (x < 0) {
                    continue;
                }
                for (int y = 0; y < this.imageHeight; y++) {
                    this.bitplaneBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    public void clear() {
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < this.bitplaneBuffer.length; i++) {
                for (int j = 0; j < this.bitplaneBuffer[i].length; j++) {
                    this.bitplaneBuffer[i][j] &= ~mask;
                }
            }
        }
    }

}
