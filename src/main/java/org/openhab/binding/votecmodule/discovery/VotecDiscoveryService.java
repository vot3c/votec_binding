package org.openhab.binding.votecmodule.discovery;

import static org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants.THING_TYPE_SAMPLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.votecmodule.handler.VotecSerialHandler;
import org.openhab.binding.votecmodule.internal.CommandConstants;
import org.openhab.binding.votecmodule.internal.DataConvertor;
import org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants;
import org.openhab.binding.votecmodule.internal.protocol.OnDiscoveryStarted;
import org.openhab.binding.votecmodule.internal.protocol.SerialMessage;
import org.openhab.binding.votecmodule.internal.protocol.VotecEventListener;
import org.openhab.binding.votecmodule.model.VotecCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component(configurationPid = "discovery.votecmodule", service = DiscoveryService.class, immediate = false)
public class VotecDiscoveryService extends AbstractDiscoveryService implements VotecEventListener {

    private final Logger logger = LoggerFactory.getLogger(VotecDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SAMPLE);

    private static ArrayList<OnDiscoveryStarted> listeners = new ArrayList<OnDiscoveryStarted>();

    SerialMessage serialMessage = new SerialMessage();

    Bridge controller;

    ArrayList<Byte> possibleNodes;

    static boolean fromScan = false;

    public VotecDiscoveryService(Bridge thing) {
        // TODO Auto-generated constructor stub
        super(SUPPORTED_THING_TYPES_UIDS, VotecModuleBindingConstants.TIMEOUT, true);
        this.controller = thing;

    }

    @Override
    protected void startScan() {
        // TODO get all devices.
        fromScan = true;
        logger.warn("Discovery started");
        for (OnDiscoveryStarted onDiscoveryStarted : listeners) {
            onDiscoveryStarted.started();
        }

        scanAvaibleNodes();

        VotecCommand scanCommand = new VotecCommand();
        scanCommand.setBroadcast(2);
        VotecSerialHandler.sendPackage(scanCommand.getPacket());

    }

    public static void addListener(OnDiscoveryStarted mListener) {
        synchronized (listeners) {
            if (listeners.contains(mListener)) {
                return;
            }
            listeners.add(mListener);
        }
    }

    public static void removeListener(OnDiscoveryStarted mListener) {
        synchronized (listeners) {
            listeners.remove(mListener);
        }

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
        fromScan = false;
        serialMessage.removeListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        // TODO Auto-generated method stub
        super.stopScan();
        if (fromScan) {
            healNotFoundDevices();
        }
        fromScan = false;
        possibleNodes = null;
        logger.warn("stop scan");
    }

    @Override
    public synchronized void abortScan() {
        // TODO Auto-generated method stub
        super.abortScan();
        logger.warn("abort");
        fromScan = false;
        possibleNodes = null;
    }

    @Override
    public void VotecIncomingEvent(ArrayList<Integer> command, ArrayList<Integer> data) {
        // TODO Auto-generated method stub
        if (DataConvertor.arrayToString(command).equals(CommandConstants.SCAN_NETWORK_RESULT)) {
            scanAvaibleNodes();

            if (fromScan) {
                addDevice(data);
                logger.warn("from scan");
            } else {
                startScan();

                Runnable stopScanRunnable = new Runnable() {

                    @Override
                    public void run() {
                        stopScan();

                    }
                };

                scheduler.schedule(stopScanRunnable, VotecModuleBindingConstants.TIMEOUT, TimeUnit.SECONDS);

                logger.warn("from device");

            }

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

        String nodeIdString = Byte.toString((possibleNodes.get(0)));

        Map<String, Object> propertiesMap = new HashMap<String, Object>();

        propertiesMap.put("serial_number", serialNumberString);

        propertiesMap.put("node_id", nodeIdString);

        serialNumberString = serialNumberString.replace("[", "");

        serialNumberString = serialNumberString.replace(", ", "");

        serialNumberString = serialNumberString.replace("]", "");

        String thingType = getThingType(data.get(4));

        ThingUID thingUID = new ThingUID(new ThingTypeUID(controller.getUID() + ":"), "node_" + nodeIdString,
                thingType);

        // ThingUID thingUID = new ThingUID(VotecModuleBindingConstants.VOTEC_THING, controller.getUID(),thingType + "_"
        // + timeStamp);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(new ThingTypeUID("votec", thingType)).withProperties(propertiesMap)
                .withLabel("Votec Output Module: " + "node " + nodeIdString).withBridge(controller.getUID()).build();

        thingDiscovered(discoveryResult);

    }

    public String getThingType(int id) {
        String thingType = "";

        switch (id) {
            case 20:
                thingType = "votec_output_10";
                break;
            case 10:
                thingType = "votec_input_35";
                break;
            case 11:
                thingType = "votec_input_20";
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

        possibleNodes = new ArrayList<Byte>();

        for (byte i = 1; i < 127; i++) {

            if (!nodes.contains(i)) {

                possibleNodes.add(i);
            }
        }

    }

    static ArrayList<String> isOnline = new ArrayList<String>();

    public boolean hasDiscovered(String serialNumber) {

        List<Thing> things = controller.getThings();

        for (Thing thing : things) {

            if (thing.getProperties().containsKey("serial_number")) {

                String mSerialNumber = thing.getProperties().get("serial_number");

                if (mSerialNumber.equals(serialNumber)) {
                    isOnline.add(mSerialNumber);
                    logger.warn("already discovered: " + isOnline.toString());
                    return true;

                }

            }
        }

        return false;
    }

    public void healNotFoundDevices() {
        List<Thing> things = controller.getThings();
        for (Thing thing : things) {
            if (thing.getProperties().containsKey("serial_number")) {
                String mSerialNumber = thing.getProperties().get("serial_number");
                if (!isOnline.contains(mSerialNumber)) {
                    // TODO: ask device that is it still avaliable
                    // if not set status to OFFLINE
                    thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "device lost!"));
                } else {
                    logger.warn("setting status to online!");
                    thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
                }
            }
        }
        isOnline = new ArrayList<String>();

    }

}
