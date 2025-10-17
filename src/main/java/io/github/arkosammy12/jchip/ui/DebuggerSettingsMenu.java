package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;

import javax.swing.*;

public class DebuggerSettingsMenu extends JMenu {

    private final JRadioButtonMenuItem showDebuggerButton;
    private MemoryFollowMode currentMemoryFollowMode = MemoryFollowMode.FOLLOW_I;

    public DebuggerSettingsMenu(JChip jchip) {
        super("Debugger");

        this.showDebuggerButton = new JRadioButtonMenuItem("Show");
        this.showDebuggerButton.addActionListener(_ -> {
            jchip.getMainWindow().setDebuggerViewEnabled(showDebuggerButton.isSelected());
        });

        JMenu memoryFollowModeMenu = new JMenu("Memory follow mode");

        ButtonGroup memoryFollowModeButtonGroup = new ButtonGroup();

        JRadioButtonMenuItem noFollowButton = new JRadioButtonMenuItem("No Follow");
        noFollowButton.addActionListener(_ -> this.currentMemoryFollowMode = MemoryFollowMode.NO_FOLLOW);

        JRadioButtonMenuItem followPcButton = new JRadioButtonMenuItem("Follow PC");
        followPcButton.addActionListener(_ -> this.currentMemoryFollowMode = MemoryFollowMode.FOLLOW_PC);

        JRadioButtonMenuItem followIButton = new JRadioButtonMenuItem("Follow I");
        followIButton.setSelected(true);
        followIButton.addActionListener(_ -> this.currentMemoryFollowMode = MemoryFollowMode.FOLLOW_I);

        memoryFollowModeButtonGroup.add(noFollowButton);
        memoryFollowModeButtonGroup.add(followPcButton);
        memoryFollowModeButtonGroup.add(followIButton);

        memoryFollowModeMenu.add(noFollowButton);
        memoryFollowModeMenu.add(followPcButton);
        memoryFollowModeMenu.add(followIButton);

        this.add(showDebuggerButton);
        this.add(memoryFollowModeMenu);

    }

    public MemoryFollowMode getCurrentMemoryFollowMode() {
        return this.currentMemoryFollowMode;
    }

    public enum MemoryFollowMode {
        NO_FOLLOW,
        FOLLOW_PC,
        FOLLOW_I
    }

}
