package minilang.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import minilang.Compilador;
import minilang.ResultadoCompilacion;

/**
 * INTERFAZ GRAFICA del compilador MiniLang (Java Swing, sin librerias externas).
 *
 * Diseno:
 *   - Cabecera de color con el titulo.
 *   - Botones con colores (verde compilar, azul ejemplo, gris limpiar).
 *   - Editor con tema oscuro tipo IDE a la izquierda.
 *   - Pestanas con el resultado de cada etapa a la derecha.
 *   - Barra de estado que cambia de color (verde = ok, rojo = error).
 *
 * Toda la logica de compilar esta en la clase Compilador; esta ventana solo
 * muestra los resultados.
 */
public class VentanaPrincipal extends JFrame {

    // ----- Paleta de colores -----
    private static final Color AZUL        = new Color(37, 99, 235);
    private static final Color AZUL_OSCURO = new Color(30, 58, 138);
    private static final Color VERDE       = new Color(22, 163, 74);
    private static final Color GRIS        = new Color(100, 116, 139);
    private static final Color FONDO       = new Color(241, 245, 249);
    private static final Color EDITOR_BG   = new Color(30, 30, 46);
    private static final Color EDITOR_FG   = new Color(226, 232, 240);
    private static final Color OK_BG       = new Color(220, 252, 231);
    private static final Color OK_FG       = new Color(22, 101, 52);
    private static final Color ERROR_BG    = new Color(254, 226, 226);
    private static final Color ERROR_FG    = new Color(153, 27, 27);

    private final JTextArea editor        = new JTextArea();
    private final JTextArea areaTokens    = crearAreaSalida(Color.WHITE);
    private final JTextArea areaAst       = crearAreaSalida(Color.WHITE);
    private final JTextArea areaSem       = crearAreaSalida(Color.WHITE);
    private final JTextArea areaCodigo    = crearAreaSalida(new Color(239, 246, 255)); // azul muy claro
    private final JTextArea areaEjecucion = crearAreaSalida(new Color(240, 253, 244)); // verde muy claro
    private final JTabbedPane pestanas    = new JTabbedPane();
    private final JLabel estado           = new JLabel("  Listo.");

    private static final String EJEMPLO =
            "// Programa de ejemplo en MiniLang\n" +
                    "int suma = 0;\n" +
                    "for (int i = 1; i <= 5; i = i + 1) {\n" +
                    "    suma = suma + i;\n" +
                    "}\n" +
                    "print(suma);\n\n" +
                    "int x = 10;\n" +
                    "if (x % 2 == 0) {\n" +
                    "    print(x);\n" +
                    "} else {\n" +
                    "    print(0);\n" +
                    "}\n";

    public VentanaPrincipal() {
        setTitle("Compilador MiniLang");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1040, 680);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        raiz.add(crearCabecera(),    BorderLayout.NORTH);
        raiz.add(crearCentro(),      BorderLayout.CENTER);
        raiz.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(raiz);

