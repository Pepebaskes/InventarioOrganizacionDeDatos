package invetario;

// Fila principal del reporte de analisis.
public class AnalisisInventarioItem {

    private String grupoABC;
    private String codigoBarras;
    private String nombreProducto;
    private String categoria;
    private double costoActual;
    private int stockActual;
    private double puntoReorden;
    private Double eoq;
    private double consumoProducto;
    private double porcentajeIndividual;
    private double porcentajeAcumulado;
    private String indicadores;

    public String getGrupoABC() {
        return grupoABC;
    }

    public void setGrupoABC(String grupoABC) {
        this.grupoABC = grupoABC;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getCostoActual() {
        return costoActual;
    }

    public void setCostoActual(double costoActual) {
        this.costoActual = costoActual;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public double getPuntoReorden() {
        return puntoReorden;
    }

    public void setPuntoReorden(double puntoReorden) {
        this.puntoReorden = puntoReorden;
    }

    public Double getEoq() {
        return eoq;
    }

    public void setEoq(Double eoq) {
        this.eoq = eoq;
    }

    public double getConsumoProducto() {
        return consumoProducto;
    }

    public void setConsumoProducto(double consumoProducto) {
        this.consumoProducto = consumoProducto;
    }

    public double getPorcentajeIndividual() {
        return porcentajeIndividual;
    }

    public void setPorcentajeIndividual(double porcentajeIndividual) {
        this.porcentajeIndividual = porcentajeIndividual;
    }

    public double getPorcentajeAcumulado() {
        return porcentajeAcumulado;
    }

    public void setPorcentajeAcumulado(double porcentajeAcumulado) {
        this.porcentajeAcumulado = porcentajeAcumulado;
    }

    public String getIndicadores() {
        return indicadores;
    }

    public void setIndicadores(String indicadores) {
        this.indicadores = indicadores;
    }
}
