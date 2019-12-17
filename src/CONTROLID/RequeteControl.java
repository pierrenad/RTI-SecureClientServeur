/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CONTROLID;

import Fenetres.GuardianWindow;
import Interface.*;
import Message.MsgSigned;
import Message.SecretMsg;
import VERIFID.*;
import java.io.ObjectInputStream;
import java.net.Socket;
import db.facilities.GestionBD;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import network.Network;

/**
 *
 * @author Pierre
 */
public class RequeteControl implements Requete, Serializable {
    public static final int LOGIN = 0; 
    public static final int CHECK_NUM = 1;
    public static final int HANDSHAKE = 2;
    public static final int VERIF_PERMIS = 3; 
    public static final int CHECK_NUM_SECU = 4; 
    
    public static final int CLOSE = 15; 
    
    private String charge;
    private int type;
    private MsgSigned msg; 
    private SecretMsg sm; 
    private Socket socketClient;
    ObjectInputStream in;
    private SecretKey authKey;
    private SecretKey convKey;
    
    private static Properties hashtable = new Properties();
    
    public Properties getHashtable(){return hashtable;}
    
    public void setCharge(String chargeUtile) {
        charge = chargeUtile;
    }
    public void setType(int type) {
        this.type = type;
    }
    public void setMsg(MsgSigned msg) {
        this.msg = msg; 
    }
    public void setSm(SecretMsg secretMsg) {
        this.sm = secretMsg; 
    }
    public void setAuthKey(SecretKey cleAuth) {
        this.authKey = cleAuth;
    }
    public void setConvKey(SecretKey cleConv) {
        this.convKey = cleConv;
    }
    public int getType() {
        return type;
    }
    public String getCharge() {
        return charge;
    }
    public MsgSigned getMsg() {
        return msg;
    }
    public SecretMsg getSm() {
        return sm;
    }
    public SecretKey getAuthKey() {
        return authKey;
    }
    public SecretKey getConvKey() {
        return convKey;
    }
    
    public RequeteControl(int t, String ch) {
        type = t;
        charge = ch;
    }
    public RequeteControl(int t, MsgSigned msg) {
        type = t;
        this.msg = msg; 
    }
    public RequeteControl(int t, SecretMsg secretMsg) {
        type = t;
        sm = secretMsg;
    }
    
