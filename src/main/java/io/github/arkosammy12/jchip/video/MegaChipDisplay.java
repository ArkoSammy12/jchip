package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;

public class MegaChipDisplay extends SChipDisplay {

    private final int[][] backBuffer = new int[256][256];
    private final int[][] indexBuffer = new int[256][256];
    private final int[][] frontBuffer = new int[256][256];
    private final int[] colorPalette = new int[256];

    private int spriteWidth = 0;
    private int spriteHeight = 0;
    private int screenAlpha = 0;
    private int collisionIndex = 0;
    private BlendMode blendMode = BlendMode.BLEND_NORMAL;
    private boolean megaChipModeEnabled = false;
    private boolean scrollTriggered = false;

    public MegaChipDisplay(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config, keyAdapter);
        this.colorPalette[0] = 0x00000000;
        this.colorPalette[255] = 0xFFFFFFFF;
    }

    @Override
    public int getWidth() {
        return 256;
    }

    @Override
    public int getHeight() {
        return 192;
    }

    @Override
    protected int getPixelScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 1;
            default -> 4;
        };
    }

    public void setSpriteWidth(int width) {
        this.spriteWidth = width < 1 ? 256 : width;
    }

    public int getSpriteWidth() {
        return this.spriteWidth;
    }

    public void setSpriteHeight(int height) {
        this.spriteHeight = height < 1 ? 256 : height;
    }

    public int getSpriteHeight() {
        return this.spriteHeight;
    }

    public void setScreenAlpha(int alpha) {
        this.screenAlpha = alpha;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public void loadPaletteEntry(int index, int argb) {
        if (index < 1) {
            return;
        }
        this.colorPalette[index] = argb ;
    }

    public void setCollisionIndex(int index) {
        this.collisionIndex = index & 0xFF;
    }

    public int getCollisionIndex() {
        return this.collisionIndex;
    }

    public int getColorForIndex(int index) {
        return this.colorPalette[index];
    }

    public int getColorIndexAt(int column, int row) {
        return this.indexBuffer[column][row];
    }

    public void setMegaChipMode(boolean value) {
        this.megaChipModeEnabled = value;
    }

    public boolean isMegaChipModeEnabled() {
        return this.megaChipModeEnabled;
    }

    public void setUpdateScrollTriggered() {
        this.scrollTriggered = true;
    }

    @Override
    public void setPixel(int column, int row, int val) {
        if (!isMegaChipModeEnabled()) {
            super.setPixel(column, row, val);
            return;
        }
        int src = this.colorPalette[val];
        int dst = this.backBuffer[column][row];
        int result = switch (this.blendMode) {
            case BLEND_NORMAL -> src;
            case BLEND_25 -> blendAlpha(src, dst, 64);
            case BLEND_50 -> blendAlpha(src, dst, 128);
            case BLEND_75 -> blendAlpha(src, dst, 192);
            case BLEND_ADD -> addColors(src, dst);
            case BLEND_MULTIPLY -> multiplyColors(src, dst);
        };
        this.backBuffer[column][row] = result;
        this.indexBuffer[column][row] = val;
    }

    public void drawFontPixel(int column, int row) {
        this.backBuffer[column][row] = 0xFFFFFFFF;
        this.indexBuffer[column][row] = 255;
    }

    public void scrollUp(int scrollAmount) {
        for (int i = 0; i < this.screenHeight; i++) {
            int shiftedVerticalPosition = i - scrollAmount;
            if (shiftedVerticalPosition < 0) {
                continue;
            }
            for (int j = 0; j < this.screenWidth; j++) {
                this.frontBuffer[j][shiftedVerticalPosition] = this.frontBuffer[j][i];
            }
        }
        // Clear the bottom scrollOffset rows
        for (int y = this.screenHeight - scrollAmount; y < this.screenHeight; y++) {
            if (y < 0) {
                continue;
            }
            for (int x = 0; x < this.screenWidth; x++) {
                this.frontBuffer[x][y] = 0x000000;
            }
        }
    }

    public void scrollDown(int scrollAmount) {
        if (!isMegaChipModeEnabled()) {
            super.scrollDown(scrollAmount);
            return;
        }

        for (int i = this.screenHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = scrollAmount + i;
            if (shiftedVerticalPosition >= this.screenHeight) {
                continue;
            }
            for (int j = 0; j < this.screenWidth; j++) {
                this.frontBuffer[j][shiftedVerticalPosition] = this.frontBuffer[j][i];
            }
        }
        // Clear the top scrollOffset rows
        for (int y = 0; y < scrollAmount && y < this.screenHeight; y++) {
            for (int x = 0; x < this.screenWidth; x++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }

    }

    public void scrollRight() {
        if (!isMegaChipModeEnabled()) {
            super.scrollRight();
            return;
        }

        for (int i = this.screenWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + 4;
            if (shiftedHorizontalPosition >= this.screenWidth) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.frontBuffer[shiftedHorizontalPosition][j] = this.frontBuffer[i][j];
            }
        }
        // Clear the leftmost 4 columns
        for (int x = 0; x < 4 && x < this.screenWidth; x++) {
            for (int y = 0; y < this.screenHeight; y++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }

    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollLeft() {
        if (!isMegaChipModeEnabled()) {
            super.scrollLeft();
            return;
        }

        for (int i = 0; i < this.screenWidth; i++) {
            int shiftedHorizontalPosition = i - 4;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.frontBuffer[shiftedHorizontalPosition][j] = this.frontBuffer[i][j];
            }
        }
        for (int x = this.screenWidth - 4; x < this.screenWidth; x++) {
            if (x < 0) {
                continue;
            }
            for (int y = 0; y < this.screenHeight; y++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }

    }

    @Override
    protected void populateDataBuffer(int[] buffer) {
        if (!isMegaChipModeEnabled()) {
            for (int y = 0; y < screenHeight; y++) {
                int base = y * screenWidth;
                for (int x = 0; x < screenWidth; x++) {
                    buffer[base + x] = 0xFF000000;
                }
            }
            int superWidth = super.getWidth();
            int superHeight = super.getHeight();
            int xScale = 2;
            int yScale = 2;
            int yOffset = (screenHeight - superHeight * yScale) / 2;
            for (int sy = 0; sy < superHeight; sy++) {
                for (int sx = 0; sx < superWidth; sx++) {
                    int color = super.colorPalette.getColorARGB(this.frameBuffer[sx][sy] & 0xF);
                    int baseY = yOffset + sy * yScale;
                    int baseX = sx * xScale;
                    for (int dy = 0; dy < yScale; dy++) {
                        int y = baseY + dy;
                        int rowBase = y * screenWidth;
                        for (int dx = 0; dx < xScale; dx++) {
                            int x = baseX + dx;
                            buffer[rowBase + x] = color;
                        }
                    }
                }
            }
            return;
        }
        for (int y = 0; y < screenHeight; y++) {
            int base = y * screenWidth;
            for (int x = 0; x < screenWidth; x++) {
                buffer[base + x] = 0xFF000000;
            }
        }
        for (int y = 0; y < screenHeight; y++) {
            int base = y * screenWidth;
            for (int x = 0; x < screenWidth; x++) {
                if (scrollTriggered) {
                    int back = this.backBuffer[x][y];
                    if ((back & 0xFF000000) != 0) {
                        buffer[base + x] = back;
                    }
                }
                int front = this.frontBuffer[x][y];
                if ((front & 0xFF000000) != 0) {
                    buffer[base + x] = front;
                }
            }
        }
    }

    public void flushBackBuffer() {
        this.scrollTriggered = false;
        for (int i = 0; i < this.backBuffer.length; i++) {
            for (int j = 0; j < this.backBuffer[i].length; j++) {
                this.frontBuffer[i][j] = this.backBuffer[i][j];
            }
        }
    }


    @Override
    public void clear() {
        if (!isMegaChipModeEnabled()) {
            super.clear();
            return;
        }
        for (int i = 0; i < this.backBuffer.length; i++) {
            for (int j = 0; j < this.backBuffer[i].length; j++) {
                this.backBuffer[i][j] = colorPalette[0];
                this.indexBuffer[i][j] = 0;
            }
        }
    }

    public void clearIndexBuffer() {
        for (int i = 0; i < this.backBuffer.length; i++) {
            for (int j = 0; j < this.backBuffer[i].length; j++) {
                this.indexBuffer[i][j] = 0;
            }
        }
    }

    private int blendAlpha(int src, int dst, int alpha) {
        int invAlpha = 255 - alpha;

        int sr = (src >> 16) & 0xFF;
        int sg = (src >> 8) & 0xFF;
        int sb = src & 0xFF;

        int dr = (dst >> 16) & 0xFF;
        int dg = (dst >> 8) & 0xFF;
        int db = dst & 0xFF;

        int r = (sr * alpha + dr * invAlpha) / 255;
        int g = (sg * alpha + dg * invAlpha) / 255;
        int b = (sb * alpha + db * invAlpha) / 255;

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int addColors(int src, int dst) {
        int sr = (src >> 16) & 0xFF;
        int sg = (src >> 8) & 0xFF;
        int sb = src & 0xFF;

        int dr = (dst >> 16) & 0xFF;
        int dg = (dst >> 8) & 0xFF;
        int db = dst & 0xFF;

        int r = Math.min(sr + dr, 255);
        int g = Math.min(sg + dg, 255);
        int b = Math.min(sb + db, 255);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int multiplyColors(int src, int dst) {
        int sr = (src >> 16) & 0xFF;
        int sg = (src >> 8) & 0xFF;
        int sb = src & 0xFF;

        int dr = (dst >> 16) & 0xFF;
        int dg = (dst >> 8) & 0xFF;
        int db = dst & 0xFF;

        int r = (sr * dr) / 255;
        int g = (sg * dg) / 255;
        int b = (sb * db) / 255;

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }



    public enum BlendMode {
        BLEND_NORMAL,
        BLEND_25,
        BLEND_50,
        BLEND_75,
        BLEND_ADD,
        BLEND_MULTIPLY
    }

}
