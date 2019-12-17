/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VERIFID;

import CONTROLID.ReponseControl;
import CONTROLID.RequeteControl;
import Interface.ConsoleServeur;
import Interface.Requete;
import db.facilities.GestionBD;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.LogServeur;
import network.Network;

/**
 *
 * @author Pierre
 */
public class RequeteVerif implements Requete, Serializable {
    public static final int CHECK_IMM = 1;
    public static final int CHECK_PERMIS = 2;
    public static final int CHECK_NUM_SECU = 3;
    
    public static final int CLOSE = 15; 
    
    private String charge;
    private int type;
    private Socket socketClient;
    ObjectInputStream in;
    
    private static Properties hashtable = new Properties();
    public Properties getHashtable(){return hashtable;}
    
    public void setCharge(String chargeUtile) {
        charge = chargeUtile;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }
    public String getCharge() {
        return charge;
    }
    
    public RequeteVerif(int t, String ch) {
        type = t;
        charge = ch;
    }
    
    /*@Override
    public Runnable createRunnable (final Socket s, final ConsoleServeur cs, final ObjectInputStream ois, final ObjectOutputStream oos)
    {        
        socketClient = s;
        
        return new Runnable() {
            @Override
            public void run() {
                if(!checkNumPlaque(s, cs, oos)) { // Si ça se passe mal
                    try {
                        s.close();
                    } catch (IOException ex) {
                        System.out.println("<createRunnable> Erreur de connection au serveur  " + ex.getMessage());
                        //Logger.getLogger(RequeteCHECKINAP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else { //System.out.println("Dans le else");
                    do {
                        RequeteVerif req = null;
                        try
                        {
                            in = new ObjectInputStream(socketClient.getInputStream()); 
                            req = (RequeteVerif)in.readObject(); // Bloquant, on attend la prochaine requete
                            System.out.println("<createRunnable> Requete lue par le serveur, instance de " + req.getClass().getName());
                            setCharge(req.getCharge()); setType(req.getType()); 
                        } 
                        catch (Exception e)
                        {
                            System.err.println("<createRunnable> " + e.getMessage()); 
                        }

                        switch(req.getType()) 
                        {
                            case CHECK_IMM : 
                                checkNumPlaque(s, cs, oos); 
                                    break;
                            case CLOSE : 
                                traiteRequeteClose(s, cs, oos); 
                                    break; 
                            default : 
                                System.out.println("<createRunnable> Code inconnu : " + getType());
                                    break;
                        }
                    }while(type != CLOSE);
                }
            }
        };
    }*/
    @Override
    public Runnable createRunnable (final Socket s, final ConsoleServeur cs, final ObjectInputStream ois, final ObjectOutputStream oos) {//System.out.println("type : " + getType());
        if (type==CHECK_IMM)
            return new Runnable() {
                @Override
                public void run() {
                    verifNumPlaque(s, cs, oos); 
                }
            };
        else if (type==CHECK_PERMIS)
            return new Runnable() {
                @Override
                public void run() {
                    verifPermis(s, cs, oos); 
                }
            };
        else if (type==CHECK_NUM_SECU)
            return new Runnable() {
                @Override
                public void run() {
                    verifNumSecu(s, cs, oos); 
                }
            };
        else if (type==CLOSE)
            return new Runnable() {
                @Override
                public void run() {
                    traiteRequeteClose(s, cs, oos); 
                }
            };
        else return null; 
    }
    
    private void verifNumSecu(Socket sock, ConsoleServeur cs, ObjectOutputStream oos) { 
        String numSecu = this.charge; 
        
        GestionBD gdb = new GestionBD(); 
        try {
            gdb.connection("bd_internat","user","user"); 
            ResultSet resSet = gdb.requete("select * from numsecu where numSecu like '" + numSecu + "' "); 
            ReponseVerif rep; 
            if(!resSet.next()) { // pas trouvé dans RegInter
                rep = new ReponseVerif(ReponseVerif.NUM_SECU_PAS_OK, "");
                oos.writeObject(rep);
                oos.flush();
            }
            else { // trouvé dans RegInter
                rep = new ReponseVerif(ReponseVerif.NUM_SECU_OK, "");
                oos.writeObject(rep);
                oos.flush();
            } 
        }
        catch(Exception e) {
            System.err.println("<verifNumSecu> " + e.getMessage());
        } 
    }
    
    private void verifPermis(Socket sock, ConsoleServeur cs, ObjectOutputStream oos) { 
        String numPermis = this.charge; 
        
        GestionBD gdb = new GestionBD(); 
        try {
            gdb.connection("bd_internat","user","user"); 
            ResultSet resSet = gdb.requete("select * from permis where idpermis like '" + numPermis + "' "); 
            ReponseVerif rep; 
            if(!resSet.next()) { // pas trouvé dans RegInter
                rep = new ReponseVerif(ReponseVerif.PERMIS_PAS_OK, "");
                oos.writeObject(rep);
                oos.flush();
            }
            else { // trouvé dans RegInter
                //rep = new ReponseVerif(ReponseVerif.PERMIS_OK, resSet.getString(2));
                rep = new ReponseVerif(ReponseVerif.PERMIS_OK, "");
                oos.writeObject(rep);
                oos.flush();
            } 
        }
        catch(Exception e) {
            System.err.println("<verifPermis> " + e.getMessage());
        } 
    }
    
    private boolean verifNumPlaque(Socket sock, ConsoleServeur cs, ObjectOutputStream oos) {
        String numPlaque = this.charge; 
        
        GestionBD gdb = new GestionBD(); 
        try {
            gdb.connection("bd_internat","user","user"); 
            ResultSet resSet = gdb.requete("select * from plaques where idplaques like '" + numPlaque + "' "); 
            ReponseVerif rep; 
            if(!resSet.next()) { // pas trouvé dans RegInter
                rep = new ReponseVerif(ReponseVerif.NUM_INT_PAS_OK, "");
                oos.writeObject(rep);
                oos.flush();
                return false; 
            }
            else { // trouvé dans RegInter
                rep = new ReponseVerif(ReponseVerif.NUM_INT_OK, resSet.getString(2));
                oos.writeObject(rep);
                oos.flush();
                return true; 
            } 
        }
        catch(Exception e) {
            System.err.println("<verifNumPlaque> " + e.getMessage());
            return false; 
        } 
    }
    
    private void traiteRequeteClose(Socket sock, ConsoleServeur cs, ObjectOutputStream oos)
    {
        try {
            ReponseVerif rep;
            rep = new ReponseVerif(ReponseVerif.CLOSE , "Fermeture");
            
            try {
                //oos = new ObjectOutputStream(sock.getOutputStream());
                oos.writeObject(rep);
                oos.flush();
            } catch (IOException ex) {
                Logger.getLogger(RequeteControl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RequeteControl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            sock.close();
        } catch (IOException ex) {
            Logger.getLogger(RequeteControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
