package com.fourtails.usuariolecturista;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;
import com.db.chart.view.animation.style.DashAnimation;
import com.fourtails.usuariolecturista.ocr.CaptureActivity;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * This is the balance fragment where it shows your metrics for the last reading, this month etc,
 * also calls for a facebook publish
 */
public class BalanceFragment extends Fragment {

    public static final String TAG = "BalanceFragment";

    public static final String PREF_LAST_READING = "lastReadingPref";
    public static final String PREF_LAST_READING_DATE = "lastReadingDatePref";
    public static final String PREF_TOTAL_LITERS_FOR_CYCLE = "totalLitersForCyclePref";
    public static final String PREF_FIRST_READING_FOR_CYCLE = "firstReadingZeroValue";

    private final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
    private final TimeInterpolator exitInterpolator = new AccelerateInterpolator();


    /**
     * Play
     */
    private static ImageButton mPlayBtn;


    /**
     * Order
     */
    private static ImageButton mOrderBtn;
    private final static int[] beginOrder = {0, 1, 2, 3, 4, 5, 6};
    private final static int[] middleOrder = {3, 2, 4, 1, 5, 0, 6};
    private final static int[] endOrder = {6, 5, 4, 3, 2, 1, 0};
    private static float mCurrOverlapFactor;
    private static int[] mCurrOverlapOrder;
    private static float mOldOverlapFactor;
    private static int[] mOldOverlapOrder;


    /**
     * Ease
     */
    private static ImageButton mEaseBtn;
    private static BaseEasingMethod mCurrEasing;
    private static BaseEasingMethod mOldEasing;


    /**
     * Enter
     */
    private static ImageButton mEnterBtn;
    private static float mCurrStartX;
    private static float mCurrStartY;
    private static float mOldStartX;
    private static float mOldStartY;


    /**
     * Alpha
     */
    private static ImageButton mAlphaBtn;
    private static int mCurrAlpha;
    private static int mOldAlpha;

    /**
     * Line
     */
    private final static int LINE_MAX = 100;
    private final static int LINE_MIN = 0;
    private final static String[] lineLabels = {"", "ANT", "GNU", "OWL", "APE", "JAY", ""};
    private final static float[][] lineValues = {{0, 25f, 26f, 39f, 42f, 56f, 70f},
            {0, 25f, 26f, 39f, 42f, 56f, 70f}};
    //private static LineChartView mLineChart;
    private Paint mLineGridPaint;
    private TextView mLineTooltip;

    private final OnEntryClickListener lineEntryListener = new OnEntryClickListener() {
        @Override
        public void onClick(int setIndex, int entryIndex, Rect rect) {
            System.out.println(setIndex);
            System.out.println(entryIndex);
            if (mLineTooltip == null)
                showLineTooltip(setIndex, entryIndex, rect);
            else
                dismissLineTooltip(setIndex, entryIndex, rect);
        }
    };

