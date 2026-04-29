package invetario;


import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;


public class ProductoDAO {

    
    private final MetodosCSV metodosCSV = new MetodosCSV(); //instanciamos la clase Metodos
    
    public List<Producto> listaProductos = new ArrayList<Producto>();
    
    //le agregamos las categorias
    public CategoriaProducto[] categoriasSistema = new CategoriaProducto[]{
        new CategoriaProducto("ELEC01", "Computadoras y laptops"),
        new CategoriaProducto("ELEC02", "Componentes de PC"),
        new CategoriaProducto("ELEC03", "Perifericos"),
        new CategoriaProducto("ELEC04", "Monitores"),
        new CategoriaProducto("ELEC05", "Impresoras y escaneres"),
        new CategoriaProducto("ELEC06", "Redes y conectividad"),
        new CategoriaProducto("ELEC07", "Almacenamiento"),
        new CategoriaProducto("ELEC08", "Accesorios para celulares"),
        new CategoriaProducto("ELEC09", "Smartphones y tablets"),
        new CategoriaProducto("ELEC10", "Audio y sonido"),
        new CategoriaProducto("ELEC11", "Video y entretenimiento"),
        new CategoriaProducto("ELEC12", "Energia y proteccion"),
        new CategoriaProducto("ELEC13", "Camaras y videovigilancia"),
        new CategoriaProducto("ELEC14", "Gadgets y wearables"),
        new CategoriaProducto("ELEC15", "Consumibles")
    };

    
    public ProductoDAO() {
        asegurarArchivoCatalogo();
        
        // si ya tiene datos los carga
        cargarProductosDesdeArchivo();
    }

    // lista de productos
    public List<Producto> obtenerProductos() {
        return listaProductos;
    }

    // arreglo de las categorias
    public CategoriaProducto[] obtenerCategoriasSistema() {
        return categoriasSistema;
    }

    /*
     metodo filtrar y buscar (en este caso filtramos con los combobox y con el header
    */
    public List<Producto> filtrarProductos(String textoBuscar, String categoriaElegida, String estadoElegido) {
       
        List<Producto> productosFiltrados = new ArrayList<Producto>(); //creamos lista para guardar productos

        // recorremos los productos
        for (Producto productoActual : listaProductos) {
            // Si coincide con los filtros, lo agregamos a la lista final.
            if (productoCoincideConFiltros(productoActual, textoBuscar, categoriaElegida, estadoElegido)) {
                productosFiltrados.add(productoActual);
            }
        }

        // Devolvemos la lista filtrada.
        return productosFiltrados;
    }

    /*
     Este metodo revisa si un producto coincide con los filtros
    */
    private boolean productoCoincideConFiltros(Producto productoActual, String textoBuscar, String categoriaElegida, String estadoElegido) {
        // Validamos por clave o nombre
        boolean coincideTexto = textoBuscar.isEmpty() || productoActual.getClave().toLowerCase().contains(textoBuscar)
                || productoActual.getNombre().toLowerCase().contains(textoBuscar);

        // Validamos por categoria
        boolean coincideCategoria = "Todas".equals(categoriaElegida) || productoActual.getCategoria().equalsIgnoreCase(categoriaElegida);

        // Validamos por estado
        boolean coincideEstado = "Todos".equals(estadoElegido) || ("Activo".equals(estadoElegido) && productoActual.isActivo()) 
                || ("Inactivo".equals(estadoElegido) && !productoActual.isActivo());

        // Regresamos verdadero si coincide en todo
        return coincideTexto && coincideCategoria && coincideEstado;
    }

    //lista para el comboBox de las categorias de filtro
    public List<String> obtenerCategoriasParaFiltro() {
        
        // creamos una lista de texto para el combo
        List<String> categorias = new ArrayList<String>();
        // Agregamos la opcion general.
        categorias.add("Todas");

        // recorremos productos para obtener solo categorias existentes
        for (Producto productoActual : listaProductos) {
            if (!categorias.contains(productoActual.getCategoria())) {
                categorias.add(productoActual.getCategoria());
            }
        }

        return categorias;
    }

    /*
     Este metodo busca un producto por su clave
    */
    public Producto buscarPorClave(String claveProducto) {
        // Recorremos la lista de productos.
        for (Producto productoActual : listaProductos) {
            // Si coincide la clave devolvemos el producto.
            if (productoActual.getClave().equals(claveProducto)) {
                return productoActual;
            }
        }

        // Si no se encontro devolvemos null.
        return null;
    }

    /*
     Este metodo cambia el estado de un producto y lo guarda
    */
    public void cambiarEstadoProducto(Producto productoActual) {
        // Cambiamos el estado al contrario del actual
        productoActual.setActivo(!productoActual.isActivo());
        // Guardamos cambios en el archivo
        guardarProductosEnArchivo();
    }

