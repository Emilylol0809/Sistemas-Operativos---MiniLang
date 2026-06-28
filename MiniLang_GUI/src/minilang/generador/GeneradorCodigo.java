package minilang.generador;

import java.util.*;
import minilang.lexico.TokenType;
import minilang.sintactico.Ast.*;

/**
 * ETAPA 4: GENERACION DE CODIGO INTERMEDIO
 * Traduce el AST a "codigo de tres direcciones" (TAC), una representacion
 * intermedia clasica donde cada instruccion tiene a lo sumo un operador.
 * Las expresiones complejas se descomponen usando variables temporales
 * (t0, t1, ...) y el control de flujo (if/while/for) usa etiquetas
 * (L0, L1, ...) con saltos (goto / if_false).
 *
 * Ejemplo:   x = (a + b) * 2;
 *   t0 = a + b
 *   t1 = t0 * 2
 *   x = t1
 */
public class GeneradorCodigo {

    private final List<String> codigo = new ArrayList<>();
    private int contadorTemp = 0;
    private int contadorEtiqueta = 0;

    public List<String> generar(List<Stmt> sentencias) {
        for (Stmt s : sentencias) genStmt(s);
        return codigo;
    }

    private void genStmt(Stmt s) {
        if (s instanceof Declaracion d) {
            String valor = genExpr(d.inicializador);
            emitir(d.nombre.lexema + " = " + valor);

        } else if (s instanceof Asignacion a) {
            String valor = genExpr(a.valor);
            emitir(a.nombre.lexema + " = " + valor);

        } else if (s instanceof Imprimir p) {
            String valor = genExpr(p.expr);
            emitir("print " + valor);

        } else if (s instanceof Si si) {
            String cond = genExpr(si.condicion);
            String etiquetaSino = nuevaEtiqueta();
            emitir("if_false " + cond + " goto " + etiquetaSino);
            for (Stmt e : si.entonces) genStmt(e);
            if (si.sino != null) {
                String etiquetaFin = nuevaEtiqueta();
                emitir("goto " + etiquetaFin);
                emitirEtiqueta(etiquetaSino);
                for (Stmt e : si.sino) genStmt(e);
                emitirEtiqueta(etiquetaFin);
            } else {
                emitirEtiqueta(etiquetaSino);
            }

        } else if (s instanceof Mientras m) {
            String etiquetaInicio = nuevaEtiqueta();
            String etiquetaFin = nuevaEtiqueta();
            emitirEtiqueta(etiquetaInicio);
            String cond = genExpr(m.condicion);
            emitir("if_false " + cond + " goto " + etiquetaFin);
            for (Stmt e : m.cuerpo) genStmt(e);
            emitir("goto " + etiquetaInicio);
            emitirEtiqueta(etiquetaFin);

        } else if (s instanceof Para p) {
            genStmt(p.inicio);
            String etiquetaInicio = nuevaEtiqueta();
            String etiquetaFin = nuevaEtiqueta();
            emitirEtiqueta(etiquetaInicio);
            String cond = genExpr(p.condicion);
            emitir("if_false " + cond + " goto " + etiquetaFin);
            for (Stmt e : p.cuerpo) genStmt(e);
            genStmt(p.actualizacion);
            emitir("goto " + etiquetaInicio);
            emitirEtiqueta(etiquetaFin);
        }
    }

    /** Genera el codigo de una expresion y devuelve donde quedo su resultado
     *  (un temporal, una variable o un literal). */
    private String genExpr(Expr e) {
        if (e instanceof Literal l) {
            return String.valueOf(l.valor);

        } else if (e instanceof Variable v) {
            return v.nombre.lexema;

        } else if (e instanceof Unario u) {
            String der = genExpr(u.derecha);
            String t = nuevoTemp();
            String op = (u.operador.tipo == TokenType.NOT) ? "!" : "-";
            emitir(t + " = " + op + der);
            return t;

        } else if (e instanceof Binario b) {
            String izq = genExpr(b.izquierda);
            String der = genExpr(b.derecha);
            String t = nuevoTemp();
            emitir(t + " = " + izq + " " + b.operador.lexema + " " + der);
            return t;
        }
        return "?";
    }

    private String nuevoTemp()     { return "t" + (contadorTemp++); }
    private String nuevaEtiqueta()  { return "L" + (contadorEtiqueta++); }
    private void emitir(String instr)        { codigo.add("    " + instr); }
    private void emitirEtiqueta(String etq)  { codigo.add(etq + ":"); }
}
