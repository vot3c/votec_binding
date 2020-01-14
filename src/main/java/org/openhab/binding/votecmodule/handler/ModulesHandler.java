package org.openhab.binding.votecmodule.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.openhab.binding.votecmodule.internal.DataConvertor;
import org.openhab.binding.votecmodule.model.VotecCommand;
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
        String string = thing.getProperties().get("serial_number");
        byte[] packet = DataConvertor.stringIntArrayToByteArray(string);
        VotecCommand votecCommand = new VotecCommand();
        votecCommand.setSerialnumber(packet);

        votecCommand.setGroupId(1);
        String[] channelAtr = channelUID.getId().toString().split("_");
        if (channelAtr[0].equals("relay")) {
            votecCommand.setAtomicId(Integer.parseInt(channelAtr[1]));
        }
        logger.warn(votecCommand.toString());
        logger.warn(Arrays.toString(votecCommand.getPacket()));
        VotecSerialHandler.sendPackage(votecCommand.getPacket());

    }

    private void refreshFromState(@NonNull ChannelUID channelUID) {
        // TODO: set all status of channels.

    }

    @Override
    public void initialize() {
        logger.warn("device initialized!");
        configureChannels();
        updateStatus(ThingStatus.ONLINE);

    }

    public void configureChannels() {
        // clear all channels.
        removeAllChannels();

        Thing mThing = null;

        BigDecimal a = (BigDecimal) thing.getConfiguration().get("blindNumber");

        int blindNumber = a.intValue();
        int relayNumber = 10 - blindNumber * 2;

        ChannelHandler handler = new ChannelHandler(thing, editThing());

        while (relayNumber > 0) {
            mThing = handler.addChannel("votecmodule:relay");
            if (mThing != null) {
                updateThing(mThing);
                handler = new ChannelHandler(thing, editThing());
                relayNumber = relayNumber - 1;
            }

        }

        while (blindNumber > 0) {
            mThing = handler.addChannel("votecmodule:blind");
            if (mThing != null) {
                updateThing(mThing);
                handler = new ChannelHandler(thing, editThing());
                blindNumber = blindNumber - 1;
            }
        }
    }

    public void removeAllChannels() {
        List<Channel> channels = thing.getChannels();
        for (Channel channel : channels) {
            updateThing(editThing().withoutChannel(channel.getUID()).build());
        }
        logger.warn("All Channels Removed Successfully!");
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        logger.warn("uptade recieved");
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub
        super.channelLinked(channelUID);

    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub
        super.channelUnlinked(channelUID);

    }

    @Override
    public void handleConfigurationUpdate(Map<@NonNull String, @NonNull Object> configurationParameters) {
        // TODO Auto-generated method stub
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public Collection<@NonNull ConfigStatusMessage> getConfigStatus() {
        // TODO Auto-generated method stub
        Collection<ConfigStatusMessage> configStatus = new ArrayList<>();
        return configStatus;
    }

}
