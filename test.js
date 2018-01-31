var uid = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 11);
var password = "password";
var config = {
    ...
}

var allowedForKey = (uid, keySpace, keyID) => true
var newKey = () => {}

var rikskit = new RiksKit(uid, password, allowedForKey, newKey, config);
//var rikskit2 = new RiksKit(uid2, password, allowedForKey, newKey, config);

var data = "Hello world"
var keySpace = "Planet Earth"

rikskit.encrypt(data, ks).then((encryptedData) => {
    console.log("Encrypted: " + encryptedData)

    rikskit.decrypt(encryptedData).then((decryptedData) => {
        console.log("Decrypted: " + decryptedData)
        f()
    }).catch(console.error)

}).catch(console.error)

//(function f() {
//
//  rikskit.rekey(ks)
//  rikskit.preshareChannel(uid2, ks)
//  
//  console.log("Encrypting: " + data);
//   
//  rikskit.encrypt(data, ks).then((encryptedData) => {
//      console.log("Encrypted: " + encryptedData);
//
//      rikskit2.decrypt(encryptedData).then((decryptedData) => {
//          console.log("Decrypted: " + decryptedData);
//          f()
//      }).catch(console.error);
//
//  }).catch(console.error);
//})()
