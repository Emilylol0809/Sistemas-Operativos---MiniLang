package minilang.interprete;

import java.util.*;
import minilang.lexico.TokenType;
import minilang.sintactico.Ast.*;

/**
 * EXTENSION: INTERPRETE
 * Recorre el AST y EJECUTA el programa de verdad (interprete "tree-walking").
 * Mientras las 4 etapas traducen el codigo, el interprete lo corre y produce
 * la salida real: por ejemplo, print(suma) muestra 15.
 *
 * Mantiene un "entorno" (Map nombre -> valor) con el valor actual de cada
 * variable. Los valores son Integer o Boolean.
 *
 * Caracteristicas:
 *   - Cortocircuito en && y ||.
 *   - Control de flujo real: if/else, while y for se ejecutan.
 *   - Errores de ejecucion controlados (ej. division por cero).
 */
public class Interprete {

    private final Map<String, Object> entorno = new HashMap<>();
    private final List<String> salida = new ArrayList<>();

    /** Ejecuta el programa y devuelve la lista de lineas impresas con print(). */
    public List<String> ejecutar(List<Stmt> sentencias) {
        for (Stmt s : sentencias) ejecutarStmt(s);
        return salida;
    }

    private void ejecutarStmt(Stmt s) {
        if (s instanceof Declaracion d) {
            entorno.put(d.nombre.lexema, evaluar(d.inicializador));

        } else if (s instanceof Asignacion a) {
            entorno.put(a.nombre.lexema, evaluar(a.valor));

        } else if (s instanceof Imprimir p) {
            salida.add(String.valueOf(evaluar(p.expr)));

        } else if (s instanceof Si si) {
            if ((Boolean) evaluar(si.condicion)) {
                for (Stmt e : si.entonces) ejecutarStmt(e);
            } else if (si.sino != null) {
                for (Stmt e : si.sino) ejecutarStmt(e);
            }

        } else if (s instanceof Mientras m) {
            while ((Boolean) evaluar(m.condicion)) {
                for (Stmt e : m.cuerpo) ejecutarStmt(e);
            }

        } else if (s instanceof Para p) {
            ejecutarStmt(p.inicio);
            while ((Boolean) evaluar(p.condicion)) {
                for (Stmt e : p.cuerpo) ejecutarStmt(e);
                ejecutarStmt(p.actualizacion);
            }
        }
    }

    /** Evalua una expresion y devuelve su valor (Integer o Boolean). */
    private Object evaluar(Expr e) {
        if (e instanceof Literal l) {
            return l.valor;

        } else if (e instanceof Variable v) {
            return entorno.get(v.nombre.lexema);

        } else if (e instanceof Unario u) {
            Object d = evaluar(u.derecha);
            if (u.operador.tipo == TokenType.NOT) return !((Boolean) d);
            return -((Integer) d);

        } else if (e instanceof Binario b) {
            // Cortocircuito: el lado derecho solo se evalua si hace falta.
            if (b.operador.tipo == TokenType.AND)
                return ((Boolean) evaluar(b.izquierda)) && ((Boolean) evaluar(b.derecha));
            if (b.operador.tipo == TokenType.OR)
                return ((Boolean) evaluar(b.izquierda)) || ((Boolean) evaluar(b.derecha));

            Object i = evaluar(b.izquierda);
            Object d = evaluar(b.derecha);
            switch (b.operador.tipo) {
                case MAS:    return (Integer) i + (Integer) d;
                case MENOS:  return (Integer) i - (Integer) d;
                case POR:    return (Integer) i * (Integer) d;
                case DIV:
                    if ((Integer) d == 0) throw new RuntimeException("Error en ejecucion: division por cero.");
                    return (Integer) i / (Integer) d;
                case MODULO:
                    if ((Integer) d == 0) throw new RuntimeException("Error en ejecucion: modulo por cero.");
                    return (Integer) i % (Integer) d;
                case MENOR:        return (Integer) i <  (Integer) d;
                case MAYOR:        return (Integer) i >  (Integer) d;
                case MENOR_IGUAL:  return (Integer) i <= (Integer) d;
                case MAYOR_IGUAL:  return (Integer) i >= (Integer) d;
                case IGUAL:        return i.equals(d);
                case DIFERENTE:    return !i.equals(d);
                default:           return null;
            }
        }
        return null;
    }
}
