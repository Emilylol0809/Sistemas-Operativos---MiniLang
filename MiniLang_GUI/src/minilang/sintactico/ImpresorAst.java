package minilang.sintactico;

import java.util.List;
import minilang.sintactico.Ast.*;

/**
 * Utilidad para mostrar el AST de forma indentada (modo arbol).
 * No es una etapa del compilador: sirve para visualizar el resultado del
 * analisis sintactico (en consola y en la interfaz grafica).
 *
 * El metodo aTexto(...) devuelve un String para poder mostrarlo donde sea.
 */
public class ImpresorAst {

    public static String aTexto(List<Stmt> sentencias) {
        StringBuilder sb = new StringBuilder();
        for (Stmt s : sentencias) stmt(sb, s, 0);
        return sb.toString();
    }

    private static void stmt(StringBuilder sb, Stmt s, int nivel) {
        if (s instanceof Declaracion d) {
            linea(sb, nivel, "Declaracion [" + d.tipo.lexema + " " + d.nombre.lexema + "]");
            expr(sb, d.inicializador, nivel + 1);
        } else if (s instanceof Asignacion a) {
            linea(sb, nivel, "Asignacion [" + a.nombre.lexema + "]");
            expr(sb, a.valor, nivel + 1);
        } else if (s instanceof Si si) {
            linea(sb, nivel, "Si");
            linea(sb, nivel + 1, "condicion:");
            expr(sb, si.condicion, nivel + 2);
            linea(sb, nivel + 1, "entonces:");
            for (Stmt e : si.entonces) stmt(sb, e, nivel + 2);
            if (si.sino != null) {
                linea(sb, nivel + 1, "sino:");
                for (Stmt e : si.sino) stmt(sb, e, nivel + 2);
            }
        } else if (s instanceof Mientras m) {
            linea(sb, nivel, "Mientras");
            linea(sb, nivel + 1, "condicion:");
            expr(sb, m.condicion, nivel + 2);
            linea(sb, nivel + 1, "cuerpo:");
            for (Stmt e : m.cuerpo) stmt(sb, e, nivel + 2);
        } else if (s instanceof Para p) {
            linea(sb, nivel, "Para");
            linea(sb, nivel + 1, "inicio:");
            stmt(sb, p.inicio, nivel + 2);
            linea(sb, nivel + 1, "condicion:");
            expr(sb, p.condicion, nivel + 2);
            linea(sb, nivel + 1, "actualizacion:");
            stmt(sb, p.actualizacion, nivel + 2);
            linea(sb, nivel + 1, "cuerpo:");
            for (Stmt e : p.cuerpo) stmt(sb, e, nivel + 2);
        } else if (s instanceof Imprimir p) {
            linea(sb, nivel, "Imprimir");
            expr(sb, p.expr, nivel + 1);
        }
    }

    private static void expr(StringBuilder sb, Expr e, int nivel) {
        if (e instanceof Literal l) {
            linea(sb, nivel, "Literal: " + l.valor);
        } else if (e instanceof Variable v) {
            linea(sb, nivel, "Variable: " + v.nombre.lexema);
        } else if (e instanceof Unario u) {
            linea(sb, nivel, "Unario [" + u.operador.lexema + "]");
            expr(sb, u.derecha, nivel + 1);
        } else if (e instanceof Binario b) {
            linea(sb, nivel, "Binario [" + b.operador.lexema + "]");
            expr(sb, b.izquierda, nivel + 1);
            expr(sb, b.derecha, nivel + 1);
        }
    }

    private static void linea(StringBuilder sb, int nivel, String texto) {
        sb.append("  ".repeat(nivel)).append(texto).append("\n");
    }
}
