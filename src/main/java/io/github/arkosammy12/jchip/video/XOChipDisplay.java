package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;
import java.util.List;

public class XOChipDisplay extends SChipDisplay {

    public static final int BITPLANE_BASE_MASK = 1 << 3;

    private int selectedBitPlanes = 1;

    public XOChipDisplay(EmulatorConfig config, List<KeyAdapter> keyAdapters) {
        super(config, keyAdapters, true);
    }

    @Override
    public void reset() {
        super.reset();
        this.selectedBitPlanes = 1;
    }

    public void setSelectedBitPlanes(int selectedBitPlanes) {
        this.selectedBitPlanes = selectedBitPlanes;
    }

    public int getSelectedBitPlanes() {
        return this.selectedBitPlanes;
    }

    public boolean togglePixelAtBitPlanes(int column, int row, int bitPlaneMask) {
        boolean collision = (this.bitplaneBuffer[column][row] & bitPlaneMask) != 0;
        this.bitplaneBuffer[column][row] ^= bitPlaneMask;
        return collision;
    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollUp(int scrollAmount) {
        int trueScrollAmount = this.extendedMode ? scrollAmount : scrollAmount * 2;
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < this.displayHeight; i++) {
                int shiftedVerticalPosition = i - trueScrollAmount;
                if (shiftedVerticalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.displayWidth; j++) {
                    int val = this.bitplaneBuffer[j][i] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] |= mask;
                    } else {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] &= ~mask;
                    }
                }
            }
            for (int y = this.displayHeight - trueScrollAmount; y < this.displayHeight; y++) {
                if (y < 0) {
                    continue;
                }
                for (int x = 0; x < this.displayWidth; x++) {
                    this.bitplaneBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollDown(int scrollAmount) {
        int trueScrollAmount = this.extendedMode ? scrollAmount : scrollAmount * 2;
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = this.displayHeight - 1; i >= 0; i--) {
                int shiftedVerticalPosition = trueScrollAmount + i;
                if (shiftedVerticalPosition >= this.displayHeight) {
                    continue;
                }
                for (int j = 0; j < this.displayWidth; j++) {
                    int val = this.bitplaneBuffer[j][i] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] |= mask;
                    } else {
                        this.bitplaneBuffer[j][shiftedVerticalPosition] &= ~mask;
                    }
                }
            }
            for (int y = 0; y < trueScrollAmount && y < this.displayHeight; y++) {
                for (int x = 0; x < this.displayWidth; x++) {
                    this.bitplaneBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollRight() {
        int scrollAmount = this.extendedMode ? 4 : 8;
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = this.displayWidth - 1; i >= 0; i--) {
                int shiftedHorizontalPosition = i + scrollAmount;
                if (shiftedHorizontalPosition >= this.displayWidth) {
                    continue;
                }
                for (int j = 0; j < this.displayHeight; j++) {
                    int val = this.bitplaneBuffer[i][j] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] |= mask;
                    } else {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] &= ~mask;
                    }
                }
            }
            for (int x = 0; x < scrollAmount && x < this.displayWidth; x++) {
                for (int y = 0; y < this.displayHeight; y++) {
                    this.bitplaneBuffer[x][y] &= ~mask;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollLeft() {
        int scrollAmount = this.extendedMode ? 4 : 8;
        for (int mask = BITPLANE_BASE_MASK; mask > 0; mask >>>= 1) {
            if ((mask & this.selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < this.displayWidth; i++) {
                int shiftedHorizontalPosition = i - scrollAmount;
                if (shiftedHorizontalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.displayHeight; j++) {
                    int val = this.bitplaneBuffer[i][j] & mask;
                    if (val != 0) {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] |= mask;
                    } else {
                        this.bitplaneBuffer[shiftedHorizontalPosition][j] &= ~mask;
                    }
                }
            }
            for (int x = this.displayWidth - scrollAmount; x < this.displayWidth; x++) {
                if (x < 0) {
                    continue;
                }
                for (int y = 0; y < this.displayHeight; y++) {
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
