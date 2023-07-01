package machine.microspin.com.drawframe_console.fragment;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import machine.microspin.com.drawframe_console.EditTextCustom;
import machine.microspin.com.drawframe_console.R;
import machine.microspin.com.drawframe_console.entity.IntegerInputFilter;
import machine.microspin.com.drawframe_console.entity.Packet;
import machine.microspin.com.drawframe_console.entity.Pattern;
import machine.microspin.com.drawframe_console.entity.Settings;
import machine.microspin.com.drawframe_console.entity.SettingsCommunicator;
import machine.microspin.com.drawframe_console.entity.TLV;
import machine.microspin.com.drawframe_console.entity.Utility;


public class DiagnosticsFragment extends Fragment  implements View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {

    private SettingsCommunicator mCallback;
    //diagnose menu items
    private Spinner testType;
    private Spinner motorCode;
    private EditText runTime;
    private EditText signalValue;
    private EditTextCustom targetRPMPercent;
    private TextView maxRpmText;
    private TextView targetRPMOut;

    //live items
    private TextView motorCodeLive;
    private TextView signalValueLive;
    private TextView actualRPMLive;
    private TextView testTypeLive;
    private TextView targetTextLive;
    private TextView targetLabelLive;

    private Button runDiagnose;
    private Button stopDiagnose;
    private Snackbar diagRunningSnackBar = null;
    private LinearLayout menuLayout;
    private LinearLayout liveLayout;

    private static Boolean isDiagnoseRunning = false;

    //harsha added
    private Snackbar snackbarComplete ;
    private int maxRPM = 0;
    private int actualRPM = 0;
    private int targetRpmCalc = 0;
    private int targetSignalVoltage = 0;

    //booleans
    private static boolean firstInit = false;
    private static boolean isSnackbarOn = false;
    //=================== STATIC Codes ========================
    final private static String SPINNER_TEST_TYPE = "TEST_TYPE";
    final private static String SPINNER_MOTOR_CODE = "MOTOR_TYPE";

    final private static String LAYOUT_MENU = "MENU";
    final public static String LAYOUT_LIVE = "LIVE";

    private static final String TAG = "Diagnose";

    final private static String STOP_BTN = "STOP";
    final private static String RESET_BTN = "RESET";
    private String stopBtnMode = STOP_BTN;

    private String testTypeSelected = "";
    private String motorCodeSelected = "";

    public boolean backButton_disable = false;


