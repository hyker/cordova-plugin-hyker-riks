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
    private static RiksKit riksKit = null;

    private static synchronized void setInit(){
	isInit = true;
    }

    private static synchronized boolean isInit(){
	return isInit;
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        switch (action) {
            case "init":

                if (isInit()){
                    callbackContext.error("can not instantiate twice");
                    return true;
                } else {
		    setInit();
		    try {
                        initRiks(data);
                    } catch (IOException e) {
                        callbackContext.error(" Error: " + e.getMessage());
			return true;
                    }
                    callbackContext.success("riks intitialized");
                    return true;
                }


            case "encrypt":

		String encrypted = null;

        	try {
                    encrypted = encrypt(data);
                } catch (IOException e) {
                    callbackContext.error(" Error: " + e.getMessage());
		    return true;
                }

                callbackContext.success(encrypted);
                return true;
	    
            case "decrypt":

                String enc = data.getString(0);
		riksKit.decryptMessageAsync(enc,  (m, e) -> {
		    
                    if (e != null){
                        callbackContext.error("Decrypt error: " + e.getMessage());
		    } else {
            	        String decryptedMessage = m.secret;
                        callbackContext.success(decryptedMessage);
		    }
                });

                return true;

            default:

                callbackContext.error("unknown java method");
                return false;

        }

    }

    private String encrypt(JSONArray data) throws JSONException, IOException {

        String message = data.getString(0);
        String topic = data.getString(1);

	String encrypted = null;
        try {

	    Message m = new Message().secret(message);
            encrypted = riksKit.encryptMessage(m, topic);

        } catch (SymKeyExpiredException | CryptoException e) {
	    throw new IOException(e.getMessage());
        }
	
	return encrypted;
    }

    private void initRiks(JSONArray data) throws JSONException, IOException {

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

	try {
            Storage storage = new AndroidStorage(ps, this.cordova.getActivity());
            riksKit = new RiksKit(deviceId, password, ps, storage, new Whitelist());
        } catch (Exception e) {
	    throw new IOException(e.getMessage());
        }
        //return deviceId + configPath + password;
        //return testPassword;
	return;

    }


}
