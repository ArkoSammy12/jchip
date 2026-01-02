package io.github.arkosammy12.jchip.ui.disassembly;

import io.github.arkosammy12.jchip.disassembler.Disassembler;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.MainWindow;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.function.IntSupplier;

public class DisassemblerTable extends JTable {

    private static final int ADDRESS_COLUMN_WIDTH = 115;
    private static final int BYTECODE_COLUMN_WIDTH = 100;
    private static final int TEXT_COLUMN_WIDTH = 200;
    private static final int ROW_HEIGHT = 20;

    private final Model model;
    private int hoveredRow = -1;
    private int hoveredColumn = -1;

    public DisassemblerTable() {
        super();
        this.model = new Model();
        this.setModel(this.model);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        this.setRowHeight(ROW_HEIGHT);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setResizingAllowed(false);
        this.setFocusable(false);
        this.setRowSelectionAllowed(false);
        this.setColumnSelectionAllowed(false);
        this.setTableHeader(null);
        this.setupColumns();
        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row != hoveredRow || col != hoveredColumn) {
                    hoveredRow = row;
                    hoveredColumn = col;
                    repaint();
                }
            }

        });
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                hoveredColumn = -1;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row >= 0 && col == 0) {
                    if (model.containsBreakpoint(row)) {
                        model.removeBreakpoint(row);
                    } else {
                        model.addBreakpoint(row);
                    }
                    repaint();
                }
            }

        });

    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        Color base = getBackground();
        if (row % 2 != 0) {
            base = base.darker();
        }

        if (this.isCurrentInstructionRow(row)) {
            Color accent = UIManager.getColor("Table.selectionBackground");
            if (accent == null) {
                accent = new Color(255, 220, 120);
            }
            base = blend(base, accent, 0.35f);
        }

        c.setBackground(base);
        return c;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int circleDiameter = 10;
            int gutterX = 6;

            if (hoveredRow >= 0 && hoveredColumn == 0) {
                Rectangle rowRect = getCellRect(hoveredRow, 0, true);
                int cy = rowRect.y + (rowRect.height - circleDiameter) / 2;

                g2.setColor(new Color(255, 0, 0, 120));
                g2.fillOval(gutterX, cy, circleDiameter, circleDiameter);
            }

            if (this.model.disassembler != null) {
                g2.setColor(Color.RED);
                for (int breakpoint : this.model.disassembler.getCurrentBreakpoints()) {
                    int ordinal = this.model.disassembler.getOrdinalForAddress(breakpoint);
                    if (ordinal < 0 || ordinal >= this.getRowCount()) {
                        continue;
                    }
                    Rectangle rowRect = getCellRect(ordinal, 0, true);
                    int cy = rowRect.y + (rowRect.height - circleDiameter) / 2;
                    g2.fillOval(gutterX, cy, circleDiameter, circleDiameter);
                }
            }

            if (this.hoveredRow >= 0 && this.hoveredRow < this.getRowCount()) {
                Rectangle r = getCellRect(hoveredRow, 0, true);
                r.x = 0;
                r.width = getWidth();
                Color outline = UIManager.getColor("Table.selectionBackground");
                if (outline == null) {
                    outline = Color.GRAY;
                }
                g2.setColor(new Color(outline.getRed(), outline.getGreen(), outline.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3, 6, 6);
            }
        } finally {
            g2.dispose();
        }
    }

    private boolean isCurrentInstructionRow(int row) {
        if (this.model.disassembler == null) {
            return false;
        }
        Optional<IntSupplier> currentInstructionSupplier = this.model.disassembler.getCurrentAddressSupplier();
        if (currentInstructionSupplier.isEmpty()) {
            return false;
        }
        int ordinal = this.model.disassembler.getOrdinalForAddress(currentInstructionSupplier.get().getAsInt());
        return ordinal >= 0 && ordinal == row;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return this.getPreferredSize().getWidth() <= this.getParent().getWidth();
    }

    public void update(Emulator emulator) {
        this.model.update(emulator);
    }

    public void clear() {
        this.model.clear();
    }

    void setDisassemblerEnabled(boolean enabled) {
        if (this.model.disassembler != null) {
            this.model.disassembler.setEnabled(enabled);
        }
    }

    public boolean isAddressVisible(int address) {
        return this.model.disassembler != null && this.model.disassembler.getOrdinalForAddress(address) >= 0;
    }

    public void scrollToAddress(int address) {
        if (this.model.disassembler == null) {
            return;
        }
        if (!this.model.disassembler.isEnabled()) {
            return;
        }
        int ordinal = this.model.disassembler.getOrdinalForAddress(address);
        if (ordinal < 0) {
            return;
        }
        int targetY = ordinal * this.getRowHeight();
        ((JViewport) this.getParent()).setViewPosition(new Point(0, targetY));
    }

    public void scrollToCurrentAddress() {
        if (this.model.disassembler == null) {
            return;
        }
        if (!this.model.disassembler.isEnabled()) {
            return;
        }
        this.model.disassembler.getCurrentAddressSupplier().ifPresent(supplier -> this.scrollToAddress(supplier.getAsInt()));
    }

    public void clearBreakpoints() {
        this.model.clearBreakpoints();
    }

    private void setupColumns() {
        DefaultTableCellRenderer addressColumnRenderer = new DefaultTableCellRenderer();
        addressColumnRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        addressColumnRenderer.setVerticalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer bytecodeColumnRenderer = new DefaultTableCellRenderer();
        bytecodeColumnRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        bytecodeColumnRenderer.setVerticalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer textColumnRenderer = new DefaultTableCellRenderer();
        textColumnRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        textColumnRenderer.setVerticalAlignment(SwingConstants.CENTER);

        TableColumnModel columnModel = this.getColumnModel();

        TableColumn addressColumn = columnModel.getColumn(0);
        addressColumn.setMinWidth(ADDRESS_COLUMN_WIDTH);
        addressColumn.setPreferredWidth(ADDRESS_COLUMN_WIDTH);
        addressColumn.setCellRenderer(addressColumnRenderer);

        TableColumn bytecodeColumn = columnModel.getColumn(1);
        bytecodeColumn.setMinWidth(BYTECODE_COLUMN_WIDTH);
        bytecodeColumn.setPreferredWidth(BYTECODE_COLUMN_WIDTH);
        bytecodeColumn.setCellRenderer(bytecodeColumnRenderer);

        TableColumn textColumn = columnModel.getColumn(2);
        textColumn.setMinWidth(TEXT_COLUMN_WIDTH);
        textColumn.setPreferredWidth(TEXT_COLUMN_WIDTH);
        textColumn.setCellRenderer(textColumnRenderer);
    }

    private static Color blend(Color base, Color overlay, float alpha) {
        float inv = 1.0f - alpha;
        return new Color(
                (int) (base.getRed() * inv + overlay.getRed() * alpha),
                (int) (base.getGreen() * inv + overlay.getGreen() * alpha),
                (int) (base.getBlue() * inv + overlay.getBlue() * alpha)
        );
    }

    private class Model extends DefaultTableModel {

        private Disassembler disassembler;
        private int rowCount = 0;

        @Override
        public int getRowCount() {
            int viewportHeight = 0;
            if (getParent() instanceof JViewport viewport) {
                viewportHeight = viewport.getHeight();
            }
            int rowsToFill = viewportHeight > 0 ? viewportHeight / ROW_HEIGHT : 0;
            return Math.max(this.rowCount, rowsToFill);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (this.disassembler == null) {
                return switch (col) {
                    case 0 -> String.format("%06X: ", row * 2);
                    case 1 -> "0000";
                    default -> "invalid";
                };
            }
            Disassembler.Entry entry = this.disassembler.getEntry(row);
            if (entry == null) {
                return switch (col) {
                    case 0 -> String.format("%06X: ", row * 2);
                    case 1 -> "0000";
                    default -> "invalid";
                };
            }
            return switch (col) {
                case 0 -> String.format("%06X: ", entry.getAddress());
                case 1 -> String.format("%0" + (entry.getLength() * 2) + "X", entry.getByteCode());
                default -> entry.getText();
            };
        }

        private void update(Emulator emulator) {
            Disassembler disassembler = emulator.getDisassembler();
            if (!Objects.equals(disassembler, this.disassembler)) {
                this.disassembler = disassembler;
                this.clearBreakpoints();
            }
            if (this.disassembler != null && this.disassembler.isEnabled()) {
                int size = disassembler.getSize();
                if (size != this.rowCount) {
                    this.rowCount = disassembler.getSize();
                    this.fireTableDataChanged();
                } else {
                    MainWindow.fireVisibleRowsUpdated(DisassemblerTable.this);
                }
            }
        }

        private void clear() {
            this.clearBreakpoints();
            this.disassembler = null;
            this.rowCount = 0;
            this.fireTableDataChanged();
        }

        private void addBreakpoint(int row) {
            if (this.disassembler == null) {
                return;
            }
            Disassembler.Entry entry = this.disassembler.getEntry(row);
            if (entry == null) {
                return;
            }
            this.disassembler.addBreakpoint(entry.getAddress());
        }

        private void removeBreakpoint(int row) {
            if (this.disassembler == null) {
                return;
            }
            Disassembler.Entry entry = this.disassembler.getEntry(row);
            if (entry == null) {
                return;
            }
            this.disassembler.removeBreakpoint(entry.getAddress());
        }

        private boolean containsBreakpoint(int row) {
            if (this.disassembler == null) {
                return false;
            }
            Disassembler.Entry entry = this.disassembler.getEntry(row);
            if (entry == null) {
                return false;
            }
            return this.disassembler.getCurrentBreakpoints().contains(entry.getAddress());
        }

        private void clearBreakpoints() {
            if (this.disassembler != null) {
                this.disassembler.clearBreakpoints();
            }
        }

    }

}
