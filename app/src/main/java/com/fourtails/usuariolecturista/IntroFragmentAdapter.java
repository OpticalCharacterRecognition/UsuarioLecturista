package com.fourtails.usuariolecturista;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fourtails.usuariolecturista.introFragments.IntroLastFragment;
import com.fourtails.usuariolecturista.introFragments.IntroMeterFragment;
import com.fourtails.usuariolecturista.introFragments.IntroPayFragment;
import com.fourtails.usuariolecturista.introFragments.IntroPromotionFragment;
import com.fourtails.usuariolecturista.introFragments.IntroWelcomeFragment;

class IntroFragmentAdapter extends FragmentPagerAdapter {

    private int mCount = 5;

    public IntroFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return IntroWelcomeFragment.newInstance();
            case 1:
                return IntroMeterFragment.newInstance();
            case 2:
                return IntroPayFragment.newInstance();
            case 3:
                return IntroPromotionFragment.newInstance();
            case 4:
                return IntroLastFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}