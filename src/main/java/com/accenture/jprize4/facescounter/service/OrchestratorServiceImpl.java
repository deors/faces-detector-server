package com.accenture.jprize4.facescounter.service;

import com.accenture.jprize4.facescounter.domain.MonitorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 *
 * @author m.rodriguez.tena
 */
@Component
public class OrchestratorServiceImpl implements OrchestratorService, MqttCallback {
    
    private static final Logger LOG = LoggerFactory.getLogger(OrchestratorServiceImpl.class);
    
    @Value("${mqtt.broker.url}")
    public String brokerUrl;
    @Value("${mqtt.client.id}")
    public String clientId;
    private MqttClient mqttClient;
    private final ObjectMapper jsonParser = new ObjectMapper();
    /** Single thread executor. */
    private final transient ExecutorService executorService;
    /** Map of listeners. */
    private final Map<String, Set<Subscriptor<MonitorInfo>>> mapSubscriptor;
    
    /**
     * Constructor.
     */
    public OrchestratorServiceImpl() {
        executorService = Executors.newSingleThreadExecutor();
        mapSubscriptor = new HashMap<>();
    }
    
    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId);
            final MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(true);
            mqttClient.setCallback(this);
            mqttClient.connect(opts);
        } catch (MqttException ex) {
            LOG.error("Mqtt subscriber: Unable to connect");
            LOG.error(ex.getMessage(), ex);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        try {
            if (mqttClient != null) {
                mqttClient.disconnect();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (MqttException ex) {
            LOG.error("Error disconnecting from MQTT broker");
            LOG.error(ex.getMessage(), ex);
        }
    }
    
    @Override
    public void connectionLost(Throwable thrwbl) {
        LOG.warn("MQTT broker connection lost!!!!");
        LOG.warn(thrwbl.getMessage(), thrwbl);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mm) throws Exception {
        final String payload = new String(mm.getPayload());
        LOG.debug("Message arrived: {}", payload);
        final MonitorInfo event = jsonParser.readValue(payload, MonitorInfo.class);
        event.setTopic(topic);
        Assert.hasText(event.getId());
        Assert.notNull(event.getCounter());
        final Set<Subscriptor<MonitorInfo>> subscriptors = mapSubscriptor.get(event.getTopic());
        if (subscriptors != null) {
            final Iterator<Subscriptor<MonitorInfo>> it = subscriptors.iterator();
            while (it.hasNext()) {
                final Subscriptor subscriptor = it.next();
                if (subscriptor.isConnected()) {
                    executorService.execute(() -> {
                        subscriptor.receiveEvent(event);
                    });
                } else {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        try {
            LOG.debug("Delivery complete: {}", imdt.getMessage());
        } catch (MqttException ex) {
            
        }
    }
    
    @Override
    public synchronized void subscribe(Subscriptor subscriptor, String topic) throws MqttException {
        Set<Subscriptor<MonitorInfo>> subscriptors = mapSubscriptor.get(topic);
        if (subscriptors == null) {
            subscriptors = new HashSet<>();
            subscriptors.add(subscriptor);
            mapSubscriptor.put(topic, subscriptors);
            mqttClient.subscribe(topic);
        } else {
            subscriptors.add(subscriptor);
        }
    }
    
    @Override
    public synchronized void unsubscribe(Subscriptor<MonitorInfo> subscriptor) {
//        for (Map.Entry<String, Set<Subscriptor<MonitorInfo>>> entry : mapSubscriptor.entrySet()) {
//            if (entry.getValue().remove(subscriptor)) {
//                LOG.debug("Removing listener {} from topic {}", subscriptor, entry.getKey());
//            }
//        }
        mapSubscriptor.entrySet().stream().filter((entry) -> (entry.getValue().remove(subscriptor))).forEach((entry) -> {
            LOG.debug("Removing listener {} from topic {}", subscriptor, entry.getKey());
        });
    }

    @Override
    public String getBroker() {
        return brokerUrl.substring(brokerUrl.indexOf("//") + 2);
    }
    
}
