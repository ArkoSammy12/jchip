package io.github.arkosammy12.jchip;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
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
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Jchip {

    private MainWindow mainWindow;
    private Emulator currentEmulator;
    private final List<StateChangedListener> stateChangedEventListeners = Collections.synchronizedList(new ArrayList<>());
    private final Chip8Database database = new Chip8Database();
    private final DefaultAudioRenderer audioRenderer = new DefaultAudioRenderer();
    private final FrameLimiter pacer = new FrameLimiter(Main.FRAMES_PER_SECOND, true, true);
    private final int[] flagsStorage = new int[16];

    private final AtomicReference<State> currentState = new AtomicReference<>(State.STOPPED);
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
                FlatOneDarkIJTheme.setup();

                UIManager.put("TitlePane.useWindowDecorations", false);
                UIManager.put("Component.hideMnemonics", false);
                UIManager.put("FileChooser.readOnly", true);
                UIManager.put("Component.arc", 8);
                UIManager.put("Button.arc", 8);

                ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                toolTipManager.setLightWeightPopupEnabled(false);
                toolTipManager.setInitialDelay(700);
                toolTipManager.setReshowDelay(700);
                toolTipManager.setDismissDelay(4000);

                JFrame.setDefaultLookAndFeelDecorated(false);
                JDialog.setDefaultLookAndFeelDecorated(false);
                Toolkit.getDefaultToolkit().setDynamicLayout(true);

                this.mainWindow = new MainWindow(this);
                this.mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                this.mainWindow.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            running.set(false);
                            onShutdown();
                        } catch (Exception ex) {
                            Logger.error("Failed to release application resources: {}", ex);
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
            throw new RuntimeException("Failed to initialize Jchip", e);
        }
    }

    public void addStateChangedListener(StateChangedListener l) {
        this.stateChangedEventListeners.add(l);
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

    public State getState() {
        return this.currentState.get();
    }

    public void setFlagRegister(int index, int value) {
        this.flagsStorage[index] = value;
    }

    public int getFlagRegister(int index) {
        return this.flagsStorage[index];
    }

    public void start() throws Exception {
        while (this.running.get()) {
            try {
                if (!this.pacer.isFrameReady(true)) {
                    continue;
                }
                switch (this.currentState.get()) {
                    case STOPPED, PAUSED, PAUSED_STOPPED -> onIdle();
                    case RESETTING_AND_RUNNING -> onResetting(false);
                    case RESETTING_AND_PAUSING -> onResetting(true);
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

    public void reset(boolean startPaused) {
        this.setNewState(startPaused ? State.RESETTING_AND_PAUSING : State.RESETTING_AND_RUNNING);
    }

    public void setPaused(boolean paused) {
        if (paused) {
            this.setNewState(this.currentEmulator == null ? State.PAUSED_STOPPED : State.PAUSED);
        } else {
            this.setNewState(this.currentEmulator == null ? State.STOPPED : State.RUNNING);
        }
    }

    public void stop() {
        this.setNewState(State.STOPPING);
    }

    public void stepFrame() {
        this.setNewState(State.STEPPING_FRAME);
    }

    public void stepCycle() {
        this.setNewState(State.STEPPING_CYCLE);
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
        this.setNewState(State.STOPPED);
    }

    private void onResetting(boolean startPaused) throws Exception {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.mainWindow.setEmulatorRenderer(null);
        }
        this.audioRenderer.setPaused(true);
        this.currentEmulator = Variant.getEmulator(this);
        this.setNewState(startPaused ? State.PAUSED : State.RUNNING);
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
        this.setNewState(State.PAUSED);
    }

    private void onSteppingCycle() {
        if (this.currentEmulator == null) {
            return;
        }
        this.audioRenderer.setPaused(true);
        this.currentEmulator.executeSingleCycle();
        this.setNewState(State.PAUSED);
    }

    public void onBreakpoint() {
        this.mainWindow.onBreakpoint();
    }

    void onShutdown() throws Exception {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
            this.currentEmulator = null;
        }
        if (this.mainWindow != null) {
            this.mainWindow.setEmulatorRenderer(null);
            this.mainWindow.close();
        }
        this.audioRenderer.close();
    }

    private void setNewState(State newState) {
        State oldState = this.currentState.get();
        this.currentState.set(newState);
        this.stateChangedEventListeners.forEach(l -> l.onStateChanged(oldState, newState));
    }

    public enum State {
        RUNNING,
        STOPPING,
        RESETTING_AND_RUNNING,
        RESETTING_AND_PAUSING,
        PAUSED,
        STEPPING_FRAME,
        STEPPING_CYCLE,
        STOPPED,
        PAUSED_STOPPED;

        public boolean isStopped() {
            return this == STOPPED || this == PAUSED_STOPPED;
        }

    }

    @FunctionalInterface
    public interface StateChangedListener {

        void onStateChanged(State oldState, State newState);

    }

}
