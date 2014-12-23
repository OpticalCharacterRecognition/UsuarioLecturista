package com.fourtails.usuariolecturista;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fourtails.usuariolecturista.utilities.SwipeDismissRecyclerViewTouchListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {


    private static final int ITEMS_COUNT = 4;
    private List<String> mItems;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter<CustomViewHolder> mAdapter;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.inject(this, view);
        initData();
        initRecyclerView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitleNotificationsFragment));
    }

    private void initData() {
        mItems = new ArrayList<String>();
        for (int i = 0; i < ITEMS_COUNT; i++) {
            mItems.add("Notificacion " + (i + 1));
        }
    }

    private void initRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RecyclerView.Adapter<CustomViewHolder>() {
            @Override
            public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_list
                        , viewGroup, false);
                return new CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
                viewHolder.mTextViewNotificationLabel.setText(mItems.get(i));
            }

            @Override
            public int getItemCount() {
                return mItems.size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        SwipeDismissRecyclerViewTouchListener touchListener =
                new SwipeDismissRecyclerViewTouchListener(
                        mRecyclerView,
                        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
//                                    mLayoutManager.removeView(mLayoutManager.getChildAt(position));
                                    mItems.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        });
        mRecyclerView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(getActivity(), "Clicked " + mItems.get(position), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextViewNotificationLabel;
        private TextView mTextViewNotificationTime;
        private TextView mTextViewNotificationContent;


        public CustomViewHolder(View itemView) {
            super(itemView);

            mTextViewNotificationLabel = (TextView) itemView.findViewById(R.id.textViewNotificationItemLabel);
            mTextViewNotificationTime = (TextView) itemView.findViewById(R.id.textViewNotificationItemTime);
            mTextViewNotificationContent = (TextView) itemView.findViewById(R.id.textViewNotificationItemContent);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
    }
}
