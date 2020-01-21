/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.votecmodule.internal;

import static org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants.THING_TYPE_SAMPLE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.votecmodule.discovery.VotecDiscoveryService;
import org.openhab.binding.votecmodule.handler.ModulesHandler;
import org.openhab.binding.votecmodule.handler.VotecSerialHandler;
import org.openhab.binding.votecmodule.internal.protocol.OnDiscoveryStarted;
import org.openhab.binding.votecmodule.model.commands.SetNodeId;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VotecModuleHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author codigger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.votecmodule", service = ThingHandlerFactory.class)
public class VotecModuleHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(VotecModuleHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SAMPLE);
    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.getBindingId().equals("votec");
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        VotecSerialHandler controller = null;
        if (thingTypeUID.equals(VotecModuleBindingConstants.THING_TYPE_SAMPLE)) {
            controller = new VotecSerialHandler((Bridge) thing, serialPortManager);
        }

        if (controller != null) {
            VotecDiscoveryService discoveryService = new VotecDiscoveryService((Bridge) thing);
            discoveryService.activate();

            VotecDiscoveryService.addListener(new OnDiscoveryStarted() {

                @Override
                public void started() {
                    clearInbox();

                }
            });

            discoveryServiceRegs.put(controller.getThing().getUID(), bundleContext.registerService(
                    DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));

            return controller;

        }

        registerModuleNodeId(thing);

        return new ModulesHandler(thing);

    }

    public void clearInbox() {
        ServiceReference<Inbox> cpr = bundleContext.getServiceReference(Inbox.class);
        Inbox in = bundleContext.getService(cpr);
        List<DiscoveryResult> list = in.getAll();

        for (DiscoveryResult discoveryResult : list) {
            ThingUID thingUID = discoveryResult.getThingUID();
            in.remove(thingUID);

        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        super.removeHandler(thingHandler);
        if (thingHandler instanceof VotecSerialHandler) {
            ServiceRegistration<?> serviceRegs = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceRegs != null) {
                VotecDiscoveryService service = (VotecDiscoveryService) bundleContext
                        .getService(serviceRegs.getReference());
                if (service != null) {
                    service.deactivate();
                }

                serviceRegs.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }

    }

    /**
     * Before Thing created set device id of things.
     *
     * @param thing The Thing to set its own properties to module.
     */
    public void registerModuleNodeId(Thing thing) {

        if (thing.getProperties().containsKey("serial_number")) {

            String serialNumber = thing.getProperties().get("serial_number");

            if (thing.getProperties().containsKey("node_id")) {

                int deviceId = Integer.parseInt(thing.getProperties().get("node_id").toString());

                SetNodeId setNodeId = new SetNodeId();

                setNodeId.setSerialnumber(DataConvertor.stringIntArrayToByteArray(serialNumber));

                setNodeId.setDeviceId(deviceId);

                VotecSerialHandler.sendPackage(setNodeId.getPacket());

            }

        }

    }

}
