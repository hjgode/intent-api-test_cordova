package com.tmdiwakara.cordova.honeywell;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
//import com.sap.smp.client.usage.p010db.DatabaseHelper;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class HoneywellScannerPlugin extends CordovaPlugin {
    /**
     * Honeywell DataCollection Intent API
     * Optional. Sets the scanner to claim. If scanner is not available or if extra is not used,
     * DataCollection will choose an available scanner.
     * Values : String
     * "dcs.scanner.imager" : Uses the internal scanner
     * "dcs.scanner.ring" : Uses the external ring scanner
     */
    private static final String EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER";
    private static final String ACTION_BARCODE_DATA = "com.honeywell.action.MY_BARCODE_DATA";
    private static final String ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER";
    private static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";
    private static final String EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE";
    private static final String EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES";
    
    private static final String TAG = "HoneywellScannerPlugin";
    private BroadcastReceiver barcodeDataReceiver;
    private CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova2, CordovaWebView webView) {
        Log.d(TAG, "initialize()...");

        super.initialize(cordova2, webView);
        this.barcodeDataReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive()..."+ intent.getStringExtra("data"));
                if (HoneywellScannerPlugin.ACTION_BARCODE_DATA.equals(intent.getAction())){//} && intent.getIntExtra(DatabaseHelper.SYSTEM_VERSION, 0) >= 1) {
                    HoneywellScannerPlugin.this.setScannedData(intent.getStringExtra("data"));
                }
            }
        };
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext2) throws JSONException {
        Log.d(TAG, "execute: "+action);

        if (action.equals("listenForScans")) {
            this.callbackContext = callbackContext2;
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
            getActivity().getApplicationContext().registerReceiver(this.barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
            claimScanner();
        }
        return true;
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "onResume()");
        super.onResume(multitasking);
        getActivity().getApplicationContext().registerReceiver(this.barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
        claimScanner();
    }

    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause()");
        super.onPause(multitasking);
        getActivity().getApplicationContext().unregisterReceiver(this.barcodeDataReceiver);
        releaseScanner();
    }

    private Activity getActivity() {
        return getActivity();
    }

    private void NotifyError(String error) {
        Log.d(TAG, "NotifyError: "+error);
        if (this.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, error);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
    }

    /* access modifiers changed from: private */
    public void setScannedData(String data) {
        Log.d(TAG, "Scanned: "+data);
        if (this.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
    }

    public void claimScanner() {
        Log.d("IntentApiSample: ", "claimScanner");
        Bundle properties = new Bundle();
        properties.putBoolean("DPR_DATA_INTENT", true);
        properties.putString("DPR_DATA_INTENT_ACTION", ACTION_BARCODE_DATA);

        properties.putInt("TRIG_AUTO_MODE_TIMEOUT", 2);
        properties.putString("TRIG_SCAN_MODE", "readOnRelease"); //This works for Hardware Trigger only! If scan is started from code, the code is responsible for a switching off the scanner before a decode

        mysendBroadcast(new Intent(ACTION_CLAIM_SCANNER)
                .setPackage("com.intermec.datacollectionservice")
                .putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                .putExtra(EXTRA_PROFILE, "DEFAULT")// "MyProfile1")
                .putExtra(EXTRA_PROPERTIES, properties)
        );
    }

    public void releaseScanner() {
        Log.d("IntentApiSample: ", "releaseScanner");
        mysendBroadcast(new Intent(ACTION_RELEASE_SCANNER)
                .setPackage("com.intermec.datacollectionservice")
        );
    }

    private static void sendImplicitBroadcast(Context ctxt, Intent i) {
        PackageManager pm = ctxt.getPackageManager();
        List<ResolveInfo> matches = pm.queryBroadcastReceivers(i, 0);
        if (matches.size() > 0) {
            for (ResolveInfo resolveInfo : matches) {
                Intent explicit = new Intent(i);
                ComponentName cn =
                        new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                                resolveInfo.activityInfo.name);

                explicit.setComponent(cn);
                ctxt.sendBroadcast(explicit);
            }

        } else{
            // to be compatible with Android 9 and later version for dynamic receiver
            ctxt.sendBroadcast(i);
        }
    }

    private  void mysendBroadcast(Intent intent){
        if(android.os.Build.VERSION.SDK_INT<26) {
            getActivity().getApplicationContext().sendBroadcast(intent);
        }else {
            //for Android O above "gives W/BroadcastQueue: Background execution not allowed: receiving Intent"
            //either set targetSDKversion to 25 or use implicit broadcast
            sendImplicitBroadcast(getActivity().getApplicationContext(), intent);
        }

    }
}