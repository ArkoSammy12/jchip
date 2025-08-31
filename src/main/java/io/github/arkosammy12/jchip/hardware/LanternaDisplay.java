package io.github.arkosammy12.jchip.hardware;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;
import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.util.CharacterFont;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class LanternaDisplay implements Display {

    private final Screen terminalScreen;
    private final CharacterFont characterFont;
    private final ConsoleVariant consoleVariant;
    private final char[][][] bitPlanes = new char[4][128][64];
    private static final char PIXEL_ON = '█';
    private static final char PIXEL_OFF = ' ';
    private int screenWidth = 128;
    private int screenHeight = 64;
    private boolean extendedMode = false;
    private boolean modified = false;

    int[] cBitColors = {
            0x181C2000, 0xE4DCD400, 0x8C888400, 0x403C3800,
            0xD8201000, 0x40D02000, 0x1040D000, 0xE0C81800,
            0x50101000, 0x10501000, 0x50B0C000, 0xF0801000,
            0xE0609000, 0xE0F09000, 0xB050F000, 0x70402000,
    };

    int[][] rgbColors = new int[cBitColors.length][3];


    public LanternaDisplay(ConsoleVariant consoleVariant, KeyAdapter keyAdapter) throws IOException {
        int fontSize = 9;
        if (consoleVariant == ConsoleVariant.CHIP_8) {
            this.screenWidth = 64;
            this.screenHeight = 32;
            fontSize = 16;
        }
        for (int i = 0; i < cBitColors.length; i++) {
            int color = cBitColors[i];
            int r = (color >> 24) & 0xFF;
            int g = (color >> 16) & 0xFF;
            int b = (color >> 8) & 0xFF;
            rgbColors[i][0] = r;
            rgbColors[i][1] = g;
            rgbColors[i][2] = b;
        }
        SwingTerminalFrame terminal = new DefaultTerminalFactory(System.out, System.in, Charset.defaultCharset())
                .setInitialTerminalSize(new TerminalSize(this.screenWidth * 2, this.screenHeight))
                .setTerminalEmulatorFontConfiguration(SwingTerminalFontConfiguration.getDefaultOfSize(fontSize))
                .setTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnEscape)
                .setTerminalEmulatorTitle(consoleVariant.getDisplayName())
                .createSwingTerminal();
        Component[] components = terminal.getContentPane().getComponents();
        for (Component component : components) {
            component.addKeyListener(keyAdapter);
        }
        terminal.setVisible(true);
        terminal.setFocusable(true);
        terminal.setForegroundColor(TextColor.ANSI.WHITE);
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setCursorVisible(false);

        this.terminalScreen = new TerminalScreen(terminal);
        this.consoleVariant = consoleVariant;
        this.characterFont = new CharacterFont(consoleVariant);

        for (int i = 0; i < 4; i++) {
            this.clear(i);
        }

        this.terminalScreen.getTerminalSize();
        this.terminalScreen.doResizeIfNecessary();
        this.terminalScreen.startScreen();
    }

    @Override
    public int getWidth() {
        return this.screenWidth;
    }

    @Override
    public int getHeight() {
        return this.screenHeight;
    }

    public void setExtendedMode(boolean extendedMode) {
        this.extendedMode = extendedMode;
    }

    public boolean isExtendedMode() {
        return this.extendedMode;
    }

    @Override
    public CharacterFont getCharacterFont() {
        return this.characterFont;
    }

    @Override
    public boolean togglePixel(int column, int row, int bitPlane) {
        if (column >= this.screenWidth || column < 0 || row >= this.screenHeight || row < 0) {
            return false;
        }
        this.modified = true;
        char currentChar = this.bitPlanes[bitPlane][column][row];
        char newChar = PIXEL_ON;
        boolean collided = false;
        if (currentChar == PIXEL_ON) {
            newChar = PIXEL_OFF;
            collided = true;
        }
        this.bitPlanes[bitPlane][column][row] = newChar;
        return collided;
    }

    @Override
    public void scrollUp(int scrollAmount, int selectedBitPlanes) {
        this.modified = true;
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
            int bitPlaneMask = (int) Math.pow(2, bitPlane);
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < this.screenHeight; i++) {
                int shiftedVerticalPosition = i - trueScrollAmount;
                if (shiftedVerticalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.screenWidth; j++) {
                    this.bitPlanes[bitPlane][j][shiftedVerticalPosition] = this.bitPlanes[bitPlane][j][i];
                }
            }
            // Clear the bottom scrollOffset rows
            for (int y = this.screenHeight - trueScrollAmount; y < this.screenHeight; y++) {
                if (y < 0) continue;
                for (int x = 0; x < this.screenWidth; x++) {
                    this.bitPlanes[bitPlane][x][y] = PIXEL_OFF;
                }
            }
        }
    }

    @Override
    public void scrollDown(int scrollAmount, int selectedBitPlanes) {
        this.modified = true;
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
            int bitPlaneMask = (int) Math.pow(2, bitPlane);
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = this.screenHeight - 1; i >= 0; i--) {
                int shiftedVerticalPosition = trueScrollAmount + i;
                if (shiftedVerticalPosition >= this.screenHeight) {
                    continue;
                }
                for (int j = 0; j < this.screenWidth; j++) {
                    this.bitPlanes[bitPlane][j][shiftedVerticalPosition] = this.bitPlanes[bitPlane][j][i];
                }
            }
            // Clear the top scrollOffset rows
            for (int y = 0; y < trueScrollAmount && y < this.screenHeight; y++) {
                for (int x = 0; x < this.screenWidth; x++) {
                    this.bitPlanes[bitPlane][x][y] = PIXEL_OFF;
                }
            }
        }
    }

    @Override
    public void scrollRight(int selectedBitPlanes) {
        this.modified = true;
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
            int bitPlaneMask = (int) Math.pow(2, bitPlane);
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = this.screenWidth - 1; i >= 0; i--) {
                int shiftedHorizontalPosition = i + scrollAmount;
                if (shiftedHorizontalPosition >= this.screenWidth) {
                    continue;
                }
                for (int j = 0; j < this.screenHeight; j++) {
                    this.bitPlanes[bitPlane][shiftedHorizontalPosition][j] = this.bitPlanes[bitPlane][i][j];
                }
            }
            // Clear the leftmost 4 columns
            for (int x = 0; x < scrollAmount && x < this.screenWidth; x++) {
                for (int y = 0; y < this.screenHeight; y++) {
                    this.bitPlanes[bitPlane][x][y] = PIXEL_OFF;
                }
            }
        }
    }

    @Override
    public void scrollLeft(int selectedBitPlanes) {
        this.modified = true;
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
            int bitPlaneMask = (int) Math.pow(2, bitPlane);
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < this.screenWidth; i++) {
                int shiftedHorizontalPosition = i - scrollAmount;
                if (shiftedHorizontalPosition < 0) {
                    continue;
                }
                for (int j = 0; j < this.screenHeight; j++) {
                    this.bitPlanes[bitPlane][shiftedHorizontalPosition][j] = this.bitPlanes[bitPlane][i][j];
                }
            }
            for (int x = this.screenWidth - scrollAmount; x < this.screenWidth; x++) {
                if (x < 0) continue;
                for (int y = 0; y < this.screenHeight; y++) {
                    this.bitPlanes[bitPlane][x][y] = PIXEL_OFF;
                }
            }
        }
    }

    @Override
    public void clear(int selectedBitPlanes) {
        this.modified = true;
        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = (int) Math.pow(2, bitPlane);
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < this.bitPlanes[bitPlane].length; i++) {
                Arrays.fill(this.bitPlanes[bitPlane][i], PIXEL_OFF);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (!this.modified) {
            return;
        }
        this.modified = false;
        for (int i = 0; i < this.screenWidth; i++) {
            for (int j = 0; j < this.screenHeight; j++) {
                int screenNibble = 0;
                for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
                    screenNibble |= (this.bitPlanes[bitPlane][i][j] == PIXEL_ON ? 1 << bitPlane : 0);
                }
                TextCharacter character = TextCharacter.fromCharacter(PIXEL_ON)[0].withForegroundColor(new TextColor.RGB(this.rgbColors[screenNibble][0], this.rgbColors[screenNibble][1], this.rgbColors[screenNibble][2]));
                this.terminalScreen.setCharacter(i * 2, j, character);
                this.terminalScreen.setCharacter((i * 2) + 1, j, character);
            }
        }
        this.terminalScreen.refresh();
    }

    public void close() throws IOException {
        this.terminalScreen.close();
    }

}
