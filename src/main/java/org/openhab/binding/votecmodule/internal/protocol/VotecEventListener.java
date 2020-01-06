package org.openhab.binding.votecmodule.internal.protocol;

import java.util.ArrayList;

public interface VotecEventListener {

    void VotecIncomingEvent(ArrayList<Integer> command, ArrayList<Integer> data);

}
