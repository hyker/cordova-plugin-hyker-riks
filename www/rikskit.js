

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

function RiksKit (deviceID, password, allowedForKey, newKey) {

    if (typeof allowedForKey !== 'function') throw 'allowedForKey must be a function'
    if (typeof newKey !== 'function') throw 'newKey must be a function'

    this.deviceId = '#' + deviceID;
    this.configPath = "www/development.conf";
    this.password = password;
    this.newKey = newKey;
    this.allowedForKey = allowedForKey;
    

    var onErr = function (err) {
	throw new Error(err);
    }



    cordova.exec(this.msgParse.bind(this), onErr, "CordovaRiksKit", "init", [this.deviceId, this.configPath, this.password]);

}


RiksKit.prototype.msgParse = function(msg){

    var json = JSON.parse(msg);
    var operation = json.operation;

    switch(operation) {
	case "INIT":
	    reallyDone();
	    break;
	case "NEW_KEY":
	    
	    break;
	case "ALLOWED":
	    
	    var uid = json.uid;
	    var namespace = json.namespace;
	    var keyid = json.keyid;

	    var boolAllow = this.allowedForKey(uid, namespace, keyid);
	    var allow = boolAllow ? "true" : "false";

	    cordova.exec("", "", "CordovaRiksKit", "keyconf", [uid, namespace, keyid,allow]);
	    break;
	default:
	    throw new Error(err);
	    break;
    }
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
