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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
    private ArrayList<String> ifaces = new ArrayList<String>();
    private CheckBox checkBox;
    private CheckBox checkBox2;
    private EditText macField;
    private Spinner iface_list;
    private TextView current_mac;
    private CheckBox understand;
    private AlertDialog.Builder warningDialog;
    private AlertDialog warnD;
    private AlertDialog.Builder resetDialog;
    private Button restoreBtn;
    private AlertDialog.Builder aboutDialog;
    private AlertDialog.Builder tipDialog;
    private WifiManager wifi;
    private AlertDialog.Builder gplDialog;
    private String defaultInterface = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spoofer);

        wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        macField = (EditText) findViewById(R.id.editText1);
        current_mac = (TextView) findViewById(R.id.current_mac);

        if (!cmd.checkRoot())
            alert(getString(R.string.no_root));

        else if (!cmd.checkBusybox())
            alert(getString(R.string.no_busybox));

        else if (!cmd.checkAccess())
            alert(getString(R.string.no_permission));

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

            checkWlan(true);

            iface_list.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView,
                                           View selectedItemView, int position, long id) {
                    current_mac.setText(getString(R.string.current_mac)
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

    private boolean checkWlan(boolean set) {
        boolean exists = false;

        for (int i = 0; i < ifaces.size(); i++) {
            // Since wlan0 is a common name for the wireless interface
            if (ifaces.get(i).equals("wlan0") || ifaces.get(i).equals("ifb0")) {
                defaultInterface = ifaces.get(i);
                if (set)
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
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.error))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.error))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
            case R.id.menu_legal:
                gplDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void gplDialog() {
        gplDialog = new AlertDialog.Builder(this);

        gplDialog
                .setIcon(android.R.drawable.ic_dialog_info).setTitle(getString(R.string.license))
                .setMessage(getString(R.string.gpl))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }

    private void tipDialog() {
        tipDialog = new AlertDialog.Builder(this);

        tipDialog
                .setIcon(android.R.drawable.ic_dialog_info).setTitle(getString(R.string.tips))
                .setMessage(getString(R.string.tip))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }

    public void onClickCheck(View v) {
        if (checkBox.isChecked()) {
            if (checkWlan(false) && cmd.getMacDir() != null)
                warningDialogue(getString(R.string.warningMsg));
            else
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
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.error))
                .setMessage(getString(R.string.no_compat))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkBox.setChecked(false);
                    }
                }).show();
    }

    public void checkInput(View v) {
        String pattern = "^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$";

        if (!macField.getText().toString().matches(pattern)) {
            simpleAlert(getString(R.string.wrong_format));
        } else {
            changeMac();
        }
    }

    private void changeMac() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.disconnect();

        String textField = macField.getText().toString();

        if (!checkBox.isChecked()) {
            cmd.changeMac(textField,
                    String.valueOf(iface_list.getSelectedItem()), 0, wifi);

            String currentMac = cmd.getCurrentMac(String.valueOf(iface_list
                    .getSelectedItem()));

            current_mac.setText(getString(R.string.current_mac)
                    + currentMac);

            if (!currentMac.trim().equals(textField.trim()))
                simpleAlert(getString(R.string.spoof_failed));
        } else {
            if (!checkBox2.isChecked()) {
                cmd.changeMac(textField,
                        defaultInterface, 1, wifi);
                checkWlanUp(true);
            } else {
                cmd.changeMac(textField,
                        defaultInterface, 1, wifi);
                checkWlanUp(false);
            }
        }
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

    public void warningDialogue(String msg) {

        warningDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.WarningDialog));
        LayoutInflater warn = LayoutInflater.from(this);
        View warnLayout = warn.inflate(R.layout.checkbox, null);
        understand = (CheckBox) warnLayout.findViewById(R.id.understand);

        warningDialog
                .setView(warnLayout)
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.warning))
                .setMessage(Html.fromHtml(msg))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (understand.isChecked()) {
                            checkBox2.setEnabled(true);
                            checkBox2.setTextColor(Color.WHITE);
                            restoreBtn.setEnabled(true);
                            iface_list.setEnabled(false);
                            checkWlan(true);
                        } else {
                            checkBox.setChecked(false);
                            restoreBtn.setEnabled(false);
                        }
                        return;
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.restore_title))
                .setMessage(getString(R.string.restore_defaults))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cmd.getMacDir() != null) {
                            wifi.setWifiEnabled(false);
                            cmd.restoreMac();
                            wifi.setWifiEnabled(true);
                        }
                        checkWlanUp(false);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }

    private void aboutDialog() {
        aboutDialog = new AlertDialog.Builder(this);

        aboutDialog
                .setIcon(android.R.drawable.ic_dialog_info).setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }

    public void checkWlanUp(final boolean restoreMac) {

        final ProgressDialog progDialog = ProgressDialog.show(Spoofer.this, getString(R.string.wait),
                getString(R.string.apply_settings), true);

        new Thread() {
            @Override
            public void run() {

                int counter = 0;
                while (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED && counter < 36) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                }

                try {

                    // code runs in a thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String currentMac = cmd.getCurrentMac(String.valueOf(iface_list
                                    .getSelectedItem()));

                            current_mac.setText(getString(R.string.current_mac)
                                    + currentMac);

                            progDialog.dismiss();

                            if (restoreMac)
                                cmd.restoreMac();
                        }
                    });
                } catch (final Exception ex) {
                }
            }
        }.start();
    }

}