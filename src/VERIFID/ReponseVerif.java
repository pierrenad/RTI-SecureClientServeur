/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VERIFID;

import Interface.Reponse;
import java.io.Serializable;

/**
 *
 * @author Pierre
 */
public class ReponseVerif implements Reponse, Serializable {
    public static int NUM_INT_OK = 100;
    public static int NUM_INT_PAS_OK = 101;
    public static int PERMIS_OK = 102;
    public static int PERMIS_PAS_OK =103; 
    public static int NUM_SECU_OK = 104;
    public static int NUM_SECU_PAS_OK =105; 
    
    public static int CLOSE = 250; 
    
    private int code;
    private String charge;
    
    public ReponseVerif(int c, String chu)
    {
        code = c;
        setCharge(chu);
    }
    
    @Override
    public int getCode() { return code; }
    public String getCharge() { return charge; }
    public void setCharge(String chargeUtile) { this.charge = chargeUtile; } 
}
