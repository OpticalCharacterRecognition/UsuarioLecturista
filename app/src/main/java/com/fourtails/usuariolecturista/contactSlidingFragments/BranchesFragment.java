package com.fourtails.usuariolecturista.contactSlidingFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fourtails.usuariolecturista.R;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class BranchesFragment extends Fragment {


    public BranchesFragment() {
        // Required empty public constructor
    }

    private static final String ARG_POSITION = "position";

    private int position;

    public static BranchesFragment newInstance(int position) {
        BranchesFragment f = new BranchesFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_branches, container, false);
        ButterKnife.inject(this, view);
        return view;
    }


}
