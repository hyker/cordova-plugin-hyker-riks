import RiksKitiOS

@objc(RiksKit) class RiksKit : CDVPlugin {
  @objc(greet:)
  func greet(command: CDVInvokedUrlCommand) {
    var pluginResult = CDVPluginResult(
      status: CDVCommandStatus_ERROR
    )

    //let riksKit = RiksKitImpl(id: "id", password: "password",  allowedForKey: { (id: String, ns: String, keyId: String) -> Bool in
    //    return true;
    //})

    //let s = riksKit.encrypt("jonas hallin")

    //print("Hello, World: " + s)

    let msg = command.arguments[0] as? String ?? "asap"

    if msg.characters.count > 0 {
      /* UIAlertController is iOS 8 or newer only. */
      let toastController: UIAlertController = 
        UIAlertController(
          title: "", 
          message: msg, 
          preferredStyle: .Alert
        )

      self.viewController?.presentViewController(
        toastController, 
        animated: true, 
        completion: nil
      )

      let duration = Double(NSEC_PER_SEC) * 3.0

      dispatch_after(
        dispatch_time(
          DISPATCH_TIME_NOW, 
          Int64(duration)
        ), 
        dispatch_get_main_queue(), 
        { 
          toastController.dismissViewControllerAnimated(
            true, 
            completion: nil
          )
        }
      )

      pluginResult = CDVPluginResult(
        status: CDVCommandStatus_OK,
        messageAsString: msg
      )
    }

    self.commandDelegate!.sendPluginResult(
      pluginResult, 
      callbackId: command.callbackId
    )
  }
}
