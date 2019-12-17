/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package log;

import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import Interface.ConsoleServeur;

/**
 *
 * @author Pierre
 */
public class LogServeur implements ConsoleServeur {
    
    private final static String fichierLog = "fichierLog.txt"; 
    
    public LogServeur() {
    }
    
    public void addLog(String ligne) {
        try {
            Date mtn = new Date();
            String maDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM,Locale.FRANCE).format(mtn);
            ligne = maDate.concat(" > " + ligne); 
            FileWriter fw = new FileWriter(System.getProperty("user.dir") + System.getProperty("file.separator") + fichierLog, true); 
            fw.write(ligne);
            fw.write(System.getProperty("line.separator"));
            fw.close();
        } catch (Exception ex) { 
            System.err.println("<LogServeur> Ajout ligne dans log : " + ex.getMessage()); 
        }
    }
    
    /*public void addLog(String ligne, ServerWindow sw) {
        try {
            Date mtn = new Date();
            String maDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM,Locale.FRANCE).format(mtn);
            ligne = maDate.concat(" > " + ligne); 
            FileWriter fw = new FileWriter(System.getProperty("user.dir") + System.getProperty("file.separator") + fichierLog, true); 
            fw.write(ligne);
            fw.write(System.getProperty("line.separator"));
            fw.close();
            sw.LogServ.append(ligne+"\n"); 
        } catch (Exception ex) { 
            System.err.println("<LogServeur> Ajout ligne dans log : " + ex.getMessage()); 
        }
    }*/
    
    @Override
    public void TraceEvenements(String commentaire) { 
        addLog(commentaire); 
    }
    
}
