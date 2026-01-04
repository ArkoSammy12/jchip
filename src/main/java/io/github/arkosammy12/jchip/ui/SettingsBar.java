package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.ui.menus.*;
import io.github.arkosammy12.jchip.util.Variant;
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

    public SettingsBar(Jchip jchip, MainWindow mainWindow) {
        super();

        this.fileMenu = new FileMenu(mainWindow);
        this.emulatorMenu = new EmulatorMenu(jchip, mainWindow);
        this.settingsMenu = new SettingsMenu(jchip, mainWindow);
        DebugMenu debugMenu = new DebugMenu(mainWindow);
        HelpMenu helpMenu = new HelpMenu(mainWindow);

        this.add(fileMenu);
        this.add(emulatorMenu);
        this.add(settingsMenu);
        this.add(debugMenu);
        this.add(helpMenu);
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        this.fileMenu.initializeSettings(primarySettingsProvider);
        this.emulatorMenu.initializeSettings(primarySettingsProvider);
        this.settingsMenu.initializeSettings(primarySettingsProvider);
    }

    public void onBreakpoint() {
        this.emulatorMenu.onBreakpoint();
    }

    @Override
    public Optional<byte[]> getRawRom() {
        return this.fileMenu.getRawRom();
    }

    @Override
    public Optional<Path> getRomPath() {
        return this.fileMenu.getRomPath();
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
    public Optional<Variant> getVariant() {
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
    public Optional<Chip8EmulatorSettings.MemoryIncrementQuirk> getMemoryIncrementQuirk() {
        return this.emulatorMenu.getQuirksMenu().getMemoryIncrementQuirk();
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
