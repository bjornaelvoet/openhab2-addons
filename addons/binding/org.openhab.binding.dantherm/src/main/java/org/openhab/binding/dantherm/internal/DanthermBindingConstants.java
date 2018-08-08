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
package org.openhab.binding.dantherm.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DanthermBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class DanthermBindingConstants {

    private static final String BINDING_ID = "dantherm";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HCV5 = new ThingTypeUID(BINDING_ID, "hcv5");

    // public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_HCV5,
    // THING_TYPE_HCV5);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_HCV5);

    // List of all Channel ids
    public static final String CHANNEL_HCV5_FANSPEED = "fan-speed";

    public static final String CHANNEL_HCV5_CURRENT_UNITMODE = "current-unitmode";
    public static final String CHANNEL_HCV5_ACTIVE_UNITMODE = "active-unitmode";

    public static final String CHANNEL_HCV5_TEMPERATURE1 = "temperature1";
    public static final String CHANNEL_HCV5_TEMPERATURE2 = "temperature2";
    public static final String CHANNEL_HCV5_TEMPERATURE3 = "temperature3";
    public static final String CHANNEL_HCV5_TEMPERATURE4 = "temperature4";

    // IP network address of the hcv5
    public static final String THING_PROPERTY_IP = "ipAddress";
    // Simulation mode for the hcv5
    public static final String THING_PROPERTY_SIMULATION_MODE = "SimulationMode";
    // Polling interval for the hcv5
    public static final String THING_PROPERTY_POLLING_INTERVAL = "pollingInterval";

}
