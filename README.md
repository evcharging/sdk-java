联盟开发工具
============

依赖
----

    'com.ning:async-http-client:1.9.36',
    'org.ehcache:ehcache:3.0.0',
    'org.json:json:20160212'

开发
----

1. 实现UnionDriver接口

2. 创建Union实例

	属性|说明
	-----|----
	httpUrl|http地址
	wsUrl|ws地址
	appId|app标识
	appSecret|app密钥
	unionDriver|实现的UnionDriver

3. 初始化Union

		union.init();


参考
-----

CcChongDriver.java
