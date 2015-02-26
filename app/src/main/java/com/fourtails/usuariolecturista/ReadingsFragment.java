package com.fourtails.usuariolecturista;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.support.v7.widget.CardView;
import android.transition.TransitionInflater;
import android.util.Log;
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
public class ReadingsFragment extends Fragment {

    public static final String TAG = "ConsumeFragment";

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

    @InjectView(R.id.linechartCardView)
    CardView linechartCardView;

    @InjectView(R.id.card_view)
    CardView sharedCardView;

    @OnClick(R.id.fabPay)
    public void payButtonClicked() {
        Fragment payOptionsFragment = new PayOptionsFragment();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            makeAnimationBetweenFragments(payOptionsFragment);
        } else {
            MainActivity.bus.post(payOptionsFragment);
        }
    }

    /**
     * This will make a transition with a shared element, in this case the CardView is the shared element
     *
     * @param fragment the fragment that will be used to replace this one
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void makeAnimationBetweenFragments(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        assert fragmentManager != null;

        setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_test));
        setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.slide_right));

        fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_test));
        fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .addSharedElement(sharedCardView, getResources().getString(R.string.transitionFirstCardView))
                .commit();

        Log.d(TAG, "fragment added with transition " + fragment.getTag());
    }

    public ReadingsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_readings, container, false);

        ButterKnife.inject(this, view);

        linechartCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));


        // setting the texts
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastReadingValue = sharedPreferences.getInt(PREF_LAST_READING, 0);

        lastReadingDateValue = sharedPreferences.getString(PREF_LAST_READING_DATE, "No cycle");

        totalLitersForCycleValue = sharedPreferences.getInt(PREF_TOTAL_LITERS_FOR_CYCLE, 0);

        lastReading.setText(String.valueOf(lastReadingValue));


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

        initLineChart();

        updateLineChart();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitleReadings));
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


        //dataSet.addPoint("5", 50f);

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

        editor.putInt(ReadingsFragment.PREF_LAST_READING, 0).commit(); // last reading value (the one we just scanned)
        editor.putString(ReadingsFragment.PREF_LAST_READING_DATE, "default").commit(); // last reading date
        editor.putInt(ReadingsFragment.PREF_TOTAL_LITERS_FOR_CYCLE, 0).commit(); // total liters for this cycle

        // Refresh the fragment
        Fragment fragment = new ReadingsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }
}
