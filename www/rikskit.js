/*global cordova, module*/

module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CordovaRiksKit", "greet", [name]);
    }
};

/// RIKS KIT API SKELETON ///

//function RiksKit(id, password, allowedForKey, newKey, file, config) {
//    if (typeof allowedForKey !== 'function')
//      throw 'must be a function: arg 4 allowedForKey'
//
//    file = file || path.join(os.homedir(), '.rikskit')
//
//    if (!fileWritable(file))
//      throw 'file not writable: ' + file
//
//    this.config = config || 'default.config'
//
//    newKey = newKey || function() {
//      this.riksKit.save(file, password)
//    }.bind(this)
//
//    //this.riksKit = riks.load(file, password, allowedForKey, newKey) ||
//    //  new riks.NodeRiksKit(id, password, config, allowedForKey, newKey);
//
//    this.riksKit = {
//      encrypt: function (values, namespace, resolve) {
//        setTimeout(function () {
//          resolve(Buffer.from(JSON.stringify(values)).toString('base64'))
//        }, 500)
//      },
//      decrypt: function (ciphertext, namespace, resolve) {
//        setTimeout(function () {
//          resolve(JSON.parse(Buffer.from(ciphertext, 'base64').toString('ascii')))
//        }, 500)
//      }
//    }
//}
//
//RiksKit.prototype.encrypt = function(values, namespace) {
//  return new Promise(function (resolve, reject) {
//    var success = function(ciphertext) {
//      resolve(ciphertext)
//    }
//
//    var failure = function(error) {
//      reject(error)
//    }
//
//    this.riksKit.encrypt(values, namespace, success, failure)
//  }.bind(this))
//}
//
//RiksKit.prototype.decrypt = function(ciphertext, namespace) {
//  return new Promise(function (resolve, reject) {
//    var success = function(values) {
//      resolve(values)
//    }
//
//    var failure = function(error) {
//      reject(error)
//    }
//
//    this.riksKit.decrypt(ciphertext, namespace, success, failure)
//  }.bind(this))
//}
//
//RiksKit.prototype.queryForKey = function () {
//  return this.riksKit.queryForKey()
//}
//
//RiksKit.prototype.rekey = function () {
//  return this.riksKit.rekey()
//}
//
//RiksKit.prototype.preshare = function (recipientId, keyId) {
//  return new Promise(function (resolve, reject) {
//    this.riksKit.preShareKey(recipientId, keyId, function(error) {
//      if (error)
//        reject()
//      else
//        resolve()
//    })
//  })
//}
//
//RiksKit.prototype.resetReplayProtector = function () {
//  return this.riksKit.resetReplayProtector()
//}
//
//RiksKit.prototype.save = function (filepath, password) {
//  return this.riksKit.save(filepath, password)
//}
//
//module.exports = RiksKit;
