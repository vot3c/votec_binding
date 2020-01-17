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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VotecModuleBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author codigger - Initial contribution
 */
@NonNullByDefault
public class VotecModuleBindingConstants {

    public static final String BINDING_ID = "votec";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "can");

    public final static String VOTEC_THING = BINDING_ID + ":device";

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    public static final String TRIGGER_CHANNEL = "button";

    public static final String CONFIGURATION_PORT = "port";

    // Serial Configs
    public static final int BOUD_RATE = 115200;

    public final static String OFFLINE_SERIAL_NOTFOUND = "Port is not known: ";

    public final static int TIMEOUT = 3;

    public static ArrayList<Integer> ADDED_NODES = new ArrayList<Integer>();

}
