/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.smarthomethinking.jaimaze.server;

import lombok.Data;

/**
 *
 * @author gde
 */
@Data
public class History {
    
    private Position lastPosition;
    private String action;
    private double reward;
    private Position newPosition;
    
}
