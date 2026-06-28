package minilang.sintactico;

import java.util.List;
import minilang.lexico.Token;

/**
 * Definicion del AST (Arbol de Sintaxis Abstracta).
 * El parser construye un arbol con estos nodos; las etapas siguientes
 * (semantico y generador de codigo) lo recorren.
 *
 * Hay dos familias: Expr (expresiones, producen un valor) y
 * Stmt (sentencias, ejecutan una accion).
 */
public class Ast {

    // ===================== EXPRESIONES =====================
    public static abstract class Expr {}

    /** Un literal: un numero (5) o un booleano (true / false). */
    public static class Literal extends Expr {
        public final Object valor;   // Integer o Boolean
        public Literal(Object valor) { this.valor = valor; }
    }

    /** El uso de una variable por su nombre. */
    public static class Variable extends Expr {
        public final Token nombre;
        public Variable(Token nombre) { this.nombre = nombre; }
    }

    /** Operacion unaria:  -x   o   !b */
    public static class Unario extends Expr {
        public final Token operador;
        public final Expr derecha;
        public Unario(Token operador, Expr derecha) {
            this.operador = operador;
            this.derecha = derecha;
        }
    }

    /** Operacion binaria: a + b, a < b, a && b, etc. */
    public static class Binario extends Expr {
        public final Expr izquierda;
        public final Token operador;
        public final Expr derecha;
        public Binario(Expr izquierda, Token operador, Expr derecha) {
            this.izquierda = izquierda;
            this.operador = operador;
            this.derecha = derecha;
        }
    }

    // ===================== SENTENCIAS =====================
    public static abstract class Stmt {}

    /** Declaracion con inicializacion:  int x = 5; */
    public static class Declaracion extends Stmt {
        public final Token tipo;          // INT o BOOL
        public final Token nombre;
        public final Expr inicializador;
        public Declaracion(Token tipo, Token nombre, Expr inicializador) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.inicializador = inicializador;
        }
    }

    /** Asignacion a una variable ya declarada:  x = x + 1; */
    public static class Asignacion extends Stmt {
        public final Token nombre;
        public final Expr valor;
        public Asignacion(Token nombre, Expr valor) {
            this.nombre = nombre;
            this.valor = valor;
        }
    }

    /** Condicional:  if (cond) { ... } else { ... }  (el 'sino' puede ser null) */
    public static class Si extends Stmt {
        public final Expr condicion;
        public final List<Stmt> entonces;
        public final List<Stmt> sino;     // null si no hay else
        public Si(Expr condicion, List<Stmt> entonces, List<Stmt> sino) {
            this.condicion = condicion;
            this.entonces = entonces;
            this.sino = sino;
        }
    }

    /** Bucle:  while (cond) { ... } */
    public static class Mientras extends Stmt {
        public final Expr condicion;
        public final List<Stmt> cuerpo;
        public Mientras(Expr condicion, List<Stmt> cuerpo) {
            this.condicion = condicion;
            this.cuerpo = cuerpo;
        }
    }

    /** Bucle:  for (inicio; cond; actualizacion) { ... } */
    public static class Para extends Stmt {
        public final Stmt inicio;
        public final Expr condicion;
        public final Stmt actualizacion;
        public final List<Stmt> cuerpo;
        public Para(Stmt inicio, Expr condicion, Stmt actualizacion, List<Stmt> cuerpo) {
            this.inicio = inicio;
            this.condicion = condicion;
            this.actualizacion = actualizacion;
            this.cuerpo = cuerpo;
        }
    }

    /** Salida:  print(expr); */
    public static class Imprimir extends Stmt {
        public final Expr expr;
        public Imprimir(Expr expr) { this.expr = expr; }
    }
}
