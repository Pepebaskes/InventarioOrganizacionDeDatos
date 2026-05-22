package invetario;

// Clase sencilla para formulas del modulo de analisis.
public class analisisMetodos {

    // Formula de consumo por producto:
    // CP = cantidad_salida * costo_actual
    public double calcularConsumoProducto(int cantidadSalida, double costoActual) {
        return cantidadSalida * costoActual;
    }

    // Suma para consumo total del inventario.
    public double sumarConsumoTotal(double consumoActual, double nuevoConsumo) {
        return consumoActual + nuevoConsumo;
    }

    // Porcentaje individual del producto:
    // porcentaje = CP / CT
    public double calcularPorcentajeIndividual(double consumoProducto, double consumoTotal) {
        if (consumoTotal <= 0) {
            return 0;
        }
        return consumoProducto / consumoTotal;
    }

    // Porcentaje acumulado:
    // acumulado = acumuladoAnterior + porcentajeActual
    public double calcularPorcentajeAcumulado(double acumuladoAnterior, double porcentajeActual) {
        return acumuladoAnterior + porcentajeActual;
    }

    // Clasificacion ABC segun porcentaje acumulado.
    public String clasificarABC(double porcentajeAcumulado) {
        if (porcentajeAcumulado <= 0.80) {
            return "A";
        }
        if (porcentajeAcumulado <= 0.95) {
            return "B";
        }
        return "C";
    }

    // Demanda diaria:
    // demanda_diaria = demanda_anual / 365
    public double calcularDemandaDiaria(int demandaAnual) {
        if (demandaAnual <= 0) {
            return 0;
        }
        return demandaAnual / 365.0;
    }

    // Punto de reorden:
    // punto_reorden = demanda_diaria * dias_entrega
    public double calcularPuntoReorden(int demandaAnual, int diasEntrega) {
        if (demandaAnual <= 0 || diasEntrega <= 0) {
            return 0;
        }
        return calcularDemandaDiaria(demandaAnual) * diasEntrega;
    }

    // EOQ:
    // EOQ = sqrt((2 * D * S) / H)
    public Double calcularEOQ(int demandaAnual, double costoPedido, double costoMantenimiento) {
        if (demandaAnual <= 0 || costoPedido <= 0 || costoMantenimiento <= 0) {
            return null;
        }
        return Math.sqrt((2.0 * demandaAnual * costoPedido) / costoMantenimiento);
    }
}
