package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SettingsMenu extends JMenuBar {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8"};

    private final JChip jchip;

    private final JMenu fileMenu;
    private final JMenu quirksMenu;
    private final JMenu variantMenu;
    private final JMenu colorPaletteMenu;
    private final JMenu displayAngleMenu;
    private final JMenu instructionsPerFrameMenu;

    private Path selectedRomPath;

    private Chip8Variant selectedVariant;
    private final JRadioButtonMenuItem unspecifiedVariantButton;
    private final List<JRadioButtonMenuItem> variantButtons = new ArrayList<>();

    private final JCheckBoxMenuItem useCustomQuirksBox;
    private final JCheckBoxMenuItem doVFResetBox;
    private final JCheckBoxMenuItem doIncrementIndexBox;
    private final JCheckBoxMenuItem doDisplayWaitBox;
    private final JCheckBoxMenuItem doClippingBox;
    private final JCheckBoxMenuItem doShiftVXInPlaceBox;
    private final JCheckBoxMenuItem doJumpWithVXBox;

    Boolean doVFReset;
    Boolean doIncrementIndex;
    Boolean doDisplayWait;
    Boolean doClipping;
    Boolean doShiftVXInPlace;
    Boolean doJumpWithVX;

    private BuiltInColorPalette selectedPalette;
    private final JRadioButtonMenuItem unspecifiedPaletteButton;
    private final List<JRadioButtonMenuItem> paletteButtons = new ArrayList<>();

    private DisplayAngle selectedAngle;
    private final JRadioButtonMenuItem unspecifiedAngleButton;
    private final List<JRadioButtonMenuItem> angleButtons = new ArrayList<>();

    private Integer instructionsPerFrame;

    public SettingsMenu(JChip jChip) {
        this.jchip = jChip;

        this.fileMenu = new JMenu("File");
        this.quirksMenu = new JMenu("Quirks");
        this.variantMenu = new JMenu("Variant");
        this.colorPaletteMenu = new JMenu("Color Palette");
        this.displayAngleMenu = new JMenu("Display Angle");
        this.instructionsPerFrameMenu = new JMenu("Instructions per frame");

        JMenuItem openItem = new JMenuItem("Load ROM");
        openItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CHIP-8 ROMs", FILE_EXTENSIONS));
            Action details = chooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            if (chooser.showOpenDialog(this.jchip) == JFileChooser.APPROVE_OPTION) {
                if (this.selectedRomPath == null) {
                    this.jchip.reset();
                }
                    this.selectedRomPath = chooser.getSelectedFile().toPath().toAbsolutePath();
            }
        });

        ButtonGroup variantButtonGroup = new ButtonGroup();
        this.unspecifiedVariantButton = new JRadioButtonMenuItem("Unspecified");
        this.unspecifiedVariantButton.addActionListener(_ -> selectedVariant = null);
        this.unspecifiedVariantButton.setSelected(true);
        variantButtonGroup.add(this.unspecifiedVariantButton);
        for (Chip8Variant variant : Chip8Variant.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(variant.getDisplayName());
            item.addActionListener(_ -> {
                this.selectedVariant = variant;
            });
            this.variantButtons.add(item);
            variantButtonGroup.add(item);
        }

        this.doVFResetBox = new JCheckBoxMenuItem("Do VF Reset", false);
        doVFResetBox.setEnabled(false);
        doVFResetBox.addActionListener(_ -> this.doVFReset = doVFResetBox.getState());

        this.doIncrementIndexBox = new JCheckBoxMenuItem("Do Increment Index", false);
        doIncrementIndexBox.setEnabled(false);
        doIncrementIndexBox.addActionListener(_ -> this.doIncrementIndex = doIncrementIndexBox.getState());

        this.doDisplayWaitBox = new JCheckBoxMenuItem("Do Display Wait", false);
        doDisplayWaitBox.setEnabled(false);
        doDisplayWaitBox.addActionListener(_ -> this.doDisplayWait = doDisplayWaitBox.getState());

        this.doClippingBox = new JCheckBoxMenuItem("Do Clipping", false);
        doClippingBox.setEnabled(false);
        doClippingBox.addActionListener(_ -> this.doClipping = doClippingBox.getState());

        this.doShiftVXInPlaceBox = new JCheckBoxMenuItem("Do Shift VX In Place", false);
        doShiftVXInPlaceBox.setEnabled(false);
        doShiftVXInPlaceBox.addActionListener(_ -> this.doShiftVXInPlace = doShiftVXInPlaceBox.getState());

        this.doJumpWithVXBox = new JCheckBoxMenuItem("Do Jump With VX", false);
        doJumpWithVXBox.setEnabled(false);
        doJumpWithVXBox.addActionListener(_ -> this.doJumpWithVX = doJumpWithVXBox.getState());

        this.useCustomQuirksBox = new JCheckBoxMenuItem("Use Custom Quirks", false);
        this.useCustomQuirksBox.addActionListener(_ -> {
            boolean enabled = this.useCustomQuirksBox.getState();
            this.doVFResetBox.setEnabled(enabled);
            this.doIncrementIndexBox.setEnabled(enabled);
            this.doDisplayWaitBox.setEnabled(enabled);
            this.doClippingBox.setEnabled(enabled);
            this.doShiftVXInPlaceBox.setEnabled(enabled);
            this.doJumpWithVXBox.setEnabled(enabled);
        });

        ButtonGroup colorPaletteButtonGroup = new ButtonGroup();
        this.unspecifiedPaletteButton = new JRadioButtonMenuItem("Unspecified");
        this.unspecifiedPaletteButton.addActionListener(_ -> selectedPalette = null);
        this.unspecifiedPaletteButton.setSelected(true);
        colorPaletteButtonGroup.add(this.unspecifiedPaletteButton);
        for (BuiltInColorPalette palette : BuiltInColorPalette.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(palette.getDisplayName());
            item.addActionListener(_ -> {
                this.selectedPalette = palette;
            });
            this.paletteButtons.add(item);
            colorPaletteButtonGroup.add(item);
        }

        ButtonGroup displayAngleButtonGroup = new ButtonGroup();
        this.unspecifiedAngleButton = new JRadioButtonMenuItem("Unspecified");
        this.unspecifiedAngleButton.addActionListener(_ -> selectedAngle = null);
        this.unspecifiedAngleButton.setSelected(true);
        displayAngleButtonGroup.add(this.unspecifiedAngleButton);
        for (DisplayAngle angle : DisplayAngle.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(angle.getDisplayName());
            item.addActionListener(_ -> {
                this.selectedAngle = angle;
            });
            this.angleButtons.add(item);
            displayAngleButtonGroup.add(item);
        }

        JLabel label = new JLabel("IPF: ");
        JTextField ipfField = new JTextField(6);
        ipfField.setMaximumSize(ipfField.getPreferredSize());
        ipfField.setText(this.instructionsPerFrame != null ? this.instructionsPerFrame.toString() : "");
        ipfField.addActionListener(_ -> {
            try {
                int ipf = Integer.parseInt(ipfField.getText().trim());
                if (ipf > 0) {
                    this.instructionsPerFrame = ipf;
                }
            } catch (NumberFormatException ignored) {
                this.instructionsPerFrame = null;
            }
        });
        JPanel ipfPanel = new JPanel();
        ipfPanel.add(label);
        ipfPanel.add(ipfField);

        fileMenu.add(openItem);

        this.variantMenu.add(this.unspecifiedVariantButton);
        this.variantButtons.forEach(variantMenu::add);

        this.colorPaletteMenu.add(this.unspecifiedPaletteButton);
        this.paletteButtons.forEach(this.colorPaletteMenu::add);

        this.displayAngleMenu.add(this.unspecifiedAngleButton);
        this.angleButtons.forEach(this.displayAngleMenu::add);

        this.quirksMenu.add(this.useCustomQuirksBox);
        this.quirksMenu.add(this.doVFResetBox);
        this.quirksMenu.add(this.doIncrementIndexBox);
        this.quirksMenu.add(this.doDisplayWaitBox);
        this.quirksMenu.add(this.doClippingBox);
        this.quirksMenu.add(this.doShiftVXInPlaceBox);
        this.quirksMenu.add(this.doJumpWithVXBox);

        this.add(fileMenu);
        this.add(quirksMenu);
        this.add(variantMenu);
        this.add(colorPaletteMenu);
        this.add(displayAngleMenu);

        this.instructionsPerFrameMenu.add(ipfPanel);
        this.add(this.instructionsPerFrameMenu);

    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        this.selectedRomPath = primarySettingsProvider.getRomPath();
        primarySettingsProvider.getChip8Variant().ifPresent(variant -> this.selectedVariant = variant);
        primarySettingsProvider.doVFReset().ifPresent(val -> this.doVFReset = val);
        primarySettingsProvider.doIncrementIndex().ifPresent(val -> this.doIncrementIndex = val);
        primarySettingsProvider.doDisplayWait().ifPresent(val -> this.doDisplayWait = val);
        primarySettingsProvider.doClipping().ifPresent(val -> this.doClipping = val);
        primarySettingsProvider.doShiftVXInPlace().ifPresent(val -> this.doShiftVXInPlace = val);
        primarySettingsProvider.doJumpWithVX().ifPresent(val -> this.doJumpWithVX = val);
        primarySettingsProvider.getInstructionsPerFrame().ifPresent(val -> this.instructionsPerFrame = val);
    }

    public Path getRomPath() {
        return this.selectedRomPath;
    }

    public Optional<KeyboardLayout> getKeyboardLayout() {
        return Optional.empty();
    }

    public Optional<Integer> getInstructionsPerFrame() {
        return Optional.ofNullable(this.instructionsPerFrame);
    }

    public Optional<ColorPalette> getColorPalette() {
        return Optional.ofNullable(this.selectedPalette);
    }

    public Optional<DisplayAngle> getDisplayAngle() {
        return Optional.ofNullable(this.selectedAngle);
    }

    public Optional<Chip8Variant> getChip8Variant() {
        return Optional.ofNullable(this.selectedVariant);
    }

    public Optional<Boolean> doVFReset() {
        if (!this.useCustomQuirksBox.getState()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.doVFReset);
    }

    public Optional<Boolean> doIncrementIndex() {
        if (!this.useCustomQuirksBox.getState()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.doIncrementIndex);
    }

    public Optional<Boolean> doDisplayWait() {
        if (!this.useCustomQuirksBox.getState()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.doDisplayWait);
    }

    public Optional<Boolean> doClipping() {
        if (!this.useCustomQuirksBox.getState()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.doClipping);
    }

    public Optional<Boolean> doShiftVXInPlace() {
        if (!this.useCustomQuirksBox.getState()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.doShiftVXInPlace);
    }

    public Optional<Boolean> doJumpWithVX() {
        if (!this.useCustomQuirksBox.getState()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.doJumpWithVX);
    }

}
