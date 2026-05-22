package invetario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// Panel del modulo 3.4 Analisis de Inventario (solo reporte).
public class AnalisisInventarioPanel extends JPanel {

    private final AnalisisInventarioService analisisService;
    private final DecimalFormat formatoMonto;
    private final DecimalFormat formatoPorcentaje;

    private JTextField txtFiltro;
    private JLabel lblConsumoTotal;
    private JLabel lblEstadoAnalisis;

    private JTable tablaAnalisis;
    private JTable tablaDetalleConsumo;
    private DefaultTableModel modeloAnalisis;
    private DefaultTableModel modeloDetalle;
    private TableRowSorter<DefaultTableModel> sorterAnalisis;
    private JButton btnVerGraficaClasificada;
    private List<AnalisisInventarioItem> filasAnalisisActuales;
    private boolean alertaSinSalidasMostrada;

    public AnalisisInventarioPanel() {
        analisisService = new AnalisisInventarioService();
        formatoMonto = new DecimalFormat("#,##0.00");
        formatoPorcentaje = new DecimalFormat("0.00%");
        filasAnalisisActuales = new ArrayList<AnalisisInventarioItem>();
        alertaSinSalidasMostrada = false;
        construirPanel();
        refrescarDatos();
    }

    // Metodo publico para actualizar reporte al entrar en la vista.
    public final void refrescarDatos() {
        AnalisisInventarioResultado resultado = analisisService.generarReporteAnalisis();
        filasAnalisisActuales = new ArrayList<AnalisisInventarioItem>(resultado.getFilasAnalisis());
        cargarResumen(resultado);
        cargarTablaAnalisis(resultado.getFilasAnalisis());
        limpiarDetalle();

        // Si no hay salidas, avisamos que no se puede hacer analisis real.
        if (resultado.getConsumoTotalInventario() <= 0) {
            if (!alertaSinSalidasMostrada) {
                JOptionPane.showMessageDialog(
                        this,
                        "No hay datos de SALIDAS. No se puede generar el analisis de inventario.",
                        "Analisis no disponible",
                        JOptionPane.WARNING_MESSAGE);
                alertaSinSalidasMostrada = true;
            }
        } else {
            // Si ya hay salidas otra vez, reseteamos bandera para futuros casos.
            alertaSinSalidasMostrada = false;
        }
    }

