package io.github.arkosammy12.jchip.emulators.video;

import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;

public class MegaChipDisplay<E extends MegaChipEmulator> extends SChip11Display<E> {

    private final int[][] backBuffer = new int[256][256];
    private final int[][] indexBuffer = new int[256][256];
    private final int[][] frontBuffer = new int[256][256];
    private final int[] colorPalette = new int[256];
    private int spriteWidth = 0;
    private int spriteHeight = 0;
    private int screenAlpha = 0xFF;
    private int collisionIndex = 0;
    private BlendMode blendMode = BlendMode.BLEND_NORMAL;

    private boolean scrollTriggered = false;

    public MegaChipDisplay(E emulator) {
        super(emulator);
        this.colorPalette[0] = 0x00000000;
        this.colorPalette[255] = 0xFFFFFFFF;
    }

    @Override
    public int getWidth() {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            return super.getWidth();
        }
        return 256;
    }

    @Override
    public int getHeight() {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            return super.getHeight();
        }
        return 256;
    }

    @Override
    public int getImageWidth() {
        return 256;
    }

    @Override
    public int getImageHeight() {
        return 192;
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
        this.screenAlpha = alpha & 0xFF;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public void loadPaletteEntry(int index, int argb) {
        if (index < 1) {
            return;
        }
        this.colorPalette[index] = argb;
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

    public void setDisplayUpdateScrollTriggered() {
        this.scrollTriggered = true;
    }

    @Override
    public void setPixel(int column, int row, int val) {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            super.setPixel(column, row, val);
            return;
        }
        this.backBuffer[column][row] = switch (this.blendMode) {
            case BLEND_NORMAL -> this.colorPalette[val];
            case BLEND_25 -> blendAlpha(this.colorPalette[val], this.backBuffer[column][row], 64);
            case BLEND_50 -> blendAlpha(this.colorPalette[val], this.backBuffer[column][row], 128);
            case BLEND_75 -> blendAlpha(this.colorPalette[val], this.backBuffer[column][row], 192);
            case BLEND_ADD -> addColors(this.colorPalette[val], this.backBuffer[column][row]);
            case BLEND_MULTIPLY -> multiplyColors(this.colorPalette[val], this.backBuffer[column][row]);
        };
        this.indexBuffer[column][row] = val;
    }

    public void drawFontPixel(int column, int row) {
        // Use hardcoded opaque white for drawing font pixels, and set the index buffer to 255
        this.backBuffer[column][row] = 0xFFFFFFFF;
        this.indexBuffer[column][row] = 255;
    }

    public void scrollUp(int scrollAmount) {
        for (int i = 0; i < this.imageHeight; i++) {
            int shiftedVerticalPosition = i - scrollAmount;
            if (shiftedVerticalPosition < 0) {
                continue;
            }
            for (int j = 0; j < this.imageWidth; j++) {
                this.frontBuffer[j][shiftedVerticalPosition] = this.frontBuffer[j][i];
            }
        }
        for (int y = this.imageHeight - scrollAmount; y < this.imageHeight; y++) {
            if (y < 0) {
                continue;
            }
            for (int x = 0; x < this.imageWidth; x++) {
                this.frontBuffer[x][y] = 0x000000;
            }
        }
    }

    public void scrollDown(int scrollAmount) {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            super.scrollDown(scrollAmount);
            return;
        }

        for (int i = this.imageHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = scrollAmount + i;
            if (shiftedVerticalPosition >= this.imageHeight) {
                continue;
            }
            for (int j = 0; j < this.imageWidth; j++) {
                this.frontBuffer[j][shiftedVerticalPosition] = this.frontBuffer[j][i];
            }
        }
        for (int y = 0; y < scrollAmount && y < this.imageHeight; y++) {
            for (int x = 0; x < this.imageWidth; x++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }

    }

    public void scrollRight() {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            super.scrollRight();
            return;
        }

        for (int i = this.imageWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + 4;
            if (shiftedHorizontalPosition >= this.imageWidth) {
                continue;
            }
            if (this.imageHeight >= 0) {
                System.arraycopy(this.frontBuffer[i], 0, this.frontBuffer[shiftedHorizontalPosition], 0, this.imageHeight);
            }
        }
        for (int x = 0; x < 4 && x < this.imageWidth; x++) {
            for (int y = 0; y < this.imageHeight; y++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public void scrollLeft() {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            super.scrollLeft();
            return;
        }

        for (int i = 0; i < this.imageWidth; i++) {
            int shiftedHorizontalPosition = i - 4;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            if (this.imageHeight >= 0) {
                System.arraycopy(this.frontBuffer[i], 0, this.frontBuffer[shiftedHorizontalPosition], 0, this.imageHeight);
            }
        }
        for (int x = this.imageWidth - 4; x < this.imageWidth; x++) {
            if (x < 0) {
                continue;
            }
            for (int y = 0; y < this.imageHeight; y++) {
                this.frontBuffer[x][y] = 0x00000000;
            }
        }

    }

    public void flushBackBuffer() {
        this.scrollTriggered = false;
        for (int i = 0; i < this.backBuffer.length; i++) {
            System.arraycopy(this.backBuffer[i], 0, this.frontBuffer[i], 0, this.backBuffer[i].length);
        }
    }

    public void populateRenderBuffer(int[][] renderBuffer) {
        if (this.emulator.getProcessor().isMegaModeOn()) {
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
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
                    renderBuffer[x][y] = blendAlpha(pixel, 0xFF000000, this.screenAlpha);
                }
            }
        } else {
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    renderBuffer[x][y] = 0xFF000000;
                }
            }
            int displayWidth = super.getImageWidth();
            int displayHeight = super.getImageHeight();
            int xScale = 2;
            int yScale = 2;
            int yOffset = (this.imageHeight - displayHeight * yScale) / 2;
            for (int sy = 0; sy < displayHeight; sy++) {
                int baseY = yOffset + sy * yScale;
                for (int sx = 0; sx < displayWidth; sx++) {
                    int color = super.colorPalette.getColorARGB(this.bitplaneBuffer[sx][sy] & 0xF);
                    int baseX = sx * xScale;
                    for (int dy = 0; dy < yScale; dy++) {
                        for (int dx = 0; dx < xScale; dx++) {
                            renderBuffer[baseX + dx][baseY + dy] = color;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
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
        return 0xFF000000 |
                (((((src >>> 16) & 0xFF) * alpha + ((dst >>> 16) & 0xFF) * invAlpha) / 255) << 16) |
                (((((src >>> 8) & 0xFF) * alpha + ((dst >>> 8) & 0xFF) * invAlpha) / 255) << 8) |
                (((src & 0xFF) * alpha + (dst & 0xFF) * invAlpha) / 255);
    }

    @SuppressWarnings("DuplicatedCode")
    private static int addColors(int src, int dst) {
        return 0xFF000000 |
                ((Math.min(((src >>> 16) & 0xFF) + ((dst >>> 16) & 0xFF), 255)) << 16) |
                ((Math.min(((src >>> 8) & 0xFF) + ((dst >>> 8) & 0xFF), 255)) << 8) |
                Math.min((src & 0xFF) + (dst & 0xFF), 255);
    }

    @SuppressWarnings("DuplicatedCode")
    private static int multiplyColors(int src, int dst) {
        return 0xFF000000 |
                (((((src >>> 16) & 0xFF) * ((dst >>> 16) & 0xFF)) / 255) << 16) |
                (((((src >>> 8) & 0xFF) * ((dst >>> 8) & 0xFF)) / 255) << 8) |
                (((src & 0xFF) * (dst & 0xFF)) / 255);
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
