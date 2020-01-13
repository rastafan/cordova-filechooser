package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.provider.OpenableColumns;
import android.database.Cursor;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;

    public static final String MIME = "mime";
    public static final String ExtraMIME ="extraMIME";

    CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            JSONObject filters = inputs.optJSONObject(0);
            chooseFile(filters, callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(JSONObject filter, CallbackContext callbackContext) {
        String uri_filter = filter.has(MIME) ? filter.optString(MIME) : "*/*";
        boolean hasExtraMIME = filter.has(ExtraMIME) ? true : false;
        // type and title should be configurable

        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(uri_filter);
        if (hasExtraMIME) {
            String [] mimeTypes=filter.optString(ExtraMIME).split(",");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {

                    Log.w(TAG, uri.toString());
                    
                    //Recovering original file name
                    String fileName = null;
                    if (uri.getScheme().equals("content")) {
                        Cursor cursor = cordova.getActivity().getContentResolver().query(uri, null, null, null, null);
                        try {
                            if (cursor != null && cursor.moveToFirst()) {
                                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            }
                        } finally {
                            cursor.close();
                        }
                    }

                    if (fileName == null) {
                        fileName = uri.getPath();
                        int cut = fileName.lastIndexOf('/');
                        if (cut != -1) {
                            fileName = fileName.substring(cut + 1);
                        }
                    }

                    Log.w(TAG, "FILE NAME - " + fileName);

                    //Extracting file mime-type (since more than one can be passed with extraMIME)
                    String mimeType = cordova.getActivity().getContentResolver().getType(uri);
                    
                    //Response JSON
                    JSONObject response = new JSONObject();

                    try {
                        response.put("uri",uri.toString());
                        response.put("name",fileName);
                        response.put("mime",mimeType);
                    } catch (JSONException e) {
                        callback.error("Error: " + e.getMessage());
                        return;
                    }

                    Log.w(TAG, "FINAL RESPONSE - " + response.toString());

                    callback.success(response);

                } else {

                    callback.error("File uri was null");

                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // keep this string the same as in iOS document picker plugin
                // https://github.com/iampossible/Cordova-DocPicker
                callback.error("User canceled.");
            } else {

                callback.error(resultCode);
            }
        }
    }
}
