package invetario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Ventana para ver stock actual de productos.
public class StockActualView extends JDialog {

    private final ProductoCSVService productoService;
    private final DefaultTableModel modeloTablaStock;
    private JTable tablaStock;

    public StockActualView(Frame owner) {
        super(owner, "Stock actual de productos", true);
        productoService = new ProductoCSVService();
        modeloTablaStock = new DefaultTableModel(new Object[]{"Codigo", "Descripcion", "Stock actual", "Stock minimo", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        construirVentana();
        cargarStockActual();
    }

    private void construirVentana() {
        setSize(900, 520);
        setMinimumSize(new Dimension(800, 460));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(Color.WHITE);
        panelTitulo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(14, 16, 14, 16)));
        JLabel lblTitulo = new JLabel("Stock actual de productos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panelTitulo.add(lblTitulo, BorderLayout.WEST);

        tablaStock = new JTable(modeloTablaStock);
        tablaStock.setRowHeight(28);
        tablaStock.setFillsViewportHeight(true);
        tablaStock.setDefaultRenderer(Object.class, new RenderEstadoStock());

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaStock.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
        tablaStock.getColumnModel().getColumn(3).setCellRenderer(renderCentro);
        tablaStock.getColumnModel().getColumn(4).setCellRenderer(renderCentro);

        JScrollPane scroll = new JScrollPane(tablaStock);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        add(panelTitulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // Carga los productos y su estado.
    private void cargarStockActual() {
        modeloTablaStock.setRowCount(0);
        List<Producto> productos = productoService.cargarProductos();

        for (Producto productoActual : productos) {
            String estado = productoService.calcularEstadoStock(productoActual);
            modeloTablaStock.addRow(new Object[]{
                productoActual.getClave(),
                productoActual.getNombre(),
                productoActual.getStockActual(),
                productoActual.getStockMinimo(),
                estado
            });
        }
    }

    // Renderer para colorear por estado.
    private class RenderEstadoStock extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
                return c;
            }

            String estado = table.getValueAt(row, 4).toString();
            if ("AGOTADO".equals(estado)) {
                c.setBackground(new Color(248, 215, 218));
            } else if ("STOCK BAJO".equals(estado)) {
                c.setBackground(new Color(255, 243, 205));
            } else if ("SOBREINVENTARIO".equals(estado)) {
                c.setBackground(new Color(209, 236, 241));
            } else {
                c.setBackground(Color.WHITE);
            }

            c.setForeground(new Color(33, 37, 41));
            return c;
        }
    }
}
