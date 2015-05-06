
/* Adapted from code posted by R.J. Lorimer in an articleentitled "Java2D: Have Fun With Affine
 Transform". The original post and code can be found 
 affineTransform http://www.javalobby.org/java/forums/t19387.html.
 */
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PanAndZoom {

    private EstructuraElementos estructuraElementos;
    PanAndZoomCanvas canvas;
    PanningHandler panner;
    AffineTransform affineTransform;
    Point2D XFormedPoint;
    JSlider zoomSlider;
    private MantenimientoProgramadoDAOImpl mantenimientoProgramadoDAOImpl;
    private DAOManager dAOManager;
    private long fecha;
    private JPanel jPanel;
    private JTextArea informacionTextArea;
    private InterfazGraficador interfaz;
    HashSet<String> elementosColaterales;

    public void pintar() {
        mantenimientoProgramadoDAOImpl = dAOManager.getMantenimientoProgramadoDAOImpl();
        mantenimientoProgramadoDAOImpl.iniciarTransaccion();
        jPanel.add(zoomSlider, BorderLayout.EAST);
        jPanel.add(canvas, BorderLayout.CENTER);
        mantenimientoProgramadoDAOImpl.commit();
        mantenimientoProgramadoDAOImpl.cerrarSession();
    }

    public PanAndZoom() {

        canvas = new PanAndZoomCanvas();
        panner = new PanningHandler();
        canvas.addMouseListener(panner);
        canvas.addMouseMotionListener(panner);
        canvas.addMouseWheelListener(panner);
        canvas.setBorder(BorderFactory.createLineBorder(Color.black));
        zoomSlider = new JSlider(JSlider.VERTICAL, 25, 600, 50);
        zoomSlider.setMajorTickSpacing(25);
        zoomSlider.setMinorTickSpacing(5);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.addChangeListener(new ScaleHandler());
        zoomSlider.setBackground(Color.WHITE);

    }

    /**
     * @return the estructuraElementos
     */
    public EstructuraElementos getEstructuraElementos() {
        return estructuraElementos;
    }

    /**
     * @param estructuraElementos the estructuraElementos to set
     */
    public void setEstructuraElementos(EstructuraElementos estructuraElementos) {
        this.estructuraElementos = estructuraElementos;
    }

    /**
     * @return the jPanel
     */
    public JPanel getjPanel() {
        return jPanel;
    }

    /**
     * @param jPanel the jPanel to set
     */
    public void setjPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }

    /**
     * @return the fecha
     */
    public long getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    /**
     * @return the informacionTextArea
     */
    public JTextArea getInformacionTextArea() {
        return informacionTextArea;
    }

    /**
     * @param informacionTextArea the informacionTextArea to set
     */
    public void setInformacionTextArea(JTextArea informacionTextArea) {
        this.informacionTextArea = informacionTextArea;
    }

    /**
     * @return the interfaz
     */
    public InterfazGraficador getInterfaz() {
        return interfaz;
    }

    /**
     * @param interfaz the interfaz to set
     */
    public void setInterfaz(InterfazGraficador interfaz) {
        this.interfaz = interfaz;
    }

    /**
     * @return the dAOManager
     */
    public DAOManager getdAOManager() {
        return dAOManager;
    }

    /**
     * @param dAOManager the dAOManager to set
     */
    public void setdAOManager(DAOManager dAOManager) {
        this.dAOManager = dAOManager;
    }

    class PanAndZoomCanvas extends JComponent {

        //No se cambian
        private static final long serialVersionUID = 1L;
        double translateX;
        double translateY;
        double scale;
        String enMantenimiento;
        String posiblesMantenimientos;
        String elementosColateralidad;

        PanAndZoomCanvas() {

            translateX = -1800;
            translateY = -2200;
            scale = 0.5;

        }

        @Override
        /**
         * Recibe un parámetro g de tipo Graphics, donde se preparan los
         * elementos a graficar que intervienen en el proceso
         */
        public void paint(Graphics g) {
            HashMap<String, RelacionSubestacion> relacionSubestaciones = estructuraElementos.getRelacionSubestacion();
            for (String linea : relacionSubestaciones.keySet()) {
                relacionSubestaciones.get(linea).resetearUsos();
            }
            elementosColaterales = new HashSet<>();
            enMantenimiento = "Elementos en mantenimiento:\n";
            posiblesMantenimientos = "Posibles mantenimientos por colateralidad:\n";
            elementosColateralidad = "Elementos que por restricción de simultaneidad no pueden salir a mantenimiento:\n";
            Graphics2D ourGraphics = (Graphics2D) g;
            AffineTransform saveTransform = ourGraphics.getTransform();
            affineTransform = new AffineTransform(saveTransform);
            affineTransform.translate(getWidth() / 2, getHeight() / 2);
            affineTransform.scale(scale, scale);
            affineTransform.translate(-getWidth() / 2, -getHeight() / 2);
            affineTransform.translate(translateX, translateY);
            ourGraphics.setTransform(affineTransform);
            ourGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graficarBahiaSinColateral(ourGraphics);
            graficarLineasBarras(ourGraphics);
            graficarLineasSinFin(ourGraphics);
            graficarLineas(ourGraphics);
            graficarSubestaciones(ourGraphics);
            if (enMantenimiento.equals("Elementos en mantenimiento:\n")) {
                informacionTextArea.setText("No se encuentran mantenimientos programados a la fecha.");
            } else {
                informacionTextArea.setText(enMantenimiento + "\n" + posiblesMantenimientos + "\n" + elementosColateralidad);
            }
            ourGraphics.setTransform(saveTransform);

        }

        /**
         * Grafica las subestaciones, teniendo en cuenta las configuraciones a
         * las que se vea sometida Adicionalmente, grafica los generadores
         * asociados a cada subestación
         *
         * @param ourGraphics
         */
        private void graficarSubestaciones(Graphics2D ourGraphics) {
            List<PosSubest> subestaciones = getEstructuraElementos().getSubestaciones();
            for (PosSubest subestacion : subestaciones) {
                //Determinar el estado de la subestacion (Se representa por el color (Estado = Pendiente))
                Rectangle rect = subestacion.generarRectangulo();
                ourGraphics.setFont(PosSubest.LETRA_SUBESTACION);
                ourGraphics.setColor(Color.BLACK);
                if (subestacion.getxTexto() > 0) { //Utilizado para pintar el nombre de las subestaciones
                    ourGraphics.drawString(subestacion.getSigtSubestacion().getNombre(), subestacion.getxTexto(), subestacion.getyTexto());
                }
                if (subestacion.getX() > 0) {   //Determina si se debe pintar la subestación y como se debe pintar
                    ourGraphics.setColor(new Color(89, 211, 48));
                    List<PosElemen> elementosSubestacion = subestacion.getSigtSubestacion().getPosicionesElementos();
                    for (PosElemen elemento : elementosSubestacion) {
                        if (elemento.elementoGenerador()) {
                            graficarGenerador(ourGraphics, elemento);
                        } else if (elemento.elementoBahiaCorteCentral()) {
                            boolean mantenimientoInicio = mantenimientoProgramadoDAOImpl.mantenimientoPendiente(elemento.getElemento(), fecha);
                            if (mantenimientoInicio) {
                                graficarBahia(ourGraphics, elemento, PosElemen.COLOR_MANTENIMIENTO);
                            } else {
                                if (subestacion.getSigtSubestacion().getVoltNomi() <= 230) {
                                    graficarBahia(ourGraphics, elemento, PosElemen.COLOR_DISPONIBLE_230KV);
                                } else {
                                    graficarBahia(ourGraphics, elemento, PosElemen.COLOR_DISPONIBLE_500KV);
                                }
                            }
                        }
                    }
                    if (subestacion.getSigtSubestacion().getVoltNomi() <= 230) {
                        ourGraphics.setColor(PosSubest.COLOR_DISPONIBLE_230KV);
                    } else if (subestacion.getSigtSubestacion().getVoltNomi() == 500) {
                        ourGraphics.setColor(PosSubest.COLOR_DISPONIBLE_500KV);
                    }
                    ourGraphics.fill(rect);
                }
            }
        }

        /**
         * Método que pinta las bahias donde existe una conexión 1 a 1 entre 2
         * subestaciones
         *
         * @param ourGraphics
         */
        private void graficarBahiaSinColateral(Graphics2D ourGraphics) {
            List<PosElemen> elementosSistema = estructuraElementos.getElementosSistema();
            for (PosElemen elementoSistema : elementosSistema) {
                if (elementoSistema.elementoBahiaLinea()) {
                    List<PosNodosLinea> nodosLineaInicial = elementoSistema.getElemento().getNodosLineaInicial();
                    List<PosNodosLinea> nodosLineaFinal = elementoSistema.getElemento().getNodosLineaFinal();
                    if (nodosLineaInicial.isEmpty() || nodosLineaFinal.isEmpty()) {
                        Color color = mantenimientoProgramadoDAOImpl.mantenimientoPendiente(elementoSistema.getElemento(), fecha) ? PosElemen.COLOR_MANTENIMIENTO : PosElemen.COLOR_DISPONIBLE_230KV;
                        if (color.equals(PosElemen.COLOR_MANTENIMIENTO)) {
                            graficarBahia(ourGraphics, elementoSistema, PosElemen.COLOR_MANTENIMIENTO);
                        } else if (elementoSistema.getSigtSubestacion().getVoltNomi() <= 230) {
                            graficarBahia(ourGraphics, elementoSistema, PosElemen.COLOR_DISPONIBLE_230KV);
                        } else {
                            graficarBahia(ourGraphics, elementoSistema, PosElemen.COLOR_DISPONIBLE_500KV);
                        }
                    }
                }
            }
        }

        /**
         * Método utilizado para la graficación de los elementos que tienen
         * comportamiento de bahia
         *
         * @param ourGraphics
         * @param bahia
         * @param color
         */
        private void graficarBahia(Graphics2D ourGraphics, PosElemen bahia, Color color) {
            ourGraphics.setFont(PosElemen.LETRA_INTERRUPTOR);
            Rectangle interruptor = bahia.crearBahia();
            ourGraphics.setColor(PosElemen.COLOR_RELLENO_INTERRUPTOR);
            ourGraphics.fill(interruptor);
            ourGraphics.setStroke(new BasicStroke(PosElemen.GROSOR_LINEA_INTERRUTOR));
            ourGraphics.setColor(color);
            ourGraphics.draw(interruptor);
            ourGraphics.setColor(PosElemen.Color_LETRA);
            ourGraphics.drawString(bahia.getNombreBahia(), interruptor.x - 4, interruptor.y - 2);
            ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
            ourGraphics.setColor(PosElemen.COLOR_DISPONIBLE_230KV);
        }

        /**
         * Método para graficar generadores y su correspondiente interruptor
         *
         * @param ourGraphics
         * @param bahiaGeneracion
         */
        private void graficarGenerador(Graphics2D ourGraphics, PosElemen bahiaGeneracion) {
            String idElemen = bahiaGeneracion.getElemento().getIdElemen();
            Color color = mantenimientoProgramadoDAOImpl.mantenimientoPendiente(bahiaGeneracion.getElemento(), fecha) ? PosElemen.COLOR_MANTENIMIENTO : PosElemen.COLOR_DISPONIBLE_230KV;
            Point posicionGenerador = new Point(bahiaGeneracion.getX(), bahiaGeneracion.getY());
            Rectangle conexion = new Rectangle();
            int orientacion = bahiaGeneracion.determinarOrientacion();
            if (orientacion == 0) {
                posicionGenerador.x = posicionGenerador.x + PosElemen.TAMANO_BAHIA_GENERACION / 2 - PosElemen.DIAMETRO_GENERADOR / 2;
                posicionGenerador.y = posicionGenerador.y + PosElemen.TAMANO_BAHIA_GENERACION + PosElemen.DIAMETRO_GENERADOR / 2;
                conexion = new Rectangle(posicionGenerador.x + PosElemen.DIAMETRO_GENERADOR / 2, bahiaGeneracion.getY(), PosNodosLinea.GROSOR_LINEA, PosElemen.TAMANO_BAHIA_GENERACION + PosElemen.DIAMETRO_GENERADOR / 2);
            } else if (orientacion == 2) {
                posicionGenerador.x = posicionGenerador.x + PosElemen.TAMANO_BAHIA_GENERACION + PosElemen.DIAMETRO_GENERADOR / 2;
                posicionGenerador.y = posicionGenerador.y + PosElemen.TAMANO_BAHIA_GENERACION / 2 - PosElemen.DIAMETRO_GENERADOR / 2 + 1;
                conexion = new Rectangle(bahiaGeneracion.getX() + 1, posicionGenerador.y + PosElemen.DIAMETRO_GENERADOR / 2 - 1, 1 + PosElemen.TAMANO_BAHIA_GENERACION + PosElemen.DIAMETRO_GENERADOR / 2, PosNodosLinea.GROSOR_LINEA);
            }
            ourGraphics.setColor(color.equals(PosElemen.COLOR_MANTENIMIENTO) ? PosElemen.COLOR_INDISPONIBLE : PosElemen.COLOR_DISPONIBLE_230KV);
            ourGraphics.fill(conexion);
            ourGraphics.drawOval(posicionGenerador.x + PosNodosLinea.GROSOR_LINEA / 2, posicionGenerador.y, PosElemen.DIAMETRO_GENERADOR, PosElemen.DIAMETRO_GENERADOR);
            graficarBahia(ourGraphics, bahiaGeneracion, color);
            ourGraphics.setColor(color.equals(PosElemen.COLOR_MANTENIMIENTO) ? PosElemen.COLOR_INDISPONIBLE : PosElemen.COLOR_DISPONIBLE_230KV);
            ourGraphics.setFont(PosElemen.LETRA_G);
            ourGraphics.drawString("G",
                    posicionGenerador.x + PosElemen.DIAMETRO_GENERADOR / 4,
                    posicionGenerador.y + PosElemen.DIAMETRO_GENERADOR / 2 + 4);
        }

        /**
         * *
         * Método para graficar los elementos que son transformadores tipo ATR
         *
         * @param ourGraphics
         * @param transformador
         * @param color
         */
        private void graficarTransformadorATR(Graphics2D ourGraphics, PosElemen transformador, Color color) {
            ourGraphics.setColor(color);
            ourGraphics.drawOval((transformador.getX() - 2) + PosNodosLinea.GROSOR_LINEA / 2, transformador.getY(), PosElemen.DIAMETRO_GENERADOR, PosElemen.DIAMETRO_GENERADOR);
            ourGraphics.setFont(PosElemen.LETRAS_ATR);
            ourGraphics.drawString("ATR",
                    transformador.getX() + PosElemen.DIAMETRO_GENERADOR / 4 - 2,
                    transformador.getY() + PosElemen.DIAMETRO_GENERADOR / 2 + 1);
        }

        /**
         * *
         * Método para graficar los elementos que son transformadores tipo BATR
         *
         * @param ourGraphics
         * @param transformador
         * @param color
         */
        private void graficarTransformadorBATR(Graphics2D ourGraphics, PosElemen transformador, Color color) {
            ourGraphics.setColor(color);
            ourGraphics.drawOval((transformador.getX() - 2) + PosNodosLinea.GROSOR_LINEA / 2, transformador.getY(), PosElemen.DIAMETRO_GENERADOR, PosElemen.DIAMETRO_GENERADOR);
            ourGraphics.setFont(PosElemen.LETRAS_BATR);
            ourGraphics.drawString("BATR",
                    transformador.getX() + PosElemen.DIAMETRO_GENERADOR / 4 - 2,
                    transformador.getY() + PosElemen.DIAMETRO_GENERADOR / 2 + 1);
        }

        /**
         * Método que pinta las lineas que están entre 2 elementos
         * Teniendo en cuenta las consideraciones de ser linea activa, linea en mantenimiento o linea que no puede salir a mantenimientos
         * @param ourGraphics
         */
        private void graficarLineas(Graphics2D ourGraphics) {

            List<String> lineas = estructuraElementos.getLineas();
            HashMap<String, RelacionSubestacion> relacionSubestaciones = estructuraElementos.getRelacionSubestacion();
            for (String linea : lineas) {
                Linea lineaGrafica = estructuraElementos.getLineaxIndicador().get(linea);
                PosElemen elementoInicialGrafico = lineaGrafica.getInicio();
                PosElemen elementoFinalGrafico = lineaGrafica.getFin();
                String relacion1 = elementoInicialGrafico.getSigtSubestacion().toString() + " - " + elementoFinalGrafico.getSigtSubestacion().toString();
                String relacion2 = elementoFinalGrafico.getSigtSubestacion().toString() + " - " + elementoInicialGrafico.getSigtSubestacion().toString();
                RelacionSubestacion relacionG = null;
                if (relacionSubestaciones.containsKey(relacion1)) {
                    relacionG = relacionSubestaciones.get(relacion1);
                } else if (relacionSubestaciones.containsKey(relacion2)) {
                    relacionG = relacionSubestaciones.get(relacion2);
                } else {
                    relacionG = new RelacionSubestacion();
                }
                List<Rectangle> rectangulos = PosNodosLinea.crearRectangulos(lineaGrafica.getLinea());
                List<QuadCurve2D> curvas = PosNodosLinea.crearCurvas(lineaGrafica.getLinea());
                boolean mantenimientoInicio = mantenimientoProgramadoDAOImpl.mantenimientoPendiente(elementoInicialGrafico.getElemento(), fecha);
                boolean mantenimientoFin = mantenimientoProgramadoDAOImpl.mantenimientoPendiente(elementoFinalGrafico.getElemento(), fecha);
                if (mantenimientoFin && mantenimientoInicio) {  //Determina el estado actual de los elementos que comunica la linea
                    ourGraphics.setColor(PosNodosLinea.COLOR_INDISPONIBLE);
                    relacionG.marcarUsada(lineaGrafica);
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                        ourGraphics.fill(rectangle);
                    }
                    for (QuadCurve2D curva : curvas) {
                        ourGraphics.draw(curva);
                    }
                    graficarBahia(ourGraphics, elementoInicialGrafico, PosElemen.COLOR_MANTENIMIENTO);
                    graficarBahia(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_MANTENIMIENTO);
                    if (elementoFinalGrafico.elementoBahiaCorteCentral() || elementoInicialGrafico.elementoBahiaCorteCentral()) {
                        enMantenimiento += elementoFinalGrafico.getElemento().getIdElemen() + "\n";
                    }
                } else if (mantenimientoInicio) {
                    ourGraphics.setColor(PosNodosLinea.COLOR_INDISPONIBLE);
                    if (elementoFinalGrafico.elementoBahiaCorteCentral() || elementoFinalGrafico.elementoBahiaLinea()
                            || elementoFinalGrafico.elementoBahiaTrafo()) {
                        posiblesMantenimientos += elementoFinalGrafico.getElemento().getIdElemen() + "\n";
                        relacionG.marcarUsada(lineaGrafica);
                    }
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                        ourGraphics.fill(rectangle);
                    }
                    for (QuadCurve2D curva : curvas) {
                        ourGraphics.draw(curva);
                    }
                    if (elementoFinalGrafico.elementoBahiaLinea() || elementoFinalGrafico.elementoBahiaCorteCentral()) {
                        List<PosNodosLinea> listaInicial = elementoFinalGrafico.getElemento().getNodosLineaInicial();
                        List<PosNodosLinea> listaDepurada = new ArrayList();
                        for (PosNodosLinea depurada : listaInicial) {
                            if (depurada.getPosicionBarra() != null) {
                                listaDepurada.add(depurada);
                            }
                        }
                        List<Rectangle> lineasMantenimiento1 = PosNodosLinea.crearRectangulos(listaDepurada);
                        ourGraphics.setColor(PosElemen.COLOR_INDISPONIBLE);
                        for (Rectangle line : lineasMantenimiento1) {
                            ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                            ourGraphics.fill(line);
                        }
                        graficarBahia(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_INDISPONIBLE);
                    } else if (elementoFinalGrafico.elementoTransformadorATR()) {
                        graficarTransformadorATR(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_INDISPONIBLE);
                    } else if (elementoFinalGrafico.elementoTransformadorBATR()) {
                        graficarTransformadorBATR(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_INDISPONIBLE);
                    }
                    graficarBahia(ourGraphics, elementoInicialGrafico, PosElemen.COLOR_MANTENIMIENTO);
                    if (elementoInicialGrafico.elementoBahiaCorteCentral()) {
                        enMantenimiento += elementoInicialGrafico.getElemento().getIdElemen() + "\n";
                    }
                } else if (mantenimientoFin) {
                    if (elementoInicialGrafico.elementoBahiaCorteCentral() || elementoInicialGrafico.elementoBahiaLinea()
                            || elementoInicialGrafico.elementoBahiaTrafo()) {
                        posiblesMantenimientos += elementoInicialGrafico.getElemento().getIdElemen() + "\n";
                        relacionG.marcarUsada(lineaGrafica);
                    }
                    ourGraphics.setColor(PosNodosLinea.COLOR_INDISPONIBLE);
                    Collections.reverse(rectangulos);
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                        ourGraphics.fill(rectangle);
                    }
                    Collections.reverse(curvas);
                    for (QuadCurve2D curva : curvas) {
                        ourGraphics.draw(curva);
                    }
                    List<PosNodosLinea> listaInicial = elementoInicialGrafico.getElemento().getNodosLineaInicial();
                    List<PosNodosLinea> listaDepurada = new ArrayList();
                    for (PosNodosLinea depurada : listaInicial) {
                        if (depurada.getPosicionBarra() != null) {
                            listaDepurada.add(depurada);
                        }
                    }
                    List<Rectangle> lineasMantenimiento2 = PosNodosLinea.crearRectangulos(listaDepurada);
                    for (Rectangle linha : lineasMantenimiento2) {
                        ourGraphics.setColor(PosElemen.COLOR_INDISPONIBLE);
                        ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                        ourGraphics.fill(linha);
                    }
                    graficarBahia(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_MANTENIMIENTO);
                    graficarBahia(ourGraphics, elementoInicialGrafico, PosElemen.COLOR_INDISPONIBLE);
                    if (elementoFinalGrafico.elementoBahiaCorteCentral()) {
                        enMantenimiento += elementoFinalGrafico.getElemento().getIdElemen() + "\n";
                    }
                } else {
                    if (elementoInicialGrafico.getSigtSubestacion().getVoltNomi() <= 230) {
                        ourGraphics.setColor(PosNodosLinea.COLOR_DISPONIBLE_230KV);
                    } else {
                        ourGraphics.setColor(PosNodosLinea.COLOR_DISPONIBLE_500KV);
                    }
                    ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.fill(rectangle);
                    }
                    for (QuadCurve2D curva : curvas) {
                        ourGraphics.draw(curva);
                    }
                    graficarBahia(ourGraphics, elementoInicialGrafico, ourGraphics.getColor());
                    if (elementoFinalGrafico.elementoBahiaLinea()) {
                        if (elementoFinalGrafico.getSigtSubestacion().getVoltNomi() <= 230) {
                            graficarBahia(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_DISPONIBLE_230KV);
                        } else {
                            graficarBahia(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_DISPONIBLE_500KV);
                        }
                    } else if (elementoFinalGrafico.elementoTransformadorATR()) {
                        if (elementoFinalGrafico.getSigtSubestacion().getVoltNomi() <= 230) {
                            graficarTransformadorATR(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_DISPONIBLE_230KV);
                        } else {
                            graficarTransformadorATR(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_DISPONIBLE_500KV);
                        }
                    } else if (elementoFinalGrafico.elementoTransformadorBATR()) {
                        if (elementoFinalGrafico.getSigtSubestacion().getVoltNomi() <= 230) {
                            graficarTransformadorBATR(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_DISPONIBLE_230KV);
                        } else {
                            graficarTransformadorBATR(ourGraphics, elementoFinalGrafico, PosElemen.COLOR_DISPONIBLE_500KV);
                        }
                    }
                }
            }
            for (String linea : relacionSubestaciones.keySet()) {
                RelacionSubestacion relacionSubestacion = relacionSubestaciones.get(linea);
                elementosColateralidad += relacionSubestacion.definirColateralidad();
                elementosColaterales.addAll(relacionSubestacion.getElementosColaterales());
            }
            PosElemenDAOImpl posElemenDAOImpl = dAOManager.getPosElemenDAO();
            SigtElemenSistDAOImpl sigtElemenSistDAOImpl = dAOManager.getSigtElemenSistDAO();
            PosNodosLineaDAOImpl posNodosLineaDAOImpl = dAOManager.getPosNodosLineaDAOImpl();
            HashSet<String> usados = new HashSet<>();
            for (String idElementoColateral : elementosColaterales) {
                SigtElemenSist elementoColateralIncio = sigtElemenSistDAOImpl.cargar(idElementoColateral);
                PosElemen elementoColateral = posElemenDAOImpl.cargarElemento(elementoColateralIncio);
                List<PosNodosLinea> lineaElemen = posNodosLineaDAOImpl.listarPorIdElementoInicialYPrimerSecuencia(elementoColateralIncio);
                for (PosNodosLinea puntoLinea : lineaElemen) {
                    List<PosNodosLinea> linea = posNodosLineaDAOImpl.obtenerLineasPorTupla(puntoLinea);
                    if (!linea.isEmpty()) {
                        PosElemen elementoColateralFin = posElemenDAOImpl.cargarElemento(linea.get(0).getElementoFinal());
                        ourGraphics.setColor(PosNodosLinea.COLOR_COLATERAL);
                        List<Rectangle> crearRectangulos = PosNodosLinea.crearRectangulos(linea);
                        List<QuadCurve2D> curvas = PosNodosLinea.crearCurvas(linea);
                        for (Rectangle rectangulos : crearRectangulos) {
                            ourGraphics.fill(rectangulos);
                        }
                        for (QuadCurve2D curva : curvas) {
                            ourGraphics.draw(curva);
                        }
                        if (!usados.contains(elementoColateralIncio.getIdElemen())) {
                            graficarBahia(ourGraphics, elementoColateral, PosNodosLinea.COLOR_COLATERAL);
                            usados.add(elementoColateralIncio.getIdElemen());
                        }
                        if (elementoColateralFin != null) {
                            if (!usados.contains(elementoColateralFin.getElemento().getIdElemen())) {
                                graficarBahia(ourGraphics, elementoColateralFin, PosNodosLinea.COLOR_COLATERAL);
                                usados.add(elementoColateralFin.getElemento().getIdElemen());
                            }
                        }
                    }
                }

            }

        }

        /**
         * *
         * Grafica las lineas que conectan bahias con subestaciones
         *
         * @param ourGraphics
         */
        private void graficarLineasBarras(Graphics2D ourGraphics) {
            List<String> lineas = estructuraElementos.getLineasBarras();
            Color color;
            for (String linea : lineas) {
                Linea lineaGrafica = estructuraElementos.getLineaxBarra().get(linea);
                PosElemen elementoInicialGrafico = lineaGrafica.getInicio();

                List<Rectangle> rectangulos = PosNodosLinea.crearRectangulos(lineaGrafica.getLinea());
                boolean mantenimientoInicio = mantenimientoProgramadoDAOImpl.mantenimientoPendiente(elementoInicialGrafico.getElemento(), fecha);
                if (mantenimientoInicio) {
                    color = PosElemen.COLOR_MANTENIMIENTO;
                    if (!elementoInicialGrafico.elementoBahiaCorteCentral()) {
                        enMantenimiento += elementoInicialGrafico.getElemento().getIdElemen() + "\n";
                    }
                    ourGraphics.setColor(PosNodosLinea.COLOR_INDISPONIBLE);
                    ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.fill(rectangle);
                    }
                } else {
                    if (elementoInicialGrafico.getSigtSubestacion().getVoltNomi() <= 230) {
                        ourGraphics.setColor(PosNodosLinea.COLOR_DISPONIBLE_230KV);
                        color = PosNodosLinea.COLOR_DISPONIBLE_230KV;
                    } else {
                        color = PosElemen.COLOR_DISPONIBLE_500KV;
                        ourGraphics.setColor(PosNodosLinea.COLOR_DISPONIBLE_500KV);
                    }
                    ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.fill(rectangle);
                    }
                }
                graficarBahia(ourGraphics, elementoInicialGrafico, color);
            }
        }

        /**
         * *
         * Graficas las lineas que por su comportamiento no tienen un final
         * conocido y les grafica una flecha para indicar hacia donde se dirige
         *
         * @param ourGraphics
         */
        private void graficarLineasSinFin(Graphics2D ourGraphics) {
            List<String> lineas = estructuraElementos.getLineasSinFin();
            for (String linea : lineas) {
                Linea lineaGrafica = estructuraElementos.getLineaxFin().get(linea);
                PosElemen elementoInicialGrafico = lineaGrafica.getInicio();
                List<Rectangle> rectangulos = PosNodosLinea.crearRectangulos(lineaGrafica.getLinea());
                List<QuadCurve2D> curvas = PosNodosLinea.crearCurvas(lineaGrafica.getLinea());
                List<PosNodosLinea> ptos = lineaGrafica.getLinea();
                boolean mantenimientoInicio = mantenimientoProgramadoDAOImpl.mantenimientoPendiente(elementoInicialGrafico.getElemento(), fecha);
                if (mantenimientoInicio) {
                    ourGraphics.setColor(PosNodosLinea.COLOR_INDISPONIBLE);
                    ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.fill(rectangle);
                    }
                    for (QuadCurve2D curva : curvas) {
                        ourGraphics.draw(curva);
                    }
                } else {
                    if (elementoInicialGrafico.getSigtSubestacion().getVoltNomi() <= 230) {
                        ourGraphics.setColor(PosNodosLinea.COLOR_DISPONIBLE_230KV);
                    } else {
                        ourGraphics.setColor(PosNodosLinea.COLOR_DISPONIBLE_500KV);
                    }
                    ourGraphics.setStroke(new BasicStroke(PosNodosLinea.GROSOR_LINEA));
                    for (Rectangle rectangle : rectangulos) {
                        ourGraphics.fill(rectangle);
                    }
                    for (QuadCurve2D curva : curvas) {
                        ourGraphics.draw(curva);
                    }
                } //Pintar flechas al final
                int x1 = ptos.get(ptos.size() - 1).getX();
                int y1 = ptos.get(ptos.size() - 1).getY();
                int x2 = ptos.get(ptos.size() - 2).getX();
                int y2 = ptos.get(ptos.size() - 2).getY();
                if (x1 == x2) {
                    if (y1 > y2) {
                        int[] x = {x1 + 5, x1 - 3, x1 + 1};
                        int[] y = {y1, y1, y1 + 6};
                        ourGraphics.fillPolygon(x, y, 3);
                    } else {
                        int[] x = {x1 - 3, x1 + 5, x1 + 1};
                        int[] y = {y1, y1, y1 - 6};
                        ourGraphics.fillPolygon(x, y, 3);
                    }
                } else {
                    if (x1 > x2) {
                        int[] x = {x1 + 1, x1 + 1, x1 + 6};
                        int[] y = {y1 + 5, y1 - 3, y2 + 1};
                        ourGraphics.fillPolygon(x, y, 3);
                    } else {
                        int[] x = {x1, x1, x1 - 6};
                        int[] y = {y1 - 3, y1 + 5, y2 + 1};
                        ourGraphics.fillPolygon(x, y, 3);
                    }
                }
                pintarNombre(ourGraphics, elementoInicialGrafico);
            }
        }

        /**
         * Pinta el nombre de un elemento
         *
         * @param ourGraphics
         * @param bahiaLinea
         */
        private void pintarNombre(Graphics2D ourGraphics, PosElemen bahiaLinea) {
            ourGraphics.setFont(PosElemen.LETRA_INTERRUPTOR);
            ourGraphics.setColor(Color.black);
            ourGraphics.drawString(bahiaLinea.getNombreBahia(), (bahiaLinea.getX() + PosNodosLinea.GROSOR_LINEA / 2) - 4, (bahiaLinea.getY() + PosNodosLinea.GROSOR_LINEA / 2) - 2);
        }

        /**
         * Calcula AutomaffineTransformicamente el punto donde irá el
         * interruptor,
         *
         * @param origen
         * @param destino
         * @return el punto donde irá el interruptor
         */
        private Point calcularPuntoInterruptor(Point origen, Point destino) {
            Point puntoInterruptor = new Point();
            if (origen.getX() == destino.getX()) {
                //Linea vertical en sentido hacia abajo
                if (origen.getY() < destino.getY()) {
                    puntoInterruptor.setLocation(origen.getX()
                            - PosElemen.TAMANO_BAHIA_GENERACION / 2, origen.getY()
                            + PosElemen.SEPARACION_BAHIA_GENERACION + PosSubest.GROSOR_BARRA);
                } else {
                    //Linea vertical en sentido hacia arriba
                    puntoInterruptor.setLocation(origen.getX()
                            - PosElemen.TAMANO_BAHIA_GENERACION / 2, origen.getY()
                            - PosElemen.SEPARACION_BAHIA_GENERACION - PosElemen.TAMANO_BAHIA_GENERACION);
                }
            } else if (origen.getY() == destino.getY()) {
                //linea horizontal sentido hacia la derecha
                if (origen.getX() < destino.getX()) {
                    puntoInterruptor.setLocation(origen.getX()
                            + PosElemen.SEPARACION_BAHIA_GENERACION, origen.getY()
                            - PosElemen.TAMANO_BAHIA_GENERACION / 2);
                } else {
                    //linea horizontal sentido hacia la izquierda
                    puntoInterruptor.setLocation(origen.getX()
                            - PosElemen.TAMANO_BAHIA_GENERACION - PosElemen.SEPARACION_BAHIA_GENERACION,
                            origen.getY() - PosElemen.TAMANO_BAHIA_GENERACION / 2);
                }
            }
            return puntoInterruptor;
        }
    }

    /**
     * Clase para capturar los eventos del mouse
     */
    class PanningHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

        private final int CANTIDAD_MOVER = 10;
        double referenceX;
        double referenceY;
        // saves the initial transform affineTransform the beginning of the pan interaction
        AffineTransform initialTransform;

        // capture the starting point
        @Override
        public void mousePressed(MouseEvent e) {

            // first transform the mouse point to the pan and zoom
            // coordinates
            try {
                XFormedPoint = affineTransform.inverseTransform(e.getPoint(), null);
            } catch (NoninvertibleTransformException te) {
                System.out.println(te);
            }

            // save the transformed starting point and the initial
            // transform
            referenceX = XFormedPoint.getX();
            referenceY = XFormedPoint.getY();
            initialTransform = affineTransform;
        }

        @Override
        public void mouseDragged(MouseEvent e) {

            // first transform the mouse point to the pan and zoom
            // coordinates. We must take care to transform by the
            // initial tranform, not the updated transform, so that
            // both the initial reference point and all subsequent
            // reference points are measured against the same origin.
            try {
                XFormedPoint = initialTransform.inverseTransform(e.getPoint(),
                        null);
            } catch (NoninvertibleTransformException te) {
                System.out.println(te);
            }

            // the size of the pan translations
            // are defined by the current mouse location subtracted
            // from the reference location
            double deltaX = XFormedPoint.getX() - referenceX;
            double deltaY = XFormedPoint.getY() - referenceY;

            // make the reference point be the new mouse point.
            referenceX = (int) XFormedPoint.getX();
            referenceY = (int) XFormedPoint.getY();

            canvas.translateX += deltaX;
            canvas.translateY += deltaY;

            // schedule a repaint.
            canvas.repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                XFormedPoint = initialTransform.inverseTransform(e.getPoint(), null);
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(PanAndZoom.class.getName()).log(Level.SEVERE, null, ex);
            }
            SigtElemenSist elemento = getElementoClick(XFormedPoint);
            if (elemento != null) {
                SigtElemenSistDAOImpl sigtElemenSistDAOImpl = dAOManager.getSigtElemenSistDAO();
                sigtElemenSistDAOImpl.iniciarTransaccion();
                elemento = sigtElemenSistDAOImpl.cargar(elemento.getIdElemen());
                if (!elementosColaterales.contains(elemento.getIdElemen())) {
                    interfaz.setElemento(elemento, true);
                } else {
                    interfaz.setElemento(elemento, false);
                }
                List<MantenimientoProgramado> mantenimientos = elemento.getMantenimiento();

                if (!mantenimientos.isEmpty()) {
                    long fechaInicioMantenimiento = mantenimientos.get(0).getFechaInicio();
                    fecha = fechaInicioMantenimiento;
                    canvas.repaint();
                }
                interfaz.mostrarPopUp();
                sigtElemenSistDAOImpl.cerrarSession();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int movimiento = e.getWheelRotation();
            zoomSlider.setValue(zoomSlider.getValue() - CANTIDAD_MOVER * movimiento);

        }

        private SigtElemenSist getElementoClick(Point2D punto) {
            List<PosSubest> subestaciones = estructuraElementos.getSubestaciones();
            for (PosSubest subestacion : subestaciones) {
                List<PosElemen> elementosSistema = subestacion.getSigtSubestacion().getPosicionesElementos();
                for (PosElemen elemento : elementosSistema) {
                    if (elemento.elementoBahiaLinea()) {
                        if (dentroRectangulo(elemento.crearBahia(), punto)) {
                            return elemento.getElemento();
                        }
                    } else if (elemento.elementoGenerador()) {
                        if (dentroRectangulo(new Rectangle(elemento.getX(), elemento.getY(), PosElemen.DIAMETRO_GENERADOR, PosElemen.DIAMETRO_GENERADOR), punto)) {
                            return elemento.getElemento();
                        }
                    }
                    if (elemento.elementoBahiaTrafo()) {
                        if (dentroRectangulo(elemento.crearBahia(), punto)) {
                            return elemento.getElemento();
                        }
                    }
                    if (elemento.elementoBahiaCorteCentral()) {
                        if (dentroRectangulo(elemento.crearBahia(), punto)) {
                            return elemento.getElemento();
                        }
                    }
                }
            }

            return null;
        }

        private boolean dentroRectangulo(Rectangle rectangulo, Point2D punto) {
            return rectangulo.contains(punto);
        }
    }

    /**
     * Clase para manejar el Slider, Zoom
     */
    class ScaleHandler implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            int zoomPercent = slider.getValue();
            canvas.scale = Math.max(0.00001, zoomPercent / 100.0);
            canvas.repaint();
        }
    }

}
