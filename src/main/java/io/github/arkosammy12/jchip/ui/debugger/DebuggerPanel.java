package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.util.DebuggerLabelTable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebuggerPanel extends JPanel {

    public static final String DEFAULT_TEXT_SECTION_NAME = "Current Quirks";
    public static final String DEFAULT_CPU_REGISTERS_SECTION_NAME = "CPU Registers";
    public static final String DEFAULT_GENERAL_PURPOSE_REGISTERS_SECTION_NAME = "General Purpose Registers";
    public static final String DEFAULT_STACK_SECTION_NAME = "Stack";

    private final Jchip jchip;
    private Debugger debugger;

    private final List<DebuggerLabel<?>> textPanelLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> cpuRegisterLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> generalPurposeRegisterLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> stackLabels = new ArrayList<>();

    private final JTextArea textArea;

    private final JScrollPane textScrollPane;
    private final JScrollPane cpuRegistersScrollPane;
    private final JScrollPane generalPurposeRegistersScrollPane;
    private final JScrollPane stackScrollPane;

    private final DebuggerLabelTable cpuRegistersTable;
    private final DebuggerLabelTable generalPurposeRegistersTable;
    private final DebuggerLabelTable stackTable;
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

        this.setPreferredSize(new Dimension(500, this.getSize().height));
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Debugger",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.getFont().deriveFont(Font.BOLD)));

        this.cpuRegistersTable = new DebuggerLabelTable(this.cpuRegisterLabels, 2);
        this.generalPurposeRegistersTable = new DebuggerLabelTable(this.generalPurposeRegisterLabels, 2, true);
        this.stackTable = new DebuggerLabelTable(this.stackLabels, 2, true);
        this.memoryTable = new MemoryTable();

        this.textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(textScrollPane.getSize().width, 20));
        textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_TEXT_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.textScrollPane.getFont().deriveFont(Font.BOLD)));

        this.cpuRegistersScrollPane = new JScrollPane(this.cpuRegistersTable);
        cpuRegistersScrollPane.setPreferredSize(new Dimension(cpuRegistersScrollPane.getSize().width, 10));
        cpuRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_CPU_REGISTERS_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.cpuRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.generalPurposeRegistersScrollPane = new JScrollPane(this.generalPurposeRegistersTable);
        generalPurposeRegistersScrollPane.setPreferredSize(new Dimension(generalPurposeRegistersScrollPane.getSize().width, 130));
        generalPurposeRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_GENERAL_PURPOSE_REGISTERS_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.generalPurposeRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.stackScrollPane = new JScrollPane(this.stackTable);
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

        JSplitPane firstSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScrollPane, cpuRegistersScrollPane);
        firstSplit.setDividerSize(3);
        firstSplit.setContinuousLayout(true);

        JSplitPane secondSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, firstSplit, generalPurposeRegistersScrollPane);
        secondSplit.setResizeWeight(0);
        secondSplit.setDividerSize(3);
        secondSplit.setContinuousLayout(true);

        JSplitPane thirdSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, secondSplit, stackScrollPane);
        thirdSplit.setDividerSize(3);
        thirdSplit.setContinuousLayout(true);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(150, leftPanel.getSize().height));
        leftPanel.add(thirdSplit);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(215, rightPanel.getSize().height));
        rightPanel.add(memoryScrollPane);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplit.setResizeWeight(1);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setDividerSize(5);
        mainSplit.setContinuousLayout(true);

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

            this.cpuRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_CPU_REGISTERS_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.cpuRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

            this.generalPurposeRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_GENERAL_PURPOSE_REGISTERS_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.generalPurposeRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

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

            this.cpuRegisterLabels.forEach(DebuggerLabel::updateState);
            this.generalPurposeRegisterLabels.forEach(DebuggerLabel::updateState);
            this.stackLabels.forEach(DebuggerLabel::updateState);

            this.cpuRegistersTable.update();
            this.generalPurposeRegistersTable.update();
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
        this.cpuRegisterLabels.addAll(this.debugger.getCpuRegisterLabels());
        this.generalPurposeRegisterLabels.addAll(this.debugger.getGeneralPurposeRegisterLabels());
        this.stackLabels.addAll(this.debugger.getStackLabels());

        this.textPanelLabels.forEach(label -> label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(15f)));
        this.cpuRegisterLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));
        this.generalPurposeRegisterLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));
        this.stackLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));

        this.textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getTextSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.textScrollPane.getFont().deriveFont(Font.BOLD)));

        this.cpuRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getCpuRegistersSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.cpuRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.generalPurposeRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getGeneralPurposeRegistersSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.generalPurposeRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getStackSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.stackScrollPane.getFont().deriveFont(Font.BOLD)));

        this.revalidate();
        this.repaint();
    }

    private void clearState() {
        this.debugger = null;

        this.textPanelLabels.clear();

        this.cpuRegisterLabels.clear();
        this.generalPurposeRegisterLabels.clear();
        this.stackLabels.clear();

        this.cpuRegistersTable.update();
        this.generalPurposeRegistersTable.update();
        this.stackTable.update();

        this.memoryTable.clear();
    }

}