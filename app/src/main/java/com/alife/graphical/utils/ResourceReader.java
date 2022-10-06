package com.alife.graphical.utils;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceReader {

    public static String ReadRawFileContent(AppCompatActivity activity, String fileName) {
        InputStream inputStream;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;
        String ret;

        try {
            inputStream = activity.getResources().openRawResource(
                    activity.getResources().getIdentifier(fileName, "raw", activity.getPackageName())
            );
            bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));
            stringBuilder = new StringBuilder();
            for (String line; (line = bufferedReader.readLine()) != null;) {
                stringBuilder.append(line).append('\n');
            }
            ret = stringBuilder.toString();

            inputStream.close();
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            ret = "";
        }

        return ret;
    }
}
