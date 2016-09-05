package com.accenture.jprize4.facescounter.ui.widget;

import com.accenture.jprize4.facescounter.domain.MonitorInfo;
import com.accenture.jprize4.facescounter.service.Subscriptor;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.ui.CustomComponent;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author m.rodriguez.tena
 */
public class TimeLineChart extends CustomComponent implements Subscriptor<MonitorInfo> {

    private final ScheduledExecutorService clock = new ScheduledThreadPoolExecutor(1);
    private final Chart chart = new Chart();
    private final Configuration chartConfiguration;
    private final String topic;

    public TimeLineChart(String topic) {
        super();
        this.topic = topic;
        chart.setTimeline(true);
        chart.setSizeFull();
        chart.setCaption(topic);
        chartConfiguration = chart.getConfiguration();
        clock.scheduleAtFixedRate(() -> {
            addPointAll(0);
        }, 4000, 1000, TimeUnit.MILLISECONDS);
        super.setCompositionRoot(chart);
    }

    @Override
    public void receiveEvent(MonitorInfo event) {
        if (topic.equals(event.getTopic())) {
            addPoint(event.getId(), event.getCounter());
        }
    }

    /**
     * Gets if this client is connected to the server.
     *
     * @return true if this client is connected to the server
     */
    @Override
    public boolean isConnected() {
        return super.getUI().getPushConnection() != null;
    }

    @Override
    public void detach() {
        clock.shutdownNow();
        super.detach();
    }
    
    private void addPoint(String id, int y) {
        super.getUI().access(() -> {
            final DataSeriesItem dsi = new DataSeriesItem(Calendar.getInstance().getTime(), y);
            boolean found = false;
            final List<Series> seriesList = chartConfiguration.getSeries();
            if (seriesList != null) {
                for (Series series : seriesList) {
                    if (series.getId().equals(id)) {
                        found = true;
                        ((DataSeries) series).add(dsi, true, false);
                    }
                }
            }
            if (!found) {
                final DataSeries series = new DataSeries(id);
                series.setId(id);
                series.add(dsi);
                chartConfiguration.addSeries(series);
                chart.drawChart();
            }
        });
    }
    
    private void addPointAll(int y) {      
        final List<Series> seriesList = chartConfiguration.getSeries();
        seriesList.stream().forEach((s) -> {
            addPoint(s.getId(), y);
        });
    }

}
