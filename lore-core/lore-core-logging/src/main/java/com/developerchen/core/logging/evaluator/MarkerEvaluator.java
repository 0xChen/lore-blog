package com.developerchen.core.logging.evaluator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;

/**
 * MarkerEvaluator
 *
 * @author syc
 */
public class MarkerEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    @Getter
    @Setter
    private String markerName;
    private Marker targetMarker;

    @Override
    public void start() {
        initializeMarker();
        super.start();
    }

    private void initializeMarker() {
        if (markerName == null || markerName.trim().isEmpty()) {
            addError("Marker name must be configured for MarkerEvaluator");
            return;
        }

        try {
            targetMarker = MarkerFactory.getMarker(markerName.trim());
            addInfo("Configured marker: " + markerName);
        } catch (Exception e) {
            addError("Failed to initialize marker: " + markerName, e);
        }
    }

    @Override
    public boolean evaluate(ILoggingEvent event) {
        if (!isStarted() || targetMarker == null) {
            return false;
        }

        List<Marker> markerList = event.getMarkerList();
        return markerList != null && markerList.stream().anyMatch(m -> m != null && m.contains(targetMarker));
    }
}

