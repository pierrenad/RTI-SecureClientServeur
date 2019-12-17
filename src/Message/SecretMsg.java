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
public final class SecretMsg implements Serializable {
    private byte[] secretKey;
    private byte[] signature;
    
    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] cle) {
        this.secretKey = cle;
    }

    public void setSignature(byte[] sign) {
        this.signature = sign; 
    }

    public byte[] getSignature() {
        return signature;
    }
    
    public SecretMsg(byte [] s, byte [] s2) {
        setSecretKey(s);
        setSignature(s2);
    }
    
    public SecretMsg(byte [] s) {
        setSecretKey(s);
    }
}
