package invetario;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.text.DecimalFormat;




public class Index extends JFrame {
    
    

    // =========================
    // COLORES DEL SISTEMA
    // =========================

    private final Color colorFondo = new Color(245, 247, 250);
    private final Color colorPanel = Color.WHITE;
    private final Color colorPrimario = new Color(21, 101, 192);
    private final Color colorTexto = new Color(33, 37, 41);
    private final Color colorAlerta = new Color(255, 243, 205);
    private final Color colorExito = new Color(212, 237, 218);
    private final Color colorPeligro = new Color(248, 215, 218);
    private final Color colorCampoError = new Color(255, 235, 238);
   
    private TableRowSorter<DefaultTableModel> sorter;

  

    /*
    instanciamos el dao
    */
    private final ProductoDAO productoDAO = new ProductoDAO();

    //modelo de la tabla
    private final DefaultTableModel modeloTablaProductos = new DefaultTableModel(
            new Object[]{"Clave", "Nombre", "Categoria", "Costo", "Precio", "Stock", "Minimo", "Entrega", "Demanda", "Estado", "Alerta"}, 0) {
        // Este metodo evita que el usuario escriba directo sobre la tabla.
        @Override
        public boolean isCellEditable(int fila, int columna) {
            // Siempre devolvemos falso para dejar la tabla solo de lectura.
            return false;
        }
    };



    // COMPONENTES PRINCIPALES
 

    // Este layout permite cambiar entre las pantallas del centro.
    private CardLayout layoutTarjetas;
    // Este panel es donde se cargan las pantallas centrales.
    private JPanel panelTarjetas;

    // Etiqueta del texto resumen superior.
    private JLabel lblResumenSuperior;
    // Etiqueta del titulo superior principal.
    private JLabel lblTituloPrincipal;
    // Etiqueta de la tarjeta de total de productos.
    private JLabel lblTotalProductos;
    // Etiqueta de la tarjeta de productos activos.
    private JLabel lblProductosActivos;
    // Etiqueta de la tarjeta de productos con stock bajo.
    private JLabel lblProductosBajoStock;
    // Etiqueta de la tarjeta de productos agotados.
    private JLabel lblProductosAgotados;
    // Etiqueta con el resumen del dashboard.
    private JLabel lblSituacionActual;

    /*
    nombres de los componentes
    */
    private JTextField txtBuscarProducto;
    private JComboBox<String> cmbFiltrarCategoria;
    private JComboBox<String> cmbFiltrarEstado;
    private JTable tblCatalogoProductos;
    private JButton btnRegistrarProducto;
    private JButton btnEditarProducto;
    private JButton btnCambiarEstadoProducto;
    private JButton btnEliminarProducto;


    private JButton btnMenuDashboard;
    private JButton btnMenuCatalogo;
    private JButton btnMenuMovimientos;
    private JButton btnMenuAnalisis;
    private JButton btnMenuReportes;
    private JButton btnMenuConfiguracion;
    // Panel del modulo de movimientos dentro del dashboard.
    private MovimientoInventarioPanel panelMovimientoInventario;
    // Panel del modulo de configuracion dentro del dashboard.
    private ConfiguracionPanel panelConfiguracion;


    // CONSTRUCTOR


    //constructor principal
    public Index() {
       
        initComponents();
        
        setTitle("Sistema de Inventario");
        setMinimumSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);
        
        construirVentana();
        actualizarTablaProductos();
        setVisible(true);
        
        //sorter de la tabla para filtrar
        sorter = new TableRowSorter<>(modeloTablaProductos);
        tblCatalogoProductos.setRowSorter(sorter);
        
        //Renderer para poner a la derecha
        DefaultTableCellRenderer alineacionDerecha = new DefaultTableCellRenderer();
        alineacionDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
            //nuimero de columna
        tblCatalogoProductos.getColumnModel().getColumn(3).setCellRenderer(alineacionDerecha);
        tblCatalogoProductos.getColumnModel().getColumn(4).setCellRenderer(alineacionDerecha);
        tblCatalogoProductos.getColumnModel().getColumn(5).setCellRenderer(alineacionDerecha);
        tblCatalogoProductos.getColumnModel().getColumn(6).setCellRenderer(alineacionDerecha);
        tblCatalogoProductos.getColumnModel().getColumn(7).setCellRenderer(alineacionDerecha);
        tblCatalogoProductos.getColumnModel().getColumn(8).setCellRenderer(alineacionDerecha);

        //al centro la columna nueve
        DefaultTableCellRenderer alineacionCentro = new DefaultTableCellRenderer();
        alineacionCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tblCatalogoProductos.getColumnModel().getColumn(9).setCellRenderer(alineacionCentro);
        
        /*
        Formateador de numeros
        */
        DecimalFormat df = new DecimalFormat("#,##0.00");

        DefaultTableCellRenderer renderDecimales = new DefaultTableCellRenderer() {
            @Override
                protected void setValue(Object value) {
                 if (value != null && !value.toString().isEmpty()) {
                  try {
                    double monto = Double.parseDouble(value.toString());
                    setText(df.format(monto)); // Usamos setText para mostrar el formato
                 } catch (NumberFormatException e) {
                    setText(value.toString());
                    }
                        } else {
                setText("");
                     }
             }
             };

