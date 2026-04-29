package invetario;

//categorias separadas por mejor manejo
public class CategoriaProducto {

    
    private String codigo;
    private String descripcion;

    // Este constructor crea una categoria con codigo y descripcion
    public CategoriaProducto(String codigo, String descripcion) {

        this.codigo = codigo;
        this.descripcion = descripcion;
    }


    public String getCodigo() {
        return codigo;
    }

    // Este metodo cambia el codigo de la categoria.
    public void setCodigo(String codigo) {
        // Guardamos el nuevo codigo.
        this.codigo = codigo;
    }

    // Este metodo devuelve la descripcion de la categoria.
    public String getDescripcion() {
        // Regresamos la descripcion actual.
        return descripcion;
    }

    // Este metodo cambia la descripcion de la categoria.
    public void setDescripcion(String descripcion) {
        // Guardamos la nueva descripcion.
        this.descripcion = descripcion;
    }

    // Este metodo define el texto que se muestra en el JComboBox.
    @Override
    public String toString() {
        // Mostramos solo la descripcion para que el formulario se vea limpio.
        return descripcion;
    }
}