    public DiagnosticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_diagnostics, container, false);
        if(Settings.device != null) {
            // setTitle(Settings.device.getName());
            getActivity().setTitle(Settings.device.getName());
        }

        //menu stuff
        testType =  rootView.findViewById(R.id.testType);
        testType.setOnItemSelectedListener(this);

        motorCode = rootView.findViewById(R.id.pidOptionValues);
        motorCode.setOnItemSelectedListener(this);

        runTime =  rootView.findViewById(R.id.testRunTime);
        signalValue = rootView.findViewById(R.id.signalValue);

        targetRPMPercent =  rootView.findViewById(R.id.targetRpmPercent);
        targetRPMPercent.setOnFocusChangeListener(this);

        maxRpmText = rootView.findViewById(R.id.maxMotorRpmText);
        targetRPMOut =   rootView.findViewById(R.id.targetRPMout);

        //LIVE STUFFS
        testTypeLive =  rootView.findViewById(R.id.typeOfTestLive);
        motorCodeLive =  rootView.findViewById(R.id.motorCodeLive);
        signalValueLive =  rootView.findViewById(R.id.signalVoltgaeLive);
        actualRPMLive =  rootView.findViewById(R.id.actualRPMLive);
        targetTextLive =  rootView.findViewById(R.id.targetTextLive);
        targetLabelLive =  rootView.findViewById(R.id.targetLabelLive);

        //Buttons
        runDiagnose =  rootView.findViewById(R.id.runDiagnose);
        runDiagnose.setOnClickListener(this);

        stopDiagnose = (Button)rootView. findViewById(R.id.stopBtn);
        stopDiagnose.setOnClickListener(this);
        stopDiagnose.setText(STOP_BTN);

        //layouts
        menuLayout =  rootView.findViewById(R.id.diagnoseMenu);
        liveLayout =  rootView.findViewById(R.id.diagnoseLive);

        List<String> motorCodeList = getValueListForSpinner(SPINNER_MOTOR_CODE);
        motorCode.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, motorCodeList));
        List<String> testTypeList = getValueListForSpinner(SPINNER_TEST_TYPE);
        testType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, testTypeList));

        setDefaultValuesMenuScreen();
        setDefaultValuesLiveScreen();
        toggleViewOn(LAYOUT_MENU);


        // Inflate the layout for this fragment
        return rootView;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SettingsCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SettingsCommunicator");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }



    //====================================== OTHER EVENTS ==========================================
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.runDiagnose:
                runDiagnose(); // sends the packet
                backButton_disable = true; //disables the hardware back button
                break;
            case R.id.stopBtn:
                if (stopBtnMode.equals(STOP_BTN)) {
                    mCallback.writeStopDiagnosis();
                    mCallback.CloseDiagSnackBar();
                    stopDiagnose.setText(RESET_BTN);
                    stopBtnMode = RESET_BTN;
                    if (diagRunningSnackBar != null) {
                        diagRunningSnackBar.dismiss();
                    }
                } else {
                    //ZERO THE LIVE SCREEN VALUES NOW THAT WE RE LEAVING IT
                    setDefaultValuesLiveScreen();
                    //SETUP THE NEW MENU SCREEN
                    stopDiagnose.setText(STOP_BTN);
                    stopBtnMode = STOP_BTN;
                    mCallback.DisableTabsForDiagnostics(false);
                    backButton_disable = false;
                    toggleTestMenuWidgets(true);
                    toggleViewOn(LAYOUT_MENU);
                    setDefaultValuesMenuScreen();
                }
                break;
        }
    }


    //Spinner TestType Events
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (!firstInit) {
            firstInit = true;
        } else {
            if (parent.getId() == R.id.testType) {
                if (pos == 0) {
                    signalValue.setEnabled(true);
                    targetRPMPercent.setEnabled(false);
                    targetRPMPercent.setText("0");
                    targetRPMOut.setText("0");
                    runTime.setText("0");
                } else {
                    signalValue.setEnabled(false);
                    signalValue.setText("0");
                    targetRPMPercent.setEnabled(true);
                    targetRPMPercent.setText("0");
                    runTime.setText("0");
                }
            }
            if (parent.getId() == R.id.pidOptionValues) {
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
            }
        }
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    //==================================== CUSTOM FUNCTIONS ========================================

    private int GetMaxRPM(String motorSelected)
    {   int maxRpm1 = 1500; // in df for all motors from Phillipines2
        return maxRpm1;
    }

    private void runDiagnose() {
        Packet diagnosePacket = new Packet(Packet.OUTGOING_PACKET);
        TLV[] attributes = new TLV[5];  //Specified in the requirements
        String attrType;
        String attrValue;
        String attrLength;

        //****** Handling=> testType Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.KIND_OF_TEST.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        testTypeSelected = Utility.formatStringCode(testType.getSelectedItem().toString());
        attrValue = Pattern.diagnoseTestTypesMap.get(testTypeSelected);
        TLV testType = new TLV(attrType, attrLength, attrValue);
        attributes[0] = testType;

        //****** Handling=> MotorCode Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.MOTOR_ID.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        motorCodeSelected = Utility.formatStringCode(motorCode.getSelectedItem().toString());
        attrValue = Pattern.motorMap.get(motorCodeSelected);
        TLV motorCode = new TLV(attrType, attrLength, attrValue);
        attributes[1] = motorCode;

        //****** Handling=> Signal Voltage Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.SIGNAL_VOLTAGE.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        if (TextUtils.isEmpty(signalValue.getText().toString()))
        {
               signalValue.setText("0");
        }
        targetSignalVoltage = Integer.parseInt(signalValue.getText().toString());
        attrValue = Utility.convertIntToHexString(targetSignalVoltage);
        TLV signalValue = new TLV(attrType, attrLength, attrValue);
        attributes[2] = signalValue;

            //****** Handling=> Target RPM Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TARGET_RPM.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        if (TextUtils.isEmpty(targetRPMOut.getText().toString()))
        {
            targetRPMPercent.setText("0");
            targetRPMOut.setText("0");
        }
        Integer i2 = Integer.parseInt(targetRPMOut.getText().toString());
        attrValue = Utility.convertIntToHexString(i2);
        TLV targetRPM = new TLV(attrType, attrLength, attrValue);
        attributes[3] = targetRPM;

        //******* Handling=> RunTime Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TEST_TIME.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        if (TextUtils.isEmpty(runTime.getText().toString()))
        {
                runTime.setText("0");
        }
        Integer i3 = Integer.parseInt(runTime.getText().toString());
        attrValue = Utility.convertIntToHexString(i3);

        TLV runTime = new TLV(attrType, attrLength, attrValue);
        attributes[4] = runTime;
     
        String screen = Utility.ReverseLookUp(Pattern.screenMap, Pattern.Screen.DIAGNOSTICS.name());
        String machineId = Settings.getMachineId();
        String machineType = Utility.ReverseLookUp(Pattern.machineTypeMap, Pattern.MachineType.DRAW_FRAME.name());
        String messageType = Utility.ReverseLookUp(Pattern.messageTypeMap, Pattern.MessageType.BACKGROUND_DATA.name());
        String screenSubState = Pattern.COMMON_NONE_PARAM;

        //****CHANGE FOR RELEASE V3 -- harsha
        //Data Validation for Test RPM & Signal Voltage
        String validateMessage = isValidData();
        if (validateMessage == null) {
            String payload = diagnosePacket.makePacket(screen,
                    machineId,
                    machineType,
                    messageType,
                    screenSubState,
                    attributes);

            mCallback.writeDiagStartCommand(payload);

            /**added here for the fragment**/
            diagRunningSnackBar = Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.msg_diagnose_running, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null);
            diagRunningSnackBar.show();
            toggleTestMenuWidgets(false);
            toggleViewOn(LAYOUT_LIVE);
            mCallback.DisableTabsForDiagnostics(true);
        } else {
            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), validateMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
    }

    private List<String> getValueListForSpinner(final String entity) {
        List<String> list = new ArrayList<>();
        int length;
        switch (entity) {
            case "MOTOR_TYPE":
                length = Pattern.MotorTypes.values().length;
                while (length > 0) {
                    String value = Pattern.MotorTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
            case "TEST_TYPE":
                length = Pattern.DiagnosticTestTypes.values().length;
                while (length > 0) {
                    String value = Pattern.DiagnosticTestTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;            
        }
        return list;
    }

    private void toggleTestMenuWidgets(final Boolean bol) {
                testType.setEnabled(bol);
                motorCode.setEnabled(bol);
                runTime.setEnabled(bol);
                if (bol) {
                    runDiagnose.setVisibility(View.VISIBLE);
                } else {
                    runDiagnose.setVisibility(View.INVISIBLE);
                }
    }

    public void toggleViewOn(final String layout) {
                switch (layout) {
                    case LAYOUT_LIVE:
                        if (liveLayout.getVisibility() == View.INVISIBLE) {
                            menuLayout.setVisibility(View.INVISIBLE);
                            liveLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                    case LAYOUT_MENU:
                        menuLayout.setVisibility(View.VISIBLE);
                        liveLayout.setVisibility(View.INVISIBLE);
                        break;
                }
    }


    private void setDefaultValuesMenuScreen() {

                motorCode.setSelection(0);
                testType.setSelection(0);
                targetRPMPercent.setEnabled(false);
                signalValue.setEnabled(true);
                runTime.setText("0");
                signalValue.setText("0");
                targetRPMPercent.setText("0");
                targetRPMOut.setText("0");
                //-----------------------//
                // set the correct max Rpm in the menu screen.
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                //put the logic here only for what the maxRpm should be
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
                //----------------------//
        }

    private void setDefaultValuesLiveScreen() {

        testTypeLive.setText("");
        motorCodeLive.setText("");
        signalValueLive.setText("");
        actualRPMLive.setText("");
        targetLabelLive.setText("");
        targetTextLive.setText("");
    }


    public void updateLiveData(final TLV[] attributes) {

        String signalVoltage = attributes[2].getValue();
        String targetRPM = attributes[3].getValue();

                
        testTypeLive.setText(testTypeSelected);
        motorCodeLive.setText(motorCodeSelected);

        if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())){
            targetLabelLive.setText("Target Signal Voltage %");
            targetTextLive.setText(Integer.toString(targetSignalVoltage));
        }
        if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())){
            targetLabelLive.setText("Target RPM ");
            targetTextLive.setText(Integer.toString(targetRpmCalc));
        }

        signalValueLive.setText(signalVoltage);
        actualRPMLive.setText(targetRPM);
    }

    private String isValidData() {
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_diagnose_ip_signal), 10, 90);
            IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_diagnose_target_rpm_percent), 10, 90);
            IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_diagnose_run_time), 30, 600);

            String testTypeSelected = Utility.formatStringCode((testType.getSelectedItem().toString()));
            //only check the box you want to use
            if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())) {
                if (set1.filter(signalValue) != null) {
                    return set1.filter(signalValue);
                }
            }

            if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())) {
                if (set2.filter(targetRPMPercent) != null) {
                    return set2.filter(targetRPMPercent);
                }
            }

            if (set3.filter(runTime) != null) {
                return set3.filter(runTime);
            }
        return null;
    }


    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            if (TextUtils.isEmpty(targetRPMPercent.getText().toString())) {
                targetRPMOut.setText(Integer.toString(0));
                targetRPMPercent.setText(Integer.toString(0));
            } else {
                int currentTargetPercent = Integer.parseInt(targetRPMPercent.getText().toString());
                targetRpmCalc = (currentTargetPercent * maxRPM) / 100;
                targetRPMOut.setText(Integer.toString(targetRpmCalc));
            }
        }
    }

}