    /*
    Este metodo elimina un producto y guarda los cambios
    */
    public void eliminarProducto(Producto productoActual) {
        // Quitamos el producto de la lista.
        listaProductos.remove(productoActual);
        // Guardamos el archivo actualizado.
        guardarProductosEnArchivo();
    }

    /*
      Este metodo registra o actualiza un producto
        Pide un Producto y el productoEditar es por si ya existe alguno
    */
    public void guardarProducto(Producto productoFormulario, Producto productoEditar) {
        // Validamos los datos recibidos antes de guardar.
        validarProducto(productoFormulario, productoEditar);

        // clave con prefijo se ajusta
        String claveFinal = ajustarClaveSegunCategoria(productoFormulario.getClave(), productoFormulario.getCodigoCategoria());
        //guardamos la clave
        productoFormulario.setClave(claveFinal);

        // Si es un registro nuevo agregamos el producto a la lista
        if (productoEditar == null) {
            listaProductos.add(productoFormulario);
        } else {
            // si es edicion actualizamos el original
            productoEditar.setClave(productoFormulario.getClave());
            productoEditar.setCodigoCategoria(productoFormulario.getCodigoCategoria());
            productoEditar.setCategoria(productoFormulario.getCategoria());
            productoEditar.setNombre(productoFormulario.getNombre());
            productoEditar.setCosto(productoFormulario.getCosto());
            productoEditar.setPrecio(productoFormulario.getPrecio());
            productoEditar.setStockActual(productoFormulario.getStockActual());
            productoEditar.setStockMinimo(productoFormulario.getStockMinimo());
            productoEditar.setDiasEntrega(productoFormulario.getDiasEntrega());
            productoEditar.setDemandaEstimada(productoFormulario.getDemandaEstimada());
            productoEditar.setActivo(productoFormulario.isActivo());
        }

        //guardamos en el csv
        guardarProductosEnArchivo();
    }

    /*
     este metodo valida los datos del producto antes de guardarlo
    */
    private void validarProducto(Producto productoFormulario, Producto productoEditar) {
        double precio = Double.parseDouble(productoFormulario.getPrecio().replace(",", "."));
        double costo = Double.parseDouble(productoFormulario.getCosto().replace(",", "."));
        int diasEntrega = convertirTextoANumero(productoFormulario.getDiasEntrega());
        int demanda = convertirTextoANumero(productoFormulario.getDemandaEstimada());
        int stock = convertirTextoANumero(productoFormulario.getStockActual());
        int stockMinimo = convertirTextoANumero(productoFormulario.getStockMinimo());
        
        if (stockMinimo == 0){
            throw new IllegalArgumentException("El stock Minimo no puede ser 0");
        }
        
        // Validamos campos obligatorios.
        if (productoFormulario.getClave().trim().isEmpty() || productoFormulario.getNombre().trim().isEmpty() || productoFormulario.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("No pueden ir campos vacios");
        }
        
        if(precio < 0 || costo < 0){
               throw new IllegalArgumentException("No pueden ser cero ni negativs");
        }
        
        
        
        if (precio < costo) {
            int respuesta = JOptionPane.showConfirmDialog(
    null, 
           "El precio es menor o igual al costo. ¿Deseas continuar?", 
            "Advertencia", 
            JOptionPane.YES_NO_OPTION, 
           JOptionPane.WARNING_MESSAGE
                );  

       if (respuesta != JOptionPane.YES_OPTION) {
         return; // O lanza una excepción si estás en el DAO para avisar al diálogo
        }
       
        if(diasEntrega < 1){
             throw new IllegalArgumentException("Los días de entrega no pueden ser menor a 1");
        }
        
        if(demanda < 1 ){
             throw new IllegalArgumentException("La demanda no puede ser menor a 1");
        }

        // calidacion de stockminimo
        if (convertirTextoANumero(productoFormulario.getStockMinimo()) <= 0) {
            throw new IllegalArgumentException("El stock minimo no puede ser 0.");
        }

        // acmodamos la clave para que se puede ver bien.
        String claveFinal = ajustarClaveSegunCategoria(productoFormulario.getClave(), productoFormulario.getCodigoCategoria());

        // que no se repita la clave
        if (existeClaveProducto(claveFinal, productoEditar)) {
            throw new IllegalArgumentException("La clave del producto ya existe. Debe ser unica.");
        }
     }
    }

