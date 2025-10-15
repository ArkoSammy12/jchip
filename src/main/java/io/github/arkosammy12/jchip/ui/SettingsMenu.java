package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Path;
import java.util.Optional;

public class SettingsMenu extends JMenuBar {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8"};

    private final JChip jchip;

    private final JMenu fileMenu;
    private final JMenu quirksMenu;
    private final EnumMenu<Chip8Variant> variantMenu;
    private final EnumMenu<BuiltInColorPalette> colorPaletteMenu;
    private final EnumMenu<DisplayAngle> displayAngleMenu;
    private final JMenu instructionsPerFrameMenu;

    private final QuirkSubMenu doVFResetMenu;
    private final QuirkSubMenu doIncrementIndexMenu;
    private final QuirkSubMenu doDisplayWaitMenu;
    private final QuirkSubMenu doClippingMenu;
    private final QuirkSubMenu doShiftVXInPlaceMenu;
    private final QuirkSubMenu doJumpWithVXMenu;

    private Path selectedRomPath;
    private Integer instructionsPerFrame;

    public SettingsMenu(JChip jChip) {
        super();
        this.jchip = jChip;

        this.fileMenu = new JMenu("File");
        this.quirksMenu = new JMenu("Quirks");
        this.variantMenu = new EnumMenu<>("Variant", Chip8Variant.class);
        this.colorPaletteMenu = new EnumMenu<>("Color Palette", BuiltInColorPalette.class);
        this.displayAngleMenu = new EnumMenu<>("Display Angle", DisplayAngle.class);
        this.instructionsPerFrameMenu = new JMenu("Instructions per frame");

        JMenuItem openItem = new JMenuItem("Load ROM");
        openItem.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CHIP-8 ROMs", FILE_EXTENSIONS));
            Action details = chooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            if (chooser.showOpenDialog(this.getParent()) == JFileChooser.APPROVE_OPTION) {
                if (this.selectedRomPath == null) {
                    this.jchip.reset();
                }
                    this.selectedRomPath = chooser.getSelectedFile().toPath().toAbsolutePath();
            }
        });

        this.doVFResetMenu = new QuirkSubMenu("Do VF Reset");
        this.doIncrementIndexMenu = new QuirkSubMenu("Do Increment Index");
        this.doDisplayWaitMenu = new QuirkSubMenu("Do Display Wait");
        this.doClippingMenu = new QuirkSubMenu("Do Clipping");
        this.doShiftVXInPlaceMenu = new QuirkSubMenu("Do Shift VX In Place");
        this.doJumpWithVXMenu = new QuirkSubMenu("Do Jump With VX");

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

        this.quirksMenu.add(this.doVFResetMenu);
        this.quirksMenu.add(this.doIncrementIndexMenu);
        this.quirksMenu.add(this.doDisplayWaitMenu);
        this.quirksMenu.add(this.doClippingMenu);
        this.quirksMenu.add(this.doShiftVXInPlaceMenu);
        this.quirksMenu.add(this.doJumpWithVXMenu);

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
        primarySettingsProvider.getChip8Variant().ifPresent(this.variantMenu::setState);
        primarySettingsProvider.doVFReset().ifPresent(this.doVFResetMenu::setState);
        primarySettingsProvider.doIncrementIndex().ifPresent(this.doIncrementIndexMenu::setState);
        primarySettingsProvider.doDisplayWait().ifPresent(this.doDisplayWaitMenu::setState);
        primarySettingsProvider.doClipping().ifPresent(this.doClippingMenu::setState);
        primarySettingsProvider.doShiftVXInPlace().ifPresent(this.doShiftVXInPlaceMenu::setState);
        primarySettingsProvider.doJumpWithVX().ifPresent(this.doJumpWithVXMenu::setState);
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

    public Optional<BuiltInColorPalette> getColorPalette() {
        return this.colorPaletteMenu.getState();
    }

    public Optional<DisplayAngle> getDisplayAngle() {
        return this.displayAngleMenu.getState();
    }

    public Optional<Chip8Variant> getChip8Variant() {
        return this.variantMenu.getState();
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

}
