package machine.microspin.com.drawframe_console.entity;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Settings Repo for storing storing throughout the application lifetime
 * Delivery speed m/min	0x30	float	(50-500)
 * Draft	0x31	float	(0.0-9.9)
 * Length limit	0x32	int	(100-1000)
 * All DrawFrame Params	0x40
 */

public class Settings {

    public static BluetoothDevice device;
    //===== Settings Parameters =======

    //-- DrawFrame Settings parameters
    private static float deliverySpeed;
    private static float draft;
    private static int lengthLimit;

    private static String machineId = "01"; //Default;
    //==== END: Settings Parameters ====

    final private static String ATTR_COUNT = "01";
    final private static String ATTR_MACHINE_TYPE = Utility.ReverseLookUp(Pattern.machineTypeMap, Pattern.MachineType.DRAW_FRAME.name());
    final private static String ATTR_MSG_TYPE_BACKGROUND = Utility.ReverseLookUp(Pattern.messageTypeMap, Pattern.MessageType.BACKGROUND_DATA.name());
    final private static String ATTR_SCREEN_SUB_STATE_NONE = "00";
    final private static String ATTR_TYPE_DRAWFRAME_PATTERN = "40"; //==>Hex value conversion check
    final private static String ATTR_SCREEN_SETTING = Utility.ReverseLookUp(Pattern.screenMap, Pattern.Screen.SETTING.name());
    final private static String ATTR_PACKET_LENGTH = "13"; // HexCode
    final private static int ATTR_LENGTH = 24;

//Default settings for factory settings
    private static float defaultdeliverySpeed = 120;
    private static float defaultdraft = 8.8f;
    private static int defaultLengthLimit = 1000;

 //====Settings for REQUEST PID =============//
    //only those different from the setting s above is  defined
    final private static String PID_ATTR_PACKET_LENGTH = "0B";  // IN HEX
    final private static String PID_ATTR_PID_PATTERN = "06";
    final private static String PID_REQUEST_ATTR_CODE= "01";
    final private static String PID_NEWSETTINGS_ATTR_CODE= "02";
    final private static int PID_ATTR_LENGTH = 16;

    // these are all the pid request msgs that you can get from the machine
    public static int pid_req_attr1 = 0;
    public static int pid_req_attr2 = 0;
    public static int pid_req_attr3 = 0;

    public static String getMachineId (){
        return machineId;
    }
    public static Boolean processSettingsPacket(String payload) {
        if (payload.length() < 4) {
            return false;
        }

        String SOF = payload.substring(0, 2);
        int payloadLength = payload.length();
        String EOF = payload.substring(payloadLength - 2, payloadLength);
        if (!SOF.equals(Packet.START_IDENTIFIER) || !EOF.equals(Packet.END_IDENTIFIER)) {
            return false;
        }

        String sender = payload.substring(2, 4);
        if (!sender.equals(Packet.SENDER_MACHINE)) {
            return false;
        }

        machineId = payload.substring(6, 8);

        // Mapping Setting Parameters.
        deliverySpeed = Utility.convertHexToFloat(payload.substring(22, 30));
        draft = Utility.convertHexToFloat(payload.substring(30, 38));
        lengthLimit = Utility.convertHexToInt(payload.substring(38, 42));

        return true;
    }

    public static String updateNewSetting(String s1, String s2, String s3) {
        // Update new values in Repo.
        deliverySpeed = Float.parseFloat(s1);
        draft = Float.parseFloat(s2);
        lengthLimit = Integer.parseInt(s3);

        // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;

        String sender = Packet.SENDER_HMI;

        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();

        attrPayload.append(ATTR_TYPE_DRAWFRAME_PATTERN).
                append(String.format("%02d", ATTR_LENGTH));

        String attr = Utility.convertFloatToHex(deliverySpeed);
        attrPayload.append(Utility.formatValueByPadding(attr, 4));
        attr = Utility.convertFloatToHex(draft);
        attrPayload.append(Utility.formatValueByPadding(attr, 4));
        attr = Utility.convertIntToHexString(lengthLimit);
        attrPayload.append(Utility.formatValueByPadding(attr, 2));

        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(ATTR_SCREEN_SETTING).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    public static String updateNewPIDSetting(String motorIndex,int s1, int s2, int s3) {

        // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;
        String sender = Packet.SENDER_HMI;

        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();

        attrPayload.append(PID_NEWSETTINGS_ATTR_CODE).
                append(String.format("%02d", PID_ATTR_LENGTH));


        attrPayload.append(Utility.formatValueByPadding(motorIndex,2));
        String attr = Utility.convertIntToHexString(s1);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertIntToHexString(s2);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertIntToHexString(s3);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(PID_ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(PID_ATTR_PID_PATTERN).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    //==========GETTERS============
    public static float getDeliverySpeed() {
        return deliverySpeed;
    }

    public static String getDeliverySpeedString() {
        Float f = deliverySpeed;
        String s = f.toString();
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static float getDraft() {
        return draft;
    }

    public static String getDraftString() {
        String s = String.format("%f", draft);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static int getLengthLimit() {
        return lengthLimit;
    }

    public static String getLengthLimitString() {
        return String.format("%d", lengthLimit);
    }


    //get default settings
    public static String getDefaultDeliverySpeedString() {
        Float f = defaultdeliverySpeed;
        String s = f.toString();
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultDraftString() {
        String s = String.format("%f", defaultdraft);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultLengthLimitString() {
        return String.format("%d", defaultLengthLimit);
    }

/********************PID SETTINGS STUFF ***********************/

    public static String RequestPIDSettings(String motorValue) {

       // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;

        String sender = Packet.SENDER_HMI;

        //the TLV
        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();
        attrPayload.append(PID_REQUEST_ATTR_CODE).
                append( Pattern.ATTR_LENGTH_02);
        attrPayload.append(motorValue);

        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(PID_ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(PID_ATTR_PID_PATTERN).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    public static Boolean processPIDSettingsPacket(String payload) {

        int motorID ;
        int attr1;
        int attr2;
        int attr3;

        if (payload.length() < 4) {
            return false;
        }

        String SOF = payload.substring(0, 2);
        int payloadLength = payload.length();
        String EOF = payload.substring(payloadLength - 2, payloadLength);
        if (!SOF.equals(Packet.START_IDENTIFIER) || !EOF.equals(Packet.END_IDENTIFIER)) {
            return false;
        }

        String sender = payload.substring(2, 4);
        if (!sender.equals(Packet.SENDER_MACHINE)) {
            return false;
        }

        machineId = payload.substring(6, 8);

        // Mapping Setting Parameters.
        motorID = Utility.convertHexToInt(payload.substring(22, 26));
        //float fl = Utility.convertHexToFloat(payload.substring(22, 26));
        pid_req_attr1 = Utility.convertHexToInt(payload.substring(26, 30));
        pid_req_attr2 = Utility.convertHexToInt(payload.substring(30, 34));
        pid_req_attr3 = Utility.convertHexToInt(payload.substring(34, 38));

        return true;
    }

    public static String MakeIntString(int input) {
        return String.format("%d", input);
    }

    public static String MakeFloatString(float input,int decimals) {
        String s = String.format("%."+ String.valueOf(decimals) + "f", input);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


}

