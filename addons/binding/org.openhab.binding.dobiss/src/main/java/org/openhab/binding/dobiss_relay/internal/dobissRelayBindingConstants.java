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
package org.openhab.binding.dobiss_relay.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link dobissRelayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class dobissRelayBindingConstants {

    private static final String BINDING_ID = "dobiss_relay";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DOBISS_RELAY = new ThingTypeUID(BINDING_ID, "relay");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_DOBISS_RELAY);

    // List of all Channel ids
    public static final String CHANNEL_DOBISS_RELAY_ID_1 = "channel1";
    public static final String CHANNEL_DOBISS_RELAY_ID_2 = "channel2";
    public static final String CHANNEL_DOBISS_RELAY_ID_3 = "channel3";
    public static final String CHANNEL_DOBISS_RELAY_ID_4 = "channel4";
    public static final String CHANNEL_DOBISS_RELAY_ID_5 = "channel5";
    public static final String CHANNEL_DOBISS_RELAY_ID_6 = "channel6";
    public static final String CHANNEL_DOBISS_RELAY_ID_7 = "channel7";
    public static final String CHANNEL_DOBISS_RELAY_ID_8 = "channel8";
    public static final String CHANNEL_DOBISS_RELAY_ID_9 = "channel9";
    public static final String CHANNEL_DOBISS_RELAY_ID_10 = "channel10";
    public static final String CHANNEL_DOBISS_RELAY_ID_11 = "channel11";
    public static final String CHANNEL_DOBISS_RELAY_ID_12 = "channel12";

    // IP network address of the hcv5
    public static final String THING_PROPERTY_IP = "ipAddress";
    // Simulation mode for the hcv5
    public static final String THING_PROPERTY_ADDRESS = "Address";
    // Polling interval for the hcv5
    public static final String THING_PROPERTY_POLLING_INTERVAL = "pollingInterval";

}
