package com.fourtails.usuariolecturista.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.text.format.Time;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.QuintEase;
import com.db.chart.view.animation.style.DashAnimation;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;
import com.fourtails.usuariolecturista.camera.CameraScreenActivity;
import com.fourtails.usuariolecturista.model.ChartReading;
import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.melnykov.fab.FloatingActionButton;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


/**
 * This is the balance fragment where it shows your metrics for the last reading, this month etc,
 * also calls for a facebook publish
 */
public class ReadingsFragment extends Fragment {

    public static final String TAG = "ReadingsFragment";

    public static Bus readingsBus;

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

    private boolean isAnimationRunning = false;

    @InjectView(R.id.lineChartReadings)
    LineChartView mLineChart;

    /**
     * Injected views and clickListeners ********************************************************
     */
    @InjectView(R.id.fabScan)
    FloatingActionButton fabScan;

    @OnClick(R.id.fabScan)
    public void scanButtonClicked() {
        Intent cameraActivity = new Intent(getActivity(), CameraScreenActivity.class);
        MainActivity.bus.post(cameraActivity);
    }

    @InjectView(R.id.fabChangeGraph)
    FloatingActionButton fabChangeGraph;

    @InjectView(R.id.cardViewReadings)
    CardView linechartCardView;

    @InjectView(R.id.cardViewReadingsBottom)
    CardView sharedCardView;

    @InjectView(R.id.progressBarReadings)
    ProgressBar progressBar;

    @InjectView(R.id.textViewNoReadingsMsg)
    TextView textViewNoReadings;

    @InjectView(R.id.textViewTotalReadingsForThisPeriod)
    TextView textViewTotalLitersForThisPeriod;

    @InjectView(R.id.textViewLastReadingDate)
    TextView textViewLastReadingDate;

    @InjectView(R.id.textViewButtonInvitationReadings)
    TextView textViewButtonInvitationReadings;


    private float[] chartValues;
    private float[] chartValuesForAnimation;

    List<ChartReading> mReadings;

    Time time;

