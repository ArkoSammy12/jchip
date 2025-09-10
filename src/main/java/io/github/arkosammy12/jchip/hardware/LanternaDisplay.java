package io.github.arkosammy12.jchip.hardware;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.util.CharacterFont;
import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.charset.Charset;

public class LanternaDisplay implements Display {

    private final TerminalScreen terminalScreen;
    private final CharacterFont characterFont;
    private final ConsoleVariant consoleVariant;
    private final ColorPalette colorPalette;
    private final int[][] frameBuffer = new int[128][64];
    private final int[][] previousFrameBuffer = new int[128][64];

    private final int screenWidth;
    private final int screenHeight;
    private boolean extendedMode = false;
    private long lastTitleUpdate = 0;

    private final String title;

    public LanternaDisplay(String title, ConsoleVariant consoleVariant, KeyAdapter keyAdapter, ColorPalette colorPalette) throws IOException {
        this.title = title;
        int fontSize = 9;
        if (consoleVariant == ConsoleVariant.CHIP_8) {
            this.screenWidth = 64;
            this.screenHeight = 32;
            fontSize = 16;
        } else {
            this.screenWidth = 128;
            this.screenHeight = 64;
        }

        Font terminalFont = new Font("Courier New", Font.PLAIN, fontSize);
        AffineTransform horizontalStretchTransform = new AffineTransform();
        horizontalStretchTransform.scale(2, 1.0);
        Font actualFont = terminalFont.deriveFont(horizontalStretchTransform);
        SwingTerminalFrame terminal = new DefaultTerminalFactory(System.out, System.in, Charset.defaultCharset())
                .setInitialTerminalSize(new TerminalSize(this.screenWidth, this.screenHeight))
                .setTerminalEmulatorFontConfiguration(SwingTerminalFontConfiguration.newInstance(actualFont))
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

        this.colorPalette = colorPalette;
        this.terminalScreen = new TerminalScreen(terminal);
        this.consoleVariant = consoleVariant;
        this.characterFont = new CharacterFont(consoleVariant);
        this.terminalScreen.setCursorPosition(null);

        for (int i = 0; i < 4; i++) {
            this.clear(i);
        }
        for (int i = 0; i < this.screenWidth; i++) {
            for (int j = 0; j < this.screenHeight; j++) {
                this.terminalScreen.setCharacter(i, j, this.colorPalette.getPixel(0));
            }
        }
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
    public void scrollUp(int scrollAmount, int selectedBitPlanes) {
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
    public void scrollDown(int scrollAmount, int selectedBitPlanes) {
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
    public void scrollRight(int selectedBitPlanes) {
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
    public void scrollLeft(int selectedBitPlanes) {
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
    public void clear(int selectedBitPlanes) {
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

    @Override
    public void flush(int currentInstructionsPerFrame) throws IOException {
        for (int i = 0; i < this.screenWidth; i++) {
            for (int j = 0; j < this.screenHeight; j++) {
                int currentPixel = this.frameBuffer[i][j] & 0xF;
                int previousPixel = this.previousFrameBuffer[i][j] & 0xF;
                if ((currentPixel ^ previousPixel) == 0) {
                    continue;
                }
                TextCharacter character = this.colorPalette.getPixel(currentPixel);
                this.terminalScreen.setCharacter(i, j, character);
                this.previousFrameBuffer[i][j] = currentPixel;
            }
        }
        long now = System.currentTimeMillis();
        if (now - lastTitleUpdate >= 1000) {
            double mips = (double) (currentInstructionsPerFrame * Main.FRAMES_PER_SECOND) / 1_000_000;
            ((SwingTerminalFrame) this.terminalScreen.getTerminal()).setTitle(
                    String.format("%s %s| IPF: %d | Mips: %f",
                            this.consoleVariant.getDisplayName(),
                            this.title != null ? "| " + title + " " : "",
                            currentInstructionsPerFrame,
                            mips));
            lastTitleUpdate = now;
        }
        this.terminalScreen.refresh();
    }

    public void close() throws IOException {
        this.terminalScreen.close();
    }

}
