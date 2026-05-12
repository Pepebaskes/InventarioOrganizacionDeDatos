package invetario;

import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JFrame;

// Ventana wrapper opcional del modulo movimientos.
public class MovimientoInventarioView extends JFrame {

    // Constructor normal.
    public MovimientoInventarioView() {
        construirVentana();
    }

    // Constructor para centrar con una ventana padre.
    public MovimientoInventarioView(Frame owner) {
        construirVentana();
        setLocationRelativeTo(owner);
    }

    // Crea la ventana y monta el panel del modulo.
    private void construirVentana() {
        setTitle("Movimientos de Inventario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1380, 820);
        setLayout(new BorderLayout());
        add(new MovimientoInventarioPanel(), BorderLayout.CENTER);
    }
}
