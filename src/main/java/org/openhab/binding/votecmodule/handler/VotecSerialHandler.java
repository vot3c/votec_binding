package org.openhab.binding.votecmodule.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.votecmodule.internal.CommandConstants;
import org.openhab.binding.votecmodule.internal.DataConvertor;
import org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants;
import org.openhab.binding.votecmodule.internal.protocol.SerialMessage;
import org.openhab.binding.votecmodule.model.VotecCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;

public class VotecSerialHandler extends VotecControllerHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(VotecSerialHandler.class);

    private SerialPortManager serialPortManager;

    private SerialPort serialPort;

    private SerialPortIdentifier portIdentifier;

    private InputStream inputStream;

    static private OutputStream outputStream;

    private SerialMessage serialMessage;

    public VotecSerialHandler(Bridge thing, SerialPortManager serialPortManager) {
        super(thing, serialPortManager);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Votec Serial Controller Initializing ..");

        serialMessage = new SerialMessage();

        super.initialize();

        if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
            logger.warn("Controller not found looking for it!");
            updateStatus(ThingStatus.UNKNOWN);
            configurePort();
        }

    }

    private void configurePort() {
        Runnable configureRunnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                initializePort();

            }
        };

        Thread configureThread = new Thread(configureRunnable);

        configureThread.start();

    }

    public void initializePort() {

        serialMessage.addListener(this);

        Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {

            CommPortIdentifier comPort = (CommPortIdentifier) portList.nextElement();
            String port = comPort.getName();

            portIdentifier = serialPortManager.getIdentifier(port);

            if (portIdentifier == null) {
                logger.warn("port identifier null!");
                continue;
            }

            try {
                serialPort = portIdentifier.open(getThing().getThingTypeUID().getAsString(), 2000);
            } catch (PortInUseException e) {
                // TODO Auto-generated catch block
                logger.warn("port in use!");
                serialPort = null;
                continue;
            }

            try {
                serialPort.setSerialPortParams(VotecModuleBindingConstants.BOUD_RATE, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                serialPort.close();
                IOUtils.closeQuietly(inputStream);
                serialPort = null;
                logger.warn("unsupported comm operation");
                continue;
            }

            try {
                serialPort.addEventListener(this);
            } catch (TooManyListenersException e) {
                serialPort.close();
                IOUtils.closeQuietly(inputStream);
                logger.warn("Too Many Listener!");
                continue;
            }

            serialPort.notifyOnDataAvailable(true);

            try {
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error!");
                logger.warn("Serial Port I/O error!");
            }

            VotecCommand newCommand = new VotecCommand();
            newCommand.setBroadcast(1);
            VotecSerialHandler.sendPackage(newCommand.getPacket());
            long currentMilis = System.currentTimeMillis();

            while (System.currentTimeMillis() - currentMilis < 1000) {
            }

            if (thing.getStatus().equals(ThingStatus.ONLINE)) {
                logger.warn("On {} Votec Controller Discovered!", port);
                break;
            } else {
                serialPort.close();
                IOUtils.closeQuietly(inputStream);

            }

        }
        serialMessage.removeListener(this);
    }

    @Override
    public void VotecIncomingEvent(ArrayList<Integer> command, ArrayList<Integer> data) {

        if (DataConvertor.arrayToString(command).equals(CommandConstants.CONTROLLER_SET_ID)) {
            logger.warn("Votec Controller Recognized. Device ID: " + data.toString());
            updateStatus(ThingStatus.ONLINE);
            updateState("channel1", new StringType(data.toString()));

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
    static public boolean sendPackage(String data) {
        if (data.length() < 1) {
            return false;
        }

        if (outputStream == null) {
            return false;
        }

        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean sendPackage(byte[] data) {
        if (outputStream == null) {
            return false;
        }

        try {
            if (data != null) {
                outputStream.write(data);
            }
        } catch (IOException e) {
            return false;
        }
        return true;

    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                ArrayList<Integer> readBuffer = null;

                try {
                    readBuffer = new ArrayList<Integer>();

                    while (inputStream.available() > 0) {
                        readBuffer.add(inputStream.read());
                    }
                    logger.warn("Input data: " + readBuffer);

                    splitMessage(readBuffer);

                } catch (IOException e1) {

                    logger.warn("Error reading from serial port: {}", e1.getMessage(), e1);

                }

                break;
            default:
                break;
        }
    }

    public void splitMessage(ArrayList<Integer> message) {
        ArrayList<Integer> tempArrayList = new ArrayList<Integer>();
        for (int i : message) {
            tempArrayList.add(i);
            if (i == 35) {
                serialMessage.setMessage(tempArrayList);
                tempArrayList = new ArrayList<Integer>();
            }
        }

    }

}
