package minilang.semantico;

import java.util.*;
import minilang.lexico.TokenType;
import minilang.sintactico.Ast.*;

/**
 * ETAPA 3: ANALISIS SEMANTICO
 *
 * Revisa que el programa tenga sentido. Verifica:
 *   - Que cada variable se declare antes de usarse.
 *   - Que no se declare dos veces la misma variable.
 *   - Que los tipos coincidan (no asignar un bool a un int, etc.).
 *   - Que la condicion de if/while/for sea de tipo bool.
 *
 * Usa una "tabla de simbolos": un mapa que guarda el tipo de cada variable.
 * Guarda todos los errores que encuentra en una lista.
 */
public class AnalizadorSemantico {

    public enum Tipo { INT, BOOL }

    private final Map<String, Tipo> tabla = new HashMap<>();   // variable -> tipo
    private final List<String> errores = new ArrayList<>();

    /** Revisa el programa. Devuelve la lista de errores (vacia si todo bien). */
    public List<String> analizar(List<Stmt> sentencias) {
        for (Stmt s : sentencias) revisar(s);
        return errores;
    }

    // Revisa una sentencia.
    private void revisar(Stmt s) {

        if (s instanceof Declaracion d) {
            Tipo tipoDeclarado = (d.tipo.tipo == TokenType.INT) ? Tipo.INT : Tipo.BOOL;
            if (tabla.containsKey(d.nombre.lexema)) {
                error("La variable '" + d.nombre.lexema + "' ya fue declarada.");
            }
            Tipo tipoValor = tipoDe(d.inicializador);
            if (tipoValor != tipoDeclarado) {
                error("No se puede asignar " + tipoValor + " a '" + d.nombre.lexema +
                      "' que es de tipo " + tipoDeclarado + ".");
            }
            tabla.put(d.nombre.lexema, tipoDeclarado);   // se registra la variable

        } else if (s instanceof Asignacion a) {
            Tipo tipoVar = tabla.get(a.nombre.lexema);
            if (tipoVar == null) {
                error("Variable no declarada: '" + a.nombre.lexema + "'.");
                return;
            }
            Tipo tipoValor = tipoDe(a.valor);
            if (tipoValor != tipoVar) {
                error("No se puede asignar " + tipoValor + " a '" + a.nombre.lexema +
                      "' que es de tipo " + tipoVar + ".");
            }

        } else if (s instanceof Si si) {
            exigirBool(si.condicion, "if");
            for (Stmt e : si.entonces) revisar(e);
            if (si.sino != null) for (Stmt e : si.sino) revisar(e);

        } else if (s instanceof Mientras m) {
            exigirBool(m.condicion, "while");
            for (Stmt e : m.cuerpo) revisar(e);

        } else if (s instanceof Para p) {
            revisar(p.inicio);
            exigirBool(p.condicion, "for");
            revisar(p.actualizacion);
            for (Stmt e : p.cuerpo) revisar(e);

        } else if (s instanceof Imprimir im) {
            tipoDe(im.expr);   // solo valida que la expresion sea correcta
        }
    }

    // La condicion de if/while/for debe ser bool.
    private void exigirBool(Expr condicion, String donde) {
        if (tipoDe(condicion) != Tipo.BOOL) {
            error("La condicion del '" + donde + "' debe ser de tipo bool.");
        }
    }

    // Calcula el tipo (INT o BOOL) de una expresion.
    private Tipo tipoDe(Expr e) {

        if (e instanceof Literal l) {
            return (l.valor instanceof Boolean) ? Tipo.BOOL : Tipo.INT;
        }

        if (e instanceof Variable v) {
            Tipo t = tabla.get(v.nombre.lexema);
            if (t == null) {
                error("Variable no declarada: '" + v.nombre.lexema + "'.");
                return Tipo.INT;   // valor por defecto para poder seguir
            }
            return t;
        }

        if (e instanceof Unario u) {
            Tipo t = tipoDe(u.derecha);
            if (u.operador.tipo == TokenType.NOT) {        // !algo  -> bool
                if (t != Tipo.BOOL) error("El operador '!' necesita un bool.");
                return Tipo.BOOL;
            } else {                                       // -algo  -> int
                if (t != Tipo.INT) error("El operador '-' necesita un int.");
                return Tipo.INT;
            }
        }

        if (e instanceof Binario b) {
            Tipo izq = tipoDe(b.izquierda);
            Tipo der = tipoDe(b.derecha);
            String op = b.operador.lexema;
            switch (b.operador.tipo) {

                // Aritmeticos:  int OP int  ->  int
                case MAS: case MENOS: case POR: case DIV: case MODULO:
                    if (izq != Tipo.INT || der != Tipo.INT)
                        error("El operador '" + op + "' necesita operandos int.");
                    return Tipo.INT;

                // Comparaciones de orden:  int OP int  ->  bool
                case MENOR: case MAYOR: case MENOR_IGUAL: case MAYOR_IGUAL:
                    if (izq != Tipo.INT || der != Tipo.INT)
                        error("La comparacion '" + op + "' necesita operandos int.");
                    return Tipo.BOOL;

                // Igualdad:  los dos lados deben ser del mismo tipo  ->  bool
                case IGUAL: case DIFERENTE:
                    if (izq != der)
                        error("No se pueden comparar tipos distintos (" + izq + " y " + der + ").");
                    return Tipo.BOOL;

                // Logicos:  bool OP bool  ->  bool
                case AND: case OR:
                    if (izq != Tipo.BOOL || der != Tipo.BOOL)
                        error("El operador logico '" + op + "' necesita operandos bool.");
                    return Tipo.BOOL;

                default:
                    return Tipo.INT;
            }
        }

        return Tipo.INT;
    }

    private void error(String mensaje) {
        errores.add("Error semantico: " + mensaje);
    }
}