    @OnClick(R.id.fabChangeGraph)
    public void changeGraphClicked() {
        if (!isAnimationRunning) {
            fabChangeGraph.setEnabled(false);
            isAnimationRunning = true;
            hideChartThenMakeTransition();
        }
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
        View view = inflater.inflate(R.layout.fragment_readings, container, false);

        ButterKnife.inject(this, view);

        textViewButtonInvitationReadings.setVisibility(View.INVISIBLE);

        readingsBus = new AndroidBus();
        readingsBus.register(this);

        if (!MainActivity.userHasAPrepay) {
            fabScan.setVisibility(View.VISIBLE);
            fabScan.hide();
        } else {
            fabScan.setVisibility(View.GONE);
        }

        linechartCardView.setCardBackgroundColor(getResources().getColor(R.color.colorJmasBlueReadings));

        fabChangeGraph.setEnabled(false);
        textViewNoReadings.setVisibility(View.GONE);

        /** Chart things **/
        mCurrOverlapFactor = .5f;
        mCurrEasing = new QuintEase();
        mCurrStartX = -1;
        mCurrStartY = 0;
        mCurrAlpha = -1;

        mOldOverlapFactor = 1;
        mOldEasing = new QuintEase();
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
                fabScan.show();
                if (!MainActivity.userHasAPrepay) {
                    animateInvitationTextFadeIn();
                }
            }
        }, 500);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        if (MainActivity.userHasAPrepay) {
            MainActivity.bus.post(getResources().getString(R.string.toolbarTitleHistoricReadings));
        } else {
            MainActivity.bus.post(getResources().getString(R.string.toolbarTitleReadings));
        }
        if (MainActivity.ranAtLeastOnce) {
            checkReadingsFromLocalDB(3);
        }
    }

    /**
     * Main Activity will call us when it finishes to get he data from the backend
     *
     * @param action just because we need an object to make the otto call
     */
    @Subscribe
    public void checkReadingsFromLocalDB(Integer action) {
        mReadings = getReadingsForThisMonthRange(2);
        time = new Time();
        if (mReadings != null) {
            long highestReading = 0;
            long lowestReading = Integer.MAX_VALUE;
            List<String> xAxisDays = new ArrayList<>();
            chartValues = new float[mReadings.size()];
            chartValuesForAnimation = new float[mReadings.size()];
            String lastReadingDate = "";
            int j = 0;
            for (ChartReading i : mReadings) {
                if (i.value > highestReading) {
                    highestReading = i.value;
                }
                if (i.value < lowestReading) {
                    lowestReading = i.value;
                }
                time.set(i.timeInMillis);
                xAxisDays.add(time.format("%d/%m"));
                lastReadingDate = time.format("%d/%m/%Y");
                chartValues[j] = i.value;
                if (!MainActivity.isFirstTime && i.timeInMillis > MainActivity.oldReadingsLastDateInMillis) {
                    chartValuesForAnimation[j] = lowestReading; // we want the lowest so we animate from there to the top
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updatePoint();
                        }
                    }, 1000);
                } else {
                    chartValuesForAnimation[j] = i.value;
                }
                j++;
            }
            String[] xAxisDaysArray = xAxisDays.toArray(new String[xAxisDays.size()]);
            long totalForThisPeriod = highestReading - lowestReading;
            updateUi(totalForThisPeriod, lastReadingDate);
            // keep in mind that charValuesForAnimation are the same if there is no "new" reading to animate
            updateLineChart(xAxisDaysArray, chartValuesForAnimation, lowestReading, highestReading);
            fabChangeGraph.setEnabled(true);
            MainActivity.ranAtLeastOnce = true;
        } else {
            progressBar.setVisibility(View.GONE);
            textViewNoReadings.setVisibility(View.VISIBLE);
        }
        Logger.d("Finished checkReadingsFromLocalDB");

    }

    /**
     * Will basically hide with a fade the indeterminate progress bar and will show the cardView
     * and the chart
     */
    private void updateUi(long totalForThisPeriod, String lastReadingDate) {
        crossfade();
        textViewTotalLitersForThisPeriod.setText(String.valueOf(totalForThisPeriod) + " m3");
        textViewLastReadingDate.setText(lastReadingDate);
        mLineChart.setVisibility(View.VISIBLE);
    }

    private void updateLineChart(String[] xAxisDaysArray, float[] valuesArray, long lowestReading, long highestReading) {
        double tempSpacing = ((highestReading - lowestReading) / xAxisDaysArray.length);
        int spacing = (int) Math.ceil(tempSpacing);
        if (spacing == 0) {
            spacing = 1;
        }
        mLineChart.reset();

        LineSet dataSet = new LineSet(xAxisDaysArray, valuesArray);

        dataSet.setColor(this.getResources().getColor(R.color.line))
                .setThickness(Tools.fromDpToPx(3))
                .setSmooth(true)
                .setDashed(new float[]{10, 10})
                .setDotsColor(this.getResources().getColor(R.color.colorPrimaryJmas))
                .setDotsRadius(Tools.fromDpToPx(5))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(this.getResources().getColor(R.color.line));
        mLineChart.addData(dataSet);

        mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
                .setLabelsFormat(new DecimalFormat("##' m3'"))
                .setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
                .setXAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYAxis(false)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setAxisBorderValues((int) lowestReading, (int) highestReading, spacing)
                .show(getAnimation(true).setEndAction(null))
        ;

        mLineChart.animateSet(0, new DashAnimation());
    }

    /**
     * DatabaseQuery
     * gets all the readings for this month range
     *
     * @return >_>
     */
    public static List<ChartReading> getReadingsForThisMonthRange(int range) {
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        return new Select()
                .from(ChartReading.class)
//                .orderBy("timeInMillis ASC")
//                .where("month >= ?", time.month - range)
//                .and("year = ?", time.year)
                .execute();
    }

    private void crossfade() {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        linechartCardView.setAlpha(0f);
        linechartCardView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        linechartCardView.animate()
                .alpha(1f)
                .setDuration(MainActivity.mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        progressBar.animate()
                .alpha(0f)
                .setDuration(MainActivity.mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
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
        mLineTooltip.setText(Integer.toString((int) chartValues[entryIndex]));

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
        time.set(mReadings.get(entryIndex).timeInMillis);
        String selectedReadingDate = time.format("%d/%m/%Y");
        textViewLastReadingDate.setText(selectedReadingDate);
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

//    private void hideChart() {
//        mLineChart.dismiss(getAnimation(false).setEndAction(mExitEndAction));
//    }

    /**
     * Hides the chart then after 500ms makes a transition
     */
    private void hideChartThenMakeTransition() {
        if (mLineTooltip != null) {
            dismissLineTooltip(-1, -1, null);
        }
        mLineChart.dismiss(getAnimation(false).setEndAction(mMakeTransition));
    }


    /**
     * Sets up a fragment and passes the parameters to make a shared element transition
     */
    public void changeGraphClickedAction() {
        Fragment billsFragment = new BillsFragment();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            makeAnimationBetweenFragments(
                    billsFragment, fabChangeGraph,
                    getResources().getString(R.string.transitionReadingsToBills),
                    android.R.transition.fade, // Exit Transition
                    android.R.transition.move); // Enter Transition
        } else {
            MainActivity.bus.post(billsFragment);
        }
        fabChangeGraph.setEnabled(true);
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
     * Updates the chart to show a "significant" way that there has been a new reading
     */
    private void updatePoint() {
        try {
            Crouton.makeText(getActivity(), "Nueva Lectura", new Style.Builder().setBackgroundColor(R.color.blue_400).build(), linechartCardView).show();

            mLineChart.updateValues(0, chartValues);
            mLineChart.notifyDataUpdate();
        } catch (Exception e) {
            Logger.e(e, "Error trying to update the chart last point");
        }

    }

    public void animateInvitationTextFadeIn() {
        textViewButtonInvitationReadings.setAlpha(0f);
        textViewButtonInvitationReadings.setVisibility(View.VISIBLE);
        textViewButtonInvitationReadings.animate()
                .alpha(1f)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }
                });
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
