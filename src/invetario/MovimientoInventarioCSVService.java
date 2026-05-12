package invetario;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

// Servicio de movimientos de inventario con archivos CSV.
public class MovimientoInventarioCSVService {

    private static final String RUTA_ENCABEZADO = "MovimientoInventarioEncabezado.csv";
    private static final String RUTA_DETALLE = "MovimientoInventarioDetalle.csv";

    private static final String[] ENCABEZADOS_MOVIMIENTO = {
        "noMovimiento", "nombre", "domicilio", "lugar", "fecha", "tipoMovimiento", "motivo"
    };

    private static final String[] ENCABEZADOS_DETALLE = {
        "noMovimiento", "codigoProducto", "descripcionProducto", "cantidad", "precioUnitario", "importe"
    };

    private final ProductoCSVService productoService;

    public MovimientoInventarioCSVService() {
        productoService = new ProductoCSVService();
        asegurarArchivosMovimiento();
    }

    // Prepara y valida un movimiento antes de guardarlo.
    public MovimientoInventarioPreparado validarYPrepararMovimiento(MovimientoInventarioEncabezado encabezado, List<MovimientoInventarioDetalle> detalles) {
        validarEncabezado(encabezado);
        validarDetalle(detalles);

        List<Producto> productosCatalogo = productoService.cargarProductos();
        List<Producto> productosActualizados = productoService.clonarProductos(productosCatalogo);
        List<String> advertenciasSobreinventario = new ArrayList<String>();

        String tipo = encabezado.getTipoMovimiento().trim().toUpperCase();

        for (MovimientoInventarioDetalle detalleActual : detalles) {
            Producto productoActual = productoService.buscarPorCodigo(detalleActual.getCodigoProducto(), productosActualizados);

            if (productoActual == null) {
                throw new IllegalArgumentException("No existe el producto con codigo: " + detalleActual.getCodigoProducto());
            }

            if (detalleActual.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0 para el producto " + detalleActual.getCodigoProducto());
            }

            if (!productoActual.isActivo() && !"AJUSTE".equals(tipo)) {
                throw new IllegalArgumentException("El producto " + detalleActual.getCodigoProducto() + " esta deshabilitado. Solo permite ajuste.");
            }

            int stockActual = productoService.convertirEnteroSeguro(productoActual.getStockActual());
            int stockMinimo = productoService.convertirEnteroSeguro(productoActual.getStockMinimo());
            int cantidadMovimiento = detalleActual.getCantidad();
            int nuevoStock = stockActual;

            if ("ENTRADA".equals(tipo)) {
                nuevoStock = stockActual + cantidadMovimiento;
                if (stockMinimo > 0 && nuevoStock > stockMinimo * 3) {
                    advertenciasSobreinventario.add("Producto " + productoActual.getClave() + " quedara con sobreinventario (" + nuevoStock + ").");
                }
            } else if ("SALIDA".equals(tipo)) {
                if (cantidadMovimiento > stockActual) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto " + productoActual.getClave() + ". Disponible: " + stockActual);
                }
                nuevoStock = stockActual - cantidadMovimiento;
            } else if ("AJUSTE".equals(tipo)) {
                if (encabezado.getMotivo().trim().isEmpty()) {
                    throw new IllegalArgumentException("El ajuste requiere un motivo obligatorio.");
                }
                // En ajuste tomamos la cantidad como nuevo stock final.
                nuevoStock = cantidadMovimiento;
            } else {
                throw new IllegalArgumentException("Tipo de movimiento no valido.");
            }

            if (nuevoStock < 0) {
                throw new IllegalArgumentException("Error de calculo de stock para el producto " + productoActual.getClave());
            }

            productoActual.setStockActual(String.valueOf(nuevoStock));

            // Aseguramos importe correcto en cada fila.
            detalleActual.setImporte(detalleActual.getCantidad() * detalleActual.getPrecioUnitario());
        }

