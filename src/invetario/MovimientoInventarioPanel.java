package invetario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Panel unificado del modulo de Movimientos de Inventario.
public class MovimientoInventarioPanel extends JPanel {

    // Servicios de negocio y CSV.
    private final MovimientoInventarioCSVService movimientoService;
    private final ProductoCSVService productoService;

    // Campos del encabezado.
    private JTextField txtNombre;
    private JTextField txtDomicilio;
    private JTextField txtLugar;
    private JTextField txtFecha;
    private JTextField txtNoMovimiento;
    private JComboBox<String> cmbTipoMovimiento;
    private JTextField txtMotivo;

    // Componentes de busqueda y tablas.
    private JTextField txtBuscarProducto;
    private JTable tablaProductos;
    private JTable tablaDetalle;
    private DefaultTableModel modeloProductos;
    private DefaultTableModel modeloDetalle;
    // Botones principales para flujo rapido con Enter.
    private JButton btnAgregarProductoPrincipal;
    private JButton btnQuitarProductoPrincipal;
    private JButton btnGuardarMovimientoPrincipal;

    // Cache de catalogo para busqueda en memoria.
    private final List<Producto> productosCache = new ArrayList<Producto>();
    // Callback opcional para avisar a otras vistas que hubo guardado.
    private Runnable onMovimientoGuardado;

    // Constructor del panel.
    public MovimientoInventarioPanel() {
        movimientoService = new MovimientoInventarioCSVService();
        productoService = new ProductoCSVService();
        construirPanel();
        refrescarDatos();
    }

    // Permite registrar un callback desde Index para refrescar catalogo.
    public void setOnMovimientoGuardado(Runnable onMovimientoGuardado) {
        this.onMovimientoGuardado = onMovimientoGuardado;
    }

    // Metodo publico para refrescar desde Index.
    public void refrescarDatos() {
        cargarProductosEnCache();
        limpiarTablaProductos();
        prepararNuevoMovimiento();
    }

    // Crea toda la estructura visual del panel.
    private void construirPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(crearPanelSuperiorFormulario(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);
        // Configuramos flujo con Enter al terminar de crear componentes.
        configurarFlujoConEnter();
    }

    // Crea formulario superior con bloques compactos.
    private JPanel crearPanelSuperiorFormulario() {
        JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel filaUno = new JPanel(new java.awt.GridLayout(1, 4, 12, 0));
        filaUno.setOpaque(false);
        JPanel filaDos = new JPanel(new java.awt.GridLayout(1, 4, 12, 0));
        filaDos.setOpaque(false);

        txtNombre = new JTextField();
        txtDomicilio = new JTextField();
        txtLugar = new JTextField();
        txtFecha = new JTextField();
        txtNoMovimiento = new JTextField();
        txtNoMovimiento.setEditable(false);
        txtNoMovimiento.setBackground(new Color(241, 245, 249));
        cmbTipoMovimiento = new JComboBox<String>(new String[]{"Entrada", "Salida", "Ajuste"});
        txtMotivo = new JTextField();

        filaUno.add(crearBloqueCampo("Nombre", txtNombre));
        filaUno.add(crearBloqueCampo("Domicilio", txtDomicilio));
        filaUno.add(crearBloqueCampo("Lugar", txtLugar));
        filaUno.add(crearBloqueCampo("Fecha (yyyy-MM-dd)", txtFecha));

        filaDos.add(crearBloqueCampo("No. Movimiento", txtNoMovimiento));
        filaDos.add(crearBloqueCampo("Tipo Movimiento", cmbTipoMovimiento));
        filaDos.add(crearBloqueCampo("Motivo", txtMotivo));
        filaDos.add(new JPanel());

        JPanel contenedorFilas = new JPanel(new java.awt.GridLayout(2, 1, 0, 10));
        contenedorFilas.setOpaque(false);
        contenedorFilas.add(filaUno);
        contenedorFilas.add(filaDos);

        panelFormulario.add(contenedorFilas, BorderLayout.CENTER);
        return panelFormulario;
    }

