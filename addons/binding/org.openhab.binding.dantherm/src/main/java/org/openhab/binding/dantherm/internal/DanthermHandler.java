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

import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
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

    // Timer for pushing the updates of the HCV5 to the UI
    @Nullable
    Timer timer;

    @Nullable
    private String ipAddress = null;
    private int pollingInterval;
    private int pollingDelay = 1000;

    private boolean simulationMode = false;

    @Nullable
    private DanthermConfiguration config = null;

    @Nullable
    private DanthermModbus danthermModbus = null;

    public DanthermHandler(Thing thing) {
        super(thing);

        this.thing = thing;
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("DanthermHandler for {} is initializing", thing.getUID());

        config = getConfigAs(DanthermConfiguration.class);

        // Check ip address
        if (StringUtils.isBlank(config.ipAddress)) {
            logger.debug("DanthermHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dantherm config. IP address is blank.");
            return;
        }

        ipAddress = config.ipAddress;

        // Check polling interval
        if (config.pollingInterval < 1) {
            logger.debug("DanthermHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dantherm config. Polling interval is smaller than 1.");
            return;
        }
        pollingInterval = config.pollingInterval;

        simulationMode = config.simulationMode;

        if (config.simulationMode) {
            // No communication needed; just fake a hcv5 unit is present.

            updateStatus(ThingStatus.ONLINE);
        } else {
            // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
            // Long running initialization should be done asynchronously in background.
            try {
                danthermModbus = new DanthermModbus(InetAddress.getByName(ipAddress), pollingInterval);
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                e.printStackTrace();

                logger.debug("Communication to HCV5 unit not implemented!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Unable to communicate with HCV5 unit");
            }
        }

        // Starting UI pushing
        startAutomaticRefresh();
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {

        timer.cancel();
        timer.purge();

        logger.debug("DanthermHandler for {} is disposing", thing.getUID());

        super.dispose();
    }

    private void startAutomaticRefresh() {
        logger.debug("Starting automatic refresh");

        timer = new Timer();
        timer.scheduleAtFixedRate(new PeriodicTask(), pollingDelay, pollingInterval * 1000);

        logger.debug("Start automatic refresh every {} seconds", pollingInterval);
    }

    private void publishValue(ChannelUID channelUID) {
        logger.debug("Publishing value...");

        String channelID = channelUID.getId();
        switch (channelID) {
            case CHANNEL_HCV5_TEMPERATURE1:
                publishHCV5Temperature1(channelUID);
                break;
            case CHANNEL_HCV5_TEMPERATURE2:
                publishHCV5Temperature2(channelUID);
                break;
            case CHANNEL_HCV5_TEMPERATURE3:
                publishHCV5Temperature3(channelUID);
                break;
            case CHANNEL_HCV5_TEMPERATURE4:
                publishHCV5Temperature4(channelUID);
                break;
            case CHANNEL_HCV5_FANSPEED:
                publishHCV5FanSpeed(channelUID);
            case CHANNEL_HCV5_CURRENT_UNITMODE:
                publishHCV5CurrentUnitMode(channelUID);
            case CHANNEL_HCV5_ACTIVE_UNITMODE:
                publishHCV5ActiveUnitMode(channelUID);
            default:
                logger.debug("Can not update channel with ID : {} - channel name might be wrong!", channelID);
                break;
        }
    }

    @SuppressWarnings("null")
    private void publishHCV5Temperature1(ChannelUID channelUID) {
        logger.debug("Publishing temperature1");
        updateState(CHANNEL_HCV5_TEMPERATURE1, DecimalType.valueOf(Float.toString(danthermModbus.temperatureOutdoor)));
    }

    @SuppressWarnings("null")
    private void publishHCV5Temperature2(ChannelUID channelUID) {
        logger.debug("Publishing temperature2");
        updateState(CHANNEL_HCV5_TEMPERATURE2, DecimalType.valueOf(Float.toString(danthermModbus.temperatureSupply)));
    }

    @SuppressWarnings("null")
    private void publishHCV5Temperature3(ChannelUID channelUID) {
        logger.debug("Publishing temperature3");
        updateState(CHANNEL_HCV5_TEMPERATURE3, DecimalType.valueOf(Float.toString(danthermModbus.temperatureExtract)));
    }

    @SuppressWarnings("null")
    private void publishHCV5Temperature4(ChannelUID channelUID) {
        logger.debug("Publishing temperature4");
        updateState(CHANNEL_HCV5_TEMPERATURE4, DecimalType.valueOf(Float.toString(danthermModbus.temperatureExhaust)));
    }

    @SuppressWarnings("null")
    private void publishHCV5FanSpeed(ChannelUID channelUID) {
        logger.debug("Publishing fan speed");
        updateState(CHANNEL_HCV5_FANSPEED, DecimalType.valueOf(Integer.toString(danthermModbus.speedLevelFan)));
    }

    @SuppressWarnings("null")
    private void publishHCV5CurrentUnitMode(ChannelUID channelUID) {
        logger.debug("Publishing current unit mode");
        updateState(CHANNEL_HCV5_CURRENT_UNITMODE,
                DecimalType.valueOf(Integer.toString(danthermModbus.currentUnitMode)));
    }

    @SuppressWarnings("null")
    private void publishHCV5ActiveUnitMode(ChannelUID channelUID) {
        logger.debug("Publishing active unit mode");
        updateState(CHANNEL_HCV5_ACTIVE_UNITMODE, DecimalType.valueOf(Integer.toString(danthermModbus.activeUnitMode)));
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // TODO Auto-generated method stub
        // super.handleUpdate(channelUID, newState);
        logger.debug("DanthermHandler TODO");

    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO Auto-generated method stub
        // super.thingUpdated(thing);
        logger.debug("DanthermHandler TODO");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

        if (channelUID.getId().equals(CHANNEL_HCV5_FANSPEED)) {
            handleHCV5FanPower(command);
        } else if (channelUID.getId().equals(CHANNEL_HCV5_CURRENT_UNITMODE)) {
            handleHCV5CurrentUnitMode(command);
        } else if (channelUID.getId().equals(CHANNEL_HCV5_ACTIVE_UNITMODE)) {
            handleHCV5ActiveUnitMode(command);
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

    @SuppressWarnings("null")
    private void handleHCV5FanPower(Command command) {
        logger.debug("Handling HCV5 fan power command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a temperature
                updateState(CHANNEL_HCV5_FANSPEED, DecimalType.valueOf("2"));

            } else {
                updateState(CHANNEL_HCV5_FANSPEED, DecimalType.valueOf(Float.toString(danthermModbus.speedLevelFan)));
            }
        } else {
            if (simulationMode) {
                // Do nothing
            } else {
                // Command must be either 0, 1, 2, 3 or 4
                switch (Integer.parseUnsignedInt(command.toString())) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        danthermModbus.setFanSpeed(Integer.parseUnsignedInt(command.toString()));

                        break;
                    default:
                        logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
                }

            }

        }
    }

    @SuppressWarnings("null")
    private void handleHCV5ActiveUnitMode(Command command) {
        logger.debug("Handling HCV5 active unit mode command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a manual mode
                updateState(CHANNEL_HCV5_ACTIVE_UNITMODE, DecimalType.valueOf("4"));

            } else {
                updateState(CHANNEL_HCV5_ACTIVE_UNITMODE,
                        DecimalType.valueOf(Integer.toString(danthermModbus.activeUnitMode)));
            }
        } else {
            if (simulationMode) {
                // Do nothing
            } else {
                danthermModbus.setActiveUnitMode(Integer.parseUnsignedInt(command.toString()));
            }
        }
    }

    @SuppressWarnings("null")
    private void handleHCV5CurrentUnitMode(Command command) {
        logger.debug("Handling HCV5 current unit mode command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a manual mode
                updateState(CHANNEL_HCV5_CURRENT_UNITMODE, DecimalType.valueOf("1"));

            } else {
                updateState(CHANNEL_HCV5_CURRENT_UNITMODE,
                        DecimalType.valueOf(Integer.toString(danthermModbus.currentUnitMode)));
            }
        } else {
            logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
        }
    }

    @SuppressWarnings("null")
    private void handleHCV5Temp1(Command command) {
        logger.debug("Handling command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a temperature
                updateState(CHANNEL_HCV5_TEMPERATURE1, DecimalType.valueOf("19.2"));

            } else {
                updateState(CHANNEL_HCV5_TEMPERATURE1,
                        DecimalType.valueOf(Float.toString(danthermModbus.temperatureOutdoor)));
            }
        } else {
            logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
        }
    }

    @SuppressWarnings("null")
    private void handleHCV5Temp2(Command command) {
        logger.debug("Handling HCV5 temp2 command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a temperature
                updateState(CHANNEL_HCV5_TEMPERATURE2, DecimalType.valueOf("18.2"));

            } else {
                updateState(CHANNEL_HCV5_TEMPERATURE2,
                        DecimalType.valueOf(Float.toString(danthermModbus.temperatureSupply)));
            }
        } else {
            logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
        }
    }

    @SuppressWarnings("null")
    private void handleHCV5Temp3(Command command) {
        logger.debug("Handling HCV5 temp3 command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a temperature
                updateState(CHANNEL_HCV5_TEMPERATURE3, DecimalType.valueOf("18.6"));

            } else {
                updateState(CHANNEL_HCV5_TEMPERATURE3,
                        DecimalType.valueOf(Float.toString(danthermModbus.temperatureExtract)));
            }
        } else {
            logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
        }
    }

    @SuppressWarnings("null")
    private void handleHCV5Temp4(Command command) {
        logger.debug("Handling HCV5 temp4 command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            if (simulationMode) {
                // Just fake a temperature
                updateState(CHANNEL_HCV5_TEMPERATURE4, DecimalType.valueOf("19.0"));

            } else {
                updateState(CHANNEL_HCV5_TEMPERATURE4,
                        DecimalType.valueOf(Float.toString(danthermModbus.temperatureExhaust)));
            }
        } else {
            logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
        }
    }

    class PeriodicTask extends TimerTask {

        @Override
        public void run() {

            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                if (isLinked(channel.getUID().getId())) {
                    publishValue(channel.getUID());
                }
            }

        }

    }

}
