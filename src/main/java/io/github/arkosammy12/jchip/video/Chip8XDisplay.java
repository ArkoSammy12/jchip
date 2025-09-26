package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;

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

    private int backgroundColorIndex = 0;
    private final int[][] foregroundColorIndexes = new int[64][32];

    public Chip8XDisplay(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config, keyAdapter);
    }

    public void cycleBackgroundColor() {
        this.backgroundColorIndex = (backgroundColorIndex + 1) % BACKGROUND_COLORS.length;
    }

    public void setForegroundColor(int column, int row, int colorIndex) {
        if (colorIndex >= 8) {
            int a = 1;
        }
        this.foregroundColorIndexes[column][row] = colorIndex;
    }

    @Override
    protected void populateDataBuffer(int[] buffer) {
        for (int y = 0; y < screenHeight; y++) {
            int base = y * screenWidth;
            for (int x = 0; x < screenWidth; x++) {
                buffer[base + x] = this.bitplaneBuffer[x][y] != 0 ? FOREGROUND_COLORS[this.foregroundColorIndexes[x][y]] : BACKGROUND_COLORS[this.backgroundColorIndex];
            }
        }
    }

}
