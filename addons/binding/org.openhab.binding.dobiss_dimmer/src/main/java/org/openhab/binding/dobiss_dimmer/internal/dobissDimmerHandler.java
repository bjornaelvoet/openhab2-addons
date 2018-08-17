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

import static org.openhab.binding.dobiss_dimmer.internal.dobissDimmerBindingConstants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

import be.aelvoetnet.dobissmutex.DobissMutex;

/**
 * The {@link dobissDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class dobissDimmerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(dobissDimmerHandler.class);

    // private FileMutex mutex = new FileMutex(Paths.get(System.getProperty("user.home"), ".dobiss.openhab.lock"));
    //
    //// Blocking for 100 ms
    // private long mutex_timeout = 100;

    @Nullable
    private dobissDimmerConfiguration config;

    // Timer for pushing the updates of the HCV5 to the UI
    @Nullable
    Timer timer;

    @Nullable
    private String ipAddress = null;

    private int pollingInterval;
    private int pollingDelay = 1000;
    private int address;

    private int portNumber = 1001;

    // Dimmer so value between 0 and 100
    private int id01 = 0;
    private int id02 = 0;
    private int id03 = 0;
    private int id04 = 0;

    public dobissDimmerHandler(Thing thing) {
        super(thing);

        this.thing = thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);

        if (channelUID.getId().equals(CHANNEL_DOBISS_DIMMER_ID_1)) {
            handleDobissDimmer(command, 1);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_DIMMER_ID_2)) {
            handleDobissDimmer(command, 2);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_DIMMER_ID_3)) {
            handleDobissDimmer(command, 3);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_DIMMER_ID_4)) {
            handleDobissDimmer(command, 4);
        } else {
            logger.debug("Received command for {} on unknown channel {}", thing.getUID(), channelUID.getId());
        }

    }

    private void handleDobissDimmer(Command command, int id) {
        logger.debug("Handling Dobiss dimmmer command for {}: {}", thing.getUID(), command);

        // TODO Again do tcp communication to have actual refresh

        if (command instanceof RefreshType) {
            switch (id) {
                case 1:
                    logger.debug("Dobiss dimmer value for id01 {}", id01);
                    updateState(CHANNEL_DOBISS_DIMMER_ID_1, PercentType.valueOf(Integer.toString(id01)));
                    break;
                case 2:
                    logger.debug("Dobiss dimmer value for id02 {}", id02);
                    updateState(CHANNEL_DOBISS_DIMMER_ID_2, PercentType.valueOf(Integer.toString(id02)));
                    break;
                case 3:
                    logger.debug("Dobiss dimmer value for id03 {}", id03);
                    updateState(CHANNEL_DOBISS_DIMMER_ID_3, PercentType.valueOf(Integer.toString(id03)));
                    break;
                case 4:
                    logger.debug("Dobiss dimmer value for id04 {}", id04);
                    updateState(CHANNEL_DOBISS_DIMMER_ID_4, PercentType.valueOf(Integer.toString(id04)));
                    break;
                default:
                    logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }

        } else {
            // Command must be number between 0 and 100
            int dimmerValue = Integer.parseInt(command.toString());
            if ((0 <= dimmerValue) && (100 >= dimmerValue)) {
                sendDobissDimmer(id, dimmerValue);
            } else {
                logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }
        }
    }

    private void sendDobissDimmer(int id, int dimmerValue) {

        // Send dobiss dimmer module for new state of dimmer

        Socket socket;
        try {
            // mutex.lock(mutex_timeout);
            DobissMutex.lock();

            socket = new Socket(ipAddress, portNumber);

            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            // Write header first
            // Dimmer type = 0x10 = 16
            byte bin[] = new byte[] { -81, 2, -1, (byte) address, 0, 0, 8, 1, 8, -1, -1, -1, -1, -1, -1, -81 };
            byte bout[] = new byte[1024];
            os.write(bin);
            logger.debug("Dobiss dimmer header send {}", Hex.encodeHexString(bin));

            is.read(bout, 0, 32);
            logger.debug("Dobiss dimmer header answer {}", Hex.encodeHexString(bout));

            byte bin2[] = new byte[] { (byte) address, (byte) (id - 1), 1, -1, -1, (byte) dimmerValue, -1, -1 };
            byte bout2[] = new byte[1024];
            os.write(bin2);
            logger.debug("Dobiss dimmer command send {}", Hex.encodeHexString(bin2));

            is.read(bout2, 0, 64);
            logger.debug("Dobiss dimmer command answer {}", Hex.encodeHexString(bout2));

            os.close();
            is.close();
            socket.close();

            switch (id) {
                case 1:
                    id01 = dimmerValue;
                    break;
                case 2:
                    id02 = dimmerValue;
                    break;
                case 3:
                    id03 = dimmerValue;
                    break;
                case 4:
                    id04 = dimmerValue;
                    break;
                default:
                    logger.debug("Wrong internal id for Dobiss dimmer.");

            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimmer unit");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimer unit");

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimer unit");

        } finally {
            // mutex.unlock();
            DobissMutex.unlock();
        }

    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {

        logger.debug("Initializing Dobbis dimmer");

        config = getConfigAs(dobissDimmerConfiguration.class);

        // Check ip address
        if (StringUtils.isBlank(config.ipAddress)) {
            logger.debug("DobissDimmerHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss dimmer config. IP address is blank.");
            return;
        }

        ipAddress = config.ipAddress;

        // Check polling interval
        if (config.pollingInterval < 1) {
            logger.debug("DobissDimmerHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss dimmer config. Polling interval is smaller than 1.");
            return;
        }
        pollingInterval = config.pollingInterval;

        // Check address
        if (config.address < 1) {
            logger.debug("DobissDimmerHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss dimmer config. Address is smaller than 1.");
            return;
        }
        address = config.address;

        // Check if dobiss dimmer module can be reached
        Socket socket;
        try {
            // mutex.lock(mutex_timeout);
            DobissMutex.lock();

            socket = new Socket(ipAddress, portNumber);

            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            // Write query of status of the dimmer
            byte bin[] = new byte[] { -81, 1, -1, (byte) address, 0, 0, 0, 1, 0, -1, -1, -1, -1, -1, -1, -81 };
            byte bout[] = new byte[1024];
            os.write(bin);
            logger.debug("Dobiss dimmer query send {}", Hex.encodeHexString(bin));

            is.read(bout);
            logger.debug("Dobiss dimmer query answer {}", Hex.encodeHexString(bout));

            updateStatus(ThingStatus.ONLINE);

            id01 = bout[32];
            logger.debug("Dobiss dimmer query answer for id01 {}", bout[32]);
            id02 = bout[33];
            logger.debug("Dobiss dimmer query answer for id01 {}", bout[32]);
            id03 = bout[34];
            logger.debug("Dobiss dimmer query answer for id01 {}", bout[32]);
            id04 = bout[35];
            logger.debug("Dobiss dimmer query answer for id01 {}", bout[32]);

            os.close();
            is.close();
            socket.close();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimmer unit");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimmer unit");

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimmer unit");
        } finally {
            // mutex.unlock();
            DobissMutex.unlock();
        }

        // Starting UI pushing
        startAutomaticRefresh();

    }

    private void queryDobissDimmer() {

        // Query dobiss dimmer module for status of the individual dimmers
        Socket socket;
        try {
            // mutex.lock(mutex_timeout);
            DobissMutex.lock();

            socket = new Socket(ipAddress, portNumber);

            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            // Write query of status of the dimmer
            byte bin[] = new byte[] { -81, 1, -1, (byte) address, 0, 0, 0, 1, 0, -1, -1, -1, -1, -1, -1, -81 };
            byte bout[] = new byte[1024];
            os.write(bin);
            logger.debug("Dobiss dimmer query send {}", Hex.encodeHexString(bin));

            is.read(bout, 0, 64);
            logger.debug("Dobiss dimmer query answer {}", Hex.encodeHexString(bout));

            id01 = bout[32];
            logger.debug("Dobiss dimmer query answer for id01 {}", bout[32]);
            id02 = bout[33];
            logger.debug("Dobiss dimmer query answer for id02 {}", bout[33]);
            id03 = bout[34];
            logger.debug("Dobiss dimmer query answer for id03 {}", bout[34]);
            id04 = bout[35];
            logger.debug("Dobiss dimmer query answer for id04 {}", bout[35]);

            os.close();
            is.close();
            socket.close();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimmer unit");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimmer unit");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss dimmer unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss dimmer unit");
        } finally {
            // mutex.unlock();
            DobissMutex.unlock();
        }
    }

    private void publishValue(ChannelUID channelUID) {
        logger.debug("Publishing value...");

        String channelID = channelUID.getId();
        switch (channelID) {
            case CHANNEL_DOBISS_DIMMER_ID_1:
                updateState(CHANNEL_DOBISS_DIMMER_ID_1, PercentType.valueOf(Integer.toString(id01)));
                logger.debug("Update status for id01 {}", id01);
                break;
            case CHANNEL_DOBISS_DIMMER_ID_2:
                updateState(CHANNEL_DOBISS_DIMMER_ID_2, PercentType.valueOf(Integer.toString(id02)));
                logger.debug("Update status for id02 {}", id02);
                break;
            case CHANNEL_DOBISS_DIMMER_ID_3:
                updateState(CHANNEL_DOBISS_DIMMER_ID_3, PercentType.valueOf(Integer.toString(id03)));
                logger.debug("Update status for id03 {}", id03);
                break;
            case CHANNEL_DOBISS_DIMMER_ID_4:
                updateState(CHANNEL_DOBISS_DIMMER_ID_4, PercentType.valueOf(Integer.toString(id04)));
                logger.debug("Update status for id04 {}", id04);
                break;
            default:
                logger.debug("Can not update channel with ID : {} - channel name might be wrong!", channelID);
                break;
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

            queryDobissDimmer();

            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                if (isLinked(channel.getUID().getId())) {
                    publishValue(channel.getUID());
                }
            }

        }

    }
}