         // Forzamos la alineación a la derecha en el mismo renderizador
         renderDecimales.setHorizontalAlignment(SwingConstants.RIGHT);
         //lo aplicamos a las columnas de precio
        tblCatalogoProductos.getColumnModel().getColumn(3).setCellRenderer(renderDecimales);
        tblCatalogoProductos.getColumnModel().getColumn(4).setCellRenderer(renderDecimales);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        // Indicamos que al cerrar la ventana se termine el programa.
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Creamos el layout base del formulario vacio generado por NetBeans.
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        // Aplicamos ese layout al contenedor principal.
        getContentPane().setLayout(layout);
        // Definimos la parte horizontal del layout vacio.
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        // Definimos la parte vertical del layout vacio.
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        // Ajustamos la ventana al contenido actual.
        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void filtrarNombre(String texto) {
    // (?i) hace que no importe si es MAYÚSCULA o minúscula
    // El número 1 es el índice de la columna "Nombre"
    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1));
}
    
    



    /*
    Metodo para construir la ventana princial
    */
    private void construirVentana() {
       
        getContentPane().removeAll();     
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(colorFondo);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        // Le damos color de fondo.
        panelPrincipal.setBackground(colorFondo);
        panelPrincipal.add(crearPanelMenuLateral(), BorderLayout.WEST);
        panelPrincipal.add(crearPanelTrabajo(), BorderLayout.CENTER);

        // Reemplazamos el contenido actual de la ventana por el nuevo.
        setContentPane(panelPrincipal);
        revalidate();
        repaint();
    }

    /*
      Este metodo crea el menu lateral de modulos.
    */
    private JPanel crearPanelMenuLateral() {
        // Creamos el panel del menu.
        JPanel panelMenuLateral = new JPanel();
        // Definimos ancho fijo para el menu.
        panelMenuLateral.setPreferredSize(new Dimension(240, 0));
        // Aplicamos color oscuro.
        panelMenuLateral.setBackground(new Color(15, 23, 42));
        // Agregamos espacios internos.
        panelMenuLateral.setBorder(new EmptyBorder(24, 18, 24, 18));
        // Ordenamos elementos en columna.
        panelMenuLateral.setLayout(new BoxLayout(panelMenuLateral, BoxLayout.Y_AXIS));

        // Creamos la etiqueta del nombre del sistema.





        panelMenuLateral.add(Box.createVerticalStrut(30));

        // Creamos el boton del dashboard.
        btnMenuDashboard = crearBotonMenu("Inicio / Dashboard");
        // Dejamos este boton solo visible para usarlo despues.
        deshabilitarBotonMenu(btnMenuDashboard);

        // Creamos el boton del catalogo.
        btnMenuCatalogo = crearBotonMenu("Catalogo de Productos");
        // Dejamos marcado visualmente el modulo que si funcionara por ahora.
        marcarBotonMenuActivo(btnMenuCatalogo);
        // Al dar clic mostramos el catalogo dentro del dashboard.
        btnMenuCatalogo.addActionListener(e -> mostrarVista("CATALOGO"));

        // Creamos el boton del modulo movimientos.
        btnMenuMovimientos = crearBotonMenu("Movimientos");
        // Al dar clic mostramos movimientos en la misma interfaz.
        btnMenuMovimientos.addActionListener(e -> mostrarVista("MOVIMIENTOS"));

        // Creamos el boton del modulo analisis.
        btnMenuAnalisis = crearBotonMenu("Analisis");
        // Dejamos este boton solo visible para usarlo despues.
        deshabilitarBotonMenu(btnMenuAnalisis);

        // Creamos el boton del modulo reportes.
        btnMenuReportes = crearBotonMenu("Reportes");
        // Dejamos este boton solo visible para usarlo despues.
        deshabilitarBotonMenu(btnMenuReportes);

        // Creamos el boton del modulo configuracion.
        btnMenuConfiguracion = crearBotonMenu("Configuracion");
        // Al dar clic mostramos configuracion dentro del dashboard.
        btnMenuConfiguracion.addActionListener(e -> mostrarVista("CONFIGURACION"));

        // Agregamos botones al menu con separacion.
        panelMenuLateral.add(crearContenedorBotonMenu(btnMenuDashboard));
        // Agregamos espacio entre botones.
        panelMenuLateral.add(Box.createVerticalStrut(10));
        // Agregamos boton catalogo.
        panelMenuLateral.add(crearContenedorBotonMenu(btnMenuCatalogo));
        // Agregamos espacio.
        panelMenuLateral.add(Box.createVerticalStrut(10));
        // Agregamos boton movimientos.
        panelMenuLateral.add(crearContenedorBotonMenu(btnMenuMovimientos));
        // Agregamos espacio.
        panelMenuLateral.add(Box.createVerticalStrut(10));
        // Agregamos boton analisis.
        panelMenuLateral.add(crearContenedorBotonMenu(btnMenuAnalisis));
        // Agregamos espacio.
        panelMenuLateral.add(Box.createVerticalStrut(10));
        // Agregamos boton reportes.
        panelMenuLateral.add(crearContenedorBotonMenu(btnMenuReportes));
        // Agregamos espacio.
        panelMenuLateral.add(Box.createVerticalStrut(10));
        // Agregamos boton configuracion.
        panelMenuLateral.add(crearContenedorBotonMenu(btnMenuConfiguracion));
        // Agregamos espacio flexible para empujar el texto final abajo.
        panelMenuLateral.add(Box.createVerticalGlue());

        //Materia
        JLabel lblEntrega = new JLabel("Organizacipon de datos");
        // Le damos un color gris azulado.
        lblEntrega.setForeground(new Color(148, 163, 184));
        // Le damos tamano pequeno.
        lblEntrega.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // Centramos la etiqueta en el panel.
        lblEntrega.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Agregamos la etiqueta al final.
        panelMenuLateral.add(lblEntrega);

        // Regresamos el panel ya armado.
        return panelMenuLateral;
    }

    /*
     Este metodo crea la zona grande del lado derecho donde se verá todo
    */
    private JPanel crearPanelTrabajo() {
        // Creamos el panel donde ira el encabezado y las tarjetas.
        JPanel panelTrabajo = new JPanel(new BorderLayout(0, 18));
        // Le damos el color de fondo del sistema.
        panelTrabajo.setBackground(colorFondo);
        // Agregamos espacio alrededor.
        panelTrabajo.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Agregamos el encabezado superior.
        panelTrabajo.add(crearPanelEncabezado(), BorderLayout.NORTH);

        // Creamos layout de tarjetas para unificar modulos en la misma interfaz.
        layoutTarjetas = new CardLayout();
        panelTarjetas = new JPanel(layoutTarjetas);
        panelTarjetas.setOpaque(false);

        // Agregamos tarjeta de catalogo.
        panelTarjetas.add(crearVistaCatalogo(), "CATALOGO");
        // Creamos y agregamos tarjeta de movimientos.
        panelMovimientoInventario = new MovimientoInventarioPanel();
        // Conectamos callback para refrescar catalogo automaticamente al guardar movimientos.
        panelMovimientoInventario.setOnMovimientoGuardado(() -> {
            // Recargamos lista en memoria desde CSV para traer stock actualizado.
            productoDAO.recargarProductosDesdeArchivo();
            // Refrescamos la tabla visual del catalogo.
            actualizarTablaProductos();
        });
        panelTarjetas.add(panelMovimientoInventario, "MOVIMIENTOS");
        // Creamos y agregamos tarjeta de configuracion.
        panelConfiguracion = new ConfiguracionPanel();
        panelTarjetas.add(panelConfiguracion, "CONFIGURACION");

        // Mostramos catalogo por defecto.
        layoutTarjetas.show(panelTarjetas, "CATALOGO");
        panelTrabajo.add(panelTarjetas, BorderLayout.CENTER);

        // Regresamos el panel armado.
        return panelTrabajo;
    }

    /*
    Este metodo crea la parte superior de la pantalla.
    */
    private JPanel crearPanelEncabezado() {
        // Creamos un panel con distribucion izquierda y derecha.
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        // Le damos color blanco.
        panelEncabezado.setBackground(colorPanel);
        // Le colocamos borde exterior e interior.
        panelEncabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(18, 20, 18, 20)));

        // Creamos un panel para los textos del lado izquierdo.
        JPanel panelTextos = new JPanel();
        // Lo dejamos transparente para que tome el color del padre.
        panelTextos.setOpaque(false);
        // Lo acomodamos en columna.
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));

        // Creamos el titulo principal.
        lblTituloPrincipal = new JLabel("Catalogo de Productos");
        // Le damos color oscuro.
        lblTituloPrincipal.setForeground(colorTexto);
        // Le damos tamano grande.
        lblTituloPrincipal.setFont(new Font("Segoe UI", Font.BOLD, 26));

        // Creamos el texto resumen de apoyo.
        lblResumenSuperior = new JLabel("Modulo activo: Catalogo de Productos.");
        // Le damos color gris.
        lblResumenSuperior.setForeground(new Color(100, 116, 139));
        // Le damos tamano normal.
        lblResumenSuperior.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Agregamos el titulo.
        panelTextos.add(lblTituloPrincipal);
        // Agregamos una pequena separacion.
        panelTextos.add(Box.createVerticalStrut(6));
        // Agregamos el resumen.
        panelTextos.add(lblResumenSuperior);

        // Creamos el boton grande para registrar.
        btnRegistrarProducto = new JButton("Registrar Producto");
        btnRegistrarProducto.setName("btnRegistrarProducto");
        btnRegistrarProducto.setBackground(colorPrimario);
        btnRegistrarProducto.setForeground(Color.WHITE);
        btnRegistrarProducto.setFocusPainted(false);
        btnRegistrarProducto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrarProducto.setBorder(new EmptyBorder(12, 18, 12, 18));
        darTamanoBotonPrincipal(btnRegistrarProducto);
        btnRegistrarProducto.addActionListener(e -> accionBotonRegistrar());

        // Agregamos los textos al centro del encabezado y ala derecha
        panelEncabezado.add(panelTextos, BorderLayout.CENTER);
        panelEncabezado.add(btnRegistrarProducto, BorderLayout.EAST);

        // Regresamos el encabezado armado.
        return panelEncabezado;
    }



    // Este metodo crea la vista completa del catalogo.
    private JPanel crearVistaCatalogo() {
        // Creamos el panel principal del catalogo.
        JPanel panelCatalogo = new JPanel(new BorderLayout(0, 16));
        // Le damos el color de fondo.
        panelCatalogo.setBackground(colorFondo);

        // Agregamos arriba los filtros.
        panelCatalogo.add(crearPanelFiltros(), BorderLayout.NORTH);
        // Agregamos en medio la tabla.
        panelCatalogo.add(crearScrollTablaProductos(), BorderLayout.CENTER);
        // Agregamos abajo la barra de acciones.
        panelCatalogo.add(crearPanelBotonesAccion(), BorderLayout.SOUTH);

        // Regresamos la vista.
        return panelCatalogo;
    }

    // Este metodo crea una vista simple de texto para otros modulos.
    private JPanel crearVistaSimple(String titulo, String descripcion) {
        // Creamos un panel base.
        JPanel panelSimple = new JPanel(new BorderLayout());
        // Le damos color blanco.
        panelSimple.setBackground(colorPanel);
        // Le agregamos bordes.
        panelSimple.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(28, 28, 28, 28)));

        // Creamos el titulo.
        JLabel lblTitulo = new JLabel(titulo);
        // Le damos tamano grande.
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        // Le damos color oscuro.
        lblTitulo.setForeground(colorTexto);

        // Creamos la descripcion.
        JLabel lblDescripcion = new JLabel("<html><div style='width:600px;'>" + descripcion + "</div></html>");
        // Le damos tamano medio.
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        // Le damos color gris oscuro.
        lblDescripcion.setForeground(new Color(71, 85, 105));

        // Agregamos el titulo arriba.
        panelSimple.add(lblTitulo, BorderLayout.NORTH);
        // Agregamos la descripcion al centro.
        panelSimple.add(lblDescripcion, BorderLayout.CENTER);

        // Regresamos la vista.
        return panelSimple;
    }


    // SECCION DEL CATALOGO
  

    /*
    Metodo para buscar y para los filtros
    */
    public JPanel crearPanelFiltros() {
        //panel para posicionar el filtro
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panelFiltros.setBackground(colorPanel);
        panelFiltros.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createLineBorder(new Color(226, 232, 240)),new EmptyBorder(8, 8, 8, 8)));

        // texfield para buscar
        txtBuscarProducto = new JTextField(22);
        txtBuscarProducto.setName("txtBuscarProducto"); 
        darEstiloCampo(txtBuscarProducto, txtBuscarProducto.LEFT); //estilo ya hecho
        txtBuscarProducto.setToolTipText("Buscar por clave o nombre"); //texto interno del textField

        //con esto buscamos, el listener escucha mientras escibirmis 
        txtBuscarProducto.getDocument().addDocumentListener(new DocumentListener() {
            // al agregar texto
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarTablaProductos();
            }
            //cuando borro y deja de escuchar
            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarTablaProductos();
            }
            // Este metodo se ejecuta en otros cambios del documento
            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarTablaProductos();
            }
        });

  
        cmbFiltrarCategoria = new JComboBox<String>();
        cmbFiltrarCategoria.setName("cmbFiltrarCategoria");
        darEstiloCombo(cmbFiltrarCategoria);
        // si cambiamos la categoria actualiza la tabla 
        cmbFiltrarCategoria.addActionListener(e -> actualizarTablaProductos());

        // creamos el combo de estado de activos y desactivados
        cmbFiltrarEstado = new JComboBox<String>(new String[]{"Todos", "Activo", "Inactivo"});
        cmbFiltrarEstado.setName("cmbFiltrarEstado");
        darEstiloCombo(cmbFiltrarEstado); 
        cmbFiltrarEstado.addActionListener(e -> actualizarTablaProductos());// Cuando cambie la opcion actualizamos tabla

        // labels de texto para buscar
        panelFiltros.add(new JLabel("Buscar:"));
        panelFiltros.add(txtBuscarProducto); // Agregamos el campo de busqueda
        
        // agregamos una etiqueta para categoria
        panelFiltros.add(new JLabel("Categoria:"));
        panelFiltros.add(cmbFiltrarCategoria);// Agregamos el combo de categoria
        
        // Agregamos una etiqueta para estado.
        panelFiltros.add(new JLabel("Estado:"));
        panelFiltros.add(cmbFiltrarEstado);// Agregamos una etiqueta para estado

        // regresamos el panel 
        return panelFiltros;
    }
            

    // Creamos la tabla 
    private JScrollPane crearScrollTablaProductos() {
        // creamos la tabla usando el modelo
        tblCatalogoProductos = new JTable(modeloTablaProductos);
        tblCatalogoProductos.setName("tblCatalogoProductos"); //nombre de la tabla

        //todo esto es el estilo de la tabla, colores, bordes internos, 
        tblCatalogoProductos.setRowHeight(28);                                                          
        tblCatalogoProductos.setShowGrid(false);
        tblCatalogoProductos.setIntercellSpacing(new Dimension(0, 0));
        tblCatalogoProductos.setSelectionBackground(new Color(219, 234, 254));
        tblCatalogoProductos.setSelectionForeground(colorTexto);
        tblCatalogoProductos.setDefaultRenderer(Object.class, new RenderTablaProductos());

        // Jalamos el encabezado
        JTableHeader encabezadoTabla = tblCatalogoProductos.getTableHeader();
        
        
        encabezadoTabla.setReorderingAllowed(false);
        encabezadoTabla.setBackground(new Color(226, 232, 240)); // Le damos color de fondo.
        encabezadoTabla.setForeground(colorTexto);// Le damos color de texto.
        encabezadoTabla.setFont(new Font("Segoe UI", Font.BOLD, 13));// Le damos tipo de letra.

        // Scroll para la tabla
        JScrollPane scrollTabla = new JScrollPane(tblCatalogoProductos);
        scrollTabla.setName("scrollTablaProductos");
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollTabla.getViewport().setBackground(Color.WHITE);

        // Regresamos el scroll
        return scrollTabla;
    }

    // Metodo para los botones de guardar y los que están abajo
    private JPanel crearPanelBotonesAccion() {
        // Creamos un panel con BorderLayout para controlar mejor la posicion de los botones
        JPanel panelBotonesAccion = new JPanel(new BorderLayout());
        panelBotonesAccion.setOpaque(false);  // Dejamos el panel transparente para respetar el fondo general

        // Creamos un subpanel con GridLayout para que todos los botones midan igual
        JPanel panelGridBotones = new JPanel(new GridLayout(1, 3, 10, 0));
        panelGridBotones.setOpaque(false);   // Dejamos el panel transparente para respetar el fondo general.
        panelGridBotones.setPreferredSize(new Dimension(480, 42)); //tamaño 

        // Creamos el boton editar
        btnEditarProducto = crearBotonSecundario("Editar");
        btnEditarProducto.setName("btnEditarProducto");
        darTamanoBotonAccion(btnEditarProducto);
        btnEditarProducto.addActionListener(e -> accionBotonEditar()); // le asignamos su accion

        // Creamos el boton activar/desactivar
        btnCambiarEstadoProducto = crearBotonSecundario("Activar / Desactivar");
        btnCambiarEstadoProducto.setName("btnCambiarEstadoProducto");
        // tamaño con el metodo de darTamano
        darTamanoBotonAccion(btnCambiarEstadoProducto);
        btnCambiarEstadoProducto.addActionListener(e -> accionBotonCambiarEstado());// Le asignamos su accion

        // Creamos el boton eliminar
        btnEliminarProducto = crearBotonSecundario("Eliminar");
        btnEliminarProducto.setName("btnEliminarProducto");
        //Le damos el tamaño de los demás con el metodo
        darTamanoBotonAccion(btnEliminarProducto);
        btnEliminarProducto.addActionListener(e -> accionBotonEliminar());// Le asignamos su accion

        // Agregamos los botones al panel de abajo transparente para que no se muevan
        panelGridBotones.add(btnEditarProducto);
        panelGridBotones.add(btnCambiarEstadoProducto);
        panelGridBotones.add(btnEliminarProducto);

        // botones con frid a la derecha 
        panelBotonesAccion.add(panelGridBotones, BorderLayout.EAST);

        // Regresamos el panel.
        return panelBotonesAccion;
    }

    /*
     Evetos y lógica para los componentes
    */

    // Este metodo cambia la vista central segun el boton del menu.
    private void mostrarVista(String nombreVista) {
        // Si las tarjetas aun no existen no hacemos nada.
        if (layoutTarjetas == null || panelTarjetas == null) {
            return;
        }

        // Si entra a movimientos refrescamos datos del panel.
        if ("MOVIMIENTOS".equals(nombreVista) && panelMovimientoInventario != null) {
            panelMovimientoInventario.refrescarDatos();
        }
        // Si entra a configuracion refrescamos datos actuales.
        if ("CONFIGURACION".equals(nombreVista) && panelConfiguracion != null) {
            panelConfiguracion.refrescarDatos();
        }

        // Mostramos la tarjeta indicada.
        layoutTarjetas.show(panelTarjetas, nombreVista);
        // Actualizamos el estilo del menu lateral segun la opcion elegida.
        actualizarBotonesMenu(nombreVista);
        // Actualizamos el texto superior segun el modulo.
        cambiarTextoEncabezado(nombreVista);
        // Mostramos boton registrar solo en catalogo.
        if (btnRegistrarProducto != null) {
            btnRegistrarProducto.setVisible("CATALOGO".equals(nombreVista));
        }
        // Actualizamos el dashboard por si hubo cambios.
        //actualizarResumenDashboard();
    }

    // Este metodo actualiza el color de los botones del menu lateral
    private void actualizarBotonesMenu(String nombreVista) {
        // Primero dejamos todos los botones con el estilo normal.
        desmarcarBotonMenu(btnMenuDashboard);
        // Dejamos normal el boton catalogo.
        desmarcarBotonMenu(btnMenuCatalogo);
        // Dejamos normal el boton movimientos.
        desmarcarBotonMenu(btnMenuMovimientos);
        // Dejamos normal el boton analisis.
        desmarcarBotonMenu(btnMenuAnalisis);
        // Dejamos normal el boton reportes.
        desmarcarBotonMenu(btnMenuReportes);
        // Dejamos normal el boton configuracion.
        desmarcarBotonMenu(btnMenuConfiguracion);

        // Si la vista es dashboard resaltamos ese boton.
        if ("DASHBOARD".equals(nombreVista)) {
            marcarBotonMenuActivo(btnMenuDashboard);
        // Si la vista es catalogo resaltamos ese boton.
        } else if ("CATALOGO".equals(nombreVista)) {
            marcarBotonMenuActivo(btnMenuCatalogo);
        // Si la vista es movimientos resaltamos ese boton.
        } else if ("MOVIMIENTOS".equals(nombreVista)) {
            marcarBotonMenuActivo(btnMenuMovimientos);
        // Si la vista es analisis resaltamos ese boton.
        } else if ("ANALISIS".equals(nombreVista)) {
            marcarBotonMenuActivo(btnMenuAnalisis);
        // Si la vista es reportes resaltamos ese boton.
        } else if ("REPORTES".equals(nombreVista)) {
            marcarBotonMenuActivo(btnMenuReportes);
        // Si la vista es configuracion resaltamos ese boton.
        } else if ("CONFIGURACION".equals(nombreVista)) {
            marcarBotonMenuActivo(btnMenuConfiguracion);
        }
    }

    // Este metodo cambia el texto de ayuda superior.
    private void cambiarTextoEncabezado(String nombreVista) {
        // Si el modulo es dashboard mostramos una descripcion.
        if ("DASHBOARD".equals(nombreVista)) {
            if (lblTituloPrincipal != null) {
                lblTituloPrincipal.setText("Inicio / Dashboard");
            }
            lblResumenSuperior.setText("Resumen general del inventario, alertas y datos importantes.");
        // Si el modulo es catalogo mostramos otra descripcion.
        } else if ("CATALOGO".equals(nombreVista)) {
            if (lblTituloPrincipal != null) {
                lblTituloPrincipal.setText("Catalogo de Productos");
            }
            lblResumenSuperior.setText("Usa esta pantalla base para conectar tus metodos de registro, consulta y edicion.");
        // Si el modulo es movimientos mostramos descripcion de captura.
        } else if ("MOVIMIENTOS".equals(nombreVista)) {
            if (lblTituloPrincipal != null) {
                lblTituloPrincipal.setText("Movimientos de Inventario");
            }
            lblResumenSuperior.setText("Registra entradas, salidas y ajustes en la misma interfaz del sistema.");
        // Si el modulo es configuracion mostramos su descripcion.
        } else if ("CONFIGURACION".equals(nombreVista)) {
            if (lblTituloPrincipal != null) {
                lblTituloPrincipal.setText("Configuracion");
            }
            lblResumenSuperior.setText("Registra y edita costo por pedido, costo de mantenimiento y tiempo de entrega.");
        // Si es otro modulo mostramos un mensaje general.
        } else {
            if (lblTituloPrincipal != null) {
                lblTituloPrincipal.setText("Sistema de Inventario");
            }
            lblResumenSuperior.setText("Esta vista ya esta preparada para que conectes tu logica despues.");
        }
    }

    /*
    Metodo que abre el dialog para el formulario de registrar con el boton
    */
    private void accionBotonRegistrar() {
        // Abrimos el formulario en modo registro.
        abrirFormularioProducto(null);
    }
    
    

    /*
    Metodo para le boton de editar
    */
    private void accionBotonEditar() {
        // Obtenemos el producto seleccionado en la tabla.
        Producto productoSeleccionado = obtenerProductoSeleccionado();
        // Si no hay producto seleccionado, avisamos y salimos.
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un producto de la tabla para editar.",
                    "Editar producto", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Abrimos el formulario en modo edicion.
        abrirFormularioProducto(productoSeleccionado);
    }

    // Este metodo se ejecuta al dar clic en activar o desactivar.
    private void accionBotonCambiarEstado() {
        // Obtenemos el producto seleccionado.
        Producto productoSeleccionado = obtenerProductoSeleccionado();
        // Si no hay producto seleccionado salimos.
        if (productoSeleccionado == null) {
            return;
        }

        // Definimos el texto segun el estado actual.
        String accion = productoSeleccionado.isActivo() ? "desactivar" : "activar";
        // Mostramos una ventana de confirmacion.
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Seguro que deseas " + accion + " el producto " + productoSeleccionado.getNombre() + "?",
                "Confirmar cambio de estado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        // Si el usuario acepta hacemos el cambio visual.
        if (respuesta == JOptionPane.YES_OPTION) {
            // Mandamos el cambio al DAO para mantener la logica fuera de la interfaz.
            productoDAO.cambiarEstadoProducto(productoSeleccionado);
            // Mostramos mensaje de confirmacion.
            JOptionPane.showMessageDialog(this,
                    "Cambio realizado correctamente.");
            // Refrescamos la tabla.
            actualizarTablaProductos();
            // AQUI puedes llamar tu metodo real para guardar el cambio en archivo o base de datos.
            // Ejemplo: metodos.cambiarEstado(productoSeleccionado.clave, productoSeleccionado.activo);
        }
    }

    // Metodo para el boton de eliminar
    private void accionBotonEliminar() {
        // Obtenemos el producto seleccionado.
        Producto productoSeleccionado = obtenerProductoSeleccionado();
        // Si no hay seleccion salimos.
        if (productoSeleccionado == null) {
            return;
        }

        // Mostramos confirmacion.
        int respuesta = JOptionPane.showConfirmDialog(this, "Se eliminara el producto " + productoSeleccionado.getNombre() + ". Deseas continuar?","Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        // Solo eliminamos de lo visual no del archivo
        if (respuesta == JOptionPane.YES_OPTION) {
            // Pedimos al DAO que elimine el producto
            productoDAO.eliminarProducto(productoSeleccionado);
            // Actualizamos la tabla.
            actualizarTablaProductos();
            // AQUI puedes llamar tu metodo real para borrar del archivo o base de datos.
            // Ejemplo: metodos.eliminarProducto(productoSeleccionado.clave);
        }
    }

    // Este metodo abre la ventana del modulo de movimientos.
    private void abrirModuloMovimientos() {
        // Redireccionamos a la tarjeta unificada de movimientos.
        mostrarVista("MOVIMIENTOS");
    }



    /*
    Este metodo recarga por completo la tabla 
    */
    private void actualizarTablaProductos() {
        // Vaciamos todas las filas actuales
        modeloTablaProductos.setRowCount(0);

        // Tomamos el texto del campo de busqueda.
        String textoBuscar = txtBuscarProducto == null ? "" : txtBuscarProducto.getText().trim().toLowerCase();
        // Tomamos la categoria elegida.
        String categoriaElegida = cmbFiltrarCategoria == null || cmbFiltrarCategoria.getSelectedItem() == null ? "Todas" : cmbFiltrarCategoria.getSelectedItem().toString();
        // Tomamos el estado elegido.
        String estadoElegido = cmbFiltrarEstado == null || cmbFiltrarEstado.getSelectedItem() == null ? "Todos" : cmbFiltrarEstado.getSelectedItem().toString();

        // Pedimos al DAO los productos que si cumplen filtros
        for (Producto productoActual : productoDAO.filtrarProductos(textoBuscar, categoriaElegida, estadoElegido)) {
            // Agregamos una fila con los datos del producto.
            modeloTablaProductos.addRow(new Object[]{
                productoActual.getClave(),
                productoActual.getNombre(),
                productoActual.getCategoria(),
                productoActual.getCosto(),
                productoActual.getPrecio(),
                productoActual.getStockActual(),
                productoActual.getStockMinimo(),
                productoActual.getDiasEntrega(),
                productoActual.getDemandaEstimada(),
                productoActual.isActivo() ? "Activo" : "Inactivo",
                productoDAO.calcularAlertaVisual(productoActual)
            });
        }

        // Actualizamos las opciones del combo de categorias.
        actualizarComboCategorias();
        // Actualizamos las tarjetas del dashboard.
        //actualizarResumenDashboard();
    }

    /*
     Este metodo actualiza el combo de categorias.
    */
    private void actualizarComboCategorias() {
        // Si el combo aun no existe terminamos.
        if (cmbFiltrarCategoria == null) {
            return;
        }

        // guardamos la opcion actual para no perderla
        String categoriaSeleccionada = cmbFiltrarCategoria.getSelectedItem() == null  ? "Todas" : cmbFiltrarCategoria.getSelectedItem().toString();

        // Pedimos al DAO la lista de categorias para el filtro.
        List<String> categorias = productoDAO.obtenerCategoriasParaFiltro();

        // Cargamos esas categorias al combo.
        cmbFiltrarCategoria.setModel(new DefaultComboBoxModel<String>(categorias.toArray(new String[0])));

        // Si la categoria anterior sigue existiendo la dejamos seleccionada.
        if (categorias.contains(categoriaSeleccionada)) {
            cmbFiltrarCategoria.setSelectedItem(categoriaSeleccionada);
        } else {
            // Si ya no existe dejamos la opcion Todas.
            cmbFiltrarCategoria.setSelectedItem("Todas");
        }
    }



    /*
    Este metodo obtiene el producto seleccionado en tabla.
    */
    private Producto obtenerProductoSeleccionado() {
        int filaVista = tblCatalogoProductos.getSelectedRow();
        if (filaVista == -1) {
            return null;
        }

        // Convertimos el indice visual al indice real del modelo.
        int filaModelo = tblCatalogoProductos.convertRowIndexToModel(filaVista);
        // Tomamos la clave real de esa fila.
        String claveProducto = modeloTablaProductos.getValueAt(filaModelo, 0).toString();
        // Buscamos el objeto directo en DAO para evitar desfases por orden/filtro.
        return productoDAO.buscarPorClave(claveProducto);
    }

    // 
    // DIALOGO DE PRODUCTO
    // 

    /*
     Este metodo abre la ventana de registro o edicion
    */
    private void abrirFormularioProducto(Producto productoEditar) {
        // Creamos el dialogo y le mandamos el producto si existe
        DialogoProducto dialogoProducto = new DialogoProducto(this, productoEditar);
        // Mostramos el dialogo.
        dialogoProducto.setVisible(true);
    }

    // =========================
    // METODOS DE ESTILO
    // =========================

    // Este metodo crea un boton del menu lateral.
    private JButton crearBotonMenu(String textoBoton) {
        // Creamos el boton.
        JButton botonMenu = new JButton(textoBoton);
        // Quitamos el enfoque pintado.
        botonMenu.setFocusPainted(false);
        // Ponemos texto en color blanco.
        botonMenu.setForeground(Color.WHITE);
        // Ponemos fondo oscuro.
        botonMenu.setBackground(new Color(30, 41, 59));
        // Definimos fuente.
        botonMenu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Alineamos texto a la izquierda.
        botonMenu.setHorizontalAlignment(SwingConstants.LEFT);
        // Centramos verticalmente el texto.
        botonMenu.setVerticalAlignment(SwingConstants.CENTER);
        // Colocamos margenes.
        botonMenu.setMargin(new Insets(12, 18, 12, 18));
        // Quitamos borde duro y dejamos un borde visual suave.
        botonMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                new EmptyBorder(12, 18, 12, 18)));
        // Hacemos que el boton use todo el ancho disponible del sidebar.
        botonMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        // Definimos un tamano preferido uniforme para todos los botones.
        botonMenu.setPreferredSize(new Dimension(200, 46));
        // Alineamos el boton a la izquierda dentro del BoxLayout.
        botonMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Regresamos el boton creado.
        return botonMenu;
    }

    // Este metodo crea un contenedor para que todos los botones del sidebar usen el mismo ancho.
    private JPanel crearContenedorBotonMenu(JButton botonMenu) {
        // Creamos un panel con BorderLayout para sostener el boton.
        JPanel panelContenedorBoton = new JPanel(new BorderLayout());
        // Dejamos transparente el contenedor para que se vea el fondo del sidebar.
        panelContenedorBoton.setOpaque(false);
        // Hacemos que el contenedor use todo el ancho disponible.
        panelContenedorBoton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        // Agregamos el boton al centro para que se estire correctamente.
        panelContenedorBoton.add(botonMenu, BorderLayout.CENTER);
        // Regresamos el contenedor armado.
        return panelContenedorBoton;
    }

    // Este metodo pinta un boton de menu como activo.
    private void marcarBotonMenuActivo(JButton botonMenu) {
        // Si el boton no existe no hacemos nada.
        if (botonMenu == null) {
            return;
        }
        // Ponemos el fondo azul para indicar seleccion.
        botonMenu.setBackground(colorPrimario);
        // Ponemos el borde en un tono azul mas claro.
        botonMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253)),
                new EmptyBorder(12, 18, 12, 18)));
    }

    // Este metodo deja un boton de menu en estado normal.
    private void desmarcarBotonMenu(JButton botonMenu) {
        // Si el boton no existe no hacemos nada.
        if (botonMenu == null) {
            return;
        }
        // Regresamos el fondo oscuro.
        botonMenu.setBackground(new Color(30, 41, 59));
        // Regresamos el borde oscuro suave.
        botonMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                new EmptyBorder(12, 18, 12, 18)));
    }

    // Este metodo deshabilita visualmente un boton del menu para usarlo despues.
    private void deshabilitarBotonMenu(JButton botonMenu) {
        // Si el boton no existe no hacemos nada.
        if (botonMenu == null) {
            return;
        }
        // Desactivamos el boton para que no haga nada por ahora.
        botonMenu.setEnabled(false);
        // Le dejamos un color mas apagado para que se note que aun no esta listo.
        botonMenu.setBackground(new Color(51, 65, 85));
        // Bajamos el contraste del texto para diferenciarlo del modulo activo.
        botonMenu.setForeground(new Color(148, 163, 184));
    }

    // Este metodo crea un boton secundario para acciones.
    private JButton crearBotonSecundario(String textoBoton) {
        // Creamos el boton.
        JButton botonSecundario = new JButton(textoBoton);
        // Quitamos efecto de enfoque.
        botonSecundario.setFocusPainted(false);
        // Definimos la fuente.
        botonSecundario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        // Definimos fondo blanco.
        botonSecundario.setBackground(colorPanel);
        // Definimos color de texto.
        botonSecundario.setForeground(colorTexto);
        // Definimos borde con linea y espacio interno.
        botonSecundario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(10, 14, 10, 14)));
        // Regresamos el boton.
        return botonSecundario;
    }

    // Este metodo da un tamano uniforme a los botones de accion de la tabla.
    private void darTamanoBotonAccion(JButton botonAccion) {
        // Definimos el tamano preferido para que todos se vean iguales.
        botonAccion.setPreferredSize(new Dimension(150, 40));
    }

    // Este metodo da un tamano uniforme al boton principal del encabezado.
    private void darTamanoBotonPrincipal(JButton botonPrincipal) {
        // Definimos un tamano un poco mas ancho porque es el boton principal.
        botonPrincipal.setPreferredSize(new Dimension(190, 44));
    }

    // Este metodo da un tamano uniforme a los botones del dialogo.
    private void darTamanoBotonDialogo(JButton botonDialogo) {
        // Definimos un tamano visual parejo para ambos botones.
        botonDialogo.setPreferredSize(new Dimension(140, 40));
    }

    // Este metodo aplica estilo a un campo de texto.
    private void darEstiloCampo(JTextField campoTexto, int alineacion) {
        // Definimos fuente del campo.
        campoTexto.setHorizontalAlignment(alineacion);
        
        campoTexto.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        // Ponemos fondo blanco para mejorar el contraste del texto.
        campoTexto.setBackground(Color.WHITE);
        // Ponemos color de texto oscuro para que siempre se lea bien.
        campoTexto.setForeground(colorTexto);
        // Ponemos color oscuro al cursor para que se note al escribir.
        campoTexto.setCaretColor(colorTexto);
        // Indicamos que el campo este habilitado.
        campoTexto.setEnabled(true);
        // Indicamos que el campo sea editable.
        campoTexto.setEditable(true);
        // Definimos un borde simple para no romper el area interna de escritura en Nimbus.
        campoTexto.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        // Definimos margen interno para que el texto respire sin quedar pegado.
        campoTexto.setMargin(new Insets(10, 12, 10, 12));
        // Definimos una altura mas comoda para que el texto no se corte con Nimbus.
        campoTexto.setPreferredSize(new Dimension(120, 48));
        // Definimos una altura minima para mantener buena lectura.
        campoTexto.setMinimumSize(new Dimension(120, 48));
        // Definimos algunas columnas base para que Swing calcule mejor el ancho interno.
        campoTexto.setColumns(18);
    }

    // Este metodo aplica estilo a un combo.
    private void darEstiloCombo(JComboBox<String> comboOpciones) {
        // Definimos la fuente.
        comboOpciones.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // Definimos fondo blanco.
        comboOpciones.setBackground(Color.WHITE);
    }

    // Este metodo aplica estilo al combo de categorias del formulario.
    private void darEstiloComboCategoria(JComboBox<CategoriaProducto> comboCategoria) {
        // Definimos la fuente del combo.
        comboCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Definimos fondo blanco.
        comboCategoria.setBackground(Color.WHITE);
        // Definimos color del texto.
        comboCategoria.setForeground(colorTexto);
        // Definimos un borde simple para mantener compatibilidad con Nimbus.
        comboCategoria.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        // Definimos una altura parecida a la de los campos.
        comboCategoria.setPreferredSize(new Dimension(120, 48));
        // Definimos una altura minima del combo.
        comboCategoria.setMinimumSize(new Dimension(120, 48));
    }

    // Este metodo crea una tarjeta de metrica.
    private JLabel crearTarjetaMetrica(JPanel panelContenedor, String tituloTarjeta) {
        // Creamos un panel para la tarjeta.
        JPanel panelTarjeta = new JPanel(new BorderLayout());
        // Le damos color blanco.
        panelTarjeta.setBackground(colorPanel);
        // Le colocamos bordes.
        panelTarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(16, 16, 16, 16)));

        // Creamos la etiqueta del titulo.
        JLabel lblTituloTarjeta = new JLabel(tituloTarjeta);
        // Definimos fuente del titulo.
        lblTituloTarjeta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Definimos color del titulo.
        lblTituloTarjeta.setForeground(new Color(71, 85, 105));

        // Creamos la etiqueta del valor.
        JLabel lblValorTarjeta = new JLabel("0");
        // Definimos fuente grande para el valor.
        lblValorTarjeta.setFont(new Font("Segoe UI", Font.BOLD, 28));
        // Definimos color del valor.
        lblValorTarjeta.setForeground(colorTexto);

        // Agregamos el titulo arriba.
        panelTarjeta.add(lblTituloTarjeta, BorderLayout.NORTH);
        // Agregamos el valor al centro.
        panelTarjeta.add(lblValorTarjeta, BorderLayout.CENTER);
        // Agregamos la tarjeta al contenedor recibido.
        panelContenedor.add(panelTarjeta);

        // Regresamos la etiqueta del valor para modificarla despues.
        return lblValorTarjeta;
    }

    // Este metodo crea una caja informativa usando texto normal.
    private JPanel crearCajaInformativa(String titulo, String texto) {
        // Creamos una etiqueta HTML con ancho controlado.
        JLabel lblTexto = new JLabel("<html><div style='width:300px;'>" + texto + "</div></html>");
        // Reutilizamos el metodo que recibe una etiqueta.
        return crearCajaInformativa(titulo, lblTexto);
    }

    // Este metodo crea una caja informativa usando una etiqueta ya creada.
    private JPanel crearCajaInformativa(String titulo, JLabel etiquetaContenido) {
        // Creamos el panel base.
        JPanel panelCaja = new JPanel(new BorderLayout());
        // Le damos fondo blanco.
        panelCaja.setBackground(colorPanel);
        // Le colocamos bordes.
        panelCaja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(18, 18, 18, 18)));

        // Creamos el titulo de la caja.
        JLabel lblTituloCaja = new JLabel(titulo);
        // Le damos fuente en negrita.
        lblTituloCaja.setFont(new Font("Segoe UI", Font.BOLD, 18));
        // Le damos color oscuro.
        lblTituloCaja.setForeground(colorTexto);

        // Damos estilo al contenido.
        etiquetaContenido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Damos color al contenido.
        etiquetaContenido.setForeground(new Color(71, 85, 105));

        // Agregamos el titulo arriba.
        panelCaja.add(lblTituloCaja, BorderLayout.NORTH);
        // Agregamos el contenido al centro.
        panelCaja.add(etiquetaContenido, BorderLayout.CENTER);

        // Regresamos el panel.
        return panelCaja;
    }

    // =========================
    // CLASES INTERNAS
    // =========================

    // Esta clase pinta cada fila con colores segun la alerta.
    private class RenderTablaProductos extends DefaultTableCellRenderer {

        // Este metodo se ejecuta cada vez que una celda se va a dibujar.
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                boolean tieneFoco, int fila, int columna) {
            // Llamamos al metodo original para conservar el comportamiento base.
            Component componente = super.getTableCellRendererComponent(tabla, valor, seleccionada, tieneFoco, fila, columna);

            // Si la fila esta seleccionada mantenemos el color de seleccion.
            if (seleccionada) {
                componente.setBackground(tabla.getSelectionBackground());
                componente.setForeground(tabla.getSelectionForeground());
                return componente;
            }

            // Obtenemos el estado de la fila.
            String estadoFila = tabla.getValueAt(fila, 9).toString();
            // Obtenemos la alerta de la fila.
            String alertaFila = tabla.getValueAt(fila, 10).toString();

            // Si esta inactivo pintamos gris muy claro.
            if ("Inactivo".equals(estadoFila)) {
                componente.setBackground(new Color(241, 245, 249));
            // Si esta agotado pintamos rojo claro.
            } else if ("Agotado".equals(alertaFila)) {
                componente.setBackground(colorPeligro);
            // Si tiene stock bajo pintamos amarillo.
            } else if ("Stock bajo".equals(alertaFila)) {
                componente.setBackground(colorAlerta);
            // Si tiene sobreinventario pintamos verde.
            } else if ("Sobreinventario".equals(alertaFila)) {
                componente.setBackground(colorExito);
            // Si no, lo dejamos blanco.
            } else {
                componente.setBackground(Color.WHITE);
            }

            // Definimos el color del texto.
            componente.setForeground(colorTexto);
            // Regresamos el componente ya configurado.
            return componente;
        }
    }

    // Esta clase crea la ventana de registrar o editar producto.
    private class DialogoProducto extends JDialog {

        // Producto a editar cuando el dialogo se abre en modo edicion.
        private Producto productoEditar;

        // Campo para clave del producto.
        private JTextField txtClaveProducto;
        // Campo para nombre del producto.
        private JTextField txtNombreProducto;
        // Combo para categoria del producto.
        private JComboBox<CategoriaProducto> cmbCategoriaProducto;
        // Campo para costo del producto.
        private JTextField txtCostoProducto;
        // Campo para precio del producto.
        private JTextField txtPrecioProducto;
        // Campo para stock actual del producto.
        private JTextField txtStockActualProducto;
        // Campo para stock minimo del producto.
        private JTextField txtStockMinimoProducto;
        // Campo para dias de entrega del producto.
        private JTextField txtDiasEntregaProducto;
        // Campo para demanda estimada del producto.
        private JTextField txtDemandaProducto;
        // Check para estado activo.
        private JCheckBox chkProductoActivo;

        // Boton para guardar desde el dialogo.
        private JButton btnGuardarProductoDialogo;
        // Boton para cancelar desde el dialogo.
        private JButton btnCancelarProductoDialogo;

        // Este constructor arma el dialogo.
        public DialogoProducto(Frame ventanaPadre, Producto productoRecibido) {
            // Llamamos al constructor del dialogo modal.
            super(ventanaPadre, true);
            // Guardamos el producto recibido.
            productoEditar = productoRecibido;
            // Definimos el titulo del dialogo.
            setTitle(productoEditar == null ? "Registrar producto" : "Editar producto");
            // Definimos tamano del dialogo.
            setSize(980, 720);
            // Definimos un tamano minimo para que el formulario no vuelva a cortarse.
            setMinimumSize(new Dimension(920, 680));
            // Centramos el dialogo respecto a la ventana principal.
            setLocationRelativeTo(ventanaPadre);
            // Usamos BorderLayout.
            setLayout(new BorderLayout());
            // Ponemos color de fondo.
            getContentPane().setBackground(colorFondo);
            // Agregamos el formulario en el centro.
            add(crearFormularioDialogo(), BorderLayout.CENTER);
            // Agregamos los botones abajo.
            add(crearPanelBotonesDialogo(), BorderLayout.SOUTH);
            // Cargamos datos si es edicion.
            cargarDatosEnDialogo();
        }

        // Este metodo crea el formulario del dialogo.
        private JPanel crearFormularioDialogo() {
            // Creamos el contenedor general.
            JPanel panelContenedorDialogo = new JPanel(new BorderLayout());
            // Le damos fondo.
            panelContenedorDialogo.setBackground(colorFondo);
            // Le damos borde interior.
            panelContenedorDialogo.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Creamos un panel blanco que contiene titulo y formulario.
            JPanel panelTarjetaFormulario = new JPanel(new BorderLayout(0, 18));
            // Le damos color blanco.
            panelTarjetaFormulario.setBackground(colorPanel);
            // Le agregamos bordes para que parezca tarjeta.
            panelTarjetaFormulario.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    new EmptyBorder(20, 20, 20, 20)));

            // Creamos un titulo para hacer mas clara la ventana.
            JLabel lblTituloFormulario = new JLabel("Datos del producto");
            // Le damos una fuente un poco mas grande.
            lblTituloFormulario.setFont(new Font("Segoe UI", Font.BOLD, 20));
            // Le damos color oscuro.
            lblTituloFormulario.setForeground(colorTexto);

            // Creamos una descripcion corta para orientar al usuario.
            JLabel lblDescripcionFormulario = new JLabel("Completa los campos del catalogo y despues conecta aqui tus metodos.");
            // Le damos una fuente normal.
            lblDescripcionFormulario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            // Le damos un color suave.
            lblDescripcionFormulario.setForeground(new Color(100, 116, 139));

            // Creamos un panel vertical para el encabezado del formulario.
            JPanel panelEncabezadoFormulario = new JPanel();
            // Lo dejamos transparente para que se vea la tarjeta blanca.
            panelEncabezadoFormulario.setOpaque(false);
            // Lo acomodamos en columna.
            panelEncabezadoFormulario.setLayout(new BoxLayout(panelEncabezadoFormulario, BoxLayout.Y_AXIS));
            // Agregamos el titulo.
            panelEncabezadoFormulario.add(lblTituloFormulario);
            // Agregamos una pequena separacion.
            panelEncabezadoFormulario.add(Box.createVerticalStrut(4));
            // Agregamos la descripcion.
            panelEncabezadoFormulario.add(lblDescripcionFormulario);

            // Creamos la rejilla de bloques para que el formulario se vea ordenado.
            JPanel panelFormularioDialogo = new JPanel(new GridLayout(0, 2, 20, 20));
            // Ponemos transparente la rejilla porque ya hay una tarjeta blanca.
            panelFormularioDialogo.setOpaque(false);

            // Creamos todos los campos de texto.
            txtClaveProducto = new JTextField();
            txtNombreProducto = new JTextField();
            cmbCategoriaProducto = new JComboBox<CategoriaProducto>(productoDAO.obtenerCategoriasSistema());
            txtCostoProducto = new JTextField();
            txtPrecioProducto = new JTextField();
            txtStockActualProducto = new JTextField();
            txtStockMinimoProducto = new JTextField();
            txtDiasEntregaProducto = new JTextField();
            txtDemandaProducto = new JTextField();
            chkProductoActivo = new JCheckBox("Producto activo");

            // Damos nombre a componentes para ubicar tu logica rapido.
            txtClaveProducto.setName("txtClaveProducto");
            txtNombreProducto.setName("txtNombreProducto");
            cmbCategoriaProducto.setName("cmbCategoriaProducto");
            txtCostoProducto.setName("txtCostoProducto");
            txtPrecioProducto.setName("txtPrecioProducto");
            txtStockActualProducto.setName("txtStockActualProducto");
            txtStockMinimoProducto.setName("txtStockMinimoProducto");
            txtDiasEntregaProducto.setName("txtDiasEntregaProducto");
            txtDemandaProducto.setName("txtDemandaProducto");
            chkProductoActivo.setName("chkProductoActivo");

            // Configuramos limpieza visual de campos rojos mientras el usuario escribe.
            configurarLimpiezaVisualCamposEnTiempoReal();

            // Configuramos la tecla Enter para avanzar al siguiente campo.
            configurarEnterComoTab(txtClaveProducto, txtNombreProducto);
            // Configuramos Enter para bajar de nombre a categoria.
            configurarEnterComoTab(txtNombreProducto, cmbCategoriaProducto);
            // Configuramos Enter para bajar de categoria a costo.
            configurarEnterComoTab(cmbCategoriaProducto, txtCostoProducto);
            // Configuramos Enter para bajar de costo a precio.
            configurarEnterComoTab(txtCostoProducto, txtPrecioProducto);
            // Configuramos Enter para bajar de precio a stock actual.
            configurarEnterComoTab(txtPrecioProducto, txtStockActualProducto);
            // Configuramos Enter para bajar de stock actual a stock minimo.
            configurarEnterComoTab(txtStockActualProducto, txtStockMinimoProducto);
            // Configuramos Enter para bajar de stock minimo a dias de entrega.
            configurarEnterComoTab(txtStockMinimoProducto, txtDiasEntregaProducto);
            // Configuramos Enter para bajar de dias de entrega a demanda estimada.
            configurarEnterComoTab(txtDiasEntregaProducto, txtDemandaProducto);
            // Configuramos Enter para bajar de demanda estimada a estado.
            configurarEnterComoTab(txtDemandaProducto, chkProductoActivo);

            // Agregamos cada campo como bloque independiente para que se vea mejor alineado.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Clave / Codigo", txtClaveProducto,txtClaveProducto.LEFT));
            // Agregamos el bloque del nombre.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Nombre", txtNombreProducto, txtNombreProducto.LEFT));
            // Agregamos el bloque de categoria.
            panelFormularioDialogo.add(crearBloqueComboFormulario("Categoria", cmbCategoriaProducto));
            // Agregamos el bloque del costo.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Costo", txtCostoProducto, txtCostoProducto.RIGHT));
            // Agregamos el bloque del precio.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Precio", txtPrecioProducto, txtPrecioProducto.RIGHT));
            // Agregamos el bloque del stock actual.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Stock actual", txtStockActualProducto, txtStockActualProducto.RIGHT));
            // Agregamos el bloque del stock minimo.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Stock minimo", txtStockMinimoProducto, txtStockMinimoProducto.RIGHT));
            // Agregamos el bloque de dias de entrega.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Dias de entrega", txtDiasEntregaProducto, txtDiasEntregaProducto.RIGHT));
            // Agregamos el bloque de demanda estimada.
            panelFormularioDialogo.add(crearBloqueCampoFormulario("Demanda estimada", txtDemandaProducto, txtDemandaProducto.RIGHT));
            // Agregamos el bloque del estado.
            panelFormularioDialogo.add(crearBloqueEstadoFormulario());

            // Agregamos el encabezado arriba de la tarjeta.
            panelTarjetaFormulario.add(panelEncabezadoFormulario, BorderLayout.NORTH);
            // Agregamos la rejilla de campos al centro de la tarjeta.
            panelTarjetaFormulario.add(panelFormularioDialogo, BorderLayout.CENTER);
            // Agregamos la tarjeta al contenedor principal.
            panelContenedorDialogo.add(panelTarjetaFormulario, BorderLayout.CENTER);
            // Regresamos el contenedor.
            return panelContenedorDialogo;
        }

        // Este metodo crea la barra de botones del dialogo.
        private JPanel crearPanelBotonesDialogo() {
            // Creamos panel con BorderLayout para controlar mejor los botones.
            JPanel panelBotonesDialogo = new JPanel(new BorderLayout());
            // Le damos color blanco.
            panelBotonesDialogo.setBackground(colorPanel);
            // Le ponemos una linea arriba.
            panelBotonesDialogo.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

            // Creamos un panel con rejilla para que ambos botones queden iguales.
            JPanel panelGridDialogo = new JPanel(new GridLayout(1, 2, 10, 0));
            // Dejamos transparente la rejilla.
            panelGridDialogo.setOpaque(false);
            // Definimos un ancho constante para ambos botones.
            panelGridDialogo.setPreferredSize(new Dimension(290, 58));
            // Agregamos espacio interno alrededor de la rejilla.
            panelGridDialogo.setBorder(new EmptyBorder(10, 0, 10, 0));

            // Creamos boton cancelar.
            btnCancelarProductoDialogo = crearBotonSecundario("Cancelar");
            // Le damos nombre.
            btnCancelarProductoDialogo.setName("btnCancelarProductoDialogo");
            // Le damos un tamano uniforme.
            darTamanoBotonDialogo(btnCancelarProductoDialogo);
            // Al hacer clic se cierra la ventana.
            btnCancelarProductoDialogo.addActionListener(e -> dispose());

            // Creamos boton guardar.
            btnGuardarProductoDialogo = new JButton(productoEditar == null ? "Guardar" : "Actualizar");
            // Le damos nombre.
            btnGuardarProductoDialogo.setName("btnGuardarProductoDialogo");
            // Le damos estilo azul.
            btnGuardarProductoDialogo.setFocusPainted(false);
            btnGuardarProductoDialogo.setBackground(colorPrimario);
            btnGuardarProductoDialogo.setForeground(Color.WHITE);
            btnGuardarProductoDialogo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnGuardarProductoDialogo.setBorder(new EmptyBorder(10, 16, 10, 16));
            // Le damos un tamano uniforme.
            darTamanoBotonDialogo(btnGuardarProductoDialogo);
            // Al hacer clic guardamos visualmente.
            btnGuardarProductoDialogo.addActionListener(e -> accionGuardarDialogo());
            // Configuramos Enter directo en el boton guardar.
            configurarEnterEnGuardar();
            // Marcamos guardar como boton por defecto del dialogo.
            getRootPane().setDefaultButton(btnGuardarProductoDialogo);

            // Configuramos Enter para pasar del estado al boton guardar.
            configurarEnterComoTab(chkProductoActivo, btnGuardarProductoDialogo);

            // Agregamos los botones al panel de rejilla.
            panelGridDialogo.add(btnCancelarProductoDialogo);
            // Agregamos el boton guardar a la rejilla.
            panelGridDialogo.add(btnGuardarProductoDialogo);

            // Colocamos la rejilla de botones al lado derecho.
            panelBotonesDialogo.add(panelGridDialogo, BorderLayout.EAST);

            // Regresamos el panel.
            return panelBotonesDialogo;
        }

        // Este metodo crea un bloque visual con etiqueta arriba y campo abajo.
        private JPanel crearBloqueCampoFormulario(String textoEtiqueta, JTextField campoTexto, int alineacion) {
            // Creamos un panel para agrupar etiqueta y campo.
            JPanel panelBloqueCampo = new JPanel(new BorderLayout(0, 8));
            // Lo dejamos transparente para que se vea la tarjeta blanca.
            panelBloqueCampo.setOpaque(false);
            // Aplicamos estilo al campo.
            darEstiloCampo(campoTexto, alineacion);
            // Agregamos la etiqueta arriba.
            panelBloqueCampo.add(crearEtiquetaFormulario(textoEtiqueta), BorderLayout.NORTH);
            // Agregamos el campo debajo.
            panelBloqueCampo.add(campoTexto, BorderLayout.CENTER);
            // Dejamos una altura uniforme mas grande para que texto y campo respiren mejor.
            panelBloqueCampo.setPreferredSize(new Dimension(220, 96));
            // Definimos altura minima del bloque para evitar recortes.
            panelBloqueCampo.setMinimumSize(new Dimension(220, 96));
            // Regresamos el bloque completo.
            return panelBloqueCampo;
        }

        // Este metodo crea un bloque visual con etiqueta arriba y combo abajo.
        private JPanel crearBloqueComboFormulario(String textoEtiqueta, JComboBox<CategoriaProducto> comboCategoria) {
            // Creamos un panel para agrupar etiqueta y combo.
            JPanel panelBloqueCombo = new JPanel(new BorderLayout(0, 8));
            // Lo dejamos transparente para que se vea la tarjeta blanca.
            panelBloqueCombo.setOpaque(false);
            // Aplicamos estilo al combo.
            darEstiloComboCategoria(comboCategoria);
            // Agregamos la etiqueta arriba.
            panelBloqueCombo.add(crearEtiquetaFormulario(textoEtiqueta), BorderLayout.NORTH);
            // Agregamos el combo debajo.
            panelBloqueCombo.add(comboCategoria, BorderLayout.CENTER);
            // Dejamos una altura uniforme como los demas bloques.
            panelBloqueCombo.setPreferredSize(new Dimension(220, 96));
            // Definimos altura minima del bloque.
            panelBloqueCombo.setMinimumSize(new Dimension(220, 96));
            // Regresamos el bloque armado.
            return panelBloqueCombo;
        }

        // Este metodo crea el bloque visual del estado del producto.
        private JPanel crearBloqueEstadoFormulario() {
            // Creamos un panel para agrupar la etiqueta y el check.
            JPanel panelBloqueEstado = new JPanel(new BorderLayout(0, 6));
            // Lo dejamos transparente.
            panelBloqueEstado.setOpaque(false);

            // Creamos una tarjeta interior para que el check no se vea suelto.
            JPanel panelInteriorEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            // Le damos color blanco.
            panelInteriorEstado.setBackground(Color.WHITE);
            // Le colocamos borde suave.
            panelInteriorEstado.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

            // Hacemos transparente el checkbox.
            chkProductoActivo.setOpaque(false);
            // Le damos fuente.
            chkProductoActivo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            // Agregamos el checkbox dentro de la tarjeta interior.
            panelInteriorEstado.add(chkProductoActivo);

            // Agregamos la etiqueta arriba.
            panelBloqueEstado.add(crearEtiquetaFormulario("Estado"), BorderLayout.NORTH);
            // Agregamos la tarjeta del check al centro.
            panelBloqueEstado.add(panelInteriorEstado, BorderLayout.CENTER);
            // Dejamos la misma altura visual que los demas bloques.
            panelBloqueEstado.setPreferredSize(new Dimension(220, 96));
            // Definimos altura minima del bloque de estado.
            panelBloqueEstado.setMinimumSize(new Dimension(220, 96));
            // Regresamos el bloque ya armado.
            return panelBloqueEstado;
        }

        // Este metodo crea una etiqueta para el formulario.
        private JLabel crearEtiquetaFormulario(String textoEtiqueta) {
            // Creamos la etiqueta.
            JLabel etiquetaFormulario = new JLabel(textoEtiqueta);
            // Le damos fuente en negrita.
            etiquetaFormulario.setFont(new Font("Segoe UI", Font.BOLD, 13));
            // Le damos color oscuro.
            etiquetaFormulario.setForeground(colorTexto);
            // Regresamos la etiqueta.
            return etiquetaFormulario;
        }

        // Este metodo llena el formulario
        private void cargarDatosEnDialogo() {
            // Si no estamos editando dejamos el check activo por defecto.
            if (productoEditar == null) {
                chkProductoActivo.setSelected(true);
                txtClaveProducto.requestFocusInWindow();
                return;
            }

            // Colocamos los datos del producto en cada campo.
            txtClaveProducto.setText(productoEditar.getClave());
            txtNombreProducto.setText(productoEditar.getNombre());
            cmbCategoriaProducto.setSelectedItem(productoDAO.buscarCategoriaPorDescripcion(productoEditar.getCategoria()));
            txtCostoProducto.setText(productoEditar.getCosto());
            txtPrecioProducto.setText(productoEditar.getPrecio());
            txtStockActualProducto.setText(productoEditar.getStockActual());
            txtStockMinimoProducto.setText(productoEditar.getStockMinimo());
            txtDiasEntregaProducto.setText(productoEditar.getDiasEntrega());
            txtDemandaProducto.setText(productoEditar.getDemandaEstimada());
            chkProductoActivo.setSelected(productoEditar.isActivo());
        }

        // metodo que controla el dialogo de guardar y acepta el guardado despues de validarlo 
        private void accionGuardarDialogo() {
            // estilo normal
            limpiarErroresFormulario(); //llamamos al metodo que limpia los campos de rojo

            //sacamos el texto de cada textField
            String clave = txtClaveProducto.getText().trim();
            String nombre = txtNombreProducto.getText().trim();
            CategoriaProducto categoriaSeleccionada = (CategoriaProducto) cmbCategoriaProducto.getSelectedItem(); //comboBOx
            String codigoCategoria = categoriaSeleccionada.getCodigo();
            String categoria = categoriaSeleccionada.getDescripcion();
            String costo = txtCostoProducto.getText().trim();
            String precio = txtPrecioProducto.getText().trim();
            String stockActual = txtStockActualProducto.getText().trim();
            String stockMinimo = txtStockMinimoProducto.getText().trim();
            String diasEntrega = txtDiasEntregaProducto.getText().trim();
            String demanda = txtDemandaProducto.getText().trim();
            boolean activo = chkProductoActivo.isSelected();

            // validamos que no esten vacios
            JComponent primerCampoInvalido = validarCamposVacios(clave, nombre, costo, precio, stockActual, stockMinimo, diasEntrega, demanda);

            
            if (primerCampoInvalido != null) { //si hay campos avisa
                JOptionPane.showMessageDialog(this,
                        "No puede haber campos vacios.",
                        "Campos Vacios", JOptionPane.WARNING_MESSAGE);
                primerCampoInvalido.requestFocusInWindow(); //no necesitamos volver a poner el mouse, lo hace solo el sistema
                return;
            }

            // que no existan comas
            JComponent campoConComa = validarCamposSinComa();

            
            if (campoConComa != null) { // si hay lazamos esto
                JOptionPane.showMessageDialog(this,
                        "No se permiten comas dentro de los campos del formulario.",
                        "Entrada incorrecta", JOptionPane.WARNING_MESSAGE);
                campoConComa.requestFocusInWindow();
                return;
            }

            // Revisamos si costo es mayor o igual a precio para mostrar advertencia y no cerrar formulario.
            boolean costoMayorOIgualPrecio = esCostoMayorOIgualPrecio(costo, precio);
            if (costoMayorOIgualPrecio) {
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "El costo es mayor o igual al precio. ¿Deseas guardar de todos modos?",
                        "Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (respuesta != JOptionPane.YES_OPTION) {
                    txtPrecioProducto.requestFocusInWindow();
                    return;
                }
            }

            Producto productoFormulario = new Producto(clave, codigoCategoria, categoria, nombre, costo, precio, stockActual, stockMinimo, diasEntrega, demanda, activo);
            try {
                // enviamos el producto guardado a dao
                productoDAO.guardarProducto(productoFormulario, productoEditar);

                // actualizamso tabla de productos
                actualizarTablaProductos();
                //cerramos el formulario
                dispose();
            } catch (IllegalArgumentException ex) {
                // si el dao detecta problema avisa
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Validacion", JOptionPane.WARNING_MESSAGE);
            }
        }

        // metodo para volver hacer los campos blancos con el metodo de restaurar
        private void limpiarErroresFormulario() {
            restaurarCampoNormal(txtClaveProducto);
            restaurarCampoNormal(txtNombreProducto);
            restaurarCampoNormal(txtCostoProducto);
            restaurarCampoNormal(txtPrecioProducto);
            restaurarCampoNormal(txtStockActualProducto);
            restaurarCampoNormal(txtStockMinimoProducto);
            restaurarCampoNormal(txtDiasEntregaProducto);
            restaurarCampoNormal(txtDemandaProducto);
        }

        /*
        Metodo para validad que no guarde campos vacios, 
        */
        private JComponent validarCamposVacios(String clave, String nombre, String costo, String precio, String stockActual, String stockMinimo, String diasEntrega, String demanda) {
            
            JComponent primerCampoInvalido = null;

            if (clave.isEmpty()) {
                marcarCampoConError(txtClaveProducto);
                primerCampoInvalido = txtClaveProducto;
            }
            if (nombre.isEmpty()) {
                marcarCampoConError(txtNombreProducto);
                if (primerCampoInvalido == null) {
                    primerCampoInvalido = txtNombreProducto;
                }
            }
            if (costo.isEmpty()) {
                marcarCampoConError(txtCostoProducto);
                if (primerCampoInvalido == null) {
                    primerCampoInvalido = txtCostoProducto;
                }
            }
            if (precio.isEmpty()) {
                marcarCampoConError(txtPrecioProducto);
                if (primerCampoInvalido == null) {
                    primerCampoInvalido = txtPrecioProducto;
                }
            }
            if (stockActual.isEmpty()) {
                marcarCampoConError(txtStockActualProducto);
                if (primerCampoInvalido == null) {
                    primerCampoInvalido = txtStockActualProducto;
                }
            }
            if (stockMinimo.isEmpty()) {
                marcarCampoConError(txtStockMinimoProducto);
                if (primerCampoInvalido == null) {
                    primerCampoInvalido = txtStockMinimoProducto;
                }
            }
            if (diasEntrega.isEmpty()) {
                marcarCampoConError(txtDiasEntregaProducto);
                if (primerCampoInvalido == null) {
                    primerCampoInvalido = txtDiasEntregaProducto;
                }
            }
            if (demanda.isEmpty()) {
                marcarCampoConError(txtDemandaProducto);
                if (primerCampoInvalido == null) {
                    primerCampoInvalido = txtDemandaProducto;
                }
            }

            return primerCampoInvalido;
        }

        /*
        metodo para validad que no tengan coma los campos
        */
        private JComponent validarCamposSinComa() {
            if (txtClaveProducto.getText().contains(",")) {
                marcarCampoConError(txtClaveProducto);
                return txtClaveProducto;
            }
            if (txtNombreProducto.getText().contains(",")) {
                marcarCampoConError(txtNombreProducto);
                return txtNombreProducto;
            }
            if (txtCostoProducto.getText().contains(",")) {
                marcarCampoConError(txtCostoProducto);
                return txtCostoProducto;
            }
            if (txtPrecioProducto.getText().contains(",")) {
                marcarCampoConError(txtPrecioProducto);
                return txtPrecioProducto;
            }
            if (txtStockActualProducto.getText().contains(",")) {
                marcarCampoConError(txtStockActualProducto);
                return txtStockActualProducto;
            }
            if (txtStockMinimoProducto.getText().contains(",")) {
                marcarCampoConError(txtStockMinimoProducto);
                return txtStockMinimoProducto;
            }
            if (txtDiasEntregaProducto.getText().contains(",")) {
                marcarCampoConError(txtDiasEntregaProducto);
                return txtDiasEntregaProducto;
            }
            if (txtDemandaProducto.getText().contains(",")) {
                marcarCampoConError(txtDemandaProducto);
                return txtDemandaProducto;
            }

            return null;
        }

        /*
        Metodo para saber si costo es mayor o igual que precio.
        */
        private boolean esCostoMayorOIgualPrecio(String costo, String precio) {
            try {
                double costoNumero = Double.parseDouble(costo.replace(",", "."));
                double precioNumero = Double.parseDouble(precio.replace(",", "."));
                return costoNumero >= precioNumero;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        /*
        metodo para que el campo marque error si no tiene nada
        */
        private void marcarCampoConError(JTextField campoTexto) {
            campoTexto.setBackground(colorCampoError);
            campoTexto.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69)));
        }

        /*
        metodo para volver hacer blanco el campo
        */
        private void restaurarCampoNormal(JTextField campoTexto) {
            campoTexto.setBackground(Color.WHITE);
            campoTexto.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        }

        /*
         Este metodo configura la tecla Enter para pasar al siguiente componente.
        */
        private void configurarEnterComoTab(JComponent componenteActual, JComponent componenteSiguiente) {
            componenteActual.setFocusTraversalKeysEnabled(false);
            componenteActual.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "moverSiguiente");
            componenteActual.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("TAB"), "moverSiguiente");
            componenteActual.getActionMap().put("moverSiguiente", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    componenteSiguiente.requestFocusInWindow();
                }
            });
        }

        /*
         Este metodo configura quitar color rojo en tiempo real mientras se llena el campo.
        */
        private void configurarLimpiezaVisualCamposEnTiempoReal() {
            configurarQuitarErrorAlEscribir(txtClaveProducto);
            configurarQuitarErrorAlEscribir(txtNombreProducto);
            configurarQuitarErrorAlEscribir(txtCostoProducto);
            configurarQuitarErrorAlEscribir(txtPrecioProducto);
            configurarQuitarErrorAlEscribir(txtStockActualProducto);
            configurarQuitarErrorAlEscribir(txtStockMinimoProducto);
            configurarQuitarErrorAlEscribir(txtDiasEntregaProducto);
            configurarQuitarErrorAlEscribir(txtDemandaProducto);
        }

        /*
         Este metodo escucha cambios de un campo y quita el rojo cuando ya tiene texto.
        */
        private void configurarQuitarErrorAlEscribir(JTextField campoTexto) {
            campoTexto.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    quitarErrorSiTieneContenido();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    quitarErrorSiTieneContenido();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    quitarErrorSiTieneContenido();
                }

                private void quitarErrorSiTieneContenido() {
                    if (!campoTexto.getText().trim().isEmpty()) {
                        restaurarCampoNormal(campoTexto);
                    }
                }
            });
        }

        /*
         Este metodo asegura que Enter ejecute guardar cuando el foco esta en el boton.
        */
        private void configurarEnterEnGuardar() {
            btnGuardarProductoDialogo.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "guardarConEnter");
            btnGuardarProductoDialogo.getActionMap().put("guardarConEnter", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    btnGuardarProductoDialogo.doClick();
                }
            });
        }
    }

    // =========================
    // MAIN
    // =========================

    // Metodo principal del programa.
    public static void main(String args[]) {
        // Intentamos aplicar el Look and Feel Nimbus.
        try {
            // Recorremos todos los estilos instalados.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                // Si encontramos Nimbus lo usamos.
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Si falla no hacemos nada grave y seguimos con el estilo por defecto.
            java.util.logging.Logger.getLogger(Index.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Ejecutamos la ventana en el hilo grafico de Swing.
        java.awt.EventQueue.invokeLater(new Runnable() {
            // Metodo que crea y muestra la ventana.
            @Override
            public void run() {
                // Creamos la ventana principal y la mostramos.
                new Index().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
