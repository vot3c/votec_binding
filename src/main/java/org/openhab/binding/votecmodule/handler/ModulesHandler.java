package org.openhab.binding.votecmodule.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModulesHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ModulesHandler.class);

    public ModulesHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String nodeId = thing.getProperties().get("node_id");
        if (nodeId.equals("20")) {

        }

        logger.warn("switch toogled");
    }

    @Override
    public void initialize() {
        logger.warn("device initialized!");
        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        logger.warn("uptade recieved");
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub
        super.channelLinked(channelUID);
        logger.warn("channel linked");
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub
        super.channelUnlinked(channelUID);
        logger.warn("channel unlinked");
    }

}
