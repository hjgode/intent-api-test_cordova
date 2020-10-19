cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "honeywell-plugin-scanner.HoneywellScannerPlugin",
      "file": "plugins/honeywell-plugin-scanner/www/HoneywellScannerPlugin.js",
      "pluginId": "honeywell-plugin-scanner",
      "clobbers": [
        "window.plugins.HoneywellScannerPlugin"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.4",
    "honeywell-plugin-scanner": "0.0.3"
  };
});