package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.util.DebuggerLabelTable;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebugPanel extends JPanel {

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

    public DebugPanel(Jchip jchip) {
        super();
        this.jchip = jchip;
        this.setFocusable(false);

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

        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        this.setLayout(migLayout);
        this.setPreferredSize(new Dimension(500, this.getHeight()));

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
        this.textScrollPane.setPreferredSize(new Dimension(this.textScrollPane.getWidth(), 120));

        this.cpuRegistersScrollPane = new JScrollPane(this.cpuRegistersTable);
        this.cpuRegistersScrollPane.setPreferredSize(new Dimension(this.cpuRegistersScrollPane.getWidth(), 105));

        this.generalPurposeRegistersScrollPane = new JScrollPane(this.generalPurposeRegistersTable);
        this.generalPurposeRegistersScrollPane.setPreferredSize(new Dimension(this.generalPurposeRegistersScrollPane.getWidth(), 230));

        this.stackScrollPane = new JScrollPane(this.stackTable);
        this.stackScrollPane.setPreferredSize(new Dimension(this.stackScrollPane.getWidth(), 210));

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Memory",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, memoryScrollPane.getFont().deriveFont(Font.BOLD)));

        this.setDefaultBorders();

        JSplitPane firstSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.textScrollPane, this.cpuRegistersScrollPane);
        firstSplit.setDividerSize(3);
        firstSplit.setResizeWeight(0.5);
        firstSplit.setContinuousLayout(true);

        JSplitPane secondSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, firstSplit, this.generalPurposeRegistersScrollPane);
        secondSplit.setDividerSize(3);
        secondSplit.setResizeWeight(0.5);
        secondSplit.setContinuousLayout(true);

        JSplitPane thirdSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, secondSplit, this.stackScrollPane);
        thirdSplit.setDividerSize(3);
        thirdSplit.setResizeWeight(0.5);
        thirdSplit.setContinuousLayout(true);

        MigLayout leftPanelLayout = new MigLayout(new LC().insets("0"));
        JPanel leftPanel = new JPanel(leftPanelLayout);
        leftPanel.add(thirdSplit, new CC().grow().push());
        leftPanel.setPreferredSize(new Dimension(150, leftPanel.getHeight()));

        MigLayout rightPanelLayout = new MigLayout(new LC().insets("0"));
        JPanel rightPanel = new JPanel(rightPanelLayout);
        rightPanel.add(memoryScrollPane, new CC().grow().push());
        rightPanel.setPreferredSize(new Dimension(200, rightPanel.getHeight()));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplit.setDividerSize(5);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);

        this.add(mainSplit, new CC().grow().push().width("500"));

    }

    public void onStopped() {
        SwingUtilities.invokeLater(() -> {
            this.clear();
            this.textArea.setText("");
            this.setDefaultBorders();
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
                label.update();
                textArea.append(label.getText() + (i == this.textPanelLabels.size() - 1 ? "" : "\n"));
            }

            this.cpuRegisterLabels.forEach(DebuggerLabel::update);
            this.generalPurposeRegisterLabels.forEach(DebuggerLabel::update);
            this.stackLabels.forEach(DebuggerLabel::update);

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
        this.clear();
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

    private void clear() {
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

    private void setDefaultBorders() {
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
    }

}