# Riks-Cordova

## Creating sample Cordova application

```
    npm install -g cordova

    cordova create myprj com.example.myprj MyPrj

    cd myprj

    cordova platform add ios
    cordova platform add android@6.2.2

    cordova plugin add cordova-plugin-add-swift-support --save
    # cordova plugin add cordova-plugin-cocoapod-support --save

    # cordova plugin remove io.hyker.riks
    cordova plugin add https://github.com/hykersec/cordova-plugin-hyker-riks

    # open platforms/ios/MyPrj.xcworkspace
      # * COPY RiksKitiOS.framework and libriks.so to MyPrj root *
      # HelloCordova > HelloCordova > Build Phases > Copy Files > Destination = Frameworks, name = [ RiksKitiOS.framework, libriks.dylib ]

    cordova run ios
    cordova run android
```

## How to use

```javascript
     onDeviceReady: function() {
        addEvent(document.querySelector('button'), 'click', () => {
            var uid = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 11);
            var password = "password";
            var config = {
                ...
            }
            
            var allowedForKey = (uid, keySpace, keyID) => true;
            var newKey = () => {}
            
            var rikskit = new RiksKit(uid, password, allowedForKey, newKey, config);
            
            var data = "Hello world";
            var keySpace = "Planet Earth";
            
            console.log("Encrypting: " + encryptedData);
            rikskit.encrypt(data, keySpace).then((encryptedData) => {
                console.log("Encrypted: " + encryptedData);
                rikskit.decrypt(encryptedData).then((decryptedData) => {
                    console.log("Decrypted: " + decryptedData);
                }.catch(console.error);
            }).catch(console.error);
        });
        
        this.receivedEvent('deviceready');
    }
```