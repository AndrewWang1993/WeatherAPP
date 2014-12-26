package com.xxmassdeveloper.myChart;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.mikephil.chartLibrary.charts.BarLineChartBase.BorderPosition;
import com.github.mikephil.chartLibrary.charts.LineChart;
import com.github.mikephil.chartLibrary.data.Entry;
import com.github.mikephil.chartLibrary.data.LineData;
import com.github.mikephil.chartLibrary.data.LineDataSet;
import com.github.mikephil.chartLibrary.interfaces.OnChartGestureListener;
import com.github.mikephil.chartLibrary.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.chartLibrary.utils.Legend;
import com.github.mikephil.chartLibrary.utils.Legend.LegendForm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.main.apapter.NumberSeekBar;
import com.main.bean.Record;
import com.main.util.ConfigCache;
import com.main.util.NetUtil;
import com.main.weather.BuildConfig;
import com.main.weather.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Pm25ChartActivity extends DemoBase implements OnSeekBarChangeListener,
        OnChartGestureListener, OnChartValueSelectedListener {
    public static final String GET_WEATHER_RECORD_URL = BuildConfig.HOST + BuildConfig.PORT + BuildConfig.RECORD;
    private LineChart mChart;
    private NumberSeekBar ns;
    private TextView tvX;
    private ImageView back;
    private TextView title;
    private LinearLayout titleright;
    private Gson weather_gson;
    public Thread th;
    private ArrayList<String> datelist = new ArrayList<String>();
    private ArrayList<String> pmlist = new ArrayList<String>();
    Record mrecord = new Record();
    ArrayList<HashMap<String, Object>> arrhash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_linechart);

        tvX = (TextView) findViewById(R.id.tvXMax);
        back = (ImageView) findViewById(R.id.title_city_manager);
        title = (TextView) findViewById(R.id.title_city_name);
        titleright = (LinearLayout) findViewById(R.id.titleRight);


        arrhash = new ArrayList<HashMap<String, Object>>();
        // 不转换没有 @Expose 注解的字段
        weather_gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .create();
        updateRecord(true);
        synchronized (th) {
            try {
                th.wait();
            } catch (Exception e) {
                Log.v("Thread Exception", e.toString());
            }
        }
        datelist = mrecord.getdate();
        pmlist = mrecord.getpm();
        Collections.reverse(datelist);
        Collections.reverse(pmlist);
        Log.v("Tag", datelist.toString());
        Log.v("Tag", pmlist.toString());


        title.setText("历史PM2.5曲线图");
        back.setImageDrawable(getResources().getDrawable(R.drawable.base_action_bar_back_normal));
        titleright.setVisibility(View.GONE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_left_out_activity);
                finish();
            }
        });
        ns = (NumberSeekBar) findViewById(R.id.seekBar1);
        ns.setProgress(7);
        ns.setOnSeekBarChangeListener(this);
        initSeekBar();

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setUnit(" ug");
        mChart.setDrawUnitsInChart(true);

        // if enabled, the chart will always start at zero on the y-axis
        mChart.setStartAtZero(false);

        // disable the drawing of values into the chart
        mChart.setDrawYValues(true);

        mChart.setDrawBorder(false);
        mChart.setBorderPositions(new BorderPosition[]{
                BorderPosition.TOP
        });

        // no description text
        mChart.setDescription("历史PM2.5曲线");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable / disable grid lines
        mChart.setDrawVerticalGrid(true);
        mChart.setDrawHorizontalGrid(true);

//        // enable / disable grid background
        mChart.setDrawGridBackground(false);
//
//        mChart.setDrawXLegend(false);
//        mChart.setDrawYLegend(false);

        // enable value highlighting
        mChart.setHighlightEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
