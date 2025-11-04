package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.ColorPalette;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Optional;

public class SettingsBar extends JMenuBar implements PrimarySettingsProvider {


    private final FileMenu fileMenu;
    private final EmulatorMenu emulatorMenu;
    private final SettingsMenu settingsMenu;
    private final DebuggerMenu debuggerMenu;
    private final HelpMenu helpMenu;


    public SettingsBar(JChip jchip) {
        super();

        this.fileMenu = new FileMenu(jchip);
        this.emulatorMenu = new EmulatorMenu(jchip);
        this.settingsMenu = new SettingsMenu(jchip);
        this.debuggerMenu = new DebuggerMenu(jchip);
        this.helpMenu = new HelpMenu(jchip);

        this.add(fileMenu);
        this.add(emulatorMenu);
        this.add(settingsMenu);
        this.add(debuggerMenu);
        this.add(helpMenu);

    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        this.fileMenu.initializeSettings(primarySettingsProvider);
        this.emulatorMenu.initializeSettings(primarySettingsProvider);
    }

    @Override
    public Path getRomPath() {
        return this.fileMenu.getRomPath();
    }

    public DebuggerMenu getDebuggerSettingsMenu() {
        return this.debuggerMenu;
    }

    @Override
    public Optional<Integer> getInstructionsPerFrame() {
        return this.emulatorMenu.getInstructionsPerFrame();
    }

    @Override
    public Optional<ColorPalette> getColorPalette() {
        return this.emulatorMenu.getColorPalette();
    }

    @Override
    public Optional<DisplayAngle> getDisplayAngle() {
        return this.emulatorMenu.getDisplayAngle();
    }

    public Optional<KeyboardLayout> getKeyboardLayout() {
        return this.settingsMenu.getKeyboardLayout();
    }

    @Override
    public Optional<Chip8Variant> getChip8Variant() {
        return this.emulatorMenu.getChip8Variant();
    }

    @Override
    public boolean useVariantQuirks() {
        return this.emulatorMenu.getQuirksMenu().forceVariantQuirks();
    }

    @Override
    public Optional<Boolean> doVFReset() {
        return this.emulatorMenu.getQuirksMenu().doVFReset();
    }

    @Override
    public Optional<Boolean> doIncrementIndex() {
        return this.emulatorMenu.getQuirksMenu().doIncrementIndex();
    }

    @Override
    public Optional<Boolean> doDisplayWait() {
        return this.emulatorMenu.getQuirksMenu().doDisplayWait();
    }

    @Override
    public Optional<Boolean> doClipping() {
        return this.emulatorMenu.getQuirksMenu().doClipping();
    }

    @Override
    public Optional<Boolean> doShiftVXInPlace() {
        return this.emulatorMenu.getQuirksMenu().doShiftVXInPlace();
    }

    @Override
    public Optional<Boolean> doJumpWithVX() {
        return this.emulatorMenu.getQuirksMenu().doJumpWithVX();
    }

}
