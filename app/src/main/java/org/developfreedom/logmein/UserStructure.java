/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *   Copyright (c) 2014 Tanjot Kaur <tanjot28@gmail.com>
 *
 *   This file is part of LogMeIn.
 *
 *   LogMeIn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LogMeIn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LogMeIn.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.developfreedom.logmein;

import java.io.Serializable;

/**
 * DataStructure used to pass user information safely
 * b/w functions and classes.
 */
public class UserStructure implements Serializable {
    String username;
    String password;
    int authType;

    public UserStructure() {}

    public UserStructure(String un, String pwd, int at) {
        username = un;
        password = pwd;
        authType = at;
    }

    /**
     * Get this user's username
     * @return
     */
    public String getUsername() {
        return (username);
    }//end of getUsername()

    /**
     * Set this user's username to un
     * @param un
     */
    public void setUsername(String un) {
        username = un;
    }//end of setUsername(String)

    /**
     * Get this user's password
     * @return
     */
    public String getPassword() {
        return (password);
    }//end of getPassword()

    /**
     * Set this user's password to pwd
     * @param pwd
     */
    public void setPassword(String pwd) {
        password = pwd;
    }//end of set_password(String)

    public int getAuthType() {
        return authType;
    }

    public void setAuthType(int authType) {
        this.authType = authType;
    }
}
// vim: set ts=4 sw=4 tw=79 et :