    private final View.OnClickListener lineClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mLineTooltip != null)
                dismissLineTooltip(-1, -1, null);
        }
    };

    private boolean mNewInstance;


    @InjectView(R.id.linechart)
    LineChartView mLineChart;

    private int lastReadingValue;
    private int totalLitersForCycleValue;

    private String lastReadingDateValue;

    // Injected views and clicklisteners
    @InjectView(R.id.textViewLastReadingDate)
    TextView lastReading;
    //@InjectView(R.id.textViewLastReadingDate) TextView lastReadingDate;
    @InjectView(R.id.textViewTotalBalance)
    TextView totalLitersForCycle;

    @InjectView(R.id.fabScan)
    FloatingActionButton fabScan;

    @OnClick(R.id.fabScan)
    public void scanButtonClicked() {
        Intent ocrCaptureActivity = new Intent(getActivity(), CaptureActivity.class);
        MainActivity.bus.post(ocrCaptureActivity);
    }

    @InjectView(R.id.fabPay)
    FloatingActionButton fabPay;

    @OnClick(R.id.fabPay)
    public void payButtonClicked() {
        Fragment payOptionsFragment = new PayOptionsFragment();
        MainActivity.bus.post(payOptionsFragment);
    }

    public BalanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_balance, container, false);

        ButterKnife.inject(this, view);

        //Button resetValues = (Button) view.findViewById(R.id.buttonResetValuesForCycle);

        // setting the texts
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastReadingValue = sharedPreferences.getInt(PREF_LAST_READING, 0);

        lastReadingDateValue = sharedPreferences.getString(PREF_LAST_READING_DATE, "No cycle");

        totalLitersForCycleValue = sharedPreferences.getInt(PREF_TOTAL_LITERS_FOR_CYCLE, 0);

        lastReading.setText(String.valueOf(lastReadingValue));

        //lastReadingDate.setText(lastReadingDateValue);

        totalLitersForCycle.setText(String.valueOf(totalLitersForCycleValue));

        /** Chart things **/
        mNewInstance = false;
        mCurrOverlapFactor = .5f;
        mCurrEasing = new QuintEaseOut();
        mCurrStartX = -1;
        mCurrStartY = 0;
        mCurrAlpha = -1;

        mOldOverlapFactor = 1;
        mOldEasing = new QuintEaseOut();
        mOldStartX = -1;
        mOldStartY = 0;
        mOldAlpha = -1;

        //mHandler = new Handler();

        //initMenu();

        initLineChart();

        updateLineChart();

//        resetValues.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resetPreferencesValuesForReadings();
//            }
//        });

        // Normal Graph
/*
        GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
                new GraphView.GraphViewData(1, 2)
                , new GraphView.GraphViewData(10, 20)
                , new GraphView.GraphViewData(15, 46)
                , new GraphView.GraphViewData(30, 30)
        });

        GraphView graphView = new LineGraphView(
                this.getActivity() // context
                , "Consumo" // heading
        );
        graphView.getGraphViewStyle().setGridStyle(GraphViewStyle.GridStyle.NONE);
        graphView.addSeries(exampleSeries); // data

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layoutBalanceContainer);
        layout.addView(graphView);
*/

        /*
         * Custom Label graph
		 * use Date as x axis label
		 */
//        Random rand = new Random();
//        int size = 15;
//
//        GraphView.GraphViewData[] data = new GraphView.GraphViewData[size];
//        long now = new Date().getTime();
//        data = new GraphView.GraphViewData[size];
//        for (int i = 0; i < size; i++) {
//            data[i] = new GraphView.GraphViewData(now + (i * 60 * 60 * 24 * 1000), rand.nextInt(20)); // next day
//        }
//
//
//        GraphViewSeries currentSpend = new GraphViewSeries("consumo actual", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(132, 215, 138), 6), new GraphView.GraphViewData[]{
//                new GraphView.GraphViewData(1, 2)
//                , new GraphView.GraphViewData(10, 15)
//                , new GraphView.GraphViewData(15, 20)
//                , new GraphView.GraphViewData(20, 27)
//                , new GraphView.GraphViewData(30, 30)
//        });
//
//        GraphViewSeries proyectedSpend = new GraphViewSeries("consumo proyectado", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(187, 202, 204), 6), new GraphView.GraphViewData[]{
//                new GraphView.GraphViewData(1, 2)
//                , new GraphView.GraphViewData(10, 15)
//                , new GraphView.GraphViewData(15, 20)
//                , new GraphView.GraphViewData(20, 32)
//                , new GraphView.GraphViewData(30, 60)
//        });
//
//
//        GraphViewSeries prepaidBar = new GraphViewSeries("Prepago", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(200, 50, 00), 3), new GraphView.GraphViewData[]{
//                new GraphView.GraphViewData(0, 40)
//                , new GraphView.GraphViewData(30, 40)
//        });
//
//        GraphViewSeries paymentLimit = new GraphViewSeries("Fecha de corte", null, new GraphView.GraphViewData[]{
//                new GraphView.GraphViewData(25, 60)
//                , new GraphView.GraphViewData(25, 0)
//        });
//
//
//        /** determine if line of bar **/
//        GraphView graphView = new LineGraphView(
//                this.getActivity()
//                , "Consumo"
//        );
//        //((LineGraphView) graphView).setDrawBackground(true);
//        ((LineGraphView) graphView).setDrawDataPoints(true);
//        ((LineGraphView) graphView).setDataPointsRadius(7f);
//
//        graphView.addSeries(proyectedSpend); // data
//        graphView.addSeries(currentSpend); // data
//        graphView.addSeries(prepaidBar); // data
//        graphView.addSeries(paymentLimit); // data
//        // set legend
//        graphView.setShowLegend(true);
//        graphView.setLegendAlign(GraphView.LegendAlign.BOTTOM);
//        graphView.getGraphViewStyle().setLegendBorder(10);
//        graphView.getGraphViewStyle().setLegendSpacing(10);
//        graphView.getGraphViewStyle().setLegendWidth(350);
//        graphView.setHorizontalLabels(new String[]{"1", "10", "15", "20", "25", "30"});
//        // set view port, start=2, size=40
//
//		/*
//         * date as label formatter
//		 */
//       /* final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
//        graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
//            @Override
//            public String formatLabel(double value, boolean isValueX) {
//                if (isValueX) {
//                    Date d = new Date((long) value);
//                    return dateFormat.format(d);
//                }
//                return null; // let graphview generate Y-axis label for us
//            }
//        });*/
//        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layoutBalanceContainer);
//        layout.addView(graphView);