    /*@Override
    public Runnable createRunnable(Socket s, ObjectInputStream ois, LogServeur cs) {
        if(type == CHECK_NUM) {
            return new Runnable() {
                public void run() {
                    checkNumPlaque(s, cs);
                }
            }; 
        }
        else return null;
    }*/
    @Override
    public Runnable createRunnable (final Socket s, final ConsoleServeur cs, final ObjectInputStream ois, final ObjectOutputStream oos)
    {        
        socketClient = s;
        
        return new Runnable() {
            @Override
            public void run() {
                if(!traiteRequeteLogin(s, cs, in, oos)) { // Si ça se passe mal  
                    try {
                        s.close();
                    } catch (IOException ex) {
                        System.out.println("<createRunnable> Erreur de connection au serveur  " + ex.getMessage());
                        //Logger.getLogger(RequeteCHECKINAP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else { //System.out.println("Dans le else");
                    do {
                        RequeteControl req = null;
                        try
                        {
                            in = new ObjectInputStream(socketClient.getInputStream()); 
                            req = (RequeteControl)in.readObject(); // Bloquant, on attend la prochaine requete
                            System.out.println("<createRunnable> Requete lue par le serveur, instance de " + req.getClass().getName());
                            setCharge(req.getCharge()); setType(req.getType()); 

                            switch(req.getType()) 
                            {
                                case LOGIN : 
                                    traiteRequeteLogin(s, cs, ois, oos); 
                                    break; 
                                case CHECK_NUM : 
                                    setMsg(req.getMsg()); 
                                    checkNumPlaque(s, cs, ois, oos); 
                                    break;
                                case HANDSHAKE : 
                                    setSm(req.getSm());
                                    traiteRequeteHandshake(s, cs, ois, oos); 
                                    break;
                                case VERIF_PERMIS: 
                                    setMsg(req.getMsg()); 
                                    checkPermis(s, cs, ois, oos); 
                                    break;
                                case CHECK_NUM_SECU: 
                                    setSm(req.getSm()); 
                                    checkNumSecu(s, cs, ois, oos); 
                                    break;
                                case CLOSE : 
                                    traiteRequeteClose(s, cs, oos); 
                                    break;
                                default : 
                                    System.out.println("<createRunnable> Code inconnu : " + getType());
                                    break;
                            }
                        } 
                        catch (Exception e)
                        {
                            System.err.println("<createRunnable> " + e.getMessage()); 
                            setType(CLOSE);
                        }
                    }while(getType() != CLOSE); 
                }
            }
        };
    }

    private void checkNumSecu(Socket s, ConsoleServeur ls, ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            Cipher dechiffre;
            dechiffre = Cipher.getInstance("DES/ECB/PKCS5Padding", "BC");
            dechiffre.init(Cipher.DECRYPT_MODE, getConvKey());

            byte[] NumSecu = dechiffre.doFinal(sm.getSecretKey());
            
            String numSecu = new String(NumSecu);
            
            System.out.println("numsecu");
            
            byte[] hashe = sm.getSignature();
            
            Mac hmac = Mac.getInstance("HmacMD5");
            hmac.init(getAuthKey());
            
            hmac.update(numSecu.getBytes());
            byte[] hb = hmac.doFinal();
            
            String mess = ""; 
            if(MessageDigest.isEqual(hashe, hb)) {
                System.out.println("EGAL");
                GestionBD gdb = new GestionBD(); 
                gdb.connection("BD_REGNAT","user","user"); 
                ResultSet resSet = gdb.requete("select * from numsecu where numSecu like '" + numSecu + "' "); 
                
                ReponseControl rep = null; 
                if(!resSet.next()) { // pas trouvé dans RegNat
                    RequeteVerif req = new RequeteVerif(RequeteVerif.CHECK_NUM_SECU, numSecu); 
                    Network n = new Network(); 
                    Socket sock = n.InitInter(); 
                    n.SendRequest(sock, req); // on demande de chercher la plaque dans registre international 

                    ReponseVerif rep2 = null;
                    try {
                        in = new ObjectInputStream(sock.getInputStream()); 
                        rep2 = (ReponseVerif)in.readObject(); 
                        System.out.println("<checkPermis> *** Reponse reçue");

                        if(rep2.getCode() == ReponseVerif.NUM_SECU_OK) { 
                            mess = "I"; //"I@"+rep2.getCharge(); 
                            rep = new ReponseControl(ReponseControl.NUMSECU_OK, mess);
                        }
                        else { 
                            mess = " "; //" @ "; 
                            rep = new ReponseControl(ReponseControl.NUMSECU_PAS_OK, mess);
                        }
                    }
                    catch (Exception e) { 
                        System.err.println("<checkPermis> " + e.getMessage()); 
                    } 
                }
                else { // trouvé dans national
                    mess = "N"; //"N@"+resSet.getString(2); 
                    rep = new ReponseControl(ReponseControl.NUMSECU_OK, mess);
                }
                
                Mac hmac2 = Mac.getInstance("HmacMD5");
                hmac2.init(getAuthKey());

                hmac2.update(String.valueOf(rep.getCode()).getBytes());
                byte[] hf2 = hmac2.doFinal();
                
                Cipher chiffrement = Cipher.getInstance("DES/ECB/PKCS5Padding", "BC");
                chiffrement.init(Cipher.ENCRYPT_MODE, getConvKey());

                byte[] chi = chiffrement.doFinal(String.valueOf(rep.getCode()).getBytes());

                rep.setCharge(Base64.getEncoder().encodeToString(hf2) + "#" + Base64.getEncoder().encodeToString(chi) + "#"); // dernier au cas ou 
                
                oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(rep);
                oos.flush();
            }
        }
        catch(Exception e) {
            //System.err.println("<checkNumSecu> " + e); 
            Logger.getLogger(RequeteControl.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void checkPermis(Socket s, ConsoleServeur ls, ObjectInputStream ois, ObjectOutputStream oos) {
        byte[] NumPermis = msg.getMessage(); 
        byte[] hamac = msg.getSignature(); 
        String numPermis = new String(NumPermis); 
        String mess = ""; 
        
        Mac hmac;
        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // bouncycastle 
        
        try {
            hmac = Mac.getInstance("HmacMD5");
            hmac.init(getAuthKey()); // Exception => key is null ??? ----------------- 
            
            hmac.update(NumPermis);
            byte[] hf = hmac.doFinal();
            
            System.out.println("h : " + hamac);
            System.out.println("h local : " + hf);
            
            if(MessageDigest.isEqual(hamac, hf))
            {
                System.out.println("EGAL");
                GestionBD gdb = new GestionBD(); 
                gdb.connection("BD_REGNAT","user","user"); 
                ResultSet resSet = gdb.requete("select * from permis where idpermis like '" + numPermis + "' "); 
                ReponseControl rep = null; 
                if(!resSet.next()) { // pas trouvé dans RegNat
                    RequeteVerif req = new RequeteVerif(RequeteVerif.CHECK_PERMIS, numPermis); 
                    Network n = new Network(); 
                    Socket sock = n.InitInter(); 
                    n.SendRequest(sock, req); // on demande de chercher la plaque dans registre international 

                    ReponseVerif rep2 = null;
                    try {
                        in = new ObjectInputStream(sock.getInputStream()); 
                        rep2 = (ReponseVerif)in.readObject(); 
                        System.out.println("<checkPermis> *** Reponse reçue");

                        if(rep2.getCode() == ReponseVerif.PERMIS_OK) { 
                            mess = "I"; //"I@"+rep2.getCharge(); 
                            rep = new ReponseControl(ReponseControl.PERMIS_OK, mess);
                        }
                        else { 
                            mess = " "; //" @ "; 
                            rep = new ReponseControl(ReponseControl.PERMIS_PAS_OK, mess);
                        }
                    }
                    catch (Exception e) { 
                        System.err.println("<checkPermis> " + e.getMessage()); 
                    } 
                }
                else { // trouvé dans national
                    mess = "N"; //"N@"+resSet.getString(2); 
                    rep = new ReponseControl(ReponseControl.PERMIS_OK, mess);
                }
                /*if(gdb.checkPermis(permis)) 
                    rep = new ReponseCONTROLID(ReponseCONTROLID.PERMIS_OK , "PERMIS OK");
                else
                    rep = new ReponseCONTROLID(ReponseCONTROLID.PERMIS_PASOK , "PERMIS PAS OK");*/
                
                Mac hmac2 = Mac.getInstance("HmacMD5");
                hmac2.init(getAuthKey());

                hmac2.update(String.valueOf(rep.getCode()).getBytes());
                byte[] hf2 = hmac2.doFinal();

                rep.setCharge(Base64.getEncoder().encodeToString(hf2)); 
                
                oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(rep);
                oos.flush();
            }
            else {
                System.out.println("PAS EGAL");
            }
        }
        catch(Exception e) {
            //System.err.println("<checkPermis> " + e); 
            Logger.getLogger(RequeteControl.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void traiteRequeteHandshake(Socket s, ConsoleServeur ls, ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            KeyStore ks = null;
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("C:\\Users\\Pierre\\Documents\\Ecole\\RTI\\Dossier4\\RTI4_SecureCliServ\\ks\\KeystoreServ.jks"), "rootroot".toCharArray());

            X509Certificate cert = (X509Certificate)ks.getCertificate("cert_cli");

            PublicKey publicKey;
            publicKey = cert.getPublicKey();

            Cipher dechiffre = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            dechiffre.init(Cipher.DECRYPT_MODE, publicKey);

            byte[] cs1 = dechiffre.doFinal(sm.getSecretKey());
            
            RequeteControl req = null;
            in = new ObjectInputStream(socketClient.getInputStream());
            req = (RequeteControl)in.readObject();
            setSm(req.getSm());
            
            byte[] cs2 = dechiffre.doFinal(sm.getSecretKey());
                        
            setAuthKey(new SecretKeySpec(cs1, "DES"));
            setConvKey(new SecretKeySpec(cs2, "DES"));
        } 
        catch (Exception ex) {
            System.err.println("<Handshake> " + ex + ex.getMessage()); 
        }
    }
    
    private void checkNumPlaque(Socket s, ConsoleServeur ls, ObjectInputStream ois, ObjectOutputStream oos) {
        byte[] NumPlaque = msg.getMessage(); 
        byte[] signature = msg.getSignature(); 
        String numPlaque = new String(NumPlaque); 
        boolean ok = false; 
        
        try { 
            KeyStore ks = null;
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("C:\\Users\\Pierre\\Documents\\Ecole\\RTI\\Dossier4\\RTI4_SecureCliServ\\ks\\KeystoreServ.jks"), "rootroot".toCharArray());

            X509Certificate certif = (X509Certificate)ks.getCertificate("cert_cli");

            PublicKey cléPublique;
            cléPublique = certif.getPublicKey();

            System.out.println("*** Cle publique recuperee = "+cléPublique.toString());
            System.out.println("Debut de verification de la signature construite");
            // confection d'une signature locale
            Signature si = Signature.getInstance("SHA1withRSA");
            si.initVerify(cléPublique);
            System.out.println("Hachage du message");
            si.update(NumPlaque);
            System.out.println("Verification de la signature construite");
            ok = si.verify(signature); 
            String mess = ""; 
            
            if(ok) { 
                GestionBD gdb = new GestionBD(); 
                try {
                    gdb.connection("BD_REGNAT","user","user"); 
                    ResultSet resSet = gdb.requete("select * from plaques where idplaques like '" + numPlaque + "' "); 
                    ReponseControl rep = null; 
                    if(!resSet.next()) { // pas trouvé dans RegNat
                        RequeteVerif req = new RequeteVerif(RequeteVerif.CHECK_IMM, numPlaque); 
                        Network n = new Network(); 
                        Socket sock = n.InitInter(); 
                        n.SendRequest(sock, req); // on demande de chercher la plaque dans registre international 

                        ReponseVerif rep2 = null;
                        try {
                            in = new ObjectInputStream(sock.getInputStream()); 
                            rep2 = (ReponseVerif)in.readObject(); 
                            System.out.println("<checkNumPlaque> *** Reponse reçue");

                            if(rep2.getCode() == ReponseVerif.NUM_INT_OK) { 
                                mess = "I@"+rep2.getCharge(); 
                                rep = new ReponseControl(ReponseControl.NUM_OK, mess);
                            }
                            else { 
                                mess = " @ "; 
                                rep = new ReponseControl(ReponseControl.NUM_PAS_OK, mess);
                            }
                        }
                        catch (Exception e) { 
                            System.err.println("<checkNumPlaque1> " + e.getMessage()); 
                        } 
                    }
                    else { // si dans registre national 
                        mess = "N@"+resSet.getString(2); 
                        rep = new ReponseControl(ReponseControl.NUM_OK, mess);
                    } 

                    // signature de la réponse 
                    try {
                            ks.load(new FileInputStream("C:\\Users\\Pierre\\Documents\\Ecole\\RTI\\Dossier4\\RTI4_SecureCliServ\\ks\\KeystoreServ.jks"), "rootroot".toCharArray());
                    } catch (CertificateException ex) {
                        Logger.getLogger(GuardianWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    PrivateKey cléPrivée;
                    cléPrivée = (PrivateKey) ks.getKey("cle_serv", "rootroot".toCharArray());

                    si.initSign(cléPrivée); 
                    si.update(String.valueOf(rep.getCode()).getBytes());
                    byte[] sign = si.sign(); 
                    rep.setCharge(mess+"#"+Base64.getEncoder().encodeToString(sign)); 
                    
                    //ObjectOutputStream oos;
                    oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(rep);
                    oos.flush();
                }
                catch(Exception e) {
                    System.err.println("<checkNumPlaque2> " + e.getMessage());
                } 
            }
        }
        catch(Exception exep) {
            System.err.println("<checkNumPlaque3> " + exep.getMessage()); 
        }
    }

    private boolean traiteRequeteLogin(Socket sock, ConsoleServeur ls, ObjectInputStream ois, ObjectOutputStream oos) {
        String user = msg.getUser();
        long temps = msg.getTemps();
        double alea = msg.getAlea();
        byte[] msgD = msg.getDigest();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // bouncycastle 
        
        ReponseControl rep; 
        
        try { 
            try {
                FileInputStream in = new FileInputStream("config.properties");
                getHashtable().load(in);
            } catch (Exception ex) {
                System.err.println("<RequeteControl> " + ex.getMessage()); 
            } 

            String pwd = null;

            pwd = getHashtable().getProperty(user);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(user.getBytes());
            md.update(pwd.getBytes());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream bdos = new DataOutputStream(baos);

            bdos.writeLong(temps); 
            bdos.writeDouble(alea);

            md.update(baos.toByteArray());
            byte[] msgDLocal = md.digest();

            if(MessageDigest.isEqual(msgD, msgDLocal)) {
                System.out.println("<RequeteControl> Logged");
                ls.TraceEvenements("Logged # RequeteControl"); 
                rep = new ReponseControl(ReponseControl.LOG_OK, "");
                try {
                    //oos = new ObjectOutputStream(sock.getOutputStream());
                    oos.writeObject(rep); 
                    oos.flush();
                    return true; 
                } 
                catch (IOException e) {
                    System.err.println("<RequeteControl> Erreur réseau "+  e.getMessage());
                    rep = new ReponseControl(ReponseControl.LOG_PAS_OK , "");

                    try {
                        //oos = new ObjectOutputStream(sock.getOutputStream());
                        oos.writeObject(rep);
                        oos.flush();
                        return false; 
                    } catch (IOException ex) { 
                        Logger.getLogger(RequeteControl.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                }
            }
            else {
                System.err.println("<RequeteControl> Erreur de login/password"); 
                rep = new ReponseControl(ReponseControl.LOG_PAS_OK , "");

                try {
                    //oos = new ObjectOutputStream(sock.getOutputStream());
                    oos.writeObject(rep);
                    oos.flush();
                    return false; 
                } catch (IOException ex) { 
                    Logger.getLogger(RequeteControl.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
        catch(Exception e) {
            System.err.println("<RequeteControl> " + e + e.getMessage()); 
        }
        return false; 
    }
    
    private boolean logged(Socket sock, ConsoleServeur ls, ObjectInputStream ois, ObjectOutputStream oos) {
        return true; 
    }
    
    private void traiteRequeteClose(Socket sock, ConsoleServeur cs, ObjectOutputStream oos)
    {
        try {
            ReponseControl rep;
            rep = new ReponseControl(ReponseControl.CLOSE , "Fermeture");
            
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
