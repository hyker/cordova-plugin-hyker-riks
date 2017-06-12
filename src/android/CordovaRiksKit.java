package io.hyker.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import android.content.Context;
import java.util.concurrent.atomic.AtomicReference;

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
import android.util.Log;

public class CordovaRiksKit extends CordovaPlugin {
    

    //private static RiksKit riksKit = null;

    private static final AtomicReference<RiksKit> riksKit = new AtomicReference<>();

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {

        switch (action) {
            case "init":

		Log.d("ACTION", "action init");

                if (riksKit.get() != null){

                    callbackContext.error("can not instantiate twice");

                    return true;

                } else {

		    try {

                        initRiks(data);
			synchronized (riksKit) {
			    try {
				while (riksKit.get() == null){
				    riksKit.wait();
				}
			    } catch (InterruptedException e) {
				callbackContext.error(" Error: " + e.getMessage());
				return true;
			    }
			}

                    } catch (IOException e) {

                        callbackContext.error(" Error: " + e.getMessage());
			return true;
                    }

                    callbackContext.success("riks intitialized");
                    return true;
                }


            case "encrypt":

		Log.d("ACTION", "action encrypt");

		String encrypted = null;

        	try {
	            synchronized (riksKit) {
			try {
			    while (riksKit.get() == null){
				riksKit.wait();
			    }
			} catch (InterruptedException e) {
			    callbackContext.error(" Error: " + e.getMessage());
			    return true;
			}
		    }

                    encrypted = encrypt(data);

                } catch (IOException e) {
                    callbackContext.error(" Error: " + e.getMessage());
		    return true;
                }

                callbackContext.success(encrypted);
                return true;
	    
            case "decrypt":

		Log.d("ACTION", "action decrypt");

                String enc = data.getString(0);

	        synchronized (riksKit) {
		    try {
			while (riksKit.get() == null){
			    riksKit.wait();
			}
		    } catch (InterruptedException e) {
			callbackContext.error(" Error: " + e.getMessage());
			return true;
		    }
		}

		riksKit.get().decryptMessageAsync(enc, new RiksKit.DecryptionCallback() {
                    @Override
                    public void callback(Message m, Exception e) {
		    
			if (e != null){
                    	    callbackContext.error("Decrypt error: " + e.getMessage());
		    	} else {
            	    	    String decryptedMessage = m.secret;
                    	    callbackContext.success(decryptedMessage);
		    	}
		    }
                });

                return true;

            case "preshare":

		Log.d("ACTION", "action preshare");
		return true;

            case "rekey":

		Log.d("ACTION", "action rekey");
		return true;

            case "reset":

		Log.d("ACTION", "action reset");
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
            encrypted = riksKit.get().encryptMessage(m, topic);

        } catch (SymKeyExpiredException | CryptoException e) {
	    throw new IOException(e.getMessage());
        }
	
	return encrypted;
    }

    private void initRiks(JSONArray data) throws JSONException, IOException {

	Log.d("ACTION", "action riks new 1");
        Context context = this.cordova.getActivity().getApplicationContext(); 

	Log.d("ACTION", "action riks new 2");
	//these are correct
        String deviceId = data.getString(0);
        String configPath = data.getString(1);
        String password = data.getString(2);

	Log.d("ACTION", "action riks new 3");

	PropertyStore ps = null;
	Log.d("ACTION", "action riks new 4");
	InputStream is = null;
	Log.d("ACTION", "action riks new 5");
	is = context.getAssets().open(configPath);
	Log.d("ACTION", "action riks new 6");
	Properties properties = new Properties();
	Log.d("ACTION", "action riks new 7");
	properties.load(is);
	Log.d("ACTION", "action riks new 8");
	ps = new PropertyStore(properties);
	Log.d("ACTION", "action riks new 9");
	String testPassword = ps.TRUST_STORE_PASSWORD;

	Log.d("ACTION", "action riks new 10");
	try {

	Log.d("ACTION", "action riks new 11");
            Storage storage = new AndroidStorage(ps, this.cordova.getActivity());
	Log.d("ACTION", "action riks new 12");
            RiksKit rk = new RiksKit(deviceId, password, ps, storage, new Whitelist());
	Log.d("ACTION", "action riks new 13");

	    synchronized(riksKit){
	Log.d("ACTION", "action riks new 14");

	        riksKit.set(rk);
	Log.d("ACTION", "action riks new 15");
	        riksKit.notifyAll();
	Log.d("ACTION", "action riks new 16");
	    }

        } catch (Exception e) {
	Log.d("ACTION", "action riks new 17");
	    throw new IOException(e.getMessage());
        }
	Log.d("ACTION", "action riks new 18");
	return;

    }


}
