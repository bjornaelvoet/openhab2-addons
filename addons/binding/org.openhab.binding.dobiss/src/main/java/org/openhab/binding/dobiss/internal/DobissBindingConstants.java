/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.dobiss.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DobissBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class DobissBindingConstants {

    private static final String BINDING_ID = "dobiss";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DOMOTICS = new ThingTypeUID(BINDING_ID, "domotics");

    // List of all Channel ids
    public static final String CHANNEL_RELAY01_ID_1 = "relay01_channel1";
    public static final String CHANNEL_RELAY01_ID_2 = "relay01_channel2";
    public static final String CHANNEL_RELAY01_ID_3 = "relay01_channel3";
    public static final String CHANNEL_RELAY01_ID_4 = "relay01_channel4";
    public static final String CHANNEL_RELAY01_ID_5 = "relay01_channel5";
    public static final String CHANNEL_RELAY01_ID_6 = "relay01_channel6";
    public static final String CHANNEL_RELAY01_ID_7 = "relay01_channel7";
    public static final String CHANNEL_RELAY01_ID_8 = "relay01_channel8";
    public static final String CHANNEL_RELAY01_ID_9 = "relay01_channel9";
    public static final String CHANNEL_RELAY01_ID_10 = "relay01_channel10";
    public static final String CHANNEL_RELAY01_ID_11 = "relay01_hannel11";
    public static final String CHANNEL_RELAY01_ID_12 = "relay01_channel12";

    public static final String CHANNEL_RELAY02_ID_1 = "relay02_channel1";
    public static final String CHANNEL_RELAY02_ID_2 = "relay02_channel2";
    public static final String CHANNEL_RELAY02_ID_3 = "relay02_channel3";
    public static final String CHANNEL_RELAY02_ID_4 = "relay02_channel4";
    public static final String CHANNEL_RELAY02_ID_5 = "relay02_channel5";
    public static final String CHANNEL_RELAY02_ID_6 = "relay02_channel6";
    public static final String CHANNEL_RELAY02_ID_7 = "relay02_channel7";
    public static final String CHANNEL_RELAY02_ID_8 = "relay02_channel8";
    public static final String CHANNEL_RELAY02_ID_9 = "relay02_channel9";
    public static final String CHANNEL_RELAY02_ID_10 = "relay02_channel10";
    public static final String CHANNEL_RELAY02_ID_11 = "relay02_hannel11";
    public static final String CHANNEL_RELAY02_ID_12 = "relay02_channel12";

    public static final String CHANNEL_DIMMER01_ID_1 = "dimmer01_channel1";
    public static final String CHANNEL_DIMMER01_ID_2 = "dimmer01_channel2";
    public static final String CHANNEL_DIMMER01_ID_3 = "dimmer01_channel3";
    public static final String CHANNEL_DIMMER01_ID_4 = "dimmer01_channel4";

    public static final String CHANNEL_DIMMER02_ID_1 = "dimmer02_channel1";
    public static final String CHANNEL_DIMMER02_ID_2 = "dimmer02_channel2";
    public static final String CHANNEL_DIMMER02_ID_3 = "dimmer02_channel3";
    public static final String CHANNEL_DIMMER02_ID_4 = "dimmer02_channel4";

    // IP network address of the dobiss module
    public static final String THING_PROPERTY_IP = "ipAddress";

    public static final String THING_PROPERTY_RELAY01_ADDRESS = "addressRelay01";
    public static final String THING_PROPERTY_RELAY02_ADDRESS = "addressRelay02";
    public static final String THING_PROPERTY_DIMMER01_ADDRESS = "addressDimmer01";
    public static final String THING_PROPERTY_DIMMER02_ADDRESS = "addressDimmer02";

    // Polling interval for the dobiss module
    public static final String THING_PROPERTY_POLLING_INTERVAL = "pollingInterval";
}
