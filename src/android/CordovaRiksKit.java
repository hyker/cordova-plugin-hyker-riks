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
    //private static final CallbackContext longTermCallback

    private void sendCallbackAndKeepRef(CallbackContext cbc, String message) {
	
	    PluginResult plugRes = new PluginResult(PluginResult.Status.OK, message);
	    plugRes.setKeepCallback(true);
	    cbc.sendPluginResult(plugRes);
    }

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
				callbackContext.error("Error: " + e.getMessage());
				return true;
			    }
			}

                    } catch (IOException e) {

                        callbackContext.error(" Error: " + e.getMessage());
			return true;
                    }

                    callbackContext.error("initialized ");
		    //sendCallbackAndKeepRef(callbackContext, "initialized");
		    //sendCallbackAndKeepRef(callbackContext, "initialized2");
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

                String recipient = data.getString(0);
                String keyId = data.getString(1);

		try {
		    riksKit.get().preShareKey(recipient, keyId);
		} catch (Exception e) {
                    callbackContext.error("Keyshare error: " + e.getMessage());
		}

                callbackContext.success("preshare successful");

		return true;

            case "rekey":

		Log.d("ACTION", "action rekey");
		riksKit.get().rekey(data.getString(0));
                callbackContext.success("rekey successful");
		return true;

            case "reset":

		Log.d("ACTION", "action reset");
		riksKit.get().rekey(data.getString(0));
                callbackContext.success("reset successful");
	
		return true;

            case "resetall":

		Log.d("ACTION", "action reset all");
		riksKit.get().resetReplayProtector();
                callbackContext.success("reset all successful");
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

        Context context = this.cordova.getActivity().getApplicationContext(); 

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
            RiksKit rk = new RiksKit(deviceId, password, ps, storage, new Whitelist());

	    synchronized(riksKit){

	        riksKit.set(rk);
	        riksKit.notifyAll();
	    }

        } catch (Exception e) {
	    throw new IOException(e.getMessage());
        }
	return;

    }


}
