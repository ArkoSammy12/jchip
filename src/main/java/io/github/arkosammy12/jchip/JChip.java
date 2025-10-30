package io.github.arkosammy12.jchip;

import io.github.arkosammy12.jchip.config.CommandLineArgs;
import io.github.arkosammy12.jchip.config.EmulatorInitializer;
import io.github.arkosammy12.jchip.config.database.Chip8Database;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.sound.SoundWriter;
import io.github.arkosammy12.jchip.ui.MainWindow;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class JChip {

    private MainWindow mainWindow;
    private Chip8Emulator<?, ?> currentEmulator;
    private final Chip8Database database = new Chip8Database();
    private final SoundWriter soundWriter = new SoundWriter();
    private final int[] flagsStorage = new int[16];

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean reset = new AtomicBoolean(false);
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public JChip(String[] args) throws IOException, InterruptedException, InvocationTargetException {
        try {
            CommandLineArgs cliArgs = null;
            if (args.length > 0) {
                cliArgs = new CommandLineArgs();
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
            }
            SwingUtilities.invokeAndWait(() -> {
                this.mainWindow = new MainWindow(this);
                this.mainWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        shutdown();
                    }
                });
                this.mainWindow.setVisible(true);
            });
            if (cliArgs != null) {
                this.mainWindow.getSettingsMenu().initializeSettings(cliArgs);
                this.currentEmulator = Chip8Variant.getEmulator(new EmulatorInitializer(this));
            }
        } catch (Exception e) {
            if (this.mainWindow != null) {
                this.mainWindow.showExceptionDialog(e);
            }
            this.onShutdown();
            throw new RuntimeException(e);
        }
    }

    public Optional<Chip8Emulator<?, ?>> getCurrentEmulator() {
        return Optional.ofNullable(this.currentEmulator);
    }

    public MainWindow getMainWindow() {
        return this.mainWindow;
    }

    public Chip8Database getDatabase() {
        return this.database;
    }

    public SoundWriter getSoundWriter() {
        return this.soundWriter;
    }

    public void setFlagRegister(int index, int value) {
        this.flagsStorage[index] = value;
    }

    public int getFlagRegister(int index) {
        return this.flagsStorage[index];
    }

    public void start() throws IOException {
        long lastFrameTime = System.nanoTime();
        while (this.running.get()) {
            try {
                if (this.stop.get()) {
                    this.handleStop();
                    lastFrameTime = System.nanoTime();
                    continue;
                }
                if (this.reset.get()) {
                    this.handleReset();
                    lastFrameTime = System.nanoTime();
                    continue;
                }
                if (this.currentEmulator == null) {
                    continue;
                }
                if (this.currentEmulator.isTerminated()) {
                    this.stop();
                    continue;
                }
                long now = System.nanoTime();
                long elapsed = now - lastFrameTime;
                if (elapsed > 1_000_000_000L) {
                    lastFrameTime = now;
                    continue;
                }
                while (elapsed >= Main.FRAME_INTERVAL) {
                    this.currentEmulator.tick();
                    this.mainWindow.update(currentEmulator);
                    lastFrameTime += Main.FRAME_INTERVAL;
                    elapsed -= Main.FRAME_INTERVAL;
                }
            } catch (EmulatorException emulatorException) {
                Logger.info("Error while running emulator: {}", emulatorException);
                this.mainWindow.showExceptionDialog(emulatorException);
                this.stop();
            }
        }
    }

    public void reset() {
        this.reset.set(true);
    }

    public void stop() {
        this.stop.set(true);
    }

    private void shutdown() {
        this.running.set(false);
    }

    private void handleReset() throws IOException {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.mainWindow.setEmulatorRenderer(null);
        }
        this.currentEmulator = Chip8Variant.getEmulator(new EmulatorInitializer(this));
        this.reset.set(false);
    }

    private void handleStop() {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.currentEmulator = null;
            this.mainWindow.setEmulatorRenderer(null);
        }
        this.mainWindow.onStopped();
        this.stop.set(false);
    }

    public void onShutdown() {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.currentEmulator = null;
        }
        if (this.mainWindow != null) {
            this.mainWindow.setEmulatorRenderer(null);
            this.mainWindow.onStopped();
            this.mainWindow.close();
        }
        this.soundWriter.close();
    }

}
