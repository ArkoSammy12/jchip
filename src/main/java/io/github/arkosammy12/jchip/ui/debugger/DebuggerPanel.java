package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.util.DebuggerLabelTablePanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebuggerPanel extends JPanel {

    public static final String DEFAULT_TEXT_SECTION_NAME = "Current Quirks";
    public static final String DEFAULT_SINGLE_REGISTERS_SECTION_NAME = "Single Registers";
    public static final String DEFAULT_REGISTERS_SECTION_NAME = "Registers";
    public static final String DEFAULT_STACK_SECTION_NAME = "Stack";

    private final Jchip jchip;

    private final JTextArea textArea;

    private final JPanel singleRegistersPanel;
    private final JPanel registersPanel;
    private final JPanel stackPanel;

    private final JScrollPane textScrollPane;
    private final JScrollPane singleRegistersScrollPane;
    private final JScrollPane registersScrollPane;
    private final JScrollPane stackScrollPane;

    private final DebuggerLabelTablePanel singleRegistersTable;
    private final DebuggerLabelTablePanel registersTable;
    private final DebuggerLabelTablePanel stackTable;

    private Debugger debugger;

    private final List<DebuggerLabel<?>> textPanelLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> singleRegisterLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> registerLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> stackLabels = new ArrayList<>();

    private final MemoryTable memoryTable;

    public DebuggerPanel(Jchip jchip) {
        super();
        this.jchip = jchip;

        this.textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setKeymap(null);
        textArea.setOpaque(false);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret.setVisible(false);

        this.singleRegistersPanel = new JPanel();
        this.registersPanel = new JPanel();
        this.stackPanel = new JPanel();

        this.singleRegistersTable = new DebuggerLabelTablePanel(this.singleRegisterLabels, 2, 3);
        this.registersTable = new DebuggerLabelTablePanel(this.registerLabels, 2, true, 8);
        this.stackTable = new DebuggerLabelTablePanel(this.stackLabels, 2, true, 8);

        this.memoryTable = new MemoryTable();

        this.textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(textScrollPane.getSize().width, 20));
        textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_TEXT_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.textScrollPane.getFont().deriveFont(Font.BOLD)));

        this.singleRegistersScrollPane = new JScrollPane(singleRegistersPanel);
        singleRegistersScrollPane.setPreferredSize(new Dimension(singleRegistersScrollPane.getSize().width, 10));
        singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_SINGLE_REGISTERS_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.singleRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.registersScrollPane = new JScrollPane(registersPanel);
        registersScrollPane.setPreferredSize(new Dimension(registersScrollPane.getSize().width, 130));
        registersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_REGISTERS_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.registersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.stackScrollPane = new JScrollPane(stackPanel);
        stackScrollPane.setPreferredSize(new Dimension(stackScrollPane.getSize().width, 130));
        stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_STACK_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.stackScrollPane.getFont().deriveFont(Font.BOLD)));

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setPreferredSize(new Dimension(280, memoryScrollPane.getSize().height));
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Memory",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, memoryScrollPane.getFont().deriveFont(Font.BOLD)));

        singleRegistersScrollPane.setViewportView(this.singleRegistersTable);
        registersScrollPane.setViewportView(this.registersTable);
        stackScrollPane.setViewportView(this.stackTable);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(150, leftPanel.getSize().height));
        leftPanel.add(textScrollPane);
        leftPanel.add(singleRegistersScrollPane);
        leftPanel.add(registersScrollPane);
        leftPanel.add(stackScrollPane);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(215, rightPanel.getSize().height));
        rightPanel.add(memoryScrollPane);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setDividerSize(5);

        this.setPreferredSize(new Dimension(500, this.getSize().height));
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Debugger",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.getFont().deriveFont(Font.BOLD)));

        this.add(mainSplit, BorderLayout.CENTER);
    }


    public void onStopped() {
        SwingUtilities.invokeLater(() -> {
            this.clearState();
            this.textArea.setText("");

            this.textScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_TEXT_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.textScrollPane.getFont().deriveFont(Font.BOLD)));

            this.singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_SINGLE_REGISTERS_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.singleRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

            this.registersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_REGISTERS_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.registersScrollPane.getFont().deriveFont(Font.BOLD)));

            this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_STACK_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.stackScrollPane.getFont().deriveFont(Font.BOLD)));

            this.revalidate();
            this.repaint();
        });
    }

    public void onFrame(Emulator emulator) {
        Debugger debugger = emulator.getDebugger();
        SwingUtilities.invokeLater(() -> {
            if (!Objects.equals(debugger, this.debugger)) {
                this.initializeDebuggerPanel(debugger);
            }

            textArea.setText("");
            for (int i = 0; i < this.textPanelLabels.size(); i++) {
                DebuggerLabel<?> label = this.textPanelLabels.get(i);
                label.updateState();
                textArea.append(label.getText() + (i == this.textPanelLabels.size() - 1 ? "" : "\n"));
            }

            this.singleRegisterLabels.forEach(DebuggerLabel::updateState);
            this.registerLabels.forEach(DebuggerLabel::updateState);
            this.stackLabels.forEach(DebuggerLabel::updateState);

            this.singleRegistersTable.update();
            this.registersTable.update();
            this.stackTable.update();

            this.memoryTable.update(emulator);
            this.debugger.getScrollAddressSupplier().ifPresent(supplier -> {
                if (this.jchip.getMainWindow().getSettingsBar().getDebuggerSettingsMenu().isMemoryFollowEnabled()) {
                    this.memoryTable.scrollToAddress(supplier.get());
                }
            });
        });
    }

    private void initializeDebuggerPanel(Debugger debugger) {
        this.clearState();
        this.debugger = debugger;

        this.textPanelLabels.addAll(this.debugger.getTextSectionLabels());
        this.singleRegisterLabels.addAll(this.debugger.getSingleRegisterLabels());
        this.registerLabels.addAll(this.debugger.getRegisterLabels());
        this.stackLabels.addAll(this.debugger.getStackLabels());

        this.textPanelLabels.forEach(label -> label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(15f)));
        this.singleRegisterLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));
        this.registerLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));
        this.stackLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));

        this.textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getTextSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.textScrollPane.getFont().deriveFont(Font.BOLD)));

        this.singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getSingleRegisterSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.singleRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.registersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getRegisterSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.registersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getStackSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.stackScrollPane.getFont().deriveFont(Font.BOLD)));


        /*
        for (DebuggerLabel<?> label : this.singleRegisterLabels) {
            this.singleRegistersPanel.add(label);
        }

        int registerLabelRows = (int) Math.ceil((double) this.registerLabels.size() / 2);
        for (int row = 0; row < registerLabelRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * registerLabelRows + row;
                if (index >= this.registerLabels.size()) {
                    continue;
                }
                DebuggerLabel<?> registerLabel = this.registerLabels.get(index);
                registersPanel.add(registerLabel);
            }
        }

        int stackLabelsRows = (int) Math.ceil((double) this.registerLabels.size() / 2);
        for (int row = 0; row < stackLabelsRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * stackLabelsRows + row;
                if (index >= this.stackLabels.size()) {
                    continue;
                }
                DebuggerLabel<?> stackLabel = this.stackLabels.get(index);
                stackPanel.add(stackLabel);
            }
        }

         */


        this.revalidate();
        this.repaint();
    }

    private void clearState() {
        this.debugger = null;

        this.textPanelLabels.clear();

        this.singleRegisterLabels.clear();
        this.registerLabels.clear();
        this.stackLabels.clear();

        this.singleRegistersTable.update();
        this.registersTable.update();
        this.stackTable.update();

        this.memoryTable.clear();
    }

}