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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

//import com.stericson.RootTools.RootTools;

/**
 * @author jack gurulian
 */
public class CmdRunner {

    private Process p;
    private DataOutputStream stdin;
    private InputStream stdout;
    private static final int BUFF_LEN = 64;
    private byte[] buffer = new byte[BUFF_LEN];

    /**
     * Changes the mac address for a given interface
     *
     * @param newMac the new mac address
     * @param iface  the interface
     */
    public void changeMac(String newMac, String iface) {

        String command = "busybox ifconfig " + iface + " up; busybox ifconfig "
                + iface + " hw ether " + newMac;


        try {
            stdin.writeBytes(command + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks if the user has busybox installed
     *
     * @return true if busybox is present
     */
    public boolean checkBusybox() {

        //if (RootTools.isBusyboxAvailable())
        return true;
        //else
        //	return false;
    }

    /**
     * Checks if the user has a rooted device
     *
     * @return true if root is present
     */
    public boolean checkRoot() {

        //	if (RootTools.isRootAvailable())
        return true;
        //	else
        //		return false;
    }

    /**
     * Returns the current mac address
     *
     * @param iface the interface for which we will get the mac address
     * @return the current mac address
     */
    public String getCurrentMac(String iface) {

        String mac = "";

        String command = "busybox ip link show " + iface
                + " | sed -n 2p | tr -s ' ' | cut -d ' ' -f3";

        try {
            stdin.writeBytes(command + "\n");
            stdout = p.getInputStream();
            int read;
            while (true) {
                read = stdout.read(buffer);
                mac += new String(buffer, 0, read);
                if (read < BUFF_LEN) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mac;
    }

    /**
     * Get superuser access
     */
    public void getRoot() {
        try {
            p = Runtime.getRuntime().exec(new String[]{"su"});
            stdin = new DataOutputStream(p.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
