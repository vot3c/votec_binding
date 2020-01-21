package org.openhab.binding.votecmodule.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
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
import org.openhab.binding.votecmodule.model.commands.OutputModule;
import org.openhab.binding.votecmodule.model.commands.TestMode;
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
            // refreshFromState(channelUID, command);
            return;
        }
        switch (thing.getThingTypeUID().getAsString()) {
            case "votec:votec_output_10":
                outputChannelCommand(channelUID, command);
                break;
            case "votec:votec_input_35":
                inputChannelCommand(channelUID, command);
                break;

            case "votec:votec_input_20":
                inputChannelCommand(channelUID, command);
                break;
            default:
                break;
        }

    }

    public void inputChannelCommand(ChannelUID channelUID, Command command) {

        logger.warn("Command Recieved: " + command.toString());

    }

    public void outputChannelCommand(ChannelUID channelUID, Command command) {
        String string = thing.getProperties().get("serial_number");

        byte[] packet = DataConvertor.stringIntArrayToByteArray(string);

        VotecCommand votecCommand = new VotecCommand();

        if ((boolean) thing.getConfiguration().get("testMode")) {
            votecCommand = new TestMode();
            votecCommand.setSerialnumber(packet);

        } else {
            int nodeId = Integer.parseInt(thing.getProperties().get("node_id"));
            votecCommand = new OutputModule();
            votecCommand.setDeviceId(nodeId);

        }

        String[] channelAtr = channelUID.getId().toString().split("_");

        int atomicId = Integer.parseInt(channelAtr[1]);

        if (channelAtr[0].equals("relay")) {
            votecCommand.setAtomicId(atomicId);
            switch (command.toString()) {
                case "ON":
                    updateState(channelUID, OnOffType.ON);
                    votecCommand.setData5(1);

                    break;

                case "OFF":
                    updateState(channelUID, OnOffType.OFF);
                    votecCommand.setData5(0);
                    break;

                default:
                    break;
            }

        } else if (channelAtr[0].equals("blind")) {
            atomicId = (6 - atomicId) * 2;
            votecCommand.setAtomicId(atomicId);
            switch (command.toString()) {
                case "UP":
                    votecCommand.setData5(2);
                    updateState(channelUID, UpDownType.UP);
                    break;

                case "DOWN":
                    votecCommand.setData5(3);
                    updateState(channelUID, UpDownType.DOWN);

                    break;

                case "STOP":
                    votecCommand.setData5(4);
                    updateState(channelUID, new PercentType(50));
                    break;
                default:
                    break;
            }
        }

        VotecSerialHandler.sendPackage(votecCommand.getPacket());

    }

    private void refreshFromState(@NonNull ChannelUID channelUID, Command command) {
        // TODO: set all status of channels.

    }

    @Override
    public void initialize() {
        logger.warn("{} initialized!", thing.getLabel());

        Runnable configureRunnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                configureChannels();

            }
        };

        Thread configureThread = new Thread(configureRunnable);

        configureThread.start();

        updateStatus(ThingStatus.ONLINE);

    }

    public void configureChannels() {
        Thing mThing = null;

        ChannelHandler handler = new ChannelHandler(thing, editThing());
        switch (thing.getThingTypeUID().getAsString()) {
            case "votec:votec_output_10":
                removeAllChannels();
                configureOutputChannels();
                return;

            case "votec:votec_input_20":

                return;

            case "votec:votec_input_35":

                return;

            default:
                return;
        }
    }

    public void configureOutputChannels() {
        // clear all channels.
        Thing mThing = null;
        removeAllChannels();

        ChannelHandler handler = new ChannelHandler(thing, editThing());

        BigDecimal a = (BigDecimal) thing.getConfiguration().get("blindNumber");

        int blindNumber = a.intValue();
        int relayNumber = 10 - blindNumber * 2;

        while (relayNumber > 0) {
            mThing = handler.addChannel("votec:relay");
            if (mThing != null) {
                updateThing(mThing);
                handler = new ChannelHandler(thing, editThing());
                relayNumber = relayNumber - 1;
            }

        }

        while (blindNumber > 0) {
            mThing = handler.addChannel("votec:blind");
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

    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {

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
