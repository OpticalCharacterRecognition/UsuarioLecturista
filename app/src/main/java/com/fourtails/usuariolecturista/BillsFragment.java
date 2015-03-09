package com.fourtails.usuariolecturista;


import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class BillsFragment extends Fragment {

    public static final String TAG = "BillsFragment";

    /**
     * Charts ***********************************************************************************
     */

    private final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
    private final TimeInterpolator exitInterpolator = new AccelerateInterpolator();

    private static float mCurrOverlapFactor;
    private static int[] mCurrOverlapOrder;
    private static float mOldOverlapFactor;
    private static int[] mOldOverlapOrder;

    /**
     * Ease
     */
    private static BaseEasingMethod mCurrEasing;
    private static BaseEasingMethod mOldEasing;

    /**
     * Enter
     */
    private static float mCurrStartX;
    private static float mCurrStartY;
    private static float mOldStartX;
    private static float mOldStartY;

    /**
     * Alpha
     */
    private static int mCurrAlpha;
    private static int mOldAlpha;

    /**
     * Line
     */
    private static int LINE_MAX = 100;
    private static int LINE_MIN = 0;
    private final static float[][] lineValues = {{0, 25f, 26f, 39f, 42f, 0f, 70f},
            {0, 25f, 26f, 39f, 42f, 60f}};
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

    private Handler mHandler;


    /**
     * This will run the update after 500ms, it fires after a dismiss on the chart
     */
    private final Runnable mExitEndAction = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    updateChart();
                }
            }, 500);
        }
    };

    /**
     * This will run the update after 50ms, it fires after a dismiss on the chart and will attempt
     * the transition
     */
    private final Runnable mMakeTransition = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    changeGraphClickedAction();
                }
            }, 50);
        }
    };


    /**
     * fires after the drawing of the last chart
     */
    private final Runnable mAnimatePoint = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    addPoint();
                }
            }, 500);
        }
    };

    private boolean isAnimationRunning = false;

    private boolean mNewInstance;

    @InjectView(R.id.lineChartBills)
    LineChartView mLineChart;

    /**
     * Injected views and clickListeners ********************************************************
     */

    @InjectView(R.id.fabPay)
    FloatingActionButton fabPay;

    @InjectView(R.id.fabChangeGraphBills)
    FloatingActionButton fabChangeGraphBills;

    @InjectView(R.id.cardViewBills)
    CardView lineChartCardViewBills;

    @InjectView(R.id.cardViewBillsBottom)
    CardView sharedCardView;

    @OnClick(R.id.fabPay)
    public void payButtonClicked() {
        Fragment payOptionsFragment = new PayOptionsFragment();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            makeAnimationBetweenFragments(
                    payOptionsFragment, sharedCardView,
                    getResources().getString(R.string.transitionFirstCardView),
                    android.R.transition.fade, // Exit Transition
                    android.R.transition.move);  // Enter Transition
        } else {
            MainActivity.bus.post(payOptionsFragment);
        }
    }


    @OnClick(R.id.fabChangeGraphBills)
    public void changeGraphClicked() {
        if (!isAnimationRunning) {
            fabChangeGraphBills.setEnabled(false);
            isAnimationRunning = true;
            hideChartThenMakeTransition();
        }
    }


    public BillsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bills, container, false);
        ButterKnife.inject(this, view);

        lineChartCardViewBills.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryJmas600));

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

        mHandler = new Handler();

        initLineChart();

        // the reason we want this on a handler is because it appears to look better if we
        // let the transition finish and then animate our chart
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLineChart();
            }
        }, 500);

        return view;

    }


    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitleBills));
    }

    /**
     * Hides the chart then after 500ms makes a transition
     */
    private void hideChartThenMakeTransition() {
        mLineChart.dismiss(getAnimation(false).setEndAction(mMakeTransition));
    }

    /**
     * Sets up a fragment and passes the parameters to make a shared element transition
     */
    private void changeGraphClickedAction() {
        Fragment readingsFragment = new ReadingsFragment();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            makeAnimationBetweenFragments(
                    readingsFragment, fabChangeGraphBills,
                    getResources().getString(R.string.transitionReadingsToBills),
                    android.R.transition.fade, // Exit Transition
                    android.R.transition.move); // Enter Transition
        } else {
            MainActivity.bus.post(readingsFragment);
        }
        fabChangeGraphBills.setEnabled(true);
        isAnimationRunning = false;
    }

    /**
     * This will make a transition with a shared element, in this case the CardView is the shared element
     *
     * @param fragment   the fragment that will be used to replace this one
     * @param sharedView the shared element between the fragments
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void makeAnimationBetweenFragments(Fragment fragment, View sharedView, String sharedTransitionName, int exitTransition, int enterTransition) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        assert fragmentManager != null;

        setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_test));
        setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(exitTransition));

        fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_test));
        fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(enterTransition));

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .addSharedElement(sharedView, sharedTransitionName)
                .commit();

        Log.d(TAG, "fragment added with transition " + fragment.getTag());
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
        mLineGridPaint.setColor(this.getResources().getColor(R.color.colorPrimaryJmas400));
        mLineGridPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        mLineGridPaint.setStyle(Paint.Style.STROKE);
        mLineGridPaint.setAntiAlias(true);
        mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.5f));
    }

    private void updateLineChart() {

        mLineChart.reset();

        LineSet dataSet = new LineSet();
        dataSet.addPoints(getDaysToShowOnCalendar(), lineValues[1]);
        //dataSet.addPoints(lineLabels, lineValues[0]);
        dataSet.setDots(true)
                .setDotsColor(this.getResources().getColor(R.color.line_bg))
                .setDotsRadius(Tools.fromDpToPx(5))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(this.getResources().getColor(R.color.line))
                .setLineColor(this.getResources().getColor(R.color.line))
                .setLineThickness(Tools.fromDpToPx(3))
                .beginAt(1).endAt(getDaysToShowOnCalendar().length - 1);
        mLineChart.addData(dataSet);

        dataSet = new LineSet();
//        we will need to wait for the developer to implement this functionality
//        String[] days = getDaysToShowOnCalendar();
//        Point point;
//        for (int i = 0; i < 5; i++) {
//            point = new Point(days[i], lineValues[1][i]);
//            if (i == 3) {
//                point.setColor(this.getResources().getColor(R.color.blue_balance));
//            } else {
//                point.setColor(this.getResources().getColor(R.color.whiteWater));
//            }
//            dataSet.addPoint(point);
//        }

        dataSet.addPoints(getDaysToShowOnCalendar(), lineValues[1]);


        //dataSet.addPoint("5", 50f);

//        dataSet.setLineColor(this.getResources().getColor(R.color.line))
//                .setLineThickness(Tools.fromDpToPx(3))
//                .setSmooth(true)
//                .setDashed(true)
//                .setDots(true)
//                .setDotsColor(this.getResources().getColor(R.color.colorPrimaryJmas))
//                .setDotsRadius(Tools.fromDpToPx(5))
//                .setDotsStrokeThickness(Tools.fromDpToPx(2))
//                .setDotsStrokeColor(this.getResources().getColor(R.color.line));
//        mLineChart.addData(dataSet);

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

    /**
     * we need to know when the day ends this month
     *
     * @return an string array of the days
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

    private void addPoint() {
        float[] thing = {0f, 25f, 26f, 39f, 42f, 30f, 100f};
        mLineChart.updateValues(0, thing);
        mLineChart.notifyDataUpdate();

    }

    private void hideChart() {
        mLineChart.dismiss(getAnimation(false).setEndAction(mExitEndAction));
    }

    /**
     * This will be executed by mExitEndAction because we first need to show a dismiss animation
     */
    private void updateChart() {
        mLineChart.reset();
        LineSet dataSet = new LineSet();
        float[] newlineValues = {0, 25f, 26f, 39f, 42f, 30f, 30f};
        String[] newLabels = {"1", "7", "13", "19", "25", "30", "5"};
        dataSet.addPoints(newLabels, newlineValues);

        dataSet.setLineColor(this.getResources().getColor(R.color.line))
                .setLineThickness(Tools.fromDpToPx(3))
                .setSmooth(true)
                .setDashed(true)
                .setDots(true)
                .setDotsColor(this.getResources().getColor(R.color.colorPrimaryJmas))
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
                .show(getAnimation(true).setEndAction(mAnimatePoint));
        mLineChart.animateSet(0, new DashAnimation());

        //addPoint();


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
}
