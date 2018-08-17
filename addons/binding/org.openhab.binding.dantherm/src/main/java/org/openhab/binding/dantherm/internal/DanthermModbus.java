/**
 *
 */
package org.openhab.binding.dantherm.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import de.re.easymodbus.modbusclient.ModbusClient.RegisterOrder;

/**
 * @author bjorn_aelvoet
 *
 */
public class DanthermModbus {

    private final Logger logger = LoggerFactory.getLogger(DanthermHandler.class);

    private InetAddress ipAddress;

    private int pollingDelay = 1000;

    // Timer for querying the HCV5 device at polling interval
    private Timer timer;

    // All information that HCV5 is exposing is stored here; conversion already happened to logical units (no int
    // registers)
    public boolean DHCPEN;
    public InetAddress currentIPAddress;
    public InetAddress currentIPMask;
    public InetAddress currentIPGateway;
    public String MACAddr;

    public long systemSerialNumber;

    public enum SwitchPosition {
        SWITCH_A_ENABLED,
        SWITCH_B_ENABLED,
        SWITCH_UNKNOWN;

        public static SwitchPosition fromInteger(int left, int right) {
            if ((left == 0) && (right == 1)) {
                return SWITCH_A_ENABLED;
            } else if ((left == 1) && (right == 0)) {
                return SwitchPosition.SWITCH_B_ENABLED;
            } else {
                return SwitchPosition.SWITCH_UNKNOWN;
            }
        }
    }

    public SwitchPosition switchPosition;

    public int speedLevelFan;

    public int fan1rpm;
    public int fan2rpm;

    public float temperatureOutdoor;
    public float temperatureSupply;
    public float temperatureExtract;
    public float temperatureExhaust;
    // The precision of the temperature we want to report out
    private int temperaturePrecision = 1;
    // The treshold (difference in kept temperature and new temperature) before we are going to update the kept
    // temperature
    private float temperatureTreshold = (float) 0.25;

    public int currentUnitMode;
    public int activeUnitMode;

    // Internal const values used for communication with HCV5
    private static final int TCP_PORT = 502;

    // Serial number registers
    private static final int prmSystemSerialNumLow = 5;

    // Switch position A/B (is fan1/fan2 for inlet/exhaust or vice versa)
    // If left is one => postion A => fan1 is extract and fan 2 is supply
    // If right is one => position B => fan1 is supply and fan2 is extract
    private static final int prmHALLeft = 85;
    private static final int prmHALRight = 87;

    // Outdoor temperature (air going into building before exchange heater)
    private static final int prmRamIdxT1 = 133;
    // Supply temperature (air going into building after exchange heater)
    private static final int prmRamIdxT2 = 135;
    // Extract temperature (air going outside of building before exchange heater)
    private static final int prmRamIdxT3 = 137;
    // Exhaust temperature (air going outside of building after exchange heater )
    private static final int prmRamIdxT4 = 139;

    // Speed level of fans (0 to 4); in manual mode this can be set; in other modes read only
    private static final int prmRomIdxSpeedLevel = 324;
    // Fan1 RPM
    private static final int prmHALTaho1 = 101;
    // Fan2 RPM
    private static final int prmHALTaho2 = 103;

    // Current unit mode (read-only)
    private static final int prmCurrentBLState = 472;

    // Active unit mode (writable)
    private static final int prmRamIdxUnitMode = 168;

    public DanthermModbus(InetAddress ipAddress, int pollingInterval) throws Exception {

        this.ipAddress = ipAddress;

        logger.debug("ip address = {} and polling interval = {}", ipAddress.toString(), pollingInterval);

        // Check the connection; only if successfully, start timer
        try {
            checkValidConnnection();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.debug("Unable to connect with HCV5 device at address {}", ipAddress.getHostAddress());

            throw e;
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new PeriodicTask(), pollingDelay, pollingInterval * 1000);

        return;

    }

    public void dispose() {
        // Currently nothing to dispose when releasing this object

        timer.cancel();
        timer.purge();

        return;
    }

