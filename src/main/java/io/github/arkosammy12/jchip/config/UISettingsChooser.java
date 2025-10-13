package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.nio.file.Path;
import java.util.Optional;

public class UISettingsChooser implements PrimarySettingsProvider {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8"};

    private Path romPath;

    private Chip8Variant chip8Variant;
    private KeyboardLayout keyboardLayout;
    private ColorPalette colorPalette;
    private DisplayAngle displayAngle;
    private Integer instructionsPerFrame;
    private Boolean doVFReset;
    private Boolean doIncrementIndex;
    private Boolean doDisplayWait;
    private Boolean doClipping;
    private Boolean doShiftVXInPlace;
    private Boolean doJumpWithVX;

    public UISettingsChooser() {
        this.showUI();
    }

    private void showUI() {
        JDialog dialog = new JDialog(null, String.format("jchip %s - Setup", Main.VERSION_STRING), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 550);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton romButton = new JButton("Select ROM...");
        JLabel romLabel = new JLabel("No file selected");
        romButton.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CHIP-8 ROMs", FILE_EXTENSIONS));
            Action details = chooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                this.romPath = chooser.getSelectedFile().toPath().toAbsolutePath();
                romLabel.setText(this.romPath.toString());
            }
        });
        panel.add(new JLabel("ROM File (required):"));
        panel.add(romButton);
        panel.add(romLabel);

        JComboBox<Chip8Variant> variantBox = new JComboBox<>(Chip8Variant.values());
        variantBox.insertItemAt(null, 0);
        variantBox.setSelectedIndex(0);
        variantBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof Chip8Variant variant ? variant.getDisplayName() : "Unspecified");
                return this;
            }
        });
        panel.add(new JLabel("CHIP-8 Variant:"));
        panel.add(variantBox);

        JTextField ipfField = new JTextField();
        panel.add(new JLabel("Instructions per frame:"));
        panel.add(ipfField);

        JComboBox<DisplayAngle> angleBox = new JComboBox<>(DisplayAngle.values());
        angleBox.insertItemAt(null, 0);
        angleBox.setSelectedIndex(0);
        angleBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof DisplayAngle angle ? angle.getDisplayName() : "Unspecified");
                return this;
            }
        });
        panel.add(new JLabel("Display angle:"));
        panel.add(angleBox);

        JComboBox<KeyboardLayout> layoutBox = new JComboBox<>(KeyboardLayout.values());
        layoutBox.insertItemAt(null, 0);
        layoutBox.setSelectedIndex(0);
        layoutBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof KeyboardLayout layout ? layout.getDisplayName() : "Unspecified");
                return this;
            }
        });
        panel.add(new JLabel("Keyboard layout:"));
        panel.add(layoutBox);

        JComboBox<BuiltInColorPalette> colorPaletteBox = new JComboBox<>(BuiltInColorPalette.values());
        colorPaletteBox.insertItemAt(null, 0);
        colorPaletteBox.setSelectedIndex(0);
        colorPaletteBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof BuiltInColorPalette palette ? palette.getDisplayName() : "Unspecified");
                return this;
            }
        });
        panel.add(new JLabel("Color Palette:"));
        panel.add(colorPaletteBox);

        JCheckBox doVFResetBox = new JCheckBox("Do VF Reset");
        JCheckBox doIncrementIndexBox = new JCheckBox("Do Increment Index");
        JCheckBox doDisplayWaitBox = new JCheckBox("Do Display Wait");
        JCheckBox doClippingBox = new JCheckBox("Do Clipping");
        JCheckBox doShiftVXInPlaceBox = new JCheckBox("Do Shift VX in place");
        JCheckBox doJumpWithVXBox = new JCheckBox("Do Jump with VX");

        doVFResetBox.setEnabled(false);
        doIncrementIndexBox.setEnabled(false);
        doDisplayWaitBox.setEnabled(false);
        doClippingBox.setEnabled(false);
        doShiftVXInPlaceBox.setEnabled(false);
        doJumpWithVXBox.setEnabled(false);

        JCheckBox useCustomQuirksBox = new JCheckBox("Use custom quirks");
        useCustomQuirksBox.addActionListener(_ -> {
            boolean selectable = useCustomQuirksBox.isSelected();
            doVFResetBox.setEnabled(selectable);
            doIncrementIndexBox.setEnabled(selectable);
            doDisplayWaitBox.setEnabled(selectable);
            doClippingBox.setEnabled(selectable);
            doShiftVXInPlaceBox.setEnabled(selectable);
            doJumpWithVXBox.setEnabled(selectable);
        });

        panel.add(new JLabel("Quirks:"));
        panel.add(useCustomQuirksBox);
        panel.add(doVFResetBox);
        panel.add(doIncrementIndexBox);
        panel.add(doDisplayWaitBox);
        panel.add(doClippingBox);
        panel.add(doShiftVXInPlaceBox);
        panel.add(doJumpWithVXBox);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(_ -> {
            if (this.romPath == null) {
                JOptionPane.showMessageDialog(dialog,
                        "You must select a ROM file before starting the emulator.",
                        "Missing ROM Path",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            this.chip8Variant = (Chip8Variant) variantBox.getSelectedItem();
            this.displayAngle = (DisplayAngle) angleBox.getSelectedItem();
            this.keyboardLayout = (KeyboardLayout) layoutBox.getSelectedItem();
            this.colorPalette = (ColorPalette) colorPaletteBox.getSelectedItem();

            if (!ipfField.getText().isEmpty()) {
                try {
                    this.instructionsPerFrame = Integer.parseInt(ipfField.getText().trim());
                } catch (NumberFormatException ignored) {
                    JOptionPane.showMessageDialog(dialog,
                            "The instructions per frame value must be a valid integer!",
                            "Incorrect formatting",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (useCustomQuirksBox.isSelected()) {
                this.doVFReset = doVFResetBox.isSelected();
                this.doIncrementIndex = doIncrementIndexBox.isSelected();
                this.doDisplayWait = doDisplayWaitBox.isSelected();
                this.doClipping = doClippingBox.isSelected();
                this.doShiftVXInPlace = doShiftVXInPlaceBox.isSelected();
                this.doJumpWithVX = doJumpWithVXBox.isSelected();
            }
            dialog.dispose();
        });

        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
        dialog.add(runButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (this.romPath == null) {
            Logger.warn("No ROM path provided. Exiting...");
            System.exit(0);
        }
    }

    public Path getRomPath() {
        return CommandLineArgs.convertToAbsolutePathIfNeeded(this.romPath);
    }

    @Override
    public Optional<Integer> getInstructionsPerFrame() {
        return Optional.ofNullable(this.instructionsPerFrame);
    }

    @Override
    public Optional<ColorPalette> getColorPalette() {
        return Optional.ofNullable(colorPalette);
    }

    @Override
    public Optional<DisplayAngle> getDisplayAngle() {
        return Optional.ofNullable(this.displayAngle);
    }

    @Override
    public Optional<KeyboardLayout> getKeyboardLayout() {
        return Optional.ofNullable(this.keyboardLayout);
    }

    @Override
    public Optional<Chip8Variant> getChip8Variant() {
        return Optional.ofNullable(this.chip8Variant);
    }

    @Override
    public Optional<Boolean> doVFReset() {
        return Optional.ofNullable(this.doVFReset);
    }

    @Override
    public Optional<Boolean> doIncrementIndex() {
        return Optional.ofNullable(this.doIncrementIndex);
    }

    @Override
    public Optional<Boolean> doDisplayWait() {
        return Optional.ofNullable(this.doDisplayWait);
    }

    @Override
    public Optional<Boolean> doClipping() {
        return Optional.ofNullable(this.doClipping);
    }

    @Override
    public Optional<Boolean> doShiftVXInPlace() {
        return Optional.ofNullable(this.doShiftVXInPlace);
    }

    @Override
    public Optional<Boolean> doJumpWithVX() {
        return Optional.ofNullable(this.doJumpWithVX);
    }


}
