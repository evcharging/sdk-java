package com.evchargings.union;

import org.json.JSONObject;

/**
 * 充电站
 */
public class Station implements IFromJSON {
    // 站号
    long id;
    // 本地标识
    String outId;
    // 站名
    String name;

    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
    }

    // 图片
    String picture;

    // 交流桩
    short acCount;
    short idleAcCount;
    short busyAcCount;

    // 直流桩
    short dcCount;
    short idleDcCount;
    short busyDcCount;

    // 地址
    String address;
    // 经度
    double lng;
    // 纬度
    double lat;
    // 所在地区
    int cityCode;

    // 费率: 分/度
    int price;

    @Override
    public void initWithJson(JSONObject jsonObject) {
        this.id = jsonObject.getLong("id");
        this.name = jsonObject.getString("name");
        this.picture = jsonObject.optString("pic");

        this.acCount = (short)jsonObject.getInt("nac");
        this.dcCount = (short)jsonObject.getInt("nac");

        this.lng = jsonObject.getDouble("lng");
        this.lat = jsonObject.getDouble("lat");
        this.address = jsonObject.getString("address");
        this.cityCode = jsonObject.getInt("city");
        this.status = Status.values()[jsonObject.getInt("status")];
    }

    @Override
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        o.put("name", this.name);
        o.put("out_id", this.outId);
        o.put("pic", this.picture);
        o.put("nac", this.acCount);
        o.put("ndc", this.dcCount);
        o.put("lng", this.lng);
        o.put("lat", this.lat);
        o.put("address", this.address);
        o.put("city", this.cityCode);
        o.put("status", this.status.ordinal());
        return o;
    }

    // 状态
    public enum Status {
        /**
         * 营业中
         */
        OPEN,
        /**
         * 建设中
         */
        WAIT,
    }
    Status status;

    public Station(JSONObject jsonObject) {
        initWithJson(jsonObject);
    }

    public Station() {

    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * 交流个数
     * @return
     */
    public short getAcCount() {
        return acCount;
    }

    public void setAcCount(short acCount) {
        this.acCount = acCount;
    }

    /**
     * 交流空闲数
     * @return
     */
    public short getIdleAcCount() {
        return idleAcCount;
    }

    public void setIdleAcCount(short idleAcCount) {
        this.idleAcCount = idleAcCount;
    }

    /**
     * 直流忙碌数
     * @return
     */
    public short getBusyAcCount() {
        return busyAcCount;
    }

    public void setBusyAcCount(short busyAcCount) {
        this.busyAcCount = busyAcCount;
    }

    /**
     * 直流个数
     * @return
     */
    public short getDcCount() {
        return dcCount;
    }

    public void setDcCount(short dcCount) {
        this.dcCount = dcCount;
    }

    public short getIdleDcCount() {
        return idleDcCount;
    }

    public void setIdleDcCount(short idleDcCount) {
        this.idleDcCount = idleDcCount;
    }

    public short getBusyDcCount() {
        return busyDcCount;
    }

    public void setBusyDcCount(short busyDcCount) {
        this.busyDcCount = busyDcCount;
    }

    /**
     * 邮编
     * @return
     */
    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 费率(分/度)
     * @return
     */
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * 状态
     * @return
     */
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 名称
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 地址
     * @return
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 图片url
     * @return
     */
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
