cordova.define("honeywell-plugin-scanner.HoneywellScannerPlugin", function(require, exports, module) {
var exec = require('cordova/exec');

function HoneywellScannerPlugin(){};

module.exports = {
    listenForScans: function(success, failure){
        //exec(successFunction, errorFunction, PluginClassName, actionArgument, argumentsToAction)
        return exec(success, failure, "HoneywellScannerPlugin", "listenForScans", []);
    }
};


});
