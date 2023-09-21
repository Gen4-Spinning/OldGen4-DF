package machine.microspin.com.drawframe_console.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for all constants patterns for the incoming/outgoing packets
 */

public interface Pattern {

    String PID_PASSWORD = "900332"; // first 6 digits of Manis phone no

    String ATTR_LENGTH_01 = "01";
    String ATTR_LENGTH_02 = "02";
    String ATTR_LENGTH_04 = "04";
    String COMMON_NONE_PARAM = "00";

    String DISABLE_MACHINE_START_SETTINGS = "7E020B010102030001010200017E";
    String DISABLE_MACHINE_START_DIAGNOSE = "7E020B010102030001010200027E";
    String STOP_DIAG_RUN = "7E020B010102030001000200037E";     
    String ENABLE_MACHINE_START = "7E020B010102020001000200007E";

    String ENABLE_LOG = "7E020B0101020A00010002007E";
    String DISABLE_LOG = "7E020B0101020B00010002007E";
    String START_STOP_RESTART_SIGNAL = "7E020B010102020001000200007E";

    String RPM_ERROR_USERMSG = "OVERLOAD?";
    String LENGTH_REACHED_USERMSG = "CAN IS FULL";
    String LAPPING_USERMSG = "ROLLER LAPPING";
    /* INCOMING PACKET PATTERNS */
    enum MachineType {
        CARDING_MACHINE, FLYER, DRAW_FRAME, RING_FRAME
    }

    enum Screen {
        IDLE, RUN, STOP, SETTING, DIAGNOSTICS, PID_REQUEST, UPDATE_RTF
    }

    enum ScreenSubState {
        NORMAL, RAMP_UP, PIECING, PAUSE, HALT, IDLE, ACK,SAVED, DIAG_SUCCESS,DIAG_FAIL,DIAG_INCOMPLETE
    }

    enum Sender {
        MACHINE, HMI
    }

    enum MessageType {
        SCREEN_DATA, BACKGROUND_DATA
    }

    enum OperationParameter {
        CYLINDER, BEATER, PRODUCTION, PRODUCTION_SPEED
    }

    enum StopMessageType {
        REASON, MOTOR_ERROR_CODE, ERROR_VAL
    }

    enum StopReasonValue {
        USER_PRESS_PAUSE, FRONT_ROLLER, BACK_ROLLER, CREEL,SLIVER_CUT,L_REACHED,LAPPING
    }


    enum MotorErrorCode {
        RPM_ERROR, MOTOR_VOLTAGE_ERROR, DRIVER_VOLTAGE_ERROR, SIGNAL_VOLTAGE_ERROR, OVERLOAD_CURRENT_ERROR,
    }

    enum MotorTypes {
        FRONT_ROLLER, BACK_ROLLER, CREEL
    }

    enum PID_OPTIONS {
        DF_RAMP_VARS,CREEL,BACK_ROLLER,FRONT_ROLLER
    }


    /* OUTGOING PACKET PATTERNS */
    enum Information {
        PAIRED, DISABLE_MACHINE_START, SETTINGS_CHANGE_VALS, HW_CHANGE_VALS, DIAGNOSTICS
    }

    enum InformationSubType {
        NONE
    }

    enum DisableMachineStartType {
        SCREEN_ENTERED_FROM_IDLE
    }

    enum DisableMachineStartValue {
        SETTINGS_CHANGE, DIAGNOSTIC, HW_CHANGE
    }

    enum DiagnosticTestTypes {
        CLOSED_LOOP, OPEN_LOOP
    }

    enum DiagnosticAttrType {
        KIND_OF_TEST, MOTOR_ID, SIGNAL_VOLTAGE, TARGET_RPM, TEST_TIME
    }

    enum Setting {
        DELIVERY_SPEED, TENSION_DRAFT, CYLINDER_SPEED, CYLINDER_FEED, BEATER_SPEED, BEATER_FEED, CONVEYOR_SPEED, CONVEYOR_DELAY, CONVEYOR_DWELL
    }

    /* INCOMING PACKET PATTERNS MAPPING */
    Map<String, String> machineTypeMap = new HashMap<String, String>() {
        {
            put("01", MachineType.CARDING_MACHINE.name());
            put("02", MachineType.DRAW_FRAME.name());
            put("03", MachineType.FLYER.name());
            put("04", MachineType.RING_FRAME.name());
        }
    };

    Map<String, String> screenMap = new HashMap<String, String>() {
        {
            put("01", Screen.IDLE.name());
            put("02", Screen.RUN.name());
            put("03", Screen.STOP.name());
            put("04", Screen.SETTING.name());
            put("05", Screen.DIAGNOSTICS.name());
            put("06", Screen.PID_REQUEST.name());
            // Flyer parameters:
            put("07", Screen.UPDATE_RTF.name());
        }
    };

