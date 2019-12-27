package org.openhab.binding.votecmodule.internal.protocol;

import java.util.ArrayList;

import org.openhab.binding.votecmodule.handler.VotecModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialMessage {

    private final Logger logger = LoggerFactory.getLogger(VotecModuleHandler.class);

    private static ArrayList<VotecEventListener> listeners = new ArrayList<VotecEventListener>();

    String message;

    public void setMessage(String mString) {
        this.message = mString;
        for (VotecEventListener votecEventListener : listeners) {
            votecEventListener.VotecIncomingEvent(mString);
        }
    }

    public void addListener(VotecEventListener mListener) {
        synchronized (listeners) {
            if (listeners.contains(mListener)) {
                logger.debug("Event Listener {} already registered", mListener);
                return;
            }
            listeners.add(mListener);
        }
    }

    public void removeListener(VotecEventListener mListener) {
        synchronized (listeners) {
            listeners.remove(mListener);
        }

    }
}
