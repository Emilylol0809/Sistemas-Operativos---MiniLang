package minilang;

import java.util.*;

/**
 * Objeto que guarda el resultado de compilar un programa.
 * La fachada Compilador lo llena y tanto la consola (Main) como la interfaz
 * grafica (VentanaPrincipal) lo leen para mostrar cada etapa.
 */
public class ResultadoCompilacion {
    public String tokens = "";                              // etapa 1
    public String ast = "";                                 // etapa 2
    public List<String> erroresSemanticos = new ArrayList<>(); // etapa 3
    public String codigoIntermedio = "";                    // etapa 4
    public List<String> salida = new ArrayList<>();         // ejecucion (interprete)

    public String errorFatal = null;   // error lexico o sintactico (detiene todo)
    public boolean exito = false;      // true si llego hasta el final sin errores
}
