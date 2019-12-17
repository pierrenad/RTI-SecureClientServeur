/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti;

/**
 *
 * @author Pierre
 */
import java.util.*; 
import Interface.*;

public class ListeTaches implements SourceTache {
    private LinkedList listeTaches;
    
    public ListeTaches() {
        listeTaches = new LinkedList();
    } 
    
    @Override
    public synchronized Runnable getTache() throws InterruptedException {
        System.out.println("<ListeTaches> getTache");
        while (!existTache()) wait();
        return (Runnable)listeTaches.remove();
    }  
    
    @Override
    public synchronized void recordTache (Runnable r) {
        listeTaches.addLast(r);
        System.out.println("<ListeTaches> tache dans la file");
        notify();
    } 

    @Override
    public boolean existTache() {
        return !listeTaches.isEmpty();
    }
}
