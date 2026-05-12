package invetario;

// Modelo del encabezado del movimiento.
public class MovimientoInventarioEncabezado {

    private String noMovimiento;
    private String nombre;
    private String domicilio;
    private String lugar;
    private String fecha;
    private String tipoMovimiento;
    private String motivo;

    public MovimientoInventarioEncabezado() {
    }

    public MovimientoInventarioEncabezado(String noMovimiento, String nombre, String domicilio, String lugar, String fecha, String tipoMovimiento, String motivo) {
        this.noMovimiento = noMovimiento;
        this.nombre = nombre;
        this.domicilio = domicilio;
        this.lugar = lugar;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.motivo = motivo;
    }

    public String getNoMovimiento() {
        return noMovimiento;
    }

    public void setNoMovimiento(String noMovimiento) {
        this.noMovimiento = noMovimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
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
}
