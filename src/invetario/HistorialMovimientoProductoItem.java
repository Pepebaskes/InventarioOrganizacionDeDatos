package invetario;

// Fila de historial por producto.
public class HistorialMovimientoProductoItem {

    private String noMovimiento;
    private String fecha;
    private String tipoMovimiento;
    private String motivo;
    private int cantidad;
    private double precioUnitario;
    private double importe;

    public HistorialMovimientoProductoItem() {
    }

    public HistorialMovimientoProductoItem(String noMovimiento, String fecha, String tipoMovimiento, String motivo, int cantidad, double precioUnitario, double importe) {
        this.noMovimiento = noMovimiento;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.motivo = motivo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.importe = importe;
    }

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

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
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

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }
}
