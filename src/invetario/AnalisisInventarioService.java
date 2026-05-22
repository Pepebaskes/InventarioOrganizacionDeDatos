package invetario;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

// Servicio de calculo del modulo de analisis de inventario.
public class AnalisisInventarioService {

    private static final String RUTA_ENCABEZADO_MOV = "MovimientoInventarioEncabezado.csv";
    private static final String RUTA_DETALLE_MOV = "MovimientoInventarioDetalle.csv";

    private static final String[] ENCABEZADOS_MOV = {
        "noMovimiento", "nombre", "domicilio", "lugar", "fecha", "tipoMovimiento", "motivo"
    };

    private static final String[] ENCABEZADOS_DET = {
        "noMovimiento", "codigoProducto", "descripcionProducto", "cantidad", "precioUnitario", "importe"
    };

    private final ProductoCSVService productoService;
    private final ConfiguracionCSVService configuracionService;
    private final analisisMetodos metodosAnalisis;

    public AnalisisInventarioService() {
        productoService = new ProductoCSVService();
        configuracionService = new ConfiguracionCSVService();
        metodosAnalisis = new analisisMetodos();
    }

    // Genera el reporte completo (solo lectura) con ABC, EOQ y reorden.
    public AnalisisInventarioResultado generarReporteAnalisis() {
        List<Producto> productos = productoService.cargarProductos();
        if (productos == null) {
            productos = new ArrayList<Producto>();
        }

        Map<String, Double> consumoPorProducto = calcularConsumoPorProductoDesdeSalidas(productos);
        double consumoTotalInventario = 0;
        for (Double valor : consumoPorProducto.values()) {
            consumoTotalInventario += valor;
        }

        ConfiguracionParametros config = obtenerConfiguracionGlobal();
        boolean hayConfiguracion = config != null;
        double costoPedidoGlobal = hayConfiguracion ? config.getCostoPedido() : 0;
        double costoMantenimientoGlobal = hayConfiguracion ? config.getCostoMantenimiento() : 0;
        int tiempoEntregaGlobal = hayConfiguracion ? config.getTiempoEntrega() : 0;

        List<AnalisisInventarioItem> filas = new ArrayList<AnalisisInventarioItem>();
        for (Producto producto : productos) {
            AnalisisInventarioItem item = new AnalisisInventarioItem();
            item.setCodigoBarras(textoSeguro(producto.getClave()));
            item.setNombreProducto(textoSeguro(producto.getNombre()));
            item.setCategoria(textoSeguro(producto.getCategoria()));

            double costoActual = obtenerCostoActual(producto);
            int stockActual = productoService.convertirEnteroSeguro(producto.getStockActual());
            int stockMinimo = productoService.convertirEnteroSeguro(producto.getStockMinimo());
            int demandaAnual = productoService.convertirEnteroSeguro(producto.getDemandaEstimada());

            double consumoProducto = consumoPorProducto.containsKey(producto.getClave()) ? consumoPorProducto.get(producto.getClave()) : 0;

            item.setCostoActual(costoActual);
            item.setStockActual(stockActual);
            item.setConsumoProducto(consumoProducto);

            // Punto de reorden global con tiempo de entrega de configuracion.
            item.setPuntoReorden(metodosAnalisis.calcularPuntoReorden(demandaAnual, tiempoEntregaGlobal));

            // EOQ: sqrt((2*D*S)/H), solo si datos validos.
            item.setEoq(metodosAnalisis.calcularEOQ(demandaAnual, costoPedidoGlobal, costoMantenimientoGlobal));

            item.setIndicadores(calcularIndicadores(stockActual, stockMinimo, item.getPuntoReorden()));
            filas.add(item);
        }

        // Orden principal por consumo descendente.
        Collections.sort(filas, Comparator.comparingDouble(AnalisisInventarioItem::getConsumoProducto).reversed());

        // Clasificacion ABC y porcentajes.
        if (consumoTotalInventario > 0) {
            double acumulado = 0;
            for (AnalisisInventarioItem item : filas) {
                double porcentaje = metodosAnalisis.calcularPorcentajeIndividual(item.getConsumoProducto(), consumoTotalInventario);
                acumulado = metodosAnalisis.calcularPorcentajeAcumulado(acumulado, porcentaje);

                item.setPorcentajeIndividual(porcentaje);
                item.setPorcentajeAcumulado(acumulado);
                item.setGrupoABC(metodosAnalisis.clasificarABC(acumulado));
            }
        } else {
            for (AnalisisInventarioItem item : filas) {
                item.setPorcentajeIndividual(0);
                item.setPorcentajeAcumulado(0);
                item.setGrupoABC("N/A");
            }
        }

        String mensajeEstado = "";
        if (consumoTotalInventario <= 0) {
            mensajeEstado = "No hay movimientos de salida suficientes para generar clasificacion ABC.";
        } else if (!hayConfiguracion) {
            mensajeEstado = "El analisis ABC se genero, pero faltan parametros globales para EOQ/Punto de reorden.";
        } else if (costoMantenimientoGlobal <= 0) {
            mensajeEstado = "Costo de mantenimiento invalido. EOQ se muestra como N/D.";
        } else {
            mensajeEstado = "Analisis calculado correctamente con movimientos de salida.";
        }

        return new AnalisisInventarioResultado(consumoTotalInventario, mensajeEstado, filas);
    }

