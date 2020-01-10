package org.openhab.binding.votecmodule.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;

public class ChannelProvider {
    ThingType thingType;

    public ChannelProvider() {

        this.thingType = ThingTypeBuilder.instance(new ThingTypeUID("someuid"), "label").build();

    }

    public ThingType getThingType() {
        return thingType;
    }
}
