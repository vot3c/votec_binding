package org.openhab.binding.votecmodule.discovery;

import static org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants.THING_TYPE_SAMPLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
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

    Bridge controller;

    ArrayList<Byte> possibleNodes;

    int avaibleNodeIndex = 0;

    public VotecDiscoveryService(Bridge thing) {
        // TODO Auto-generated constructor stub
        super(SUPPORTED_THING_TYPES_UIDS, VotecModuleBindingConstants.TIMEOUT, true);
        this.controller = thing;

    }

    @Override
    protected void startScan() {
        // TODO get all devices.
        logger.warn("Discovery started");
        scanAvaibleNodes();

        VotecCommand scanCommand = new VotecCommand();
        scanCommand.setBroadcast(2);
        VotecSerialHandler.sendPackage(scanCommand.getPacket());

    }

    public void activate() {
        logger.warn("discovery activated!");
        serialMessage.addListener(this);
    }

    @Override
    public void deactivate() {
        // TODO Auto-generated method stub
        super.deactivate();
        logger.warn("discovery deactivated!");
        serialMessage.removeListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        // TODO Auto-generated method stub
        super.stopScan();
        logger.warn("discovery stopped!");
        possibleNodes = null;
        avaibleNodeIndex = 0;
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public synchronized void abortScan() {
        // TODO Auto-generated method stub
        super.abortScan();
        logger.warn("discovery aborted!");
        possibleNodes = null;
        avaibleNodeIndex = 0;
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

        String serialNumberString = data.subList(0, 4).toString();

        if (hasDiscovered(serialNumberString)) {
            logger.warn("device already discovered: " + serialNumberString);
            return;
        }

        if (possibleNodes == null) {
            logger.warn("Controller can not handle more node !");
            return;
        }

        String nodeIdString = Byte.toString((possibleNodes.get(avaibleNodeIndex)));

        avaibleNodeIndex++;

        Map<String, Object> propertiesMap = new HashMap<String, Object>();

        propertiesMap.put("serial_number", serialNumberString);

        propertiesMap.put("node_id", nodeIdString);

        serialNumberString = serialNumberString.replace("[", "");

        serialNumberString = serialNumberString.replace(", ", "");

        serialNumberString = serialNumberString.replace("]", "");

        String timeStamp = Long.toString((new Date().getTime()));

        String thingType = getThingType(data.get(4));

        ThingUID bridgeUID = controller.getUID();
        ThingUID thingUID = new ThingUID(new ThingTypeUID(thingType + ":" + timeStamp), "node_" + nodeIdString,
                "hello");

        // ThingUID thingUID = new ThingUID(VotecModuleBindingConstants.VOTEC_THING, controller.getUID(),thingType + "_"
        // + timeStamp);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(new ThingTypeUID("votec", thingType)).withProperties(propertiesMap)
                .withLabel("Votec Output Module: " + serialNumberString).withBridge(controller.getUID()).build();

        thingDiscovered(discoveryResult);

    }

    public String getThingType(int id) {
        String thingType = "";

        switch (id) {
            case 20:
                thingType = "votec_output_10";
                break;

            default:
                break;
        }

        return thingType;
    }

    public void scanAvaibleNodes() {

        List<Thing> things = controller.getThings();

        ArrayList<Byte> nodes = new ArrayList<Byte>();

        for (Thing thing : things) {

            byte nodeId = Byte.parseByte(thing.getProperties().get("node_id").toString());
            nodes.add(nodeId);

        }

        logger.warn("unavaible nodes: " + nodes.toString());

        possibleNodes = new ArrayList<Byte>();

        for (byte i = 1; i < 127; i++) {

            if (!nodes.contains(i)) {

                possibleNodes.add(i);
            }
        }

        logger.warn("avaible nodes: " + possibleNodes.toString());

    }

    public boolean hasDiscovered(String serialNumber) {

        List<Thing> things = controller.getThings();

        for (Thing thing : things) {

            if (thing.getProperties().containsKey("serial_number")) {

                String mSerialNumber = thing.getProperties().get("serial_number");

                if (mSerialNumber.equals(serialNumber)) {
                    return true;
                }

            }
        }

        return false;
    }

}
