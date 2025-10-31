package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.video.Display;
import io.github.arkosammy12.jchip.video.EmulatorRenderer;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public MainWindow(JChip jchip) {
        super();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBackground(Color.BLACK);
        this.getContentPane().setBackground(Color.BLACK);
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

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "volumeDown");
        am.put("volumeDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jchip.getSoundWriter().volumeDown();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "volumeUp");
        am.put("volumeUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jchip.getSoundWriter().volumeUp();
            }
        });

        this.debuggerViewPanel = new DebuggerViewPanel(jchip);
        this.debuggerViewPanel.setPreferredSize(new Dimension(230, 0));

        this.settingsMenu = new SettingsMenu(jchip);
        this.setJMenuBar(this.settingsMenu);

        this.setTitle(DEFAULT_TITLE);
        this.requestFocusInWindow();
        this.setResizable(true);
    }

    public SettingsMenu getSettingsMenu() {
        return this.settingsMenu;
    }

    public void update(Chip8Emulator<?, ?> emulator) {
        if (this.showingDebuggerView.get()) {
            this.debuggerViewPanel.update(emulator);
        }
        this.updateWindowTitle(emulator);
    }

    public void onStopped() {
        lastWindowTitleUpdate = 0;
        lastFrameTime = System.nanoTime();
        framesSinceLastUpdate = 0;
        totalIpfSinceLastUpdate = 0;
        totalFrameTimeSinceLastUpdate = 0;
        this.debuggerViewPanel.clear();
    }

    public void setEmulatorRenderer(EmulatorRenderer emulatorRenderer) {
        SwingUtilities.invokeLater(() -> {
            if (this.emulatorRenderer != null) {
                this.emulatorRenderer.close();
                this.getContentPane().remove(this.emulatorRenderer);
            }
            if (emulatorRenderer == null) {
                this.emulatorRenderer = null;
                this.getContentPane().revalidate();
                this.getContentPane().repaint();
                this.setTitle(DEFAULT_TITLE);
                return;
            }
            this.emulatorRenderer = emulatorRenderer;
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
        this.showingDebuggerView.set(enabled);
        SwingUtilities.invokeLater(() -> {
            if (enabled) {
                this.getContentPane().add(this.debuggerViewPanel, BorderLayout.EAST);
            } else {
                this.getContentPane().remove(this.debuggerViewPanel);
            }
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
        });
    }

    public void showExceptionDialog(Exception e) {
        SwingUtilities.invokeLater(() -> {
           JOptionPane.showMessageDialog(
                   this,
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    "Emulation has stopped unexpectedly!",
                   JOptionPane.ERROR_MESSAGE
           );
        });
    }

    private void updateWindowTitle(Chip8Emulator<?, ?> emulator) {
        this.totalIpfSinceLastUpdate += emulator.getCurrentInstructionsPerFrame();
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

        Display display = emulator.getDisplay();
        EmulatorRenderer renderer = display.getEmulatorRenderer();
        String title = DEFAULT_TITLE;
        title += " | " + display.getChip8Variant().getDisplayName();
        title += renderer.getRomTitle();
        title += " | IPF: " + averageIpf;
        title += " | MIPS: " + String.format("%.2f", mips);
        title += " | Frame Time: " + String.format("%.2f ms", averageFrameTimeMs);
        title += " | FPS: " + String.format("%.2f", fps);

        String finalTitle = title;
        SwingUtilities.invokeLater(() -> this.setTitle(finalTitle));
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(this::dispose);
    }

}
