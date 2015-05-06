/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db2.main;

import db.dao.DAOManager;
import db2.ventana.Ventana;
import db.dao.HibernateDAOImpl;
import javax.swing.JOptionPane;

/**
 *
 * @author Jhunior
 */
public class Main {

    public static void main(String[] args) {
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {

        try {
            Ventana vent = null;
            DAOManager dAOManager = new DAOManager();
            vent = new Ventana();
            vent.setdAOManager(dAOManager);
            vent.inicio();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new Ventana(), "Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
//            }
//        });

    }
}
