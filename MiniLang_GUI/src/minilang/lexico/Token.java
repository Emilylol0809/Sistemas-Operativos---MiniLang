package minilang.lexico;

/**
 * Un token es la unidad minima con significado del lenguaje.
 * Guarda su tipo, el texto original (lexema), el valor literal si lo tiene
 * (un Integer o un Boolean) y la linea donde aparecio (util para los errores).
 */
public class Token {
    public final TokenType tipo;
    public final String lexema;
    public final Object valor;   // Integer o Boolean; null si no aplica
    public final int linea;

    public Token(TokenType tipo, String lexema, Object valor, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.valor = valor;
        this.linea = linea;
    }

    @Override
    public String toString() {
        if (valor != null) {
            return String.format("%-14s '%s'  (valor=%s, linea %d)", tipo, lexema, valor, linea);
        }
        return String.format("%-14s '%s'  (linea %d)", tipo, lexema, linea);
    }
}
