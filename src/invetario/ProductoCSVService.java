package invetario;

import java.util.ArrayList;
import java.util.List;

// Servicio para operaciones de productos en CSV.
public class ProductoCSVService {

    private final MetodosCSV metodosCSV;

    public ProductoCSVService() {
        metodosCSV = new MetodosCSV();
        asegurarArchivoCatalogo();
    }

    // Carga todos los productos del catalogo.
    public List<Producto> cargarProductos() {
        try {
            return metodosCSV.leerProductos();
        } catch (Exception ex) {
            return new ArrayList<Producto>();
        }
    }

    // Guarda todos los productos en el catalogo.
    public void guardarProductos(List<Producto> productos) {
        try {
            metodosCSV.guardarProductos(productos);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo guardar el catalogo de productos.");
        }
    }

    // Busca por codigo de producto.
    public Producto buscarPorCodigo(String codigoProducto, List<Producto> productos) {
        for (Producto productoActual : productos) {
            if (productoActual.getClave() != null && productoActual.getClave().equalsIgnoreCase(codigoProducto)) {
                return productoActual;
            }
        }
        return null;
    }

    // Devuelve una copia para trabajar sin mutar la lista original.
    public List<Producto> clonarProductos(List<Producto> productos) {
        List<Producto> copia = new ArrayList<Producto>();
        for (Producto productoActual : productos) {
            Producto clon = new Producto();
            clon.setClave(productoActual.getClave());
            clon.setCodigoCategoria(productoActual.getCodigoCategoria());
            clon.setCategoria(productoActual.getCategoria());
            clon.setNombre(productoActual.getNombre());
            clon.setCosto(productoActual.getCosto());
            clon.setPrecio(productoActual.getPrecio());
            clon.setStockActual(productoActual.getStockActual());
            clon.setStockMinimo(productoActual.getStockMinimo());
            clon.setDiasEntrega(productoActual.getDiasEntrega());
            clon.setDemandaEstimada(productoActual.getDemandaEstimada());
            clon.setActivo(productoActual.isActivo());
            copia.add(clon);
        }
        return copia;
    }

    // Convierte texto a entero de forma segura.
    public int convertirEnteroSeguro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    // Convierte texto a decimal de forma segura.
    public double convertirDecimalSeguro(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (Exception ex) {
            return 0;
        }
    }

    // Calcula estado visual de stock.
    public String calcularEstadoStock(Producto producto) {
        int stockActual = convertirEnteroSeguro(producto.getStockActual());
        int stockMinimo = convertirEnteroSeguro(producto.getStockMinimo());

        if (stockActual == 0) {
            return "AGOTADO";
        }
        if (stockMinimo > stockActual) {
            return "STOCK BAJO";
        }
        if (stockActual > stockMinimo * 3) {
            return "SOBREINVENTARIO";
        }
        return "NORMAL";
    }

    // Asegura que existe el archivo del catalogo.
    private void asegurarArchivoCatalogo() {
        try {
            metodosCSV.asegurarArchivoExiste();
        } catch (Exception ex) {
            // Si falla la creacion, no detenemos la app.
        }
    }
}
