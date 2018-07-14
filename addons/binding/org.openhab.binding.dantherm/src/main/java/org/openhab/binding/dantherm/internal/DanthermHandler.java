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

import static org.openhab.binding.dantherm.internal.DanthermBindingConstants.*;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DanthermHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class DanthermHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DanthermHandler.class);

    private static final StringType OFF = new StringType("OFF");
    private static final StringType POWER1 = new StringType("POWER1");
    private static final StringType POWER2 = new StringType("POWER2");
    private static final StringType POWER3 = new StringType("POWER3");
    private static final StringType POWER4 = new StringType("POWER4");

    @Nullable
    private String ipAddress = null;

    private boolean simulationMode = false;

    @Nullable
    private DanthermConfiguration config;

    // private DanthermListener DanthermListener;

    public DanthermHandler(Thing thing) {
        super(thing);

        this.thing = thing;

        logger.info("Creating DanthermListener object for {}", thing.getUID());

        // DanthermListener = new DanthermListener(ipv4Address);
    }

    @Override
    public void initialize() {
        logger.info("DanthermHandler for {} is initializing", thing.getUID());

        config = getConfigAs(DanthermConfiguration.class);

        if (StringUtils.isBlank(config.ipAddress)) {
            logger.debug("DanthermHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dantherm config. Check configuration.");
            return;
        }

        ipAddress = config.ipAddress;

        simulationMode = config.simulationMode;

        if (config.simulationMode) {
            // No communication needed; just fake a hcv5 unit is present.

            updateStatus(ThingStatus.ONLINE);
        } else {
            // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
            // Long running initialization should be done asynchronously in background.

            // TODO Write modbus communication to check if we have hcv5 unit on the give ip address

            logger.debug("Communication to HCV5 unit not implemented!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Communication to HCV5 unit not implemented.");
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        logger.info("DanthermHandler for {} is disposing", thing.getUID());
        // danthermListener.stopDanthermListener();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.info("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

        if (channelUID.getId().equals(CHANNEL_HCV5_FANPOWER)) {
            handleHCV5FanPower(command);
        } else if (channelUID.getId().equals(CHANNEL_HCV5_TEMPERATURE1)) {
            handleHCV5Temp1(command);
        } else if (channelUID.getId().equals(CHANNEL_HCV5_TEMPERATURE2)) {
            handleHCV5Temp2(command);
        } else if (channelUID.getId().equals(CHANNEL_HCV5_TEMPERATURE3)) {
            handleHCV5Temp3(command);
        } else if (channelUID.getId().equals(CHANNEL_HCV5_TEMPERATURE4)) {
            handleHCV5Temp4(command);
        } else {
            logger.debug("Received command for {} on unknown channel {}", thing.getUID(), channelUID.getId());
        }
    }

    private void handleHCV5FanPower(Command command) {
        logger.info("Handling HCV5 fan power command for {}: {}", thing.getUID(), command);

        if (simulationMode) {
            // Just fake a temperature
            // updateState(CHANNEL_HCV5_TEMP1, new DecimalType(19.2));

        } else {

        }

        // <mac;FAN;PWR;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                // sendCommand(macAddress, ";FAN;PWR;OFF");
            } else if (command.equals(OnOffType.ON)) {
                // sendCommand(macAddress, ";FAN;PWR;ON");
            }
        }
    }

    private void handleHCV5Temp1(Command command) {
        logger.info("Handling command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a temperature
                updateState(CHANNEL_HCV5_TEMPERATURE1, DecimalType.valueOf("19.2"));
            } else {
                // TODO Query HCV5 device over MODBUS for temperature
            }
        } else {
            logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
        }

        // <mac;FAN;SPD;SET;0..7>
        // if (command instanceof PercentType) {
        // sendCommand(macAddress, ";FAN;SPD;SET;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
        // }
    }

    private void handleHCV5Temp2(Command command) {
        logger.info("Handling HCV5 temp2 command for {}: {}", thing.getUID(), command);

        // <mac;FAN;SPD;SET;0..7>
        // if (command instanceof PercentType) {
        // sendCommand(macAddress, ";FAN;SPD;SET;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
        // }
    }

    private void handleHCV5Temp3(Command command) {
        logger.info("Handling HCV5 temp3 command for {}: {}", thing.getUID(), command);

        // <mac;FAN;SPD;SET;0..7>
        // if (command instanceof PercentType) {
        // sendCommand(macAddress, ";FAN;SPD;SET;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
        // }
    }

    private void handleHCV5Temp4(Command command) {
        logger.info("Handling HCV5 temp4 command for {}: {}", thing.getUID(), command);

        // <mac;FAN;SPD;SET;0..7>
        // if (command instanceof PercentType) {
        // sendCommand(macAddress, ";FAN;SPD;SET;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
        // }
    }

}
