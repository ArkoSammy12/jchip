package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MainWindow extends JFrame implements Closeable {

    private static final String DEFAULT_TITLE = "jchip " + Main.VERSION_STRING;

    private EmulatorRenderer emulatorRenderer;
    private final SettingsMenu settingsMenu;
    private final DebuggerViewPanel debuggerViewPanel;
    private final AtomicBoolean showingDebuggerView = new AtomicBoolean(false);

    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;
    private final StringBuilder stringBuilder = new StringBuilder(128);

    public MainWindow(JChip jchip) {
        super();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBackground(Color.BLACK);
        this.getContentPane().setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIgnoreRepaint(false);
        this.pack();
        this.setSize((int) (screenSize.getWidth() / 2), (int) (screenSize.getHeight() / 2));
        this.setLocation((screenSize.width - this.getWidth()) / 2, ((screenSize.height - this.getHeight()) / 2) - 15);
        this.setMaximumSize(screenSize);

        this.getContentPane().setLayout(new BorderLayout());

        InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "resetEmulator");
        am.put("resetEmulator", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jchip.reset();
                } catch (EmulatorException cause) {
                    Logger.info("Error resetting emulator: {}", cause);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "stopEmulator");
        am.put("stopEmulator", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jchip.stop();
                } catch (EmulatorException cause) {
                    Logger.info("Error stopping emulator: {}", cause);
                }
            }
        });

        this.debuggerViewPanel = new DebuggerViewPanel(jchip);
        this.debuggerViewPanel.setPreferredSize(new Dimension(225, 0));

        this.settingsMenu = new SettingsMenu(jchip);
        this.setJMenuBar(this.settingsMenu);

        this.setTitle(DEFAULT_TITLE);
        this.requestFocusInWindow();
        this.setResizable(true);
        this.setVisible(true);
    }

    public SettingsMenu getSettingsMenu() {
        return this.settingsMenu;
    }

    public void update(Chip8Emulator<?, ?> emulator) {
        if (this.showingDebuggerView.get()) {
            this.debuggerViewPanel.update();
        }
        this.updateWindowTitle(emulator.getCurrentInstructionsPerFrame());
    }

    public void onStopped() {
        this.debuggerViewPanel.clear();
    }

    public void setEmulatorRenderer(EmulatorRenderer emulatorRenderer) {
        if (this.emulatorRenderer != null) {
            this.emulatorRenderer.close();
            this.getContentPane().remove(this.emulatorRenderer);
        }
        if (emulatorRenderer == null) {
            this.emulatorRenderer = null;
            SwingUtilities.invokeLater(() -> {
                this.getContentPane().revalidate();
                this.getContentPane().repaint();
                this.setTitle(DEFAULT_TITLE);
            });
            return;
        }
        this.emulatorRenderer = emulatorRenderer;
        SwingUtilities.invokeLater(() -> {
            int displayWidth = emulatorRenderer.getDisplayWidth();
            int displayHeight = emulatorRenderer.getDisplayHeight();
            int initialScale = emulatorRenderer.getInitialScale();
            this.getContentPane().add(emulatorRenderer);
            this.setMinimumSize(new Dimension(displayWidth * (initialScale / 2), displayHeight * (initialScale / 2)));
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
            this.emulatorRenderer.requestFocusInWindow();
        });
    }

    public void setDebuggerViewEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            this.showingDebuggerView.set(enabled);
            if (enabled) {
                this.getContentPane().add(this.debuggerViewPanel, BorderLayout.EAST);
            } else {
                this.getContentPane().remove(this.debuggerViewPanel);
            }
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
        });
    }

    private void updateWindowTitle(int currentInstructionsPerFrame) {
        this.ifEmulatorRendererSet(renderer -> {
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

            stringBuilder.append(DEFAULT_TITLE)
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

    private void ifEmulatorRendererSet(Consumer<EmulatorRenderer> consumer) {
        if (this.emulatorRenderer != null) {
            consumer.accept(this.emulatorRenderer);
        }
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(this::dispose);
    }

}
