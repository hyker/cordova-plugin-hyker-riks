

var backlog = [];
var initialized = false;

var ensureInitialized = function (f) { 
    initialized ? f() : backlog.push(f)
}

function RiksKit (deviceID, password, allowedForKey, newKey) {

    this.deviceId = '#' + deviceID;
    this.configPath = "www/development.conf";
    this.password = password;
    

    var onErr = function (err) {
	throw new Error(err);
    }

    var reallyDone = function () {

	for (var i = 0; i < backlog.length; i++) {
	    backlog[i]();
	    initialized = true;
	}
    }



    cordova.exec(reallyDone, onErr, "CordovaRiksKit", "init", [this.deviceId, this.configPath, this.password]);

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
