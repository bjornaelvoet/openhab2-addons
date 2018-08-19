package org.openhab.binding.dobiss.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DobissRelay {

    private final Logger logger = LoggerFactory.getLogger(DobissHandler.class);

    // Relays so value either 0 or 1
    public int id01;
    public int id02;
    public int id03;
    public int id04;
    public int id05;
    public int id06;
    public int id07;
    public int id08;
    public int id09;
    public int id10;
    public int id11;
    public int id12;

    // Dobiss ip address where the module can be reached
    private String ipAddress;

    // Dobiss module address as defined by the Dobiss software
    private int moduleAddress;

    // TCP port number to connect
    private int portNumber = 1001;

    // TCP socket timeout
    private int socketTimeout = 200;

    public DobissRelay() {
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
        logger.debug("Dobiss relay query send {}", Hex.encodeHexString(bin));

        is.read(bout);
        logger.debug("Dobiss relay query answer {}", Hex.encodeHexString(bout));

        os.close();
        is.close();
        socket.close();

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
    }

    public void sendCommand(int id, int onOff) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, portNumber), socketTimeout);

        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream is = new DataInputStream(socket.getInputStream());
        // Write header first
        byte bin[] = new byte[] { -81, 2, 4, (byte) moduleAddress, 0, 0, 8, 1, 8, -1, -1, -1, -1, -1, -1, -81 };
        byte bout[] = new byte[128];
        os.write(bin);
        logger.debug("Dobiss relay header send {}", Hex.encodeHexString(bin));

        is.read(bout, 0, 32);
        logger.debug("Dobiss relay header answer {}", Hex.encodeHexString(bout));

        byte bin2[] = new byte[] { (byte) moduleAddress, (byte) (id - 1), (byte) onOff, -1, -1, 0x64, -1, -1 };
        byte bout2[] = new byte[1024];
        os.write(bin2);
        logger.debug("Dobiss relay command send {}", Hex.encodeHexString(bin2));

        is.read(bout2, 0, 64);
        logger.debug("Dobiss relay command answer {}", Hex.encodeHexString(bout2));

        os.close();
        is.close();
        socket.close();

        switch (id) {
            case 1:
                id01 = onOff;
                break;
            case 2:
                id02 = onOff;
                break;
            case 3:
                id03 = onOff;
                break;
            case 4:
                id04 = onOff;
                break;
            case 5:
                id05 = onOff;
                break;
            case 6:
                id06 = onOff;
                break;
            case 7:
                id07 = onOff;
                break;
            case 8:
                id08 = onOff;
                break;
            case 9:
                id09 = onOff;
                break;
            case 10:
                id10 = onOff;
                break;
            case 11:
                id11 = onOff;
                break;
            case 12:
                id12 = onOff;
                break;
            default:
                logger.debug("Wrong internal id for Dobiss relay.");

        }

    }

}
