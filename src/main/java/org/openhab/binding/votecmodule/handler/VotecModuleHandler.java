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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
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
        // TODO Auto-generated method stub

    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
    }

}
