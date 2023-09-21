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
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import machine.microspin.com.drawframe_console.EditTextCustom;
import machine.microspin.com.drawframe_console.R;
import machine.microspin.com.drawframe_console.entity.DoubleInputFilter;
import machine.microspin.com.drawframe_console.entity.IntegerInputFilter;
import machine.microspin.com.drawframe_console.entity.SettingsCommunicator;
import machine.microspin.com.drawframe_console.entity.Settings;
import machine.microspin.com.drawframe_console.entity.Utility;
import machine.microspin.com.drawframe_console.entity.Pattern;
/**
 * Fragment to handle Settings (Editable and non Editable)
 */

public class SettingsFragment extends Fragment implements View.OnClickListener,View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {
    private SettingsCommunicator mCallback;
    private EditTextCustom setting1;
    private EditText setting2,setting3;
    public EditText Kpsetting,Kisetting,startOffsetsetting,FFMultiplierSetting; // pid motor options
    public EditText RampUpSetting, RampDownSetting; //df start vars
    public EditText RPMStopTriggersetting; //df stop vars

    public Button saveBtn,factorystngsBtn,PIDBtn,refreshPIDBtn,savePIDBtn;
    public ScrollView settingsScroll,PIDScroll;
    public Spinner pidOptionTypes;
    private String SETTINGS = "SETTINGS";
    private String PIDSETTINGS = "PID";

    // PID LAYOUTS
    private int PID_MOTOR_LAYOUT = 0;
    private int PID_DF_START_LAYOUT = 1;
    private int PID_DF_STOP_LAYOUT = 2;

    private String currentSettingsScreen = SETTINGS;
    public int PID_current_Layout = PID_MOTOR_LAYOUT;
    public Boolean waitingForPIDSettingsResponse = false;
    public LinearLayout PID_motor_optionView,PID_df_Start_optionView,PID_df_stop_optionView;

    private float currentDeliverySpeed ;
    private int currentDeliverySpeedRange;

