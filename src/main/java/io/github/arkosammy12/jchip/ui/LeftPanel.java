package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.ui.disassembly.DisassemblyPanel;
import io.github.arkosammy12.jchip.ui.util.ToggleableSplitPane;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LeftPanel extends JPanel {

    private final ToggleableSplitPane splitPane;

    public LeftPanel(Jchip jchip, MainWindow mainWindow) {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);
        EmulatorViewport emulatorViewport = new EmulatorViewport(jchip);
        DisassemblyPanel disassemblyPanel = new DisassemblyPanel(jchip, mainWindow);
        this.splitPane = new ToggleableSplitPane(JSplitPane.VERTICAL_SPLIT, emulatorViewport, disassemblyPanel, 5, 0.75);

        this.add(this.splitPane, new CC().grow().push());
    }

    public void setDisassemblerEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            this.splitPane.toggleShowSplit(enabled);
            this.revalidate();
            this.repaint();
        });
    }

}