    private void checkValidConnnection() throws Exception {

        // Checks if we can query some basic information with the given ip address
        // We read the read-only information here that is of interest
        int[] holdingRegister;

        int value3;
        int value4;
        int value5;
        int value6;

        ModbusClient modbusClient = new ModbusClient(ipAddress.getHostAddress(), TCP_PORT);

        modbusClient.Connect();

        // Read out system serial number low and high; if successful we have valid connection
        holdingRegister = modbusClient.ReadHoldingRegisters(prmSystemSerialNumLow, 4);
        logger.debug("System serial number register values {}, {}, {} and {} read out.",
                holdingRegister[0] & 0x0000FFFF, holdingRegister[1] & 0x0000FFFF, holdingRegister[2] & 0x0000FFFF,
                holdingRegister[3] & 0x0000FFFF);
        // value = ModbusClient.ConvertRegistersToLong(holdingRegister, RegisterOrder.HighLow);
        // logger.debug("System serial number value {}", value);
        // systemSerialNumber = value;

        // Read switch A/B
        holdingRegister = modbusClient.ReadHoldingRegisters(prmHALLeft, 2);
        // value = ModbusClient.ConvertRegistersToLong(holdingRegister, RegisterOrder.HighLow);
        value3 = holdingRegister[0] & 0x0000FFFF;
        // holdingRegister = modbusClient.ReadHoldingRegisters(prmHALLeft + 1, 1);
        value4 = holdingRegister[1] & 0x0000FFFF;
        logger.debug("Switch position A {} {}", value3, value4);
        holdingRegister = modbusClient.ReadHoldingRegisters(prmHALRight, 2);
        value5 = holdingRegister[0] & 0x0000FFFF;
        // holdingRegister = modbusClient.ReadHoldingRegisters(prmHALLeft + 3, 1);
        value6 = holdingRegister[1] & 0x0000FFFF;
        logger.debug("Switch position B {} {}", value5, value6);

        modbusClient.Disconnect();

        return;
    }

