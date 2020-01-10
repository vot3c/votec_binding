package org.openhab.binding.votecmodule.handler;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModulesHandler extends ConfigStatusThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ModulesHandler.class);

    public ModulesHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
            return;
        }

        logger.warn("switch toogled: " + command.toString());

        ThingType thingTypeUID = new ThingType("votecmodule", "deneme", "hello world");

        changeThingType(thingTypeUID.getUID(), thing.getConfiguration());

    }

    private void refreshFromState(@NonNull ChannelUID channelUID) {
        updateState(channelUID, OnOffType.ON);
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

    @Override
    public Collection<@NonNull ConfigStatusMessage> getConfigStatus() {
        // TODO Auto-generated method stub
        Collection<ConfigStatusMessage> configStatus = new ArrayList<>();
        return configStatus;
    }

}