//        mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // define an offset to change the original position of the marker
        // (optional)
        mv.setOffsets(-mv.getMeasuredWidth() / 2, -mv.getMeasuredHeight());

        // set the marker to the chart
        mChart.setMarkerView(mv);

        // enable/disable highlight indicators (the lines that indicate the
        // highlighted Entry)
        mChart.setHighlightIndicatorEnabled(false);

        // add data
        setData(8, 200);

        mChart.animateXY(2500, 2500);

        // // restrain the maximum scale-out factor
        // mChart.setScaleMinima(3f, 3f);
        //
        // // center the view to a specific position inside the chart
        // mChart.centerViewPort(10, 50);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
    }

    private void initSeekBar() {
        ns.setMax(15);
        ns.setProgress(7);
        ns.setTextSize(20);// 设置字体大小
        ns.setTextColor(Color.WHITE);// 颜色
        ns.setMyPadding(10, 10, 10, 10);// 设置padding 调用setpadding会无效
        ns.setImagePadding(0, 1);// 可以不设置
        ns.setTextPadding(0, 0);// 可以不设置
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText("历史" + (ns.getProgress() + 1) + "天PM2.5");

        setData(ns.getProgress() + 1, 7);

        // redraw
        mChart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    private void setData(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = count - 1; i >= 0; i--) {
            xVals.add(datelist.get(i));
        }
        Log.v("xVals", xVals.toString());
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            yVals1.add(new Entry(Integer.valueOf(pmlist.get(i)), count - i - 1));
        }
        Log.v("yVals1", yVals1.toString());

        // create a dataset and give it a type    set1
        LineDataSet set1 = new LineDataSet(yVals1, "PM2.5每日均值");
        set1.setFillAlpha(110);
        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.RED);
        set1.setLineWidth(5f);
        set1.setCircleSize(5f);
        set1.setFillAlpha(85);
        set1.setFillColor(Color.RED);
//        set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
//        Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));


        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1);   // add the datasets

        // create a data object with the datasets   上下虚线
        LineData data = new LineData(xVals, dataSets);

//        LimitLine ll1 = new LimitLine(130f);
//        ll1.setLineWidth(2f);
//        ll1.enableDashedLine(10f, 10f, 0f);
//        ll1.setDrawValue(false);
//        ll1.setLabelPosition(LimitLabelPosition.RIGHT);
//
//        LimitLine ll2 = new LimitLine(-30f);
//        ll2.setLineWidth(2f);
//        ll2.enableDashedLine(10f, 10f, 0f);
//        ll2.setDrawValue(false);
//        ll2.setLabelPosition(LimitLabelPosition.RIGHT);
//
//        data.addLimitLine(ll1);
//        data.addLimitLine(ll2);

        // set data

        mChart.setDrawXLabels(true);
        mChart.setValueTextSize(15);
        mChart.setValueTextColor(Color.WHITE);
        mChart.setDrawLegend(true);
        mChart.setLegendLabelPaint(20, Color.WHITE);


//        mChart.setDescriptionTextSize(25f);                                             // 没有，这是设置无数据时候的提示

        mChart.setDescriptionTypeface(Typeface.create(Typeface.DEFAULT, 1), 20, Color.WHITE); //这才是真的设置

        mChart.setData(data);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }


    private void getRecord(boolean isfresh) {

        String recordResult = connServerForResult(GET_WEATHER_RECORD_URL);

        try {
            recordResult = new String(recordResult.getBytes("iso8859-1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        parseRecord(GET_WEATHER_RECORD_URL, recordResult, isfresh);
    }

    private void parseRecord(String url, String result, boolean isrefresh) {
        mrecord = null;
        if (!TextUtils.isEmpty(result) && !result.contains("error")) {
            List<Record> mrec = weather_gson.fromJson(result,
                    new TypeToken<List<Record>>() {
                    }.getType()
            );
            mrecord = mrec.get(0);
        } else {
            result = "";
        }
        if (isrefresh && !TextUtils.isEmpty(result))
            // save2File(result, PM2D5_INFO_FILENAME);
            ConfigCache.setUrlCache(result, url);
    }

    private String connServerForResult(String url) {
        HttpGet httpRequest = new HttpGet(url);
        String strResult = "";
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            try {
                // HttpClient对象
                HttpClient httpClient = new DefaultHttpClient();
                // 获得HttpResponse对象
                HttpResponse httpResponse = httpClient.execute(httpRequest);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                    // 取得返回的数据
                    strResult = EntityUtils.toString(httpResponse.getEntity());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strResult; // 返回结果
    }

    private void updateRecord(final boolean isfresh) {
        th = new Thread() {
            @Override
            public void run() {

                try {
                    synchronized (this) {
                        getRecord(isfresh);
                        this.notify();
                    }
                } catch (Exception e) {
                    Log.v("Thread Exception", e.toString());
                }

            }

        };
        th.start();
    }

}
