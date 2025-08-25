package io.github.arkosammy12.jchip.io;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class EmulatorScreen {

    private final Screen terminalScreen;
    private final char[][] screenBuffer = new char[64][32];
    private Clip beepClip;
    private boolean isBeeping = false;
    private static final char PIXEL_ON = '█';
    private static final char PIXEL_OFF = ' ';
    public static final int SCREEN_WIDTH = 64;
    public static final int SCREEN_HEIGHT = 32;


    public EmulatorScreen() throws IOException {
        SwingTerminalFrame terminal = new DefaultTerminalFactory(System.out, System.in, Charset.defaultCharset())
                .setInitialTerminalSize(new TerminalSize(SCREEN_WIDTH, SCREEN_HEIGHT))
                .setTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnEscape)
                .setTerminalEmulatorTitle("Chip-8")
                .createSwingTerminal();
        /*
        Component[] components = terminal.getContentPane().getComponents();
        for (Component component : components) {
            component.addKeyListener(keyAdapter);
        }

         */
        terminal.setVisible(true);
        terminal.setFocusable(true);
        terminal.setForegroundColor(TextColor.ANSI.WHITE);
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setCursorVisible(false);
        this.terminalScreen = new TerminalScreen(terminal);
        this.terminalScreen.getTerminalSize();
        this.terminalScreen.doResizeIfNecessary();
        this.terminalScreen.startScreen();
        this.clear();
    }

    public boolean togglePixelAt(int column, int row) {
        if (column >= SCREEN_WIDTH || column < 0 || row >= SCREEN_HEIGHT || row < 0) {
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
             Arrays.fill(chars, PIXEL_OFF);
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
