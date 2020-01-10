package org.openhab.binding.votecmodule.discovery;

import static org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants.THING_TYPE_SAMPLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.votecmodule.handler.VotecSerialHandler;
import org.openhab.binding.votecmodule.internal.CommandConstants;
import org.openhab.binding.votecmodule.internal.DataConvertor;
import org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants;
import org.openhab.binding.votecmodule.internal.protocol.SerialMessage;
import org.openhab.binding.votecmodule.internal.protocol.VotecEventListener;
import org.openhab.binding.votecmodule.model.VotecCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component(configurationPid = "discovery.votecmodule", service = DiscoveryService.class, immediate = false)
public class VotecDiscoveryService extends AbstractDiscoveryService implements VotecEventListener {

    private final Logger logger = LoggerFactory.getLogger(VotecDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SAMPLE);

    SerialMessage serialMessage = new SerialMessage();

    Thing controller;

    static int nodeId = 1;

    public VotecDiscoveryService(Thing thing) {
        // TODO Auto-generated constructor stub
        super(SUPPORTED_THING_TYPES_UIDS, VotecModuleBindingConstants.TIMEOUT, true);
        this.controller = thing;
    }

    @Override
    protected void startScan() {
        // TODO get all devices.
        removeOlderResults(getTimestampOfLastScan());
        logger.warn("Discovery started");
        VotecCommand scanCommand = new VotecCommand();
        scanCommand.setBroadcast(2);
        VotecSerialHandler.sendPackage(scanCommand.getPacket());
        serialMessage.addListener(this);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    public void addNode(String node) {

    }

    @Override
    protected void deactivate() {
        // TODO Auto-generated method stub
        super.deactivate();
        serialMessage.removeListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        // TODO Auto-generated method stub
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public synchronized void abortScan() {
        // TODO Auto-generated method stub
        super.abortScan();
        serialMessage.removeListener(this);
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void VotecIncomingEvent(ArrayList<Integer> command, ArrayList<Integer> data) {
        // TODO Auto-generated method stub
        if (DataConvertor.arrayToString(command).equals(CommandConstants.SCAN_NETWORK_RESULT)) {
            addDevice(data);
        }
    }

    public void addDevice(ArrayList<Integer> data) {

        ThingUID thingUID = new ThingUID(VotecModuleBindingConstants.VOTEC_THING, controller.getUID(),
                "node" + Integer.toString(nodeId));

        Map<String, Object> propertiesMap = new HashMap<String, Object>();
        propertiesMap.put("node_id", Integer.toString(nodeId));
        propertiesMap.put("conf_node_id", nodeId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(new ThingTypeUID("votecmodule", "votec_output_10")).withProperties(propertiesMap)
                .withLabel("Votec Output Module").withBridge(controller.getBridgeUID()).build();

        thingDiscovered(discoveryResult);

    }

}