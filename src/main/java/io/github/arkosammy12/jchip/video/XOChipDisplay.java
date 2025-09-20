package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;

public class XOChipDisplay extends SChipDisplay {

    public static final int BITPLANE_BASE_MASK = 1 << 3;

    private int selectedBitPlanes = 1;

    public XOChipDisplay(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config, keyAdapter);
        this.isModern = true;
    }

    public void setSelectedBitPlanes(int selectedBitPlanes) {
        this.selectedBitPlanes = selectedBitPlanes;
    }

    public int getSelectedBitPlanes() {
        return this.selectedBitPlanes;
    }

    public boolean togglePixelAtBitPlanes(int bitPlaneMask, int column, int row) {
        boolean collision = (this.frameBuffer[column][row] & bitPlaneMask) != 0;
        this.frameBuffer[column][row] ^= bitPlaneMask;
        return collision;
    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollUp(int scrollAmount) {
        int trueScrollAmount;
        if (this.extendedMode) {
            trueScrollAmount = scrollAmount;
        } else {
            trueScrollAmount = scrollAmount * 2;
        }

        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < this.screenHeight; i++) {
                int shiftedVerticalPosition = i - trueScrollAmount;
                if (shiftedVerticalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.screenWidth; j++) {
                    int val = this.frameBuffer[j][i] & mask;
                    if (val != 0) {
                        this.frameBuffer[j][shiftedVerticalPosition] |= mask;
                    } else {
                        this.frameBuffer[j][shiftedVerticalPosition] &= ~mask;
                    }
                }
            }
            // Clear the bottom scrollOffset rows
            for (int y = this.screenHeight - trueScrollAmount; y < this.screenHeight; y++) {
                if (y < 0) {
                    continue;
                }
                for (int x = 0; x < this.screenWidth; x++) {
                    this.frameBuffer[x][y] &= ~mask;
                }
            }

        }

    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollDown(int scrollAmount) {
        int trueScrollAmount;
        if (this.extendedMode) {
            trueScrollAmount = scrollAmount;
        } else {
            trueScrollAmount = scrollAmount * 2;
        }

        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = this.screenHeight - 1; i >= 0; i--) {
                int shiftedVerticalPosition = trueScrollAmount + i;
                if (shiftedVerticalPosition >= this.screenHeight) {
                    continue;
                }
                for (int j = 0; j < this.screenWidth; j++) {
                    int val = this.frameBuffer[j][i] & mask;
                    if (val != 0) {
                        this.frameBuffer[j][shiftedVerticalPosition] |= mask;
                    } else {
                        this.frameBuffer[j][shiftedVerticalPosition] &= ~mask;
                    }
                }
            }
            // Clear the top scrollOffset rows
            for (int y = 0; y < trueScrollAmount && y < this.screenHeight; y++) {
                for (int x = 0; x < this.screenWidth; x++) {
                    this.frameBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollRight() {
        int scrollAmount;
        if (this.extendedMode) {
            scrollAmount = 4;
        } else {
            scrollAmount = 8;
        }

        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = this.screenWidth - 1; i >= 0; i--) {
                int shiftedHorizontalPosition = i + scrollAmount;
                if (shiftedHorizontalPosition >= this.screenWidth) {
                    continue;
                }
                for (int j = 0; j < this.screenHeight; j++) {
                    int val = this.frameBuffer[i][j] & mask;
                    if (val != 0) {
                        this.frameBuffer[shiftedHorizontalPosition][j] |= mask;
                    } else {
                        this.frameBuffer[shiftedHorizontalPosition][j] &= ~mask;
                    }
                }
            }
            // Clear the leftmost 4 columns
            for (int x = 0; x < scrollAmount && x < this.screenWidth; x++) {
                for (int y = 0; y < this.screenHeight; y++) {
                    this.frameBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollLeft() {
        int scrollAmount;
        if (this.extendedMode) {
            scrollAmount = 4;
        } else {
            scrollAmount = 8;
        }

        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < this.screenWidth; i++) {
                int shiftedHorizontalPosition = i - scrollAmount;
                if (shiftedHorizontalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.screenHeight; j++) {
                    int val = this.frameBuffer[i][j] & mask;
                    if (val != 0) {
                        this.frameBuffer[shiftedHorizontalPosition][j] |= mask;
                    } else {
                        this.frameBuffer[shiftedHorizontalPosition][j] &= ~mask;
                    }
                }
            }
            for (int x = this.screenWidth - scrollAmount; x < this.screenWidth; x++) {
                if (x < 0) {
                    continue;
                }
                for (int y = 0; y < this.screenHeight; y++) {
                    this.frameBuffer[x][y] &= ~mask;
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
            for (int i = 0; i < this.frameBuffer.length; i++) {
                for (int j = 0; j < this.frameBuffer[i].length; j++) {
                    this.frameBuffer[i][j] &= ~mask;
                }
            }
        }
    }

}
