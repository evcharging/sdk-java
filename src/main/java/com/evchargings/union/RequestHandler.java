package com.evchargings.union;

import com.ning.http.client.Response;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * internal use only.
 */
public class RequestHandler extends Thread {
    JSONObject req;
    UnionDriver unionDriver;
    Union union;

    public RequestHandler(Union union) {
        this.union = union;
        this.unionDriver = union.getUnionDriver();
    }
    public void process(JSONObject req) {
        this.req = req;
        this.start();
    }
    public void onComplete(String uuid, int code, JSONObject body) {

    }
    @Override
    public void run() {
        String uuid = req.getString("uuid");
        String path = req.getString("path");
        String method = req.getString("method");
        int status = 200;
        JSONObject body = req.getJSONObject("body");
        JSONObject resBody = new JSONObject();
        try {
            if ("charge".equals(path)) {
                if ("POST".equals(method)) {
                    // 开始充电
                    String outId = body.getString("out_id");
                    long orderId = body.optLong("order_id", 0);
                    unionDriver.startCharging(outId, orderId);
                } else if ("DELETE".equals(method)) {
                    // 结束充电
                    String outId = body.getString("out_id");
                    Charge charge = unionDriver.stopCharging(outId);
                    resBody = charge.toJson();
                } else if ("GET".equals(method)) {
                    // 结束充电
                    String outId = body.getString("out_id");
                    Charge charge = unionDriver.getCharging(outId);
                    resBody = charge.toJson();
                } else if ("PUT".equals(method)) {
                    // 更新充电状态
                    unionDriver.onChargeChange(new Charge(body));
                }
            } else if ("/".equals(path)) {
                if ("SYNC".equals(method)) {
                    int pageSize = body.getInt("pageSize");
                    int code = body.getInt("code");
                    int page = 1;
                    while (true) {
                        Station[] stations = unionDriver.getStations(code, page, pageSize);
                        if (null == stations)
                            break;
                        JSONArray reqBody = Utils.makeJSONArray(stations);
                        if (stations.length > 0) {
                            try {
                                Future<Response> f = union.asyncHttpClient.preparePut(union.getHttpUrl() + "/stations/mput")
                                        .addHeader("Authorization", union.authorization)
                                        .addHeader("Content-Type", "application/json; charset=UTF-8")
                                        .setBody(reqBody.toString()).execute();
                                Response res = f.get();
                                if (res.getStatusCode() != 200) {
                                    System.out.println(res.getResponseBody("UTF-8"));
                                    break;
                                }
                                for (Station s : stations) {
                                    Pile[] piles = unionDriver.getPiles(s.getOutId());
                                    reqBody = Utils.makeJSONArray(piles);
                                    f = union.asyncHttpClient.preparePut(union.getHttpUrl() + "/piles/mput")
                                            .addHeader("Authorization", union.authorization)
                                            .addHeader("Content-Type", "application/json; charset=UTF-8")
                                            .setBody(reqBody.toString()).execute();
                                    res = f.get();
                                    if (res.getStatusCode() != 200) {
                                        System.out.println(res.getResponseBody("UTF-8"));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            page++;
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (UnionException e) {
            e.printStackTrace();
            resBody.put("errno", e.getErrno());
            resBody.put("detail", e.getMessage());
            status = 500;
        }
        onComplete(uuid, status, resBody);
    }
}
