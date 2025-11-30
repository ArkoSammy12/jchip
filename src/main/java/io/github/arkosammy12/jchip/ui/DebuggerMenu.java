package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DebuggerMenu extends JMenu {

    private final JRadioButtonMenuItem showDebuggerButton;
    private final JCheckBoxMenuItem memoryFollowButton;
    private boolean memoryFollowEnabled = true;

    public DebuggerMenu(Jchip jchip) {
        super("Debugger");

        this.setMnemonic(KeyEvent.VK_D);

        this.showDebuggerButton = new JRadioButtonMenuItem("Show");
        this.showDebuggerButton.addActionListener(_ ->
                jchip.getMainWindow().setDebuggerViewEnabled(showDebuggerButton.isSelected())
        );
        this.showDebuggerButton.setToolTipText("Toggle the debugger view panel.");

        this.memoryFollowButton = new JCheckBoxMenuItem("Bus Follow");
        this.memoryFollowButton.setSelected(memoryFollowEnabled);
        this.memoryFollowButton.setToolTipText("Automatically scroll memory viewer.");
        this.memoryFollowButton.addActionListener(_ ->
                memoryFollowEnabled = memoryFollowButton.isSelected()
        );

        this.add(showDebuggerButton);
        this.add(memoryFollowButton);
    }

    public boolean isMemoryFollowEnabled() {
        return memoryFollowEnabled;
    }
}