    /*
    metodo para saber si ya existe una clave
    */
    public boolean existeClaveProducto(String claveProducto, Producto productoIgnorado) {
        // Recorremos los productos actuales.
        for (Producto productoActual : listaProductos) {
            // Si coincide la clave y no es el mismo producto devolvemos verdadero.
            if (productoActual != productoIgnorado && productoActual.getClave().equalsIgnoreCase(claveProducto)) {
                return true;
            }
        }

        return false;
    }

    /*
    este metodo busca una categoria por su descripcion
    */
    public CategoriaProducto buscarCategoriaPorDescripcion(String descripcionCategoria) {
        // for para recorrer todas las categorías
        for (CategoriaProducto categoriaActual : categoriasSistema) {
            // Si coincide la descripcion, devolvemos la categoria.
            if (categoriaActual.getDescripcion().equalsIgnoreCase(descripcionCategoria)) {
                return categoriaActual;
            }
        }

        // regresa la primera si no encuentra nda
        return categoriasSistema[0];
    }

    /*
    metodo para sacamos el prefijo de las categorias
    */
    public String obtenerPrefijoCategoria(String codigoCategoria) {
        // Si el codigo no es valido devolvemos 00
        if (codigoCategoria == null || codigoCategoria.length() < 2) {
            return "00";
        }

        // regresamos los utimos dos digitos para usarlos
        return codigoCategoria.substring(codigoCategoria.length() - 2);
    }

    /*
    este metodo corrige la clave para que inicie con el prefijo de la categoria
    */
    public String ajustarClaveSegunCategoria(String claveCapturada, String codigoCategoria) {
        // Quitamos espacios extras.
        String claveLimpia = claveCapturada.trim();
        // Obtenemos el prefijo correcto.
        String prefijoCategoria = obtenerPrefijoCategoria(codigoCategoria);

        // Si la clave ya empieza con el prefijo correcto la dejamos igual.
        if (claveLimpia.startsWith(prefijoCategoria)) {
            return claveLimpia;
        }

        // Si no empieza bien, agregamos el prefijo al inicio.
        return prefijoCategoria + "-" + claveLimpia;
    }

    // Este metodo calcula la alerta visual de un producto.
    public String calcularAlertaVisual(Producto productoActual) {
        // Convertimos stock actual a entero.
        int stockActualNumero = convertirTextoANumero(productoActual.getStockActual());
        // Convertimos stock minimo a entero.
        int stockMinimoNumero = convertirTextoANumero(productoActual.getStockMinimo());

        if (stockActualNumero == 0) {
            return "Agotado";
        }

        if (stockActualNumero < stockMinimoNumero) {
            return "Stock bajo";
        }

        if (stockActualNumero > stockMinimoNumero * 3) {
            return "Sobreinventario";
        }

        return "Estable";
    }

    /*
    metodo para contar los productos activos
    */
    public int productosActivos() {
        int totalActivos = 0;

        for (Producto productoActual : listaProductos) {
            if (productoActual.isActivo()) {
                totalActivos++;
            }
        }

        return totalActivos;
    }

    // Este metodo cuenta productos con stock bajo.
    public int contarProductosBajoStock() {
        int totalBajoStock = 0;

        for (Producto productoActual : listaProductos) {
            if (convertirTextoANumero(productoActual.getStockActual()) < convertirTextoANumero(productoActual.getStockMinimo())) {
                totalBajoStock++;
            }
        }

        return totalBajoStock;
    }

    // Este metodo cuenta productos agotados.
    public int contarProductosAgotados() {
        int totalAgotados = 0;

        for (Producto productoActual : listaProductos) {
            if (convertirTextoANumero(productoActual.getStockActual()) == 0) {
                totalAgotados++;
            }
        }

        return totalAgotados;
    }

    // Este metodo convierte un texto a entero.
    public int convertirTextoANumero(String textoNumero) {
        try {
            return Integer.parseInt(textoNumero);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /*
     este metodo guarda el archivo CSV usando la clase MetodosCSV
    */
    private void guardarProductosEnArchivo() {
        try {
            metodosCSV.guardarProductos(listaProductos);
        } catch (Exception ex) {
            // Si falla el guardado no detenemos la interfaz.
        }
    }

    /*
    metodo para carga el archivo CSV usando la clase MetodosCSV
    */
    private void cargarProductosDesdeArchivo() {
        try {
            listaProductos.clear(); //evita que dupliquemos la lista
            listaProductos.addAll(metodosCSV.leerProductos());
        } catch (Exception ex) {
            listaProductos.clear();
        }
    }

    /*
    Metodo para asegurar que el archivo existe
    */
    private void asegurarArchivoCatalogo() {
        try {
            metodosCSV.asegurarArchivoExiste();
        } catch (Exception ex) {
            // Si falla la creacion no detenemos la app.
        }
    }
}
