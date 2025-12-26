package io.github.arkosammy12.jchip.ui.disassembly;

import io.github.arkosammy12.jchip.emulators.Emulator;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DisassemblyPanel extends JPanel {

    private final DisassemblerTable disassemblerTable;

    public DisassemblyPanel() {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);

        this.setFocusable(false);
        this.setPreferredSize(new Dimension(this.getWidth(), 100));

        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Disassembler",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.getFont().deriveFont(Font.BOLD)));

        this.disassemblerTable = new DisassemblerTable();

        JScrollPane disassemblerScrollPane = new JScrollPane(this.disassemblerTable);
        this.add(disassemblerScrollPane, new CC().grow().push());
    }

    public void onFrame(Emulator emulator) {
        SwingUtilities.invokeLater(() -> this.disassemblerTable.update(emulator));
    }

    public void onStopped() {
        SwingUtilities.invokeLater(this.disassemblerTable::clear);
    }

}
