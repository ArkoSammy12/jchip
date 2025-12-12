package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerPanel;
import io.github.arkosammy12.jchip.ui.menus.InfoPanel;
import io.github.arkosammy12.jchip.video.EmulatorRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainWindow extends JFrame implements Closeable {

    private static final String DEFAULT_TITLE = "jchip " + Main.VERSION_STRING;

    private final JSplitPane mainSplitPane;
    private final EmulatorViewport emulatorViewport;
    private final SettingsBar settingsBar;
    private final DebuggerPanel debuggerPanel;
    private final InfoPanel infoPanel;

    private final AtomicBoolean showingDebuggerPanel = new AtomicBoolean(false);

    public MainWindow(Jchip jchip) {
        super(DEFAULT_TITLE);
        this.setBackground(Color.BLACK);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().setBackground(Color.BLACK);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setPreferredSize(new Dimension(((int) (screenSize.getWidth() / 1.5)), (int) (screenSize.getHeight() / 1.5)));

        this.emulatorViewport = new EmulatorViewport();
        this.emulatorViewport.setVisible(true);

        this.settingsBar = new SettingsBar(jchip, this);

        this.infoPanel = new InfoPanel();
        this.infoPanel.setVisible(true);

        this.debuggerPanel = new DebuggerPanel(jchip);
        this.debuggerPanel.setVisible(false);

        this.mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.emulatorViewport, this.debuggerPanel);
        this.mainSplitPane.setContinuousLayout(true);
        this.mainSplitPane.setResizeWeight(0.5);
        this.mainSplitPane.setDividerSize(0);

        this.setJMenuBar(this.settingsBar);
        this.getContentPane().add(this.infoPanel, BorderLayout.SOUTH);
        this.getContentPane().add(this.mainSplitPane, BorderLayout.CENTER);

        this.setTitle(DEFAULT_TITLE);
        this.requestFocusInWindow();
        this.setResizable(true);
        this.pack();
        this.setLocationRelativeTo(null);

    }

    public SettingsBar getSettingsBar() {
        return this.settingsBar;
    }

    public void onFrame(@Nullable Emulator emulator) {
        if (emulator == null) {
            return;
        }
        if (this.showingDebuggerPanel.get()) {
            this.debuggerPanel.onFrame(emulator);
        }
        this.infoPanel.onFrame(emulator);
        emulator.getDisplay().getEmulatorRenderer().requestFrame();
    }

    public void onStopped() {
        this.infoPanel.onStopped();
        this.debuggerPanel.onStopped();
        this.settingsBar.onStopped();
    }

    public void setEmulatorRenderer(EmulatorRenderer emulatorRenderer) {
        this.emulatorViewport.setEmulatorRenderer(emulatorRenderer);
    }

    public void setDebuggerViewEnabled(boolean enabled) {
        this.showingDebuggerPanel.set(enabled);
        SwingUtilities.invokeLater(() -> {
            this.debuggerPanel.setVisible(enabled);
            this.mainSplitPane.setDividerSize(enabled ? 5 : 0);
            if (enabled) {
                SwingUtilities.invokeLater(() -> mainSplitPane.setDividerLocation(mainSplitPane.getLastDividerLocation()));
            } else {
                mainSplitPane.setLastDividerLocation(mainSplitPane.getDividerLocation());
            }
            this.revalidate();
            this.repaint();
        });
    }

    public void setInfoPanelEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            this.infoPanel.setVisible(enabled);
            this.revalidate();
            this.repaint();
        });
    }

    public void showExceptionDialog(Exception e) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
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
