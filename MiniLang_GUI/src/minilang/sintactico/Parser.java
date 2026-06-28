package minilang.sintactico;

import java.util.*;
import minilang.lexico.Token;
import minilang.lexico.TokenType;
import minilang.sintactico.Ast.*;

/**
 * ETAPA 2: ANALISIS SINTACTICO
 *
 * Toma la lista de tokens y arma el arbol (AST) siguiendo la gramatica.
 * Es un "parser descendente recursivo": hay un metodo por cada regla y se
 * llaman unos a otros segun lo que va apareciendo.
 *
 * PRECEDENCIA DE OPERADORES (de menor a mayor prioridad):
 *   1. ||                  (logicoOr)
 *   2. &&                  (logicoAnd)
 *   3. == != < > <= >=     (comparacion)
 *   4. + -                 (termino)
 *   5. * / %               (factor)
 *   6. ! -                 (unario)
 *
 * Como cada nivel llama al siguiente, lo que esta mas abajo se evalua
 * primero. Por eso "2 + 3 * 4" hace primero 3*4.
 */
public class Parser {
    private final List<Token> tokens;
    private int actual = 0;   // posicion del token que estamos mirando

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /** Parsea el programa completo: una lista de sentencias. */
    public List<Stmt> parsear() {
        List<Stmt> sentencias = new ArrayList<>();
        while (!finArchivo()) {
            sentencias.add(sentencia());
        }
        return sentencias;
    }

    // ============ SENTENCIAS ============
    private Stmt sentencia() {
        if (coincide(TokenType.INT, TokenType.BOOL)) return declaracion();
        if (coincide(TokenType.IF))                  return sentenciaSi();
        if (coincide(TokenType.WHILE))               return sentenciaMientras();
        if (coincide(TokenType.FOR))                 return sentenciaPara();
        if (coincide(TokenType.PRINT))               return sentenciaImprimir();
        if (verificar(TokenType.IDENTIFICADOR))      return asignacion();
        throw error(mirar(), "Se esperaba una sentencia.");
    }

    // int x = expresion ;
    private Stmt declaracion() {
        Token tipo = anterior();   // INT o BOOL (ya consumido)
        Token nombre = consumir(TokenType.IDENTIFICADOR, "Falta el nombre de la variable.");
        consumir(TokenType.ASIGNAR, "Falta '=' en la declaracion.");
        Expr valor = expresion();
        consumir(TokenType.PUNTO_COMA, "Falta ';' al final.");
        return new Declaracion(tipo, nombre, valor);
    }

    // x = expresion ;
    private Stmt asignacion() {
        Token nombre = consumir(TokenType.IDENTIFICADOR, "Falta el identificador.");
        consumir(TokenType.ASIGNAR, "Falta '=' en la asignacion.");
        Expr valor = expresion();
        consumir(TokenType.PUNTO_COMA, "Falta ';' al final.");
        return new Asignacion(nombre, valor);
    }

    // if ( expresion ) bloque  [ else bloque ]
    private Stmt sentenciaSi() {
        consumir(TokenType.PAR_IZQ, "Falta '(' despues de 'if'.");
        Expr condicion = expresion();
        consumir(TokenType.PAR_DER, "Falta ')' despues de la condicion.");
        List<Stmt> entonces = bloque();
        List<Stmt> sino = null;
        if (coincide(TokenType.ELSE)) {
            sino = bloque();
        }
        return new Si(condicion, entonces, sino);
    }

    // while ( expresion ) bloque
    private Stmt sentenciaMientras() {
        consumir(TokenType.PAR_IZQ, "Falta '(' despues de 'while'.");
        Expr condicion = expresion();
        consumir(TokenType.PAR_DER, "Falta ')' despues de la condicion.");
        List<Stmt> cuerpo = bloque();
        return new Mientras(condicion, cuerpo);
    }

    // for ( inicio ; condicion ; actualizacion ) bloque
    private Stmt sentenciaPara() {
        consumir(TokenType.PAR_IZQ, "Falta '(' despues de 'for'.");

        // inicio: una declaracion (int/bool) o una asignacion (ambas comen su ';')
        Stmt inicio;
        if (coincide(TokenType.INT, TokenType.BOOL)) inicio = declaracion();
        else                                         inicio = asignacion();

        // condicion, seguida de ';'
        Expr condicion = expresion();
        consumir(TokenType.PUNTO_COMA, "Falta ';' despues de la condicion del 'for'.");

        // actualizacion: una asignacion pero SIN ';' (ej: i = i + 1)
        Token nombre = consumir(TokenType.IDENTIFICADOR, "Falta la variable a actualizar en el 'for'.");
        consumir(TokenType.ASIGNAR, "Falta '=' en la actualizacion del 'for'.");
        Expr valor = expresion();
        Stmt actualizacion = new Asignacion(nombre, valor);

        consumir(TokenType.PAR_DER, "Falta ')' en el 'for'.");
        List<Stmt> cuerpo = bloque();
        return new Para(inicio, condicion, actualizacion, cuerpo);
    }

