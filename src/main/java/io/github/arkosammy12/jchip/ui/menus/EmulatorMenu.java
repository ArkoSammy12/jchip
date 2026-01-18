package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.DataManager;
import io.github.arkosammy12.jchip.config.Serializable;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializer;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializerConsumer;
import io.github.arkosammy12.jchip.ui.MainWindow;
import io.github.arkosammy12.jchip.ui.util.EnumMenu;
import io.github.arkosammy12.jchip.ui.util.NumberOnlyTextField;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class EmulatorMenu extends JMenu implements EmulatorInitializerConsumer {

    private final MainWindow mainWindow;

    private final JMenuItem resetButton = new JMenuItem("Reset");
    private final JRadioButtonMenuItem pauseButton = new JRadioButtonMenuItem("Pause");
    private final JMenuItem stopButton = new JMenuItem("Stop");
    private final JMenuItem stepFrameButton = new JMenuItem("Step Frame");
    private final JMenuItem stepCycleButton = new JMenuItem("Step Cycle");

    private final QuirksMenu quirksMenu;
    private final EnumMenu<Variant> variantMenu;
    private final EnumMenu<BuiltInColorPalette> colorPaletteMenu;
    private final EnumMenu<DisplayAngle> displayAngleMenu;

    private final JTextField instructionsPerFrameField;

    private volatile Integer instructionsPerFrame;

    public EmulatorMenu(Jchip jchip, MainWindow mainWindow) {
        super("Emulator");
        this.mainWindow = mainWindow;

        this.setMnemonic(KeyEvent.VK_E);

        this.resetButton.addActionListener(_ -> jchip.reset(this.pauseButton.isSelected()));
        this.resetButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, true));
        this.resetButton.setEnabled(true);

        this.pauseButton.addActionListener(_ -> jchip.setPaused(this.pauseButton.isSelected()));
        this.pauseButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, true));
        this.pauseButton.setEnabled(true);
        this.pauseButton.setSelected(false);

        this.stopButton.addActionListener(_ -> {
            jchip.setPaused(false);
            jchip.stop();
        });
        this.stopButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, true));
        this.stopButton.setEnabled(false);

        this.stepFrameButton.addActionListener(_ -> {
            if (!this.pauseButton.isSelected()) {
                return;
            }
            jchip.stepFrame();
        });
        this.stepFrameButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, true));
        this.stepFrameButton.setEnabled(false);

        this.stepCycleButton.addActionListener(_ -> {
            if (!this.pauseButton.isSelected()) {
                return;
            }
            jchip.stepCycle();
        });
        this.stepCycleButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, true));
        this.stepCycleButton.setEnabled(false);

        this.quirksMenu = new QuirksMenu(jchip);
        this.variantMenu = new EnumMenu<>("Variant", Variant.class, true);
        this.variantMenu.setMnemonic(KeyEvent.VK_V);

        this.colorPaletteMenu = new EnumMenu<>("Color Palette", BuiltInColorPalette.class, true);
        this.colorPaletteMenu.setMnemonic(KeyEvent.VK_C);

        this.displayAngleMenu = new EnumMenu<>("Display Angle", DisplayAngle.class, true);
        this.displayAngleMenu.setMnemonic(KeyEvent.VK_D);

        JMenuItem instructionsPerFrameMenu = new JMenu("Instructions per frame");
        instructionsPerFrameMenu.setMnemonic(KeyEvent.VK_I);

        JLabel label = new JLabel("IPF: ");
        this.instructionsPerFrameField = new NumberOnlyTextField();
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
                        mainWindow,
                        "The IPF value must be a valid positive integer!",
                        "Incorrect formatting",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        });

        instructionsPerFrameField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String text = instructionsPerFrameField.getText().trim();
                if (text.isEmpty()) {
                    instructionsPerFrame = null;
                    return;
                }
                try {
                    int ipf = Integer.parseInt(text);
                    if (ipf > 0) {
                        instructionsPerFrame = ipf;
                    }
                } catch (NumberFormatException _) {}
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

        });

        instructionsPerFrameField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(instructionsPerFrameField::selectAll);
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

        instructionsPerFrameMenu.add(ipfPanel);
        this.add(instructionsPerFrameMenu);

        this.mainWindow.setTitleSection(2, "Stopped");

        jchip.addStateChangedListener(((_, _, newState) -> {
            SwingUtilities.invokeLater(() -> {
                switch (newState) {
                    case RUNNING -> { // Loaded rom state
                        this.stopButton.setEnabled(true);
                        this.stepFrameButton.setEnabled(false);
                        this.stepCycleButton.setEnabled(false);
                        mainWindow.setTitleSection(2, "Running");
                    }
                    case PAUSED -> { // Loaded rom state
                        this.stopButton.setEnabled(true);
                        this.stepFrameButton.setEnabled(true);
                        this.stepCycleButton.setEnabled(true);
                        mainWindow.setTitleSection(2, "Paused");
                    }
                    case PAUSED_STOPPED -> { // No loaded rom state
                        this.stepFrameButton.setEnabled(false);
                        this.stepCycleButton.setEnabled(false);
                        mainWindow.setTitleSection(2, "Stopped (Paused)");
                    }
                    case STOPPED -> { // No loaded rom state
                        this.stopButton.setEnabled(false);
                        this.pauseButton.setSelected(false);
                        this.stepFrameButton.setEnabled(false);
                        this.stepCycleButton.setEnabled(false);
                        mainWindow.setTitleSection(2, "Stopped");
                    }
                }
            });
        }));

        jchip.addShutdownListener(() -> {
            DataManager dataManager = jchip.getDataManager();

            dataManager.putPersistent(DataManager.VARIANT, Serializable.serialize(this.getVariant().orElse(null)));
            this.getColorPalette().ifPresent(palette -> {
                if (palette instanceof BuiltInColorPalette builtInColorPalette) {
                    dataManager.putPersistent(DataManager.COLOR_PALETTE, Serializable.serialize(builtInColorPalette));
                }
            });
            dataManager.putPersistent(DataManager.DISPLAY_ANGLE, Serializable.serialize(this.getDisplayAngle().orElse(null)));
            dataManager.putPersistent(DataManager.INSTRUCTIONS_PER_FRAME, String.valueOf(this.getInstructionsPerFrame().orElse(-1)));
        });
    }

    public QuirksMenu getQuirksMenu() {
        return this.quirksMenu;
    }

    public Optional<Variant> getVariant() {
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

    public void onBreakpoint() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (!this.pauseButton.isSelected()) {
                        this.pauseButton.doClick();
                    }
                });
            } catch (Exception e) {
                this.mainWindow.showExceptionDialog(e);
            }
        }
    }

    @Override
    public void accept(EmulatorInitializer initializer) {
        this.quirksMenu.accept(initializer);

        initializer.getVariant().ifPresent(this.variantMenu::setState);
        initializer.getColorPalette().ifPresent(colorPalette -> {
            if (colorPalette instanceof BuiltInColorPalette builtInColorPalette) {
                this.colorPaletteMenu.setState(builtInColorPalette);
            }
        });
        initializer.getDisplayAngle().ifPresent(this.displayAngleMenu::setState);
        initializer.getInstructionsPerFrame().ifPresent(val -> {
            this.instructionsPerFrame = val;
            this.instructionsPerFrameField.setText(String.valueOf(val));
        });
    }

}
