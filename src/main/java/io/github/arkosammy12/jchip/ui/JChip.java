package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.config.CommandLineArgs;
import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.function.Consumer;

public class JChip extends JFrame {

    private GameRenderer gameRenderer;
    private final SettingsMenu settingsBar;
    private Chip8Emulator<?, ?> currentEmulator;

    private volatile boolean shouldTerminate = false;
    private volatile boolean shouldReset = false;

    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;
    private final StringBuilder stringBuilder = new StringBuilder(128);

    public JChip(String[] args) throws IOException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBackground(Color.BLACK);
        this.getContentPane().setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIgnoreRepaint(false);
        this.pack();
        this.setSize((int) (screenSize.getWidth() / 2), (int) (screenSize.getHeight() / 2));
        this.setLocation((screenSize.width - this.getWidth()) / 2, ((screenSize.height - this.getHeight()) / 2) - 15);
        this.setMaximumSize(screenSize);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                ifGameRendererSet(GameRenderer::requestFocusInWindow);
            }
        });
        this.settingsBar = new SettingsMenu(this);

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
            this.settingsBar.initializeSettings(cliArgs);
            this.currentEmulator = Chip8Variant.getEmulator(new EmulatorConfig(this));
        }

        this.setJMenuBar(this.settingsBar);
        this.setVisible(true);
        this.setResizable(true);
    }

    public SettingsMenu getSettingsBar() {
        return this.settingsBar;
    }

    public void reset() {
        this.shouldReset = true;
    }

    public void start() {
        try {
            long lastFrame = System.nanoTime();
            while (!this.shouldTerminate) {
                if (this.shouldReset) {
                    this.handleReset();
                    this.shouldReset = false;
                }
                if (this.currentEmulator == null) {
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
            }
        } catch (Exception e) {
            Logger.error("jchip has crashed!");
            throw new RuntimeException(e);
        } finally {
            if (this.currentEmulator != null) {
                this.currentEmulator.close();
            }
        }
        SwingUtilities.invokeLater(this::dispose);
    }

    public void setGameRenderer(GameRenderer gameRenderer) {
        if (this.gameRenderer != null) {
            this.getContentPane().remove(this.gameRenderer);
        }
        if (gameRenderer == null) {
            return;
        }
        this.gameRenderer = gameRenderer;
        this.getContentPane().add(gameRenderer);
        int displayWidth = gameRenderer.getDisplayWidth();
        int displayHeight = gameRenderer.getDisplayHeight();
        int initialScale = gameRenderer.getInitialScale();
        this.setMinimumSize(new Dimension(displayWidth * (initialScale / 2), displayHeight * (initialScale / 2)));
        this.repaint();
        this.revalidate();
        SwingUtilities.invokeLater(gameRenderer::requestFocusInWindow);
    }

    public void updateWindowTitle(int currentInstructionsPerFrame) {
        ifGameRendererSet(renderer -> {
            this.totalIpfSinceLastUpdate += currentInstructionsPerFrame;
            long now = System.nanoTime();
            double lastFrameDuration = now - lastFrameTime;
            lastFrameTime = now;
            totalFrameTimeSinceLastUpdate += lastFrameDuration;
            framesSinceLastUpdate++;

            long deltaTime = now - lastWindowTitleUpdate;
            if (deltaTime < 1_000_000_000L) {
                return;
            }

            double fps = framesSinceLastUpdate / (deltaTime / 1_000_000_000.0);
            long averageIpf = totalIpfSinceLastUpdate / framesSinceLastUpdate;
            double averageFrameTimeMs = (totalFrameTimeSinceLastUpdate / framesSinceLastUpdate) / 1_000_000.0;
            double mips = (averageIpf * fps) / 1_000_000.0;

            framesSinceLastUpdate = 0;
            totalIpfSinceLastUpdate = 0;
            totalFrameTimeSinceLastUpdate = 0;
            lastWindowTitleUpdate = now;

            stringBuilder.append("jchip ").append(Main.VERSION_STRING)
                    .append(" | ").append(renderer.getChip8Variant().getDisplayName())
                    .append(renderer.getRomTitle())
                    .append(" | IPF: ").append(averageIpf)
                    .append(" | MIPS: ").append(String.format("%.2f", mips))
                    .append(" | Frame Time: ").append(String.format("%.2f ms", averageFrameTimeMs))
                    .append(" | FPS: ").append(String.format("%.2f", fps));

            String title = stringBuilder.toString();
            stringBuilder.setLength(0);

            SwingUtilities.invokeLater(() -> this.setTitle(title));
        });

    }

    private void handleReset() throws IOException {
        if (this.currentEmulator != null) {
            this.currentEmulator.close();
        }
        this.currentEmulator = Chip8Variant.getEmulator(new EmulatorConfig(this));
    }

    private void ifGameRendererSet(Consumer<GameRenderer> consumer) {
        if (this.gameRenderer != null) {
            consumer.accept(this.gameRenderer);
        }
    }

}
