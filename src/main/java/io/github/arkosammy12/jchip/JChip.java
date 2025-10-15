package io.github.arkosammy12.jchip;

import io.github.arkosammy12.jchip.config.CommandLineArgs;
import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.config.database.Chip8Database;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.ui.MainWindow;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class JChip {

    private final MainWindow mainWindow;
    private Chip8Emulator<?, ?> currentEmulator;
    private final Chip8Database database = new Chip8Database();

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean reset = new AtomicBoolean(false);
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public JChip(String[] args) throws IOException {
        this.mainWindow = new MainWindow(this);
        if (args.length > 0) {
            CommandLineArgs cliArgs = new CommandLineArgs();
            CommandLine cli = new CommandLine(cliArgs);
            CommandLine.ParseResult parseResult = cli.parseArgs(args);
            Integer executeHelpResult = CommandLine.executeHelpRequest(parseResult);
            int exitCodeOnUsageHelp = cli.getCommandSpec().exitCodeOnUsageHelp();
            int exitCodeOnVersionHelp = cli.getCommandSpec().exitCodeOnVersionHelp();
            if (executeHelpResult != null) {
                if (executeHelpResult == exitCodeOnUsageHelp) {
                    System.exit(exitCodeOnUsageHelp);
                } else if (executeHelpResult == exitCodeOnVersionHelp) {
                    System.exit(exitCodeOnVersionHelp);
                }
            }
            this.mainWindow.getSettingsBar().initializeSettings(cliArgs);
            this.currentEmulator = Chip8Variant.getEmulator(new EmulatorConfig(this));
        }
    }

    public MainWindow getMainWindow() {
        return this.mainWindow;
    }

    public Chip8Database getDatabase() {
        return this.database;
    }

    public void start() {
        try {
            long lastFrame = System.nanoTime();
            while (this.running.get()) {
                try {
                    if (this.stop.get()) {
                        this.handleStop();
                        this.stop.set(false);
                        this.reset.set(false);
                    }
                    if (this.reset.get()) {
                        this.handleReset();
                        this.reset.set(false);
                    }
                    if (this.currentEmulator == null) {
                        continue;
                    }
                    if (this.currentEmulator.isTerminated()) {
                        this.handleStop();
                        continue;
                    }
                    long now = System.nanoTime();
                    long elapsed = now - lastFrame;
                    if (elapsed > 1_000_000_000L) {
                        lastFrame = now;
                        continue;
                    }
                    while (elapsed >= Main.FRAME_INTERVAL) {
                        this.currentEmulator.tick();
                        lastFrame += Main.FRAME_INTERVAL;
                        elapsed -= Main.FRAME_INTERVAL;
                    }
                } catch (EmulatorException emulatorException) {
                    Logger.info("The current CHIP-8 emulator has crashed due to: {}", emulatorException);
                    Logger.info("Stopping...");
                    this.stop();
                }
            }
        } catch (Exception e) {
            Logger.error("jchip has crashed!");
            throw new RuntimeException(e);
        } finally {
            if (this.currentEmulator != null) {
                this.currentEmulator.close();
            }
        }
        this.mainWindow.close();
    }

    public void reset() {
        this.reset.set(true);
    }

    public void stop() {
        this.stop.set(true);
    }

    private void handleReset() throws IOException {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.mainWindow.setGameRenderer(null);
        }
        this.currentEmulator = Chip8Variant.getEmulator(new EmulatorConfig(this));
    }

    private void handleStop() {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
        }
        this.currentEmulator = null;
        this.mainWindow.setGameRenderer(null);
    }

}
