package com.faizan.haierremoteesp8266;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


public class RemoteControl {
    protected static final String POWER = "power";
    protected static final String MODE = "mode";
    protected static final String SWING_V = "swingV";
    protected static final String TEMPERATURE = "temp";
    protected static final String FAN_SPEED = "fan";
    protected static final String HEALTH = "health";
    protected static final String QUIET = "quiet";
    protected static final String TURBO = "turbo";
    protected static final String DISPLAY = "disp";



    JSONObject state = new JSONObject();
    String url;
    Context context;
    boolean sending = false;
    boolean autoSend = true;
    boolean multiSend = false;

    public boolean isAutoSend() {
        return autoSend;
    }

    public void setAutoSend(boolean autoSend) {
        this.autoSend = autoSend;
    }

    public RemoteControl(String url,Context context){
        this.url = url;
        this.context = context;
    }

    public void tostMsg(String msg){
        Log.e("Error",msg);
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
    }

    public void updateState(final VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        state = response;
                        if(callback != null){
                            callback.onResponse(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tostMsg(error.toString());
                        if(callback != null){
                            callback.onError(error.toString());
                        }
                    }
                });
        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);
    }

    public void doPost(VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.PUT, url, state, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        tostMsg(response.toString());
                        if(callback != null){
                            callback.onResponse(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tostMsg(error.toString());
                        if(callback != null){
                            callback.onError(error.toString());
                        }
                    }
                });
        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);
    }

    public void postWithParam(String param,VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.PUT, url+param, state, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        tostMsg(response.toString());
                        if(callback != null){
                            callback.onResponse(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tostMsg(error.toString());
                        if(callback != null){
                            callback.onError(error.toString());
                        }
                    }
                });
        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);
    }

    public void send(){
        if(autoSend){
            if(sending){
                multiSend = true;
                return;
            }
            sending = true;
            doPost(null);
            //https://stackoverflow.com/questions/26311470/what-is-the-equivalent-of-javascript-settimeout-in-java
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // here goes your code to delay
                    if(multiSend){
                        doPost(null);
                        multiSend = false;
                    }
                    sending = false;
                }
            }, 2000L); // 300 is the delay in millis

        }
    }

    public void setKeyVal(String key,int val){
        try {
            state.put(key,val);
        } catch (JSONException e) {
            e.printStackTrace();
            tostMsg(e.toString());
        }
    }

    public int getKeyValInt(String key){
        int value = -1;
        try {
            value = state.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
            tostMsg(e.toString());
        }
        return value;
    }

    public void setKeyVal(String key,boolean val){
        try {
            state.put(key,val);
        } catch (JSONException e) {
            e.printStackTrace();
            tostMsg(e.toString());
        }
    }

    public boolean getKeyValBool(String key){
        boolean value = false;
        try {
            value = state.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
            tostMsg(e.toString());
        }
        return value;
    }

    public void setPower(boolean power){
        setKeyVal(POWER,power);
    }
    public boolean getPower(){
        return getKeyValBool(POWER);
    }
    public void setTemp(int temp){
        setKeyVal(TEMPERATURE,temp);
    }
    public int getTemperature(){
        return getKeyValInt(TEMPERATURE);
    }
    public void setClimateMode(int mode){
        setKeyVal(MODE,mode);
    }
    public int getClimateMode(){
        return getKeyValInt(MODE);
    }
    public void setFanSpeed(int speed){
        setKeyVal(FAN_SPEED,speed);
    }
    public int getFanSpeed(){
        return getKeyValInt(FAN_SPEED);
    }
    public void setVerticalSwing(int swingV){
        setKeyVal(SWING_V,swingV);
    }
    public int getVerticalSwing(){
        return getKeyValInt(SWING_V);
    }
    public void setHealth(boolean health){
        setKeyVal(HEALTH,health);
    }
    public boolean getHealth(){
        return getKeyValBool(HEALTH);
    }
    public void setQuietMode(boolean quietMode){
        setKeyVal(QUIET,quietMode);
        setKeyVal(TURBO,false);
    }
    public boolean getQuietMode(){
        return getKeyValBool(QUIET);
    }
    public void setTurboMode(boolean turboMode){
        setKeyVal(TURBO,turboMode);
        setKeyVal(QUIET,false);
    }
    public boolean getTurboMode(){
        return getKeyValBool(TURBO);
    }
    public void toggleDisplay(){
        setKeyVal(DISPLAY,true);
        doPost(null);
        state.remove(DISPLAY);
    }

}
