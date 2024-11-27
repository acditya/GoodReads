import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class MemberTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Object authorizedValue = table.getValueAt(row, -2);
        Object deletedValue = table.getValueAt(row, -1);
        boolean isAuthorized = authorizedValue != null && (int) authorizedValue == 1 && deletedValue != null && (int) deletedValue == 0;

        if (!isAuthorized) {
            cell.setBackground(Color.RED);
            cell.setForeground(Color.WHITE);
        } else {
            cell.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cell.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        }

        return cell;
    }
}