package invetario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.List;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Panel del modulo de configuracion.
public class ConfiguracionPanel extends JPanel {

    // Servicio del CSV.
    private final ConfiguracionCSVService configuracionService;
    // Formateador de dinero con dos decimales.
    private final DecimalFormat formatoMoneda;

    // Campos del formulario.
    private JTextField txtCostoPedido;
    private JTextField txtCostoMantenimiento;
    private JTextField txtTiempoEntrega;

    // Tabla de registros guardados.
    private JTable tablaConfiguraciones;
    private DefaultTableModel modeloTablaConfiguraciones;

    // Botones de accion.
    private JButton btnRegistrar;
    private JButton btnEditar;
    private JButton btnLimpiar;

    // Estado actual.
    private JLabel lblEstado;
    private int indiceSeleccionado = -1;

    // Constructor.
    public ConfiguracionPanel() {
        configuracionService = new ConfiguracionCSVService();
        formatoMoneda = new DecimalFormat("0.00");
        construirPanel();
        cargarTablaConfiguraciones();
        limpiarFormulario();
    }

    // Permite refrescar cuando Index cambia a esta vista.
    public void refrescarDatos() {
        cargarTablaConfiguraciones();
        limpiarFormulario();
    }

    // Construye toda la vista.
    private void construirPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Tarjeta principal.
        JPanel panelTarjeta = new JPanel(new BorderLayout(12, 12));
        panelTarjeta.setBackground(Color.WHITE);
        panelTarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(18, 18, 18, 18)));

        // Cabecera.
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("Parametros de configuracion");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 41, 59));
        panelTitulo.add(lblTitulo, BorderLayout.WEST);

        lblEstado = new JLabel("Registra un nuevo parametro o selecciona uno para editar.");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstado.setForeground(new Color(100, 116, 139));
        panelTitulo.add(lblEstado, BorderLayout.SOUTH);
        panelTarjeta.add(panelTitulo, BorderLayout.NORTH);

        // Panel de campos.
        JPanel panelCampos = new JPanel(new GridLayout(1, 3, 14, 0));
        panelCampos.setOpaque(false);
        txtCostoPedido = new JTextField();
        txtCostoMantenimiento = new JTextField();
        txtTiempoEntrega = new JTextField();
        panelCampos.add(crearBloqueCampo("Costo por pedido", txtCostoPedido));
        panelCampos.add(crearBloqueCampo("Costo de mantenimiento", txtCostoMantenimiento));
        panelCampos.add(crearBloqueCampo("Tiempo de entrega (dias)", txtTiempoEntrega));

        // Tabla.
        modeloTablaConfiguraciones = new DefaultTableModel(
                new Object[]{"Costo pedido", "Costo mantenimiento", "Tiempo entrega"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaConfiguraciones = new JTable(modeloTablaConfiguraciones);
        tablaConfiguraciones.setRowHeight(26);
        tablaConfiguraciones.setSelectionBackground(new Color(219, 234, 254));
        tablaConfiguraciones.setSelectionForeground(new Color(33, 37, 41));

        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaConfiguraciones.getColumnModel().getColumn(0).setCellRenderer(renderDerecha);
        tablaConfiguraciones.getColumnModel().getColumn(1).setCellRenderer(renderDerecha);
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaConfiguraciones.getColumnModel().getColumn(2).setCellRenderer(renderCentro);

        // Al seleccionar fila se cargan valores y se habilita editar.
        tablaConfiguraciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarRegistroSeleccionado();
            }
        });

        // Enter en tabla: cargar seleccionado en campos.
        tablaConfiguraciones.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "cargarSeleccion");
        tablaConfiguraciones.getActionMap().put("cargarSeleccion", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cargarRegistroSeleccionado();
                txtCostoPedido.requestFocusInWindow();
                txtCostoPedido.selectAll();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaConfiguraciones);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Registros guardados"));

        JPanel panelCentro = new JPanel(new BorderLayout(0, 12));
        panelCentro.setOpaque(false);
        panelCentro.add(panelCampos, BorderLayout.NORTH);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);
        panelTarjeta.add(panelCentro, BorderLayout.CENTER);

        // Botones.
        JPanel panelBotones = new JPanel(new BorderLayout());
        panelBotones.setOpaque(false);
        JPanel panelDerecha = new JPanel(new GridLayout(1, 3, 10, 0));
        panelDerecha.setOpaque(false);

        btnRegistrar = new JButton("Registrar");
        btnEditar = new JButton("Editar");
        btnLimpiar = new JButton("Limpiar");
        btnRegistrar.setFocusPainted(false);
        btnEditar.setFocusPainted(false);
        btnLimpiar.setFocusPainted(false);
        // Mnemonics para accion rapida con Alt + tecla.
        btnRegistrar.setMnemonic(KeyEvent.VK_R);
        btnEditar.setMnemonic(KeyEvent.VK_E);
        btnLimpiar.setMnemonic(KeyEvent.VK_L);
        btnRegistrar.setBackground(new Color(21, 101, 192));
        btnRegistrar.setForeground(Color.WHITE);
        btnEditar.setEnabled(false);

        btnRegistrar.addActionListener(e -> registrarConfiguracion());
        btnEditar.addActionListener(e -> editarConfiguracion());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelDerecha.add(btnRegistrar);
        panelDerecha.add(btnEditar);
        panelDerecha.add(btnLimpiar);
        panelBotones.add(panelDerecha, BorderLayout.EAST);

        panelTarjeta.add(panelBotones, BorderLayout.SOUTH);
        add(panelTarjeta, BorderLayout.CENTER);

        configurarFlujoConEnter();
        configurarAtajosGlobalesTeclado();
        configurarNavegacionBotonesConFlechas();
    }

    // Crea bloque de etiqueta + campo.
    private JPanel crearBloqueCampo(String etiqueta, JTextField campo) {
        JPanel bloque = new JPanel(new BorderLayout(0, 8));
        bloque.setOpaque(false);

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));
        bloque.add(lbl, BorderLayout.NORTH);

        campo.setHorizontalAlignment(SwingConstants.RIGHT);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(8, 10, 8, 10)));
        bloque.add(campo, BorderLayout.CENTER);
        return bloque;
    }

    // Carga todos los registros en la tabla.
    private void cargarTablaConfiguraciones() {
        modeloTablaConfiguraciones.setRowCount(0);
        List<ConfiguracionParametros> lista = configuracionService.leerConfiguraciones();

        for (ConfiguracionParametros parametros : lista) {
            modeloTablaConfiguraciones.addRow(new Object[]{
                formatoMoneda.format(parametros.getCostoPedido()),
                formatoMoneda.format(parametros.getCostoMantenimiento()),
                parametros.getTiempoEntrega()
            });
        }
    }

    // Carga fila seleccionada en los textfields.
    private void cargarRegistroSeleccionado() {
        int fila = tablaConfiguraciones.getSelectedRow();
        if (fila < 0) {
            return;
        }

        indiceSeleccionado = fila;
        txtCostoPedido.setText(modeloTablaConfiguraciones.getValueAt(fila, 0).toString());
        txtCostoMantenimiento.setText(modeloTablaConfiguraciones.getValueAt(fila, 1).toString());
        txtTiempoEntrega.setText(modeloTablaConfiguraciones.getValueAt(fila, 2).toString());
        btnEditar.setEnabled(true);
        lblEstado.setText("Registro seleccionado. Puedes modificar y presionar Editar.");
    }

    // Registra un nuevo conjunto de parametros.
    private void registrarConfiguracion() {
        // Si hay un registro seleccionado, no permitimos registrar para evitar duplicidad accidental.
        if (indiceSeleccionado >= 0) {
            JOptionPane.showMessageDialog(this,
                    "Tienes un registro seleccionado. Usa Editar o presiona Limpiar para registrar uno nuevo.",
                    "Registro bloqueado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ConfiguracionParametros parametros = construirParametrosDesdeFormulario();
            configuracionService.registrarConfiguracion(parametros);
            cargarTablaConfiguraciones();
            limpiarFormulario();
            lblEstado.setText("Registro guardado correctamente.");
            JOptionPane.showMessageDialog(this, "Registro guardado correctamente.");
            txtCostoPedido.requestFocusInWindow();
            txtCostoPedido.selectAll();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Edita el registro seleccionado.
    private void editarConfiguracion() {
        if (indiceSeleccionado < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro de la tabla para editar.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ConfiguracionParametros parametros = construirParametrosDesdeFormulario();
            configuracionService.actualizarConfiguracion(indiceSeleccionado, parametros);
            cargarTablaConfiguraciones();
            limpiarFormulario();
            lblEstado.setText("Registro editado correctamente.");
            JOptionPane.showMessageDialog(this, "Registro editado correctamente.");
            txtCostoPedido.requestFocusInWindow();
            txtCostoPedido.selectAll();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Limpia campos y estado de seleccion.
    private void limpiarFormulario() {
        txtCostoPedido.setText("");
        txtCostoMantenimiento.setText("");
        txtTiempoEntrega.setText("");
        indiceSeleccionado = -1;
        btnEditar.setEnabled(false);
        tablaConfiguraciones.clearSelection();
        txtCostoPedido.requestFocusInWindow();
    }

    // Construye objeto validado desde textfields.
    private ConfiguracionParametros construirParametrosDesdeFormulario() {
        double costoPedido = parsearMonedaValida(txtCostoPedido.getText(), "Costo por pedido");
        double costoMantenimiento = parsearMonedaValida(txtCostoMantenimiento.getText(), "Costo de mantenimiento");
        int tiempoEntrega = parsearEnteroValido(txtTiempoEntrega.getText(), "Tiempo de entrega");
        return new ConfiguracionParametros(costoPedido, costoMantenimiento, tiempoEntrega);
    }

    // Moneda: numerico y mayor a 0.
    private double parsearMonedaValida(String texto, String nombreCampo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es obligatorio.");
        }

        try {
            String normalizado = normalizarTextoMoneda(texto);
            double valor = Double.parseDouble(normalizado);
            if (valor <= 0) {
                throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser mayor a 0.");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser numerico.");
        }
    }

    // Entero: numerico, mayor a 0 y sin decimales.
    private int parsearEnteroValido(String texto, String nombreCampo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es obligatorio.");
        }

        String limpio = texto.trim();
        if (limpio.contains(".") || limpio.contains(",")) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " no permite decimales.");
        }

        try {
            int valor = Integer.parseInt(limpio);
            if (valor <= 0) {
                throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser mayor a 0.");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser entero numerico.");
        }
    }

    // Normaliza texto para parsear moneda.
    private String normalizarTextoMoneda(String texto) {
        String valor = texto.trim().replace(" ", "");
        if (valor.contains(".") && valor.contains(",")) {
            valor = valor.replace(",", "");
            return valor;
        }
        if (!valor.contains(".") && valor.contains(",")) {
            valor = valor.replace(",", ".");
        }
        return valor;
    }

    // Flujo por teclado: Enter avanza y guarda/edita.
    private void configurarFlujoConEnter() {
        txtCostoPedido.addActionListener(e -> txtCostoMantenimiento.requestFocusInWindow());
        txtCostoMantenimiento.addActionListener(e -> txtTiempoEntrega.requestFocusInWindow());
        txtTiempoEntrega.addActionListener(e -> {
            if (indiceSeleccionado >= 0) {
                btnEditar.requestFocusInWindow();
            } else {
                btnRegistrar.requestFocusInWindow();
            }
        });

        btnRegistrar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "registrarConEnter");
        btnRegistrar.getActionMap().put("registrarConEnter", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnRegistrar.doClick();
            }
        });

        btnEditar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "editarConEnter");
        btnEditar.getActionMap().put("editarConEnter", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnEditar.doClick();
            }
        });

        btnLimpiar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "limpiarConEnter");
        btnLimpiar.getActionMap().put("limpiarConEnter", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnLimpiar.doClick();
            }
        });
    }

    // Atajos globales para operar el modulo casi sin mouse.
    private void configurarAtajosGlobalesTeclado() {
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();

        // Ctrl + 1: foco al primer campo.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK), "focoCostoPedido");
        actionMap.put("focoCostoPedido", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtCostoPedido.requestFocusInWindow();
                txtCostoPedido.selectAll();
            }
        });

        // Ctrl + T: foco a la tabla.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "focoTabla");
        actionMap.put("focoTabla", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (tablaConfiguraciones.getRowCount() > 0) {
                    if (tablaConfiguraciones.getSelectedRow() < 0) {
                        tablaConfiguraciones.setRowSelectionInterval(0, 0);
                    }
                    tablaConfiguraciones.requestFocusInWindow();
                }
            }
        });

        // Ctrl + G: registrar.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK), "registrarRapido");
        actionMap.put("registrarRapido", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnRegistrar.doClick();
            }
        });

        // Ctrl + E: editar registro seleccionado.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), "editarRapido");
        actionMap.put("editarRapido", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (btnEditar.isEnabled()) {
                    btnEditar.doClick();
                }
            }
        });

        // Ctrl + L o Escape: limpiar y volver al primer campo.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "limpiarRapido");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "limpiarRapido");
        actionMap.put("limpiarRapido", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnLimpiar.doClick();
            }
        });
    }

    // Permite mover foco entre botones con flechas izquierda/derecha.
    private void configurarNavegacionBotonesConFlechas() {
        // En Registrar: derecha -> Editar, izquierda -> Limpiar.
        btnRegistrar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "focoEditarDesdeRegistrar");
        btnRegistrar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "focoLimpiarDesdeRegistrar");
        btnRegistrar.getActionMap().put("focoEditarDesdeRegistrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnEditar.requestFocusInWindow();
            }
        });
        btnRegistrar.getActionMap().put("focoLimpiarDesdeRegistrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnLimpiar.requestFocusInWindow();
            }
        });

        // En Editar: derecha -> Limpiar, izquierda -> Registrar.
        btnEditar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "focoLimpiarDesdeEditar");
        btnEditar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "focoRegistrarDesdeEditar");
        btnEditar.getActionMap().put("focoLimpiarDesdeEditar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnLimpiar.requestFocusInWindow();
            }
        });
        btnEditar.getActionMap().put("focoRegistrarDesdeEditar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnRegistrar.requestFocusInWindow();
            }
        });

        // En Limpiar: derecha -> Registrar, izquierda -> Editar.
        btnLimpiar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "focoRegistrarDesdeLimpiar");
        btnLimpiar.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "focoEditarDesdeLimpiar");
        btnLimpiar.getActionMap().put("focoRegistrarDesdeLimpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnRegistrar.requestFocusInWindow();
            }
        });
        btnLimpiar.getActionMap().put("focoEditarDesdeLimpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnEditar.requestFocusInWindow();
            }
        });
    }
}
