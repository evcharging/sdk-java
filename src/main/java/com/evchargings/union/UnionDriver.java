package com.evchargings.union;

/**
 * 联盟驱动接口
 */
public interface UnionDriver {
    /**
     * 初始化
     * @param union
     */
    public void init(Union union);

    /**
     * 按城市搜索充电站
     * @param code 邮编
     * @param page 页码
     * @param pageSize 页长
     */
    public Station[] getStations(int code, int page, int pageSize);

    /**
     * 获取充电站下的充电桩
     * @param outId 充电站本地编号
     * @return
     */
    public Pile[] getPiles(String outId);

    /**
     * 开始放电
     * @param outId 充电桩本地编号
     * @param orderId 订单编号
     * @throws UnionException
     */
    public void startCharging(String outId, long orderId) throws UnionException;

    /**
     * 停止充电
     * @param outId 充电桩本地编号
     * @throws UnionException
     */
    public Charge stopCharging(String outId) throws UnionException;

    /**
     * 查询充电状态
     * @param outId 充电桩本地编号
     * @return
     * @throws UnionException
     */
    public Charge getCharging(String outId) throws UnionException;

    /**
     * 充电过程状态更新
     * @param charge 充电过程详情
     */
    public void onChargeChange(Charge charge);
}