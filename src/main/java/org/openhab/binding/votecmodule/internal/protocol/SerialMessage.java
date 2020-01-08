package org.openhab.binding.votecmodule.internal.protocol;

import java.util.ArrayList;

import org.openhab.binding.votecmodule.handler.VotecControllerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialMessage {

    private final Logger logger = LoggerFactory.getLogger(VotecControllerHandler.class);

    private static ArrayList<VotecEventListener> listeners = new ArrayList<VotecEventListener>();

    ArrayList<Integer> message = null;
    ArrayList<Integer> command = null;
    ArrayList<Integer> data = null;

    public void setMessage(ArrayList<Integer> incomingArrayList) {
        this.message = incomingArrayList;
        parseMessage();
        for (VotecEventListener votecEventListener : listeners) {
            votecEventListener.VotecIncomingEvent(getCommand(), getData());
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

    public void parseMessage() {
        if (message.isEmpty()) {
            logger.warn("Input Message is empty!");
            return;
        }
        int star = message.indexOf(42);
        int hash = message.indexOf(35);

        command = new ArrayList<Integer>();
        data = new ArrayList<Integer>();

        if (star > 0) {
            for (int i = 0; i < star; i++) {
                command.add(message.get(i));
            }
        }
        if (hash > star) {
            for (int i = star + 1; i < hash; i++) {
                data.add(message.get(i));
            }
        }
    }

    public ArrayList<Integer> getCommand() {
        return this.command;
    }

    public ArrayList<Integer> getData() {
        return this.data;
    }

}
