package com.evchargings.union;

import org.json.JSONObject;

/**
 * 充电桩
 */
public class Pile implements IFromJSON {

    public Pile() {
    }
    public Pile(JSONObject jsonObject) {
        initWithJson(jsonObject);
    }

    @Override
    public void initWithJson(JSONObject jsonObject) {
        this.id = jsonObject.getLong("id");
        this.status = Status.values()[jsonObject.getInt("status")];
        this.lng = jsonObject.optDouble("lng");
        if (Double.isNaN(this.lng)) {
            this.lng = 0;
        }
        this.lat = jsonObject.optDouble("lat");
        if (Double.isNaN(this.lat)) {
            this.lat = 0;
        }
        this.stationId = jsonObject.getLong("station");
        this.price = jsonObject.getInt("price");
        this.withGun = jsonObject.getBoolean("with_gun");
        this.ratedCurrent = jsonObject.getInt("rated_current");
        this.ratedVoltage = jsonObject.getInt("rated_voltage");
        this.ratedPower = jsonObject.getInt("rated_power");
        this.qrCode = jsonObject.getString("qr_code");
        this.type = Type.values()[jsonObject.getInt("type")];
    }

    @Override
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        o.put("out_id", this.outId);
        o.put("status", this.status.ordinal());
        o.put("lng", this.lng);
        o.put("lat", this.lat);
        o.put("out_station_id", this.outStationId);
        o.put("price", this.price);
        o.put("rated_current", this.ratedCurrent);
        o.put("rated_voltage", this.ratedVoltage);
        o.put("rated_power", this.ratedPower);
        o.put("with_gun", this.withGun);
        o.put("qr_code", this.qrCode);
        o.put("type", this.type.ordinal());
        return o;
    }

    public static enum Status {
        /**
         * 空闲
         */
        IDLE,
        /**
         * 充电
         */
        BUSY,
        /**
         * 故障
         */
        DOWN
    };

    public static enum Type {
        /**
         * 直流
         */
        DC,
        /**
         * 交流
         */
        AC
    };

    // 桩号
    long id;

    /**
     * 电桩标识
     * @return
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * 电桩本地标识
     * @return
     */
    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
    }

    /**
     * 电桩状态
     * @return
     */
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 电桩类型
     * @return
     */
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * 经度
     * @return
     */
    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    /**
     * 纬度
     * @return
     */
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * 电站标识
     * @return
     */
    public long getStationId() {
        return stationId;
    }

    public void setStationId(long stationId) {
        this.stationId = stationId;
    }

    /**
     * 费率(分)
     * @return
     */
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    String outId;
    // 状态
    Status status;
    // 类型
    Type type;
    // 经度
    double lng;
    // 纬度
    double lat;

    int ratedVoltage;
    int ratedCurrent;
    int ratedPower;
    // 充电站编号
    long stationId;

    /**
     * 二维码字符串
     * @return
     */
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    String qrCode;

    /**
     * 是否提供充电枪
     * @return
     */
    public boolean isWithGun() {
        return withGun;
    }

    public void setWithGun(boolean withGun) {
        this.withGun = withGun;
    }
    /**
     * 额定功率(KW)
     * @return
     */
    public int getRatedPower() {
        return ratedPower;
    }

    public void setRatedPower(int ratedPower) {
        this.ratedPower = ratedPower;
    }
    /**
     * 额定电流(A)
     * @return
     */
    public int getRatedCurrent() {
        return ratedCurrent;
    }

    public void setRatedCurrent(int ratedCurrent) {
        this.ratedCurrent = ratedCurrent;
    }

    /**
     * 额定电压(V)
     * @return
     */
    public int getRatedVoltage() {
        return ratedVoltage;
    }

    public void setRatedVoltage(int ratedVoltage) {
        this.ratedVoltage = ratedVoltage;
    }

    boolean withGun;

    /**
     * 电站本地标识
     * @return
     */
    public String getOutStationId() {
        return outStationId;
    }

    public void setOutStationId(String outStationId) {
        this.outStationId = outStationId;
    }

    String outStationId;
    //单价
    int price;
}
