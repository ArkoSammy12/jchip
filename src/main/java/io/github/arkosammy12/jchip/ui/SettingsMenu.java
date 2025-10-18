package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Path;
import java.util.Optional;

public class SettingsMenu extends JMenuBar implements PrimarySettingsProvider {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8"};

    private final JChip jchip;

    private final JMenu fileMenu;
    private final JMenu quirksMenu;
    private final EnumMenu<Chip8Variant> variantMenu;
    private final EnumMenu<BuiltInColorPalette> colorPaletteMenu;
    private final EnumMenu<DisplayAngle> displayAngleMenu;
    private final EnumMenu<KeyboardLayout> keyboardLayoutMenu;
    private final JMenu instructionsPerFrameMenu;
    private final DebuggerSettingsMenu debuggerMenu;

    private final JRadioButtonMenuItem useVariantQuirksButton;
    private final QuirkSubMenu doVFResetMenu;
    private final QuirkSubMenu doIncrementIndexMenu;
    private final QuirkSubMenu doDisplayWaitMenu;
    private final QuirkSubMenu doClippingMenu;
    private final QuirkSubMenu doShiftVXInPlaceMenu;
    private final QuirkSubMenu doJumpWithVXMenu;

    private final JTextField instructionsPerFrameField;

    private Path selectedRomPath;
    private Integer instructionsPerFrame;
    private boolean useVariantQuirks;

    public SettingsMenu(JChip jChip) {
        super();
        this.jchip = jChip;

        this.fileMenu = new JMenu("File");
        this.quirksMenu = new JMenu("Quirks");
        this.variantMenu = new EnumMenu<>("Variant", Chip8Variant.class);
        this.colorPaletteMenu = new EnumMenu<>("Color Palette", BuiltInColorPalette.class);
        this.displayAngleMenu = new EnumMenu<>("Display Angle", DisplayAngle.class);
        this.keyboardLayoutMenu = new EnumMenu<>("Keyboard Layout", KeyboardLayout.class);
        this.instructionsPerFrameMenu = new JMenu("Instructions per frame");
        this.debuggerMenu = new DebuggerSettingsMenu(jchip);

        JMenuItem openItem = new JMenuItem("Load ROM");
        openItem.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CHIP-8 ROMs", FILE_EXTENSIONS));
            Action details = chooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            if (chooser.showOpenDialog(this.getParent()) == JFileChooser.APPROVE_OPTION) {
                this.selectedRomPath = chooser.getSelectedFile().toPath().toAbsolutePath();
            }
        });

        this.useVariantQuirksButton = new JRadioButtonMenuItem("Use Variant Quirks");
        this.useVariantQuirksButton.addActionListener(_ -> this.useVariantQuirks = useVariantQuirksButton.isSelected());

        this.doVFResetMenu = new QuirkSubMenu("Do VF Reset");
        this.doIncrementIndexMenu = new QuirkSubMenu("Do Increment Index");
        this.doDisplayWaitMenu = new QuirkSubMenu("Do Display Wait");
        this.doClippingMenu = new QuirkSubMenu("Do Clipping");
        this.doShiftVXInPlaceMenu = new QuirkSubMenu("Do Shift VX In Place");
        this.doJumpWithVXMenu = new QuirkSubMenu("Do Jump With VX");

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
                        "The instructions per frame value must be a valid integer!",
                        "Incorrect formatting",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        });
        JPanel ipfPanel = new JPanel();
        ipfPanel.add(label);
        ipfPanel.add(instructionsPerFrameField);

        fileMenu.add(openItem);

        this.quirksMenu.add(this.useVariantQuirksButton);
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
        this.add(keyboardLayoutMenu);

        this.instructionsPerFrameMenu.add(ipfPanel);
        this.add(this.instructionsPerFrameMenu);

        this.add(debuggerMenu);

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
        primarySettingsProvider.getInstructionsPerFrame().ifPresent(val -> {
            this.instructionsPerFrame = val;
            this.instructionsPerFrameField.setText(String.valueOf(val));
        });
        this.useVariantQuirks = primarySettingsProvider.useVariantQuirks();
        this.useVariantQuirksButton.setSelected(this.useVariantQuirks);
    }

    @Override
    public Path getRomPath() {
        return this.selectedRomPath;
    }

    public DebuggerSettingsMenu getDebuggerSettingsMenu() {
        return this.debuggerMenu;
    }

    @Override
    public Optional<Integer> getInstructionsPerFrame() {
        return Optional.ofNullable(this.instructionsPerFrame);
    }

    @Override
    public Optional<ColorPalette> getColorPalette() {
        Optional<BuiltInColorPalette> optionalBuiltInColorPalette = this.colorPaletteMenu.getState();
        if (optionalBuiltInColorPalette.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(optionalBuiltInColorPalette.get());
    }

    @Override
    public Optional<DisplayAngle> getDisplayAngle() {
        return this.displayAngleMenu.getState();
    }

    @Override
    public Optional<KeyboardLayout> getKeyboardLayout() {
        return this.keyboardLayoutMenu.getState();
    }

    @Override
    public Optional<Chip8Variant> getChip8Variant() {
        return this.variantMenu.getState();
    }

    @Override
    public boolean useVariantQuirks() {
        return this.useVariantQuirks;
    }

    @Override
    public Optional<Boolean> doVFReset() {
        return this.doVFResetMenu.getState();
    }

    @Override
    public Optional<Boolean> doIncrementIndex() {
        return this.doIncrementIndexMenu.getState();
    }

    @Override
    public Optional<Boolean> doDisplayWait() {
        return this.doDisplayWaitMenu.getState();
    }

    @Override
    public Optional<Boolean> doClipping() {
        return this.doClippingMenu.getState();
    }

    @Override
    public Optional<Boolean> doShiftVXInPlace() {
        return this.doShiftVXInPlaceMenu.getState();
    }

    @Override
    public Optional<Boolean> doJumpWithVX() {
        return this.doJumpWithVXMenu.getState();
    }

}
