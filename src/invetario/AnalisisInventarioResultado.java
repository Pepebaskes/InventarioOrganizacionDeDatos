package invetario;

import java.util.List;

// Resultado completo del reporte de analisis.
public class AnalisisInventarioResultado {

    private double consumoTotalInventario;
    private String mensajeEstado;
    private List<AnalisisInventarioItem> filasAnalisis;

    public AnalisisInventarioResultado(double consumoTotalInventario, String mensajeEstado, List<AnalisisInventarioItem> filasAnalisis) {
        this.consumoTotalInventario = consumoTotalInventario;
        this.mensajeEstado = mensajeEstado;
        this.filasAnalisis = filasAnalisis;
    }

    public double getConsumoTotalInventario() {
        return consumoTotalInventario;
    }

    public String getMensajeEstado() {
        return mensajeEstado;
    }

    public List<AnalisisInventarioItem> getFilasAnalisis() {
        return filasAnalisis;
    }
}
