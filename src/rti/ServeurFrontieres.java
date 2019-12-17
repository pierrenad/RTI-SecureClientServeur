/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti;

import Interface.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import log.LogServeur;

/**
 *
 * @author Pierre
 */
public class ServeurFrontieres extends Thread {
    private int port;
    private ListeTaches tachesAExecuter; 
    private LogServeur logServ; 
    private ServerSocket SSocket = null;
    private int maxClient = 3; 

    public ServeurFrontieres(int p, ListeTaches lt, LogServeur ls) { 
       port = p; tachesAExecuter = lt; logServ = ls;

        try { 
            FileInputStream in = new FileInputStream("donnees.properties");
            Properties data = new Properties();
            data.load(in);
            maxClient = Integer.parseInt((String) data.get("maxClient"));
        } catch (Exception e) {
            System.err.println("<ServeurFrontieres> " + e.getMessage()); 
        }
    }

    @Override
    public void run()
    {
        try {
           SSocket = new ServerSocket(port);
        }
        catch (IOException e) {
           System.err.println("<ServeurFrontieres> Erreur de port : " + e.getMessage()); 
           System.exit(1);
        }
        // Démarrage du pool de threads
        logServ.TraceEvenements("Démarrage pool de thread # serveur frontiere"); 
        for (int i=0; i<maxClient; i++) { 
           Client th = new Client (tachesAExecuter, "Thread du pool n°" + String.valueOf(i), logServ); 
           th.start();
        } 

        // Mise en attente du serveur
        Socket CSocket = null;
        while (!isInterrupted()) {
            try {
                System.out.println("<ServeurFrontieres> ************ Serveur en attente");
                logServ.TraceEvenements("************ Serveur en attente # serveur frontiere");
                CSocket = SSocket.accept();
                logServ.TraceEvenements(CSocket.getRemoteSocketAddress().toString()+" # accept # serveur frontiere");
            }
            catch (IOException e) {
                System.err.println("<ServeurFrontieres> Erreur d'accept : " + e.getMessage()); 
                System.exit(1);
            }

            ObjectInputStream ois= null; 
            Requete req = null;
            try {
                ois = new ObjectInputStream(CSocket.getInputStream());
                req = (Requete)ois.readObject();
                System.out.println("<ServeurFrontieres> Requete lue par le serveur");
                logServ.TraceEvenements("Requete lue par le serveur # serveur frontiere");
            }
            catch (Exception e) {
                System.err.println("<ServeurFrontieres> Erreur " + e.getMessage()); 
            } 

            try { 
                Runnable travail = req.createRunnable(CSocket, logServ, ois, new ObjectOutputStream(CSocket.getOutputStream())); 
                if (travail != null) {
                    tachesAExecuter.recordTache(travail);
                    System.out.println("<ServeurFrontieres> Travail mis dans la file");
                    logServ.TraceEvenements("Ajout d'un travail dans la file # serveur frontiere");
                }
                else {
                    System.out.println("<ServeurFrontieres> Pas de mise en file");
                    logServ.TraceEvenements("Pas de mise en file # serveur frontiere");
                }
            }
            catch(IOException e) {
                System.err.println("<ServeurFrontieres> Erreur de runnable : " + e.getMessage()); 
            }
        }
    }
}
