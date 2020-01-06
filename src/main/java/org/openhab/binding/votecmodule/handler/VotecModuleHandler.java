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
package org.openhab.binding.votecmodule.handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.votecmodule.internal.CommandConstants;
import org.openhab.binding.votecmodule.internal.DataConvertor;
import org.openhab.binding.votecmodule.internal.protocol.SerialMessage;
import org.openhab.binding.votecmodule.internal.protocol.VotecEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VotecModuleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author codigger - Initial contribution
 */

public class VotecModuleHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VotecModuleHandler.class);

    private final SerialPortManager serialPortManager;

    private SerialPortIdentifier portId;

    private SerialPort serialPort;

    private InputStream inputStream;

    private OutputStream outputStream;

    SerialMessage serialMessage;

    boolean flag = true;

    public VotecModuleHandler(Thing thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        serialMessage = new SerialMessage();
        VotecSerialHandler.sendPackage(CommandConstants.CONTROLLER_GET_ID.getBytes());

        VotecEventListener controllerListener = new VotecEventListener() {

            @Override
            public void VotecIncomingEvent(ArrayList<Integer> command, ArrayList<Integer> data) {
                if (command.isEmpty()) {
                    logger.warn("Command is empty");
                }
                if (DataConvertor.arrayToString(command).equals(CommandConstants.CONTROLLER_SET_ID)) {
                    logger.warn("Command has found");
                    updateStatus(ThingStatus.ONLINE);
                }

            }
        };

        serialMessage.addListener(controllerListener);

    }

}