    // Historial de consumo por producto (solo tipo SALIDA).
    public List<AnalisisConsumoDetalleItem> obtenerDetalleConsumoProducto(String codigoProducto) {
        Map<String, MovimientoCabeceraSimple> cabecerasSalida = cargarCabecerasSalida();
        List<AnalisisConsumoDetalleItem> detalle = new ArrayList<AnalisisConsumoDetalleItem>();

        List<Producto> productos = productoService.cargarProductos();
        Producto producto = productoService.buscarPorCodigo(codigoProducto, productos);
        double costoActual = producto == null ? 0 : obtenerCostoActual(producto);

        File archivoDetalle = new File(RUTA_DETALLE_MOV);
        if (!archivoDetalle.exists()) {
            return detalle;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS_DET).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivoDetalle); CSVParser parser = new CSVParser(reader, formato)) {
            for (CSVRecord registro : parser) {
                String noMovimiento = textoSeguro(registro.get("noMovimiento"));
                String codigo = textoSeguro(registro.get("codigoProducto"));

                if (!codigo.equalsIgnoreCase(codigoProducto)) {
                    continue;
                }

                MovimientoCabeceraSimple cabecera = cabecerasSalida.get(noMovimiento);
                if (cabecera == null) {
                    continue;
                }

                int cantidad = parsearEntero(textoSeguro(registro.get("cantidad")));
                AnalisisConsumoDetalleItem item = new AnalisisConsumoDetalleItem();
                item.setNoMovimiento(noMovimiento);
                item.setFecha(cabecera.fecha);
                item.setMotivo(cabecera.motivo);
                item.setCantidad(cantidad);
                item.setCostoUnitarioActual(costoActual);
                item.setImporteConsumo(cantidad * costoActual);
                detalle.add(item);
            }
        } catch (Exception ex) {
            return new ArrayList<AnalisisConsumoDetalleItem>();
        }

        // Orden descendente por fecha.
        Collections.sort(detalle, (a, b) -> compararFechasDesc(a.getFecha(), b.getFecha()));
        return detalle;
    }

    // Devuelve consumo mensual del inventario usando solo movimientos SALIDA.
    public Map<String, Double> obtenerConsumoMensualSalidas() {
        Map<String, Double> consumoMensualOrdenado = new LinkedHashMap<String, Double>();
        Map<String, MovimientoCabeceraSimple> cabecerasSalida = cargarCabecerasSalida();
        if (cabecerasSalida.isEmpty()) {
            return consumoMensualOrdenado;
        }

        List<Producto> productos = productoService.cargarProductos();
        if (productos == null) {
            productos = new ArrayList<Producto>();
        }

        File archivoDetalle = new File(RUTA_DETALLE_MOV);
        if (!archivoDetalle.exists()) {
            return consumoMensualOrdenado;
        }

        // TreeMap ordena las llaves mes de forma ascendente: yyyy-MM.
        Map<String, Double> acumuladoPorMes = new TreeMap<String, Double>();
        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS_DET).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivoDetalle); CSVParser parser = new CSVParser(reader, formato)) {
            for (CSVRecord registro : parser) {
                String noMovimiento = textoSeguro(registro.get("noMovimiento"));
                MovimientoCabeceraSimple cabecera = cabecerasSalida.get(noMovimiento);
                if (cabecera == null) {
                    continue;
                }

                String codigoProducto = textoSeguro(registro.get("codigoProducto"));
                Producto producto = productoService.buscarPorCodigo(codigoProducto, productos);
                if (producto == null) {
                    continue;
                }

                int cantidad = parsearEntero(textoSeguro(registro.get("cantidad")));
                if (cantidad <= 0) {
                    continue;
                }

                double costoActual = obtenerCostoActual(producto);
                double consumo = metodosAnalisis.calcularConsumoProducto(cantidad, costoActual);
                String mes = obtenerMesAnio(cabecera.fecha);
                double acumulado = acumuladoPorMes.containsKey(mes) ? acumuladoPorMes.get(mes) : 0;
                acumuladoPorMes.put(mes, metodosAnalisis.sumarConsumoTotal(acumulado, consumo));
            }
        } catch (Exception ex) {
            return new LinkedHashMap<String, Double>();
        }

        consumoMensualOrdenado.putAll(acumuladoPorMes);
        return consumoMensualOrdenado;
    }

    // Calcula consumo por producto solo con cabeceras SALIDA.
    private Map<String, Double> calcularConsumoPorProductoDesdeSalidas(List<Producto> productos) {
        Map<String, Double> consumoPorProducto = new HashMap<String, Double>();
        Map<String, MovimientoCabeceraSimple> cabecerasSalida = cargarCabecerasSalida();

        File archivoDetalle = new File(RUTA_DETALLE_MOV);
        if (!archivoDetalle.exists()) {
            return consumoPorProducto;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS_DET).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivoDetalle); CSVParser parser = new CSVParser(reader, formato)) {
            for (CSVRecord registro : parser) {
                String noMovimiento = textoSeguro(registro.get("noMovimiento"));
                if (!cabecerasSalida.containsKey(noMovimiento)) {
                    continue;
                }

                String codigoProducto = textoSeguro(registro.get("codigoProducto"));
                int cantidad = parsearEntero(textoSeguro(registro.get("cantidad")));
                if (cantidad <= 0) {
                    continue;
                }

                Producto producto = productoService.buscarPorCodigo(codigoProducto, productos);
                if (producto == null) {
                    continue;
                }

                double costoActual = obtenerCostoActual(producto);
                double consumoActual = metodosAnalisis.calcularConsumoProducto(cantidad, costoActual);
                double consumoAcumulado = consumoPorProducto.containsKey(codigoProducto) ? consumoPorProducto.get(codigoProducto) : 0;
                consumoPorProducto.put(codigoProducto, metodosAnalisis.sumarConsumoTotal(consumoAcumulado, consumoActual));
            }
        } catch (Exception ex) {
            return new HashMap<String, Double>();
        }

        return consumoPorProducto;
    }

    // Carga cabeceras tipo SALIDA.
    private Map<String, MovimientoCabeceraSimple> cargarCabecerasSalida() {
        Map<String, MovimientoCabeceraSimple> salida = new HashMap<String, MovimientoCabeceraSimple>();
        File archivo = new File(RUTA_ENCABEZADO_MOV);
        if (!archivo.exists()) {
            return salida;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS_MOV).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivo); CSVParser parser = new CSVParser(reader, formato)) {
            for (CSVRecord registro : parser) {
                String tipo = textoSeguro(registro.get("tipoMovimiento")).toUpperCase();
                if (!"SALIDA".equals(tipo)) {
                    continue;
                }

                MovimientoCabeceraSimple simple = new MovimientoCabeceraSimple();
                simple.noMovimiento = textoSeguro(registro.get("noMovimiento"));
                simple.fecha = textoSeguro(registro.get("fecha"));
                simple.motivo = textoSeguro(registro.get("motivo"));
                salida.put(simple.noMovimiento, simple);
            }
        } catch (Exception ex) {
            return new HashMap<String, MovimientoCabeceraSimple>();
        }
        return salida;
    }

    // Obtiene configuracion global actual.
    private ConfiguracionParametros obtenerConfiguracionGlobal() {
        List<ConfiguracionParametros> lista = configuracionService.leerConfiguraciones();
        if (lista == null || lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    private String calcularIndicadores(int stockActual, int stockMinimo, double puntoReorden) {
        List<String> indicadores = new ArrayList<String>();

        if (stockMinimo > 0 && stockActual < stockMinimo) {
            indicadores.add("STOCK BAJO");
        }
        if (stockMinimo > 0 && stockActual > (stockMinimo * 3)) {
            indicadores.add("SOBREINVENTARIO");
        }
        if (puntoReorden > 0 && puntoReorden >= stockActual) {
            indicadores.add("BAJO REORDEN");
        }

        if (indicadores.isEmpty()) {
            return "NORMAL";
        }
        return String.join(" | ", indicadores);
    }

    private double obtenerCostoActual(Producto producto) {
        double costo = productoService.convertirDecimalSeguro(producto.getCosto());
        if (costo > 0) {
            return costo;
        }
        return productoService.convertirDecimalSeguro(producto.getPrecio());
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private int parsearEntero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (Exception ex) {
            return 0;
        }
    }

    private int compararFechasDesc(String fechaUno, String fechaDos) {
        LocalDate fechaA = parsearFechaFlexible(fechaUno);
        LocalDate fechaB = parsearFechaFlexible(fechaDos);
        return fechaB.compareTo(fechaA);
    }

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
                // Intentamos con siguiente formato.
            }
        }
        return LocalDate.MIN;
    }

    // Convierte fecha a llave mensual yyyy-MM para grafica.
    private String obtenerMesAnio(String textoFecha) {
        LocalDate fecha = parsearFechaFlexible(textoFecha);
        if (LocalDate.MIN.equals(fecha)) {
            return "Sin fecha";
        }
        return fecha.getYear() + "-" + String.format("%02d", fecha.getMonthValue());
    }

    // Clase simple interna para cabeceras de salida.
    private static class MovimientoCabeceraSimple {
        String noMovimiento;
        String fecha;
        String motivo;
    }
}
