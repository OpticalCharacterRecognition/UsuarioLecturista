package com.fourtails.usuariolecturista.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Resources;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.QuintEase;
import com.db.chart.view.animation.style.DashAnimation;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;
import com.fourtails.usuariolecturista.model.ChartBill;
import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.fourtails.usuariolecturista.ottoEvents.CreateNewBillEvent;
import com.melnykov.fab.FloatingActionButton;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import static com.fourtails.usuariolecturista.MainActivity.allowUserToPrepay;
import static com.fourtails.usuariolecturista.MainActivity.bus;
import static com.fourtails.usuariolecturista.MainActivity.prepayModeEnabled;
import static com.fourtails.usuariolecturista.MainActivity.userHasAPrepay;


/**
 * A simple {@link Fragment} subclass.
 */
public class BillsFragment extends Fragment {

    public static final String TAG = "BillsFragment";

    public static Bus billsBus;

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

    public static double selectedBill;

    /**
     * Line
     */
    //private static LineChartView mLineChart;
    private Paint mLineGridPaint;
    private TextView mLineTooltip;

    private final OnEntryClickListener lineEntryListener = new OnEntryClickListener() {
        @Override
        public void onClick(int setIndex, int entryIndex, Rect rect) {
            System.out.println(setIndex);
            System.out.println(entryIndex);
            if (mLineTooltip == null)
                showLineTooltip(entryIndex, rect);
            else
                dismissLineTooltip(entryIndex, rect);
        }
    };

