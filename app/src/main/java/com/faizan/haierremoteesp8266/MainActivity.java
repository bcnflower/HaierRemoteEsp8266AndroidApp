package com.faizan.haierremoteesp8266;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    String url = "http://192.168.1.39/state";
//    String url = "http://ac.local/state";
    int orientation = -1;
    RemoteControl remoteControl;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView tempLabel;
    SeekBar tempSeek;
    Switch powerSwitch;
    SharedPreferences settings;
    SharedPreferences.Editor editor;


    public void updateTempLabel(){
        tempLabel.setText((tempSeek.getProgress() + 16) + " °C");
    }

    public void tempPlus(View v){
        tempSeek.setProgress(tempSeek.getProgress()+1);
        remoteControl.setTemp(tempSeek.getProgress()+16);
        remoteControl.send();
    }
    public void tempMinus(View v){
        tempSeek.setProgress(tempSeek.getProgress()-1);
        remoteControl.setTemp(tempSeek.getProgress()+16);
        remoteControl.send();
    }

    public void tostMsg(String msg){
        Log.e("Error",msg);
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    public int getOrientation(){
        return getResources().getConfiguration().orientation;
    }

    public void setOrientation(int orientation){
        if (orientation > 0) {

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        settings = getSharedPreferences("Settings", 0);
        editor = settings.edit();

        if(settings.contains("url")){
            this.url = settings.getString("url","");
        }else{
            editor.putString("url",this.url);
            editor.commit();
        }

        if(settings.contains("orientation")){
            this.orientation = settings.getInt("orientation",-1);
        }else{
            editor.putInt("orientation",this.orientation);
            editor.commit();
        }

        remoteControl = new RemoteControl(url,this);
        setOrientation(orientation);

        tempLabel = findViewById(R.id.tempLabel);
        tempSeek = findViewById(R.id.tempSeek);
        powerSwitch = findViewById(R.id.powerSwitch);

        updateTempLabel();

        tempSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTempLabel();
                remoteControl.setTemp(progress+16);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                remoteControl.send();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                remoteControl.updateState(new VolleyCallback() {
                    @Override
                    public void onError(String message) {
                        tostMsg("Error while Refreshing");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    @Override
                    public void onResponse(JSONObject response) {
                        tostMsg("State Refreshed");
                        updateUi();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        updateRemoteState();
    }

    public void togglePower(View v){
        remoteControl.setPower(powerSwitch.isChecked());
        remoteControl.send();
    }

    public void selectLayoutButton(int LayoutId,String tag){
        ArrayList<View> allButtons;
        allButtons = ((LinearLayout) findViewById(LayoutId)).getTouchables();
        for(View button : allButtons){
//            tostMsg("Tag ="+tag+". getTag()="+button.getTag().toString()+".");
            if(tag.equals(button.getTag().toString())){
                ((ToggleButton)button).setChecked(true);
                continue;
            }
            ((ToggleButton)button).setChecked(false);
        }
    }

    public void selectFanSpeed(View v){
        remoteControl.setFanSpeed(Integer.parseInt(v.getTag().toString()));
        selectLayoutButton(R.id.fanSpeedLayout,v.getTag().toString());
        remoteControl.send();
    }

    public void selectClimateMode(View v){
        remoteControl.setClimateMode(Integer.parseInt(v.getTag().toString()));
        selectLayoutButton(R.id.climateModeLayout,v.getTag().toString());
        remoteControl.send();
    }

    public void selectVerticalSwingMode(View v){
        remoteControl.setVerticalSwing(Integer.parseInt(v.getTag().toString()));
        selectLayoutButton(R.id.verticalSwingLayout,v.getTag().toString());
        remoteControl.send();
    }

    public void selectTurboButton(boolean active){
        ((ToggleButton)findViewById(R.id.turboBtn)).setChecked(active);
        if(active){
            ((ToggleButton)findViewById(R.id.quietBtn)).setChecked(false);
        }
    }

    public void selectTurbo(View v){
        boolean active = ((ToggleButton)v).isChecked();
        remoteControl.setTurboMode(active);
        selectTurboButton(active);
        remoteControl.send();
    }

    public void selectQuietButton(boolean active){
        ((ToggleButton)findViewById(R.id.quietBtn)).setChecked(active);
        if(active){
            ((ToggleButton)findViewById(R.id.turboBtn)).setChecked(false);
        }
    }

    public void selectQuiet(View v){
        boolean active = ((ToggleButton)v).isChecked();
        remoteControl.setQuietMode(active);
        selectQuietButton(active);
        remoteControl.send();
    }

    public void selectHealthButton(boolean active){
        ((ToggleButton)findViewById(R.id.healthBtn)).setChecked(active);
    }

    public void selectHealth(View v){
        boolean active = ((ToggleButton)v).isChecked();
        remoteControl.setHealth(active);
        selectHealthButton(active);
        remoteControl.send();
    }

    public void toggleDisplay(View v){
        remoteControl.toggleDisplay();
    }

    public void saveButton(View v){
        remoteControl.postWithParam("?save=1",null);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ToggleButton)v).setChecked(false);
                    }
                });
            }
        }, 300L); // 300 is the delay in millis
    }

    public void selectAutoSend(View v){
        remoteControl.setAutoSend( ((ToggleButton)v).isChecked() );
    }

    public void sendButton(View v){
        remoteControl.doPost(null);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ToggleButton)v).setChecked(false);
                }
                });
            }
        }, 300L); // 300 is the delay in millis
    }

    public void updateUi(){
        ((ToggleButton)findViewById(R.id.autosendBtn)).setChecked(remoteControl.isAutoSend());
        powerSwitch.setChecked(remoteControl.getPower());
        tempSeek.setProgress(remoteControl.getTemperature()-16);
        selectLayoutButton(R.id.fanSpeedLayout,Integer.toString(remoteControl.getFanSpeed()));
        selectLayoutButton(R.id.climateModeLayout,Integer.toString(remoteControl.getClimateMode()));
        selectLayoutButton(R.id.verticalSwingLayout,Integer.toString(remoteControl.getVerticalSwing()));
        selectTurboButton(remoteControl.getTurboMode());
        selectHealthButton(remoteControl.getHealth());
        selectQuietButton(remoteControl.getQuietMode());
    }

    public void setDefaultControls(){
        remoteControl.setTemp(28);
        remoteControl.setFanSpeed(0);
        remoteControl.setClimateMode(1);
        remoteControl.setVerticalSwing(1);
        remoteControl.setHealth(true);
    }

    public void updateRemoteState(){
        remoteControl.updateState(new VolleyCallback() {
            @Override
            public void onError(String message) {
            }
            @Override
            public void onResponse(JSONObject response) {
                updateUi();
            }
        });
    }

    public void func(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.settings_popup, (ViewGroup)findViewById(android.R.id.content), false);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        final Switch orientationLock = (Switch)viewInflated.findViewById(R.id.orientationLock);
        input.setText(remoteControl.url);
        if(orientation>0){
            orientationLock.setChecked(true);
        }else{
            orientationLock.setChecked(false);
        }
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                remoteControl.url = input.getText().toString();
                editor.putString("url",remoteControl.url);
                if(orientationLock.isChecked()){
                    orientation = getOrientation();
                }else{
                    orientation = -1;
                }
                editor.putInt("orientation",orientation);
                setOrientation(orientation);
                editor.commit();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}