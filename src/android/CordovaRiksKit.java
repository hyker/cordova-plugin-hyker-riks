package io.hyker.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class CordovaRiksKit extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        switch (action) {
            case "greet":

                String name = data.getString(0);
                String message = "Hello!, " + name + "!";
                callbackContext.success(message);

                return true;


            default:

                return false;

        }

    }


}
