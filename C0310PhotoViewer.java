package com.sarriaroman.PhotoViewer;

import android.content.Intent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/* renamed from: com.sarriaroman.PhotoViewer.PhotoViewer */
public class C0310PhotoViewer extends CordovaPlugin {
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final String READ = "android.permission.READ_EXTERNAL_STORAGE";
    public static final int REQ_CODE = 0;
    public static final String WRITE = "android.permission.WRITE_EXTERNAL_STORAGE";
    protected JSONArray args;
    protected CallbackContext callbackContext;

    public boolean execute(String action, JSONArray args2, CallbackContext callbackContext2) throws JSONException {
        if (!action.equals("show")) {
            return false;
        }
        this.args = args2;
        this.callbackContext = callbackContext2;
        boolean requiresExternalPermission = true;
        try {
            requiresExternalPermission = this.args.getBoolean(2);
        } catch (JSONException e) {
        }
        if (!requiresExternalPermission || (this.f42cordova.hasPermission(READ) && this.f42cordova.hasPermission(WRITE))) {
            launchActivity();
        } else {
            getPermission();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void getPermission() {
        this.f42cordova.requestPermissions(this, 0, new String[]{WRITE, READ});
    }

    /* access modifiers changed from: protected */
    public void launchActivity() throws JSONException {
        Intent i = new Intent(this.f42cordova.getActivity(), PhotoActivity.class);
        PhotoActivity.mArgs = this.args;
        this.f42cordova.getActivity().startActivity(i);
        this.callbackContext.success("");
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == -1) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, 20));
                return;
            }
        }
        switch (requestCode) {
            case 0:
                launchActivity();
                return;
            default:
                return;
        }
    }
}
