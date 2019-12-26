package org.openhab.binding.votecmodule.internal.protocol.event;

public abstract class VotecEvent {

    private final int id;

    public VotecEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

}
