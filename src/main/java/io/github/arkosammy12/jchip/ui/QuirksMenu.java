package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class QuirksMenu extends JMenu {

    private final JRadioButtonMenuItem forceVariantQuirksButton;
    private final QuirkSubMenu doVFResetMenu;
    private final QuirkSubMenu doIncrementIndexMenu;
    private final QuirkSubMenu doDisplayWaitMenu;
    private final QuirkSubMenu doClippingMenu;
    private final QuirkSubMenu doShiftVXInPlaceMenu;
    private final QuirkSubMenu doJumpWithVXMenu;

    private boolean forceVariantQuirks;

    public QuirksMenu() {
        super("Quirks");

        this.setMnemonic(KeyEvent.VK_Q);
        this.setToolTipText("Configure the emulator's quirk settings.");

        this.forceVariantQuirksButton = new JRadioButtonMenuItem("Force Variant Quirks");
        this.forceVariantQuirksButton.addActionListener(_ -> this.forceVariantQuirks = forceVariantQuirksButton.isSelected());
        this.forceVariantQuirksButton.setToolTipText("Force the used quirks to be of the variant used to run the current ROM.");
        this.forceVariantQuirksButton.setMnemonic(KeyEvent.VK_F);

        this.doVFResetMenu = new QuirkSubMenu("VF Reset");
        this.doVFResetMenu.setMnemonic(KeyEvent.VK_V);
        this.doVFResetMenu.setToolTipText("Toggle the VF Reset quirk or leave unspecified.");

        this.doIncrementIndexMenu = new QuirkSubMenu("Increment Index");
        this.doIncrementIndexMenu.setMnemonic(KeyEvent.VK_I);
        this.doIncrementIndexMenu.setToolTipText("Toggle the Increment Index quirk or leave unspecified.");

        this.doDisplayWaitMenu = new QuirkSubMenu("Display Wait");
        this.doDisplayWaitMenu.setMnemonic(KeyEvent.VK_D);
        this.doDisplayWaitMenu.setToolTipText("Toggle the Display Wait quirk or leave unspecified.");

        this.doClippingMenu = new QuirkSubMenu("Clipping");
        this.doClippingMenu.setMnemonic(KeyEvent.VK_C);
        this.doClippingMenu.setToolTipText("Toggle the Clipping quirk or leave unspecified.");

        this.doShiftVXInPlaceMenu = new QuirkSubMenu("Shift VX In Place");
        this.doShiftVXInPlaceMenu.setMnemonic(KeyEvent.VK_S);
        this.doShiftVXInPlaceMenu.setToolTipText("Toggle the Shift VX in Place quirk or leave unspecified.");

        this.doJumpWithVXMenu = new QuirkSubMenu("Jump With VX");
        this.doJumpWithVXMenu.setMnemonic(KeyEvent.VK_J);
        this.doJumpWithVXMenu.setToolTipText("Toggle the Jump with VX quirk or leave unspecified.");

        this.add(this.forceVariantQuirksButton);
        this.add(this.doVFResetMenu);
        this.add(this.doIncrementIndexMenu);
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

    public Optional<Boolean> doIncrementIndex() {
        return this.doIncrementIndexMenu.getState();
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
        primarySettingsProvider.doIncrementIndex().ifPresent(this.doIncrementIndexMenu::setState);
        primarySettingsProvider.doDisplayWait().ifPresent(this.doDisplayWaitMenu::setState);
        primarySettingsProvider.doClipping().ifPresent(this.doClippingMenu::setState);
        primarySettingsProvider.doShiftVXInPlace().ifPresent(this.doShiftVXInPlaceMenu::setState);
        primarySettingsProvider.doJumpWithVX().ifPresent(this.doJumpWithVXMenu::setState);
    }

}
