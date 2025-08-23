package io.github.arkosammy12.jchip.display;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class EmulatorScreen {

    private final Screen terminalScreen;
    private final char[][] screenBuffer = new char[64][32];
    private Clip beepClip;
    private boolean isBeeping = false;

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
        if (column >= 64 || column < 0 || row >= 32 || row < 0) {
            return false;
        }
        char currentChar = this.screenBuffer[column][row];
        char newChar = '█';
        boolean toggledOff = false;
        if (currentChar == '█') {
            newChar = ' ';
            toggledOff = true;
        }
        this.screenBuffer[column][row] = newChar;
        return toggledOff;
    }

    public KeyStroke pollInput() throws IOException {
        return this.terminalScreen.pollInput();
    }

    public void beep() {
        if (!isBeeping) {
            try {
                AudioFormat af = new AudioFormat( (float )44100, 8, 1, true, false );
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

    public void stopBeep() {
        if (isBeeping && beepClip != null) {
            beepClip.stop();
            beepClip.close();
            isBeeping = false;
        }
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
