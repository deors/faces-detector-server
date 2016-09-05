package com.accenture.jprize4.facescounter.service;

import com.accenture.jprize4.facescounter.domain.MonitorInfo;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author m.rodriguez.tena
 */
public interface OrchestratorService {
    
    void subscribe(Subscriptor subscriptor, String topic) throws MqttException;
    void unsubscribe(Subscriptor<MonitorInfo> subscriptor);
    String getBroker();
    
}
