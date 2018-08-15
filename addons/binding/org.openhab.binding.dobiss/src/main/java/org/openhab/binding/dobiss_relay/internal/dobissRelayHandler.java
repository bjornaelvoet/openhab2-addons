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

import static org.openhab.binding.dobiss_relay.internal.dobissRelayBindingConstants.*;

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
import org.eclipse.smarthome.core.library.types.DecimalType;
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
 * The {@link dobissRelayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
@NonNullByDefault
public class dobissRelayHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(dobissRelayHandler.class);

    @Nullable
    private dobissRelayConfiguration config;

    // Timer for pushing the updates of the HCV5 to the UI
    @Nullable
    Timer timer;

    @Nullable
    private String ipAddress = null;

    private int pollingInterval;
    private int pollingDelay = 1000;
    private int address;

    private int portNumber = 1001;

    private int id01;
    private int id02;
    private int id03;
    private int id04;
    private int id05;
    private int id06;
    private int id07;
    private int id08;
    private int id09;
    private int id10;
    private int id11;
    private int id12;

    public dobissRelayHandler(Thing thing) {
        super(thing);

        this.thing = thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);

        if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_1)) {
            handleDobissRelay(command, 1);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_2)) {
            handleDobissRelay(command, 2);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_3)) {
            handleDobissRelay(command, 3);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_4)) {
            handleDobissRelay(command, 4);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_5)) {
            handleDobissRelay(command, 5);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_6)) {
            handleDobissRelay(command, 6);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_7)) {
            handleDobissRelay(command, 7);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_8)) {
            handleDobissRelay(command, 8);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_9)) {
            handleDobissRelay(command, 9);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_10)) {
            handleDobissRelay(command, 10);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_11)) {
            handleDobissRelay(command, 11);
        } else if (channelUID.getId().equals(CHANNEL_DOBISS_RELAY_ID_12)) {
            handleDobissRelay(command, 12);
        } else {
            logger.debug("Received command for {} on unknown channel {}", thing.getUID(), channelUID.getId());
        }

    }

    private void handleDobissRelay(Command command, int id) {
        logger.debug("Handling Dobiss relay command for {}: {}", thing.getUID(), command);

        // TODO Again do tcp communication to have actual refresh

        if (command instanceof RefreshType) {
            switch (id) {
                case 1:
                    updateState(CHANNEL_DOBISS_RELAY_ID_1, DecimalType.valueOf(Integer.toString(id01)));
                    break;
                case 2:
                    updateState(CHANNEL_DOBISS_RELAY_ID_2, DecimalType.valueOf(Integer.toString(id02)));
                    break;
                case 3:
                    updateState(CHANNEL_DOBISS_RELAY_ID_3, DecimalType.valueOf(Integer.toString(id03)));
                    break;
                case 4:
                    updateState(CHANNEL_DOBISS_RELAY_ID_4, DecimalType.valueOf(Integer.toString(id04)));
                    break;
                case 5:
                    updateState(CHANNEL_DOBISS_RELAY_ID_5, DecimalType.valueOf(Integer.toString(id05)));
                    break;
                case 6:
                    updateState(CHANNEL_DOBISS_RELAY_ID_6, DecimalType.valueOf(Integer.toString(id06)));
                    break;
                case 7:
                    updateState(CHANNEL_DOBISS_RELAY_ID_7, DecimalType.valueOf(Integer.toString(id07)));
                    break;
                case 8:
                    updateState(CHANNEL_DOBISS_RELAY_ID_8, DecimalType.valueOf(Integer.toString(id08)));
                    break;
                case 9:
                    updateState(CHANNEL_DOBISS_RELAY_ID_9, DecimalType.valueOf(Integer.toString(id09)));
                    break;
                case 10:
                    updateState(CHANNEL_DOBISS_RELAY_ID_10, DecimalType.valueOf(Integer.toString(id10)));
                    break;
                case 11:
                    updateState(CHANNEL_DOBISS_RELAY_ID_11, DecimalType.valueOf(Integer.toString(id11)));
                    break;
                case 12:
                    updateState(CHANNEL_DOBISS_RELAY_ID_12, DecimalType.valueOf(Integer.toString(id12)));
                    break;
                default:
                    logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }

        } else {
            // Command must be either ON or OFF
            if (command.toString().equals("OFF")) {
                sendDobissRelay(id, 0);
            } else if (command.toString().equals("ON")) {
                sendDobissRelay(id, 1);
            } else {
                logger.debug("Command {} not implemented for thing {}", command, thing.getUID());
            }
        }
    }

    private void sendDobissRelay(int id, int OnOff) {

        // Send dobiss relay module for new state of relay
        Socket socket;
        try {
            socket = new Socket(ipAddress, portNumber);

            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            // Write header first
            byte bin[] = new byte[] { -81, 2, 4, (byte) address, 0, 0, 8, 1, 8, -1, -1, -1, -1, -1, -1, -81 };
            byte bout[] = new byte[1024];
            os.write(bin);
            logger.info("Dobiss relay header send {}", Hex.encodeHexString(bin));

            is.read(bout, 0, 32);
            logger.info("Dobiss relay header answer {}", Hex.encodeHexString(bout));

            byte bin2[] = new byte[] { (byte) address, (byte) (id - 1), (byte) OnOff, -1, -1, 0x64, -1, -1 };
            byte bout2[] = new byte[1024];
            os.write(bin2);
            logger.info("Dobiss relay command send {}", Hex.encodeHexString(bin2));

            is.read(bout2, 0, 64);
            logger.info("Dobiss relay command answer {}", Hex.encodeHexString(bout2));

            os.close();
            is.close();
            socket.close();

            switch (id) {
                case 1:
                    id01 = OnOff;
                    break;
                case 2:
                    id02 = OnOff;
                    break;
                case 3:
                    id03 = OnOff;
                    break;
                case 4:
                    id04 = OnOff;
                    break;
                case 5:
                    id05 = OnOff;
                    break;
                case 6:
                    id06 = OnOff;
                    break;
                case 7:
                    id07 = OnOff;
                    break;
                case 8:
                    id08 = OnOff;
                    break;
                case 9:
                    id09 = OnOff;
                    break;
                case 10:
                    id10 = OnOff;
                    break;
                case 11:
                    id11 = OnOff;
                    break;
                case 12:
                    id12 = OnOff;
                    break;
                default:
                    logger.debug("Wrong internal id for Dobiss relay.");

            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss relay unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss relay unit");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss relay unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss relay unit");

        }

    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {

        logger.debug("Initializing Dobbis relay");

        config = getConfigAs(dobissRelayConfiguration.class);

        // Check ip address
        if (StringUtils.isBlank(config.ipAddress)) {
            logger.debug("DobissRelayHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss relay config. IP address is blank.");
            return;
        }

        ipAddress = config.ipAddress;

        // Check polling interval
        if (config.pollingInterval < 1) {
            logger.debug("DobissRelayHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss relay config. Polling interval is smaller than 1.");
            return;
        }
        pollingInterval = config.pollingInterval;

        // Check address
        if (config.address < 1) {
            logger.debug("DobissRelayHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Dobiss relay config. Address is smaller than 1.");
            return;
        }
        address = config.address;

        // Check if dobiss relay module can be reached
        Socket socket;
        try {
            socket = new Socket(ipAddress, portNumber);

            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            // Write query of status of the relay
            byte bin[] = new byte[] { -81, 1, -1, (byte) address, 0, 0, 0, 1, 0, -1, -1, -1, -1, -1, -1, -81 };
            byte bout[] = new byte[1024];
            os.write(bin);
            logger.debug("Dobiss relay query send {}", Hex.encodeHexString(bin));

            is.read(bout);
            logger.debug("Dobiss relay query answer {}", Hex.encodeHexString(bout));

            updateStatus(ThingStatus.ONLINE);

            id01 = bout[32];
            id02 = bout[33];
            id03 = bout[34];
            id04 = bout[35];
            id05 = bout[36];
            id06 = bout[37];
            id07 = bout[38];
            id08 = bout[39];
            id09 = bout[40];
            id10 = bout[41];
            id11 = bout[42];
            id12 = bout[43];

            os.close();
            is.close();
            socket.close();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss relay unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss relay unit");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss relay unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss relay unit");

        }

        // Starting UI pushing
        startAutomaticRefresh();
    }

    private void queryDobissRelay() {

        // Query dobiss relay module for status of the individual relays
        Socket socket;
        try {
            socket = new Socket(ipAddress, portNumber);

            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            // Write query of status of the relay
            byte bin[] = new byte[] { -81, 1, -1, (byte) address, 0, 0, 0, 1, 0, -1, -1, -1, -1, -1, -1, -81 };
            byte bout[] = new byte[1024];
            os.write(bin);
            // logger.info("Dobiss relay query send {}", Hex.encodeHexString(bin));

            is.read(bout, 0, 64);
            // logger.info("Dobiss relay query answer {}", Hex.encodeHexString(bout));

            id01 = bout[32];
            logger.info("Dobiss relay query answer for id01 {}", bout[32]);
            id02 = bout[33];
            logger.info("Dobiss relay query answer for id02 {}", bout[33]);
            id03 = bout[34];
            logger.info("Dobiss relay query answer for id03 {}", bout[34]);
            id04 = bout[35];
            logger.info("Dobiss relay query answer for id04 {}", bout[35]);
            id05 = bout[36];
            logger.info("Dobiss relay query answer for id05 {}", bout[36]);
            id06 = bout[37];
            logger.info("Dobiss relay query answer for id06 {}", bout[37]);
            id07 = bout[38];
            logger.info("Dobiss relay query answer for id07 {}", bout[38]);
            id08 = bout[39];
            logger.info("Dobiss relay query answer for id08 {}", bout[39]);
            id09 = bout[40];
            logger.info("Dobiss relay query answer for id09 {}", bout[40]);
            id10 = bout[41];
            logger.info("Dobiss relay query answer for id10 {}", bout[41]);
            id11 = bout[42];
            logger.info("Dobiss relay query answer for id11 {}", bout[42]);
            id12 = bout[43];
            logger.info("Dobiss relay query answer for id12 {}", bout[43]);

            os.close();
            is.close();
            socket.close();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss relay unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss relay unit");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            logger.debug("Communication to Dobiss relay unit failed!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to communicate with Dobiss relay unit");

        }

    }

    private void publishValue(ChannelUID channelUID) {
        logger.debug("Publishing value...");

        String channelID = channelUID.getId();
        switch (channelID) {
            case CHANNEL_DOBISS_RELAY_ID_1:
                updateState(CHANNEL_DOBISS_RELAY_ID_1, DecimalType.valueOf(Integer.toString(id01)));
                logger.info("Update status for id01 {}", id01);
                break;
            case CHANNEL_DOBISS_RELAY_ID_2:
                updateState(CHANNEL_DOBISS_RELAY_ID_2, DecimalType.valueOf(Integer.toString(id02)));
                logger.info("Update status for id02 {}", id02);
                break;
            case CHANNEL_DOBISS_RELAY_ID_3:
                updateState(CHANNEL_DOBISS_RELAY_ID_3, DecimalType.valueOf(Integer.toString(id03)));
                logger.info("Update status for id03 {}", id03);
                break;
            case CHANNEL_DOBISS_RELAY_ID_4:
                updateState(CHANNEL_DOBISS_RELAY_ID_4, DecimalType.valueOf(Integer.toString(id04)));
                logger.info("Update status for id04 {}", id04);
                break;
            case CHANNEL_DOBISS_RELAY_ID_5:
                updateState(CHANNEL_DOBISS_RELAY_ID_5, DecimalType.valueOf(Integer.toString(id05)));
                logger.info("Update status for id05 {}", id05);
                break;
            case CHANNEL_DOBISS_RELAY_ID_6:
                updateState(CHANNEL_DOBISS_RELAY_ID_6, DecimalType.valueOf(Integer.toString(id06)));
                logger.info("Update status for id06 {}", id06);
                break;
            case CHANNEL_DOBISS_RELAY_ID_7:
                updateState(CHANNEL_DOBISS_RELAY_ID_7, DecimalType.valueOf(Integer.toString(id07)));
                logger.info("Update status for id07 {}", id07);
                break;
            case CHANNEL_DOBISS_RELAY_ID_8:
                updateState(CHANNEL_DOBISS_RELAY_ID_8, DecimalType.valueOf(Integer.toString(id08)));
                logger.info("Update status for id08 {}", id08);
                break;
            case CHANNEL_DOBISS_RELAY_ID_9:
                updateState(CHANNEL_DOBISS_RELAY_ID_9, DecimalType.valueOf(Integer.toString(id09)));
                logger.info("Update status for id09 {}", id09);
                break;
            case CHANNEL_DOBISS_RELAY_ID_10:
                updateState(CHANNEL_DOBISS_RELAY_ID_10, DecimalType.valueOf(Integer.toString(id10)));
                logger.info("Update status for id10 {}", id10);
                break;
            case CHANNEL_DOBISS_RELAY_ID_11:
                updateState(CHANNEL_DOBISS_RELAY_ID_11, DecimalType.valueOf(Integer.toString(id11)));
                logger.info("Update status for id11 {}", id11);
                break;
            case CHANNEL_DOBISS_RELAY_ID_12:
                updateState(CHANNEL_DOBISS_RELAY_ID_12, DecimalType.valueOf(Integer.toString(id12)));
                logger.info("Update status for id12 {}", id12);
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

            queryDobissRelay();

            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                if (isLinked(channel.getUID().getId())) {
                    publishValue(channel.getUID());
                }
            }

        }

    }

}
