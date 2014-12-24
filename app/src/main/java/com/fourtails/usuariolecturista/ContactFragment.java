package com.fourtails.usuariolecturista;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.fourtails.usuariolecturista.contactSlidingFragments.BranchesFragment;
import com.fourtails.usuariolecturista.contactSlidingFragments.CustomerSupport;
import com.fourtails.usuariolecturista.contactSlidingFragments.PaymentCenters;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This will be a tabbed view containing multiple contact info
 */
public class ContactFragment extends Fragment {

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    private MyPagerAdapter adapter;
    private Drawable oldBackground = null;
    private int currentColor = 0xFF666666;


    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.inject(this, view);

        adapter = new MyPagerAdapter(getActivity().getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
        tabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitleContactFragment));
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {"SUCURSALES", "CENTROS DE PAGO", "ATENCION A CLIENTES"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BranchesFragment.newInstance(position);
                case 1:
                    return PaymentCenters.newInstance(position);
                case 2:
                    return CustomerSupport.newInstance(position);
                default:
                    return null;
            }
        }

    }
}
