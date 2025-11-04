package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class EmulatorMenu extends JMenu {

    private final JMenuItem resetButton = new JMenuItem("Reset");
    private final JRadioButtonMenuItem pauseButton = new JRadioButtonMenuItem("Pause");
    private final JMenuItem stepFrameButton = new JMenuItem("Step Frame");
    private final JMenuItem stepCycleButton = new JMenuItem("Step Cycle");
    private final JMenuItem stopButton = new JMenuItem("Stop");

    private final QuirksMenu quirksMenu;
    private final EnumMenu<Chip8Variant> variantMenu;
    private final EnumMenu<BuiltInColorPalette> colorPaletteMenu;
    private final EnumMenu<DisplayAngle> displayAngleMenu;
    private final JMenuItem instructionsPerFrameMenu;

    private final JTextField instructionsPerFrameField;

    private Integer instructionsPerFrame;

    public EmulatorMenu(JChip jchip) {
        super("Emulator");

        this.setMnemonic(KeyEvent.VK_E);

        this.pauseButton.addActionListener(_ -> {
            jchip.setPaused(pauseButton.isSelected());
            this.stepFrameButton.setEnabled(pauseButton.isSelected());
            this.stepCycleButton.setEnabled(pauseButton.isSelected());
        });
        this.pauseButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        this.pauseButton.setEnabled(false);
        this.pauseButton.setSelected(false);
        this.pauseButton.setToolTipText("Pause execution of the emulator.");

        this.resetButton.addActionListener(_ -> {
            jchip.reset();
            this.pauseButton.setEnabled(true);
        });
        this.resetButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        this.resetButton.setToolTipText("Apply any emulator setting changes, reload the ROM and begin emulation from scratch.");

        this.stopButton.addActionListener(_ -> {
            jchip.setPaused(false);
            jchip.stop();
            this.pauseButton.setSelected(false);
            this.pauseButton.setEnabled(false);
        });
        this.stopButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        this.stopButton.setToolTipText("Stop emulation of the currently running ROM.");

        this.stepFrameButton.addActionListener(_ -> {
            if (!this.pauseButton.isEnabled()) {
                return;
            }
            jchip.stepFrame();
        });
        this.stepFrameButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        this.stepFrameButton.setEnabled(false);
        this.stepFrameButton.setToolTipText("Makes the emulator execute one frame's worth of instructions.");

        this.stepCycleButton.addActionListener(_ -> {
            if (!this.pauseButton.isEnabled()) {
                return;
            }
            jchip.stepCycle();
        });
        this.stepCycleButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        this.stepCycleButton.setEnabled(false);
        this.stepCycleButton.setToolTipText("Makes the emulator execute one processor cycle, which can either be a full instruction, or a part of an instruction depending on the variant.");

        this.quirksMenu = new QuirksMenu();
        this.variantMenu = new EnumMenu<>("Variant", Chip8Variant.class, true);
        this.variantMenu.setMnemonic(KeyEvent.VK_V);
        this.variantMenu.setToolTipText("Select the desired CHIP-8 variant or leave unspecified.");

        this.colorPaletteMenu = new EnumMenu<>("Color Palette", BuiltInColorPalette.class, true);
        this.colorPaletteMenu.setMnemonic(KeyEvent.VK_C);
        this.colorPaletteMenu.setToolTipText("Select the desired display color palette or leave unspecified.");

        this.displayAngleMenu = new EnumMenu<>("Display Angle", DisplayAngle.class, true);
        this.displayAngleMenu.setMnemonic(KeyEvent.VK_D);
        this.displayAngleMenu.setToolTipText("Select the screen rotation or leave unspecified.");

        this.instructionsPerFrameMenu = new JMenu("Instructions per frame");
        this.instructionsPerFrameMenu.setMnemonic(KeyEvent.VK_I);
        this.instructionsPerFrameMenu.setToolTipText("Set the desired IPF or leave blank to unspecify.");

        JLabel label = new JLabel("IPF: ");
        this.instructionsPerFrameField = new JTextField(6);
        instructionsPerFrameField.setMaximumSize(instructionsPerFrameField.getPreferredSize());
        instructionsPerFrameField.setText(this.instructionsPerFrame != null ? this.instructionsPerFrame.toString() : "");
        instructionsPerFrameField.addActionListener(_ -> {
            String text = instructionsPerFrameField.getText().trim();
            if (text.isEmpty()) {
                this.instructionsPerFrame = null;
                return;
            }
            try {
                int ipf  = Integer.parseInt(text);
                if (ipf > 0) {
                    this.instructionsPerFrame = ipf;
                }
            } catch (NumberFormatException ignored) {
                JOptionPane.showMessageDialog(
                        jchip.getMainWindow(),
                        "The IPF value must be a valid integer!",
                        "Incorrect formatting",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        });
        JPanel ipfPanel = new JPanel();
        ipfPanel.add(label);
        ipfPanel.add(instructionsPerFrameField);

        this.add(resetButton);
        this.add(pauseButton);
        this.add(stopButton);
        this.add(stepFrameButton);
        this.add(stepCycleButton);

        this.addSeparator();

        this.add(quirksMenu);
        this.add(variantMenu);
        this.add(colorPaletteMenu);
        this.add(displayAngleMenu);

        this.instructionsPerFrameMenu.add(ipfPanel);
        this.add(this.instructionsPerFrameMenu);

    }

    public QuirksMenu getQuirksMenu() {
        return this.quirksMenu;
    }

    public Optional<Chip8Variant> getChip8Variant() {
        return this.variantMenu.getState();
    }

    public Optional<ColorPalette> getColorPalette() {
        Optional<BuiltInColorPalette> optionalBuiltInColorPalette = this.colorPaletteMenu.getState();
        if (optionalBuiltInColorPalette.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(optionalBuiltInColorPalette.get());
    }

    public Optional<DisplayAngle> getDisplayAngle() {
        return this.displayAngleMenu.getState();
    }

    public Optional<Integer> getInstructionsPerFrame() {
        return Optional.ofNullable(this.instructionsPerFrame);
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        this.quirksMenu.initializeSettings(primarySettingsProvider);
        primarySettingsProvider.getChip8Variant().ifPresent(this.variantMenu::setState);
        primarySettingsProvider.getDisplayAngle().ifPresent(this.displayAngleMenu::setState);
        primarySettingsProvider.getInstructionsPerFrame().ifPresent(val -> {
            this.instructionsPerFrame = val;
            this.instructionsPerFrameField.setText(String.valueOf(val));
        });
        this.resetButton.doClick();
    }

}
