# Cordova Plugin: HYKER RIKS

## How To Use In Project

    npm install -g cordova
    npm install -g taco-cli

    git clone git@github.com:hykersec/cordova-plugin-hyker-riks.git

    cordova create myprj com.example.myprj MyPrj

    cd myprj

    cordova platform add ios
    cordova platform add android@6.2.2

    *may need to apply patch*

    cordova plugin add cordova-plugin-add-swift-support --save
    #cordova plugin add cordova-plugin-cocoapod-support --save

    cordova plugin remove io.hyker.riks
    cordova plugin add ../cordova-plugin-hyker-riks

    open platforms/ios/HelloCordova.xcworkspace
      * COPY RiksKitiOS.framework and libriks.so to MyPrj root *
      HelloCordova > HelloCordova > Build Phases > Copy Files > Destination = Frameworks, name = [ RiksKitiOS.framework, libriks.dylib ]

    cordova run ios
    #taco run android --livereload

    PATCH:

    platforms/android/cordova/lib/emulator.js
      return superspawn.spawn('android', ['list', 'avd'])
        ->
      return superspawn.spawn('android', ['list', 'avds’])

    chmod +x /Applications/Android\ Studio\ 2.4\ Preview.app/Contents/gradle/gradle-3.4.1/bin/gradle

---
# Cordova Hello World Plugin

Simple plugin that returns your string prefixed with hello.

Greeting a user with "Hello, world" is something that could be done in JavaScript. This plugin provides a simple example demonstrating how Cordova plugins work.

## Using

Create a new Cordova Project

    $ cordova create hello com.example.helloapp Hello
    
Install the plugin

    $ cd hello
    $ cordova plugin add https://github.com/don/cordova-plugin-hello.git
    

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var success = function(message) {
        alert(message);
    }

    var failure = function() {
        alert("Error calling Hello Plugin");
    }

    hello.greet("World", success, failure);
```

Install iOS or Android platform

    cordova platform add ios
    cordova platform add android
    
Run the code

    cordova run 

## More Info

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/latest/guide/cli/index.html)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/index.html)
