/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

/**
 *
 * @author tiboo, still the bg
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*; 
import log.LogServeur;

public interface Requete
{
 // Ce qui va être exécuté doit connaître la socket du client distant
 // ainsi que le GUI qui affiche les traces
 //public Runnable createRunnable (Socket s, ObjectInputStream ois, LogServeur cs); 
 public Runnable createRunnable (Socket s, ConsoleServeur cs, ObjectInputStream ois, ObjectOutputStream oos); 
} 
