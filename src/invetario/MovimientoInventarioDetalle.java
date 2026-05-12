package invetario;

// Modelo del detalle por producto.
public class MovimientoInventarioDetalle {

    private String noMovimiento;
    private String codigoProducto;
    private String descripcionProducto;
    private int cantidad;
    private double precioUnitario;
    private double importe;

    public MovimientoInventarioDetalle() {
    }

    public MovimientoInventarioDetalle(String noMovimiento, String codigoProducto, String descripcionProducto, int cantidad, double precioUnitario, double importe) {
        this.noMovimiento = noMovimiento;
        this.codigoProducto = codigoProducto;
        this.descripcionProducto = descripcionProducto;
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

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
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
