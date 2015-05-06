/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package depro.util;

/**
 * Clase encargada de cargar las propiedades del proyecto quality
 *
 * @author Jhunior
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CargaProperties {

    private final static String RUTA_PROPERTIES = "config/depro.properties";
    private final static String ESCALA_PUNTO_VENTA = "escala";
    private final static String NOMBRE_TIENDA = "titulo";
    private final static String RUTA_POR_DEFECTO = "rutaCarga";
    private static int escala = 99;
    private static String titulo = "BIG";
    private static String rutaCarga = "C:\\";

    /**
     * Obtiene la propiedad del archivo de properties
     * depro.properties
     *
     * @param nombrePropiedad
     * @return
     */
    private Properties getProperties() throws Exception {
        Properties propiedades = new Properties();
        File archivoProperties = new File(RUTA_PROPERTIES);

        //Si el archivo de properties no existe, crearlo
        if (!archivoProperties.exists()) {
            archivoProperties.createNewFile();
        }

        //Se carga el archivo de propiedades
        propiedades.load(new FileReader(archivoProperties));

        //Si el archivo de propiedades está vacio se crea la propiedad vacia
        if (propiedades.isEmpty() || !(propiedades.containsKey(ESCALA_PUNTO_VENTA)) 
                || !(propiedades.containsKey(NOMBRE_TIENDA))) {
            propiedades.setProperty(ESCALA_PUNTO_VENTA, "99");
            propiedades.setProperty(NOMBRE_TIENDA, "BIG");
        }

        return propiedades;
    }

    /**
     * *
     * Obtener el punto de venta que se va a utilizar
     *
     * @return
     */
    public int obtenerEscala() {
        try {
            Properties properties = this.getProperties();
            if (properties.getProperty(ESCALA_PUNTO_VENTA, "99") != null) {
                escala = Integer.parseInt(properties.getProperty(ESCALA_PUNTO_VENTA, "99"));
                setEscala(escala);
            }
            return escala;
        } catch (Exception ex) {
            Logger.getLogger(CargaProperties.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    /**
     * *
     * Obtener el nombre de la tienda actual
     *
     * @return
     */
    public String obtenerNombreTienda() {
        try {
            Properties properties = this.getProperties();
            if (properties.getProperty(NOMBRE_TIENDA, "BIG") != null) {
                titulo = properties.getProperty(NOMBRE_TIENDA, "BIG");
                setNombreTienda(titulo);
            }
            return titulo;
        } catch (Exception ex) {
            Logger.getLogger(CargaProperties.class.getName()).log(Level.SEVERE, null, ex);
            return "BIG";
        }
    }

    /**
     * @return the rutaCarga
     */
    public String getRutaCarga() {
        try {
            Properties properties = this.getProperties();
            if (properties.getProperty(RUTA_POR_DEFECTO, "C:\\") != null) {
                rutaCarga = properties.getProperty(RUTA_POR_DEFECTO, "C:\\");
            }
            return rutaCarga;
        } catch (Exception ex) {
            Logger.getLogger(CargaProperties.class.getName()).log(Level.SEVERE, null, ex);
            return "C:\\";
        }
    }

    /**
     * *
     * Método para modificar el punto de venta que esta siendo utilizado
     *
     * @param escala
     * @throws java.io.FileNotFoundException
     */
    public void setEscala(int escala) throws Exception {
        FileInputStream in = new FileInputStream(RUTA_PROPERTIES);
        Properties properties = this.getProperties();
        properties.load(in);
        in.close();

        FileOutputStream out = new FileOutputStream(RUTA_PROPERTIES);
        if ((properties.containsKey(ESCALA_PUNTO_VENTA))) {
            properties.setProperty(ESCALA_PUNTO_VENTA, String.valueOf(escala));

        } else {
            properties.setProperty(ESCALA_PUNTO_VENTA, "99");
        }
        properties.store(out, "Quality");
        out.close();
        CargaProperties.escala = escala;
    }

    /**
     * *
     * Método para modificar el nombre de la tienda que se esta utilizando
     *
     * @param nombreTienda
     * @throws java.io.FileNotFoundException
     */
    public void setNombreTienda(String nombreTienda) throws Exception {
        FileInputStream in = new FileInputStream(RUTA_PROPERTIES);
        Properties properties = this.getProperties();
        properties.load(in);
        in.close();

        FileOutputStream out = new FileOutputStream(RUTA_PROPERTIES);
        if ((properties.containsKey(NOMBRE_TIENDA))) {
            properties.setProperty(NOMBRE_TIENDA, String.valueOf(nombreTienda));

        } else {
            properties.setProperty(NOMBRE_TIENDA, "BIG");
        }
        properties.store(out, "Quality");
        out.close();
        CargaProperties.titulo = nombreTienda;
    }

    /**
     * *
     * Método para modificar la ruta de la tienda que se esta utilizando
     *
     * @param rutaTienda
     * @throws java.io.FileNotFoundException
     */
    public void setRutaTienda(String rutaTienda) throws Exception {
        FileInputStream in = new FileInputStream(RUTA_PROPERTIES);
        Properties properties = this.getProperties();
        properties.load(in);
        in.close();

        FileOutputStream out = new FileOutputStream(RUTA_PROPERTIES);
        if ((properties.containsKey(RUTA_POR_DEFECTO))) {
            properties.setProperty(RUTA_POR_DEFECTO, String.valueOf(rutaTienda));

        } else {
            properties.setProperty(RUTA_POR_DEFECTO, "C:\\");
        }
        properties.store(out, "Quality");
        out.close();
        CargaProperties.rutaCarga = rutaTienda;
    }

}