//        Button button = (Button) view.findViewById(R.id.buttonPaypalTest);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), BalancePaypalActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        textViewBalanceMoney = (TextView) view.findViewById(R.id.textViewBalanceMoney);
//        textViewBalanceMoneyOut = (TextView) view.findViewById(R.id.textViewBalanceMoneyOutOf);
//        progressBar = (ProgressBar) view.findViewById(R.id.progressBarBalance);
//
//
//
//        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(GoLocky.PREFERENCES, 0);
//        final String userAccountName = settings.getString(GoLocky.PREFERENCES_LOGGED_USER_EMAIL, "");
//
//        /** get user call so we can get the updated balance (might not want to do this and just add

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(TAG);
    }

    /**
     * we need to know when the day ends this month
     *
     * @return
     */
    public String[] getDaysToShowOnCalendar() {
        List<String> days = new ArrayList<>();
        int maxDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        int divider = maxDay / 5;
        for (int i = 1; i < maxDay; i = i + divider) {
            days.add(String.valueOf(i));
        }
        days.add(String.valueOf(maxDay));

        String[] stringArray = days.toArray(new String[days.size()]);

        return stringArray;
    }


    /**
     * Chart things
     *
     * @param setIndex
     * @param entryIndex
     * @param rect
     */
    @SuppressLint("NewApi")
    private void showLineTooltip(int setIndex, int entryIndex, Rect rect) {

        mLineTooltip = (TextView) getActivity().getLayoutInflater().inflate(R.layout.circular_tooltip, null);
        mLineTooltip.setText(Integer.toString((int) lineValues[setIndex][entryIndex]));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Tools.fromDpToPx(35), (int) Tools.fromDpToPx(35));
        layoutParams.leftMargin = rect.centerX() - layoutParams.width / 2;
        layoutParams.topMargin = rect.centerY() - layoutParams.height / 2;
        mLineTooltip.setLayoutParams(layoutParams);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mLineTooltip.setPivotX(layoutParams.width / 2);
            mLineTooltip.setPivotY(layoutParams.height / 2);
            mLineTooltip.setAlpha(0);
            mLineTooltip.setScaleX(0);
            mLineTooltip.setScaleY(0);
            mLineTooltip.animate()
                    .setDuration(150)
                    .alpha(1)
                    .scaleX(1).scaleY(1)
                    .rotation(360)
                    .setInterpolator(enterInterpolator);
        }

        mLineChart.showTooltip(mLineTooltip);
    }

    @SuppressLint("NewApi")
    private void dismissLineTooltip(final int setIndex, final int entryIndex, final Rect rect) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mLineTooltip.animate()
                    .setDuration(100)
                    .scaleX(0).scaleY(0)
                    .alpha(0)
                    .setInterpolator(exitInterpolator).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mLineChart.removeView(mLineTooltip);
                    mLineTooltip = null;
                    if (entryIndex != -1)
                        showLineTooltip(setIndex, entryIndex, rect);
                }
            });
        } else {
            mLineChart.dismissTooltip(mLineTooltip);
            mLineTooltip = null;
            if (entryIndex != -1)
                showLineTooltip(setIndex, entryIndex, rect);
        }
    }

    private void initLineChart() {

        mLineChart.setOnEntryClickListener(lineEntryListener);
        mLineChart.setOnClickListener(lineClickListener);

        mLineGridPaint = new Paint();
        mLineGridPaint.setColor(this.getResources().getColor(R.color.colorPrimaryDarker));
        mLineGridPaint.setPathEffect(new DashPathEffect(new float[]{3, 7}, 0));
        mLineGridPaint.setStyle(Paint.Style.STROKE);
        mLineGridPaint.setAntiAlias(true);
        mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
    }

    private void updateLineChart() {

        mLineChart.reset();

        LineSet dataSet = new LineSet();
//        dataSet.addPoints(getDaysToShowOnCalendar(), lineValues[0]);
//        //dataSet.addPoints(lineLabels, lineValues[0]);
//        dataSet.setDots(true)
//                .setDotsColor(this.getResources().getColor(R.color.line_bg))
//                .setDotsRadius(Tools.fromDpToPx(5))
//                .setDotsStrokeThickness(Tools.fromDpToPx(2))
//                .setDotsStrokeColor(this.getResources().getColor(R.color.line))
//                .setLineColor(this.getResources().getColor(R.color.line))
//                .setLineThickness(Tools.fromDpToPx(3))
//                .beginAt(1).endAt(lineLabels.length - 1);
//        mLineChart.addData(dataSet);

        dataSet = new LineSet();

        dataSet.addPoints(getDaysToShowOnCalendar(), lineValues[1]);

        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();

        dataSet.addPoint("5", 50f);

        dataSet.setLineColor(this.getResources().getColor(R.color.line))
                .setLineThickness(Tools.fromDpToPx(3))
                .setSmooth(true)
                .setDashed(true)
                .setDots(true)
                .setDotsColor(this.getResources().getColor(R.color.line_bg))
                .setDotsRadius(Tools.fromDpToPx(5))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(this.getResources().getColor(R.color.line));
        mLineChart.addData(dataSet);

        mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
                .setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
                .setXAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYAxis(false)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setAxisBorderValues(LINE_MIN, LINE_MAX, 20) // "20" is the spacing and must be a divisor of distance between minValue and maxValue
                .setLabelsMetric(" lts")
                .show(getAnimation(true).setEndAction(null))
        //.show()
        ;

        mLineChart.animateSet(0, new DashAnimation());
    }

    private Animation getAnimation(boolean newAnim) {
        if (newAnim)
            return new Animation()
                    .setAlpha(mCurrAlpha)
                    .setEasing(mCurrEasing)
                    .setOverlap(mCurrOverlapFactor, mCurrOverlapOrder)
                    .setStartPoint(mCurrStartX, mCurrStartY);
        else
            return new Animation()
                    .setAlpha(mOldAlpha)
                    .setEasing(mOldEasing)
                    .setOverlap(mOldOverlapFactor, mOldOverlapOrder)
                    .setStartPoint(mOldStartX, mOldStartY);
    }

    /**
     * Resets the preferences to 0 and then refreshes the fragment
     */
    private void resetPreferencesValuesForReadings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(BalanceFragment.PREF_LAST_READING, 0).commit(); // last reading value (the one we just scanned)
        editor.putString(BalanceFragment.PREF_LAST_READING_DATE, "default").commit(); // last reading date
        editor.putInt(BalanceFragment.PREF_TOTAL_LITERS_FOR_CYCLE, 0).commit(); // total liters for this cycle

        // Refresh the fragment
        Fragment fragment = new BalanceFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }
}
