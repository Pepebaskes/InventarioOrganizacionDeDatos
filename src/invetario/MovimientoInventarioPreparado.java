package invetario;

import java.util.List;

// Resultado de la validacion previa antes de guardar.
public class MovimientoInventarioPreparado {

    private MovimientoInventarioEncabezado encabezado;
    private List<MovimientoInventarioDetalle> detalles;
    private List<Producto> productosActualizados;
    private List<String> advertenciasSobreinventario;

    public MovimientoInventarioPreparado(MovimientoInventarioEncabezado encabezado, List<MovimientoInventarioDetalle> detalles, List<Producto> productosActualizados, List<String> advertenciasSobreinventario) {
        this.encabezado = encabezado;
        this.detalles = detalles;
        this.productosActualizados = productosActualizados;
        this.advertenciasSobreinventario = advertenciasSobreinventario;
    }

    public MovimientoInventarioEncabezado getEncabezado() {
        return encabezado;
    }

    public List<MovimientoInventarioDetalle> getDetalles() {
        return detalles;
    }

    public List<Producto> getProductosActualizados() {
        return productosActualizados;
    }

    public List<String> getAdvertenciasSobreinventario() {
        return advertenciasSobreinventario;
    }
}
