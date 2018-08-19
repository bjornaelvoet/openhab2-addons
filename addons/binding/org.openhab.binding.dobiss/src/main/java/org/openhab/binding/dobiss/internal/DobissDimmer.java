/**
 *
 */
package org.openhab.binding.dobiss.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bjorn_aelvoet
 *
 */
public class DobissDimmer {

    private final Logger logger = LoggerFactory.getLogger(DobissHandler.class);

    // Dimmers so value between 0 and 100
    public int id01;
    public int id02;
    public int id03;
    public int id04;

    // Dobiss ip address where the module can be reached
    private String ipAddress;

    // Dobiss module address as defined by the Dobiss software
    private int moduleAddress;

    // TCP port number to connect
    private int portNumber = 1001;

    // TCP socket timeout
    private int socketTimeout = 200;

    public DobissDimmer() {
        ipAddress = "Not defined";
        moduleAddress = -1;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String address) {
        this.ipAddress = address;
    }

    public int getModuleAddress() {
        return moduleAddress;
    }

    public void setModuleAddress(int address) {
        this.moduleAddress = address;
    }

    public void updateStatus() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, portNumber), socketTimeout);

        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream is = new DataInputStream(socket.getInputStream());

        // Write query of status of the relay
        byte bin[] = new byte[] { -81, 1, -1, (byte) moduleAddress, 0, 0, 0, 1, 0, -1, -1, -1, -1, -1, -1, -81 };
        byte bout[] = new byte[128];
        os.write(bin);
        logger.debug("Dobiss dimmer query send {}", Hex.encodeHexString(bin));

        is.read(bout);
        logger.debug("Dobiss dimmer query answer {}", Hex.encodeHexString(bout));

        os.close();
        is.close();
        socket.close();

        id01 = bout[32];
        id02 = bout[33];
        id03 = bout[34];
        id04 = bout[35];
    }

    public void sendCommand(int id, int dimmerValue) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, portNumber), socketTimeout);

        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream is = new DataInputStream(socket.getInputStream());
        // Write header first
        // Dimmer type = 0x10 = 16
        byte bin[] = new byte[] { -81, 2, -1, (byte) moduleAddress, 0, 0, 8, 1, 8, -1, -1, -1, -1, -1, -1, -81 };
        byte bout[] = new byte[128];
        os.write(bin);
        logger.debug("Dobiss dimmer header send {}", Hex.encodeHexString(bin));

        is.read(bout, 0, 32);
        logger.debug("Dobiss dimmer header answer {}", Hex.encodeHexString(bout));

        byte bin2[] = new byte[] { (byte) moduleAddress, (byte) (id - 1), 1, -1, -1, (byte) dimmerValue, -1, -1 };
        byte bout2[] = new byte[128];
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

    }
}