    private String ZEROSTRING = "0";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frament_settings, container, false);

        setting1 =  rootView.findViewById(R.id.setting1);
        setting1.setOnFocusChangeListener(this);

        setting2 =  rootView.findViewById(R.id.setting2);
        setting3 =  rootView.findViewById(R.id.setting3);

        saveBtn = (Button) rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);

        factorystngsBtn = (Button) rootView.findViewById(R.id.factorystngs);
        factorystngsBtn.setOnClickListener(this);
	    setStatusInputFields(false);

        /* Pid Setting Stuff */
        PIDBtn = (Button)rootView.findViewById(R.id.pidBtn);
        PIDBtn.setOnClickListener(this);
        String buttonText = getString(R.string.msg_setting_next1);
        PIDBtn.setText(buttonText);

        refreshPIDBtn = (Button) rootView.findViewById(R.id.refreshPIDbtn);
        buttonText = getString(R.string.msg_PIDSetting_buttonRefresh);
        refreshPIDBtn.setText(buttonText);
        refreshPIDBtn.setOnClickListener(this);
        savePIDBtn = (Button) rootView.findViewById(R.id.savePIDbtn);
        savePIDBtn.setOnClickListener(this);

        settingsScroll = (ScrollView)rootView.findViewById(R.id.settingsScroll);
        PIDScroll = (ScrollView) rootView.findViewById(R.id.settingsPID);

        settingsScroll.setVisibility(View.VISIBLE);
        PIDScroll.setVisibility(View.INVISIBLE);

        //pid option value
        pidOptionTypes = (Spinner)rootView.findViewById(R.id.pidOptionValues);
        pidOptionTypes.setOnItemSelectedListener(this);

        List<String> motorCodeList = getValueListForPIDOptions();
        ArrayAdapter<String> spinnerArray = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item, motorCodeList);
        pidOptionTypes.setAdapter(spinnerArray);

        // Pid Linear Arrays for different types of options
        PID_motor_optionView = (LinearLayout)rootView.findViewById(R.id.PID_layout);
        PID_df_Start_optionView = (LinearLayout)rootView.findViewById(R.id.DF_StartVars);
        PID_df_stop_optionView = (LinearLayout)rootView.findViewById(R.id.DF_Stop_Vars);
        //texts within each options - motor pid options
        Kpsetting =  (EditText)rootView.findViewById(R.id.kpSetting);
        Kisetting =  (EditText)rootView.findViewById(R.id.kiSetting);
        startOffsetsetting =  (EditText)rootView.findViewById(R.id.startoffsetSetting);
        FFMultiplierSetting = (EditText)rootView.findViewById(R.id.FFConstantSetting);
        // df start vars options
        RampUpSetting =  (EditText)rootView.findViewById(R.id.c1Setting);
        RampDownSetting =  (EditText)rootView.findViewById(R.id.c2Setting);

        //df stop var options
        RPMStopTriggersetting =  (EditText)rootView.findViewById(R.id.df_stopSetting);

        PID_current_Layout = PID_MOTOR_LAYOUT;
        //ChangePIDLayoutState(PID_current_Layout);

        currentSettingsScreen = SETTINGS;

        /***************************/

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveBtn) {
            if (TextUtils.isEmpty(setting1.getText().toString()))
            {
                setting1.setText("0");
            }
            if (TextUtils.isEmpty(setting2.getText().toString()))
            {
                setting2.setText("0");
            }
            if (TextUtils.isEmpty(setting3.getText().toString()))
            {
                setting3.setText("0");
            }

            String validateMessage = isValidSettings();
            if (validateMessage == null) {
                String payload = Settings.updateNewSetting(
                        setting1.getText().toString(),
                        setting2.getText().toString(),
                        setting3.getText().toString()
                );
                mCallback.onSettingsUpdate(payload.toUpperCase());
            } else {
                mCallback.raiseMessage(validateMessage);
            }

        }
        if (v.getId() == R.id.factorystngs){
            setting1.setText(Settings.getDefaultDeliverySpeedString());
            setting2.setText(Settings.getDefaultDraftString());
            setting3.setText(Settings.getDefaultLengthLimitString());
         }

	if (v.getId() == R.id.pidBtn){
            if (currentSettingsScreen.equals(SETTINGS)) {
                settingsScroll.setVisibility(View.INVISIBLE);
                PIDScroll.setVisibility(View.VISIBLE);
                String buttonText = getString(R.string.msg_setting_back);
                PIDBtn.setText(buttonText);
                saveBtn.setVisibility(View.GONE);
                factorystngsBtn.setVisibility(View.GONE);
                currentSettingsScreen = PIDSETTINGS;
                ChangePIDLayoutState(PID_current_Layout);
            }else{
                settingsScroll.setVisibility(View.VISIBLE);
                PIDScroll.setVisibility(View.INVISIBLE);
                String buttonText = getString(R.string.msg_setting_next1);
                PIDBtn.setText(buttonText);
                saveBtn.setVisibility(View.VISIBLE);
                factorystngsBtn.setVisibility(View.VISIBLE);
                currentSettingsScreen = SETTINGS;
                CancelPIDWaitingResponseState();
                mCallback.ClosePIDWaitSnackBar();
            }
        }

        if (v.getId() == R.id.refreshPIDbtn){
            if (!waitingForPIDSettingsResponse){
                pidOptionTypes.setEnabled(false);
                String buttonText = getString(R.string.msg_PIDSetting_buttonCancel);
                refreshPIDBtn.setText(buttonText);
                // depending on the current motor layout get the correct vals
                Enable_PID_EditTexts(PID_current_Layout,false);
                // send only the request option to the macine to get back the correct value
                String pidOptionSelected = Utility.formatStringCode(pidOptionTypes.getSelectedItem().toString());
                String attrValue = Pattern.PIDOptionMap.get(pidOptionSelected);
                attrValue = Utility.formatValueByPadding(attrValue,2);
                String payload = Settings.RequestPIDSettings(attrValue);
                mCallback.onPIDRequest(payload);
                waitingForPIDSettingsResponse = true;
                }else{
                    CancelPIDWaitingResponseState();
                    Snackbar.make(getView(), R.string.msg_refresh_cancelled, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                     ChangePIDLayoutState(PID_current_Layout); // change if necessary and set defaults
                 }
            }

        if (v.getId() == R.id.savePIDbtn) {
            Check_PID_TextBoxes(PID_current_Layout);
            String validateMessage = isValidPIDSettings(PID_current_Layout);

            if( validateMessage == null) {
                String pidOptionSelected = Utility.formatStringCode(pidOptionTypes.getSelectedItem().toString());
                String attrValue = Pattern.PIDOptionMap.get(pidOptionSelected);
                // check which layout and get the current value
                int attr1Int= 0;
                int attr2Int = 0;
                int attr3Int = 0;
                int attr4Int = 0;
                if (PID_current_Layout == PID_MOTOR_LAYOUT) {
                    attr1Int  = (int)(Float.parseFloat(Kpsetting.getText().toString()) * 100);
                    attr2Int = (int)(Float.parseFloat(Kisetting.getText().toString()) * 100);
                    attr3Int = Integer.parseInt(startOffsetsetting.getText().toString());
                    attr4Int = (int)(Float.parseFloat((FFMultiplierSetting.getText().toString()))*100);
                }
                else if (PID_current_Layout == PID_DF_START_LAYOUT){
                    attr1Int  = Integer.parseInt(RampUpSetting.getText().toString());
                    attr2Int = Integer.parseInt(RampDownSetting.getText().toString());
                    attr3Int = 0;
                    attr4Int = 0;
                }
                else{
                    attr1Int  = Integer.parseInt(RPMStopTriggersetting.getText().toString());
                    attr2Int = 0;
                    attr3Int = 0;
                    attr4Int = 0;
                }
                String payload = Settings.updateNewPIDSetting(attrValue,
                        attr1Int,
                        attr2Int,
                        attr3Int,
                        attr4Int
                );

                mCallback.onSettingsUpdate(payload.toUpperCase());
            }else{
                mCallback.raiseMessage(validateMessage);
            }
        }
    }

    private void Check_PID_TextBoxes(int pidLayout){
        if (pidLayout == PID_MOTOR_LAYOUT){
            if (TextUtils.isEmpty(Kpsetting.getText().toString()))
            { Kpsetting.setText("0"); }
            if (TextUtils.isEmpty(Kisetting.getText().toString()))
            { Kisetting.setText("0"); }
            if (TextUtils.isEmpty(startOffsetsetting.getText().toString()))
            { startOffsetsetting.setText("0"); }
            if (TextUtils.isEmpty(FFMultiplierSetting.getText().toString()))
            { FFMultiplierSetting.setText("0"); }

        }else if (pidLayout == PID_DF_START_LAYOUT){
            if (TextUtils.isEmpty(RampUpSetting.getText().toString()))
            { RampUpSetting.setText("0"); }
            if (TextUtils.isEmpty(RampDownSetting.getText().toString()))
            { RampDownSetting.setText("0"); }
        }else{
            if (TextUtils.isEmpty(RPMStopTriggersetting.getText().toString()))
            { RPMStopTriggersetting.setText("0"); }
        }
    }

   private void SetSettingsScreenFromPID() {
        settingsScroll.setVisibility(View.VISIBLE);
        PIDScroll.setVisibility(View.INVISIBLE);
        String buttonText = getString(R.string.msg_setting_next1);
        PIDBtn.setText(buttonText);
        saveBtn.setVisibility(View.VISIBLE);
        factorystngsBtn.setVisibility(View.VISIBLE);
        currentSettingsScreen = SETTINGS;
        CancelPIDWaitingResponseState();
        mCallback.ClosePIDWaitSnackBar();
    }
    private String isValidPIDSettings(int pidLayout) {

        if (pidLayout == PID_MOTOR_LAYOUT) {
            DoubleInputFilter set1 = new DoubleInputFilter(getString(R.string.label_Kp), 0.01, 6.0);
            DoubleInputFilter set2 = new DoubleInputFilter(getString(R.string.label_Ki), 0.01, 6.0);
            IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_startingOffset), 0, 700);
            DoubleInputFilter set4 = new DoubleInputFilter(getString(R.string.label_FFConstant), 0.01, 5.0);

            if (set1.filter(Kpsetting) != null) {
                return set1.filter(Kpsetting);
            }
            if (set2.filter(Kisetting) != null) {
                return set2.filter(Kisetting);
            }
            if (set3.filter(startOffsetsetting) != null) {
                return set3.filter(startOffsetsetting);
            }
            if (set4.filter(FFMultiplierSetting) != null) {
                return set4.filter(FFMultiplierSetting);
            }

        }
        else if (pidLayout == PID_DF_START_LAYOUT){
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_C1), 2, 12);
            IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_C2), 2, 12);

            if (set1.filter(RampUpSetting) != null) {
                return set1.filter(RampUpSetting);
            }
            if (set2.filter(RampDownSetting) != null) {
                return set2.filter(RampDownSetting);
            }
        }
        else{
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_speedStop), 0, 999);
            if (set1.filter(RPMStopTriggersetting) != null) {
                return set1.filter(RPMStopTriggersetting);
            }
        }

        return null;
    }

    private String isValidSettings() {
        IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_delivery_speed), 40, 140);

        float deliverySpeed = Float.valueOf(setting1.getText().toString());
        currentDeliverySpeedRange =  GetDeliverySpeedRange(deliverySpeed);

        double min_draft_thresh = 0.0;
        double max_draft_thresh = 11.0;
        if (currentDeliverySpeedRange == 1){
            min_draft_thresh = 5.5;
            max_draft_thresh = 9.9;}
        if (currentDeliverySpeedRange == 2){
            min_draft_thresh = 6.5;
            max_draft_thresh = 9.9;}
        if (currentDeliverySpeedRange == 3){
            min_draft_thresh = 9.0;
            max_draft_thresh = 11.0;}

        DoubleInputFilter set2 = new DoubleInputFilter(getString(R.string.label_tension_draft), min_draft_thresh, max_draft_thresh);
        IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_length_limit), 100, 5000);

        if (set1.filter(setting1) != null) {
            return set1.filter(setting1);
        }
        if (set2.filter(setting2) != null) {
            return set2.filter(setting2);
        }
        if (set3.filter(setting3) != null) {
            return set3.filter(setting3);
        }
        return null;
    }

    public void isEditMode(Boolean isEdit) {
        if (isEdit) {
            //Make settings editable
            saveBtn.setVisibility(View.VISIBLE);
            factorystngsBtn.setVisibility(View.VISIBLE);
            setStatusInputFields(true);
        } else {
            //Make settings non editable.
            saveBtn.setVisibility(View.INVISIBLE);
            factorystngsBtn.setVisibility(View.INVISIBLE);
            setStatusInputFields(false);
        }
    }


    private boolean isInRangeFloat(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

    public int GetDeliverySpeedRange(float deliverySpeed){
       if (isInRangeFloat(40,100,deliverySpeed)){
           return 1;
       }else if (isInRangeFloat(100.01f,120,deliverySpeed)) {
        return 2;
       }else{
           return 3;
       }
    }

    public void updateContent() {
        setting1.setText(Settings.getDeliverySpeedString());
        setting2.setText(Settings.getDraftString());
        setting3.setText(Settings.getLengthLimitString());
    }

    public void setStatusInputFields(Boolean bol) {
        setting1.setEnabled(bol);
        setting2.setEnabled(bol);
        setting3.setEnabled(bol);
    }

/**************NEW FUNCTIONS ADDED FOR PID SEtting MODE ********8/
 *
 */
private List<String> getValueListForPIDOptions() {
    List<String> list = new ArrayList<>();
    int length;
    length = Pattern.PID_OPTIONS.values().length;
    while (length > 0) {
        String value = Pattern.PID_OPTIONS.values()[length-1].name();
        list.add(Utility.formatString(value));
        length--;
    }
    return list;
    }

    //Spinner TestType Events
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // if not motors, then show the options of the start and stop variables, else show the motor variables
        String pidOptionsSelected = Utility.formatStringCode((pidOptionTypes.getSelectedItem().toString()));
        if (pidOptionsSelected.equals(Pattern.PID_OPTIONS.DF_RAMP_VARS.toString())) {
            PID_current_Layout = PID_DF_START_LAYOUT;
        }else {
            PID_current_Layout = PID_MOTOR_LAYOUT;
        }
        ChangePIDLayoutState(PID_current_Layout);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

     public void Enable_PID_EditTexts(int current_layout,boolean setting) {
         if (current_layout == PID_MOTOR_LAYOUT) {
             Kisetting.setEnabled(setting);
             Kpsetting.setEnabled(setting);
             startOffsetsetting.setEnabled(setting);
             FFMultiplierSetting.setEnabled(setting);
         }
         if (current_layout == PID_DF_START_LAYOUT) {
             RampUpSetting.setEnabled(setting);
             RampDownSetting.setEnabled(setting);
         }
         if (current_layout == PID_DF_STOP_LAYOUT) {
             RPMStopTriggersetting.setEnabled(setting);
         }
     }


    public void updatePIDContent() {
        if (PID_current_Layout == PID_MOTOR_LAYOUT) {
            Kpsetting.setText(Settings.MakeFloatString(Settings.pid_req_attr1 / 100.0f,2));
            Kisetting.setText(Settings.MakeFloatString(Settings.pid_req_attr2 / 100.0f,2));
            startOffsetsetting.setText(Settings.MakeIntString(Settings.pid_req_attr3));
            FFMultiplierSetting.setText(Settings.MakeFloatString(Settings.pid_req_attr4 / 100.0f,2));
            Kisetting.setEnabled(true);
            Kpsetting.setEnabled(true);
            startOffsetsetting.setEnabled(true);
            FFMultiplierSetting.setEnabled(true);
        }
        else if(PID_current_Layout == PID_DF_START_LAYOUT){
            RampUpSetting.setText(Settings.MakeIntString(Settings.pid_req_attr1));
            RampDownSetting.setText(Settings.MakeIntString(Settings.pid_req_attr2));
            RampUpSetting.setEnabled(true);
            RampDownSetting.setEnabled(true);
        }
        else {
            RPMStopTriggersetting.setText(Settings.MakeIntString(Settings.pid_req_attr1));
            RPMStopTriggersetting.setEnabled(true);
        }
        pidOptionTypes.setEnabled(true);
    }

    public void CancelPIDWaitingResponseState(){
        pidOptionTypes.setEnabled(true);
        String buttonText = getString(R.string.msg_PIDSetting_buttonRefresh);
        refreshPIDBtn.setText(buttonText);
        waitingForPIDSettingsResponse = false;
    }

    public void ChangePIDLayoutState(int layout){
        // set the PID motor linear layout on and set all the default options of that.
        if (layout == PID_MOTOR_LAYOUT) {
            PID_motor_optionView.setVisibility(View.VISIBLE);
            PID_df_Start_optionView.setVisibility(View.GONE);
            PID_df_stop_optionView.setVisibility(View.GONE);

            Kisetting.setText(ZEROSTRING);
            Kpsetting.setText(ZEROSTRING);
            startOffsetsetting.setText(ZEROSTRING);
            FFMultiplierSetting.setText(ZEROSTRING);
            Kisetting.setEnabled(false);
            Kpsetting.setEnabled(false);
            startOffsetsetting.setEnabled(false);
            FFMultiplierSetting.setEnabled(false);
        }

        if (layout == PID_DF_START_LAYOUT) {
            PID_motor_optionView.setVisibility(View.GONE);
            PID_df_Start_optionView.setVisibility(View.VISIBLE);
            PID_df_stop_optionView.setVisibility(View.GONE);

            RampUpSetting.setText(ZEROSTRING);
            RampDownSetting.setText(ZEROSTRING);
            RampUpSetting.setEnabled(false);
            RampDownSetting.setEnabled(false);
        }

        if (layout == PID_DF_STOP_LAYOUT) {
            PID_motor_optionView.setVisibility(View.GONE);
            PID_df_Start_optionView.setVisibility(View.GONE);
            PID_df_stop_optionView.setVisibility(View.VISIBLE);

            RPMStopTriggersetting.setText(ZEROSTRING);
            RPMStopTriggersetting.setEnabled(false);
        }
        waitingForPIDSettingsResponse = false;
    }

    public void MakePIDButtonVisible(boolean visible){
        if (visible){
            PIDBtn.setVisibility(View.VISIBLE);
        }else{
            if (currentSettingsScreen.equals(PIDSETTINGS)){
                settingsScroll.setVisibility(View.VISIBLE);
                PIDScroll.setVisibility(View.INVISIBLE);
                String buttonText = getString(R.string.msg_setting_next1);
                PIDBtn.setText(buttonText);
                saveBtn.setVisibility(View.VISIBLE);
                factorystngsBtn.setVisibility(View.VISIBLE);
                currentSettingsScreen = SETTINGS;
                CancelPIDWaitingResponseState();
                mCallback.ClosePIDWaitSnackBar();
            }
            PIDBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
       /* if (!b) {
            float newDeliverySpeed = Float.valueOf(setting1.getText().toString());
            int newDeliverySpeedRange = GetDeliverySpeedRange(newDeliverySpeed);

            if (newDeliverySpeedRange != currentDeliverySpeedRange){
                 currentDeliverySpeed = newDeliverySpeed;
                 currentDeliverySpeedRange = newDeliverySpeedRange;
            }
        }*/
    }

    }
