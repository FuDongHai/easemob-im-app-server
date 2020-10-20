package com.easemob.qiniu.pili;

import okhttp3.*;

import java.net.URL;

/**
 * rpc for qiniu api
 */
final class RPC {
    private Mac mac;
    private OkHttpClient okHttpClient;

    public RPC(Mac mac) {
        this.mac = mac;
        this.okHttpClient = new OkHttpClient();
    }

    public Mac getMac() {
        return mac;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public String callWithJson(String urlStr, String json) throws Exception {
        String contentType = "application/json";
        URL url = new URL(urlStr);
        byte[] body = json.getBytes("UTF-8");
        String macToken = mac.signRequest(url, "POST", body, contentType);
        RequestBody rBody = RequestBody.create(MediaType.parse(contentType), body);
        Request request = new Request.Builder()
                .url(url)
                .post(rBody)
                .header("User-Agent", Config.APIUserAgent)
                .addHeader("Authorization", "Qiniu " + macToken)
                .addHeader("Content-Type", contentType)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        return responseHandler(response);
    }

    public String callWithGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        String macToken = mac.signRequest(url, "GET", null, null);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", Config.APIUserAgent)
                .addHeader("Authorization", "Qiniu " + macToken)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        return responseHandler(response);
    }

    public String callWithDelete(String urlStr) throws Exception{
        URL url = new URL(urlStr);
        String macToken = mac.signRequest(url, "DELETE", null,null);
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .header("User-Agent", Config.APIUserAgent)
                .addHeader("Authorization", "Qiniu " + macToken)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        return responseHandler(response);
    }

    private String responseHandler(Response response) throws Exception {

        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            return responseBody.string();
        } else {
            if (response.body() != null) {
                response.body().close();
            }
            throw new PiliException(response);
        }
    }

}
