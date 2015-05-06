/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db.dao;

/**
 *
 * @author jhunior
 */
import java.io.Serializable;
import java.util.List;

public interface HibernateDAO<T, ID extends Serializable> {

    public ID guardar(T instancia);

    /**
     * Carga un objeto del modelo desde la base de datos
     *     
* @param clavePrimaria
     * @return
     */
    public T cargar(ID clavePrimaria);

    /**
     * Actualiza un objeto del modelo en la base de datos
     *     
* @param instancia
     */
    public void actualizar(T instancia);

    /**
     * Elimina un objeto del modelo en la base de datos
     *     
* @param instancia
     */
    public void eliminar(T instancia);

    /**
     * Lista todos los elementos contenidos en la base de datos
     *     
     * @return
     */
    public List<T> listar();
    
    /**
     * Lista el numero maximo de todos los elementos contenidos 
     * en la base de datos
     *     
     * @param limiteResultados
     * @return
     */
    public List<T> listarConLimite(int limiteResultados);

}