package org.openhab.binding.votecmodule.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(ChannelHandler.class);

    ChannelUID channelUID;
    Thing thing;
    ThingBuilder thingBuilder;

    public ChannelHandler(ChannelUID channelUID, Thing thing, ThingBuilder thingBuilder) {
        this.channelUID = channelUID;
        this.thing = thing;
        this.thingBuilder = thingBuilder;
    }

    public ChannelHandler(Thing thing, ThingBuilder thingBuilder) {
        this.thing = thing;
        this.thingBuilder = thingBuilder;
    }

    public Thing updateChannelLabel(ChannelUID channelUID, String channelName) {
        if (channelUID != null && channelName != null) {
            Channel existingChannel = thing.getChannel(channelUID.getId());
            if (existingChannel != null) {
                String acceptedItem = existingChannel.getAcceptedItemType();
                Configuration configuration = existingChannel.getConfiguration();
                Set<String> defaultTags = existingChannel.getDefaultTags();
                String description = existingChannel.getDescription();
                ChannelKind kind = existingChannel.getKind();
                Map<String, String> properties = existingChannel.getProperties();
                ChannelTypeUID type = existingChannel.getChannelTypeUID();

                ThingBuilder mThingBuilder = thingBuilder;

                Channel channel = ChannelBuilder.create(channelUID, acceptedItem).withConfiguration(configuration)
                        .withDefaultTags(defaultTags).withDescription(description != null ? description : "")
                        .withKind(kind).withLabel(channelName).withProperties(properties).withType(type).build();

                mThingBuilder.withoutChannel(channelUID).withChannel(channel);

                return mThingBuilder.build();
            }
        }
        return null;
    }

    public Thing updateChannelType(ChannelUID toUpdate, ChannelUID fromUpdate) {

        if (toUpdate == null || fromUpdate == null) {
            return null;
        }
        Channel channel = thing.getChannel(fromUpdate);

        if (channel == null) {
            return null;
        }
        String acceptedItem = channel.getAcceptedItemType();
        Configuration configuration = channel.getConfiguration();
        Set<String> defaultTags = channel.getDefaultTags();
        String description = channel.getDescription();
        ChannelKind kind = channel.getKind();
        Map<String, String> properties = channel.getProperties();
        ChannelTypeUID type = channel.getChannelTypeUID();

        ThingBuilder mThingBuilder = thingBuilder;

        ChannelBuilder newChannel = ChannelBuilder.create(toUpdate, acceptedItem).withConfiguration(configuration)
                .withDefaultTags(defaultTags).withDescription(description != null ? description : "").withKind(kind)
                .withProperties(properties).withType(type);
        String channelName = channel.getLabel();
        if (channelName != null) {
            newChannel.withLabel(channelName);
        }

        mThingBuilder.withoutChannel(toUpdate).withChannel(newChannel.build());

        return mThingBuilder.build();

    }

    public Thing removeChannel(ChannelUID toRemove) {
        ThingBuilder mThingBuilder = thingBuilder;
        mThingBuilder.withoutChannel(channelUID);
        return mThingBuilder.build();
    }

    // TODO: Creating new Channels requires a channel instance
    @SuppressWarnings("null")
    public Thing addChannel(String type) {
        // votecmodule:votec_output_10:89e71138:relay1
        List<Channel> channels = thing.getChannels();
        int channelTypeId = 1;
        Channel channel = null;

        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getChannelTypeUID().getAsString().equals(type)) {
                channelTypeId = channelTypeId + 1;
                channel = channels.get(i);
            }

        }
        String[] typeStrings = type.split(":");

        if (typeStrings[1].length() > 1) {
            type = typeStrings[1];
        }

        if (channel != null) {

            String acceptedItemType = channel.getAcceptedItemType();

            ChannelKind kind = channel.getKind();

            ChannelUID newChannelUID = new ChannelUID(thing.getUID() + ":" + type + Integer.toString(channelTypeId));

            Channel newChannel = ChannelBuilder.create(newChannelUID, acceptedItemType).withKind(kind)
                    .withType(channel.getChannelTypeUID()).withLabel(type + " " + Integer.toString(channelTypeId))
                    .build();

            ThingBuilder mThingBuilder = thingBuilder;

            mThingBuilder.withChannel(newChannel);

            logger.warn("Channel Created!");

            return mThingBuilder.build();
        }

        return null;
    }

    /**
     * @return the channelUID
     */
    public ChannelUID getChannelUID() {
        return channelUID;
    }

    /**
     * @param channelUID the channelUID to set
     */
    public void setChannelUID(ChannelUID channelUID) {
        this.channelUID = channelUID;
    }

    /**
     * @return the thing
     */
    public Thing getThing() {
        return thing;
    }

    /**
     * @param thing the thing to set
     */
    public void setThing(Thing thing) {
        this.thing = thing;
    }

}
