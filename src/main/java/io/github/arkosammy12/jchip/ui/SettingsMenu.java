package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.CommandLineArgs;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.util.KeyboardLayout;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class SettingsMenu extends JMenu {

    private final JMenuItem volumeUpButton;
    private final JMenuItem volumeDownButton;

    private final EnumMenu<KeyboardLayout> keyboardLayoutMenu;

    private final JRadioButtonMenuItem showInfoPanelButton;

    public SettingsMenu(JChip jchip) {
        super("Settings");

        this.setMnemonic(KeyEvent.VK_S);

        this.volumeDownButton = new JMenuItem("Volume Down");
        this.volumeDownButton.addActionListener(_ -> jchip.getSoundWriter().volumeDown());
        this.volumeDownButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK, true));
        this.volumeDownButton.setToolTipText("Decrease the sound volume of the emulator.");

        this.volumeUpButton = new JMenuItem("Volume Up");
        this.volumeUpButton.addActionListener(_ -> jchip.getSoundWriter().volumeUp());
        this.volumeUpButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.CTRL_DOWN_MASK, true));
        this.volumeUpButton.setToolTipText("Increase the sound volume of the emulator.");

        this.keyboardLayoutMenu = new EnumMenu<>("Keyboard Layout", KeyboardLayout.class, false);
        this.keyboardLayoutMenu.setState(KeyboardLayout.QWERTY);
        this.keyboardLayoutMenu.setMnemonic(KeyEvent.VK_K);
        this.keyboardLayoutMenu.setToolTipText("Select the desired keyboard layout configuration for using the CHIP-8 keypad.");

        this.showInfoPanelButton = new JRadioButtonMenuItem("Show Info Bar");
        this.showInfoPanelButton.setSelected(true);
        this.showInfoPanelButton.addActionListener(_ -> jchip.getMainWindow().setInfoPanelEnabled(this.showInfoPanelButton.isSelected()));

        this.add(volumeUpButton);
        this.add(volumeDownButton);
        this.addSeparator();
        this.add(keyboardLayoutMenu);
        this.addSeparator();
        this.add(showInfoPanelButton);
    }

    public Optional<KeyboardLayout> getKeyboardLayout() {
        return this.keyboardLayoutMenu.getState();
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        if (primarySettingsProvider instanceof CommandLineArgs cliArgs) {
            cliArgs.getKeyboardLayout().ifPresent(this.keyboardLayoutMenu::setState);
        }
    }

}
