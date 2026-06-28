# Compilador MiniLang (con interfaz grafica)

Compilador para el lenguaje MiniLang, en Java puro (sin librerias externas),
organizado por paquetes segun las etapas de compilacion. Incluye version de
consola, version con interfaz grafica (Swing) y una bateria de pruebas.

## Estructura de paquetes

```
src/minilang/
├── Main.java                  -> version de CONSOLA
├── Compilador.java            -> fachada: corre todas las etapas
├── ResultadoCompilacion.java  -> guarda el resultado de cada etapa
├── lexico/
│   ├── TokenType.java
│   ├── Token.java
│   └── Lexer.java             -> ETAPA 1
├── sintactico/
│   ├── Ast.java
│   ├── Parser.java            -> ETAPA 2
│   └── ImpresorAst.java
├── semantico/
│   └── AnalizadorSemantico.java -> ETAPA 3
├── generador/
│   └── GeneradorCodigo.java   -> ETAPA 4
├── interprete/
│   └── Interprete.java        -> EXTENSION: ejecuta el programa
├── gui/
│   └── VentanaPrincipal.java  -> INTERFAZ GRAFICA
└── pruebas/
    └── Pruebas.java           -> bateria de pruebas
```

## Como ejecutar

### Interfaz grafica (recomendado)
En IntelliJ: abrir `VentanaPrincipal.java` y darle Run.
Por terminal:
```bash
javac -d out $(find src -name "*.java")
java -cp out minilang.gui.VentanaPrincipal
```

### Version de consola
```bash
java -cp out minilang.Main ejemplos/ejemplo1.ml
```

### Pruebas
```bash
java -cp out minilang.pruebas.Pruebas
```

## Como se usa la interfaz

1. Se escribe (o se carga con "Cargar ejemplo") un programa en el editor de la izquierda.
2. Se presiona "Compilar y Ejecutar".
3. En las pestanas de la derecha aparece el resultado de cada etapa:
   Tokens, AST, Semantico, Codigo intermedio y Ejecucion.
4. Si hay un error, se muestra en la pestana correspondiente y en la barra de estado.
