

var backlog = [];
var initialized = false;

var ensureInitialized = function (f) { 
    initialized ? f() : backlog.push(f)
}
var reallyDone = function () {

    for (var i = 0; i < backlog.length; i++) {
	backlog[i]();
	initialized = true;
    }
}


function msgParse(msg){

    console.log("msg parse: " + msg);
    var json = JSON.parse(msg);
    var operation = json.operation;

    switch(operation) {
	case "INIT":
	    console.log("init called msg pass");
	    reallyDone();
	    break;
	case "NEW_KEY":
	    console.log("new_key called msg pass");
	    
	    break;
	case "ALLOWED":
	    
	    console.log("allowed called msg pass");
	    break;
	default:
	    console.log("msg: " + json);
	    throw new Error(err);
	    break;
    }
}

function RiksKit (deviceID, password, allowedForKey, newKey) {

    this.deviceId = '#' + deviceID;
    this.configPath = "www/development.conf";
    this.password = password;
    this.newKey = newKey;
    

    var onErr = function (err) {
	throw new Error(err);
    }



    cordova.exec(msgParse, onErr, "CordovaRiksKit", "init", [this.deviceId, this.configPath, this.password]);

}

RiksKit.prototype.encrypt = function (data, namespace) {

    return new Promise(function (resolve, reject) {

	ensureInitialized(function () {

    	    cordova.exec(resolve, reject, "CordovaRiksKit", "encrypt", [data,namespace]);
	})
    })
}

RiksKit.prototype.decrypt = function (data) {
    return new Promise((resolve, reject) => {

	ensureInitialized(function () {

	    cordova.exec(resolve, reject, "CordovaRiksKit", "decrypt", [data]);

	})
    })
}

RiksKit.prototype.preshare = function (recipientUID, keyID) {
    return new Promise((resolve, reject) => {

	ensureInitialized(function () {
	    cordova.exec(resolve, reject, "CordovaRiksKit", "preshare", [recipientUID, keyID]);
	})

    })
}

RiksKit.prototype.rekey = function (namespace) {
    return new Promise((resolve, reject) => {
	console.log("rekey api called");

	ensureInitialized(function () {
	    cordova.exec(resolve, reject, "CordovaRiksKit", "rekey", [namespace]);
	})
    })
}

RiksKit.prototype.resetReplayProtector = function () {
    return new Promise((resolve, reject) => {
	ensureInitialized(function () {
	    cordova.exec(resolve, reject, "CordovaRiksKit", "resetall", []);
        })
    })
}

module.exports = RiksKit;
