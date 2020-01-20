package org.openhab.binding.votecmodule.model.commands;

import org.openhab.binding.votecmodule.model.VotecCommand;

public class ScanBus extends VotecCommand {

    public ScanBus() {
        setBroadcast(1);
        setGroupId(1);
    }

}
