package minilang;

import java.util.*;
import minilang.lexico.*;
import minilang.sintactico.*;
import minilang.semantico.AnalizadorSemantico;
import minilang.generador.GeneradorCodigo;
import minilang.interprete.Interprete;

/**
 * FACHADA del compilador.
 * Ejecuta las etapas en orden y junta los resultados en un
 * ResultadoCompilacion. Asi, tanto la consola como la interfaz grafica usan
 * la misma logica sin repetir codigo.
 *
 * Flujo: lexico -> sintactico -> semantico -> codigo intermedio -> ejecucion.
 * Si una etapa falla, se detiene y se reporta el error.
 */
public class Compilador {

    public ResultadoCompilacion compilar(String fuente) {
        ResultadoCompilacion r = new ResultadoCompilacion();
        try {
            // 1. Analisis lexico
            List<Token> tokens = new Lexer(fuente).escanear();
            StringBuilder sb = new StringBuilder();
            for (Token t : tokens) sb.append(t).append("\n");
            r.tokens = sb.toString();

            // 2. Analisis sintactico
            List<Ast.Stmt> programa = new Parser(tokens).parsear();
            r.ast = ImpresorAst.aTexto(programa);

            // 3. Analisis semantico
            r.erroresSemanticos = new AnalizadorSemantico().analizar(programa);
            if (!r.erroresSemanticos.isEmpty()) {
                return r;   // se detiene: no se genera codigo ni se ejecuta
            }

            // 4. Generacion de codigo intermedio
            List<String> codigo = new GeneradorCodigo().generar(programa);
            r.codigoIntermedio = String.join("\n", codigo);

            // 5. Ejecucion (interprete)
            r.salida = new Interprete().ejecutar(programa);

            r.exito = true;

        } catch (RuntimeException ex) {
            // Errores lexicos o sintacticos (o de ejecucion)
            r.errorFatal = ex.getMessage();
        }
        return r;
    }
}
