package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.initializers.ApplicationInitializer;
import io.github.arkosammy12.jchip.config.Config;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializer;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializerConsumer;
import io.github.arkosammy12.jchip.ui.MainWindow;
import io.github.arkosammy12.jchip.ui.util.EnumMenu;
import io.github.arkosammy12.jchip.util.KeyboardLayout;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class SettingsMenu extends JMenu implements EmulatorInitializerConsumer {

    private final JSlider volumeSlider;
    private final JRadioButtonMenuItem muteButton;

    private final EnumMenu<KeyboardLayout> keyboardLayoutMenu;

    private final JRadioButtonMenuItem showInfoBarButton;

    public SettingsMenu(Jchip jchip, MainWindow mainWindow) {
        super("Settings");

        this.setMnemonic(KeyEvent.VK_S);

        JMenu volumeMenu = new JMenu("Volume");
        this.volumeSlider = new JSlider(0, 100, 50);
        this.volumeSlider.setPaintTrack(true);
        this.volumeSlider.setPaintTicks(true);
        this.volumeSlider.setPaintLabels(true);
        this.volumeSlider.setMajorTickSpacing(25);
        this.volumeSlider.setMinorTickSpacing(5);
        this.volumeSlider.addChangeListener(_ -> jchip.getAudioRenderer().setVolume(this.volumeSlider.getValue()));
        JPanel volumePanel = new JPanel();
        volumePanel.add(this.volumeSlider);
        volumeMenu.add(volumePanel);

        this.muteButton = new JRadioButtonMenuItem("Mute");
        this.muteButton.addChangeListener(_ -> jchip.getAudioRenderer().setMuted(this.muteButton.isSelected()));
        this.muteButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, true));
        this.muteButton.setSelected(false);

        this.keyboardLayoutMenu = new EnumMenu<>("Keyboard Layout", KeyboardLayout.class, false);
        this.keyboardLayoutMenu.setState(KeyboardLayout.QWERTY);
        this.keyboardLayoutMenu.setMnemonic(KeyEvent.VK_K);
        this.keyboardLayoutMenu.setToolTipText("Select the desired keyboard layout configuration for using the CHIP-8 keypad.");

        this.showInfoBarButton = new JRadioButtonMenuItem("Show info bar");
        this.showInfoBarButton.setSelected(true);
        this.showInfoBarButton.addChangeListener(_ -> mainWindow.setInfoBarEnabled(this.showInfoBarButton.isSelected()));

        this.add(volumeMenu);
        this.add(muteButton);
        this.addSeparator();
        this.add(keyboardLayoutMenu);
        this.addSeparator();
        this.add(showInfoBarButton);

        jchip.addShutdownListener(() -> {
            Config config = jchip.getConfig();

            config.setIntegerSettingIfPresent(Config.VOLUME, this.volumeSlider.getValue());
            config.setBooleanSettingIfPresent(Config.MUTED, this.muteButton.isSelected());
            config.setBooleanSettingIfPresent(Config.SHOW_INFO_BAR, this.showInfoBarButton.isSelected());

            config.setEnumSettingIfPresent(Config.KEYBOARD_LAYOUT, switch (this.getKeyboardLayout()) {
                case Optional<KeyboardLayout> optional when optional.isPresent() -> switch (optional.get()) {
                    case QWERTY -> Config.KeyboardLayoutValue.qwerty;
                    case DVORAK -> Config.KeyboardLayoutValue.dvorak;
                    case COLEMAK -> Config.KeyboardLayoutValue.colemak;
                    case AZERTY -> Config.KeyboardLayoutValue.azerty;
                };
                case null, default -> Config.KeyboardLayoutValue.qwerty;
            });
        });
    }

    public Optional<KeyboardLayout> getKeyboardLayout() {
        return this.keyboardLayoutMenu.getState();
    }

    @Override
    public void accept(EmulatorInitializer initializer) {
        initializer.getKeyboardLayout().ifPresent(this.keyboardLayoutMenu::setState);
        if (initializer instanceof ApplicationInitializer applicationInitializer) {
            applicationInitializer.getVolume().ifPresent(this.volumeSlider::setValue);
            applicationInitializer.isMuted().ifPresent(this.muteButton::setSelected);
            applicationInitializer.isShowingInfoBar().ifPresent(this.showInfoBarButton::setSelected);
        }
    }

}
