/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CONTROLID;

import Interface.Reponse;
import java.io.Serializable;

/**
 *
 * @author Pierre
 */
public class ReponseControl implements Reponse, Serializable {
    public static int LOG_OK = 201; 
    public static int LOG_PAS_OK = 202; 
    public static int NUM_OK = 203; 
    public static int NUM_PAS_OK = 204;
    public static int PERMIS_OK = 205; 
    public static int PERMIS_PAS_OK = 206; 
    public static int NUMSECU_OK = 207; 
    public static int NUMSECU_PAS_OK = 208; 
    
    public static int CLOSE = 250; 
    
    private int code;
    private String charge;
    
    public ReponseControl(int c, String chu)
    {
        code = c;
        setCharge(chu);
    }
    
    @Override
    public int getCode() { return code; }
    public String getCharge() { return charge; }
    public void setCharge(String chargeUtile) { this.charge = chargeUtile; } 
}
