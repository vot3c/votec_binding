package org.openhab.binding.votecmodule.handler;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModulesHandler extends ConfigStatusThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ModulesHandler.class);

    public ModulesHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("switch toogled");
    }

    @Override
    public void initialize() {
        logger.warn("device initialized!");
        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public Collection<@NonNull ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> collection = new ArrayList<>();
        return collection;
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        logger.warn("uptade recieved");
    }

}
