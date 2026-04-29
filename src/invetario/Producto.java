package invetario;

// Clase para los productos
public class Producto {

    private String clave;
    private String codigoCategoria;
    private String categoria;
    private String nombre;
    private String costo;
    private String precio;
    private String stockActual;
    private String stockMinimo;
    private String diasEntrega;
    private String demandaEstimada;
    private boolean activo;

    //constructor vacio para el llando de los get y set
    public Producto() {
    }

    //creamos el producto con los datos
    public Producto(String clave, String codigoCategoria, String categoria, String nombre, String costo, String precio,String stockActual, String stockMinimo, String diasEntrega, String demandaEstimada, boolean activo) {
        
        this.clave = clave;
        this.codigoCategoria = codigoCategoria;
        this.categoria = categoria;
        this.nombre = nombre;
        this.costo = costo;
        this.precio = precio;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.diasEntrega = diasEntrega;
        this.demandaEstimada = demandaEstimada;
        this.activo = activo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getCodigoCategoria() {
        return codigoCategoria;
    }

    public void setCodigoCategoria(String codigoCategoria) {
        this.codigoCategoria = codigoCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCosto() {
        return costo;
    }

    public void setCosto(String costo) {
        this.costo = costo;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getStockActual() {
        return stockActual;
    }

    public void setStockActual(String stockActual) {
        this.stockActual = stockActual;
    }

    public String getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(String stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getDiasEntrega() {
        return diasEntrega;
    }

    public void setDiasEntrega(String diasEntrega) {
        this.diasEntrega = diasEntrega;
    }

    public String getDemandaEstimada() {
        return demandaEstimada;
    }

    public void setDemandaEstimada(String demandaEstimada) {
        this.demandaEstimada = demandaEstimada;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
