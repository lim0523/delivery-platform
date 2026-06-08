package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.BaiduMapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Component
public class BaiduMapUtil {

    @Autowired
    private BaiduMapProperties baiduMapProperties;

    private static final String GEO_URL =
            "https://api.map.baidu.com/geocoding/v3/";

    private static final String DIRECTION_URL =
            "https://api.map.baidu.com/directionlite/v1/driving";

    public String getLocation(String address) {
        Map<String, String> map = new HashMap<>();

        map.put("address", address);
        map.put("output", "json");
        map.put("ak", baiduMapProperties.getAk());

        String json = HttpClientUtil.doGet(GEO_URL, map);
        JSONObject jsonObject = JSON.parseObject(json);
//        log.info("地址解析请求参数：{}", map);
//        log.info("地址解析返回结果：{}", json);
        if (!jsonObject.getInteger("status").equals(0)) {
            throw new OrderBusinessException("地址解析失败");
        }

        JSONObject location =
                jsonObject.getJSONObject("result").getJSONObject("location");

        Double lng = location.getDouble("lng");
        Double lat = location.getDouble("lat");

        return lat + "," + lng;
    }

    public Integer getDistance(String origin, String destination) {

        Map<String, String> map = new HashMap<>();
        map.put("origin", origin);
        map.put("destination", destination);
        map.put("ak", baiduMapProperties.getAk());

        String json = HttpClientUtil.doGet(DIRECTION_URL, map);
//
//        log.info("路线规划请求参数：{}", map);
//        log.info("百度路线规划返回结果：{}", json);

        JSONObject jsonObject = JSON.parseObject(json);

        if (!jsonObject.getInteger("status").equals(0)) {
            throw new OrderBusinessException("配送距离计算失败");
        }

        JSONArray routes =
                jsonObject.getJSONObject("result")
                        .getJSONArray("routes");

        return routes.getJSONObject(0).getInteger("distance");
    }
}