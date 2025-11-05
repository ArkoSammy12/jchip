package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerViewPanel;
import io.github.arkosammy12.jchip.video.EmulatorRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainWindow extends JFrame implements Closeable {

    private static final String DEFAULT_TITLE = "jchip " + Main.VERSION_STRING;

    private EmulatorRenderer emulatorRenderer;
    private final SettingsBar settingsBar;
    private final DebuggerViewPanel debuggerViewPanel;
    private final InfoPanel infoPanel;

    private final AtomicBoolean showingDebuggerPanel = new AtomicBoolean(false);
    private final AtomicBoolean showingInfoPanel = new AtomicBoolean(true);

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

        this.settingsBar = new SettingsBar(jchip);
        this.setJMenuBar(this.settingsBar);

        this.infoPanel = new InfoPanel();
        this.infoPanel.setVisible(true);
        this.getContentPane().add(this.infoPanel, BorderLayout.SOUTH);

        this.debuggerViewPanel = new DebuggerViewPanel(jchip);
        this.debuggerViewPanel.setVisible(false);
        this.getContentPane().add(this.debuggerViewPanel, BorderLayout.EAST);

        this.setTitle(DEFAULT_TITLE);
        this.requestFocusInWindow();
        this.setResizable(true);
    }

    public SettingsBar getSettingsBar() {
        return this.settingsBar;
    }

    public void onFrame(Chip8Emulator<?, ?> emulator) {
        if (this.showingDebuggerPanel.get()) {
            this.debuggerViewPanel.update(emulator);
        }
        if (this.showingInfoPanel.get()) {
            this.infoPanel.update(emulator);
        }
        emulator.getDisplay().getEmulatorRenderer().requestFrame();
    }

    public void onStopped() {
        this.infoPanel.clear();
        this.debuggerViewPanel.clear();
        this.settingsBar.onStopped();
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
        this.showingDebuggerPanel.set(enabled);
        SwingUtilities.invokeLater(() -> {
            this.debuggerViewPanel.setVisible(enabled);
        });
    }

    public void setInfoPanelEnabled(boolean enabled) {
        this.showingInfoPanel.set(enabled);
        SwingUtilities.invokeLater(() -> {
            if (!enabled) {
                this.infoPanel.clear();
            }
            this.infoPanel.setVisible(enabled);
        });
    }

    public void showExceptionDialog(Exception e) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this,
                 e.getClass().getSimpleName() + ": " + e.getMessage(),
                 "Emulation has stopped unexpectedly!",
                JOptionPane.ERROR_MESSAGE
        ));
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(this::dispose);
    }

}
