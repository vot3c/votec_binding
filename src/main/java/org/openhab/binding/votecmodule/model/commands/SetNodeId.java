package org.openhab.binding.votecmodule.model.commands;

import org.openhab.binding.votecmodule.model.VotecCommand;

public class SetNodeId extends VotecCommand {
    /**
     * @implSpec setSerialNumber
     * @implSpec setDeviceId
     */
    public SetNodeId() {
        setBroadcast(1);
        setGroupId(1);
    }
}
