package io.github.arkosammy12.jchip.ui.disassembly;

import io.github.arkosammy12.jchip.disassembler.Disassembler;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Bus;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Objects;

public class DisassemblerTable extends JTable {

    private static final int ROW_HEIGHT = 15;

    private final Model model;

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
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        Color baseColor = getBackground();
        if (row % 2 != 0) {
            baseColor = baseColor.darker();
        }
        c.setBackground(baseColor);
        return c;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return this.getPreferredSize().getWidth() <= this.getParent().getWidth();
    }

    public void update(Emulator emulator) {
        this.model.update(emulator);
        if (this.model.disassembler != null && this.model.disassembler.isEnabled()) {
            this.model.disassembler.getCurrentAddressSupplier().ifPresent(supplier -> this.scrollToAddress(supplier.getAsInt()));
        }
    }

    public void clear() {
        this.model.clear();
    }

    void setDisassemblerEnabled(boolean enabled) {
        if (this.model.disassembler != null) {
            this.model.disassembler.setEnabled(enabled);
        }
    }

    public void scrollToAddress(int address) {
        if (this.model.disassembler == null) {
            return;
        }
        int ordinal = this.model.disassembler.getIndexForAddress(address);
        if (ordinal < 0) {
            return;
        }
        int targetY = ordinal * this.getRowHeight();
        ((JViewport) this.getParent()).setViewPosition(new Point(0, targetY));
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
                    case 0 -> String.format("%04X", row * 2);
                    case 1 -> "0000";
                    default -> "invalid";
                };
            }
            Disassembler.Entry entry = this.disassembler.getEntry(row);
            if (entry == null) {
                return switch (col) {
                    case 0 -> String.format("%04X", row * 2);
                    case 1 -> "0000";
                    default -> "invalid";
                };
            }
            return switch (col) {
                case 0 -> String.format("%06X", entry.getInstructionAddress());
                case 1 -> String.format("%0" + (entry.getLength() * 2) + "X", entry.getByteCode());
                default -> entry.getText();
            };
        }

        public void update(Emulator emulator) {
            Disassembler disassembler = emulator.getDisassembler();
            if (!Objects.equals(disassembler, this.disassembler)) {
                this.disassembler = disassembler;
                this.rowCount = disassembler.getSize();
            }
            if (this.disassembler != null && this.disassembler.isEnabled()) {
                this.fireTableDataChanged();
            }
        }

        public void clear() {
            this.disassembler = null;
            this.fireTableDataChanged();
        }

    }

}
