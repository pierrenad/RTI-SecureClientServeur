/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.Serializable;

/**
 *
 * @author Pierre
 */
public final class MsgSigned implements Serializable {
    // message
    private byte[] message;
    private byte[] signature;
    
    public void setMessage(byte[] message) {
        this.message = message;
    }
    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
    
    public byte[] getMessage() {
        return message;
    }
    public byte[] getSignature() {
        return signature;
    }
    
    //login
    private String user;
    private long temps;
    private double alea;
    private byte[] digest;
    
    public void setUser(String user) {
        this.user = user;
    }
    public void setTemps(long temps) {
        this.temps = temps;
    }
    public void setAlea(double alea) {
        this.alea = alea;
    }
    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public String getUser() {
        return user;
    }
    public long getTemps() {
        return temps;
    }
    public double getAlea() {
        return alea;
    }
    public byte[] getDigest() {
        return digest;
    }
    
    public MsgSigned(byte [] m, byte [] s) {
        setMessage(m);
        setSignature(s);
    }
    public MsgSigned(String u, long t, double a, byte[] dig)
    {
        setUser(u);
        setTemps(t);
        setAlea(a);
        setDigest(dig);
    }
}
