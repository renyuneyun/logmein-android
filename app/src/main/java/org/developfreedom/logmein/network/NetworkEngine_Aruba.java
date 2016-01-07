package org.developfreedom.logmein.network;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

class NetworkEngine_Aruba extends NetworkEngine {
    /** The url where login request will be posted */
    public String BASE_URL = "https://securelogin.arubanetworks.com/cgi-bin/login";

    public NetworkEngine_Aruba(Context context) {
        super(context);
    }

    @Override
    protected StatusCode login_runner(String username, String password) throws Exception {
        if (username == null || password == null) {
            Log.wtf("Error", "Either username or password is null");
            return StatusCode.CREDENTIAL_NONE;
        }
        String urlParameters = "user=" + username + "&password=" + password; // "param1=a&param2=b&param3=c";

        String request = BASE_URL + "?cmd=login";
        URL puServerUrl = new URL(request);

        URLConnection puServerConnection = puServerUrl.openConnection();
        puServerConnection.setDoOutput(true);

        //FIXME: Handle protocol exception
        StatusCode returnStatus = null;
        //TODO: use try-with-resources
        try {
            OutputStream stream = puServerConnection.getOutputStream();
            //Output
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(urlParameters);
            writer.flush();

            String lineBuffer;
            try {
                BufferedReader htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
                try {
                    while (((lineBuffer = htmlBuffer.readLine()) != null) && returnStatus == null) {
                        if (lineBuffer.contains("External Welcome Page")) {
                            Log.d("NetworkEngine", "External Welcome Match");
                            returnStatus = StatusCode.LOGIN_SUCCESS;
                        } else if (lineBuffer.contains("Authentication failed")) {
                            returnStatus = StatusCode.AUTHENTICATION_FAILED;
                        } else if (lineBuffer.contains("Only one user login session is allowed")) {
                            returnStatus = StatusCode.MULTIPLE_SESSIONS;
                        } else {
                            Log.i("html", lineBuffer);
                        }
                    }
                }finally {
                    htmlBuffer.close();
                }
            }
            catch (java.net.ProtocolException e) {
                returnStatus = StatusCode.LOGGED_IN;
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                writer.close();
            }
        } catch (java.net.ConnectException e) {
            e.printStackTrace();
            Log.d("NetworkEngine", "Connection Exception");
            return StatusCode.CONNECTION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnStatus;
    }

    @Override
    protected StatusCode logout_runner() throws Exception {
        System.out.println("Loggin out");
        URL puServerUrl = new URL(BASE_URL+"?cmd=logout");
        URLConnection puServerConnection = puServerUrl.openConnection();

        StatusCode returnStatus = null;
        //TODO: use try-with-resources
        try {
            //Get inputStream and show output
            BufferedReader htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
            try {
                //TODO parse output
                String lineBuffer;
                while ((lineBuffer = htmlBuffer.readLine()) != null && returnStatus == null) {

                    if (lineBuffer.contains("Logout")) {
                        returnStatus = StatusCode.LOGOUT_SUCCESS;
                    } else if (lineBuffer.contains("User not logged in")) {
                        returnStatus = StatusCode.NOT_LOGGED_IN;
                    }
                    Log.w("html", lineBuffer);
                }
            }finally {
                htmlBuffer.close();
            }
        } catch (java.net.ConnectException e) {
            e.printStackTrace();
            Log.d("NetworkEngine", "Connection Exception");
            return StatusCode.CONNECTION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnStatus;
    }
}
