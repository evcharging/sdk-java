import com.evchargings.union.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Properties;

/**
 * unit test cases
 */
public class TestUnion {
    String appId;
    String appSecret;
    String httpUrl;
    String wsUrl;
    static double LONGITUDE = 121.514597;
    static double LATITUDE = 31.04173;
    Union u;
    public TestUnion() throws IOException {
        Properties p = new Properties();
        p.load(new BufferedInputStream(new FileInputStream("union.properties")));
        appId = p.getProperty("app_id");
        appSecret = p.getProperty("app_secret");
        httpUrl = p.getProperty("http_url");
        wsUrl = p.getProperty("ws_url");
        System.out.println(appId);
        System.out.println(appSecret);
    }


    @Before
    public void setUp() {
        u = new Union();
        u.setAppId(appId);
        u.setAppSecret(appSecret);
        u.setHttpUrl(httpUrl);
        u.setWsUrl(wsUrl);
    }

    @After
    public void tearDown() {
        u.close();
    }

    @Test
    public void testInit() {
        try {
            u.init();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSearchStations() {
        testInit();
        Station[] ss = u.searchStations(LONGITUDE, LATITUDE, 5000, 1, 20);
        Assert.assertTrue(ss.length > 0);

        //错误的页
        ss = u.searchStations(LONGITUDE, LATITUDE, 5000, 0, 20);
        Assert.assertTrue(ss == null);
    }

    @Test
    public void testGetPiles() {
        testInit();
        Station[] ss = u.searchStations(LONGITUDE, LATITUDE, 5000, 1, 20);
        Pile[] ps = u.getPiles(ss[0].getId());
        Assert.assertTrue(ps.length > 0);
    }

    @Test
    public void testGetPilesByQrCode() {
        testInit();
        Pile[] ps = u.getPiles("123456789002");
        Assert.assertTrue(ps.length == 1);
        System.out.println(ps[0].toJson().toString());
        // 错误桩号
        ps = u.getPiles("000");
        Assert.assertTrue(ps.length == 0);
    }

    @Test
    public void testGetPile() {
        long id = 123;
        testInit();
        Pile pile = u.getPile(id);
        Assert.assertTrue(pile.getId() == id);
    }

    @Test
    public void testNewStation() {
        testInit();

        Station s = new Station();
        s.setOutId("87654321");
        s.setName("动力联盟测试点");
        s.setStatus(Station.Status.OPEN);
        s.setPicture("http://www.evchargings.com/uploadfile/2015/1203/20151203031018279.png");
        s.setAddress("上海市黄浦区龙华东路858号海外滩办公B座");
        s.setLng(LONGITUDE);
        s.setLat(LATITUDE);
        s.setCityCode(u.getCityCode("上海市黄浦区"));
        u.onStationChange(s);
        u.searchStations(LONGITUDE, LATITUDE, 5000, 1, 20);

        u.onStationDelete(s.getOutId());
        u.searchStations(LONGITUDE, LATITUDE, 5000, 1, 20);
    }

    @Test
    public void testNewPile() {
        testInit();
        Station s = new Station();
        s.setOutId("87654321");
        s.setName("动力联盟测试点");
        s.setStatus(Station.Status.OPEN);
        s.setPicture("http://www.evchargings.com/uploadfile/2015/1203/20151203031018279.png");
        s.setAddress("上海市黄浦区龙华东路858号海外滩办公B座");
        s.setLng(LONGITUDE);
        s.setLat(LATITUDE);
        s.setCityCode(u.getCityCode("上海市黄浦区"));
        u.onStationChange(s);

        Pile p = new Pile();
        p.setOutId("3");
        p.setOutStationId("87654321");
        p.setType(Pile.Type.AC);
        p.setStatus(Pile.Status.IDLE);
        p.setQrCode("3");
        u.onPileChange(p);

        u.onPileDelete(p.getOutId());
        u.onStationDelete(s.getOutId());
    }

    @Test
    public void testGetCityName() {
        int id = 652300;
        testInit();
        String name = u.getCityName(id);
        Assert.assertEquals("昌吉回族自治州", name);

        //错误的编码
        id = 999999;
        name = u.getCityName(id);
        Assert.assertEquals(null, name);
    }

    @Test
    public void testGetCityCode() {
        testInit();
        String name = "昌吉回族自治州";
        int id = u.getCityCode(name);
        Assert.assertEquals(652300, id);

        //错误的编码
        name = "哪里呀";
        id = u.getCityCode(name);
        Assert.assertEquals(0, id);
    }

    @Test
    public void testSync() {
        UnionDriver ud = new TestDriver();
        u.setUnionDriver(ud);
        testInit();
    }
    @Test
    public void testSearchByCity() {
        testInit();
        // 查找正确城市
        int code = 310000;
        Station[] ss = u.searchStations(code);
        Assert.assertTrue(ss.length > 0);

        // 错误城市
        code = 999999;
        ss = u.searchStations(code);
        Assert.assertTrue(ss == null);

        // 空城市
        code = 511300;
        ss = u.searchStations(code);
        Assert.assertTrue(ss.length == 0);
    }
    @Test(timeout=60000)
    public void testCharging() {
        testInit();
        Pile[] ps = u.getPiles("123456789002");
        Assert.assertTrue(ps.length == 1);
        long id = ps[0].getId();
        String outOrderId = Long.toString(System.currentTimeMillis());
        Charge charge;
        try {
          // (4.x) 开始充电
          charge = u.startCharging(id, outOrderId, "123");
          System.out.println(charge.toJson().toString());
          Thread.sleep(5000);
        } catch (Exception ue) {
            ue.printStackTrace();
        }
        try {
          // (4.x) 查询充电状态
          charge = u.getCharging(id);
          System.out.println(charge.toJson().toString());
          Thread.sleep(5000);
        } catch (Exception ue) {
            ue.printStackTrace();
        }
        try {
            // (4.x) 用外部订单编号查询充电状态
            charge = u.getCharging(outOrderId);
            System.out.println(charge.toJson().toString());
            Thread.sleep(5000);
        } catch (Exception ue) {
            ue.printStackTrace();
        }
        try {
          // (4.x) 停止充电
          charge = u.stopCharging(id);
          System.out.println(charge.toJson().toString());
        } catch (Exception ue) {
          ue.printStackTrace();
        }
    }
}
