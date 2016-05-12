package com.evchargings.union;

import com.ning.http.client.ws.WebSocket;
import com.ning.http.client.ws.WebSocketTextListener;
import com.ning.http.client.ws.WebSocketUpgradeHandler;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ning.http.client.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.*;

/**
 * 昂朝动力提供的接口
 */
public class Union implements WebSocketTextListener {
    String httpUrl;
    String wsUrl;
    String appId;
    String appSecret;
    String authorization;
    UnionDriver unionDriver;
    WebSocket websocket;
    AsyncHttpClient asyncHttpClient;
    Cache<Integer, String> cityNameCache;
    ScheduledExecutorService pingService;
    private class JSON {
        String str;
        Object json;
        public JSON(String str) {
            this.str = str;
        }
        public JSONArray getJSONArray() {
            if (json == null)
                json = new JSONArray(str);
            return (JSONArray)json;
        }
        public JSONObject getJSONObject() {
            if (json == null)
                json = new JSONObject(str);
            return (JSONObject)json;
        }

    }
    private JSON getJSON(AsyncHttpClient.BoundRequestBuilder b) throws UnionException {
        Future<Response> f = b.addHeader("Authorization", authorization)
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .execute();
        Exception cause = null;
        Response res = null;
        String body = null;
        try {
            res = f.get();
            body = res.getResponseBody("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            cause = e;
        } catch (InterruptedException e) {
            e.printStackTrace();
            cause = e;
        } catch (ExecutionException e) {
            e.printStackTrace();
            cause = e;
        }
        if (null != cause)
            throw new UnionException(500, cause.getMessage(), cause);
        JSON json = new JSON(body);
        if (res.getStatusCode() < 200 || res.getStatusCode() >= 300) {
            System.out.println(body);
            throw new UnionException(res.getStatusCode(), json.getJSONObject().optString("detail"));
        }
        return json;
    }
    public Union() {
    }

    /**
     * websocket地址
     * @return
     */
    public String getWsUrl() {
        return wsUrl;
    }

    /**
     * websocket地址
     */
    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    /**
     * http地址
     * @return
     */
    public String getHttpUrl() {
        return httpUrl;
    }

    /**
     * http地址
     */
    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    void tryInitWebSocket() {
        synchronized (this) {
            if (unionDriver == null || websocket != null && websocket.isOpen()) return;
            new Thread(new Runnable() {
                public void run() {
                    System.out.println("Trying to connect to union");
                    while (null != unionDriver && !initWebSocket()) {
                        System.out.println("Failed to connect to union");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    pingService = Executors.newScheduledThreadPool(1);
                    pingService.scheduleWithFixedDelay(new Runnable() {
                        @Override
                        public void run() {
                            if (websocket != null)
                                websocket.sendPing(new byte[] {0x31});
                        }
                    }, 0, 10, TimeUnit.SECONDS);
                }
            }).start();
        }
    }

    boolean initWebSocket() {
        try {
            websocket = asyncHttpClient.prepareGet(wsUrl + "/app")
                    .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(this).build()).get();
            JSONObject json = new JSONObject();
            json.put("uuid", "login");
            JSONObject body = new JSONObject();
            body.put("app_id", appId);
            body.put("app_secret", appSecret);
            json.put("body", body);
            websocket.sendMessage(json.toString());
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    void makeClient() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{ new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }
        }}, new SecureRandom());
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setSSLContext(sc);
        AsyncHttpClientConfig config = builder.build();
        asyncHttpClient = new AsyncHttpClient(config);
    }

    /**
     * 初始化
     * @throws UnionException
     */
    public void init() throws UnionException {
        try {
            makeClient();

            Future<Response> f = asyncHttpClient.preparePost(httpUrl + "/token")
                    .addFormParam("app_id", this.appId)
                    .addFormParam("app_secret", this.appSecret).execute();
            Response res = f.get();
            JSONObject json = new JSONObject(res.getResponseBody("UTF-8"));
            String token = json.optString("token");
            if (token == null) {
                String detail = json.optString("detail", "Unexpected exception.");
                throw new UnionException(res.getStatusCode(), detail);
            }
            authorization = "Token " + token;

            CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
            cityNameCache = cacheManager.createCache("cityNameCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, String.class,
                            ResourcePoolsBuilder.heap(100)).build());
            if (unionDriver != null) {
                unionDriver.init(this);
                tryInitWebSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnionException(500, "Failed to create http client", e);
        }
    }

    public void close() {
        unionDriver = null;
        websocket.close();
    }

    /**
     * 按城市搜索充电站
     * @param code 城市行政编码
     * @return null on error
     */
    public Station[] searchStations(long code) {
        try {
            JSON json = getJSON(asyncHttpClient.prepareGet(httpUrl + "/cities/" + code + "/stations"));
            JSONArray array = json.getJSONArray();
            Station[] stations = new Station[array.length()];
            for (int i = 0; i < array.length(); i++)
                stations[i] = new Station(array.getJSONObject(i));
            return stations;
        } catch (UnionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按经纬度和范围搜索充电站
     * @param longitude 经度
     * @param latitude 纬度
     * @param radius 半径(米)
     * @param page 页码,从1开始
     * @param pageSize 最大50
     * @return null on error
     */
    public Station[] searchStations(double longitude, double latitude, double radius, int page, int pageSize) {
        try {
            JSON json = getJSON(asyncHttpClient.prepareGet(httpUrl + "/stations")
                    .addQueryParam("lng", Double.toString(longitude))
                    .addQueryParam("lat", Double.toString(latitude))
                    .addQueryParam("radius", Double.toString(radius))
                    .addQueryParam("page", Integer.toString(page))
                    .addQueryParam("page_size", Integer.toString(pageSize)));
            JSONArray array = json.getJSONObject().getJSONArray("results");
            Station[] stations = new Station[array.length()];
            for (int i = 0; i < array.length(); i++)
                stations[i] = new Station(array.getJSONObject(i));
            return stations;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取充电桩
     * @param id 充电站编号
     * @return null on error
     */
    public Pile[] getPiles(long id) {
        try {
            Future<Response> f = asyncHttpClient.prepareGet(httpUrl + "/stations/" + id + "/piles")
                    .addHeader("Authorization", authorization)
                    .execute();
            JSONArray array = new JSONArray(f.get().getResponseBody("UTF-8"));
            Pile[] piles = new Pile[array.length()];
            for (int i = 0; i < array.length(); i++)
                piles[i] = new Pile(array.getJSONObject(i));
            return piles;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查看一个充电桩的详情
     * @param id 充电桩编号
     * @return null on error or failure
     */
    public Pile getPile(long id) {
        try {
            Future<Response> f = asyncHttpClient.prepareGet(httpUrl + "/piles/" + id)
                    .addHeader("Authorization", authorization)
                    .execute();
            JSONObject json = new JSONObject(f.get().getResponseBody("UTF-8"));
            return new Pile(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过二维码查询充电桩
     * @param qrCode 二维码内容
     * @return null on error
     */
    public Pile[] getPiles(String qrCode) {
        try {
            Future<Response> f = asyncHttpClient.prepareGet(httpUrl + "/piles")
                    .addHeader("Authorization", authorization)
                    .addQueryParam("qr_code", qrCode)
                    .execute();
            JSONArray array = new JSONArray(f.get().getResponseBody("UTF-8"));
            Pile[] piles = new Pile[array.length()];
            for (int i = 0; i < array.length(); i++)
                piles[i] = new Pile(array.getJSONObject(i));
            return piles;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 开始充电
     * @param id 充电桩编号
     * @param outOrderId 充电订单外本地编号,可选
     * @param outUserId 用户本地编号,可选
     * @return Charge
     * @throws UnionException
     */
    public Charge startCharging(long id, String outOrderId, String outUserId) throws UnionException {
        JSONObject body = new JSONObject();
        body.put("out_order_id", outOrderId);
        body.put("out_user_id", outUserId);
        JSON json = getJSON(asyncHttpClient.preparePost(httpUrl + "/piles/" + id + "/charging")
                .setBody(body.toString()));
        return new Charge(json.getJSONObject());
    }

    /**
     * 获取充电过程详情
     * @param id 充电桩编号
     * @return Charge
     * @throws UnionException
     */
    public Charge getCharging(long id) throws UnionException {
        JSON json = getJSON(asyncHttpClient.prepareGet(httpUrl + "/piles/" + id + "/charging"));
        return new Charge(json.getJSONObject());
    }

    /**
     * 停止充电
     * @param id 充电桩编号
     * @return Charge
     * @throws UnionException
     */
    public Charge stopCharging(long id) throws UnionException {
        JSON json = getJSON(asyncHttpClient.prepareDelete(httpUrl + "/piles/" + id + "/charging"));
        return new Charge(json.getJSONObject());
    }

    @Override
    public void onOpen(WebSocket websocket) {
        System.out.println("I'm connected to the union.");
    }

    @Override
    public void onClose(WebSocket websocket) {
        System.out.println("I'm disconnected to the union.");
        pingService.shutdown();
        this.websocket = null;
        tryInitWebSocket();
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("Something wrong related to the connection to the union.");
        t.printStackTrace();
    }

    @Override
    public void onMessage(String message) {
        JSONObject msg = new JSONObject(message);
        System.out.println("Got: " + msg.toString(2));
        if (msg.has("method")) { // request
            new RequestHandler(this) {
                @Override
                public void onComplete(String uuid, int code, JSONObject body) {
                    JSONObject res = new JSONObject();
                    res.put("uuid", uuid);
                    res.put("status", code);
                    res.put("body", body);
                    System.out.println("Response: " + res.toString(2));
                    websocket.sendMessage(res.toString());
                }
            }.process(msg);
        } else {
            // TODO response or notification
        }
    }

    /**
     * 充电过程更新
     * charge.outPileId必须指定
     * @param charge
     */
    public void onChargeChange(Charge charge) {
        try {
            Future<Response> f = asyncHttpClient.preparePatch(httpUrl + "/piles/@" + charge.outPileId + "/charging")
                    .addHeader("Authorization", authorization)
                    .setBody(charge.toJson().toString()).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 充电站更新
     * station.outId充电站本地编号
     * @param station
     */
    public void onStationChange(Station station) {
        try {
            String body = new JSONArray().put(station.toJson()).toString();
            Future<Response> f = asyncHttpClient.preparePut(httpUrl + "/stations/mput")
                    .addHeader("Authorization", authorization)
                    .addHeader("Content-Type", "application/json; charset=UTF-8")
                    .setBody(body)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 充电站删除
     * @param outId 充电桩本地编号
     */
    public void onStationDelete(String outId) {
        try {
            Future<Response> f = asyncHttpClient.prepareDelete(httpUrl + "/stations/@" + outId)
                    .addHeader("Authorization", authorization)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 充电桩信息更新
     * @param pile
     */
    public void onPileChange(Pile pile) {
        try {
            Future<Response> f = asyncHttpClient.preparePut(httpUrl + "/piles/mput")
                    .addHeader("Authorization", authorization)
                    .addHeader("Content-Type", "application/json; charset=UTF-8")
                    .setBody(new JSONArray().put(pile).toString()).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 充电桩删除
     * @param outId 充电桩本地编号
     */
    public void onPileDelete(String outId) {
        try {
            Future<Response> f = asyncHttpClient.prepareDelete(httpUrl + "/piles/@" + outId)
                    .addHeader("Authorization", authorization)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过行政编码查城市名
     * @param code 行政编码
     * @return
     */
    public String getCityName(int code) {
        try {
            if (cityNameCache.get(code) != null)
                return cityNameCache.get(code);
            Future<Response> f = asyncHttpClient.prepareGet(httpUrl + "/cities/" + code)
                    .addHeader("Authorization", authorization)
                    .execute();
            String body =f.get().getResponseBody("UTF-8");
            System.out.println(body);
            String name = new JSONObject(body).optString("name", null);
            if (name != null) {
                cityNameCache.put(code, name);
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过城市名查行政编码
     * @param name 城市名
     * @return 0 on error
     */
    public int getCityCode(String name) {
        try {
            Future<Response> f = asyncHttpClient.prepareGet(httpUrl + "/cities")
                    .addHeader("Authorization", authorization)
                    .addQueryParam("name", name)
                    .execute();
            JSONArray cities = new JSONArray(f.get().getResponseBody("UTF-8"));
            if (cities != null && cities.length() > 0)
                return cities.getJSONObject(0).getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public UnionDriver getUnionDriver() {
        return unionDriver;
    }

    public void setUnionDriver(UnionDriver unionDriver) {
        this.unionDriver = unionDriver;
    }
}
