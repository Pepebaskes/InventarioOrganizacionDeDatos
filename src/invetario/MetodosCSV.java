package invetario;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class MetodosCSV {

    // nombre del archivo en constante para poder utilizarlo en las otras
    private static final String RUTA_ARCHIVO = "productos.csv";

    //encabezados del archivo
    private static final String[] ENCABEZADOS = {
        "codigo_producto",
        "codigo_categoria",
        "categoria",
        "nombre",
        "costo",
        "precio",
        "stock_actual",
        "stock_minimo",
        "dias_entrega",
        "demanda_estimada",
        "estado"
    };

    // Este metodo crea el archivo con encabezados si todavia no existe
    public void asegurarArchivoExiste() throws IOException {
        // Creamos el objeto File para revisar si el archivo ya existe.
        File archivoFisico = new File(RUTA_ARCHIVO);

        // si si existe, no hacemos nada
        if (archivoFisico.exists()) {
            return;
        }

        // si no existe, llenamos con una linea vacia para guardar el encabezado
        guardarProductos(new ArrayList<Producto>());
    }

    /*
    metodo para escribir los productos en el CSV
    */
    public void guardarProductos(List<Producto> listaProductos) throws IOException {
       
        File archivoFisico = new File(RUTA_ARCHIVO);

        // formato y encabezado
        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS).get();

        // Abrimos el archivo en modo escritura para reemplazar el contenido anterior.
        try (Writer writer = new FileWriter(archivoFisico, false);
             CSVPrinter printer = new CSVPrinter(writer, formato)) {

            // recorremos cada fila recibida
            for (Producto productoActual : listaProductos) {
                // imprimimos la fila actual dentro del archivo.
                printer.printRecord(
                        productoActual.getClave(),
                        productoActual.getCodigoCategoria(),
                        productoActual.getCategoria(),
                        productoActual.getNombre(),
                        productoActual.getCosto(),
                        productoActual.getPrecio(),
                        productoActual.getStockActual(),
                        productoActual.getStockMinimo(),
                        productoActual.getDiasEntrega(),
                        productoActual.getDemandaEstimada(),
                        productoActual.isActivo() ? "Activo" : "Inactivo"
                );
            }

            // Forzamos la escritura inmediata al archivo.
            printer.flush();
        }
    }

    
    /*
    metodo para leer el archivo CSV y devuelve los productos como lista de arreglo
    */
    public List<Producto> leerProductos() throws IOException {

        List<Producto> filasLeidas = new ArrayList<Producto>();// Creamos la lista donde guardaremos las filas leidas
        
        File archivoFisico = new File(RUTA_ARCHIVO); // Creamos el objeto File del archivo fisico.

        // si el archivo no existe devolvemos la lista vacia 
        if (!archivoFisico.exists()) {
            return filasLeidas;
        }

        //lectura
        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS).setSkipHeaderRecord(true).get();

        // abrimos y leemos el archivo
        try (Reader reader = new FileReader(archivoFisico);
             CSVParser parser = new CSVParser(reader, formato)) {

            // recorremos las filas para leerlas
            for (CSVRecord registro : parser) {
                
                //lo hacemos arreglo
                Producto productoActual = new Producto();
                productoActual.setClave(registro.get("codigo_producto"));
                productoActual.setCodigoCategoria(registro.get("codigo_categoria"));
                productoActual.setCategoria(registro.get("categoria"));
                productoActual.setNombre(registro.get("nombre"));
                productoActual.setCosto(registro.get("costo"));
                productoActual.setPrecio(registro.get("precio"));
                productoActual.setStockActual(registro.get("stock_actual"));
                productoActual.setStockMinimo(registro.get("stock_minimo"));
                productoActual.setDiasEntrega(registro.get("dias_entrega"));
                productoActual.setDemandaEstimada(registro.get("demanda_estimada"));
                productoActual.setActivo("Activo".equalsIgnoreCase(registro.get("estado")));

                //agregamos la nueva fila a la lista del final
                filasLeidas.add(productoActual);
            }
        }

        // regresamos las listas leidas
        return filasLeidas;
    }
    
   /**
 * Metodo para editar un producto existente.
 * Busca el producto por su codigo_producto (clave) y actualiza sus datos.
 */
    public void editarProducto(Producto productoEditado) throws IOException {
        // Cargamos todos los productos actuales del CSV a una lista en memoria
        List<Producto> listaProductos = leerProductos();
        boolean encontrado = false;

        // Buscamos el producto por su clave única
        for (int i = 0; i < listaProductos.size(); i++) {
            Producto p = listaProductos.get(i);
        
            // Comparamos las claves (asegúrate de que getClave() no sea nulo)
        if (p.getClave() != null && p.getClave().equals(productoEditado.getClave())) {
            // Reemplazamos el objeto viejo por el nuevo en esa posición
            listaProductos.set(i, productoEditado);
            encontrado = true;
            break; // Salimos del ciclo al encontrarlo
                    }
                }

         // Si se encontró y editó, sobrescribimos el archivo con la lista actualizada
        if (encontrado) {
            guardarProductos(listaProductos);
            System.out.println("Producto con clave " + productoEditado.getClave() + " actualizado correctamente.");
      } else {
            System.out.println("No se encontró el producto con la clave: " + productoEditado.getClave());
             }
        }
        }
