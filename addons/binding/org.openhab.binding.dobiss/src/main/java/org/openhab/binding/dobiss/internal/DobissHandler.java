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

import static org.openhab.binding.dobiss.internal.DobissBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
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
 * The {@link DobissHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class DobissHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DobissHandler.class);

    @Nullable
    private DobissConfiguration config;

    // Timer for pushing the updates of the dobiss modules to the UI
    @Nullable
    Timer timer;

    // Polling interval in seconds
    private int pollingInterval;
    // Delay of the internal timer in ms
    private int pollingDelay = 500;

    // Caching variables
    private int dobissRelay01Id01 = -1;
    private int dobissRelay01Id02 = -1;
    private int dobissRelay01Id03 = -1;
    private int dobissRelay01Id04 = -1;
    private int dobissRelay01Id05 = -1;
    private int dobissRelay01Id06 = -1;
    private int dobissRelay01Id07 = -1;
    private int dobissRelay01Id08 = -1;
    private int dobissRelay01Id09 = -1;
    private int dobissRelay01Id10 = -1;
    private int dobissRelay01Id11 = -1;
    private int dobissRelay01Id12 = -1;
    private int dobissRelay02Id01 = -1;
    private int dobissRelay02Id02 = -1;
    private int dobissRelay02Id03 = -1;
    private int dobissRelay02Id04 = -1;
    private int dobissRelay02Id05 = -1;
    private int dobissRelay02Id06 = -1;
    private int dobissRelay02Id07 = -1;
    private int dobissRelay02Id08 = -1;
    private int dobissRelay02Id09 = -1;
    private int dobissRelay02Id10 = -1;
    private int dobissRelay02Id11 = -1;
    private int dobissRelay02Id12 = -1;
    private int dobissDimmer01Id01 = -1;
    private int dobissDimmer01Id02 = -1;
    private int dobissDimmer01Id03 = -1;
    private int dobissDimmer01Id04 = -1;
    private int dobissDimmer02Id01 = -1;
    private int dobissDimmer02Id02 = -1;
    private int dobissDimmer02Id03 = -1;
    private int dobissDimmer02Id04 = -1;

    private DobissRelay dobissRelay01 = new DobissRelay();
    private DobissRelay dobissRelay02 = new DobissRelay();

    private DobissDimmer dobissDimmer01 = new DobissDimmer();
    private DobissDimmer dobissDimmer02 = new DobissDimmer();

    // Multiple modules can be reached with same ip address, but Dobiss tcp server only expects on client.
    // So we need to make sure that we are communicating one at a time.
    // Semaphore will be locked when tcp socket communication is already happening (e.g. in another thread because of
    // timer).
    private static Semaphore mutex_tcp_communication = new Semaphore(1, true);

    public DobissHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);

        if (channelUID.getId().equals(CHANNEL_RELAY01_ID_1)) {
            handleDobissRelay(command, 1, 1);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_2)) {
            handleDobissRelay(command, 1, 2);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_3)) {
            handleDobissRelay(command, 1, 3);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_4)) {
            handleDobissRelay(command, 1, 4);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_5)) {
            handleDobissRelay(command, 1, 5);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_6)) {
            handleDobissRelay(command, 1, 6);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_7)) {
            handleDobissRelay(command, 1, 7);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_8)) {
            handleDobissRelay(command, 1, 8);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_9)) {
            handleDobissRelay(command, 1, 9);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_10)) {
            handleDobissRelay(command, 1, 10);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_11)) {
            handleDobissRelay(command, 1, 11);
        } else if (channelUID.getId().equals(CHANNEL_RELAY01_ID_12)) {
            handleDobissRelay(command, 1, 12);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_1)) {
            handleDobissRelay(command, 2, 1);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_2)) {
            handleDobissRelay(command, 2, 2);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_3)) {
            handleDobissRelay(command, 2, 3);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_4)) {
            handleDobissRelay(command, 2, 4);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_5)) {
            handleDobissRelay(command, 2, 5);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_6)) {
            handleDobissRelay(command, 2, 6);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_7)) {
            handleDobissRelay(command, 2, 7);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_8)) {
            handleDobissRelay(command, 2, 8);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_9)) {
            handleDobissRelay(command, 2, 9);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_10)) {
            handleDobissRelay(command, 2, 10);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_11)) {
            handleDobissRelay(command, 2, 11);
        } else if (channelUID.getId().equals(CHANNEL_RELAY02_ID_12)) {
            handleDobissRelay(command, 2, 12);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER01_ID_1)) {
            handleDobissDimmer(command, 1, 1);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER01_ID_2)) {
            handleDobissDimmer(command, 1, 2);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER01_ID_3)) {
            handleDobissDimmer(command, 1, 3);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER01_ID_4)) {
            handleDobissDimmer(command, 1, 4);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER02_ID_1)) {
            handleDobissDimmer(command, 2, 1);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER02_ID_1)) {
            handleDobissDimmer(command, 2, 1);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER02_ID_1)) {
            handleDobissDimmer(command, 2, 1);
        } else if (channelUID.getId().equals(CHANNEL_DIMMER02_ID_1)) {
            handleDobissDimmer(command, 2, 1);
        } else {
            logger.debug("Received command for {} on unknown channel {}", thing.getUID(), channelUID.getId());
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {

        logger.debug("{}: Initializing Dobbis handler", thing.getUID());

        config = getConfigAs(DobissConfiguration.class);

        // Check ip address
        if (StringUtils.isBlank(config.ipAddress)) {
            logger.debug("{}: DobissHandler config is invalid. Check configuration.", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss config. IP address is blank.");
            return;
        }
        dobissRelay01.setIpAddress(config.ipAddress);
        dobissRelay02.setIpAddress(config.ipAddress);
        dobissDimmer01.setIpAddress(config.ipAddress);
        dobissDimmer02.setIpAddress(config.ipAddress);
        logger.debug("{}: IP address for relay01 used is {}", thing.getUID(), dobissRelay01.getIpAddress());
        logger.debug("{}: IP address for relay02 used is {}", thing.getUID(), dobissRelay02.getIpAddress());
        logger.debug("{}: IP address for dimmer01 used is {}", thing.getUID(), dobissDimmer01.getIpAddress());
        logger.debug("{}: IP address for dimmer02 used is {}", thing.getUID(), dobissDimmer02.getIpAddress());

        // Check polling interval
        if (config.pollingInterval < 1) {
            logger.debug("{}: DobissHandler config is invalid. Check configuration.", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss config. Polling interval is smaller than 1.");
            return;
        }
        pollingInterval = config.pollingInterval;
        logger.debug("{}: Polling interval (in seconds) used is {}", thing.getUID(), pollingInterval);

        // Check relay01 address
        if (config.addressRelay01 < 1) {
            dobissRelay01.setModuleAddress(-1);
            logger.debug("{}: Relay01 not used.", thing.getUID());
        } else {
            dobissRelay01.setModuleAddress(config.addressRelay01);
            logger.debug("{}: Address for relay01 used is {}", thing.getUID(), dobissRelay01.getModuleAddress());
        }

        // Check relay02 address
        if (config.addressRelay02 < 1) {
            dobissRelay02.setModuleAddress(-1);
            logger.debug("{}: Relay02 not used.", thing.getUID());
        } else {
            dobissRelay02.setModuleAddress(config.addressRelay02);
            logger.debug("{}: Address for relay02 used is {}", thing.getUID(), dobissRelay02.getModuleAddress());
        }

        // Check dimmer01 address
        if (config.addressDimmer01 < 1) {
            dobissDimmer01.setModuleAddress(-1);
            logger.debug("{}: Dimmer01 not used.", thing.getUID());
        } else {
            dobissDimmer01.setModuleAddress(config.addressDimmer01);
            logger.debug("{}: Address for dimmer01 used is {}", thing.getUID(), dobissDimmer01.getModuleAddress());
        }

        // Check dimmer02 address
        if (config.addressDimmer02 < 1) {
            logger.debug("{} Dimmer02 not used.", thing.getUID());
            dobissDimmer02.setModuleAddress(-1);
        } else {
            dobissDimmer02.setModuleAddress(config.addressDimmer01);
            logger.debug("{}: Address for dimmer01 used is {}", thing.getUID(), dobissDimmer02.getModuleAddress());
        }

        // Check connections to configured Dobiss modules
        // No tcp mutex needed here as the timer is not started yet
        if (dobissRelay01.getModuleAddress() != -1) {
            try {
                dobissRelay01.updateStatus();
            } catch (IOException e) {
                logger.debug("Communication to Dobiss relay01 unit failed!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Unable to communicate with Dobiss relay01 unit");
            }
        }

        if (dobissRelay02.getModuleAddress() != -1) {
            try {
                dobissRelay02.updateStatus();
            } catch (IOException e) {
                logger.debug("Communication to Dobiss relay02 unit failed!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Unable to communicate with Dobiss relay02 unit");
            }
        }

        if (dobissDimmer01.getModuleAddress() != -1) {
            try {
                dobissDimmer01.updateStatus();
            } catch (IOException e) {
                logger.debug("Communication to Dobiss dimmer01 unit failed!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Unable to communicate with Dobiss dimmer01 unit");
            }
        }

        if (dobissDimmer02.getModuleAddress() != -1) {
            try {
                dobissDimmer02.updateStatus();
            } catch (IOException e) {
                logger.debug("Communication to Dobiss dimmer02 unit failed!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Unable to communicate with Dobiss dimmer02 unit");
            }
        }

        updateStatus(ThingStatus.ONLINE);

        startAutomaticRefresh();

    }

    private void handleDobissRelay(Command command, int relayIndex, int id) {
        logger.debug("Handling Dobiss relay command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            switch (id) {
                case 1:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_1,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id01)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_1,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id01)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 2:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_2,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id02)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_2,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id02)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 3:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_3,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id03)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_3,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id03)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 4:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_4,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id04)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_4,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id04)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 5:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_5,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id05)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_5,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id05)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 6:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_6,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id06)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_6,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id06)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 7:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_7,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id07)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_7,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id07)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 8:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_8,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id08)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_8,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id08)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 9:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_9,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id09)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_9,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id09)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 10:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_10,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id10)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_10,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id10)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 11:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_11,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id11)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_11,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id11)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                case 12:
                    switch (relayIndex) {
                        case 1:
                            updateState(CHANNEL_RELAY01_ID_12,
                                    DecimalType.valueOf(Integer.toString(dobissRelay01.id12)));
                            break;
                        case 2:
                            updateState(CHANNEL_RELAY02_ID_12,
                                    DecimalType.valueOf(Integer.toString(dobissRelay02.id12)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong relay index");
                    }
                    break;
                default:
                    logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }

        } else {
            // Command must be either ON or OFF
            if (command.toString().equals("OFF")) {
                sendDobissRelay(id, relayIndex, 0);
            } else if (command.toString().equals("ON")) {
                sendDobissRelay(id, relayIndex, 1);
            } else {
                logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }
        }
    }

    private void handleDobissDimmer(Command command, int dimmerIndex, int id) {
        logger.debug("Handling Dobiss dimmer command for {}: {}", thing.getUID(), command);

        if (command instanceof RefreshType) {
            switch (id) {
                case 1:
                    switch (dimmerIndex) {
                        case 1:
                            updateState(CHANNEL_DIMMER01_ID_1,
                                    PercentType.valueOf(Integer.toString(dobissDimmer01.id01)));
                            break;
                        case 2:
                            updateState(CHANNEL_DIMMER02_ID_1,
                                    PercentType.valueOf(Integer.toString(dobissDimmer02.id01)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong dimmer index");
                    }
                    break;
                case 2:
                    switch (dimmerIndex) {
                        case 1:
                            updateState(CHANNEL_DIMMER01_ID_2,
                                    PercentType.valueOf(Integer.toString(dobissDimmer01.id02)));
                            break;
                        case 2:
                            updateState(CHANNEL_DIMMER02_ID_2,
                                    PercentType.valueOf(Integer.toString(dobissDimmer02.id02)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong dimmer index");
                    }
                    break;
                case 3:
                    switch (dimmerIndex) {
                        case 1:
                            updateState(CHANNEL_DIMMER01_ID_3,
                                    PercentType.valueOf(Integer.toString(dobissDimmer01.id03)));
                            break;
                        case 2:
                            updateState(CHANNEL_DIMMER02_ID_3,
                                    PercentType.valueOf(Integer.toString(dobissDimmer02.id03)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong dimmer index");
                    }
                    break;
                case 4:
                    switch (dimmerIndex) {
                        case 1:
                            updateState(CHANNEL_DIMMER01_ID_4,
                                    PercentType.valueOf(Integer.toString(dobissDimmer01.id04)));
                            break;
                        case 2:
                            updateState(CHANNEL_DIMMER02_ID_4,
                                    PercentType.valueOf(Integer.toString(dobissDimmer02.id04)));
                            break;
                        default:
                            logger.debug("Internal error: Wrong dimmer index");
                    }
                    break;
                default:
                    logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }

        } else {
            // Command must be number between 0 and 100
            int dimmerValue = Integer.parseInt(command.toString());
            if ((0 <= dimmerValue) && (100 >= dimmerValue)) {
                sendDobissDimmer(id, dimmerIndex, dimmerValue);
            } else {
                logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }
        }
    }

    private void sendDobissRelay(int id, int relayIndex, int onOff) {
        switch (relayIndex) {
            case 1:
                try {
                    mutex_tcp_communication.acquire();
                    try {
                        dobissRelay01.sendCommand(id, onOff);
                    } finally {
                        mutex_tcp_communication.release();
                    }
                } catch (IOException e) {
                    logger.info("Unable to send command to relay01 for id {} switching to {}", id, onOff);
                } catch (InterruptedException e) {
                    logger.info("Mutex error for Dobiss tcp communication.");
                }
                break;
            case 2:
                try {
                    mutex_tcp_communication.acquire();
                    try {
                        dobissRelay02.sendCommand(id, onOff);
                    } finally {
                        mutex_tcp_communication.release();
                    }
                } catch (IOException e) {
                    logger.info("Unable to send command to relay02 for id {} switching to {}", id, onOff);
                } catch (InterruptedException e) {
                    logger.info("Mutex error for Dobiss tcp communication.");
                }
                break;
            default:
                logger.debug("Internal error: Wrong relay index");
        }

    }

    private void sendDobissDimmer(int id, int index, int value) {
        switch (index) {
            case 1:
                try {
                    mutex_tcp_communication.acquire();
                    try {
                        dobissDimmer01.sendCommand(id, value);
                    } finally {
                        mutex_tcp_communication.release();
                    }
                } catch (IOException e) {
                    logger.info("Unable to send command to dimmer01 for id {} percentage to {}", id, value);
                } catch (InterruptedException e) {
                    logger.info("Mutex error for Dobiss tcp communication.");
                }
                break;
            case 2:
                try {
                    mutex_tcp_communication.acquire();
                    try {
                        dobissDimmer02.sendCommand(id, value);
                    } finally {
                        mutex_tcp_communication.release();
                    }
                } catch (IOException e) {
                    logger.info("Unable to send command to dimmer02 for id {} percentage to {}", id, value);
                } catch (InterruptedException e) {
                    logger.info("Mutex error for Dobiss tcp communication.");
                }
                break;
            default:
                logger.debug("Internal error: Wrong relay index");
        }

    }

    private void publishValue(ChannelUID channelUID) {
        logger.debug("Publishing value for channel {}", channelUID);

        String channelID = channelUID.getId();
        switch (channelID) {
            case CHANNEL_RELAY01_ID_1:
                if (dobissRelay01Id01 != dobissRelay01.id01) {
                    updateState(CHANNEL_RELAY01_ID_1, DecimalType.valueOf(Integer.toString(dobissRelay01.id01)));
                    dobissRelay01Id01 = dobissRelay01.id01;
                    logger.debug("Update status for relay01_id01 {}", dobissRelay01.id01);
                }
                break;
            case CHANNEL_RELAY01_ID_2:
                if (dobissRelay01Id02 != dobissRelay01.id02) {
                    updateState(CHANNEL_RELAY01_ID_2, DecimalType.valueOf(Integer.toString(dobissRelay01.id02)));
                    dobissRelay01Id02 = dobissRelay01.id02;
                    logger.debug("Update status for relay01_id02 {}", dobissRelay01.id02);
                }
                break;
            case CHANNEL_RELAY01_ID_3:
                if (dobissRelay01Id03 != dobissRelay01.id03) {
                    updateState(CHANNEL_RELAY01_ID_3, DecimalType.valueOf(Integer.toString(dobissRelay01.id03)));
                    dobissRelay01Id03 = dobissRelay01.id03;
                    logger.debug("Update status for relay01_id03 {}", dobissRelay01.id03);
                }
                break;
            case CHANNEL_RELAY01_ID_4:
                if (dobissRelay01Id04 != dobissRelay01.id04) {
                    updateState(CHANNEL_RELAY01_ID_4, DecimalType.valueOf(Integer.toString(dobissRelay01.id04)));
                    dobissRelay01Id04 = dobissRelay01.id04;
                    logger.debug("Update status for relay01_id04 {}", dobissRelay01.id04);
                }
                break;
            case CHANNEL_RELAY01_ID_5:
                if (dobissRelay01Id05 != dobissRelay01.id05) {
                    updateState(CHANNEL_RELAY01_ID_5, DecimalType.valueOf(Integer.toString(dobissRelay01.id05)));
                    dobissRelay01Id05 = dobissRelay01.id05;
                    logger.debug("Update status for relay01_id05 {}", dobissRelay01.id05);
                }
                break;
            case CHANNEL_RELAY01_ID_6:
                if (dobissRelay01Id06 != dobissRelay01.id06) {
                    updateState(CHANNEL_RELAY01_ID_6, DecimalType.valueOf(Integer.toString(dobissRelay01.id06)));
                    dobissRelay01Id06 = dobissRelay01.id06;
                    logger.debug("Update status for relay01_id06 {}", dobissRelay01.id06);
                }
                break;
            case CHANNEL_RELAY01_ID_7:
                if (dobissRelay01Id07 != dobissRelay01.id07) {
                    updateState(CHANNEL_RELAY01_ID_7, DecimalType.valueOf(Integer.toString(dobissRelay01.id07)));
                    dobissRelay01Id07 = dobissRelay01.id07;
                    logger.debug("Update status for relay01_id07 {}", dobissRelay01.id07);
                }
                break;
            case CHANNEL_RELAY01_ID_8:
                if (dobissRelay01Id08 != dobissRelay01.id08) {
                    updateState(CHANNEL_RELAY01_ID_8, DecimalType.valueOf(Integer.toString(dobissRelay01.id08)));
                    dobissRelay01Id08 = dobissRelay01.id08;
                    logger.debug("Update status for relay01_id08 {}", dobissRelay01.id08);
                }
                break;
            case CHANNEL_RELAY01_ID_9:
                if (dobissRelay01Id09 != dobissRelay01.id09) {
                    updateState(CHANNEL_RELAY01_ID_9, DecimalType.valueOf(Integer.toString(dobissRelay01.id09)));
                    dobissRelay01Id09 = dobissRelay01.id09;
                    logger.debug("Update status for relay01_id09 {}", dobissRelay01.id09);
                }
                break;
            case CHANNEL_RELAY01_ID_10:
                if (dobissRelay01Id10 != dobissRelay01.id10) {
                    updateState(CHANNEL_RELAY01_ID_10, DecimalType.valueOf(Integer.toString(dobissRelay01.id10)));
                    dobissRelay01Id10 = dobissRelay01.id10;
                    logger.debug("Update status for relay01_id10 {}", dobissRelay01.id10);
                }
                break;
            case CHANNEL_RELAY01_ID_11:
                if (dobissRelay01Id11 != dobissRelay01.id11) {
                    updateState(CHANNEL_RELAY01_ID_11, DecimalType.valueOf(Integer.toString(dobissRelay01.id11)));
                    dobissRelay01Id11 = dobissRelay01.id11;
                    logger.debug("Update status for relay01_id11 {}", dobissRelay01.id11);
                }
                break;
            case CHANNEL_RELAY01_ID_12:
                if (dobissRelay01Id12 != dobissRelay01.id12) {
                    updateState(CHANNEL_RELAY01_ID_12, DecimalType.valueOf(Integer.toString(dobissRelay01.id12)));
                    dobissRelay01Id12 = dobissRelay01.id12;
                    logger.debug("Update status for relay01_id12 {}", dobissRelay01.id12);
                }
                break;
            case CHANNEL_RELAY02_ID_1:
                if (dobissRelay02Id01 != dobissRelay02.id01) {
                    updateState(CHANNEL_RELAY02_ID_1, DecimalType.valueOf(Integer.toString(dobissRelay02.id01)));
                    dobissRelay02Id01 = dobissRelay02.id01;
                    logger.debug("Update status for relay02_id01 {}", dobissRelay02.id01);
                }
                break;
            case CHANNEL_RELAY02_ID_2:
                if (dobissRelay02Id02 != dobissRelay02.id02) {
                    updateState(CHANNEL_RELAY02_ID_2, DecimalType.valueOf(Integer.toString(dobissRelay02.id02)));
                    dobissRelay02Id02 = dobissRelay02.id02;
                    logger.debug("Update status for relay02_id02 {}", dobissRelay02.id02);
                }
                break;
            case CHANNEL_RELAY02_ID_3:
                if (dobissRelay02Id03 != dobissRelay02.id03) {
                    updateState(CHANNEL_RELAY02_ID_3, DecimalType.valueOf(Integer.toString(dobissRelay02.id03)));
                    dobissRelay02Id03 = dobissRelay02.id03;
                    logger.debug("Update status for relay02_id03 {}", dobissRelay02.id03);
                }
                break;
            case CHANNEL_RELAY02_ID_4:
                if (dobissRelay02Id04 != dobissRelay02.id04) {
                    updateState(CHANNEL_RELAY02_ID_4, DecimalType.valueOf(Integer.toString(dobissRelay02.id04)));
                    dobissRelay02Id04 = dobissRelay02.id04;
                    logger.debug("Update status for relay02_id04 {}", dobissRelay02.id04);
                }
                break;
            case CHANNEL_RELAY02_ID_5:
                if (dobissRelay02Id05 != dobissRelay02.id05) {
                    updateState(CHANNEL_RELAY02_ID_5, DecimalType.valueOf(Integer.toString(dobissRelay02.id05)));
                    dobissRelay02Id05 = dobissRelay02.id05;
                    logger.debug("Update status for relay02_id05 {}", dobissRelay02.id05);
                }
                break;
            case CHANNEL_RELAY02_ID_6:
                if (dobissRelay02Id06 != dobissRelay02.id06) {
                    updateState(CHANNEL_RELAY02_ID_6, DecimalType.valueOf(Integer.toString(dobissRelay02.id06)));
                    dobissRelay02Id06 = dobissRelay02.id06;
                    logger.debug("Update status for relay02_id06 {}", dobissRelay02.id06);
                }
                break;
            case CHANNEL_RELAY02_ID_7:
                if (dobissRelay02Id07 != dobissRelay02.id07) {
                    updateState(CHANNEL_RELAY02_ID_7, DecimalType.valueOf(Integer.toString(dobissRelay02.id07)));
                    dobissRelay02Id07 = dobissRelay02.id07;
                    logger.debug("Update status for relay02_id07 {}", dobissRelay02.id07);
                }
                break;
            case CHANNEL_RELAY02_ID_8:
                if (dobissRelay02Id08 != dobissRelay02.id08) {
                    updateState(CHANNEL_RELAY02_ID_8, DecimalType.valueOf(Integer.toString(dobissRelay02.id08)));
                    dobissRelay02Id08 = dobissRelay02.id08;
                    logger.debug("Update status for relay02_id08 {}", dobissRelay02.id08);
                }
                break;
            case CHANNEL_RELAY02_ID_9:
                if (dobissRelay02Id09 != dobissRelay02.id09) {
                    updateState(CHANNEL_RELAY02_ID_9, DecimalType.valueOf(Integer.toString(dobissRelay02.id09)));
                    dobissRelay02Id09 = dobissRelay02.id09;
                    logger.debug("Update status for relay02_id09 {}", dobissRelay02.id09);
                }
                break;
            case CHANNEL_RELAY02_ID_10:
                if (dobissRelay02Id10 != dobissRelay02.id10) {
                    updateState(CHANNEL_RELAY02_ID_10, DecimalType.valueOf(Integer.toString(dobissRelay02.id10)));
                    dobissRelay02Id10 = dobissRelay02.id10;
                    logger.debug("Update status for relay02_id10 {}", dobissRelay02.id10);
                }
                break;
            case CHANNEL_RELAY02_ID_11:
                if (dobissRelay02Id11 != dobissRelay02.id11) {
                    updateState(CHANNEL_RELAY02_ID_11, DecimalType.valueOf(Integer.toString(dobissRelay02.id11)));
                    dobissRelay02Id11 = dobissRelay02.id11;
                    logger.debug("Update status for relay02_id11 {}", dobissRelay02.id11);
                }
                break;
            case CHANNEL_RELAY02_ID_12:
                if (dobissRelay02Id12 != dobissRelay02.id12) {
                    updateState(CHANNEL_RELAY02_ID_12, DecimalType.valueOf(Integer.toString(dobissRelay02.id12)));
                    dobissRelay02Id12 = dobissRelay02.id12;
                    logger.debug("Update status for relay02_id12 {}", dobissRelay02.id12);
                }
                break;
            case CHANNEL_DIMMER01_ID_1:
                if (dobissDimmer01Id01 != dobissDimmer01.id01) {
                    updateState(CHANNEL_DIMMER01_ID_1, PercentType.valueOf(Integer.toString(dobissDimmer01.id01)));
                    dobissDimmer01Id01 = dobissDimmer01.id01;
                    logger.debug("Update status for dimmer01_id01 {}", dobissDimmer01.id01);
                }
                break;
            case CHANNEL_DIMMER01_ID_2:
                if (dobissDimmer01Id02 != dobissDimmer01.id02) {
                    updateState(CHANNEL_DIMMER01_ID_2, PercentType.valueOf(Integer.toString(dobissDimmer01.id02)));
                    dobissDimmer01Id02 = dobissDimmer01.id02;
                    logger.debug("Update status for dimmer01_id02 {}", dobissDimmer01.id02);
                }
                break;
            case CHANNEL_DIMMER01_ID_3:
                if (dobissDimmer01Id03 != dobissDimmer01.id03) {
                    updateState(CHANNEL_DIMMER01_ID_3, PercentType.valueOf(Integer.toString(dobissDimmer01.id03)));
                    dobissDimmer01Id03 = dobissDimmer01.id03;
                    logger.debug("Update status for dimmer01_id03 {}", dobissDimmer01.id03);
                }
                break;
            case CHANNEL_DIMMER01_ID_4:
                if (dobissDimmer01Id04 != dobissDimmer01.id04) {
                    updateState(CHANNEL_DIMMER01_ID_4, PercentType.valueOf(Integer.toString(dobissDimmer01.id04)));
                    dobissDimmer01Id04 = dobissDimmer01.id04;
                    logger.debug("Update status for dimmer01_id04 {}", dobissDimmer01.id04);
                }
                break;
            case CHANNEL_DIMMER02_ID_1:
                if (dobissDimmer02Id01 != dobissDimmer02.id01) {
                    updateState(CHANNEL_DIMMER02_ID_1, PercentType.valueOf(Integer.toString(dobissDimmer02.id01)));
                    dobissDimmer02Id01 = dobissDimmer02.id01;
                    logger.debug("Update status for dimmer02_id01 {}", dobissDimmer02.id01);
                }
                break;
            case CHANNEL_DIMMER02_ID_2:
                if (dobissDimmer02Id02 != dobissDimmer02.id02) {
                    updateState(CHANNEL_DIMMER02_ID_2, PercentType.valueOf(Integer.toString(dobissDimmer02.id02)));
                    dobissDimmer02Id02 = dobissDimmer02.id02;
                    logger.debug("Update status for dimmer02_id02 {}", dobissDimmer02.id02);
                }
                break;
            case CHANNEL_DIMMER02_ID_3:
                if (dobissDimmer02Id03 != dobissDimmer02.id03) {
                    updateState(CHANNEL_DIMMER02_ID_3, PercentType.valueOf(Integer.toString(dobissDimmer02.id03)));
                    dobissDimmer02Id03 = dobissDimmer02.id03;
                    logger.debug("Update status for dimmer02_id03 {}", dobissDimmer02.id03);
                }
                break;
            case CHANNEL_DIMMER02_ID_4:
                if (dobissDimmer02Id04 != dobissDimmer02.id04) {
                    updateState(CHANNEL_DIMMER02_ID_4, PercentType.valueOf(Integer.toString(dobissDimmer02.id04)));
                    dobissDimmer02Id04 = dobissDimmer02.id04;
                    logger.debug("Update status for dimmer02_id04 {}", dobissDimmer02.id04);
                }
                break;
            default:
                logger.debug("Can not update channel with ID : {} - channel name might be wrong!", channelID);
                break;
        }
    }

    private void queryDobiss() {
        try {
            dobissRelay01.updateStatus();
        } catch (IOException e) {
            logger.info("Unable to update values for relay01");
        }
        try {
            dobissRelay02.updateStatus();
        } catch (IOException e) {
            logger.info("Unable to update values for relay02");
        }
        try {
            dobissDimmer01.updateStatus();
        } catch (IOException e) {
            logger.info("Unable to update values for dimmer01");
        }
        try {
            dobissDimmer02.updateStatus();
        } catch (IOException e) {
            logger.info("Unable to update values for dimmer02");
        }
    }

    private void startAutomaticRefresh() {
        logger.debug("Starting automatic refresh");

        timer = new Timer();
        timer.scheduleAtFixedRate(new PeriodicTask(), pollingDelay, pollingInterval * 1000);

        logger.debug("Start automatic refresh every {} seconds", pollingInterval);
    }

    class PeriodicTask extends TimerTask {

        @Override
        public void run() {

            try {
                mutex_tcp_communication.acquire();
                try {
                    queryDobiss();
                } finally {
                    mutex_tcp_communication.release();
                }
            } catch (InterruptedException e) {
                logger.info("Mutex error for Dobiss tcp communication.");
            }

            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                if (isLinked(channel.getUID().getId())) {
                    publishValue(channel.getUID());
                }
            }

        }

    }

}
