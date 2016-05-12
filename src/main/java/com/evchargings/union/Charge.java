package com.evchargings.union;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 充电过程
 */
public class Charge implements IFromJSON {
    long id;
    String outId;
    long pileId;
    String outPileId;
    long stationId;
    String outStationId;
    String outUserId;
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * 外部用户标识
     * @return
     */
    public String getOutUserId() {
        return outUserId;
    }

    public void setOutUserId(String outUserId) {
        this.outUserId = outUserId;
    }

    /**
     * 充电站外部标识
     * @return
     */
    public String getOutStationId() {
        return outStationId;
    }

    public void setOutStationId(String outStationId) {
        this.outStationId = outStationId;
    }

    /**
     * 充电过程外部标识
     */
    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
    }

    /**
     * 充电桩外部标识
     * @return
     */
    public String getOutPileId() {
        return outPileId;
    }

    public void setOutPileId(String outPileId) {
        this.outPileId = outPileId;
    }

    // 开始时间戳
    long started;
    // 结束时间戳
    long stopped;

    /**
     * 费用,单位为分
     * @return
     */
    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * 充电过程标识
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * 充电站标识
     * @return
     */
    public long getStationId() {
        return stationId;
    }

    public void setStationId(long stationId) {
        this.stationId = stationId;
    }

    /**
     * 开始时间戳,unix微秒
     * @return
     */
    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    /**
     * 结束时间戳,unix微秒
     * @return
     */
    public long getStopped() {
        return stopped;
    }

    public void setStopped(long stopped) {
        this.stopped = stopped;
    }

    // 费用
    int cost;

    // 当前电流(A)
    double current;
    // 当前电压(V)
    double voltage;
    // 使用电能(Kwh)
    double energy;
    // 使用功率(Kwh)
    double power;
    // 状态
    State state;

    public static enum State {
        /**
         * 成功结束
         */
        DONE,
        /**
         * 进行中
         */
        BUSY,
        /**
         * 错误
         */
        ERROR
    }

    public Charge() {

    }
    public Charge(JSONObject jsonObject) {
        initWithJson(jsonObject);
    }

    @Override
    public void initWithJson(JSONObject jsonObject) {
        System.out.println(jsonObject.toString());
        this.pileId = jsonObject.optLong("pile", 0);
        this.outPileId = jsonObject.optString("out_pile_id");
        this.current = jsonObject.optDouble("current", 0);
        this.voltage = jsonObject.optDouble("voltage", 0);
        this.energy = jsonObject.optDouble("energy", 0);
        this.cost = jsonObject.optInt("cost", 0);
        this.id = jsonObject.optLong("id", 0);
        this.outId = jsonObject.optString("out_id");
        this.outUserId = jsonObject.optString("out_user_id");
        this.stationId = jsonObject.optInt("station", 0);
        String str = jsonObject.optString("started");
        try {
            this.started = formatter.parse(str).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            this.started = 0;
        }
        str = jsonObject.optString("stopped");
        try {
            this.stopped = formatter.parse(str).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            this.stopped = 0;
        }
        this.state = State.values()[jsonObject.optInt("state", 0)];
    }

    @Override
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("out_id", outId);
        o.put("pile", pileId);
        o.put("out_pile_id", outPileId);
        o.put("out_station_id", outStationId);
        o.put("out_user_id", outUserId);

        o.put("cost", cost);
        o.put("started", started);
        o.put("stopped", stopped);
        o.put("current", current);
        o.put("voltage", voltage);
        o.put("energy", energy);
        o.put("state", this.state.ordinal());
        return o;
    }

    /**
     * 充电桩标识
     * @return
     */
    public long getPileId() {
        return pileId;
    }

    public void setPileId(long pileId) {
        this.pileId = pileId;
    }

    /**
     * 当前电流(A)
     * @return
     */
    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    /**
     * 当前电压(V)
     */
    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    /**
     * 使用功率(Kw)
     * @return
     */
    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    /**
     * 使用电量(Kwh)
     * @return
     */
    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    /**
     * 过程状态
     * @return
     */
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * 充电类型
     */
    public static enum ChargeType {
        /**
         * 直充
         */
        BY_NONE,
        /**
         * 限定金额
         */
        BY_MONEY,
        /**
         * 限定电量
         */
        BY_POWER,
        /**
         * 限定时间
         */
        BY_TIME,
    }
}
