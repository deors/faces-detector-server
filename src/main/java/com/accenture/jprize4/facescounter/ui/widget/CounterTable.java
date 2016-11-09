package com.accenture.jprize4.facescounter.ui.widget;

import com.accenture.jprize4.facescounter.domain.MonitorInfo;
import com.accenture.jprize4.facescounter.service.Subscriptor;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.Table;

/**
 *
 * @author m.rodriguez.tena
 */
public class CounterTable extends Table implements Subscriptor<MonitorInfo> {
    
    private final BeanItemContainer<MonitorInfo> datasource = new BeanItemContainer(MonitorInfo.class);
    private final String topic;

    public CounterTable(String topic) {
        super(topic);
        this.topic = topic;
        super.setContainerDataSource(datasource);
        super.setVisibleColumns("id", "counter");
        super.setColumnHeaders("id", "counter");
    }

    @Override
    public void receiveEvent(final MonitorInfo event) {
        if (topic.equals(event.getTopic())) {
            super.getUI().access(new Runnable() {
                @Override
                public void run() {
                    
                    final BeanItem<MonitorInfo> item = datasource.getItem(event);
                    if (item == null) {
                        datasource.addItem(event);
                    } else {
                        final MethodProperty<Integer> property =
                                (MethodProperty<Integer>) item.getItemProperty("counter");
                        property.setValue(property.getValue() + event.getCounter());
                        // Forces a value change so the grid value is updated
                        property.fireValueChange();
                    }
                }
            });
        }
        
    }

    /**
     * Gets if this client is connected to the server.
     * @return true if this client is connected to the server
     */
    @Override
    public boolean isConnected() {
        return super.getUI().getPushConnection() != null;
    }
    
}
