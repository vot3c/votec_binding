package org.openhab.binding.votecmodule.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
            return;
        }
        logger.warn("switch toogled: " + command.toString());
        logger.warn("channleUID: " + channelUID.toString());
        ChannelHandler handler = new ChannelHandler(channelUID, thing, editThing());
        // updateChannelLabel(channelUID, "hello sukru", "");
        // updateThing(handler.updateChannelType(channelUID, getBlindType().getUID()));

    }

    public Channel getBlindType() {
        List<Channel> channels = thing.getChannels();
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getChannelTypeUID().getAsString().equals("votecmodule:blind")) {
                return channels.get(i);
            }

        }
        return null;
    }

    public Channel getRelayType() {
        List<Channel> channels = thing.getChannels();
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getChannelTypeUID().getAsString().equals("votecmodule:relay")) {
                return channels.get(i);
            }

        }
        return null;
    }

    private void refreshFromState(@NonNull ChannelUID channelUID) {
        // updateState(channelUID, OnOffType.ON);
    }

    @Override
    public void initialize() {
        logger.warn("device initialized!");
        configureChannels();

        updateStatus(ThingStatus.ONLINE);

    }

    public void configureChannels() {
        Thing mThing = null;
        BigDecimal a = (BigDecimal) thing.getConfiguration().get("blindNumber");
        int blindNumber = a.intValue();
        int relayNumber = 10 - blindNumber * 2;
        ChannelHandler handler = new ChannelHandler(thing, editThing());

        if (blindNumber == 0) {

            mThing = handler.removeChannel(getBlindType().getUID());
            updateThing(mThing);
            handler = new ChannelHandler(thing, editThing());

        } else if (relayNumber == 0) {
            mThing = handler.removeChannel(getRelayType().getUID());
            updateThing(mThing);
            handler = new ChannelHandler(thing, editThing());
        } else {
            while (blindNumber > 1) {
                mThing = handler.addChannel("votecmodule:blind");
                updateThing(mThing);
                handler = new ChannelHandler(thing, editThing());
                blindNumber = blindNumber - 1;
            }
            while (relayNumber > 1) {
                mThing = handler.addChannel("votecmodule:relay");
                updateThing(mThing);
                handler = new ChannelHandler(thing, editThing());
                relayNumber = relayNumber - 1;
            }
        }

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
