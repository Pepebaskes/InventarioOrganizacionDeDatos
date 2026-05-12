package invetario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// Ventana para consultar historial por producto.
public class HistorialProductoView extends javax.swing.JDialog {

    private final ProductoCSVService productoService;
    private final MovimientoInventarioCSVService movimientoService;

    private JTextField txtBuscarProducto;
    private JTable tablaProductos;
    private JTable tablaHistorial;
    private DefaultTableModel modeloProductos;
    private DefaultTableModel modeloHistorial;
    private TableRowSorter<DefaultTableModel> sorterProductos;

    public HistorialProductoView(Frame owner) {
        super(owner, "Historial por producto", true);
        productoService = new ProductoCSVService();
        movimientoService = new MovimientoInventarioCSVService();
        construirVentana();
        cargarProductos();
    }

    private void construirVentana() {
        setSize(1120, 620);
        setMinimumSize(new Dimension(980, 560));
        setLocationRelativeTo(getOwner());
        getContentPane().setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel panelSuperior = new JPanel(new BorderLayout(8, 8));
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 12, 12, 12)));

        JLabel lblTitulo = new JLabel("Historial de movimientos por producto");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        JPanel panelBuscar = new JPanel(new BorderLayout(6, 0));
        panelBuscar.setOpaque(false);
        panelBuscar.add(new JLabel("Buscar producto:"), BorderLayout.WEST);
        txtBuscarProducto = new JTextField();
        panelBuscar.add(txtBuscarProducto, BorderLayout.CENTER);
        panelSuperior.add(panelBuscar, BorderLayout.SOUTH);

        modeloProductos = new DefaultTableModel(new Object[]{"Codigo", "Descripcion", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setRowHeight(26);
        sorterProductos = new TableRowSorter<DefaultTableModel>(modeloProductos);
        tablaProductos.setRowSorter(sorterProductos);

        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.setBorder(BorderFactory.createTitledBorder("Catalogo de productos"));
        scrollProductos.setPreferredSize(new Dimension(340, 0));

        modeloHistorial = new DefaultTableModel(new Object[]{"No Movimiento", "Fecha", "Tipo", "Motivo", "Cantidad", "Precio Unitario", "Importe"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setRowHeight(26);
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaHistorial.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaHistorial.getColumnModel().getColumn(1).setCellRenderer(renderCentro);
        tablaHistorial.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
        tablaHistorial.getColumnModel().getColumn(4).setCellRenderer(renderCentro);

        DefaultTableCellRenderer renderDecimal = new DefaultTableCellRenderer() {
            private final DecimalFormat formato = new DecimalFormat("#,##0.00");

            @Override
            protected void setValue(Object value) {
                try {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(formato.format(Double.parseDouble(value.toString())));
                } catch (Exception ex) {
                    setText(value == null ? "" : value.toString());
                }
            }
        };
        tablaHistorial.getColumnModel().getColumn(5).setCellRenderer(renderDecimal);
        tablaHistorial.getColumnModel().getColumn(6).setCellRenderer(renderDecimal);

        JScrollPane scrollHistorial = new JScrollPane(tablaHistorial);
        scrollHistorial.setBorder(BorderFactory.createTitledBorder("Historial"));

        JPanel panelCentral = new JPanel(new BorderLayout(12, 0));
        panelCentral.setOpaque(false);
        panelCentral.add(scrollProductos, BorderLayout.WEST);
        panelCentral.add(scrollHistorial, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);

        txtBuscarProducto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltroProductos();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltroProductos();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltroProductos();
            }
        });

        // Seleccion simple en tabla de productos: carga historial automaticamente.
        tablaProductos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    cargarHistorialSeleccionado();
                }
            }
        });

        // Enter en tabla: fuerza carga de historial sin click extra.
        tablaProductos.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "cargarHistorialEnter");
        tablaProductos.getActionMap().put("cargarHistorialEnter", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cargarHistorialSeleccionado();
            }
        });
    }

    // Carga productos en tabla lateral.
    private void cargarProductos() {
        modeloProductos.setRowCount(0);
        List<Producto> productos = productoService.cargarProductos();
        for (Producto productoActual : productos) {
            String estado = productoActual.isActivo() ? "Activo" : "Inactivo";
            modeloProductos.addRow(new Object[]{productoActual.getClave(), productoActual.getNombre(), estado});
        }
    }

    // Filtra tabla lateral por codigo o descripcion.
    private void aplicarFiltroProductos() {
        String texto = txtBuscarProducto.getText().trim();
        if (texto.isEmpty()) {
            sorterProductos.setRowFilter(null);
            modeloHistorial.setRowCount(0);
            return;
        }
        sorterProductos.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 0, 1));

        // Si no hay filas visibles, limpiamos historial.
        if (tablaProductos.getRowCount() == 0) {
            modeloHistorial.setRowCount(0);
        }
    }

    // Carga el historial del producto seleccionado.
    private void cargarHistorialSeleccionado() {
        int filaVista = tablaProductos.getSelectedRow();
        if (filaVista == -1) {
            // Si no hay seleccion solo limpiamos historial sin mostrar alertas.
            modeloHistorial.setRowCount(0);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(filaVista);
        String codigoProducto = modeloProductos.getValueAt(filaModelo, 0).toString();

        List<HistorialMovimientoProductoItem> historial = movimientoService.obtenerHistorialPorProducto(codigoProducto);
        modeloHistorial.setRowCount(0);

        for (HistorialMovimientoProductoItem item : historial) {
            modeloHistorial.addRow(new Object[]{
                item.getNoMovimiento(),
                item.getFecha(),
                item.getTipoMovimiento(),
                item.getMotivo(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getImporte()
            });
        }
    }
}
