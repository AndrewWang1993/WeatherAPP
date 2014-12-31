package com.main.Json;

import android.util.Log;

import com.main.weather.BuildConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wangxm-wr on 2014/10/27 in Vanke.
 */
public class Json_Operation {

    public Json_Operation() {
    }

    String TAG_KEY = "Key";
    String TAG_EQUIPMENTID = "EquipmentId";
    String TAG_SIGNALID = "SignalId";
    String TAG_VALUE = "Value";
    String Tag_SUCCESS_STATUE = "Success";
    String TAG_VALUE_SUCCESS = "1";
    String TAG_VALUE_FAILURE = "0";

    /**
     * @param Value 开关除霾机的信号量 ，"1" 代表开，"0"代表关除霾机
     * @return 成功返回1，失败返回0
     */
    public int controlAirFliter(String Value) {
        String Success;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(TAG_KEY, BuildConfig.TAG_KEY));
        params.add(new BasicNameValuePair(TAG_EQUIPMENTID, BuildConfig.TAG_EQUIPMENTID));
        params.add(new BasicNameValuePair(TAG_SIGNALID, BuildConfig.TAG_SIGNALID));
        params.add(new BasicNameValuePair(TAG_VALUE, Value));
        try {
            JSONObject json = null;
            json = new JSONParser().makeHttpRequest(BuildConfig.TAG_HOST_SET,
                    "POST", params);
            Log.d("Response", json.toString());
            Success = json.getString(Tag_SUCCESS_STATUE);

            if (Success.equals(TAG_VALUE_SUCCESS)) {
                return 1;
            } else if (Success.equals(TAG_VALUE_FAILURE)) {
                return 0;
            }
        } catch (Exception e) {
            Log.v("JSON", "controlAirFliterJSON error");
            return 0;
        }

        return 0;
    }

    /**
     * 获取除霾机当前状态
     *
     * @return 成功返回1，失败返回0
     */
    public int getAirFilterStatus() {
        String Success = null;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(TAG_KEY, BuildConfig.TAG_KEY));
        params.add(new BasicNameValuePair(TAG_EQUIPMENTID, BuildConfig.TAG_EQUIPMENTID));
        params.add(new BasicNameValuePair(TAG_SIGNALID, BuildConfig.TAG_SIGNALID));
        try {
            JSONObject json = null;
            json = new JSONParser().makeHttpRequest(BuildConfig.TAG_HOST_GET, "POST", params);
            Success = json.getString(TAG_VALUE);

            assert Success != null;
            if (Success.equals(TAG_VALUE_SUCCESS)) {
                return 1;
            } else if (Success.equals(TAG_VALUE_FAILURE)) {
                return 0;
            }
        } catch (Exception e) {
            Log.v("JSON", "getAirFilterStatusJSON error");
        }
        return 0;
    }

}

