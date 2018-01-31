var callbacks = [];
var initialized = false;

var ensureInitialized = (callback) => { 
  initialized ? callback() : callbacks.push(callback)
}

var reallyDone = () => {
  initialized = true;
  for (var i = 0; i < callbacks.length; i++) {
    callbacks[i]();
  }
}

function RiksKit(uid, password, allowedForKey, newKey, config) {
  if (typeof uid !== 'string') throw 'uid must be a string'
  if (typeof password !== 'string') throw 'password must be a string'
  if (typeof allowedForKey !== 'function') throw 'allowedForKey must be a function'
  if (typeof newKey !== 'function') throw 'newKey must be a function'
  if (typeof config !== 'string' && typeof config !== 'object') throw 'config must be a filename or an object'

  this.uid = uid;
  this.password = password;
  this.allowedForKey = allowedForKey;
  this.newKey = newKey;
  this.config = config;

  var onError = (error) => {
    throw new Error(error);
  }

  cordova.exec(this.messageParse.bind(this), onError, "CordovaRiksKit", "init", [this.uid, this.password, this.config]);
}

RiksKit.prototype.messageParse = (json) => {
    var message = JSON.parse(json);

    switch (message.operation) {
      case "INIT":
        reallyDone();
        break;
      case "NEW_KEY":
        break;
      case "ALLOWED":
        var arguments = [
          message.uid,
          message.keySpace,
          message.keyID,
          this.allowedForKey(message.uid, message.keySpace, message.keyID) ? "true" : "false"
        ];

        cordova.exec("", "", "CordovaRiksKit", "resolveWhitelist", arguments);
        break;
      default:
        throw new Error('unknown response');
    }
}

RiksKit.prototype.encrypt = (data, keySpace) => {
  return new Promise((resolve, reject) => {
    ensureInitialized(() => {
      cordova.exec(resolve, reject, "CordovaRiksKit", "encrypt", [data, keySpace]);
    })
  })
}

RiksKit.prototype.decrypt = (data) => {
  return new Promise((resolve, reject) => { 
    ensureInitialized(() => { 
      cordova.exec(resolve, reject, "CordovaRiksKit", "decrypt", [data]);
    })
  })
}

RiksKit.prototype.preshare = (recipientUID, keyID) => {
  return new Promise((resolve, reject) => {
    ensureInitialized(() => {
      cordova.exec(resolve, reject, "CordovaRiksKit", "preshare", [recipientUID, keyID]);
    })
  })
}

RiksKit.prototype.preshareKeyspace = (recipientUID, keySpace) => {
  return new Promise((resolve, reject) => {
    ensureInitialized(() => {
      cordova.exec(resolve, reject, "CordovaRiksKit", "preshareKeyspace", [recipientUID, keySpace]);
    })
  })
}

RiksKit.prototype.rekey = (keySpace) => {
  return new Promise((resolve, reject) => {
    ensureInitialized(() => {
      cordova.exec(resolve, reject, "CordovaRiksKit", "rekey", [keySpace]);
    })
  })
}

RiksKit.prototype.resetReplayProtector = () => {
  return new Promise((resolve, reject) => {
    ensureInitialized(() => {
      cordova.exec(resolve, reject, "CordovaRiksKit", "resetReplayProtector", []);
    })
  })
}

module.exports = RiksKit;