        editor.setText(EJEMPLO);
    }

    // ---- Cabecera con titulo y botones ----
    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(AZUL_OSCURO);
        cabecera.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel titulo = new JLabel("Compilador MiniLang");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel sub = new JLabel("Lexico  >  Sintactico  >  Semantico  >  Codigo intermedio  >  Ejecucion");
        sub.setForeground(new Color(191, 219, 254));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(sub);

        JButton btnCompilar = crearBoton("Compilar y Ejecutar", VERDE);
        JButton btnEjemplo  = crearBoton("Cargar ejemplo", AZUL);
        JButton btnLimpiar  = crearBoton("Limpiar", GRIS);
        btnCompilar.addActionListener(e -> compilar());
        btnEjemplo.addActionListener(e -> { editor.setText(EJEMPLO); estado("  Ejemplo cargado.", FONDO, Color.DARK_GRAY); });
        btnLimpiar.addActionListener(e -> { editor.setText(""); limpiarSalidas(); estado("  Listo.", FONDO, Color.DARK_GRAY); });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnCompilar);
        botones.add(btnEjemplo);
        botones.add(btnLimpiar);

        cabecera.add(textos,  BorderLayout.WEST);
        cabecera.add(botones, BorderLayout.EAST);
        return cabecera;
    }

    // ---- Centro: editor (izq) + pestanas (der) ----
    private JSplitPane crearCentro() {
        editor.setFont(new Font("Monospaced", Font.PLAIN, 15));
        editor.setBackground(EDITOR_BG);
        editor.setForeground(EDITOR_FG);
        editor.setCaretColor(Color.WHITE);
        editor.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scrollEditor = new JScrollPane(editor);
        scrollEditor.setBorder(crearBordeTitulo("Codigo fuente (MiniLang)", AZUL));

        pestanas.setFont(new Font("SansSerif", Font.BOLD, 12));
        pestanas.addTab("1. Tokens",            new JScrollPane(areaTokens));
        pestanas.addTab("2. AST",               new JScrollPane(areaAst));
        pestanas.addTab("3. Semantico",         new JScrollPane(areaSem));
        pestanas.addTab("4. Codigo intermedio", new JScrollPane(areaCodigo));
        pestanas.addTab("5. Ejecucion",         new JScrollPane(areaEjecucion));
        pestanas.setBorder(crearBordeTitulo("Resultado de cada etapa", AZUL));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEditor, pestanas);
        split.setDividerLocation(430);
        split.setBorder(new EmptyBorder(10, 12, 6, 12));
        split.setBackground(FONDO);
        return split;
    }

    // ---- Barra de estado (abajo) ----
    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(0, 12, 8, 12));
        estado.setOpaque(true);
        estado.setBackground(FONDO);
        estado.setForeground(Color.DARK_GRAY);
        estado.setFont(new Font("SansSerif", Font.BOLD, 13));
        estado.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(estado, BorderLayout.CENTER);
        return panel;
    }

    // ---- Fabrica de botones de color (plano) ----
    private JButton crearBoton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private Border crearBordeTitulo(String titulo, Color color) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 1), titulo);
        tb.setTitleColor(color);
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        return tb;
    }

    private JTextArea crearAreaSalida(Color fondo) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setBackground(fondo);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private void estado(String texto, Color bg, Color fg) {
        estado.setText(texto);
        estado.setBackground(bg);
        estado.setForeground(fg);
    }

    // ---- Accion principal: compilar el codigo del editor ----
    private void compilar() {
        ResultadoCompilacion r = new Compilador().compilar(editor.getText());

        limpiarSalidas();
        areaTokens.setText(r.tokens);

        if (r.errorFatal != null) {                       // error lexico o sintactico
            areaSem.setText(r.errorFatal);
            estado("  " + r.errorFatal, ERROR_BG, ERROR_FG);
            pestanas.setSelectedIndex(0);
            return;
        }

        areaAst.setText(r.ast);

        if (!r.erroresSemanticos.isEmpty()) {             // errores semanticos
            areaSem.setText(String.join("\n", r.erroresSemanticos));
            estado("  Se encontraron " + r.erroresSemanticos.size() + " error(es) semantico(s).", ERROR_BG, ERROR_FG);
            pestanas.setSelectedIndex(2);
            return;
        }

        areaSem.setText("OK: sin errores semanticos.");   // todo bien
        areaCodigo.setText(r.codigoIntermedio);
        areaEjecucion.setText(String.join("\n", r.salida));
        estado("  Compilacion y ejecucion exitosa.", OK_BG, OK_FG);
        pestanas.setSelectedIndex(4);
    }

    private void limpiarSalidas() {
        areaTokens.setText("");
        areaAst.setText("");
        areaSem.setText("");
        areaCodigo.setText("");
        areaEjecucion.setText("");
    }

    // ---- Punto de entrada de la version grafica ----
    public static void main(String[] args) {
        // Nimbus: un look mas moderno que viene incluido en Java
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignorado) { }

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}