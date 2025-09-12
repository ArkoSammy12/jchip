package io.github.arkosammy12.jchip.hardware;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;
import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.charset.Charset;

public class LanternaDisplay extends AbstractDisplay {

    private final TerminalScreen terminalScreen;
    private long lastTitleUpdate = 0;

    public LanternaDisplay(String title, ConsoleVariant consoleVariant, KeyAdapter keyAdapter, ColorPalette colorPalette) throws IOException {
        super(title, consoleVariant, colorPalette);
        int fontSize = 9;
        if (consoleVariant == ConsoleVariant.CHIP_8) {
            fontSize = 16;
        }
        Font terminalFont = new Font("Monospaced", Font.PLAIN, fontSize);
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
        this.terminalScreen = new TerminalScreen(terminal);
        this.terminalScreen.setCursorPosition(null);
        for (int i = 0; i < this.screenWidth; i++) {
            for (int j = 0; j < this.screenHeight; j++) {
                this.terminalScreen.setCharacter(i, j, this.colorPalette.getTextCharacterColor(0));
            }
        }
        this.terminalScreen.doResizeIfNecessary();
        this.terminalScreen.startScreen();
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
                TextCharacter character = this.colorPalette.getTextCharacterColor(currentPixel);
                this.terminalScreen.setCharacter(i, j, character);
                this.previousFrameBuffer[i][j] = currentPixel;
            }
        }
        long now = System.currentTimeMillis();
        if (now - lastTitleUpdate >= 1000) {
            double mips = (double) (currentInstructionsPerFrame * 60) / 1_000_000;
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
