package org.openhab.binding.votecmodule.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VotecSerialHandler extends VotecModuleHandler {

    private final Logger logger = LoggerFactory.getLogger(VotecModuleHandler.class);

    private String portId;

    private SerialPortManager serialPortManager;

    private SerialPort serialPort;

    private SerialPortIdentifier portIdentifier;

    public VotecSerialHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing, serialPortManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initialize() {
        logger.debug("Votec Serial Controller Initializing ..");

        portId = (String) getConfig().get("CONFIGURATION_PORT");

        if (portId == null || portId.length() == 0) {
            logger.debug("Votec Serial Controller is not set");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
            return;
        }

        // TODO:Calling reinitialize method maybe wrong?
        super.initialize();

        portIdentifier = serialPortManager.getIdentifier(portId);

        if (portIdentifier == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    VotecModuleBindingConstants.OFFLINE_SERIAL_NOTFOUND + portId);
        }

    }

}
