package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.util.CharacterSpriteFont;
import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.ConsoleVariant;


public abstract class AbstractDisplay implements Display {

    private final CharacterSpriteFont characterSpriteFont;
    protected final ConsoleVariant consoleVariant;
    protected final ColorPalette colorPalette;
    protected final int[][] frameBuffer = new int[128][64];
    protected final int[][] previousFrameBuffer = new int[128][64];

    protected final int screenWidth;
    protected final int screenHeight;
    private boolean extendedMode = false;
    private int selectedBitPlanes = 1;

    protected final String title;

    public AbstractDisplay(String title, ConsoleVariant consoleVariant, ColorPalette colorPalette) {
        this.title = title;
        if (consoleVariant == ConsoleVariant.CHIP_8) {
            this.screenWidth = 64;
            this.screenHeight = 32;
        } else {
            this.screenWidth = 128;
            this.screenHeight = 64;
        }
        this.colorPalette = colorPalette;
        this.consoleVariant = consoleVariant;
        this.characterSpriteFont = new CharacterSpriteFont(consoleVariant);
    }


    @Override
    public int getWidth() {
        return this.screenWidth;
    }

    @Override
    public int getHeight() {
        return this.screenHeight;
    }

    @Override
    public void setExtendedMode(boolean extendedMode) {
        this.extendedMode = extendedMode;
    }

    @Override
    public boolean isExtendedMode() {
        return this.extendedMode;
    }

    @Override
    public void setSelectedBitPlanes(int selectedBitPlanes) {
        this.selectedBitPlanes = selectedBitPlanes;
    }

    @Override
    public int getSelectedBitPlanes() {
        return this.selectedBitPlanes;
    }

    @Override
    public CharacterSpriteFont getCharacterFont() {
        return this.characterSpriteFont;
    }

    @Override
    public boolean togglePixel(int bitPlaneIndex, int column, int row) {
        if (column >= this.screenWidth || column < 0 || row >= this.screenHeight || row < 0) {
            return false;
        }
        boolean pixelSet = this.getPixel(bitPlaneIndex, column, row);
        boolean newPixel = true;
        boolean collided = false;
        if (pixelSet) {
            newPixel = false;
            collided = true;
        }
        this.setPixel(bitPlaneIndex, column, row, newPixel);
        return collided;
    }

    public void setPixel(int bitPlaneIndex, int column, int row, boolean value) {
        int mask = 1 << bitPlaneIndex;
        int newPixel = frameBuffer[column][row];
        if (value) {
            newPixel |= mask;
        } else {
            newPixel &= ~mask;
        }
        frameBuffer[column][row] = newPixel;
    }

    @Override
    public boolean getPixel(int bitPlaneIndex, int column, int row) {
        int bit = (this.frameBuffer[column][row] >> bitPlaneIndex) & 1;
        return bit == 1;
    }

    @Override
    public void scrollUp(int scrollAmount) {
        int trueScrollAmount;
        if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
            trueScrollAmount = scrollAmount;
        } else {
            if (this.extendedMode) {
                trueScrollAmount = scrollAmount;
            } else {
                trueScrollAmount = scrollAmount * 2;
            }
        }
        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = 1 << bitPlane;
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < this.screenHeight; i++) {
                int shiftedVerticalPosition = i - trueScrollAmount;
                if (shiftedVerticalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.screenWidth; j++) {
                    this.setPixel(bitPlane, j, shiftedVerticalPosition, this.getPixel(bitPlane, j, i));
                }
            }
            // Clear the bottom scrollOffset rows
            for (int y = this.screenHeight - trueScrollAmount; y < this.screenHeight; y++) {
                if (y < 0) {
                    continue;
                }
                for (int x = 0; x < this.screenWidth; x++) {
                    this.setPixel(bitPlane, x, y, false);
                }
            }
        }
    }

    @Override
    public void scrollDown(int scrollAmount) {
        int trueScrollAmount;
        if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
            trueScrollAmount = scrollAmount;
        } else {
            if (this.extendedMode) {
                trueScrollAmount = scrollAmount;
            } else {
                trueScrollAmount = scrollAmount * 2;
            }
        }

        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = 1 << bitPlane;
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = this.screenHeight - 1; i >= 0; i--) {
                int shiftedVerticalPosition = trueScrollAmount + i;
                if (shiftedVerticalPosition >= this.screenHeight) {
                    continue;
                }
                for (int j = 0; j < this.screenWidth; j++) {
                    this.setPixel(bitPlane, j, shiftedVerticalPosition, this.getPixel(bitPlane, j, i));
                }
            }
            // Clear the top scrollOffset rows
            for (int y = 0; y < trueScrollAmount && y < this.screenHeight; y++) {
                for (int x = 0; x < this.screenWidth; x++) {
                    this.setPixel(bitPlane, x, y, false);
                }
            }
        }
    }

    @Override
    public void scrollRight() {
        int scrollAmount;
        if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
            scrollAmount = 4;
        } else {
            if (this.extendedMode) {
                scrollAmount = 4;
            } else {
                scrollAmount = 8;
            }
        }
        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = 1 << bitPlane;
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = this.screenWidth - 1; i >= 0; i--) {
                int shiftedHorizontalPosition = i + scrollAmount;
                if (shiftedHorizontalPosition >= this.screenWidth) {
                    continue;
                }
                for (int j = 0; j < this.screenHeight; j++) {
                    this.setPixel(bitPlane, shiftedHorizontalPosition, j, this.getPixel(bitPlane, i, j));
                }
            }
            // Clear the leftmost 4 columns
            for (int x = 0; x < scrollAmount && x < this.screenWidth; x++) {
                for (int y = 0; y < this.screenHeight; y++) {
                    this.setPixel(bitPlane, x, y, false);
                }
            }
        }
    }

    @Override
    public void scrollLeft() {
        int scrollAmount;
        if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
            scrollAmount = 4;
        } else {
            if (this.extendedMode) {
                scrollAmount = 4;
            } else {
                scrollAmount = 8;
            }
        }
        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = 1 << bitPlane;
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < this.screenWidth; i++) {
                int shiftedHorizontalPosition = i - scrollAmount;
                if (shiftedHorizontalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.screenHeight; j++) {
                    this.setPixel(bitPlane, shiftedHorizontalPosition, j, this.getPixel(bitPlane, i, j));
                }
            }
            for (int x = this.screenWidth - scrollAmount; x < this.screenWidth; x++) {
                if (x < 0) {
                    continue;
                }
                for (int y = 0; y < this.screenHeight; y++) {
                    this.setPixel(bitPlane, x, y, false);
                }
            }
        }
    }

    @Override
    public void clear() {
        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = 1 << bitPlane;
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < this.frameBuffer.length; i++) {
                for (int j = 0; j < this.frameBuffer[i].length; j++) {
                    this.setPixel(bitPlane, i, j, false);
                }
            }
        }
    }

}
