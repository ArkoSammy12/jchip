package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.util.CharacterSpriteFont;
import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

public abstract class AbstractDisplay implements Display {

    private final CharacterSpriteFont characterSpriteFont;
    protected final Chip8Variant chip8Variant;
    protected final ColorPalette colorPalette;
    protected final int[][] frameBuffer = new int[128][64];

    protected final int screenWidth;
    protected final int screenHeight;
    private boolean extendedMode = false;
    private int selectedBitPlanes = 1;

    private final String romTitle;
    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;

    private final StringBuilder stringBuilder = new StringBuilder(128);

    public AbstractDisplay(EmulatorConfig config) {
        String romTitle = config.getProgramTitle();
        Chip8Variant chip8Variant = config.getConsoleVariant();
        ColorPalette colorPalette = config.getColorPalette();

        if (romTitle == null) {
            this.romTitle = "";
        } else {
            this.romTitle = " | " + romTitle;
        }
        if (chip8Variant == Chip8Variant.CHIP_8) {
            this.screenWidth = 64;
            this.screenHeight = 32;
        } else {
            this.screenWidth = 128;
            this.screenHeight = 64;
        }
        this.colorPalette = colorPalette;
        this.chip8Variant = chip8Variant;
        this.characterSpriteFont = new CharacterSpriteFont(chip8Variant);
    }

    @Override
    public CharacterSpriteFont getCharacterSpriteFont() {
        return this.characterSpriteFont;
    }

    @Override
    public int getFrameBufferWidth() {
        return this.screenWidth;
    }

    @Override
    public int getFrameBufferHeight() {
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
    public boolean togglePixel(int bitPlaneIndex, int column, int row) {
        if (column >= this.screenWidth || column < 0 || row >= this.screenHeight || row < 0) {
            return false;
        }
        boolean currentPixel = this.getPixel(bitPlaneIndex, column, row);
        this.setPixel(bitPlaneIndex, column, row, !currentPixel);
        return currentPixel;
    }

    public void setPixel(int bitPlaneIndex, int column, int row, boolean value) {
        if (value) {
            frameBuffer[column][row] |= 1 << bitPlaneIndex;
        } else {
            frameBuffer[column][row] &= ~(1 << bitPlaneIndex);
        }
    }

    @Override
    public boolean getPixel(int bitPlaneIndex, int column, int row) {
        return ((frameBuffer[column][row] >> bitPlaneIndex) & 1) != 0;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void scrollUp(int scrollAmount) {
        int trueScrollAmount;
        if (chip8Variant == Chip8Variant.SUPER_CHIP_LEGACY) {
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
        if (chip8Variant == Chip8Variant.SUPER_CHIP_LEGACY) {
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
        if (chip8Variant == Chip8Variant.SUPER_CHIP_LEGACY) {
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
    @SuppressWarnings("DuplicatedCode")
    public void scrollLeft() {
        int scrollAmount;
        if (chip8Variant == Chip8Variant.SUPER_CHIP_LEGACY) {
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

    protected String getWindowTitle(int currentInstructionsPerFrame) {
        long now = System.nanoTime();
        double lastFrameDuration = now - this.lastFrameTime;
        this.lastFrameTime = now;
        this.totalFrameTimeSinceLastUpdate += lastFrameDuration;
        this.totalIpfSinceLastUpdate += currentInstructionsPerFrame;
        this.framesSinceLastUpdate++;
        long deltaTime = now - lastWindowTitleUpdate;
        if (deltaTime < 1_000_000_000L) {
            return null;
        }
        double lastFps = this.framesSinceLastUpdate / (deltaTime / 1_000_000_000.0);
        long averageInstructionsPerFrame = this.totalIpfSinceLastUpdate / this.framesSinceLastUpdate;
        double averageFrameTimeMs = (this.totalFrameTimeSinceLastUpdate / this.framesSinceLastUpdate) / 1_000_000.0;
        double mips = (averageInstructionsPerFrame * lastFps) / 1_000_000;
        this.framesSinceLastUpdate = 0;
        this.totalFrameTimeSinceLastUpdate = 0;
        this.totalIpfSinceLastUpdate = 0;
        this.lastWindowTitleUpdate = now;

        stringBuilder.append("jchip ").append(Main.VERSION_STRING)
                .append(" | ").append(chip8Variant.getDisplayName())
                .append(romTitle)
                .append(" | IPF: ").append(averageInstructionsPerFrame)
                .append(" | MIPS: ").append((long)(mips * 100) / 100.0)  // round to 2 decimals
                .append(" | Frame Time: ").append((long)(averageFrameTimeMs * 100) / 100.0)
                .append(" ms | FPS: ").append((long)(lastFps * 100) / 100.0);

        String titleString = stringBuilder.toString();
        stringBuilder.setLength(0);
        return titleString;

    }

}
