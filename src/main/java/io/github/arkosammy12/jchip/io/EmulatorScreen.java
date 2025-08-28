package io.github.arkosammy12.jchip.io;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;
import io.github.arkosammy12.jchip.Emulator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class EmulatorScreen {

    private final Screen terminalScreen;
    private final char[][] screenBuffer = new char[128][64];
    private Clip beepClip;
    private boolean isBeeping = false;
    private static final char PIXEL_ON = '█';
    private static final char PIXEL_OFF = ' ';
    private int screenWidth = 128;
    private int screenHeight = 64;
    private boolean extendedMode = false;

    public EmulatorScreen(Emulator emulator, KeyAdapter keyAdapter) throws IOException {
        ConsoleVariant consoleVariant = emulator.getConsoleVariant();
        if (consoleVariant == ConsoleVariant.CHIP_8) {
            this.screenWidth = 64;
            this.screenHeight = 32;
        }
        this.clear();
        int fontSize;
        if (consoleVariant == ConsoleVariant.CHIP_8) {
            fontSize = 16;
        } else {
            fontSize = 9;
        }
        SwingTerminalFrame terminal = new DefaultTerminalFactory(System.out, System.in, Charset.defaultCharset())
                .setInitialTerminalSize(new TerminalSize(this.screenWidth * 2, this.screenHeight))
                .setTerminalEmulatorFontConfiguration(SwingTerminalFontConfiguration.getDefaultOfSize(fontSize))
                .setTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnEscape)
                .setTerminalEmulatorTitle(emulator.getProgramArgs().getConsoleVariant().getDisplayName())
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
        this.terminalScreen.getTerminalSize();
        this.terminalScreen.doResizeIfNecessary();
        this.terminalScreen.startScreen();
    }

    public int getScreenWidth() {
        return this.screenWidth;
    }

    public int getScreenHeight() {
        return this.screenHeight;
    }

    public void setExtendedMode(boolean extendedMode) {
        this.extendedMode = extendedMode;
    }

    public boolean isExtendedMode() {
        return this.extendedMode;
    }

    public boolean togglePixelAt(int column, int row) {
        if (column >= this.screenWidth || column < 0 || row >= this.screenHeight || row < 0) {
            return false;
        }
        char currentChar = this.screenBuffer[column][row];
        char newChar = PIXEL_ON;
        boolean toggledOff = false;
        if (currentChar == PIXEL_ON) {
            newChar = PIXEL_OFF;
            toggledOff = true;
        }
        this.screenBuffer[column][row] = newChar;
        return toggledOff;
    }


    public void buzz() {
        if (!isBeeping) {
            try {
                AudioFormat af = new AudioFormat(44100f, 8, 1, true, false );
                beepClip = AudioSystem.getClip();
                byte[] data = new byte[44100];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte)(Math.sin(2 * Math.PI * i / ((double) 44100 / 440)) * 127);
                }
                beepClip.open(af, data, 0, data.length);
                beepClip.loop(Clip.LOOP_CONTINUOUSLY);
                isBeeping = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopBuzz() {
        if (isBeeping && beepClip != null) {
            beepClip.stop();
            beepClip.close();
            isBeeping = false;
        }
    }

     public void clear() {
         for (char[] chars : screenBuffer) {
             Arrays.fill(chars, PIXEL_OFF);
         }
     }

    public void flush() throws IOException {
        for (int i = 0; i < this.screenWidth; i++) {
            for (int j = 0; j < this.screenHeight; j++) {
                TextCharacter character = TextCharacter.fromCharacter(screenBuffer[i][j])[0];
                this.terminalScreen.setCharacter(i * 2, j, character);
                this.terminalScreen.setCharacter((i * 2) + 1, j, character);
            }
        }
        this.terminalScreen.refresh();
    }

    public void scrollDown(int scrollOffset) {
        for (int i = this.screenHeight - 1; i >= 0; i--) {
            int shiftedVerticalPosition = scrollOffset + i;
            if (shiftedVerticalPosition >= this.screenHeight) {
                continue;
            }
            for (int j = 0; j < this.screenWidth; j++) {
                this.screenBuffer[j][shiftedVerticalPosition] = this.screenBuffer[j][i];
            }
        }
        // Clear the top scrollOffset rows
        for (int y = 0; y < scrollOffset && y < this.screenHeight; y++) {
            for (int x = 0; x < this.screenWidth; x++) {
                this.screenBuffer[x][y] = PIXEL_OFF;
            }
        }
    }

    public void scrollRight() {
        for (int i = this.screenWidth - 1; i >= 0; i--) {
            int shiftedHorizontalPosition = i + 4;
            if (shiftedHorizontalPosition >= this.screenWidth) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.screenBuffer[shiftedHorizontalPosition][j] = this.screenBuffer[i][j];
            }
        }
        // Clear the leftmost 4 columns
        for (int x = 0; x < 4 && x < this.screenWidth; x++) {
            for (int y = 0; y < this.screenHeight; y++) {
                this.screenBuffer[x][y] = PIXEL_OFF;
            }
        }
    }

    public void scrollLeft() {
        for (int i = 0; i < this.screenWidth; i++) {
            int shiftedHorizontalPosition = i - 4;
            if (shiftedHorizontalPosition < 0) {
                continue;
            }
            for (int j = 0; j < this.screenHeight; j++) {
                this.screenBuffer[shiftedHorizontalPosition][j] = this.screenBuffer[i][j];
            }
        }
        // Clear the rightmost 4 columns
        for (int x = this.screenWidth - 4; x < this.screenWidth; x++) {
            if (x < 0) continue;
            for (int y = 0; y < this.screenHeight; y++) {
                this.screenBuffer[x][y] = PIXEL_OFF;
            }
        }
    }

    public void close() throws IOException {
        this.terminalScreen.close();
    }

}
