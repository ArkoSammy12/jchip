package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.ui.util.BooleanMenu;
import io.github.arkosammy12.jchip.ui.util.EnumMenu;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class QuirksMenu extends JMenu {

    private final JRadioButtonMenuItem forceVariantQuirksButton;
    private final BooleanMenu doVFResetMenu;
    private final EnumMenu<Chip8EmulatorSettings.MemoryIncrementQuirk> memoryIncrementQuirkEnumMenu;
    private final BooleanMenu doDisplayWaitMenu;
    private final BooleanMenu doClippingMenu;
    private final BooleanMenu doShiftVXInPlaceMenu;
    private final BooleanMenu doJumpWithVXMenu;

    private boolean forceVariantQuirks;

    public QuirksMenu() {
        super("Quirks");

        this.setMnemonic(KeyEvent.VK_Q);

        this.forceVariantQuirksButton = new JRadioButtonMenuItem("Force Variant Quirks");
        this.forceVariantQuirksButton.addActionListener(_ -> this.forceVariantQuirks = forceVariantQuirksButton.isSelected());
        this.forceVariantQuirksButton.setToolTipText("Force the used quirks to be of the variant used to run the current ROM.");
        this.forceVariantQuirksButton.setMnemonic(KeyEvent.VK_F);

        this.doVFResetMenu = new BooleanMenu("VF Reset");
        this.doVFResetMenu.setMnemonic(KeyEvent.VK_V);

        this.memoryIncrementQuirkEnumMenu = new EnumMenu<>("I increment", Chip8EmulatorSettings.MemoryIncrementQuirk.class, true);
        this.memoryIncrementQuirkEnumMenu.setMnemonic(KeyEvent.VK_I);

        this.doDisplayWaitMenu = new BooleanMenu("Display Wait");
        this.doDisplayWaitMenu.setMnemonic(KeyEvent.VK_D);

        this.doClippingMenu = new BooleanMenu("Clipping");
        this.doClippingMenu.setMnemonic(KeyEvent.VK_C);

        this.doShiftVXInPlaceMenu = new BooleanMenu("Shift VX In Place");
        this.doShiftVXInPlaceMenu.setMnemonic(KeyEvent.VK_S);

        this.doJumpWithVXMenu = new BooleanMenu("Jump With VX");
        this.doJumpWithVXMenu.setMnemonic(KeyEvent.VK_J);

        this.add(this.forceVariantQuirksButton);
        this.addSeparator();
        this.add(this.doVFResetMenu);
        this.add(this.memoryIncrementQuirkEnumMenu);
        this.add(this.doDisplayWaitMenu);
        this.add(this.doClippingMenu);
        this.add(this.doShiftVXInPlaceMenu);
        this.add(this.doJumpWithVXMenu);

    }

    public boolean forceVariantQuirks() {
        return this.forceVariantQuirks;
    }

    public Optional<Boolean> doVFReset() {
        return this.doVFResetMenu.getState();
    }

    public Optional<Chip8EmulatorSettings.MemoryIncrementQuirk> getMemoryIncrementQuirk() {
        return this.memoryIncrementQuirkEnumMenu.getState();
    }

    public Optional<Boolean> doDisplayWait() {
        return this.doDisplayWaitMenu.getState();
    }

    public Optional<Boolean> doClipping() {
        return this.doClippingMenu.getState();
    }

    public Optional<Boolean> doShiftVXInPlace() {
        return this.doShiftVXInPlaceMenu.getState();
    }

    public Optional<Boolean> doJumpWithVX() {
        return this.doJumpWithVXMenu.getState();
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        this.forceVariantQuirks = primarySettingsProvider.useVariantQuirks();
        this.forceVariantQuirksButton.setSelected(this.forceVariantQuirks);
        primarySettingsProvider.doVFReset().ifPresent(this.doVFResetMenu::setState);
        primarySettingsProvider.getMemoryIncrementQuirk().ifPresent(this.memoryIncrementQuirkEnumMenu::setState);
        primarySettingsProvider.doDisplayWait().ifPresent(this.doDisplayWaitMenu::setState);
        primarySettingsProvider.doClipping().ifPresent(this.doClippingMenu::setState);
        primarySettingsProvider.doShiftVXInPlace().ifPresent(this.doShiftVXInPlaceMenu::setState);
        primarySettingsProvider.doJumpWithVX().ifPresent(this.doJumpWithVXMenu::setState);
    }

}
