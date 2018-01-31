import RiksKit

@objc(CordovaRiksKit) class CordovaRiksKit : CDVPlugin {
    
    var OPERATION_INIT = "INIT"
    var OPERATION_NEW_KEY = "NEW_KEY"
    var OPERATION_ALLOWED = "ALLOWED"
    
    var longTermCallbackId: String?
    var riksKit: RiksKit?
    
    var pending: Dictionary<String, ((Bool) -> Void)>
    
    override init() {
        pending = [String: ((Bool) -> Void)]()
    }
    
    func sendCallbackAndKeepRef(message: String) {
        
        print("sendCallbackAndKeepRef: " + longTermCallbackId!)
        
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: message
        )
        
        pluginResult?.setKeepCallbackAs(true)
        
        self.commandDelegate!.send(
            pluginResult,
            callbackId: longTermCallbackId
        )
    }

    @objc(init:)
    func init_(command: CDVInvokedUrlCommand) {
        
        print("init!!")
        
        if (riksKit != nil) {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Cannot instantiate twice."), callbackId: command.callbackId)
        } else {
            
            let uid = command.arguments[0] as! String
            let password = command.arguments[1] as! String
            var config = command.arguments[2] as! [String: Any]
            
            if let path = config["storage_path"] as? String {
                
                let lib = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true)[0]
                let url = NSURL(fileURLWithPath: lib).appendingPathComponent(path)
                
                config["storage_path"] = url?.path
                
                print(url?.path)
            }
    
            //let config = [String: Any]()
            
            let path = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true)[0]
            
            print("Hello, World: " + path)
    
            /*
            riksKit = RiksKit(uid: uid, password: password,
                              allowedForKey: { (id: String, ns: String, keyId: String, callback: ((Bool) -> Void)) -> Void in

                                //self.pending[id + ns + keyId] = callback
                                
                                self.sendCallbackAndKeepRef(message: String(format: "{\"operation\": \"%@\", \"uid\": \"%@\", \"keySpace\": \"%@\", \"keyID\": \"%@\"}", self.OPERATION_ALLOWED, id, ns, keyId))
            },
                              newKey: { (id: String, ns: String) -> Void in
                                self.sendCallbackAndKeepRef(message: String(format: "{\"operation\": \"%@\", \"keyID\": \"%@\"}", self.OPERATION_NEW_KEY, id))
            }, config: config)
 
            */
            longTermCallbackId = command.callbackId
            sendCallbackAndKeepRef(message: String(format: "{\"operation\": \"%@\"}", OPERATION_INIT))
        }
    }
    
    @objc(encrypt:)
    func encrypt(command: CDVInvokedUrlCommand) {
        //let msg = command.arguments[0] as? String ?? "asap"
        
        let string = command.arguments[0] as! String
        let keySpace = command.arguments[1] as! String
        let data = string.data(using: String.Encoding.utf8)
        
        riksKit!.encrypt(secretData: data!, immutableData: Data(), mutableData: Data(), messageNamespace: keySpace,
                         callback: { (data: Data) -> Void in
                            
                            let string = String(data: data, encoding: String.Encoding.utf8) as String!
                            
                            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: string), callbackId: command.callbackId)
        },
                         errorCallback: { (error: String) -> Void in
                            
                            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error), callbackId: command.callbackId)
        })
    }
    
    @objc(decrypt:)
    func decrypt(command: CDVInvokedUrlCommand) {
        
        let string = command.arguments[0] as! String
        let data = string.data(using: String.Encoding.utf8)
    
        riksKit!.resetReplayProtector(messageNamespace: nil)
        
        riksKit!.decrypt(data: data!,
                         callback: { (secretData: Data?, immutableData: Data?, mutableData: Data?) -> Void in
                            
                            let string = String(data: secretData!, encoding: String.Encoding.utf8) as String!
                            
                            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: string), callbackId: command.callbackId)
        },
                         errorCallback: { (error: String) -> Void in
                            
                            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error), callbackId: command.callbackId)
        })
    }
    
    @objc(preshare:)
    func preshare(command: CDVInvokedUrlCommand) {
        
        let recipientUID = command.arguments[0] as! String
        let keyID = command.arguments[1] as! String
        
        riksKit!.preshareKey(recipientUID: recipientUID, keyID: keyID)
    
        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
    
    @objc(preshareKeyspace:)
    func preshareKeyspace(command: CDVInvokedUrlCommand) {
    
        let recipientUID = command.arguments[0] as! String
        let keySpace = command.arguments[1] as! String
    
        riksKit!.preshareKeyspace(recipientUID: recipientUID, messageNamespace: keySpace)
    
        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
    
    @objc(rekey:)
    func rekey(command: CDVInvokedUrlCommand) {
        
        let messageNamespace = command.arguments[0] as! String
        
        riksKit!.rekey(messageNamespace: messageNamespace)
    
        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
    
    @objc(resetReplayProtector:)
    func resetReplayProtector(command: CDVInvokedUrlCommand) {
        
        riksKit!.resetReplayProtector(messageNamespace: nil)
        
        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
    
    @objc(resolveWhitelist:)
    func resolveWhitelist(command: CDVInvokedUrlCommand) {
        let uid = command.arguments[0] as! String
        let keySpace = command.arguments[1] as! String
        let keyID = command.arguments[2] as! String
        let status = command.arguments[3] as! String
        
        let key = uid + keySpace + keyID

        if let val = pending[key] {
            val(status == "true")
            pending.removeValue(forKey: key)
        }
    
        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
}


