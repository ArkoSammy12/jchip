package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.disassembly.DisassemblyPanel;
import io.github.arkosammy12.jchip.ui.util.ToggleableSplitPane;
import io.github.arkosammy12.jchip.video.DisplayRenderer;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.tinylog.Logger;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LeftPanel extends JPanel {

    private final ToggleableSplitPane splitPane;
    private final EmulatorViewport emulatorViewport;
    private final DisassemblyPanel disassemblyPanel;

    public LeftPanel() {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);
        this.emulatorViewport = new EmulatorViewport();
        this.disassemblyPanel = new DisassemblyPanel();
        this.splitPane = new ToggleableSplitPane(JSplitPane.VERTICAL_SPLIT, this.emulatorViewport, this.disassemblyPanel, 5, 0.75);

        this.add(this.splitPane, new CC().grow().push());
    }

    public void setEmulatorRenderer(DisplayRenderer displayRenderer) {
        this.emulatorViewport.setEmulatorRenderer(displayRenderer);
    }

    public void setDisassemblyViewEnabled(boolean enabled) {
        this.disassemblyPanel.setDisassemblerEnabled(enabled);
        SwingUtilities.invokeLater(() -> {
            if (this.splitPane.isSplitVisible()) {
                this.splitPane.hideRightPanel();
            } else {
                this.splitPane.showSplit();
            }
            this.revalidate();
            this.repaint();
        });
    }

    public void onFrame(Emulator emulator) {
        this.disassemblyPanel.onFrame(emulator);
    }

    public void onStopped() {
        this.disassemblyPanel.onStopped();
    }

}
