package org.developfreedom.logmein.network;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class NetworkEngine_NWAFU extends NetworkEngine {
    static final long period = 600 * 1000;
    String host = "219.245.192.20";
    int port = 8080;
    HashMap<String, String> urls;
    HashMap<String, String> headers;
    HashMap<String, String> infos;
    HashMap<String, String> cfg = null;
    String cookie;
    AndroidHttpClient httpclient;
    TimerTask keep_online_task = null;
    Timer timer = null;

    public NetworkEngine_NWAFU(Context context) {
        super(context);
        urls = new HashMap<>();
        urls.put("base", "http://" + host + ":" + port);
        urls.put("portal", "/portal/");
        urls.put("appRoot", urls.get("base") + urls.get("portal"));
        urls.put("custompath", "templatePage/20150510015825585/");
        urls.put("pws:li", "/portal/pws?t=li");
        urls.put("doHeartBeat", "/portal/page/doHeartBeat.jsp");
        urls.put("tmpl:afterLogin", "/portal/page/afterLogin.jsp?pl=%s&custompath=" + urls.get("custompath") + "&uamInitCustom=%s&uamInitLogo=H3C&customCfg=%s2&loginType=3&v_is_selfLogin=0&byodserverip=%s&byodserverhttpport=%s");
        urls.put("tmpl:online", "/portal/page/online.jsp?st=2&pl=%s&custompath=" + urls.get("custompath") + "&uamInitCustom=%s&customCfg=%s&uamInitLogo=H3C&userName=null&userPwd=null&loginType=3&innerStr=null&outerStr=null&v_is_selfLogin=0");
        urls.put("tmpl:listenClose", "/portal/page/listenClose.jsp?pl=%s");
        urls.put("tmpl:online_showTimer", "/portal/" + urls.get("custompath") + "online_showTimer.jsp?hlo=null&pl=%s&startTime=%s&userName=null&userPwd=null&loginType=3&innerStr=null&outerStr=null&v_is_selfLogin=0&custompath=" + urls.get("custompath") + "&uamInitCustom=%s&uamInitLogo=H3C&customCfg=%s");
        urls.put("tmpl:online_heartBeat", "/portal/page/online_heartBeat.jsp?pl=%s&custompath=" + urls.get("custompath") + "&uamInitCustom=%s&uamInitLogo=H3C");
        urls.put("tmpl:logout", "/portal/" + urls.get("custompath") + "logout.jsp?pl=%s&hlo=null&customCfg=%s&custompath=" + urls.get("custompath") + "&uamInitCustom=%s&uamInitLogo=H3C");
        urls.put("tmpl:pws:lo", "/portal/pws?t=lo&language=%s&userip=&basip=&_=%s");

        headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:42.0) Gecko/20100101 Firefox/42.0");
        headers.put("Accept", "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Encoding", "identity");

        infos = new HashMap<>();
    }

    @Override
    protected StatusCode login_runner(String username, String password) throws Exception {
        cfg = new HashMap<>();
        cfg.put("id_userName", username);
        cfg.put("userName", username);
        cfg.put("id_userPwd", password);
        cfg.put("userPwd", Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
        synchronized (this) {
            httpclient = AndroidHttpClient.newInstance(headers.get("User-Agent"));
            String data;
            JSONObject jsonObj;
            int err;
            data = getCookie();
            jsonObj = Utils.decode(data.substring(7));
            if ((err = jsonObj.getInt("errorNumber")) != 1) {
                throw new IllegalStateException(jsonObj.getString(jsonObj.getString("e_d")));
            }
            for (String str : new String[]{"clientLanguage", "iNodePwdNeedEncrypt", "assignIpType", "customCfg", "byodServerIp", "byodServerHttpPort", "uamInitCustom"}) {
                Log.d("JSON value", String.format("%s %s", str, jsonObj.getString(str)));
                infos.put(str, jsonObj.getString((str)));
            }
            data = new String(sendRequest(auth()), "utf-8");
            jsonObj = Utils.decode(data);
            if ((err = jsonObj.getInt("errorNumber")) != 1) {
                if (err == 7) {
                    return StatusCode.MULTIPLE_SESSIONS;
                } else {
                    throw new IllegalStateException(jsonObj.getString(jsonObj.getString("e_d")));
                }
            }
            for (String str : new String[]{"userDevPort", "userStatus", "serialNo", "clientLanguage", "portalLink"}) {
                Log.d("JSON value", String.format("%s %s", str, jsonObj.getString(str)));
                infos.put(str, jsonObj.getString((str)));
            }
            sendRequest(afterLogin());
            sendRequest(openPage__online());
            sendRequest(listenClose());
            sendRequest(online_showTimer());
            sendRequest(online_heartBeat());
            httpclient.close();
        }
        if (timer == null) {
            timer = new Timer("keep-online");
            keep_online_task = new TimerTask() {
                @Override
                public void run() {
                    keep_online();
                }
            };
            timer.scheduleAtFixedRate(keep_online_task, 0, period);
        }

        return StatusCode.LOGIN_SUCCESS;
    }

    @Override
    protected StatusCode logout_runner() throws Exception {
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                httpclient = AndroidHttpClient.newInstance(headers.get("User-Agent"));
                sendRequest(logout_stage1());
                sendRequest(logout_stage2());
                httpclient.close();
            }
        }
        return StatusCode.LOGOUT_SUCCESS;
    }

    public void keep_online() {
        synchronized (this) {
            httpclient = AndroidHttpClient.newInstance(headers.get("User-Agent"));
            try {
                sendRequest(doHeartBeat());
            } catch (Exception e) {
                e.printStackTrace();
            }
            httpclient.close();
        }
    }

    private byte[] sendRequest(AuthData d) throws Exception {
        Log.d("sendRequest", String.format("%s", d.procedure));
        int statusCode;
        HttpUriRequest request;
        HttpResponse response;
        if (d.method == "GET")
            request = new HttpGet(d.url);
        else {
            request = new HttpPost(d.url);
            if (d.body != null) {
                ((HttpPost) request).setEntity(new StringEntity(d.body));
            }
        }
        for (String key: headers.keySet()) {
            request.setHeader(key, headers.get(key));
        }
        for (String key: d.headers.keySet()) {
            request.setHeader(key, d.headers.get(key));
        }
        response = httpclient.execute(request);
        statusCode = response.getStatusLine().getStatusCode();
        Log.d("status code", String.format("status code:%d", statusCode));
        byte []data = null;
        if (statusCode == 200) {
            data = EntityUtils.toByteArray(response.getEntity());
        }
        return data;
    }

    private String getCookie() throws Exception {
        int statusCode;
        HttpGet request;
        HttpResponse response;
        request = new HttpGet(urls.get("base")+urls.get("portal"));
        for (String key: headers.keySet()) {
            request.setHeader(key, headers.get(key));
        }
        response = httpclient.execute(request);
        statusCode = response.getStatusLine().getStatusCode();
        Log.d("status code", String.format("status code:%d(%s)", statusCode, request.getURI()));
        if (response.containsHeader("Set-Cookie")) {
            cookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
        }
        urls.put("wp:login_custom", response.getFirstHeader("Location").getValue());
        request = new HttpGet(urls.get("wp:login_custom"));
        for (String key: headers.keySet()) {
            request.setHeader(key, headers.get(key));
        }
        response = httpclient.execute(request);
        statusCode = response.getStatusLine().getStatusCode();
        Log.d("status code", String.format("status code:%d(%s)", statusCode, request.getURI()));
        if (response.containsHeader("Set-Cookie")) {
            cookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
        }
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            Log.println(1, "body", result);
            Log.d(String.format("response body (%s)[%d/%d]", entity.getContentType().getValue(), result.length(), entity.getContentLength()), result);
        }
        return cookie;
    }

    private AuthData auth() throws Exception {
        String url;
        HashMap<String, String> headers = new HashMap<>();
        String body = null;

        url = urls.get("pws:li");

        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Referer", urls.get("base") + urls.get("wp:login_custom"));
        headers.put("Cookie", cookie);
        headers.put("Connection", "keep-alive");

        body = String.format("id_userName=%s&userName=%s&id_userPwd=%s&userPwd=%s", cfg.get("id_userName"), cfg.get("userName"), cfg.get("id_userPwd"), cfg.get("userPwd"));
        body += "&serviceTypeHIDE=&serviceType=&userurl=&userip=&basip=";
        body += String.format("&language=%s&usermac=null&wlannasid=&entrance=null", infos.get("clientLanguage"));
        body += "&portalProxyIP=" + host + "&portalProxyPort=50200";
        body += String.format("&dcPwdNeedEncrypt=%s&assignIpType=%s", infos.get("iNodePwdNeedEncrypt"), infos.get("assignIpType"));
        body += "&appRootUrl=" + URLEncoder.encode(urls.get("appRoot"), "utf-8");
        body += "&manualUrl=&manualUrlEncryptKey=";

        return new AuthData("POST", url, headers, body, "auth");
    }

    private AuthData afterLogin() {
        String url;
        HashMap<String, String> headers = new HashMap<>();

        url = String.format(urls.get("tmpl:afterLogin"), infos.get("portalLink"), infos.get("uamInitCustom"), infos.get("customCfg"), infos.get("byodServerIp"), infos.get("byodServerHttpPort"));
        headers.put("Cookie", cookie);
        headers.put("Connection", "keep-alive");
        headers.put("Referer", urls.get("base") + urls.get("wp:login_custom"));

        return new AuthData(url, headers, "afterLogin");
    }

    private AuthData openPage__online() {
        String url;
        HashMap<String, String> headers = new HashMap<>();

        url = String.format(urls.get("tmpl:online"), infos.get("portalLink"), infos.get("uamInitCustom"), infos.get("customCfg"));
        headers.put("Cookie", cookie);
        headers.put("Connection", "keep-alive");
        headers.put("Referer", String.format(urls.get("tmpl:afterLogin"), infos.get("portalLink"), infos.get("uamInitCustom"), infos.get("customCfg"), infos.get("byodServerIp"), infos.get("byodServerHttpPort")));

        return new AuthData(url, headers, "openPage__online");
    }

    private AuthData listenClose() {
        String url;
        HashMap<String, String> headers = new HashMap<>();

        url = String.format(urls.get("tmpl:listenClose"), infos.get("portalLink"));
        headers.put("Cookie", cookie);
        headers.put("Connection", "keep-alive");
        headers.put("Referer", String.format(urls.get("tmpl:online"), infos.get("portalLink"), infos.get("uamInitCustom"), infos.get("customCfg")));

        return new AuthData(url, headers, "listenClose");
    }

    private AuthData online_showTimer() {
        String url;
        HashMap<String, String> headers = new HashMap<>();

        url = String.format(urls.get("tmpl:online_showTimer"), infos.get("portalLink"), Utils.now(), infos.get("uamInitCustom"), infos.get("customCfg"));
        headers.put("Cookie", "hello1=" + cfg.get("id_userName")+ "; hello2=false; " + cookie);
        headers.put("Connection", "keep-alive");
        headers.put("Referer", String.format(urls.get("tmpl:online"), infos.get("portalLink"), infos.get("uamInitCustom"), infos.get("customCfg")));

        return new AuthData(url, headers, "online_showTimer");
    }

    private AuthData online_heartBeat() {
        String url;
        HashMap<String, String> headers = new HashMap<>();

        url = String.format(urls.get("tmpl:online_heartBeat"), infos.get("portalLink"), infos.get("uamInitCustom"));
        headers.put("Cookie", cookie);
        headers.put("Connection", "keep-alive");
        headers.put("Referer", String.format(urls.get("tmpl:online"), infos.get("portalLink"), infos.get("uamInitCustom"), infos.get("customCfg")));

        return new AuthData(url, headers, "online_heartBeat");
    }

    private AuthData doHeartBeat() {
        String url;
        HashMap<String, String> headers = new HashMap<>();
        String body;

        url = urls.get("doHeartBeat");
        headers.put("Cookie", cookie);
        headers.put("Connection", "keep-alive");
        headers.put("Referer", String.format(urls.get("tmpl:online_heartBeat"), infos.get("portalLink"), infos.get("uamInitCustom")));
        body = String.format("userip=&basip=&userDevPort=%s&userStatus=%s&serialNo=%s&language=%s&e_d=&t=hb", URLEncoder.encode(infos.get("userDevPort")), infos.get("userStatus"), infos.get("serialNo"), infos.get("clientLanguage"));

        return new AuthData("POST", url, headers, body, "doHeartBeat");
    }

    private AuthData logout_stage1() {
        String url;
        HashMap<String, String> headers = new HashMap<>();
        String body;

        url = String.format(urls.get("tmpl:logout"), infos.get("portalLink"), infos.get("customCfg"), infos.get("uamInitCustom"));
        headers.put("Cookie", "hello1=" + cfg.get("id_userName")+ "; hello2=false; " + cookie);
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Referer", String.format(urls.get("tmpl:online_showTimer"), infos.get("portalLink"), Utils.now(), infos.get("uamInitCustom"), infos.get("customCfg")));
        body = null;

        return new AuthData("POST", url, headers, body, "logout_stage1");
    }

    private AuthData logout_stage2() {
        String url;
        HashMap<String, String> headers = new HashMap<>();

        url = String.format(urls.get("tmpl:pws:lo"), infos.get("clientLanguage"), Utils.now());
        headers.put("Cookie", cookie);
        headers.put("Connection", "keep-alive");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Referer", String.format(urls.get("tmpl:logout"), infos.get("portalLink"), infos.get("customCfg"), infos.get("uamInitCustom")));

        return new AuthData(url, headers, "logout_stage2");
    }

    private class AuthData {
        String method;
        String url;
        HashMap<String, String> headers;
        String body;
        String procedure;
        AuthData(String url, HashMap<String, String> headers) {
            this("GET", url, headers, null);
        }
        AuthData(String url, HashMap<String, String> headers, String procedure) {
            this(url, headers);
            this.procedure = procedure;
        }
        AuthData(String method, String url, HashMap<String, String> headers, String body) {
            this.method = method;
            this.url = urls.get("base") + url;
            this.headers = headers;
            this.body = body;
        }
        AuthData(String method, String url, HashMap<String, String> headers, String body, String procedure) {
            this(method, url, headers, body);
            this.procedure = procedure;
        }
    }
}

class Utils {
    static String decorate(String data) {
        int len = data.length();
        int alen = (int) Math.ceil(len / 4.f) * 4;
        String trail = "";
        while (alen > len) {
            trail += "=";
            alen--;
        }
        data += trail;
        Log.d("decorate", String.format("%s", data));
        return data;
    }
    static JSONObject decode(String ticket) {
        ticket = decorate(ticket);
        JSONObject jsonObj = null;
        try {
            String data = URLDecoder.decode(new String(Base64.decode(ticket, Base64.DEFAULT), "utf-8"), "utf-8");
            Log.d("decoded data", data);
            jsonObj = new JSONObject(data);
        } catch (UnsupportedEncodingException e) {
            Log.wtf("WTF", "UTF-8 Unsupported");
        } catch (JSONException e) {
            Log.wtf("WTF", "");
        }
        return jsonObj;
    }
    static String now() {
        long n = System.currentTimeMillis();
        Log.d("now", String.valueOf(n));
        return String.valueOf(n);
    }
}
