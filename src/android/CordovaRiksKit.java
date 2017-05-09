package io.hyker.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import android.content.Context;


import io.hyker.cryptobox.PropertyStore;
import io.hyker.cryptobox.Storage;
import io.hyker.riks.box.RiksKit;
import io.hyker.riks.keys.SymKeyExpiredException;
import io.hyker.riks.message.Message;
import org.spongycastle.crypto.CryptoException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Properties;

public class CordovaRiksKit extends CordovaPlugin {
    
    private static boolean isInit = false;
    //private static RiksKit riksKit = null;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        switch (action) {
            case "init":

         //       if (isInit){
          //          callbackContext.error("can not instantiate twice");
           //         return true;
            //    } else {
                    isInit = true;
                    //riksKit = initRiks(data);
		    String concat = "";
		    try {
                        concat = initRiks(data);
                    } catch (IOException e) {
                        callbackContext.error(" Error: " + e.getMessage());
                    }
                    callbackContext.success("mock object intitialized: " + concat);
                    return true;
             //   }


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

    private String initRiks(JSONArray data) throws JSONException, IOException {

        Context context = this.cordova.getActivity().getApplicationContext(); 

	//these are correct
        String deviceId = data.getString(0);
        String configPath = data.getString(1);
        String password = data.getString(2);

	PropertyStore ps = null;
	InputStream is = null;
	is = context.getAssets().open(configPath);
	Properties properties = new Properties();
	properties.load(is);
	ps = new PropertyStore(properties);
	String testPassword = ps.TRUST_STORE_PASSWORD;

        //return deviceId + configPath + password;
        return testPassword;

    }


}
