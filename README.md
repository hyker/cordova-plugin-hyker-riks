# Cordova Plugin: HYKER RIKS

## How To Use In Project

    npm install -g cordova

    cordova create myprj com.example.myprj MyPrj

    cd myprj

    cordova platform add ios
    cordova platform add android@6.2.2

    cordova plugin add cordova-plugin-add-swift-support --save
    #cordova plugin add cordova-plugin-cocoapod-support --save

    cordova plugin remove io.hyker.riks
    cordova plugin add https://github.com/hykersec/cordova-plugin-hyker-riks

    open platforms/ios/MyPrj.xcworkspace
      * COPY RiksKitiOS.framework and libriks.so to MyPrj root *
      HelloCordova > HelloCordova > Build Phases > Copy Files > Destination = Frameworks, name = [ RiksKitiOS.framework, libriks.dylib ]

    cordova run ios
    cordova run android --livereload

## Example Application
    //this application registers a rikskit and send an encrypted message to itself
     onDeviceReady: function() {

	var button = document.querySelector('button')

        var cb = function(str){
	    console.log("cb: " + str);
            alert(str);
        }

        addEvent(button, 'click', function () {

	    var uid = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 11);

	    //function for filtering key requests
	    var allowedForKey = function(uid, namespace, keyID){
		//allow all keys
		return true;
	    }

	    //function notified when a new key is added
	    var newKey= function(){}

	    //initialize the RiksKit, and register pubkey online
            var rikskit = new RiksKit(uid, "password", allowedForKey, newKey);

	    var decrypt = function(str){
            	alert("Encrypted:" + str);
	        rikskit.decrypt(str).then(cb).then(rikskit.rekey("topiclol")).catch(cb);
	    }

            rikskit.encrypt("loolmsg", "topiclol").then(decrypt).catch(cb);


        })


