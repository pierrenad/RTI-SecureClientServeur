/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

/**
 *
 * @author tiboo, the bg
 */
public interface SourceTache
// synchronized ne s'utilise pas dans un interface
{
 public Runnable getTache() throws InterruptedException;
 public boolean existTache();
 public void recordTache (Runnable r);
}