    Map<String, String> screenSubStateMap = new HashMap<String, String>() {
        {
            put("00", ScreenSubState.IDLE.name());
            put("01", ScreenSubState.NORMAL.name());
            put("02", ScreenSubState.RAMP_UP.name());
            put("03", ScreenSubState.PIECING.name());
            put("11", ScreenSubState.PAUSE.name());
            put("12", ScreenSubState.HALT.name());
            put("96", ScreenSubState.DIAG_INCOMPLETE.name());
            put("97", ScreenSubState.DIAG_FAIL.name());
            put("98", ScreenSubState.DIAG_SUCCESS.name());
            put("88", ScreenSubState.SAVED.name()); //settings saved
            put("99", ScreenSubState.ACK.name());
        }
    };

    Map<String, String> senderMap = new HashMap<String, String>() {
        {
            put("01", Sender.MACHINE.name());
            put("02", Sender.HMI.name());
        }
    };

    Map<String, String> messageTypeMap = new HashMap<String, String>() {
        {
            put("01", MessageType.SCREEN_DATA.name());
            put("02", MessageType.BACKGROUND_DATA.name());
        }
    };

    Map<String, String> operationParameterMap = new HashMap<String, String>() {
        {
            put("00", "");
            put("01", OperationParameter.CYLINDER.name());
            put("02", OperationParameter.BEATER.name());
            put("05", OperationParameter.PRODUCTION.name());
            //--Draw-frame parameters:
            put("06", OperationParameter.PRODUCTION_SPEED.name());
        }
    };

    Map<String, String> stopMessageTypeMap = new HashMap<String, String>() {
        {
            put("01", StopMessageType.REASON.name());
            put("02", StopMessageType.MOTOR_ERROR_CODE.name());
            put("03", StopMessageType.ERROR_VAL.name());
            put("00", "");
        }
    };

    Map<String, String> stopReasonValueMap = new HashMap<String, String>() {
        {
            put("0008", StopReasonValue.USER_PRESS_PAUSE.name());
			put("0009", StopReasonValue.SLIVER_CUT.name());
            put("0012", LENGTH_REACHED_USERMSG);//StopReasonValue.L_REACHED.name()); // PUT THE  final length reached text
            put("0014", LAPPING_USERMSG);//StopReasonValue.LAPPING.name());
            //--Draw-frame parameters:
            put("0030", StopReasonValue.FRONT_ROLLER.name()); //-->0x1E
            put("0031", StopReasonValue.BACK_ROLLER.name());  //-->0x1F
            put("0032", StopReasonValue.CREEL.name());        //-->0x20
        }
    };

    Map<String, String> motorErrorCodeMap = new HashMap<String, String>() {
        {
            put("0002", RPM_ERROR_USERMSG ) ; // MotorErrorCode.RPM_ERROR.name());
            put("0003", MotorErrorCode.MOTOR_VOLTAGE_ERROR.name());
            put("0004", MotorErrorCode.DRIVER_VOLTAGE_ERROR.name());
            put("0005", MotorErrorCode.SIGNAL_VOLTAGE_ERROR.name());
            put("0006", MotorErrorCode.OVERLOAD_CURRENT_ERROR.name());
        }
    };

    Map<String, String> motorMap = new HashMap<String, String>() {
        {
            put(MotorTypes.FRONT_ROLLER.name(), "30");
            put(MotorTypes.BACK_ROLLER.name(), "31");
            put(MotorTypes.CREEL.name(), "32");
        }
    };

    Map<String, String> PIDOptionMap = new HashMap<String, String>() {
        {
            put(PID_OPTIONS.FRONT_ROLLER.name(), "30");
            put(PID_OPTIONS.BACK_ROLLER.name(), "31");
            put(PID_OPTIONS.CREEL.name(), "32");
            put(PID_OPTIONS.DF_RAMP_VARS.name(), "33");
        }
    };

    Map<String, String> diagnoseTestTypesMap = new HashMap<String, String>() {
        {
            put(DiagnosticTestTypes.CLOSED_LOOP.name(), "01");
            put(DiagnosticTestTypes.OPEN_LOOP.name(), "02");
        }
    };

    Map<String, String> diagnoseAttrTypesMap = new HashMap<String, String>() {
        {
            put(DiagnosticAttrType.KIND_OF_TEST.name(), "01");
            put(DiagnosticAttrType.MOTOR_ID.name(), "02");
            put(DiagnosticAttrType.SIGNAL_VOLTAGE.name(), "03");
            put(DiagnosticAttrType.TARGET_RPM.name(), "04");
            put(DiagnosticAttrType.TEST_TIME.name(), "05");
        }
    };


}

