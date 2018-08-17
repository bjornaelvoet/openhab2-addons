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
package org.openhab.binding.dobiss_dimmer.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link dobissDimmerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class dobissDimmerBindingConstants {

    private static final String BINDING_ID = "dobiss_dimmer";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DOBISS_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_DOBISS_DIMMER);

    // List of all Channel ids
    public static final String CHANNEL_DOBISS_DIMMER_ID_1 = "channel1";
    public static final String CHANNEL_DOBISS_DIMMER_ID_2 = "channel2";
    public static final String CHANNEL_DOBISS_DIMMER_ID_3 = "channel3";
    public static final String CHANNEL_DOBISS_DIMMER_ID_4 = "channel4";

    // IP network address of the hcv5
    public static final String THING_PROPERTY_IP = "ipAddress";
    // Simulation mode for the hcv5
    public static final String THING_PROPERTY_ADDRESS = "Address";
    // Polling interval for the hcv5
    public static final String THING_PROPERTY_POLLING_INTERVAL = "pollingInterval";
}
