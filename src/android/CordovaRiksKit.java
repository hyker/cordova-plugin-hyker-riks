package io.hyker.plugin;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.HashMap;
import java.io.IOException;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.*;

import io.hyker.FileStorage;
import io.hyker.Future;
import io.hyker.Json;
import io.hyker.Storage;
import io.hyker.riks.RiksKit;
import io.hyker.riks.Message;
import io.hyker.riks.Whitelist;

public class CordovaRiksKit extends CordovaPlugin {

    private static final String OPERATION_INIT = "INIT";
    private static final String OPERATION_NEW_KEY = "NEW_KEY";
    private static final String OPERATION_ALLOWED = "ALLOWED";

    private final AtomicReference<RiksKit> atomicRiksKit = new AtomicReference<RiksKit>();
    private final HashMap<String, Future<Boolean>> pendingFutures = new HashMap<String, Future<Boolean>>();
    private CallbackContext longTermCallback;

    @Override
    public boolean execute(final String action, final JSONArray arguments, final CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "init": {
                    if (atomicRiksKit.get() != null) {
                        callbackContext.error("Cannot instantiate twice.");
                    } else {
                        String uid = arguments.getString(0);
                        String password = arguments.getString(1);
                        JSONObject config = arguments.getJSONObject(2);

                        initRiksKit(uid, password, config);

                        synchronized (atomicRiksKit) {
                            while (atomicRiksKit.get() == null) {
                                atomicRiksKit.wait();
                            }
                        }

                        this.longTermCallback = callbackContext;
                        sendCallbackAndKeepRef(String.format("{\"operation\": \"%s\"}", OPERATION_INIT));
                    }

                    break;
                }
                case "encrypt": {
                    String data = arguments.getString(0);
                    String keySpace = arguments.getString(0);

                    getRiksKit().encryptMessage(new Message(data.getBytes(StandardCharsets.UTF_8)), keySpace).then(new Consumer<byte[]>() {
                        @Override
                        public void accept(byte[] encryptedMessage) {
                            callbackContext.success(new String(encryptedMessage, StandardCharsets.UTF_8));
                        }
                    }).onError(new Consumer<Exception>() {
                        @Override
                        public void accept(Exception e) {
                            callbackContext.error(String.format("%s: %s", e.getClass().getCanonicalName(), e.getMessage()));
                        }
                    });

                    break;
                }
                case "decrypt": {
                    String data = arguments.getString(0);

                    getRiksKit().resetReplayProtector();

                    getRiksKit().decryptMessage(data.getBytes(StandardCharsets.UTF_8)).then(new Consumer<Message>() {
                        @Override
                        public void accept(Message message) {
                            callbackContext.success(new String(message.getSecretData(), StandardCharsets.UTF_8));
                        }
                    }).onError(new Consumer<Exception>() {
                        @Override
                        public void accept(Exception e) {
                            callbackContext.error(String.format("%s: %s", e.getClass().getCanonicalName(), e.getMessage()));
                        }
                    });

                    break;
                }
                case "preshare": {
                    String recipientUID = arguments.getString(0);
                    String keyID = arguments.getString(1);

                    getRiksKit().preshareKey(recipientUID, keyID);

                    callbackContext.success();
                    break;
                }
                case "preshareKeyspace": {
                    String recipientUID = arguments.getString(0);
                    String keySpace = arguments.getString(1);

                    getRiksKit().preshareChannel(recipientUID, keySpace);

                    callbackContext.success();
                    break;
                }
                case "rekey": {
                    getRiksKit().rekey(arguments.getString(0));

                    callbackContext.success();
                    break;
                }
                case "resetReplayProtector": {
                    getRiksKit().resetReplayProtector();

                    callbackContext.success();
                    break;
                }
                case "resolveWhitelist": {
                    String uid = arguments.getString(0);
                    String keySpace = arguments.getString(1);
                    String keyID = arguments.getString(2);
                    String status = arguments.getString(3);

                    if (pendingFutures.containsKey(hash(uid, keySpace, keyID))) {
                        pendingFutures.remove(hash(uid, keySpace, keyID)).set(status.equals("true"));
                    }

                    callbackContext.success();
                    break;
                }
                default: {
                    callbackContext.error("Unknown Java method.");
                    return false;
                }
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

        return true;
    }

    private void sendCallbackAndKeepRef(String message) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, message);
        pluginResult.setKeepCallback(true);
        longTermCallback.sendPluginResult(pluginResult);
    }

    private RiksKit getRiksKit() throws InterruptedException {
        synchronized (atomicRiksKit) {
            while (atomicRiksKit.get() == null) {
                atomicRiksKit.wait();
            }
            return atomicRiksKit.get();
        }
    }

    private void initRiksKit(String uid, String password, JSONObject config) throws IOException {
        try {
            Storage storage = new FileStorage(this.cordova.getActivity().getFilesDir());
            RiksKit riksKit = new RiksKit(uid, password, initWhitelist(), Json.parse(config.toString()), storage);

            synchronized (atomicRiksKit) {
                atomicRiksKit.set(riksKit);
                atomicRiksKit.notifyAll();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }


    private Whitelist initWhitelist(){
        return new Whitelist() {
            @Override
            public Future<Boolean> allowedForKey(String uid, String keySpace, String keyID) {
                Future<Boolean> future = new Future<Boolean>();
                pendingFutures.put(hash(uid, keySpace, keyID), future);

                sendCallbackAndKeepRef(String.format("{\"operation\": \"%s\", \"uid\": \"%s\", \"keySpace\": \"%s\", \"keyID\": \"%s\"}", OPERATION_ALLOWED, uid, keySpace, keyID));

                return future;
            }

            @Override
            public void newKey(String keySpace, String keyID) {
                sendCallbackAndKeepRef(String.format("{\"operation\": \"%s\", \"keyID\": \"%s\"}", OPERATION_NEW_KEY, keyID));
            }
        };
    }

    public String hash(String... values) {
        long result = 17;
        for (String value : values) result = 37 * result + value.hashCode();
        return String.valueOf(result);
    }

}