package minilang.lexico;

/**
 * Tipos de token que reconoce MiniLang.
 * Cada token producido por el Lexer pertenece a uno de estos tipos.
 */
public enum TokenType {
    // --- Literales ---
    NUMERO, IDENTIFICADOR, TRUE, FALSE,

    // --- Palabras clave (tipos y estructuras de control) ---
    INT, BOOL, IF, ELSE, WHILE, FOR, PRINT,

    // --- Operadores aritmeticos ---
    MAS, MENOS, POR, DIV, MODULO,    // +  -  *  /  %

    // --- Asignacion ---
    ASIGNAR,                          // =

    // --- Operadores relacionales ---
    IGUAL, DIFERENTE,                 // ==  !=
    MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL,  // <  >  <=  >=

    // --- Operadores logicos ---
    AND, OR, NOT,                     // &&  ||  !

    // --- Delimitadores ---
    PAR_IZQ, PAR_DER,                 // (  )
    LLAVE_IZQ, LLAVE_DER,             // {  }
    PUNTO_COMA,                       // ;

    // --- Fin de archivo ---
    FIN
}
