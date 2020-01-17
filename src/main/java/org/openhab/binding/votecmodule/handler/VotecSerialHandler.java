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

    private String portId;

    private SerialPortManager serialPortManager;

    private SerialPort serialPort;

    private SerialPortIdentifier portIdentifier;

    private InputStream inputStream;

    static private OutputStream outputStream;

    private SerialMessage serialMessage;

    boolean isFailed = false;
    boolean isChecking = true;

    public VotecSerialHandler(Bridge thing, SerialPortManager serialPortManager) {
        super(thing, serialPortManager);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Votec Serial Controller Initializing ..");

        updateStatus(ThingStatus.UNKNOWN);
        super.initialize();

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
        serialMessage = new SerialMessage();

        serialMessage.addListener(this);

        Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {

            isChecking = true;
            isFailed = false;

            CommPortIdentifier comPort = (CommPortIdentifier) portList.nextElement();
            String port = comPort.getName();
            System.out.println(port);

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
                continue;
            }

            try {
                serialPort.setSerialPortParams(VotecModuleBindingConstants.BOUD_RATE, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                serialPort.close();
                IOUtils.closeQuietly(inputStream);
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
            }

            VotecCommand newCommand = new VotecCommand();
            newCommand.setBroadcast(1);
            VotecSerialHandler.sendPackage(newCommand.getPacket());
            long currentTime = System.currentTimeMillis();
            while (isChecking) {
                if ((System.currentTimeMillis() - currentTime) > 1000) {
                    isChecking = false;
                    isFailed = true;
                }
            }
            if (!isFailed) {
                break;
            } else {
                serialPort.close();
                IOUtils.closeQuietly(inputStream);
            }
        }

    }

    @Override
    public void VotecIncomingEvent(ArrayList<Integer> command, ArrayList<Integer> data) {

        isChecking = false;

        if (DataConvertor.arrayToString(command).equals(CommandConstants.CONTROLLER_SET_ID)) {
            logger.warn("Votec Controller Recognized. Device ID: " + data.toString());
            updateStatus(ThingStatus.ONLINE);
            updateState("channel1", new StringType(data.toString()));
            isFailed = false;

        } else {
            isFailed = true;
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
    static public void sendPackage(String data) {
        if (data.length() < 1) {
            return;
        }

        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {

        }
    }

    static public void sendPackage(byte[] data) {

        try {
            if (data != null) {
                outputStream.write(data);
            }
        } catch (IOException e) {

        }
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

                    logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);

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
