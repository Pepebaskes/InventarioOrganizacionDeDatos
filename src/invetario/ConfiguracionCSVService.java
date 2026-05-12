package invetario;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

// Servicio CSV para parametros del modulo de configuracion.
public class ConfiguracionCSVService {

    private static final String RUTA_ARCHIVO = "ConfiguracionParametros.csv";
    private static final String[] ENCABEZADOS = {
        "costo_pedido",
        "costo_mantenimiento",
        "tiempo_entrega"
    };

    public ConfiguracionCSVService() {
        asegurarArchivoExiste();
    }

    // Crea el archivo con encabezado si no existe.
    public final void asegurarArchivoExiste() {
        File archivo = new File(RUTA_ARCHIVO);
        if (archivo.exists()) {
            return;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS).get();
        try (Writer writer = new FileWriter(archivo, false); CSVPrinter printer = new CSVPrinter(writer, formato)) {
            printer.flush();
        } catch (Exception ex) {
            // No detenemos la app por este error.
        }
    }

    // Lee todos los registros de configuracion.
    public List<ConfiguracionParametros> leerConfiguraciones() {
        List<ConfiguracionParametros> lista = new ArrayList<ConfiguracionParametros>();
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) {
            return lista;
        }

        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS).setSkipHeaderRecord(true).get();
        try (Reader reader = new FileReader(archivo); CSVParser parser = new CSVParser(reader, formato)) {
            for (CSVRecord registro : parser) {
                ConfiguracionParametros parametros = new ConfiguracionParametros();
                parametros.setCostoPedido(parsearDouble(registro.get("costo_pedido")));
                parametros.setCostoMantenimiento(parsearDouble(registro.get("costo_mantenimiento")));
                parametros.setTiempoEntrega(parsearEntero(registro.get("tiempo_entrega")));
                lista.add(parametros);
            }
        } catch (Exception ex) {
            return new ArrayList<ConfiguracionParametros>();
        }

        return lista;
    }

    // Registra un nuevo conjunto de parametros.
    public void registrarConfiguracion(ConfiguracionParametros parametros) {
        List<ConfiguracionParametros> lista = leerConfiguraciones();

        // Evitamos duplicar registro exactamente igual.
        for (ConfiguracionParametros actual : lista) {
            if (Double.compare(actual.getCostoPedido(), parametros.getCostoPedido()) == 0
                    && Double.compare(actual.getCostoMantenimiento(), parametros.getCostoMantenimiento()) == 0
                    && actual.getTiempoEntrega() == parametros.getTiempoEntrega()) {
                throw new IllegalArgumentException("Ese registro ya existe.");
            }
        }

        lista.add(parametros);
        guardarConfiguraciones(lista);
    }

    // Actualiza un registro segun el indice seleccionado en tabla.
    public void actualizarConfiguracion(int indiceRegistro, ConfiguracionParametros parametros) {
        List<ConfiguracionParametros> lista = leerConfiguraciones();
        if (indiceRegistro < 0 || indiceRegistro >= lista.size()) {
            throw new IllegalArgumentException("No se encontro el registro a editar.");
        }

        // Evitamos duplicar contra otros registros.
        for (int i = 0; i < lista.size(); i++) {
            if (i == indiceRegistro) {
                continue;
            }
            ConfiguracionParametros actual = lista.get(i);
            if (Double.compare(actual.getCostoPedido(), parametros.getCostoPedido()) == 0
                    && Double.compare(actual.getCostoMantenimiento(), parametros.getCostoMantenimiento()) == 0
                    && actual.getTiempoEntrega() == parametros.getTiempoEntrega()) {
                throw new IllegalArgumentException("Ya existe otro registro con esos mismos valores.");
            }
        }

        lista.set(indiceRegistro, parametros);
        guardarConfiguraciones(lista);
    }

    // Guarda toda la lista con escritura segura y sin sobreponer mal.
    private void guardarConfiguraciones(List<ConfiguracionParametros> lista) {
        File archivoFinal = new File(RUTA_ARCHIVO);
        File archivoTemporal = new File(RUTA_ARCHIVO + ".tmp");
        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(ENCABEZADOS).get();

        // Escribimos primero en temporal.
        try (Writer writer = new FileWriter(archivoTemporal, false); CSVPrinter printer = new CSVPrinter(writer, formato)) {
            for (ConfiguracionParametros parametros : lista) {
                printer.printRecord(
                        parametros.getCostoPedido(),
                        parametros.getCostoMantenimiento(),
                        parametros.getTiempoEntrega()
                );
            }
            printer.flush();
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo guardar la configuracion.");
        }

        // Reemplazamos el archivo final por el temporal.
        try {
            Path origen = archivoTemporal.toPath();
            Path destino = archivoFinal.toPath();
            Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo finalizar el guardado de la configuracion.");
        }
    }

    private double parsearDouble(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (Exception ex) {
            return 0;
        }
    }

    private int parsearEntero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception ex) {
            return 0;
        }
    }
}
