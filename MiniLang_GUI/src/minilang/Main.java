package minilang;

import java.nio.file.*;

/**
 * Version de CONSOLA del compilador.
 * Lee un archivo .ml, lo compila y muestra cada etapa por pantalla.
 *
 * Uso:  java minilang.Main <archivo.ml>
 *
 * (Para la version con ventana, ejecutar minilang.gui.VentanaPrincipal)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Uso: java minilang.Main <archivo.ml>");
            return;
        }
        String fuente = new String(Files.readAllBytes(Paths.get(args[0])));
        ResultadoCompilacion r = new Compilador().compilar(fuente);

        System.out.println("############# 1) TOKENS #############");
        System.out.print(r.tokens);

        if (r.errorFatal != null) {
            System.out.println("\n" + r.errorFatal);
            System.out.println(">> Compilacion detenida.");
            return;
        }

        System.out.println("\n############# 2) AST #############");
        System.out.print(r.ast);

        System.out.println("\n############# 3) ANALISIS SEMANTICO #############");
        if (r.erroresSemanticos.isEmpty()) {
            System.out.println("OK: sin errores semanticos.");
        } else {
            r.erroresSemanticos.forEach(System.out::println);
            System.out.println(">> Compilacion detenida por errores semanticos.");
            return;
        }

        System.out.println("\n############# 4) CODIGO INTERMEDIO #############");
        System.out.println(r.codigoIntermedio);

        System.out.println("\n############# 5) EJECUCION #############");
        if (r.salida.isEmpty()) System.out.println("(el programa no imprimio nada)");
        else r.salida.forEach(System.out::println);

        System.out.println("\n>> Compilacion finalizada con exito.");
    }
}
