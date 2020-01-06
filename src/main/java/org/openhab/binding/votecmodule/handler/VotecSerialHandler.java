package org.openhab.binding.votecmodule.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants;
import org.openhab.binding.votecmodule.internal.protocol.SerialMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VotecSerialHandler extends VotecModuleHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(VotecModuleHandler.class);

    private String portId;

    private SerialPortManager serialPortManager;

    private SerialPort serialPort;

    private SerialPortIdentifier portIdentifier;

    private InputStream inputStream;

    static private OutputStream outputStream;

    private SerialMessage serialMessage;

    public VotecSerialHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing, serialPortManager);
        // TODO Auto-generated constructor stub
        this.serialPortManager = serialPortManager;
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

        try {
            portIdentifier = serialPortManager.getIdentifier(portId);

            if (portIdentifier == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        VotecModuleBindingConstants.OFFLINE_SERIAL_NOTFOUND + portId);
                return;
            }

            serialPort = portIdentifier.open(getThing().getThingTypeUID().getAsString(), 2000);
            serialPort.setSerialPortParams(VotecModuleBindingConstants.BOUD_RATE, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            serialMessage = new SerialMessage();

            updateStatus(ThingStatus.UNKNOWN);
            super.initialize();

        } catch (PortInUseException e) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
        } catch (final IOException e) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error!");
        } catch (TooManyListenersException e) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Cannot attach listener to port!");
        } catch (UnsupportedCommOperationException e) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Unsupported Comm Operation!");
        }

    }

    @Override
    public void dispose() {
        super.dispose();
        if (serialPort != null) {
            serialPort.close();
        }
        IOUtils.closeQuietly(inputStream);
    }

    // TODO: optimize sendPackage method.
    static public void sendPackage(byte[] data) {
        if (data.length < 1) {
            return;
        }
        try {
            outputStream.write(data);
        } catch (IOException e) {

        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                ArrayList<Integer> readBuffer = null;

                try {
                    readBuffer = new ArrayList<>();

                    while (inputStream.available() > 0) {
                        readBuffer.add(inputStream.read());
                    }

                    logger.warn("Input data: " + readBuffer);
                    serialMessage.setMessage(readBuffer);

                } catch (IOException e1) {
                    logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);
                }
                break;
            default:
                break;
        }
    }

}
