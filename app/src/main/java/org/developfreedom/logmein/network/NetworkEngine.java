/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
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

package org.developfreedom.logmein.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.developfreedom.logmein.R;

/**
 * Network Engine is the main interface used to perform network tasks
 * like login, logout etc.
 * <p>
 */
public abstract class NetworkEngine {

    private static NetworkEngine instance = null;
    /**
     * A collection of various situations that might occur in Engine
     */
    public enum StatusCode {
        LOGIN_SUCCESS, AUTHENTICATION_FAILED, MULTIPLE_SESSIONS,
        CREDENTIAL_NONE, LOGOUT_SUCCESS, NOT_LOGGED_IN, LOGGED_IN,
        CONNECTION_ERROR,
    }
    Context m_context;

    public NetworkEngine(Context context) {
        m_context = context;
    }

    /**
     * Singleton method with lazy initialization.
     * Desired way to create/access the Engine object
     * @param context Context in which the notification and toasts will be displayed.
     * @param type Auth Type (see res/values/auth_types.xml)
     * @return Reference to singleton object of Engine
     */
    public static synchronized NetworkEngine getInstance(Context context, int type) {
        String engine_classname = "NetworkEngine_" + context.getResources().getStringArray(R.array.auth_type_array)[type];
        boolean create_new = false;
        if (instance == null) {
            create_new = true;
        } else if (! instance.getClass().getSimpleName().equals(engine_classname)) {
            create_new = true;
        }
        if (create_new) {
            switch (type) {
                case 0:
                    instance = new NetworkEngine_Aruba(context);
                    break;
                case 1:
                    instance = new NetworkEngine_NWAFU(context);
                    break;
            }
        }
        return instance;
    }

    /**
     * Start the login task in a background task with default/old context
     * @param username the data for username field
     * @param password the data for password field
     * @return enum StatusCode explaining the situation
     * @throws Exception
     */
    public StatusCode login(final String username, final String password) throws Exception {
        NetworkTask longRunningTask = new NetworkTask(m_context);
        longRunningTask.execute("login", username, password);
        return longRunningTask.return_status;
    }

    /**
     * Perform logout in a background thread
     * @return
     * @throws Exception
     */
    public NetworkEngine.StatusCode logout() throws Exception {
        NetworkTask longRunningTask = new NetworkTask(m_context);
        longRunningTask.execute("logout");
        return longRunningTask.return_status;
    }

    /**
     * TODO: check documentation
     * Attempts to login into the network using credentials provided as parameters
     * @param username
     * @param password
     * @return status of login attempt
     * @throws Exception
     */
    abstract protected StatusCode login_runner(String username, String password) throws Exception;

    /**
     * TODO: check documentation
     * Attempt logout request to network
     * @return
     * @throws Exception
     */
    abstract protected NetworkEngine.StatusCode logout_runner() throws Exception;

    /**
     * TODO: check documentation
     * Sets network status at current time in a string
     * @param status
     * @return status string
     */
    public String get_status_text(StatusCode status) {
        String outputText;    //To be shown in User Text Box
        if (status == NetworkEngine.StatusCode.LOGIN_SUCCESS) {
            outputText = "Login Successful";
        } else if (status == NetworkEngine.StatusCode.CREDENTIAL_NONE) {
            outputText = "Either username or password in empty";
        } else if (status == NetworkEngine.StatusCode.AUTHENTICATION_FAILED) {
            outputText = "Authentication Failed";
        } else if (status == NetworkEngine.StatusCode.MULTIPLE_SESSIONS) {
            outputText = "Only one user login session is allowed";
        } else if (status == NetworkEngine.StatusCode.LOGGED_IN) {
            outputText = "You're already logged in";
        } else if (status == NetworkEngine.StatusCode.CONNECTION_ERROR) {
            outputText = "There was a connection error";
        } else if (status == NetworkEngine.StatusCode.LOGOUT_SUCCESS) {
            outputText = "Logout Successful";
        } else if (status == NetworkEngine.StatusCode.NOT_LOGGED_IN) {
            outputText = "You're not logged in";
        } else if (status == null) {
            Log.d("NetworkEngine", "StatusCode was null in login");
            outputText = "Unable to perform the operation";
        } else {
            outputText = "Unknown operation status";
        }
        return outputText;
    }

    /**
     * NetworkTask is an AsyncTask that can run in background
     * and is capable of sending login and logout requests without
     * blocking the main thread.
     */
    public class NetworkTask extends AsyncTask<String, Void, StatusCode> {
        String username, password;
        StatusCode return_status;
        Context m_context;

        public NetworkTask(Context context) {
            m_context = context;
        }

        @Override
        protected StatusCode doInBackground(String... input_strings) {
            String operation = input_strings[0];
            try {
                if (operation.equals("login")) {
                    username = input_strings[1];
                    password = input_strings[2];
                    return_status = login_runner(username, password);
                } else if (operation.equals("logout")) {
                    return_status = logout_runner();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return return_status;
        }

        @Override
        protected void onPostExecute(NetworkEngine.StatusCode status) {
            Toast.makeText(
                    m_context,
                    get_status_text(status),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}

// vim: set ts=4 sw=4 tw=79 et :