        return new MovimientoInventarioPreparado(encabezado, detalles, productosActualizados, advertenciasSobreinventario);
    }

    // Guarda encabezado, detalle y actualiza stock.
    public void guardarMovimiento(MovimientoInventarioPreparado movimientoPreparado) {
        int siguienteNoMovimiento = generarSiguienteNoMovimiento();
        String noMovimientoTexto = String.valueOf(siguienteNoMovimiento);

        movimientoPreparado.getEncabezado().setNoMovimiento(noMovimientoTexto);

        for (MovimientoInventarioDetalle detalleActual : movimientoPreparado.getDetalles()) {
            detalleActual.setNoMovimiento(noMovimientoTexto);
            detalleActual.setImporte(detalleActual.getCantidad() * detalleActual.getPrecioUnitario());
        }

        guardarEncabezado(movimientoPreparado.getEncabezado());
        guardarDetalle(movimientoPreparado.getDetalles());
        productoService.guardarProductos(movimientoPreparado.getProductosActualizados());
    }

    // Genera el folio siguiente tomando el maximo del archivo.
    public int generarSiguienteNoMovimiento() {
        int maximo = 0;
        File archivo = new File(RUTA_ENCABEZADO);

        if (!archivo.exists()) {
            return 1;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS_MOVIMIENTO).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivo); CSVParser parser = new CSVParser(reader, formato)) {
            for (CSVRecord registro : parser) {
                String valor = registro.get("noMovimiento");
                int numero = convertirEnteroSeguro(valor);
                if (numero > maximo) {
                    maximo = numero;
                }
            }
        } catch (Exception ex) {
            return maximo + 1;
        }

        return maximo + 1;
    }

    // Lee historial de movimientos por codigo de producto.
    public List<HistorialMovimientoProductoItem> obtenerHistorialPorProducto(String codigoProducto) {
        List<HistorialMovimientoProductoItem> historial = new ArrayList<HistorialMovimientoProductoItem>();

        Map<String, MovimientoInventarioEncabezado> mapaEncabezados = cargarEncabezadosPorMovimiento();

        File archivoDetalle = new File(RUTA_DETALLE);
        if (!archivoDetalle.exists()) {
            return historial;
        }

        CSVFormat formatoDetalle = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS_DETALLE).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivoDetalle); CSVParser parser = new CSVParser(reader, formatoDetalle)) {
            for (CSVRecord registro : parser) {
                String codigoProductoArchivo = registro.get("codigoProducto");
                if (!codigoProductoArchivo.equalsIgnoreCase(codigoProducto)) {
                    continue;
                }

                String noMovimiento = registro.get("noMovimiento");
                MovimientoInventarioEncabezado encabezado = mapaEncabezados.get(noMovimiento);
                if (encabezado == null) {
                    continue;
                }

                HistorialMovimientoProductoItem item = new HistorialMovimientoProductoItem();
                item.setNoMovimiento(noMovimiento);
                item.setFecha(encabezado.getFecha());
                item.setTipoMovimiento(encabezado.getTipoMovimiento());
                item.setMotivo(encabezado.getMotivo());
                item.setCantidad(convertirEnteroSeguro(registro.get("cantidad")));
                item.setPrecioUnitario(convertirDecimalSeguro(registro.get("precioUnitario")));
                item.setImporte(convertirDecimalSeguro(registro.get("importe")));
                historial.add(item);
            }
        } catch (Exception ex) {
            return historial;
        }

        historial.sort((itemUno, itemDos) -> compararFechasDesc(itemUno.getFecha(), itemDos.getFecha()));
        return historial;
    }

    // Carga productos desde catalogo para la vista lateral.
    public List<Producto> obtenerCatalogoProductos() {
        return productoService.cargarProductos();
    }

    // Reglas de estado para la vista de stock.
    public String calcularEstadoStock(Producto producto) {
        return productoService.calcularEstadoStock(producto);
    }

    // Crea los archivos de movimiento si no existen.
    private void asegurarArchivosMovimiento() {
        asegurarArchivoConEncabezado(RUTA_ENCABEZADO, ENCABEZADOS_MOVIMIENTO);
        asegurarArchivoConEncabezado(RUTA_DETALLE, ENCABEZADOS_DETALLE);
    }

    // Valida encabezado obligatorio.
    private void validarEncabezado(MovimientoInventarioEncabezado encabezado) {
        if (encabezado.getNombre() == null || encabezado.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo Nombre es obligatorio.");
        }
        if (encabezado.getDomicilio() == null || encabezado.getDomicilio().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo Domicilio es obligatorio.");
        }
        if (encabezado.getLugar() == null || encabezado.getLugar().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo Lugar es obligatorio.");
        }
        if (encabezado.getFecha() == null || encabezado.getFecha().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo Fecha es obligatorio.");
        }
        if (encabezado.getTipoMovimiento() == null || encabezado.getTipoMovimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio.");
        }
        // El motivo solo sera obligatorio cuando el tipo de movimiento sea Ajuste.
        if ("AJUSTE".equalsIgnoreCase(encabezado.getTipoMovimiento())
                && (encabezado.getMotivo() == null || encabezado.getMotivo().trim().isEmpty())) {
            throw new IllegalArgumentException("En Ajuste, el motivo del movimiento es obligatorio.");
        }
    }

    // Valida que exista detalle.
    private void validarDetalle(List<MovimientoInventarioDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Debes agregar al menos un producto al movimiento.");
        }
    }

    // Guarda una linea de encabezado en append.
    private void guardarEncabezado(MovimientoInventarioEncabezado encabezado) {
        File archivo = new File(RUTA_ENCABEZADO);
        boolean archivoVacio = !archivo.exists() || archivo.length() == 0;

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader(ENCABEZADOS_MOVIMIENTO)
                .setSkipHeaderRecord(!archivoVacio)
                .get();

        try (Writer writer = new FileWriter(archivo, true); CSVPrinter printer = new CSVPrinter(writer, formato)) {
            printer.printRecord(
                    encabezado.getNoMovimiento(),
                    limpiarComas(encabezado.getNombre()),
                    limpiarComas(encabezado.getDomicilio()),
                    limpiarComas(encabezado.getLugar()),
                    limpiarComas(encabezado.getFecha()),
                    limpiarComas(encabezado.getTipoMovimiento()),
                    limpiarComas(encabezado.getMotivo())
            );
            printer.flush();
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo guardar el encabezado del movimiento.");
        }
    }

    // Guarda detalles en append.
    private void guardarDetalle(List<MovimientoInventarioDetalle> detalles) {
        File archivo = new File(RUTA_DETALLE);
        boolean archivoVacio = !archivo.exists() || archivo.length() == 0;

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader(ENCABEZADOS_DETALLE)
                .setSkipHeaderRecord(!archivoVacio)
                .get();

        try (Writer writer = new FileWriter(archivo, true); CSVPrinter printer = new CSVPrinter(writer, formato)) {
            for (MovimientoInventarioDetalle detalleActual : detalles) {
                printer.printRecord(
                        detalleActual.getNoMovimiento(),
                        limpiarComas(detalleActual.getCodigoProducto()),
                        limpiarComas(detalleActual.getDescripcionProducto()),
                        detalleActual.getCantidad(),
                        detalleActual.getPrecioUnitario(),
                        detalleActual.getImporte()
                );
            }
            printer.flush();
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo guardar el detalle del movimiento.");
        }
    }

    // Carga encabezados en un mapa para relacion con detalle.
    private Map<String, MovimientoInventarioEncabezado> cargarEncabezadosPorMovimiento() {
        Map<String, MovimientoInventarioEncabezado> mapa = new HashMap<String, MovimientoInventarioEncabezado>();
        File archivo = new File(RUTA_ENCABEZADO);
        if (!archivo.exists()) {
            return mapa;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS_MOVIMIENTO).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivo); CSVParser parser = new CSVParser(reader, formato)) {
            for (CSVRecord registro : parser) {
                MovimientoInventarioEncabezado encabezado = new MovimientoInventarioEncabezado();
                encabezado.setNoMovimiento(registro.get("noMovimiento"));
                encabezado.setNombre(registro.get("nombre"));
                encabezado.setDomicilio(registro.get("domicilio"));
                encabezado.setLugar(registro.get("lugar"));
                encabezado.setFecha(registro.get("fecha"));
                encabezado.setTipoMovimiento(registro.get("tipoMovimiento"));
                encabezado.setMotivo(registro.get("motivo"));
                mapa.put(encabezado.getNoMovimiento(), encabezado);
            }
        } catch (Exception ex) {
            return mapa;
        }
        return mapa;
    }

    // Asegura archivo con encabezado.
    private void asegurarArchivoConEncabezado(String rutaArchivo, String[] encabezados) {
        File archivo = new File(rutaArchivo);
        if (archivo.exists()) {
            return;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(encabezados).get();
        try (Writer writer = new FileWriter(archivo, false); CSVPrinter printer = new CSVPrinter(writer, formato)) {
            printer.flush();
        } catch (Exception ex) {
            // Si falla la creacion no detenemos la app.
        }
    }

    // Compara fechas descendente con varios formatos comunes.
    private int compararFechasDesc(String fechaUno, String fechaDos) {
        LocalDate fechaA = parsearFechaFlexible(fechaUno);
        LocalDate fechaB = parsearFechaFlexible(fechaDos);
        return fechaB.compareTo(fechaA);
    }

    // Parsea fecha con intentos.
    private LocalDate parsearFechaFlexible(String textoFecha) {
        DateTimeFormatter[] formatos = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };

        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDate.parse(textoFecha, formato);
            } catch (DateTimeParseException ex) {
                // Seguimos con el siguiente formato.
            }
        }
        return LocalDate.MIN;
    }

    // Limpia comas para evitar romper CSV.
    private String limpiarComas(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(",", " ");
    }

    private int convertirEnteroSeguro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private double convertirDecimalSeguro(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (Exception ex) {
            return 0;
        }
    }
}
