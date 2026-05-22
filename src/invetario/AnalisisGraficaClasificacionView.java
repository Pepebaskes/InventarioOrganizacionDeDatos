package invetario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

// Ventana para mostrar grafica comparativa por productos clasificados ABC.
public class AnalisisGraficaClasificacionView extends JDialog {

    private final DecimalFormat formatoMonto;

    public AnalisisGraficaClasificacionView(Window owner, List<AnalisisInventarioItem> filasAnalisis) {
        super(owner, "Grafica de consumo por clasificacion ABC", ModalityType.APPLICATION_MODAL);
        formatoMonto = new DecimalFormat("#,##0.00");
        construirVentana(filasAnalisis);
    }

    // Construye la ventana completa de grafica + tabla resumen.
    private void construirVentana(List<AnalisisInventarioItem> filasAnalisis) {
        setSize(1080, 640);
        setMinimumSize(new Dimension(920, 560));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        double totalConsumo = calcularTotalConsumo(filasAnalisis);
        JLabel lblTotal = new JLabel("Consumo total clasificado: " + formatoMonto.format(totalConsumo));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTotal.setForeground(new Color(194, 24, 91));
        add(lblTotal, BorderLayout.NORTH);

        ChartPanel panelGrafica = new ChartPanel(crearGraficaBarras(filasAnalisis));
        panelGrafica.setMouseWheelEnabled(true);
        panelGrafica.setPreferredSize(new Dimension(0, 480));
        panelGrafica.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(panelGrafica, BorderLayout.CENTER);

        // Escape cierra dialogo para flujo rapido con teclado.
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // Crea grafica de barras simple por clasificacion A, B y C.
    private JFreeChart crearGraficaBarras(List<AnalisisInventarioItem> filasAnalisis) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        double totalA = 0;
        double totalB = 0;
        double totalC = 0;

        if (filasAnalisis != null) {
            for (AnalisisInventarioItem fila : filasAnalisis) {
                String grupo = normalizarGrupo(fila.getGrupoABC());
                if ("A".equals(grupo)) {
                    totalA += fila.getConsumoProducto();
                } else if ("B".equals(grupo)) {
                    totalB += fila.getConsumoProducto();
                } else {
                    totalC += fila.getConsumoProducto();
                }
            }
        }

        dataset.addValue(totalA, "Clasificacion", "A");
        dataset.addValue(totalB, "Clasificacion", "B");
        dataset.addValue(totalC, "Clasificacion", "C");

        if (totalA == 0 && totalB == 0 && totalC == 0) {
            dataset.addValue(0, "Clasificacion", "Sin datos");
        }

        JFreeChart chart = ChartFactory.createBarChart("Consumo por clasificacion ABC", "Clasificacion", "Consumo", dataset, PlotOrientation.VERTICAL, false, true, false);

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setRangeGridlinePaint(new Color(203, 213, 225));

        // Colores por serie de clasificacion.
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(37, 99, 235));   // A
        renderer.setSeriesPaint(1, new Color(249, 115, 22));  // B
        renderer.setSeriesPaint(2, new Color(107, 114, 128)); // C
        renderer.setMaximumBarWidth(0.12);

        return chart;
    }

    // Calcula total general de consumo.
    private double calcularTotalConsumo(List<AnalisisInventarioItem> filasAnalisis) {
        double total = 0;
        if (filasAnalisis == null) {
            return total;
        }
        for (AnalisisInventarioItem fila : filasAnalisis) {
            total += fila.getConsumoProducto();
        }
        return total;
    }

    // Normaliza grupo ABC para no romper la grafica.
    private String normalizarGrupo(String grupo) {
        if (grupo == null) {
            return "C";
        }
        String valor = grupo.trim().toUpperCase();
        if ("A".equals(valor) || "B".equals(valor) || "C".equals(valor)) {
            return valor;
        }
        return "C";
    }

}
