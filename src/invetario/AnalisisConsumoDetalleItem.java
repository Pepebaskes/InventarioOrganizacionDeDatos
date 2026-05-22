package invetario;

// Fila de historial de consumo (solo salidas) por producto.
public class AnalisisConsumoDetalleItem {

    private String noMovimiento;
    private String fecha;
    private String motivo;
    private int cantidad;
    private double costoUnitarioActual;
    private double importeConsumo;

    public String getNoMovimiento() {
        return noMovimiento;
    }

    public void setNoMovimiento(String noMovimiento) {
        this.noMovimiento = noMovimiento;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getCostoUnitarioActual() {
        return costoUnitarioActual;
    }

    public void setCostoUnitarioActual(double costoUnitarioActual) {
        this.costoUnitarioActual = costoUnitarioActual;
    }

    public double getImporteConsumo() {
        return importeConsumo;
    }

    public void setImporteConsumo(double importeConsumo) {
        this.importeConsumo = importeConsumo;
    }
}
