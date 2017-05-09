package io.hyker.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class CordovaRiksKit extends CordovaPlugin {
    
    private static boolean isInit = false;
    //private static RiksKit riksKit = null;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        switch (action) {
            case "init":

                if (isInit){
                    callbackContext.error("can not instantiate twice");
                    return true;
                } else {
                    isInit = true;
                    //riksKit = initRiks(data);
                    String concat = initRiks(data);
                    callbackContext.success("mock object intitialized:" + concat);
                    return true;
                }


            case "greet":

                String name = data.getString(0);
                String message = "Hello!, " + name + "!";
                callbackContext.success(message);

                return true;


            default:

                callbackContext.error("unknown java method");
                return false;

        }

    }

    private String initRiks(JSONArray data) throws JSONException {

        String deviceId = data.getString(0);
        String password = data.getString(1);
        String configPath = data.getString(2);

        return deviceId + password + configPath;

    }


}
