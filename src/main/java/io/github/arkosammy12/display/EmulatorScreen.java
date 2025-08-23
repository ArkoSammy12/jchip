package io.github.arkosammy12.display;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class EmulatorScreen {

    private final Screen terminalScreen;
    private final char[][] screenBuffer = new char[64][32];

    public EmulatorScreen() throws IOException {

        Terminal terminal = new DefaultTerminalFactory(System.out, System.in, Charset.defaultCharset())
                .setInitialTerminalSize(new TerminalSize(64, 32))
                .setTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnEscape)
                .setTerminalEmulatorTitle("Chip-8")
                .setTerminalEmulatorFontConfiguration(AWTTerminalFontConfiguration.newInstance(new Font("Monospaced", Font.BOLD, 1)))
                .setPreferTerminalEmulator(true)
                .createTerminal();

        terminal.setForegroundColor(TextColor.ANSI.WHITE);
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setCursorVisible(false);
        this.terminalScreen = new TerminalScreen(terminal);
        this.terminalScreen.startScreen();
        this.clear();
    }

    public boolean togglePixelAt(int column, int row) {
        char currentChar = this.screenBuffer[column][row];
        char newChar = '█';
        boolean onToOff = false;
        if (currentChar == '█') {
            newChar = ' ';
            onToOff = true;
        }
        if (column > 64 || column < 0 || row > 32 || row < 0) {
            return false;
        }
        this.screenBuffer[column][row] = newChar;
        return onToOff;
    }

    public void setPixelAt(int column, int row) {
        this.screenBuffer[row][column] = '█';
    }

    public void resetPixelAt(int column, int row) {
        this.screenBuffer[row][column] = ' ';
    }

     public void clear() {
         for (char[] chars : screenBuffer) {
             Arrays.fill(chars, ' ');
         }
     }

    public void flush() throws IOException {
        for (int i = 0; i < this.screenBuffer.length; i++) {
            for (int j = 0; j < this.screenBuffer[i].length; j++) {
                TextCharacter character = TextCharacter.fromCharacter(screenBuffer[i][j])[0];
                this.terminalScreen.setCharacter(i, j, character);
            }
        }
        this.terminalScreen.refresh();
    }

}
