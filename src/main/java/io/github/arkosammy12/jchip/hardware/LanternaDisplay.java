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
    private final char[][] screenBuffer = new char[128][64];
    private static final char PIXEL_ON = '█';
    private static final char PIXEL_OFF = ' ';
    private int screenWidth = 128;
    private int screenHeight = 64;
    private boolean extendedMode = false;
    private boolean modified = false;

    public LanternaDisplay(ConsoleVariant consoleVariant, KeyAdapter keyAdapter) throws IOException {
        int fontSize = 9;
        if (consoleVariant == ConsoleVariant.CHIP_8) {
            this.screenWidth = 64;
            this.screenHeight = 32;
            fontSize = 16;
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

        this.terminalScreen.getTerminalSize();
        this.terminalScreen.doResizeIfNecessary();
        this.clear();
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
    public boolean togglePixel(int column, int row) {
        if (column >= this.screenWidth || column < 0 || row >= this.screenHeight || row < 0) {
            return false;
        }
        this.modified = true;
        char currentChar = this.screenBuffer[column][row];
        char newChar = PIXEL_ON;
        boolean collided = false;
        if (currentChar == PIXEL_ON) {
            newChar = PIXEL_OFF;
            collided = true;
        }
        this.screenBuffer[column][row] = newChar;
        return collided;
    }

    @Override
    public void scrollDown(int scrollAmount) {
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
        for (int i = this.screenHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = trueScrollAmount + i;
            if (shiftedVerticalPosition >= this.screenHeight) {
                continue;
            }
            for (int j = 0; j < this.screenWidth; j++) {
                this.screenBuffer[j][shiftedVerticalPosition] = this.screenBuffer[j][i];
            }
        }
        // Clear the top scrollOffset rows
        for (int y = 0; y < trueScrollAmount && y < this.screenHeight; y++) {
            for (int x = 0; x < this.screenWidth; x++) {
                this.screenBuffer[x][y] = PIXEL_OFF;
            }
        }
    }

    @Override
    public void scrollRight() {
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
        for (int i = this.screenWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + scrollAmount;
            if (shiftedHorizontalPosition >= this.screenWidth) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.screenBuffer[shiftedHorizontalPosition][j] = this.screenBuffer[i][j];
            }
        }
        // Clear the leftmost 4 columns
        for (int x = 0; x < scrollAmount && x < this.screenWidth; x++) {
            for (int y = 0; y < this.screenHeight; y++) {
                this.screenBuffer[x][y] = PIXEL_OFF;
            }
        }
    }

    @Override
    public void scrollLeft() {
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
        for (int i = 0; i < this.screenWidth; i++) {
            int shiftedHorizontalPosition = i - scrollAmount;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.screenBuffer[shiftedHorizontalPosition][j] = this.screenBuffer[i][j];
            }
        }

        for (int x = this.screenWidth - scrollAmount; x < this.screenWidth; x++) {
            if (x < 0) continue;
            for (int y = 0; y < this.screenHeight; y++) {
                this.screenBuffer[x][y] = PIXEL_OFF;
            }
        }
    }

    @Override
    public void clear() {
        this.modified = false;
        for (char[] chars : screenBuffer) {
            Arrays.fill(chars, PIXEL_OFF);
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
                TextCharacter character = TextCharacter.fromCharacter(screenBuffer[i][j])[0];
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
