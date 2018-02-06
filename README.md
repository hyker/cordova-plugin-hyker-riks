# Riks-Cordova

## Creating sample Cordova application

```
    npm install -g cordova

    cordova create myprj com.example.myprj MyPrj

    cd myprj

    cordova platform add ios
    cordova platform add android

    cordova plugin add cordova-plugin-add-swift-support --save

    cordova plugin remove cordova-plugin-hyker-riks
    cordova plugin add https://github.com/hykersec/cordova-plugin-hyker-riks

    open platforms/ios/MyPrj.xcworkspace
    COPY RiksKit.framework and libriks.dylib to MyPrj root
    Add the framework and dylib to "Embedded Binaries"
    
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
            
            console.log("Encrypting: " + data);
            rikskit.encrypt(data, keySpace).then((encryptedData) => {
                console.log("Encrypted: " + encryptedData);
                rikskit.decrypt(encryptedData).then((decryptedData) => {
                    console.log("Decrypted: " + decryptedData);
                }).catch(console.error);
            }).catch(console.error);
        });
        
        this.receivedEvent('deviceready');
    }
```

### Device 2 device

```javascript
var LOCAL = false

var app = {
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function() {
        this.receivedEvent('deviceready');
        
        var uid = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 11);
        var password = "password";
        
        var config = {
            "storage_path":                 "riks_data",
            "msg_host":                     "dev.msg.hykr.io",
            "msg_port":                     1443,
            "msg_api_key":                  "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
            "kds_host":                     "alpha.kds.hykr.io",
            "kds_port":                     8443,
            "kds_cache_expiration":         -1,
            "kds_api_key":                  "UNTRUSTED_API_KEY",
            "kds_root_certificate":         "-----BEGIN CERTIFICATE-----\r\nMIIBoDCCAUWgAwIBAgIJALJOHjjAY42bMAoGCCqGSM49BAMCMCwxKjAoBgNVBAMM\r\nIUhZS0VSX0RFVkVMT1BNRU5UX0NBX0RPX05PVF9UUlVTVDAeFw0xNzEwMDQxNDQw\r\nNTJaFw0yNzEwMDIxNDQwNTJaMCwxKjAoBgNVBAMMIUhZS0VSX0RFVkVMT1BNRU5U\r\nX0NBX0RPX05PVF9UUlVTVDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABG0jjTQs\r\nFCnTYOpRik3qIOuZkGvet9Z+3mzVhhhMdJ4Oc+v5+8dR8qqSskyq4YytO+H7dySq\r\nLxU4pG4L2rzqmUCjUDBOMB0GA1UdDgQWBBSpUu5tEEvb5NxolVI5jauQljSb8jAf\r\nBgNVHSMEGDAWgBSpUu5tEEvb5NxolVI5jauQljSb8jAMBgNVHRMEBTADAQH/MAoG\r\nCCqGSM49BAMCA0kAMEYCIQCNpMt38MCafJcbo1OPzhI9AAavCpUHJbDHW9+YLbvz\r\nwQIhAI0ZRPhOsGsNMWm0pDq91c7yx4jUcpsHZTjR80iof1EQ\r\n-----END CERTIFICATE-----",

            "replay_protector_window_size": 1000,
            "key_relay_enabled":            false
        }
        
        var allowedForKey = (uid, keySpace, keyID) => true;
        var newKey = () => {}
        
        var rikskit = new RiksKit(uid, password, allowedForKey, newKey, config);

        var data = "Hello world";
        var keySpace = "Planet Earth";

        if (LOCAL) {
            (function f() {
                 rikskit.rekey(keySpace)
                rikskit.preshareKeyspace(uid, keySpace)
                console.log("Encrypting: " + data);
                rikskit.encrypt(data, keySpace).then((encryptedData) => {
                    console.log("Encrypted: " + encryptedData);
                    rikskit.decrypt(encryptedData).then((decryptedData) => {
                        console.log("Decrypted: " + decryptedData);
                        f()
                    }).catch(console.error);
                }).catch(console.error);
             })()
        } else {
         
          var android = '8aa88e58-e185-4fb1-9fc7-fa9de6f236c2-3'
          var ios = 'eacf729a-7b1a-41bb-92a1-01465669129c-3'
          var isIOS = device.platform == 'iOS'
          
          var ws = new WebSocket("ws://192.168.0.99:1337")
          
          var uid = null, other = null
          
          if (isIOS) {
          uid = ios
          other = android
          } else {
          uid = android
          other = ios
          }
          
            if (isIOS) {
                setTimeout(function () {
                    rikskit.encrypt(data, keySpace).then((encryptedData) => {
                        console.log("Encrypted: " + encryptedData)
                        ws.send(encryptedData)
                        console.log('did send encrypted payload')
                    })
                    ws.send('ping')
                }, 5000)
            }
            
            ws.onmessage = function (msg) {
                console.log("trying to decrypt: " + msg.data)
                rikskit.decrypt(msg.data).then((decryptedData) => {
                    console.log("Decrypted: " + decryptedData)
                    rikskit.encrypt(data, keySpace).then((encryptedData) => {
                        console.log("Encrypted: " + encryptedData)
                        ws.send(encryptedData)
                    })
                })
            }
        }
    }
};

app.initialize();

```

#### Communication server
```js
// yarn init
// yarn add ws
async function main() {
  const WebSocket = require('ws')
  const wss = new WebSocket.Server({ host: '0.0.0.0', port: 1337 })
  var first = null
  var second = null
  wss.on('connection', function connection(ws) {
    console.log('connect!')
    if (!first) {
      first = ws
      ws.on('message', msg => {
        console.log(+new Date + ' 1')
        if (second)
          second.send(msg)
      })
    } else {
      second = ws
      ws.on('message', msg => {
        console.log(+new Date + ' 2')
        if (first)
          first.send(msg)
      })
    }
  })
}
main().catch(e => console.log(e))
```
