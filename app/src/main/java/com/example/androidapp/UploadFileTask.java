package com.example.androidapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UploadFileTask extends AsyncTask<String, Void, Boolean> {

    private Context context;
    private static final String TAG = "UploadFileTask";

    public UploadFileTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String filePath = params[0];
        String serverUrl = "http://10.0.2.2:5000/upload_audio";

        int serverResponseCode = 0;
        String serverResponseMessage = null;

        HttpURLConnection httpUrlConnection = null;
        DataOutputStream outputStream = null;
        FileInputStream fileInputStream = null;

        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            File file = new File(filePath);

            if (!file.exists()) {
                Log.e(TAG, "File not found: " + filePath);
                return false;
            }

            fileInputStream = new FileInputStream(file);

            URL url = new URL(serverUrl);
            httpUrlConnection = (HttpURLConnection) url.openConnection();

            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(httpUrlConnection.getOutputStream());

            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + lineEnd);
            //outputStream.writeBytes("Content-Type: " + "audio/3gp" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            serverResponseCode = httpUrlConnection.getResponseCode();
            serverResponseMessage = httpUrlConnection.getResponseMessage();

        } catch (MalformedURLException e) {
            Log.e(TAG, "Error uploading file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error uploading file: " + e.getMessage());
            return false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }

                if (fileInputStream != null) {
                    fileInputStream.close();
                }

                if (httpUrlConnection != null) {
                    httpUrlConnection.disconnect();
                }

            } catch (IOException e) {
                Log.e(TAG, "Error closing connections: " + e.getMessage());
                return false;
            }
        }

        if (serverResponseCode == 200) {
            Log.i(TAG, "File uploaded successfully: " + serverResponseMessage);
            return true;
        } else {
            Log.e(TAG, "Server returned non-OK response code: " + serverResponseCode);
            return false;
        }
    }
    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.i(TAG, "Audio file uploaded successfully.");
        } else {
            Log.e(TAG, "Error uploading audio file.");
        }
    }
}
