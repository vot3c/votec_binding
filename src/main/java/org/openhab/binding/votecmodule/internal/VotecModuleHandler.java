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

import static org.openhab.binding.votecmodule.internal.VotecModuleBindingConstants.CHANNEL_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.votecmodule.DataConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VotecModuleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author codigger - Initial contribution
 */

public class VotecModuleHandler extends BaseThingHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(VotecModuleHandler.class);

    private @Nullable VotecModuleConfiguration config;

    private final SerialPortManager serialPortManager;

    private SerialPortIdentifier portId;

    private SerialPort serialPort;
    private InputStream inputStream;

    private OutputStream outputStream;

    public VotecModuleHandler(Thing thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @SuppressWarnings({ "unused" })
    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(VotecModuleConfiguration.class);
        // PORT HERE
        String port1 = "COM2";

        if (port1 == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }

        // parse ports and if the port is found, initialize the reader
        portId = serialPortManager.getIdentifier(port1);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port is not known!");
            return;
        }

        // initialize serial port
        try {
            serialPort = portId.open(getThing().getUID().toString(), 2000);

            serialPort.addEventListener(this);

            // activate the DATA_AVAILABLE notifier
            serialPort.notifyOnDataAvailable(true);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            outputStream.write(DataConvertor.toByteArray("I_GCID"));
            updateStatus(ThingStatus.ONLINE);

        } catch (final IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error!");
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Cannot attach listener to port!");
        }

    }

    @Override
    public void dispose() {
        if (serialPort != null) {
            serialPort.close();
        }

        IOUtils.closeQuietly(inputStream);
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                // we get here if data has been received
                String res = "";
                try {
                    do {
                        // read data from serial device
                        int[] readBuffer = new int[10];
                        int i = 0;
                        while (inputStream.available() > 0) {
                            readBuffer[i] = inputStream.read();
                            i++;
                        }
                        res = Arrays.toString(readBuffer);
                        try {
                            // add wait states around reading the stream, so that interrupted transmissions are merged
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore interruption
                        }

                    } while (inputStream.available() > 0);
                    /*
                     * *
                     *
                     * Serial Input received.
                     */
                    // String channelIdString = getThing().getChannels().get(1).getUID().getId();
                    updateState("channel1", new StringType(res));
                    logger.warn(res);
                    logger.warn(Integer.toHexString(255));
                    outputStream.write(Integer.parseInt("FC", 16));
                    /*
                     * *
                     * Integer.parseInt("FC",16) -> byte (11111100)
                     * Integer.toHexString(255) ->String ("ff")
                     */

                    res = "";
                } catch (IOException e1) {
                    logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);
                }
                break;
            default:
                break;
        }
    }

}