    // Crea bloque vertical con etiqueta y componente.
    private JPanel crearBloqueCampo(String etiqueta, Component componente) {
        JPanel panelBloque = new JPanel(new BorderLayout(0, 6));
        panelBloque.setOpaque(false);
        JLabel lbl = new JLabel(etiqueta + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(51, 65, 85));
        panelBloque.add(lbl, BorderLayout.NORTH);
        panelBloque.add(componente, BorderLayout.CENTER);
        return panelBloque;
    }

    // Crea zona central con detalle y buscador lateral.
    private JPanel crearPanelCentral() {
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setOpaque(false);

        modeloDetalle = new DefaultTableModel(new Object[]{"Cantidad", "Codigo Producto", "Descripcion", "Precio Unitario", "Importe"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaDetalle = new JTable(modeloDetalle);
        tablaDetalle.setRowHeight(26);
        configurarRendererDecimales(tablaDetalle, 3, 4);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del movimiento"));

        modeloProductos = new DefaultTableModel(new Object[]{"Codigo", "Descripcion", "Stock", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setRowHeight(24);
        // Reducimos el tamano visible para que sea una tabla compacta de seleccion.
        tablaProductos.setPreferredScrollableViewportSize(new Dimension(300, 120));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tablaProductos.getColumnModel().getColumn(2).setCellRenderer(center);
        tablaProductos.getColumnModel().getColumn(3).setCellRenderer(center);

        txtBuscarProducto = new JTextField();
        JButton btnBuscar = new JButton("Buscar");
        // Boton buscar: valida y avisa si no existe.
        btnBuscar.addActionListener(e -> buscarProductosPorCodigo(true));
        // Enter en buscador: valida y avisa si no existe.
        txtBuscarProducto.addActionListener(e -> buscarProductosPorCodigo(true));

        JPanel panelBusqueda = new JPanel(new BorderLayout(6, 6));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(new JLabel("Buscar producto por codigo"), BorderLayout.NORTH);
        JPanel lineaBusqueda = new JPanel(new BorderLayout(6, 0));
        lineaBusqueda.setOpaque(false);
        lineaBusqueda.add(txtBuscarProducto, BorderLayout.CENTER);
        lineaBusqueda.add(btnBuscar, BorderLayout.EAST);
        panelBusqueda.add(lineaBusqueda, BorderLayout.CENTER);

        JPanel panelLateral = new JPanel(new BorderLayout(8, 8));
        panelLateral.setBackground(Color.WHITE);
        panelLateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 8, 8, 8)));
        // Agregamos la tabla compacta al centro.
        panelLateral.add(panelBusqueda, BorderLayout.NORTH);
        panelLateral.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        // Creamos botones principales debajo de la tabla de seleccion.
        JPanel panelBotonesLateral = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        panelBotonesLateral.setOpaque(false);
        btnAgregarProductoPrincipal = new JButton("Agregar producto");
        btnQuitarProductoPrincipal = new JButton("Quitar producto");
        btnAgregarProductoPrincipal.addActionListener(e -> agregarProductoAlDetalle());
        btnQuitarProductoPrincipal.addActionListener(e -> quitarProductoDelDetalle());
        panelBotonesLateral.add(btnAgregarProductoPrincipal);
        panelBotonesLateral.add(btnQuitarProductoPrincipal);
        panelLateral.add(panelBotonesLateral, BorderLayout.SOUTH);

        // Ajustamos ancho del panel lateral para que no robe tanto espacio.
        panelLateral.setPreferredSize(new Dimension(340, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollDetalle, panelLateral);
        split.setResizeWeight(0.74);
        split.setDividerLocation(0.74);

        panelCentral.add(split, BorderLayout.CENTER);
        return panelCentral;
    }

    // Crea botones de acciones del modulo.
    private JPanel crearPanelBotones() {
        JPanel panelBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        panelBotones.setOpaque(false);

        btnGuardarMovimientoPrincipal = new JButton("Guardar movimiento");
        JButton btnLimpiarFormulario = new JButton("Limpiar");
        JButton btnVerStockActual = new JButton("Ver stock actual");
        JButton btnVerHistorial = new JButton("Ver historial por producto");

        btnGuardarMovimientoPrincipal.addActionListener(e -> guardarMovimiento());
        btnLimpiarFormulario.addActionListener(e -> limpiarFormulario());
        btnVerStockActual.addActionListener(e -> new StockActualView(null).setVisible(true));
        btnVerHistorial.addActionListener(e -> new HistorialProductoView(null).setVisible(true));

        panelBotones.add(btnGuardarMovimientoPrincipal);
        panelBotones.add(btnLimpiarFormulario);
        panelBotones.add(btnVerStockActual);
        panelBotones.add(btnVerHistorial);
        return panelBotones;
    }

    // Carga productos en cache desde CSV.
    private void cargarProductosEnCache() {
        productosCache.clear();
        productosCache.addAll(movimientoService.obtenerCatalogoProductos());
    }

    // Limpia la tabla lateral para no mostrar todo el catalogo.
    private void limpiarTablaProductos() {
        modeloProductos.setRowCount(0);
    }

    // Busca y muestra coincidencias por codigo o descripcion.
    private void buscarProductosPorCodigo() {
        buscarProductosPorCodigo(false);
    }

    // Busca por codigo/descripcion y permite controlar si muestra alerta cuando no encuentra.
    private void buscarProductosPorCodigo(boolean mostrarAvisoSiNoExiste) {
        String texto = txtBuscarProducto.getText().trim().toLowerCase();
        modeloProductos.setRowCount(0);

        if (texto.isEmpty()) {
            return;
        }

        for (Producto productoActual : productosCache) {
            String codigo = productoActual.getClave() == null ? "" : productoActual.getClave().toLowerCase();
            String descripcion = productoActual.getNombre() == null ? "" : productoActual.getNombre().toLowerCase();

            if (codigo.contains(texto) || descripcion.contains(texto)) {
                modeloProductos.addRow(new Object[]{
                    productoActual.getClave(),
                    productoActual.getNombre(),
                    productoActual.getStockActual(),
                    productoActual.isActivo() ? "Activo" : "Inactivo"
                });
            }
        }

        // Si encontro resultados, seleccionamos el primero para usar Enter de inmediato.
        if (modeloProductos.getRowCount() > 0) {
            tablaProductos.setRowSelectionInterval(0, 0);
            tablaProductos.requestFocusInWindow();
        } else if (mostrarAvisoSiNoExiste) {
            JOptionPane.showMessageDialog(this, "No existe un producto con ese codigo o descripcion.", "Busqueda", JOptionPane.WARNING_MESSAGE);
            txtBuscarProducto.requestFocusInWindow();
            txtBuscarProducto.selectAll();
        }
    }

    // Prepara fecha y folio por defecto.
    private void prepararNuevoMovimiento() {
        txtNoMovimiento.setText(String.valueOf(movimientoService.generarSiguienteNoMovimiento()));
        // Fecha por defecto al dia de registro, editable por usuario.
        txtFecha.setText(LocalDate.now().toString());
    }

    // Agrega producto seleccionado al detalle.
    private void agregarProductoAlDetalle() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto del buscador.", "Detalle", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipoMovimiento = cmbTipoMovimiento.getSelectedItem().toString();
        String codigoProducto = modeloProductos.getValueAt(fila, 0).toString();
        String descripcionProducto = modeloProductos.getValueAt(fila, 1).toString();
        int stockDisponible = productoService.convertirEnteroSeguro(modeloProductos.getValueAt(fila, 2).toString());
        String estadoProducto = modeloProductos.getValueAt(fila, 3).toString();

        if ("Inactivo".equalsIgnoreCase(estadoProducto) && !"Ajuste".equalsIgnoreCase(tipoMovimiento)) {
            JOptionPane.showMessageDialog(this, "Producto inactivo: solo se permite Ajuste.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String mensaje = "Cantidad para " + tipoMovimiento + " de " + codigoProducto + ":";
        if ("Ajuste".equalsIgnoreCase(tipoMovimiento)) {
            mensaje = "Nuevo stock final para ajuste de " + codigoProducto + ":";
        }

        String textoCantidad = (String) JOptionPane.showInputDialog(
                this,
                mensaje,
                "Cantidad",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "1");
        if (textoCantidad == null) {
            return;
        }

        int cantidad = productoService.convertirEnteroSeguro(textoCantidad);
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Salida".equalsIgnoreCase(tipoMovimiento) && cantidad > stockDisponible) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente. Disponible: " + stockDisponible, "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double precioUnitario = obtenerPrecioProducto(codigoProducto);
        int filaExistente = buscarFilaDetallePorCodigo(codigoProducto);

        if (filaExistente >= 0) {
            int cantidadActual = productoService.convertirEnteroSeguro(modeloDetalle.getValueAt(filaExistente, 0).toString());
            int nuevaCantidad = "Ajuste".equalsIgnoreCase(tipoMovimiento) ? cantidad : cantidadActual + cantidad;
            modeloDetalle.setValueAt(nuevaCantidad, filaExistente, 0);
            modeloDetalle.setValueAt(precioUnitario, filaExistente, 3);
            modeloDetalle.setValueAt(nuevaCantidad * precioUnitario, filaExistente, 4);
            // Dejamos listo el buscador para capturar el siguiente producto sin clics.
            prepararSiguienteBusquedaProducto();
            return;
        }

        modeloDetalle.addRow(new Object[]{
            cantidad,
            codigoProducto,
            descripcionProducto,
            precioUnitario,
            cantidad * precioUnitario
        });

        // Dejamos listo el buscador para capturar el siguiente producto sin clics.
        prepararSiguienteBusquedaProducto();
    }

    // Obtiene precio real desde cache.
    private double obtenerPrecioProducto(String codigoProducto) {
        for (Producto productoActual : productosCache) {
            if (productoActual.getClave() != null && productoActual.getClave().equalsIgnoreCase(codigoProducto)) {
                return productoService.convertirDecimalSeguro(productoActual.getPrecio());
            }
        }
        return 0;
    }

    // Busca fila en detalle por codigo.
    private int buscarFilaDetallePorCodigo(String codigoProducto) {
        for (int i = 0; i < modeloDetalle.getRowCount(); i++) {
            if (codigoProducto.equalsIgnoreCase(modeloDetalle.getValueAt(i, 1).toString())) {
                return i;
            }
        }
        return -1;
    }

    // Quita producto del detalle.
    private void quitarProductoDelDetalle() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila del detalle.", "Detalle", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloDetalle.removeRow(fila);
    }

    // Guarda movimiento con validaciones.
    private void guardarMovimiento() {
        try {
            MovimientoInventarioEncabezado encabezado = construirEncabezado();
            List<MovimientoInventarioDetalle> detalles = construirDetallesDesdeTabla();
            MovimientoInventarioPreparado preparado = movimientoService.validarYPrepararMovimiento(encabezado, detalles);

            if (!preparado.getAdvertenciasSobreinventario().isEmpty() && "ENTRADA".equalsIgnoreCase(encabezado.getTipoMovimiento())) {
                StringBuilder mensaje = new StringBuilder();
                mensaje.append("Advertencia de sobreinventario:\n");
                for (String advertencia : preparado.getAdvertenciasSobreinventario()) {
                    mensaje.append("- ").append(advertencia).append("\n");
                }
                mensaje.append("\nDeseas continuar?");

                int respuesta = JOptionPane.showConfirmDialog(this, mensaje.toString(), "Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            movimientoService.guardarMovimiento(preparado);
            JOptionPane.showMessageDialog(this, "Movimiento guardado correctamente.");
            limpiarFormulario();
            refrescarDatos();
            // Si existe callback, avisamos que se guardo para refrescar tabla de catalogo.
            if (onMovimientoGuardado != null) {
                onMovimientoGuardado.run();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Construye encabezado desde formulario.
    private MovimientoInventarioEncabezado construirEncabezado() {
        String nombre = txtNombre.getText().trim();
        String domicilio = txtDomicilio.getText().trim();
        String lugar = txtLugar.getText().trim();
        String fecha = txtFecha.getText().trim();
        String tipo = cmbTipoMovimiento.getSelectedItem() == null ? "" : cmbTipoMovimiento.getSelectedItem().toString().trim();
        String motivo = txtMotivo.getText().trim();

        // Motivo solo sera obligatorio cuando el tipo sea Ajuste.
        boolean motivoObligatorio = "AJUSTE".equalsIgnoreCase(tipo);
        if (nombre.isEmpty() || domicilio.isEmpty() || lugar.isEmpty() || fecha.isEmpty() || tipo.isEmpty()) {
            throw new IllegalArgumentException("Nombre, Domicilio, Lugar, Fecha y Tipo son obligatorios.");
        }
        if (motivoObligatorio && motivo.isEmpty()) {
            throw new IllegalArgumentException("En movimiento de Ajuste, el motivo es obligatorio.");
        }

        return new MovimientoInventarioEncabezado(
                txtNoMovimiento.getText().trim(),
                nombre,
                domicilio,
                lugar,
                fecha,
                tipo.toUpperCase(),
                motivo
        );
    }

    // Convierte la tabla detalle en lista de objetos.
    private List<MovimientoInventarioDetalle> construirDetallesDesdeTabla() {
        List<MovimientoInventarioDetalle> detalles = new ArrayList<MovimientoInventarioDetalle>();
        if (modeloDetalle.getRowCount() == 0) {
            throw new IllegalArgumentException("Debes agregar productos al detalle.");
        }

        for (int i = 0; i < modeloDetalle.getRowCount(); i++) {
            MovimientoInventarioDetalle detalle = new MovimientoInventarioDetalle();
            detalle.setNoMovimiento(txtNoMovimiento.getText().trim());
            detalle.setCantidad(productoService.convertirEnteroSeguro(modeloDetalle.getValueAt(i, 0).toString()));
            detalle.setCodigoProducto(modeloDetalle.getValueAt(i, 1).toString());
            detalle.setDescripcionProducto(modeloDetalle.getValueAt(i, 2).toString());
            detalle.setPrecioUnitario(productoService.convertirDecimalSeguro(modeloDetalle.getValueAt(i, 3).toString()));
            detalle.setImporte(detalle.getCantidad() * detalle.getPrecioUnitario());
            detalles.add(detalle);
        }
        return detalles;
    }

    // Limpia formulario y detalle.
    private void limpiarFormulario() {
        txtNombre.setText("");
        txtDomicilio.setText("");
        txtLugar.setText("");
        txtMotivo.setText("");
        modeloDetalle.setRowCount(0);
        txtBuscarProducto.setText("");
        limpiarTablaProductos();
        cmbTipoMovimiento.setSelectedIndex(0);
        prepararNuevoMovimiento();
        txtNombre.requestFocusInWindow();
    }

    // Formatea columnas decimales.
    private void configurarRendererDecimales(JTable tabla, int... columnas) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
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

        for (int columna : columnas) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(renderer);
        }
    }

    // Configura flujo de captura usando Enter como accion principal.
    private void configurarFlujoConEnter() {
        // Flujo de encabezado.
        txtNombre.addActionListener(e -> txtDomicilio.requestFocusInWindow());
        txtDomicilio.addActionListener(e -> txtLugar.requestFocusInWindow());
        txtLugar.addActionListener(e -> txtFecha.requestFocusInWindow());
        txtFecha.addActionListener(e -> cmbTipoMovimiento.requestFocusInWindow());
        txtMotivo.addActionListener(e -> txtBuscarProducto.requestFocusInWindow());

        // Enter en combo para avanzar a motivo.
        cmbTipoMovimiento.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "irMotivo");
        cmbTipoMovimiento.getActionMap().put("irMotivo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtMotivo.requestFocusInWindow();
            }
        });

        // Enter en tabla de productos agrega producto al detalle.
        InputMap mapaTablaProductos = tablaProductos.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mapaTablaProductos.put(KeyStroke.getKeyStroke("ENTER"), "agregarDesdeTabla");
        tablaProductos.getActionMap().put("agregarDesdeTabla", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                agregarProductoAlDetalle();
            }
        });

        // Enter en tabla detalle dispara guardado para cerrar flujo sin clicks.
        InputMap mapaTablaDetalle = tablaDetalle.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mapaTablaDetalle.put(KeyStroke.getKeyStroke("ENTER"), "guardarDesdeDetalle");
        tablaDetalle.getActionMap().put("guardarDesdeDetalle", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (btnGuardarMovimientoPrincipal != null) {
                    btnGuardarMovimientoPrincipal.doClick();
                }
            }
        });
    }

    // Limpia buscador y devuelve foco para captura continua de productos.
    private void prepararSiguienteBusquedaProducto() {
        txtBuscarProducto.setText("");
        limpiarTablaProductos();
        txtBuscarProducto.requestFocusInWindow();
    }
}
