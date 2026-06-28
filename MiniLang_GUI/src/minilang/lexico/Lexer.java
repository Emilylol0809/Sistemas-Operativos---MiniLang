package minilang.lexico;

import java.util.*;

/**
 * ETAPA 1: ANALISIS LEXICO
 * Recorre el codigo fuente caracter por caracter y lo convierte en una
 * lista de tokens. Tambien ignora espacios y comentarios de linea (//),
 * y reporta caracteres invalidos.
 */
public class Lexer {
    private final String fuente;
    private final List<Token> tokens = new ArrayList<>();
    private int inicio = 0;    // inicio del lexema actual
    private int actual = 0;    // posicion del caracter que estamos leyendo
    private int linea = 1;

    // Tabla de palabras reservadas del lenguaje
    private static final Map<String, TokenType> PALABRAS_CLAVE = new HashMap<>();
    static {
        PALABRAS_CLAVE.put("int",   TokenType.INT);
        PALABRAS_CLAVE.put("bool",  TokenType.BOOL);
        PALABRAS_CLAVE.put("if",    TokenType.IF);
        PALABRAS_CLAVE.put("else",  TokenType.ELSE);
        PALABRAS_CLAVE.put("while", TokenType.WHILE);
        PALABRAS_CLAVE.put("for",   TokenType.FOR);
        PALABRAS_CLAVE.put("print", TokenType.PRINT);
        PALABRAS_CLAVE.put("true",  TokenType.TRUE);
        PALABRAS_CLAVE.put("false", TokenType.FALSE);
    }

    public Lexer(String fuente) {
        this.fuente = fuente;
    }

    /** Devuelve la lista completa de tokens, terminada con un token FIN. */
    public List<Token> escanear() {
        while (!finArchivo()) {
            inicio = actual;
            escanearToken();
        }
        tokens.add(new Token(TokenType.FIN, "", null, linea));
        return tokens;
    }

    private void escanearToken() {
        char c = avanzar();
        switch (c) {
            case '(': agregar(TokenType.PAR_IZQ);    break;
            case ')': agregar(TokenType.PAR_DER);    break;
            case '{': agregar(TokenType.LLAVE_IZQ);  break;
            case '}': agregar(TokenType.LLAVE_DER);  break;
            case ';': agregar(TokenType.PUNTO_COMA); break;
            case '+': agregar(TokenType.MAS);        break;
            case '-': agregar(TokenType.MENOS);      break;
            case '*': agregar(TokenType.POR);        break;
            case '%': agregar(TokenType.MODULO);     break;
            case '/':
                if (coincide('/')) {                       // comentario //
                    while (mirar() != '\n' && !finArchivo()) avanzar();
                } else {
                    agregar(TokenType.DIV);
                }
                break;
            case '=': agregar(coincide('=') ? TokenType.IGUAL       : TokenType.ASIGNAR); break;
            case '!': agregar(coincide('=') ? TokenType.DIFERENTE   : TokenType.NOT);     break;
            case '<': agregar(coincide('=') ? TokenType.MENOR_IGUAL : TokenType.MENOR);   break;
            case '>': agregar(coincide('=') ? TokenType.MAYOR_IGUAL : TokenType.MAYOR);   break;
            case '&':
                if (coincide('&')) agregar(TokenType.AND);
                else error("Se esperaba '&&' (y solo se encontro un '&')");
                break;
            case '|':
                if (coincide('|')) agregar(TokenType.OR);
                else error("Se esperaba '||' (y solo se encontro un '|')");
                break;
            case ' ':
            case '\t':
            case '\r':
                break;            // se ignoran los espacios en blanco
            case '\n':
                linea++;
                break;
            default:
                if (Character.isDigit(c)) {
                    numero();
                } else if (Character.isLetter(c) || c == '_') {
                    identificador();
                } else {
                    error("Caracter inesperado: '" + c + "'");
                }
        }
    }

    /** Lee una secuencia de digitos y crea un token NUMERO (entero). */
    private void numero() {
        while (Character.isDigit(mirar())) avanzar();
        String texto = fuente.substring(inicio, actual);
        agregar(TokenType.NUMERO, Integer.parseInt(texto));
    }

    /** Lee un identificador o palabra clave. */
    private void identificador() {
        while (Character.isLetterOrDigit(mirar()) || mirar() == '_') avanzar();
        String texto = fuente.substring(inicio, actual);
        TokenType tipo = PALABRAS_CLAVE.getOrDefault(texto, TokenType.IDENTIFICADOR);
        if (tipo == TokenType.TRUE)       agregar(tipo, true);
        else if (tipo == TokenType.FALSE) agregar(tipo, false);
        else                              agregar(tipo);
    }

    // ---------- Utilidades ----------
    private boolean finArchivo() { return actual >= fuente.length(); }
    private char avanzar()       { return fuente.charAt(actual++); }
    private char mirar()         { return finArchivo() ? '\0' : fuente.charAt(actual); }

    /** Consume el siguiente caracter solo si es el esperado (para operadores de 2 simbolos). */
    private boolean coincide(char esperado) {
        if (finArchivo() || fuente.charAt(actual) != esperado) return false;
        actual++;
        return true;
    }

    private void agregar(TokenType tipo) { agregar(tipo, null); }
    private void agregar(TokenType tipo, Object valor) {
        String lexema = fuente.substring(inicio, actual);
        tokens.add(new Token(tipo, lexema, valor, linea));
    }

    private void error(String msg) {
        throw new RuntimeException("Error lexico (linea " + linea + "): " + msg);
    }
}