    public void setFanSpeed(int value) {
        int[] holdingRegister = new int[2];

        holdingRegister[0] = value;
        holdingRegister[1] = 0;

        logger.debug("Writing fan speed {}", value);

        try {
            ModbusClient modbusClient = new ModbusClient(ipAddress.getHostAddress(), TCP_PORT);
            modbusClient.Connect();
            modbusClient.WriteMultipleRegisters(prmRomIdxSpeedLevel, holdingRegister);
            modbusClient.Disconnect();

            logger.debug("Fan speed successfully written");

            // This will send update back
            speedLevelFan = value;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (ModbusException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    public void setActiveUnitMode(int value) {
        int[] holdingRegister = new int[2];

        holdingRegister[0] = value;
        holdingRegister[1] = 0;

        logger.debug("Writing active unit mode {}", value);

        try {
            ModbusClient modbusClient = new ModbusClient(ipAddress.getHostAddress(), TCP_PORT);
            modbusClient.Connect();
            modbusClient.WriteMultipleRegisters(prmRamIdxUnitMode, holdingRegister);
            modbusClient.Disconnect();

            logger.debug("Active unit mode {} successfully written", value);

            // This will send update back
            activeUnitMode = value;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (ModbusException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private int convertHCV5IntsToInt(int InLow, int InHigh) {
        // Register read in for HCV5 is 16 bit per register; low first (lower address) then high (higher address)
        return ((InHigh & 0xFFFF0000) | (InLow & 0x0000FFFF));
    }

    class PeriodicTask extends TimerTask {

        @Override
        public void run() {
            // This function is executed periodically (pollingInterval) and is fetching all information from HCV5.
            // It opens a TCP connection; queries all information and closes TCP connection again.
            // Implementation is like this because HCV5 device is closing connections automatically if socket is unused
            // for
            // 1 minute.

            try {
                int[] holdingRegister;

                float newTemperatureOutdoor;
                float newTemperatureSupply;
                float newTemperatureExtract;
                float newTemperatureExhaust;

                int precision = temperaturePrecision;
                int scale = (int) Math.pow(10, precision);

                ModbusClient modbusClient = new ModbusClient(ipAddress.getHostAddress(), TCP_PORT);
                modbusClient.Connect();

                // Read out speed level of fans
                holdingRegister = modbusClient.ReadHoldingRegisters(prmRomIdxSpeedLevel, 2);
                logger.debug("Fan speed level register values {} and {} read out.", holdingRegister[0],
                        holdingRegister[1]);
                speedLevelFan = convertHCV5IntsToInt(holdingRegister[0], holdingRegister[1]);
                logger.debug("Fan speed level value {}", speedLevelFan);

                // Read out of current unit mode
                holdingRegister = modbusClient.ReadHoldingRegisters(prmCurrentBLState, 2);
                logger.debug("Current unit mode register values {} and {} read out.", holdingRegister[0],
                        holdingRegister[1]);
                currentUnitMode = convertHCV5IntsToInt(holdingRegister[0], holdingRegister[1]);
                logger.debug("Fan speed level value {}", speedLevelFan);

                // Read out of active unit mode
                holdingRegister = modbusClient.ReadHoldingRegisters(prmRamIdxUnitMode, 2);
                logger.debug("Active unit mode register values {} and {} read out.", holdingRegister[0],
                        holdingRegister[1]);
                activeUnitMode = convertHCV5IntsToInt(holdingRegister[0], holdingRegister[1]);
                logger.debug("Fan speed level value {}", speedLevelFan);

                // Read out fan1 rpm
                holdingRegister = modbusClient.ReadHoldingRegisters(prmHALTaho1, 2);
                logger.debug("Fan 1 rpm register values {} and {} read out.", holdingRegister[0], holdingRegister[1]);
                fan1rpm = convertHCV5IntsToInt(holdingRegister[0], holdingRegister[1]);
                logger.debug("Fan 1 rpm value {}", fan1rpm);

                // Read out fan2 rpm
                holdingRegister = modbusClient.ReadHoldingRegisters(prmHALTaho2, 2);
                logger.debug("Fan 2 rpm register values {} and {} read out.", holdingRegister[0], holdingRegister[1]);
                fan2rpm = convertHCV5IntsToInt(holdingRegister[0], holdingRegister[1]);
                logger.debug("Fan 2 rpm value {}", fan2rpm);

                // Read out outdoor temperature
                holdingRegister = modbusClient.ReadHoldingRegisters(prmRamIdxT1, 2);
                logger.debug("Outdoor temperature register values {} and {} read out.", holdingRegister[0],
                        holdingRegister[1]);
                newTemperatureOutdoor = ModbusClient.ConvertRegistersToFloat(holdingRegister, RegisterOrder.HighLow);
                logger.debug("New temperature outdoor = {}", newTemperatureOutdoor);
                logger.debug("Current temperature outdoor = {}", temperatureOutdoor);
                if (Math.abs(newTemperatureOutdoor - temperatureOutdoor) > temperatureTreshold) {
                    temperatureOutdoor = (float) (Math.round(newTemperatureOutdoor * scale)) / scale;
                }
                logger.debug("Outdoor temperature value {}", temperatureOutdoor);

                // Read out supply temperature
                holdingRegister = modbusClient.ReadHoldingRegisters(prmRamIdxT2, 2);
                logger.debug("Supply temperature register values {} and {} read out.", holdingRegister[0],
                        holdingRegister[1]);
                newTemperatureSupply = ModbusClient.ConvertRegistersToFloat(holdingRegister, RegisterOrder.HighLow);
                if (Math.abs(newTemperatureSupply - temperatureSupply) > temperatureTreshold) {
                    temperatureSupply = (float) (Math.round(newTemperatureSupply * scale)) / scale;
                }
                logger.debug("Supply temperature value {}", temperatureSupply);

                // Read out extract temperature
                holdingRegister = modbusClient.ReadHoldingRegisters(prmRamIdxT3, 2);
                logger.debug("Extract temperature register values {} and {} read out.", holdingRegister[0],
                        holdingRegister[1]);
                newTemperatureExtract = ModbusClient.ConvertRegistersToFloat(holdingRegister, RegisterOrder.HighLow);
                if (Math.abs(newTemperatureExtract - temperatureExtract) > temperatureTreshold) {
                    temperatureExtract = (float) (Math.round(newTemperatureExtract * scale)) / scale;
                }
                logger.debug("Extract temperature value {}", temperatureExtract);

                // Read out exhaust temperature
                holdingRegister = modbusClient.ReadHoldingRegisters(prmRamIdxT4, 2);
                logger.debug("Exhaust temperature register values {} and {} read out.", holdingRegister[0],
                        holdingRegister[1]);
                newTemperatureExhaust = ModbusClient.ConvertRegistersToFloat(holdingRegister, RegisterOrder.HighLow);
                if (Math.abs(newTemperatureExhaust - temperatureExhaust) > temperatureTreshold) {
                    temperatureExhaust = (float) (Math.round(newTemperatureExhaust * scale)) / scale;
                }
                logger.debug("Exhaust temperature value {}", temperatureExhaust);

                modbusClient.Disconnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.debug(e.getMessage());
            } catch (ModbusException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}
