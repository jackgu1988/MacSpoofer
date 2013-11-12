/**
 * Copyright (C) 2012-2013 Iakovos Gurulian
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

/**
 * @author jack gurulian
 */
public class Spoofer extends Activity {

    private CmdRunner cmd = new CmdRunner();
    private boolean correctMac = false;
    private ArrayList<String> ifaces = new ArrayList<String>();
    private CheckBox checkBox;
    private CheckBox checkBox2;
    private TextView method;
    private EditText macField;
    private Spinner iface_list;
    private TextView current_mac;
    private Button randomBtn;
    private CheckBox understand;
    private AlertDialog.Builder warningDialog;
    private AlertDialog warnD;
    private AlertDialog.Builder resetDialog;
    private Button restoreBtn;
    private AlertDialog.Builder aboutDialog;
    private AlertDialog.Builder tipDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spoofer);

        macField = (EditText) findViewById(R.id.editText1);
        current_mac = (TextView) findViewById(R.id.current_mac);
        randomBtn = (Button) findViewById(R.id.Random);

        if (!cmd.checkRoot())
            alert("You do not seem to have a rooted device.\n Exiting...");

        else if (!cmd.checkBusybox())
            alert("You do not seem to have busybox installed.\n Exiting...");

        else if (!cmd.checkAccess())
            alert("You seem to have denied root access.\n Exiting...");

        else {
            cmd.getRoot();

            cmd.saveMac();

            checkBox = (CheckBox) findViewById(R.id.checkBox);
            checkBox2 = (CheckBox) findViewById(R.id.checkBox2);

            checkBox2.setEnabled(false);
            checkBox2.setTextColor(Color.GRAY);

            restoreBtn = (Button) findViewById(R.id.button);
            restoreBtn.setEnabled(false);

            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    if (!intf.getDisplayName().equals("lo"))
                        ifaces.add(intf.getDisplayName());
                }
            } catch (SocketException e) {
            }

            iface_list = (Spinner) findViewById(R.id.iface_selector);

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, ifaces);
            dataAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            iface_list.setAdapter(dataAdapter);

            checkWlan();

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

            final Button buttonRnd = (Button) findViewById(R.id.Random);
            buttonRnd.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    rndNum();
                }
            });
        }
    }

    private boolean checkWlan() {
        boolean exists = false;

        for (int i = 0; i < ifaces.size(); i++) {
            // Since wlan0 is a common name for the wireless interface
            if (ifaces.get(i).equals("wlan0")) {
                iface_list.setSelection(i);
                exists = true;
            }
        }

        return exists;
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

    /**
     * Alerts for simple errors that do not require the app to be closed with popup
     *
     * @param msg the message to be displayed
     */
    private void simpleAlert(String msg) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_spoofer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_about:
                aboutDialog();
                return true;
            case R.id.action_tip:
                tipDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void tipDialog() {
        tipDialog = new AlertDialog.Builder(this);

        tipDialog
                .setIcon(android.R.drawable.ic_dialog_info).setTitle("Tips")
                .setMessage("MacSpoofer v2.0")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }

    public void onClickCheck(View v) {
        if (checkBox.isChecked()) {
            if (checkWlan() && cmd.getMacDir() != null) {
                warningDialogue(getString(R.string.warningMsg));
                iface_list.setEnabled(false);
            } else
                noWlanDialog();
        } else {
            checkBox2.setEnabled(false);
            checkBox2.setTextColor(Color.GRAY);
            restoreBtn.setEnabled(false);
            iface_list.setEnabled(true);
        }
    }

    private void noWlanDialog() {

        AlertDialog.Builder noWlan = new AlertDialog.Builder(this);

        noWlan
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error!")
                .setMessage(getString(R.string.no_compat))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkBox.setChecked(false);
                    }
                }).show();
    }

    public void checkInput(View v) {
        String pattern = "^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$";

        if (!macField.getText().toString().matches(pattern)) {
            simpleAlert("Wrong format provided!");
        } else {
            changeMac();
        }
    }

    private void changeMac() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.disconnect();

        String textField = macField.getText().toString();

        cmd.changeMac(textField,
                String.valueOf(iface_list.getSelectedItem()), 0, false);

        String currentMac = cmd.getCurrentMac(String.valueOf(iface_list
                .getSelectedItem()));

        current_mac.setText("Current MAC: "
                + currentMac);

        if (!currentMac.trim().equals(textField.trim()))
            simpleAlert("The MAC address failed to change! Please try some different address.");
    }

    private void rndNum() {
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

    public String getMACAddress() {
        WifiManager wifiMan = (WifiManager) this.getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String macAddr = wifiInf.getMacAddress();
        return macAddr;
    }

    public void warningDialogue(String msg) {

        warningDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.WarningDialog));
        LayoutInflater warn = LayoutInflater.from(this);
        View warnLayout = warn.inflate(R.layout.checkbox, null);
        understand = (CheckBox) warnLayout.findViewById(R.id.understand);

        warningDialog
                .setView(warnLayout)
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Warning!!!")
                .setMessage(Html.fromHtml(msg))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (understand.isChecked()) {
                            checkBox2.setEnabled(true);
                            checkBox2.setTextColor(Color.WHITE);
                            restoreBtn.setEnabled(true);
                        } else {
                            checkBox.setChecked(false);
                            restoreBtn.setEnabled(false);
                        }
                        return;
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkBox.setChecked(false);
                        restoreBtn.setEnabled(false);
                        return;
                    }
                });

        warnD = warningDialog.create();
        warnD.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                warnD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        warnD.setCancelable(false);

        warnD.show();
    }

    public void enableOK(View v) {
        if (!understand.isChecked())
            warnD.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        else
            warnD.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
    }

    public void resetDefaults(View v) {

        resetDialog = new AlertDialog.Builder(this);

        resetDialog
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Restore")
                .setMessage(getString(R.string.restore_defaults))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }

    private void aboutDialog() {
        aboutDialog = new AlertDialog.Builder(this);

        aboutDialog
                .setIcon(android.R.drawable.ic_dialog_info).setTitle("About")
                .setMessage("MacSpoofer v2.0")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }
}