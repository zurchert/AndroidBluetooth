package com.example.klimczam.bluetoothrobotcontrol;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by klimczam on 18/03/16.
 */
public class JoystickActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(new JoystickView(this.getApplicationContext()));
    }
}
