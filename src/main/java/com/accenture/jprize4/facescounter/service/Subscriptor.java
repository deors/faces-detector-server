package com.accenture.jprize4.facescounter.service;

import java.io.Serializable;

/**
 *
 * @author m.rodriguez.tena
 * @param <T>
 */
public interface Subscriptor<T extends Serializable> {
    
    void receiveEvent(T event);

    
    boolean isConnected();
}
