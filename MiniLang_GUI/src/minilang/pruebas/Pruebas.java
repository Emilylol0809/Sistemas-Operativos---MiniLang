package minilang.pruebas;

import java.util.*;
import minilang.lexico.*;
import minilang.sintactico.*;
import minilang.semantico.AnalizadorSemantico;
import minilang.interprete.Interprete;

/**
 * BATERIA DE PRUEBAS AUTOMATICA (sin dependencias externas).
 * Ejecuta programas MiniLang por todo el pipeline (lexico -> sintactico ->
 * semantico -> interprete) y compara la salida obtenida con la esperada.
 * Tambien valida que los programas con errores sean rechazados.
 *
 * Uso:  java minilang.pruebas.Pruebas
 */
public class Pruebas {

    static int pasaron = 0;
    static int fallaron = 0;

    public static void main(String[] args) {
        System.out.println("================ BATERIA DE PRUEBAS MINILANG ================\n");
        System.out.println("---- Casos VALIDOS (se compara la salida) ----\n");

        verificar("Precedencia aritmetica",
            "int r = 2 + 3 * 4 - 1; print(r);", "13");
        verificar("Parentesis",
            "int r = (2 + 3) * 4; print(r);", "20");
        verificar("Division entera y modulo",
            "int a = 17 / 5; int b = 17 % 5; print(a); print(b);", "3", "2");
        verificar("Logica y comparaciones",
            "bool x = (3 < 5) && (10 == 10); bool y = !x || false; print(x); print(y);", "true", "false");
        verificar("if rama verdadera",
            "int n = 8; if (n > 5) { print(n); } else { print(0); }", "8");
        verificar("if rama falsa",
            "int n = 2; if (n > 5) { print(n); } else { print(0); }", "0");
        verificar("if anidados",
            "int n = 7; if (n > 0) { if (n % 2 == 0) { print(1); } else { print(2); } }", "2");
        verificar("while acumulador",
            "int s = 0; int i = 1; while (i <= 5) { s = s + i; i = i + 1; } print(s);", "15");
        verificar("while factorial",
            "int f = 1; int i = 1; while (i <= 5) { f = f * i; i = i + 1; } print(f);", "120");
        verificar("for suma de pares",
            "int s = 0; for (int i = 0; i <= 8; i = i + 2) { s = s + i; } print(s);", "20");
        verificar("for con if interno",
            "int c = 0; for (int i = 1; i <= 10; i = i + 1) { if (i % 3 == 0) { c = c + 1; } } print(c);", "3");
        verificar("Menos unario",
            "int a = -5; int b = -a; print(a); print(b);", "-5", "5");

        System.out.println("\n---- Casos con ERRORES (se espera que sean rechazados) ----\n");

        verificarError("Variable no declarada", "x = 5;");
        verificarError("Doble declaracion", "int x = 1; int x = 2;");
        verificarError("Tipo incompatible (int a bool)", "bool b = 5;");
        verificarError("Aritmetica con bool", "int z = true + 1;");
        verificarError("Condicion no booleana", "int x = 3; if (x + 1) { print(x); }");
        verificarError("Comparar tipos distintos", "bool b = (1 == true);");

        System.out.println("\n============================================================");
        System.out.println("RESUMEN:  " + pasaron + " pasaron, " + fallaron + " fallaron, " +
                           "de " + (pasaron + fallaron) + " pruebas.");
        System.out.println("============================================================");
    }

    static void verificar(String nombre, String fuente, String... esperado) {
        try {
            List<Token> tokens = new Lexer(fuente).escanear();
            List<Ast.Stmt> programa = new Parser(tokens).parsear();
            List<String> errores = new AnalizadorSemantico().analizar(programa);
            if (!errores.isEmpty()) {
                fallar(nombre, "esperaba salida pero hubo errores semanticos: " + errores);
                return;
            }
            List<String> obtenido = new Interprete().ejecutar(programa);
            List<String> esp = Arrays.asList(esperado);
            if (obtenido.equals(esp)) pasar(nombre);
            else fallar(nombre, "esperado=" + esp + "  obtenido=" + obtenido);
        } catch (Exception ex) {
            fallar(nombre, "excepcion: " + ex.getMessage());
        }
    }

    static void verificarError(String nombre, String fuente) {
        try {
            List<Token> tokens = new Lexer(fuente).escanear();
            List<Ast.Stmt> programa = new Parser(tokens).parsear();
            List<String> errores = new AnalizadorSemantico().analizar(programa);
            if (!errores.isEmpty()) pasar(nombre + " (detectado)");
            else fallar(nombre, "se esperaba un error pero no se detecto ninguno");
        } catch (RuntimeException ex) {
            pasar(nombre + " (rechazado)");
        }
    }

    static void pasar(String nombre)  { pasaron++;  System.out.println("  [PASA]  " + nombre); }
    static void fallar(String n, String d) { fallaron++; System.out.println("  [FALLA] " + n + "  ->  " + d); }
}
