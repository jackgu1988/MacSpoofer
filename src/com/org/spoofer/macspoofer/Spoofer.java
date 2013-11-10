/**
 * Copyright (C) 2012 Iakovos Gurulian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.org.spoofer.macspoofer;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author jack gurulian
 */
public class Spoofer extends Activity {

    private CmdRunner cmd = new CmdRunner();
    private boolean correctMac = false;
    private ArrayList<String> ifaces = new ArrayList<String>();
    private CheckBox checkBox;
    private CheckBox checkBox2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spoofer);

        final EditText macField = (EditText) findViewById(R.id.editText1);
        final TextView error = (TextView) findViewById(R.id.Error);
        final TextView current_mac = (TextView) findViewById(R.id.current_mac);

        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox2 = (CheckBox) findViewById(R.id.checkBox2);

        checkBox2.setEnabled(false);
        checkBox2.setTextColor(Color.GRAY);

        if (!cmd.checkRoot())
            alert("You do not seem to have a rooted device.\n Exiting...");

        if (!cmd.checkBusybox())
            alert("You do not seem to have busybox installed.\n Exiting...");

        if (cmd.checkRoot()) {
            cmd.getRoot();

            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    if (!intf.getDisplayName().equals("lo"))
                        ifaces.add(intf.getDisplayName());
                }
            } catch (SocketException e) {
            }

            final Spinner iface_list = (Spinner) findViewById(R.id.iface_selector);

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, ifaces);
            dataAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            iface_list.setAdapter(dataAdapter);

            for (int i = 0; i < ifaces.size(); i++) {
                // Since wlan0 is a common name for the wireless interface
                if (ifaces.get(i).equals("wlan0")) {
                    iface_list.setSelection(i);
                }
            }

            iface_list.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView,
                                           View selectedItemView, int position, long id) {
                    current_mac.setText("Current MAC: "
                            + cmd.getCurrentMac(String.valueOf(iface_list
                            .getSelectedItem())));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }

            });

            current_mac.setText("Current MAC: "
                    + cmd.getCurrentMac(String.valueOf(iface_list
                    .getSelectedItem())));

            macField.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                    String pattern = "^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$";

                    if (!macField.getText().toString().matches(pattern)) {
                        error.setText("Wrong format!");
                        correctMac = false;
                    } else {
                        error.setText("");
                        correctMac = true;
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            final Button buttonOK = (Button) findViewById(R.id.OK);
            buttonOK.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (correctMac) {
                        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wifi.disconnect();

                        cmd.changeMac(macField.getText().toString(),
                                String.valueOf(iface_list.getSelectedItem()));

                        current_mac.setText("Current MAC: "
                                + cmd.getCurrentMac(String.valueOf(iface_list
                                .getSelectedItem())));
                    }
                }
            });

            final Button buttonRnd = (Button) findViewById(R.id.Random);
            buttonRnd.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Random rand = new Random();
                    String result = "";
                    for (int i = 0; i < 6; i++) {
                        int myRandomNumber = rand.nextInt(0xff) + 0x00;

                        if (myRandomNumber <= 15)
                            result += "0";

                        result += Integer.toHexString(myRandomNumber);

                        if (i < 5)
                            result += ":";
                    }
                    macField.setText(result);
                }
            });
        }
    }

    /**
     * Alerts for critical errors (missing busybox/root) with popup
     *
     * @param msg the message to be displayed
     */
    private void alert(String msg) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_spoofer, menu);
        return true;
    }

    public void onClickCheck(View v) {
        if (checkBox.isChecked()) {
            checkBox2.setEnabled(true);
            checkBox2.setTextColor(Color.WHITE);
        } else {
            checkBox2.setEnabled(false);
            checkBox2.setTextColor(Color.GRAY);
        }
    }

}