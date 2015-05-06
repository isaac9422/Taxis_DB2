/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db.dao;

import db2.modelo.Hoyo;
import java.util.List;

/**
 *
 * @author Jhunior
 */
public class HoyoDAOImpl extends HibernateDAOImpl<Hoyo, Integer>{

    public HoyoDAOImpl() {
        super(Hoyo.class);
    }
    
    public List<Hoyo> listadoCompleto(){
        String query = "FROM Hoyo";
        List list = getCurrentSession().createQuery(query).list();
        return list;
    }
    
}
