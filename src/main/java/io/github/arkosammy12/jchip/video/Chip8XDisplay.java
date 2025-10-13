package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.config.EmulatorConfig;

import java.awt.event.KeyAdapter;
import java.util.List;

public class Chip8XDisplay extends Chip8Display {

    private static final int[] BACKGROUND_COLORS = {
            0xFF000080,
            0xFF000000,
            0xFF008000,
            0XFF800000
    };

    private static final int[] FOREGROUND_COLORS = {
            0xFF181818,
            0xFFFF0000,
            0xFF0000FF,
            0xFFFF00FF,
            0xFF00FF00,
            0xFFFFFF00,
            0xFF00FFFF,
            0xFFFFFFFF
    };

    private final int[][] foregroundColorIndexes = new int[64][32];
    private int backgroundColorIndex = 0;
    private boolean extendedColorDraw = false;

    public Chip8XDisplay(EmulatorConfig config, List<KeyAdapter> keyAdapters) {
        super(config, keyAdapters);

        // CHIP-8X self color test on startup
        for (int i = 0; i < 8; i++) {
            this.foregroundColorIndexes[i][0] = 2;
        }
    }

    @Override
    public synchronized void reset() {
        super.reset();
        this.backgroundColorIndex = 0;
        this.extendedColorDraw = false;
        for (int i = 0; i < bitplaneBuffer.length; i++) {
            for (int j = 0; j < bitplaneBuffer[i].length; j++) {
                this.foregroundColorIndexes[i][j] = 0;
            }
        }
        for (int i = 0; i < 8; i++) {
            this.foregroundColorIndexes[i][0] = 2;
        }
    }

    public synchronized void cycleBackgroundColor() {
        this.backgroundColorIndex = (backgroundColorIndex + 1) % BACKGROUND_COLORS.length;
    }

    public synchronized void setForegroundColor(int column, int row, int colorIndex) {
        this.foregroundColorIndexes[column][row] = colorIndex;
    }

    public synchronized void setExtendedColorDraw(boolean extendedColorDraw) {
        this.extendedColorDraw = extendedColorDraw;
    }

    @Override
    protected synchronized void fillImageBuffer(int[] buffer) {
        if (this.extendedColorDraw) {
            for (int y = 0; y < displayHeight; y++) {
                int base = y * displayWidth;
                for (int x = 0; x < displayWidth; x++) {
                    buffer[base + x] = this.bitplaneBuffer[x][y] != 0 ? FOREGROUND_COLORS[this.foregroundColorIndexes[x][y]] : BACKGROUND_COLORS[this.backgroundColorIndex];
                }
            }
        } else {
            for (int i = 0; i < 8; i++) {
                int zoneY = i * 4;
                for (int j = 0; j < 8; j++) {
                    int zoneX = j * 8;
                    int zoneColorIndex = this.foregroundColorIndexes[zoneX][zoneY];
                    for (int dy = 0; dy < 4; dy++) {
                        int y = zoneY + dy;
                        int base = y * displayWidth;
                        for (int dx = 0; dx < 8; dx++) {
                            int x = zoneX + dx;
                            buffer[base + x] = this.bitplaneBuffer[x][y] != 0 ? FOREGROUND_COLORS[zoneColorIndex] : BACKGROUND_COLORS[this.backgroundColorIndex];
                        }
                    }
                }
            }
        }
    }

}
