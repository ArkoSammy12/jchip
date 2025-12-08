package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.CLIArgs;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.util.KeyboardLayout;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class SettingsMenu extends JMenu {

    private final EnumMenu<KeyboardLayout> keyboardLayoutMenu;

    private final JRadioButtonMenuItem showInfoPanelButton;

    public SettingsMenu(Jchip jchip) {
        super("Settings");

        this.setMnemonic(KeyEvent.VK_S);

        JMenu volumeMenu = new JMenu("Volume");
        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.addChangeListener(_ -> jchip.getSoundWriter().setVolume(volumeSlider.getValue()));
        JPanel volumePanel = new JPanel();
        volumePanel.add(volumeSlider);
        volumeMenu.add(volumePanel);

        this.keyboardLayoutMenu = new EnumMenu<>("Keyboard Layout", KeyboardLayout.class, false);
        this.keyboardLayoutMenu.setState(KeyboardLayout.QWERTY);
        this.keyboardLayoutMenu.setMnemonic(KeyEvent.VK_K);
        this.keyboardLayoutMenu.setToolTipText("Select the desired keyboard layout configuration for using the CHIP-8 keypad.");

        this.showInfoPanelButton = new JRadioButtonMenuItem("Show Info Bar");
        this.showInfoPanelButton.setSelected(true);
        this.showInfoPanelButton.addActionListener(_ -> jchip.getMainWindow().setInfoPanelEnabled(this.showInfoPanelButton.isSelected()));

        this.add(volumeMenu);
        this.addSeparator();
        this.add(keyboardLayoutMenu);
        this.addSeparator();
        this.add(showInfoPanelButton);
    }

    public Optional<KeyboardLayout> getKeyboardLayout() {
        return this.keyboardLayoutMenu.getState();
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        if (primarySettingsProvider instanceof CLIArgs cliArgs) {
            cliArgs.getKeyboardLayout().ifPresent(this.keyboardLayoutMenu::setState);
        }
    }

}
