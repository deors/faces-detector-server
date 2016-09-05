package com.accenture.jprize4.facescounter.ui;

import com.accenture.jprize4.facescounter.service.OrchestratorService;
import com.accenture.jprize4.facescounter.ui.widget.TimeLineChart;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@SpringUI
@Push
@Theme("reindeer")
@Widgetset("com.accenture.jprize4.facescounter.ui.AppWidgetSet")
public class MyUI extends UI {
    
    @Autowired
    private OrchestratorService service;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout mainLayout = new VerticalLayout();
        final HorizontalLayout inputLayout = new HorizontalLayout();
        final HorizontalLayout tablesLayout = new HorizontalLayout();
        
        final Label connectedLabel = new Label("Connected to " + service.getBroker());
        connectedLabel.setStyleName("connect");
        
        
        final TextField topicTextField = new TextField();
        topicTextField.setSizeFull();

        final Button button = new Button("Subscribe");
        button.addClickListener(e -> {
            try {
//                final CounterTable customComponent = new CounterTable(topicTextField.getValue());
                final TimeLineChart customComponent = new TimeLineChart(topicTextField.getValue());
                service.subscribe(customComponent, topicTextField.getValue());
                tablesLayout.addComponent(customComponent);
            } catch (MqttException ex) {
                final Notification notification = new Notification("Error", ex.getMessage());
                notification.show(Page.getCurrent());
            }
        });
        
        //<editor-fold defaultstate="collapsed" desc="Layout adjustments">
        connectedLabel.setWidth("50%");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();
        tablesLayout.setSpacing(true);
        inputLayout.addComponents(topicTextField, button);
        inputLayout.setWidth("50%");
        mainLayout.addComponents(connectedLabel, inputLayout, tablesLayout);
        mainLayout.setComponentAlignment(connectedLabel, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(inputLayout, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(tablesLayout, Alignment.TOP_CENTER);
        mainLayout.setExpandRatio(tablesLayout, 3);
        //</editor-fold>
        
        setContent(mainLayout);

    }
    
    

    
}
