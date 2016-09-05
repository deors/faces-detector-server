package com.accenture.jprize4.facescounter.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Mariano
 */
public class MonitorInfo implements Serializable {
    private String id;
    private Integer counter;
    private String topic;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Integer getCounter() {
        return this.counter;
    }
    
    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MonitorInfo other = (MonitorInfo) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    

    @Override
    public String toString() {
        return "MonitorInfo{" + "id=" + id + ", counter=" + counter + ", topic=" + topic + '}';
    }
    
    
    
    
}
