/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti;

import java.io.FileInputStream;
import java.util.Properties;
import log.LogServeur;

/**
 *
 * @author Pierre
 */
public class MainProgram {
    
    public static void main(String[] args) {
        int portFront = 58000;
        int portInt = 58001; 
        try {
            FileInputStream in = new FileInputStream("donnees.properties"); 
            Properties data = new Properties();
            data.load(in);
            
            portFront = Integer.parseInt((String) data.getProperty("portFront"));
            portInt = Integer.parseInt((String) data.getProperty("portInt")); 
        } catch (Exception e) {
            System.err.println("<MainProgram> " + e.getMessage());
        }
        
        ListeTaches lt = new ListeTaches(); 
        LogServeur ls = new LogServeur(); 

        ServeurFrontieres serv = new ServeurFrontieres(portFront, lt, ls); 
        Thread th = new Thread(serv); 
        th.start();
        
        ServeurRegInternat serv2 = new ServeurRegInternat(portInt, lt, ls); 
        Thread th2 = new Thread(serv2);
        th2.start(); 
    }
}
