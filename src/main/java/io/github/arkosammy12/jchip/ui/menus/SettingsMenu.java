package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.ui.MainWindow;
import io.github.arkosammy12.jchip.ui.util.EnumMenu;
import io.github.arkosammy12.jchip.util.KeyboardLayout;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class SettingsMenu extends JMenu {

    private final EnumMenu<KeyboardLayout> keyboardLayoutMenu;

    private final JRadioButtonMenuItem showInfoPanelButton;

    public SettingsMenu(Jchip jchip, MainWindow mainWindow) {
        super("Settings");

        this.setMnemonic(KeyEvent.VK_S);

        JMenu volumeMenu = new JMenu("Volume");
        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setPaintTrack(true);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.addChangeListener(_ -> jchip.getAudioRenderer().setVolume(volumeSlider.getValue()));
        JPanel volumePanel = new JPanel();
        volumePanel.add(volumeSlider);
        volumeMenu.add(volumePanel);

        JRadioButtonMenuItem muteButton = new JRadioButtonMenuItem("Mute");
        muteButton.addActionListener(_ -> jchip.getAudioRenderer().setMuted(muteButton.isSelected()));
        muteButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, true));
        muteButton.setSelected(false);

        this.keyboardLayoutMenu = new EnumMenu<>("Keyboard Layout", KeyboardLayout.class, false);
        this.keyboardLayoutMenu.setState(KeyboardLayout.QWERTY);
        this.keyboardLayoutMenu.setMnemonic(KeyEvent.VK_K);
        this.keyboardLayoutMenu.setToolTipText("Select the desired keyboard layout configuration for using the CHIP-8 keypad.");

        this.showInfoPanelButton = new JRadioButtonMenuItem("Show info bar");
        this.showInfoPanelButton.setSelected(true);
        this.showInfoPanelButton.addActionListener(_ -> mainWindow.setInfoBarEnabled(this.showInfoPanelButton.isSelected()));

        this.add(volumeMenu);
        this.add(muteButton);
        this.addSeparator();
        this.add(keyboardLayoutMenu);
        this.addSeparator();
        this.add(showInfoPanelButton);
    }

    public Optional<KeyboardLayout> getKeyboardLayout() {
        return this.keyboardLayoutMenu.getState();
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        primarySettingsProvider.getKeyboardLayout().ifPresent(this.keyboardLayoutMenu::setState);
    }

}
