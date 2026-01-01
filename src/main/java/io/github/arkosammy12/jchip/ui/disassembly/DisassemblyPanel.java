package io.github.arkosammy12.jchip.ui.disassembly;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import net.miginfocom.layout.AlignX;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class DisassemblyPanel extends JPanel {

    private final DisassemblerTable disassemblerTable;
    private final JCheckBox followCheckbox;

    public DisassemblyPanel(Jchip jchip) {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);

        this.disassemblerTable = new DisassemblerTable();

        this.setFocusable(false);
        this.setPreferredSize(new Dimension(this.getWidth(), 100));

        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Disassembler",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.getFont().deriveFont(Font.BOLD)));

        JLabel goToAddressLabel = new JLabel("Go to address: ");
        JTextField goToAddressField = new JTextField();
        goToAddressField.addActionListener(_ -> {
            String text = goToAddressField.getText().trim();
            if (text.isEmpty()) {
                return;
            }
            int address;
            try {
                address = Integer.decode(text);
            } catch (NumberFormatException _) {
                JOptionPane.showMessageDialog(
                        jchip.getMainWindow(),
                        "The address must be valid integer!",
                        "Invalid address value",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            if (!this.disassemblerTable.isAddressVisible(address)) {
                JOptionPane.showMessageDialog(
                        jchip.getMainWindow(),
                        "No disassembly currently exists for the provided instruction address!",
                        "Invalid address value",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            this.disassemblerTable.scrollToAddress(address);
        });

        goToAddressField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(goToAddressField::selectAll);
            }

        });

        JButton clearBreakpointsButton = new JButton("Clear Breakpoints");
        clearBreakpointsButton.addActionListener(_ -> this.disassemblerTable.clearBreakpoints());

        this.followCheckbox = new JCheckBox("Follow");
        this.followCheckbox.setFocusable(false);
        this.followCheckbox.addActionListener(_ -> goToAddressField.setEnabled(!this.followCheckbox.isSelected()));

        JScrollPane disassemblerScrollPane = new JScrollPane(this.disassemblerTable);

        this.add(this.followCheckbox, new CC().growX().pushX().alignX(AlignX.CENTER));
        this.add(goToAddressLabel, new CC().split(2).alignX(AlignX.CENTER));
        this.add(goToAddressField, new CC().growX().pushX().alignX(AlignX.CENTER));
        this.add(clearBreakpointsButton, new CC().growX().pushX().alignX(AlignX.CENTER).wrap());
        this.add(disassemblerScrollPane, new CC().grow().push().spanX());

        jchip.addStateChangedListener((_, newState) -> {
            if (newState.isStopped()) {
                SwingUtilities.invokeLater(this.disassemblerTable::clear);
            }
        });

    }

    public void onFrame(Emulator emulator) {
        SwingUtilities.invokeLater(() -> {
            this.disassemblerTable.update(emulator);
            if (this.followCheckbox.isSelected()) {
                this.disassemblerTable.scrollToCurrentAddress();
            }
        });
    }

    public void setDisassemblerEnabled(boolean enabled) {
        this.disassemblerTable.setDisassemblerEnabled(enabled);
    }

}
