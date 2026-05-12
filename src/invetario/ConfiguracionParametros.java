package invetario;

// Modelo simple para parametros de configuracion.
public class ConfiguracionParametros {

    private double costoPedido;
    private double costoMantenimiento;
    private int tiempoEntrega;

    public ConfiguracionParametros() {
    }

    public ConfiguracionParametros(double costoPedido, double costoMantenimiento, int tiempoEntrega) {
        this.costoPedido = costoPedido;
        this.costoMantenimiento = costoMantenimiento;
        this.tiempoEntrega = tiempoEntrega;
    }

    public double getCostoPedido() {
        return costoPedido;
    }

    public void setCostoPedido(double costoPedido) {
        this.costoPedido = costoPedido;
    }

    public double getCostoMantenimiento() {
        return costoMantenimiento;
    }

    public void setCostoMantenimiento(double costoMantenimiento) {
        this.costoMantenimiento = costoMantenimiento;
    }

    public int getTiempoEntrega() {
        return tiempoEntrega;
    }

    public void setTiempoEntrega(int tiempoEntrega) {
        this.tiempoEntrega = tiempoEntrega;
    }
}