    private final View.OnClickListener lineClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mLineTooltip != null)
                dismissLineTooltip(-1, null);
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

    @InjectView(R.id.textViewNoBillsMsg)
    TextView textViewNoBills;

    @InjectView(R.id.textViewBillingDateBills)
    TextView textViewBillingDateBills;

    @InjectView(R.id.textViewSelectedBills)
    TextView textViewSelectedBill;

    @InjectView(R.id.textViewBillsStatus)
    TextView textViewBillsStatus;

    @InjectView(R.id.textViewButtonInvitationBills)
    TextView textViewPrepayInvitation;

    @InjectView(R.id.buttonNewBill)
    Button buttonNewBill;

    String selectedBillStatus;

    private float[] chartValues;

    private List<ChartBill> mBills;

    public static int selectedBillIndex;

    Time time;

    @OnClick(R.id.fabPay)
    public void payButtonClicked() {
        if (prepayModeEnabled) {
            Fragment prepaidFragment = new PrepayCalculatorFragment();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                makeAnimationBetweenFragments(
                        prepaidFragment, sharedCardView,
                        getResources().getString(R.string.transitionFirstCardView),
                        android.R.transition.fade, // Exit Transition
                        android.R.transition.move);  // Enter Transition
            } else {
                bus.post(prepaidFragment); // Non lollipop
            }
        } else {
            if (mBills.get(selectedBillIndex).status.equalsIgnoreCase("Unpaid")) {
                Fragment payOptionsFragment = new PayOptionsFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    makeAnimationBetweenFragments(
                            payOptionsFragment, sharedCardView,
                            getResources().getString(R.string.transitionFirstCardView),
                            android.R.transition.fade, // Exit Transition
                            android.R.transition.move);  // Enter Transition
                } else {
                    bus.post(payOptionsFragment); // Non lollipop
                }
            }
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

    @OnClick(R.id.buttonNewBill)
    public void newBillClicked() {
        if (mLineTooltip != null) {
            dismissLineTooltip(-1, null);
        }
        buttonNewBill.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonNewBill.setEnabled(true);
            }
        }, 2000);
        MainActivity.bus.post(new CreateNewBillEvent(CreateNewBillEvent.Type.STARTED, 1));
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

        billsBus = new AndroidBus();
        billsBus.register(this);

        if (MainActivity.userHasAPrepay) { // User has a prepay (negative balance)
            fabPay.setVisibility(View.GONE);
        } else {
            fabPay.setVisibility(View.VISIBLE);
            fabPay.hide();
        }

        lineChartCardViewBills.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryJmas600));

        textViewNoBills.setVisibility(View.GONE);

        if (prepayModeEnabled) {
            fabPay.setImageDrawable(getResources().getDrawable(R.drawable.ic_schedule_white_24dp));
        }

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
                checkBillsFromLocalDB();
            }
        }, 500);
        fabPay.hide();

        return view;

    }


    @Override
    public void onResume() {
        super.onResume();
        // Set title
        if (MainActivity.userHasAPrepay) {
            bus.post(getResources().getString(R.string.toolbarTitleHistoricBills));
        } else {
            bus.post(getResources().getString(R.string.toolbarTitleBills));
        }
        if (prepayModeEnabled) {
            fabPay.setImageDrawable(getResources().getDrawable(R.drawable.ic_schedule_white_24dp));
        } else {
            fabPay.setImageDrawable(getResources().getDrawable(R.drawable.ic_payment_white_24dp));
        }
    }


    /**
     * This is basically just a refresh for when we pay
     *
     * @param option most of the time 1
     */
    @Subscribe
    public void updateBills(Integer option) {
        if (option == 1) {
            checkBillsFromLocalDB();
        }
    }

    /**
     * This might be the most important method here, it basically just gets the bills and puts them
     * into the chart
     * TODO: get the unpaid bills in a different array
     */
    public void checkBillsFromLocalDB() {
        mBills = getBillsForThisMonthRange(2);
        time = new Time();
        if (mBills != null) {
            double highestReading = 0;
            double lowestReading = Integer.MAX_VALUE;
            List<String> xAxisDays = new ArrayList<>();
            chartValues = new float[mBills.size()];
            String lastBillStatus = "";
            double lastBillAmount = 0.0;
            String lastReadingDate = "";
            int j = 0;
            for (ChartBill i : mBills) {
                if (i.amount > highestReading) {
                    highestReading = i.amount;
                }
                if (i.amount < lowestReading) {
                    lowestReading = i.amount;
                }
                time.set(i.timeInMillis);
                xAxisDays.add(time.format("%d/%m"));
                lastReadingDate = time.format("%d/%m/%Y");
                selectedBillIndex = j;
                chartValues[j++] = (float) i.amount;
                lastBillStatus = i.status;
                lastBillAmount = i.amount;
            }

            String[] xAxisDaysArray = xAxisDays.toArray(new String[xAxisDays.size()]);

            updateUi(lastBillAmount, lastBillStatus, lastReadingDate);

            try {
                updateLineChart(xAxisDaysArray, chartValues, lowestReading, highestReading);
            } catch (Exception e) {
                Logger.e(e, "The user most likely pressed back a bunch of times");
            }

        } else {
            lineChartCardViewBills.setVisibility(View.GONE);
            textViewNoBills.setVisibility(View.VISIBLE);
        }
        Logger.d("Finished checkBillsFromLocalDB");
    }

    /**
     * Update the textViews
     *
     * @param lastBillAmount  last bill
     * @param lastBillStatus  last status
     * @param lastReadingDate
     */
    private void updateUi(double lastBillAmount, String lastBillStatus, String lastReadingDate) {
        textViewSelectedBill.setText("$" + String.valueOf(lastBillAmount));
        textViewBillingDateBills.setText(lastReadingDate);
        String statusTranslate;
        selectedBill = lastBillAmount;
        if (prepayModeEnabled && allowUserToPrepay) {
            buttonNewBill.setVisibility(View.GONE);
            fabPay.setImageDrawable(getResources().getDrawable(R.drawable.ic_schedule_white_24dp));
            fabPay.show();
            animateInvitationTextFadeIn(getString(R.string.billsLabelsPrepayInvitation));
            statusTranslate = "Pagada";
        } else {
            fabPay.setImageDrawable(getResources().getDrawable(R.drawable.ic_payment_white_24dp));
            if (lastBillStatus.equalsIgnoreCase("Paid")) {
                statusTranslate = "Pagada";
                animateInvitationTextFadeOut();
                fabPay.hide();
            } else if (lastBillStatus.equalsIgnoreCase("UnPaid")) {
                statusTranslate = "No Pagada";
                animateInvitationTextFadeIn(getString(R.string.billsLabelsPayInvitation));
                fabPay.show();
            } else {
                statusTranslate = "Desconocido";
                animateInvitationTextFadeOut();
                fabPay.hide();
            }
            if (MainActivity.showNewBillButton && !userHasAPrepay) {
                buttonNewBill.setVisibility(View.VISIBLE);
                showDebtCrouton();
            } else {
                buttonNewBill.setVisibility(View.GONE);
            }
        }
        textViewBillsStatus.setText(statusTranslate);
        mLineChart.setVisibility(View.VISIBLE);
    }

    public void animateInvitationTextFadeIn(String textToShow) {
        textViewPrepayInvitation.setAlpha(0f);
        textViewPrepayInvitation.setText(textToShow);
        textViewPrepayInvitation.setVisibility(View.VISIBLE);
        textViewPrepayInvitation.animate()
                .alpha(1f)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }
                });
    }


    public void animateInvitationTextFadeOut() {
        textViewPrepayInvitation.setAlpha(1f);
        textViewPrepayInvitation.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        textViewPrepayInvitation.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * DatabaseQuery
     * gets all the readings for this month range
     *
     * @return >_>
     */
    public static List<ChartBill> getBillsForThisMonthRange(int range) {
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        return new Select()
                .from(ChartBill.class)
                .orderBy("timeInMillis ASC")
//                .where("month >= ?", time.month - range)
//                .and("year = ?", time.year)
                .execute();
    }

    /**
     * Hides the chart then after 500ms makes a transition
     */
    private void hideChartThenMakeTransition() {
        try {
            if (mLineTooltip != null) {
                dismissLineTooltip(-1, null);
            }
            mLineChart.dismiss(getAnimation(false).setEndAction(mMakeTransition));
        } catch (Exception e) {
            Logger.e(e, "Something went wrong trying to hide the chart");
        }
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
            bus.post(readingsFragment);
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
     * @param entryIndex
     * @param rect
     */
    @SuppressLint("NewApi")
    private void showLineTooltip(int entryIndex, Rect rect) {

        try {
            selectedBillIndex = entryIndex;

            mLineTooltip = (TextView) getActivity().getLayoutInflater().inflate(R.layout.circular_tooltip, null);
            mLineTooltip.setText(Integer.toString((int) chartValues[entryIndex]));

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Tools.fromDpToPx(35), (int) Tools.fromDpToPx(35));
            layoutParams.leftMargin = rect.centerX() - layoutParams.width / 2;
            layoutParams.topMargin = rect.centerY() - layoutParams.height / 2;
            mLineTooltip.setLayoutParams(layoutParams);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
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

            selectedBill = chartValues[entryIndex];
            selectedBillStatus = mBills.get(entryIndex).status;
            time.set(mBills.get(entryIndex).timeInMillis);
            String selectedDate = time.format("%d/%m/%Y");
            // Prepay logic
            if (prepayModeEnabled) {
                selectedBillStatus = "Pagada";
            } else {
                if (selectedBillStatus.equalsIgnoreCase("Paid")) {
                    selectedBillStatus = "Pagada";
                    animateInvitationTextFadeOut();
                    fabPay.hide();
                } else if (selectedBillStatus.equalsIgnoreCase("UnPaid")) {
                    selectedBillStatus = "No Pagada";
                    animateInvitationTextFadeIn(getString(R.string.billsLabelsPayInvitation));
                    fabPay.show();
                } else {
                    selectedBillStatus = "Desconocido";
                    animateInvitationTextFadeOut();
                    fabPay.hide();
                }
            }
            textViewBillsStatus.setText(selectedBillStatus);
            textViewSelectedBill.setText("$" + String.valueOf(selectedBill));
            textViewBillingDateBills.setText(selectedDate);
            mLineChart.showTooltip(mLineTooltip);
        } catch (Exception e) {
            Logger.e("why is this crash happening?");
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void dismissLineTooltip(final int entryIndex, final Rect rect) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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
                            showLineTooltip(entryIndex, rect);
                    }
                });
            } else {
                mLineChart.dismissTooltip(mLineTooltip);
                mLineTooltip = null;
                if (entryIndex != -1)
                    showLineTooltip(entryIndex, rect);
            }
        } catch (Exception e) {
            Logger.e("why is this crash happening?");
            e.printStackTrace();
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

    private void updateLineChart(String[] xAxisDaysArray, float[] chartValues, double lowestReading, double highestReading) {

        try {
            double tempSpacing = ((highestReading - lowestReading) / xAxisDaysArray.length);
            int spacing = (int) Math.ceil(tempSpacing);
            if (spacing == 0) {
                spacing = 1;
            }
            mLineChart.reset();

            // this will change the dots color making the "Unpaid" ones in red
            LineSet dataSet = new LineSet();

            Point point;
            for (int i = 0; i < xAxisDaysArray.length; i++) {
                point = new Point(xAxisDaysArray[i], chartValues[i]);
                if (mBills.get(i).status.equalsIgnoreCase("Unpaid")) {
                    point.setColor(this.getResources().getColor(R.color.red_300));
                } else {
                    point.setColor(this.getResources().getColor(R.color.colorJmasBlueReadings));
                }
                dataSet.addPoint(point);
            }
            dataSet.setDotsRadius(Tools.fromDpToPx(5))
                    .setDotsStrokeThickness(Tools.fromDpToPx(2))
                    .setDotsStrokeColor(this.getResources().getColor(R.color.line))
                    .setThickness(Tools.fromDpToPx(3))
                    .setColor(this.getResources().getColor(R.color.whiteWater))
                    .setDashed(new float[]{10, 10});
            mLineChart.addData(dataSet);


            mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
                    .setLabelsFormat(new DecimalFormat("'$ '##"))
                    .setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
                    .setXAxis(false)
                    .setXLabels(XController.LabelPosition.OUTSIDE)
                    .setYAxis(false)
                    .setYLabels(YController.LabelPosition.OUTSIDE)
                    .setAxisBorderValues((int) lowestReading, (int) highestReading, spacing)
                    .show(getAnimation(true).setEndAction(null))
            ;

            mLineChart.animateSet(0, new DashAnimation());
        } catch (Resources.NotFoundException e) {
            Logger.e("why is this crash happening?");
            e.printStackTrace();
        }
    }

    private void addPoint() {
        float[] thing = {0f, 25f, 26f, 39f, 42f, 30f, 100f};
        mLineChart.updateValues(0, thing);
        mLineChart.notifyDataUpdate();

    }

    /**
     * Shows am infinite crouton to show image is being upload to the server
     */
    private void showDebtCrouton() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.crouton_normal_custom_view, null);

        TextView textView = (TextView) view.findViewById(R.id.textViewNormalCustomCrouton);

        textView.setText("Tiene adeudo en su cuenta, por favor cree una nueva factura para pagar");

        Configuration configuration = new Configuration.Builder()
                .setDuration(Configuration.DURATION_LONG)
                .build();
        Crouton crouton;
        crouton = Crouton.make(getActivity(), view, mLineChart).setConfiguration(configuration);
        crouton.show();
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
