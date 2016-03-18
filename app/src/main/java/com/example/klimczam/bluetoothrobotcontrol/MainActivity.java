package com.example.klimczam.bluetoothrobotcontrol;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import fr.iutvalence.android.BTConnectionHandlerLib.BTConnectionHandler;
import fr.iutvalence.android.BTConnectionHandlerLib.exceptions.BTHandlingException;


public class MainActivity extends ActionBarActivity {

    private BTConnectionHandler btConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.btConnection = new BTConnectionHandler(this);
        setContentView(R.layout.activity_main);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectDevice(View v) throws BTHandlingException, IOException {
        EditText str = (EditText) this.findViewById(R.id.device);
        String device = str.getText().toString();
        this.btConnection.connectToBTDevice(device);
    }

    public void sendData(View v) throws IOException, BTHandlingException {
        EditText text = (EditText) this.findViewById(R.id.textToSend);
        String textToSend = text.getText().toString();
        this.btConnection.sendData(textToSend);
    }

    public void controllerListener(View v) throws IOException, BTHandlingException {
        String str = null;
        switch (v.getId()) {
            case R.id.r:
                str = "r";
                break;
            case R.id.a:
                str = "a";
                break;
            case R.id.g:
                str = "g";
                break;
            case R.id.d:
                str = "d";
                break;
            case R.id.s:
                str = "s";
                break;
        }
        this.btConnection.sendData(str);
    }

    public void openJoystickActivity(View v){
        this.startActivity(new Intent(this, JoystickActivity.class));
    }

}
