package io.github.arkosammy12.jchip;

import io.github.arkosammy12.jchip.config.CLIArgs;
import io.github.arkosammy12.jchip.config.database.Chip8Database;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.sound.DefaultAudioRenderer;
import io.github.arkosammy12.jchip.sound.AudioRenderer;
import io.github.arkosammy12.jchip.ui.MainWindow;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.util.FrameLimiter;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Jchip {

    private MainWindow mainWindow;
    private Emulator currentEmulator;
    private final Chip8Database database = new Chip8Database();
    private final DefaultAudioRenderer audioRenderer = new DefaultAudioRenderer();
    private final FrameLimiter pacer = new FrameLimiter(Main.FRAMES_PER_SECOND, true, true);
    private final int[] flagsStorage = new int[16];

    private final AtomicReference<State> currentState = new AtomicReference<>(State.IDLE);
    private final AtomicBoolean running = new AtomicBoolean(true);

    Jchip(String[] args) {
        try {
            CLIArgs cliArgs = null;
            if (args.length > 0) {
                cliArgs = new CLIArgs();
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
                this.mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                this.mainWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            running.set(false);
                            onShutdown();
                        } catch (Exception ex) {
                            Logger.error("Error releasing application resources: {}", ex);
                        }
                    }
                });
            });
            if (cliArgs != null) {
                this.mainWindow.getSettingsBar().initializeSettings(cliArgs);
                this.currentEmulator = Variant.getEmulator(this);
            }
            SwingUtilities.invokeLater(() -> this.mainWindow.setVisible(true));
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Jchip", e);
        }
    }

    public MainWindow getMainWindow() {
        return this.mainWindow;
    }

    public Chip8Database getDatabase() {
        return this.database;
    }

    public AudioRenderer getAudioRenderer() {
        return this.audioRenderer;
    }

    public void setFlagRegister(int index, int value) {
        this.flagsStorage[index] = value;
    }

    public int getFlagRegister(int index) {
        return this.flagsStorage[index];
    }

    public void setPaused(boolean paused) {
        if (paused) {
            this.currentState.set(State.PAUSED);
        } else {
            this.currentState.set(State.RUNNING);
        }
    }

    public void start() throws Exception {
        while (this.running.get()) {
            try {
                if (!this.pacer.isFrameReady(true)) {
                    continue;
                }
                switch (this.currentState.get()) {
                    case IDLE, PAUSED -> onIdle();
                    case RESETTING -> onResetting();
                    case STOPPING -> onStopping();
                    case RUNNING -> onRunning();
                    case STEPPING_FRAME -> onSteppingFrame();
                    case STEPPING_CYCLE -> onSteppingCycle();
                }
                this.mainWindow.onFrame(this.currentEmulator);
                this.audioRenderer.onFrame();
            } catch (EmulatorException e) {
                Logger.error("Error while running emulator: {}", e);
                this.mainWindow.showExceptionDialog(e);
                this.stop();
            }
        }
    }

    public void reset() {
        this.currentState.set(State.RESETTING);
    }

    public void stop() {
        this.currentState.set(State.STOPPING);
    }

    public void stepFrame() {
        this.currentState.set(State.STEPPING_FRAME);
    }

    public void stepCycle() {
        this.currentState.set(State.STEPPING_CYCLE);
    }

    private void onIdle() {
        this.audioRenderer.setPaused(true);
    }

    private void onStopping() throws Exception {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.currentEmulator = null;
            this.mainWindow.setEmulatorRenderer(null);
        }
        this.audioRenderer.setPaused(true);
        this.mainWindow.onStopped();
        this.currentState.set(State.IDLE);
    }

    private void onResetting() throws Exception {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.mainWindow.setEmulatorRenderer(null);
        }
        this.audioRenderer.setPaused(true);
        this.currentEmulator = Variant.getEmulator(this);
        this.currentState.set(State.RUNNING);
    }

    private void onRunning() {
        if (currentEmulator == null) {
            return;
        }
        this.audioRenderer.setPaused(false);
        this.currentEmulator.executeFrame();
    }

    private void onSteppingFrame() {
        if (currentEmulator == null) {
            return;
        }
        this.audioRenderer.setPaused(true);
        this.currentEmulator.executeFrame();
        this.currentState.set(State.PAUSED);
    }

    private void onSteppingCycle() {
        if (this.currentEmulator == null) {
            return;
        }
        this.audioRenderer.setPaused(true);
        this.currentEmulator.executeSingleCycle();
        this.currentState.set(State.PAUSED);
    }

    void onShutdown() throws Exception {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.currentEmulator = null;
        }
        if (this.mainWindow != null) {
            this.mainWindow.setEmulatorRenderer(null);
            this.mainWindow.onStopped();
            this.mainWindow.close();
        }
        this.audioRenderer.close();
    }

    private enum State {
        RUNNING,
        STOPPING,
        RESETTING,
        PAUSED,
        STEPPING_FRAME,
        STEPPING_CYCLE,
        IDLE
    }

}