    // print ( expresion ) ;
    private Stmt sentenciaImprimir() {
        consumir(TokenType.PAR_IZQ, "Falta '(' despues de 'print'.");
        Expr expr = expresion();
        consumir(TokenType.PAR_DER, "Falta ')'.");
        consumir(TokenType.PUNTO_COMA, "Falta ';'.");
        return new Imprimir(expr);
    }

    // { sentencia* }
    private List<Stmt> bloque() {
        consumir(TokenType.LLAVE_IZQ, "Falta '{'.");
        List<Stmt> sentencias = new ArrayList<>();
        while (!verificar(TokenType.LLAVE_DER) && !finArchivo()) {
            sentencias.add(sentencia());
        }
        consumir(TokenType.LLAVE_DER, "Falta '}'.");
        return sentencias;
    }

    // ============ EXPRESIONES (por prioridad) ============
    // Todos estos metodos tienen la MISMA forma:
    //   1) piden una expresion del nivel de abajo (mas prioridad)
    //   2) mientras vean su operador, la combinan en un nodo Binario
    private Expr expresion() {
        return logicoOr();
    }

    private Expr logicoOr() {       // ||
        Expr expr = logicoAnd();
        while (coincide(TokenType.OR)) {
            Token op = anterior();
            expr = new Binario(expr, op, logicoAnd());
        }
        return expr;
    }

    private Expr logicoAnd() {      // &&
        Expr expr = comparacion();
        while (coincide(TokenType.AND)) {
            Token op = anterior();
            expr = new Binario(expr, op, comparacion());
        }
        return expr;
    }

    private Expr comparacion() {    // == != < > <= >=
        Expr expr = termino();
        while (coincide(TokenType.IGUAL, TokenType.DIFERENTE,
                        TokenType.MENOR, TokenType.MAYOR,
                        TokenType.MENOR_IGUAL, TokenType.MAYOR_IGUAL)) {
            Token op = anterior();
            expr = new Binario(expr, op, termino());
        }
        return expr;
    }

    private Expr termino() {        // + -
        Expr expr = factor();
        while (coincide(TokenType.MAS, TokenType.MENOS)) {
            Token op = anterior();
            expr = new Binario(expr, op, factor());
        }
        return expr;
    }

    private Expr factor() {         // * / %
        Expr expr = unario();
        while (coincide(TokenType.POR, TokenType.DIV, TokenType.MODULO)) {
            Token op = anterior();
            expr = new Binario(expr, op, unario());
        }
        return expr;
    }

    private Expr unario() {         // ! -
        if (coincide(TokenType.NOT, TokenType.MENOS)) {
            Token op = anterior();
            return new Unario(op, unario());
        }
        return primario();
    }

    // Lo mas basico: un numero, true/false, una variable, o algo entre parentesis.
    private Expr primario() {
        if (coincide(TokenType.NUMERO))        return new Literal(anterior().valor);
        if (coincide(TokenType.TRUE))          return new Literal(true);
        if (coincide(TokenType.FALSE))         return new Literal(false);
        if (coincide(TokenType.IDENTIFICADOR)) return new Variable(anterior());
        if (coincide(TokenType.PAR_IZQ)) {
            Expr expr = expresion();
            consumir(TokenType.PAR_DER, "Falta ')' para cerrar la expresion.");
            return expr;
        }
        throw error(mirar(), "Se esperaba una expresion (numero, variable, etc.).");
    }

    // ============ AYUDANTES ============
    private boolean coincide(TokenType... tipos) {
        for (TokenType t : tipos) {
            if (verificar(t)) { avanzar(); return true; }
        }
        return false;
    }
    private boolean verificar(TokenType tipo) {
        return !finArchivo() && mirar().tipo == tipo;
    }
    private Token avanzar()      { if (!finArchivo()) actual++; return anterior(); }
    private boolean finArchivo() { return mirar().tipo == TokenType.FIN; }
    private Token mirar()        { return tokens.get(actual); }
    private Token anterior()     { return tokens.get(actual - 1); }

    private Token consumir(TokenType tipo, String mensaje) {
        if (verificar(tipo)) return avanzar();
        throw error(mirar(), mensaje);
    }
    private RuntimeException error(Token token, String mensaje) {
        return new RuntimeException(
            "Error sintactico (linea " + token.linea + "): " + mensaje +
            " Se encontro: " + token);
    }
}
