package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.Jchip;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DebugMenu extends JMenu {

    private final JRadioButtonMenuItem showDebuggerButton;
    private final JCheckBoxMenuItem memoryFollowButton;
    private boolean memoryFollowEnabled = true;

    public DebugMenu(Jchip jchip) {
        super("Debug");

        this.setMnemonic(KeyEvent.VK_D);

        this.showDebuggerButton = new JRadioButtonMenuItem("Show debug view");
        this.showDebuggerButton.addActionListener(_ ->
                jchip.getMainWindow().setDebuggerViewEnabled(showDebuggerButton.isSelected())
        );

        this.memoryFollowButton = new JCheckBoxMenuItem("Memory Follow");
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
