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
        super(config, keyAdapter, false);
        this.colorPalette[0] = 0x00000000;
        this.colorPalette[255] = 0xFFFFFFFF;
    }

    @Override
    public int getWidth() {
        if (!this.isMegaChipModeEnabled()) {
            return super.getWidth();
        }
        return 256;
    }

    @Override
    public int getHeight() {
        if (!this.isMegaChipModeEnabled()) {
            return super.getWidth();
        }
        return 256;
    }

    @Override
    protected int getRenderWidth() {
        return 256;
    }

    @Override
    protected int getRenderHeight() {
        return 192;
    }

    @Override
    protected int getPixelRenderScale(DisplayAngle displayAngle) {
        return switch (displayAngle) {
            case DEG_90, DEG_270 -> 3;
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
        if (!this.isMegaChipModeEnabled()) {
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
        // Use hardcoded opaque white for drawing font pixels, and set the index buffer to 255
        this.backBuffer[column][row] = 0xFFFFFFFF;
        this.indexBuffer[column][row] = 255;
    }

    public void scrollUp(int scrollAmount) {
        for (int i = 0; i < this.displayHeight; i++) {
            int shiftedVerticalPosition = i - scrollAmount;
            if (shiftedVerticalPosition < 0) {
                continue;
            }
            for (int j = 0; j < this.displayWidth; j++) {
                this.frontBuffer[j][shiftedVerticalPosition] = this.frontBuffer[j][i];
            }
        }
        for (int y = this.displayHeight - scrollAmount; y < this.displayHeight; y++) {
            if (y < 0) {
                continue;
            }
            for (int x = 0; x < this.displayWidth; x++) {
                this.frontBuffer[x][y] = 0x000000;
            }
        }
    }

    public void scrollDown(int scrollAmount) {
        if (!isMegaChipModeEnabled()) {
            super.scrollDown(scrollAmount);
            return;
        }

        for (int i = this.displayHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = scrollAmount + i;
            if (shiftedVerticalPosition >= this.displayHeight) {
                continue;
            }
            for (int j = 0; j < this.displayWidth; j++) {
                this.frontBuffer[j][shiftedVerticalPosition] = this.frontBuffer[j][i];
            }
        }
        for (int y = 0; y < scrollAmount && y < this.displayHeight; y++) {
            for (int x = 0; x < this.displayWidth; x++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }

    }

    public void scrollRight() {
        if (!isMegaChipModeEnabled()) {
            super.scrollRight();
            return;
        }

        for (int i = this.displayWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + 4;
            if (shiftedHorizontalPosition >= this.displayWidth) {
                continue;
            }
            if (this.displayHeight >= 0) {
                System.arraycopy(this.frontBuffer[i], 0, this.frontBuffer[shiftedHorizontalPosition], 0, this.displayHeight);
            }
        }
        for (int x = 0; x < 4 && x < this.displayWidth; x++) {
            for (int y = 0; y < this.displayHeight; y++) {
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

        for (int i = 0; i < this.displayWidth; i++) {
            int shiftedHorizontalPosition = i - 4;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            if (this.displayHeight >= 0) {
                System.arraycopy(this.frontBuffer[i], 0, this.frontBuffer[shiftedHorizontalPosition], 0, this.displayHeight);
            }
        }
        for (int x = this.displayWidth - 4; x < this.displayWidth; x++) {
            if (x < 0) {
                continue;
            }
            for (int y = 0; y < this.displayHeight; y++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }

    }

    @Override
    protected void fillRenderBuffer(int[] buffer) {
        if (this.isMegaChipModeEnabled()) {
            for (int y = 0; y < displayHeight; y++) {
                int base = y * displayWidth;
                for (int x = 0; x < displayWidth; x++) {
                    int pixel = 0xFF000000;
                    if (scrollTriggered) {
                        int back = this.backBuffer[x][y];
                        if ((back & 0xFF000000) != 0) {
                            pixel = back;
                        }
                    }
                    int front = this.frontBuffer[x][y];
                    if ((front & 0xFF000000) != 0) {
                        pixel = front;
                    }
                    buffer[base + x] = pixel;
                }
            }
        } else {
            for (int y = 0; y < displayHeight; y++) {
                int base = y * displayWidth;
                for (int x = 0; x < displayWidth; x++) {
                    buffer[base + x] = 0xFF000000;
                }
            }
            int displayWidth = super.getRenderWidth();
            int displayHeight = super.getRenderHeight();
            int xScale = 2;
            int yScale = 2;
            int yOffset = (this.displayHeight - displayHeight * yScale) / 2;
            for (int sy = 0; sy < displayHeight; sy++) {
                int baseY = yOffset + sy * yScale;
                for (int sx = 0; sx < displayWidth; sx++) {
                    int color = super.colorPalette.getColorARGB(this.bitplaneBuffer[sx][sy] & 0xF);
                    int baseX = sx * xScale;
                    for (int dy = 0; dy < yScale; dy++) {
                        int rowBase = (baseY + dy) * this.displayWidth;
                        for (int dx = 0; dx < xScale; dx++) {
                            buffer[rowBase + baseX + dx] = color;
                        }
                    }
                }
            }
        }
    }

    public void flushBackBuffer() {
        this.scrollTriggered = false;
        for (int i = 0; i < this.backBuffer.length; i++) {
            System.arraycopy(this.backBuffer[i], 0, this.frontBuffer[i], 0, this.backBuffer[i].length);
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

    @SuppressWarnings("DuplicatedCode")
    private static int blendAlpha(int src, int dst, int alpha) {
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

    @SuppressWarnings("DuplicatedCode")
    private static int addColors(int src, int dst) {
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

    @SuppressWarnings("DuplicatedCode")
    private static int multiplyColors(int src, int dst) {
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
