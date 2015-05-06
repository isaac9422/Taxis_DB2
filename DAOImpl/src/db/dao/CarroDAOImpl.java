/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db.dao;

import db2.modelo.Carro;

/**
 *
 * @author Jhunior
 */
public class CarroDAOImpl extends HibernateDAOImpl<Carro, Integer>{

    public CarroDAOImpl() {
        super(Carro.class);
    }
    
}