    // Estructura principal del panel.
    private void construirPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);

        configurarFlujoTeclado();
    }

    // Panel superior con resumen y filtro.
    private JPanel crearPanelSuperior() {
        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.setBackground(Color.WHITE);
        superior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel panelResumen = new JPanel(new BorderLayout());
        panelResumen.setOpaque(false);
        lblConsumoTotal = new JLabel("<html><span style='color:#DC2626;'>Consumo total del inventario: </span><span style='color:#1D4ED8;'>0.00</span></html>");
        lblConsumoTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panelResumen.add(lblConsumoTotal, BorderLayout.NORTH);

        lblEstadoAnalisis = new JLabel("Esperando datos de salida para analizar.");
        lblEstadoAnalisis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstadoAnalisis.setForeground(new Color(71, 85, 105));
        panelResumen.add(lblEstadoAnalisis, BorderLayout.SOUTH);

        // Boton superior derecho para abrir grafica en ventana aparte.
        btnVerGraficaClasificada = new JButton("Grafica");
        btnVerGraficaClasificada.addActionListener(e -> abrirGraficaClasificada());

        JPanel panelLineaSuperior = new JPanel(new BorderLayout());
        panelLineaSuperior.setOpaque(false);
        panelLineaSuperior.add(panelResumen, BorderLayout.CENTER);
        panelLineaSuperior.add(btnVerGraficaClasificada, BorderLayout.EAST);

        JPanel panelFiltro = new JPanel(new BorderLayout(6, 0));
        panelFiltro.setOpaque(false);
        panelFiltro.add(new JLabel("Buscar por codigo, nombre o categoria:"), BorderLayout.WEST);
        txtFiltro = new JTextField();
        panelFiltro.add(txtFiltro, BorderLayout.CENTER);
        JButton btnLimpiarFiltro = new JButton("Limpiar filtro");
        btnLimpiarFiltro.addActionListener(e -> {
            txtFiltro.setText("");
            txtFiltro.requestFocusInWindow();
        });
        panelFiltro.add(btnLimpiarFiltro, BorderLayout.EAST);

        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltroTabla();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltroTabla();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltroTabla();
            }
        });

        superior.add(panelLineaSuperior, BorderLayout.NORTH);
        superior.add(panelFiltro, BorderLayout.SOUTH);
        return superior;
    }

    // Panel central con tabla principal y detalle de consumo.
    private JPanel crearPanelCentral() {
        JPanel central = new JPanel(new BorderLayout(0, 12));
        central.setOpaque(false);

        modeloAnalisis = new DefaultTableModel(new Object[]{
            "Grupo ABC", "Codigo", "Nombre", "Categoria", "Costo actual", "Stock actual",
            "Punto reorden", "EOQ", "Consumo producto", "% Individual", "% Acumulado", "Indicadores"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAnalisis = new JTable(modeloAnalisis);
        tablaAnalisis.setRowHeight(26);
        tablaAnalisis.setSelectionBackground(new Color(219, 234, 254));
        tablaAnalisis.setSelectionForeground(new Color(33, 37, 41));
        sorterAnalisis = new TableRowSorter<DefaultTableModel>(modeloAnalisis);
        tablaAnalisis.setRowSorter(sorterAnalisis);

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaAnalisis.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaAnalisis.getColumnModel().getColumn(5).setCellRenderer(renderCentro);
        tablaAnalisis.getColumnModel().getColumn(11).setCellRenderer(new RenderIndicadores());

        DefaultTableCellRenderer renderNumero = new DefaultTableCellRenderer();
        renderNumero.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaAnalisis.getColumnModel().getColumn(4).setCellRenderer(renderNumero);
        tablaAnalisis.getColumnModel().getColumn(6).setCellRenderer(renderNumero);
        tablaAnalisis.getColumnModel().getColumn(7).setCellRenderer(renderNumero);
        tablaAnalisis.getColumnModel().getColumn(8).setCellRenderer(renderNumero);
        tablaAnalisis.getColumnModel().getColumn(9).setCellRenderer(renderNumero);
        tablaAnalisis.getColumnModel().getColumn(10).setCellRenderer(renderNumero);

        tablaAnalisis.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDetalleSeleccionado();
            }
        });

        JScrollPane scrollPrincipal = new JScrollPane(tablaAnalisis);
        scrollPrincipal.setBorder(BorderFactory.createTitledBorder("Reporte ABC / EOQ / Reorden (solo lectura)"));

        modeloDetalle = new DefaultTableModel(new Object[]{
            "No Movimiento", "Fecha", "Motivo", "Cantidad", "Costo actual", "Importe consumo"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaDetalleConsumo = new JTable(modeloDetalle);
        tablaDetalleConsumo.setRowHeight(24);
        DefaultTableCellRenderer renderDetalleCentro = new DefaultTableCellRenderer();
        renderDetalleCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaDetalleConsumo.getColumnModel().getColumn(0).setCellRenderer(renderDetalleCentro);
        tablaDetalleConsumo.getColumnModel().getColumn(1).setCellRenderer(renderDetalleCentro);
        tablaDetalleConsumo.getColumnModel().getColumn(3).setCellRenderer(renderDetalleCentro);

        DefaultTableCellRenderer renderDetalleNumero = new DefaultTableCellRenderer();
        renderDetalleNumero.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaDetalleConsumo.getColumnModel().getColumn(4).setCellRenderer(renderDetalleNumero);
        tablaDetalleConsumo.getColumnModel().getColumn(5).setCellRenderer(renderDetalleNumero);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalleConsumo);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Detalle de consumo por producto (solo SALIDA)"));
        scrollDetalle.setPreferredSize(new Dimension(0, 180));

        central.add(scrollPrincipal, BorderLayout.CENTER);
        central.add(scrollDetalle, BorderLayout.SOUTH);
        return central;
    }

    // Abre una ventana con grafica de barras del consumo por productos clasificados ABC.
    private void abrirGraficaClasificada() {
        if (filasAnalisisActuales == null || filasAnalisisActuales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para mostrar en la grafica.", "Grafica", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.awt.Window owner = SwingUtilities.getWindowAncestor(this);
        AnalisisGraficaClasificacionView view = new AnalisisGraficaClasificacionView(owner, filasAnalisisActuales);
        view.setVisible(true);
    }

    // Carga resumen de consumo total y mensajes.
    private void cargarResumen(AnalisisInventarioResultado resultado) {
        lblConsumoTotal.setText("<html><span style='color:#DC2626;'>Consumo total del inventario: </span><span style='color:#1D4ED8;'>" + formatoMonto.format(resultado.getConsumoTotalInventario()) + "</span></html>");
        lblEstadoAnalisis.setText(resultado.getMensajeEstado());
    }

    // Carga filas calculadas del analisis.
    private void cargarTablaAnalisis(List<AnalisisInventarioItem> filas) {
        modeloAnalisis.setRowCount(0);

        for (AnalisisInventarioItem fila : filas) {
            String eoqTexto = fila.getEoq() == null ? "N/D" : formatoMonto.format(fila.getEoq());
            modeloAnalisis.addRow(new Object[]{
                fila.getGrupoABC(),
                fila.getCodigoBarras(),
                fila.getNombreProducto(),
                fila.getCategoria(),
                formatoMonto.format(fila.getCostoActual()),
                fila.getStockActual(),
                formatoMonto.format(fila.getPuntoReorden()),
                eoqTexto,
                formatoMonto.format(fila.getConsumoProducto()),
                formatoPorcentaje.format(fila.getPorcentajeIndividual()),
                formatoPorcentaje.format(fila.getPorcentajeAcumulado()),
                fila.getIndicadores()
            });
        }

        // Si no hay texto en buscador, dejamos el detalle vacio para evitar
        // mostrar una salida "pegada" sin filtro.
        String textoFiltro = txtFiltro == null ? "" : txtFiltro.getText().trim();
        if (textoFiltro.isEmpty()) {
            tablaAnalisis.clearSelection();
            limpiarDetalle();
            return;
        }

        // Si hay filtro activo y hay filas visibles, seleccionamos la primera.
        if (modeloAnalisis.getRowCount() > 0) {
            int filasVista = tablaAnalisis.getRowCount();
            if (filasVista > 0) {
                tablaAnalisis.setRowSelectionInterval(0, 0);
                cargarDetalleSeleccionado();
            } else {
                limpiarDetalle();
            }
        }
    }

    // Filtro de tabla por codigo, nombre y categoria.
    private void aplicarFiltroTabla() {
        String texto = txtFiltro.getText().trim();
        if (texto.isEmpty()) {
            sorterAnalisis.setRowFilter(null);
            // Si el buscador esta vacio, no dejamos una fila seleccionada
            // para evitar que se quede un dato en la tabla de salidas.
            tablaAnalisis.clearSelection();
            limpiarDetalle();
            return;
        } else {
            sorterAnalisis.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 1, 2, 3));
        }

        if (tablaAnalisis.getRowCount() > 0) {
            tablaAnalisis.setRowSelectionInterval(0, 0);
            cargarDetalleSeleccionado();
        } else {
            limpiarDetalle();
        }
    }

    // Carga detalle de salidas para el producto seleccionado.
    private void cargarDetalleSeleccionado() {
        int filaVista = tablaAnalisis.getSelectedRow();
        if (filaVista < 0) {
            limpiarDetalle();
            return;
        }

        int filaModelo = tablaAnalisis.convertRowIndexToModel(filaVista);
        String codigo = modeloAnalisis.getValueAt(filaModelo, 1).toString();
        List<AnalisisConsumoDetalleItem> detalle = analisisService.obtenerDetalleConsumoProducto(codigo);

        modeloDetalle.setRowCount(0);
        // Si el producto no tiene salidas, mostramos una fila informativa.
        if (detalle.isEmpty()) {
            modeloDetalle.addRow(new Object[]{
                "",
                "",
                "SIN SALIDAS",
                "",
                "",
                ""
            });
            return;
        }

        for (AnalisisConsumoDetalleItem item : detalle) {
            modeloDetalle.addRow(new Object[]{
                item.getNoMovimiento(),
                item.getFecha(),
                item.getMotivo(),
                item.getCantidad(),
                formatoMonto.format(item.getCostoUnitarioActual()),
                formatoMonto.format(item.getImporteConsumo())
            });
        }
    }

    private void limpiarDetalle() {
        modeloDetalle.setRowCount(0);
    }

    // Flujo de teclado para consulta rapida.
    private void configurarFlujoTeclado() {
        // Enter en filtro selecciona primera fila y baja a tabla.
        txtFiltro.addActionListener(e -> {
            aplicarFiltroTabla();
            if (tablaAnalisis.getRowCount() > 0) {
                tablaAnalisis.requestFocusInWindow();
            }
        });

        // Enter en tabla principal refresca detalle.
        tablaAnalisis.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "detalleConEnter");
        tablaAnalisis.getActionMap().put("detalleConEnter", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cargarDetalleSeleccionado();
            }
        });
    }

    // Renderer para colorear indicadores en la tabla principal.
    private static class RenderIndicadores extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                return c;
            }

            String indicador = value == null ? "" : value.toString();
            if (indicador.contains("STOCK BAJO")) {
                c.setBackground(new Color(255, 243, 205));
            } else if (indicador.contains("SOBREINVENTARIO")) {
                c.setBackground(new Color(209, 236, 241));
            } else if (indicador.contains("BAJO REORDEN")) {
                c.setBackground(new Color(255, 229, 180));
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
}
