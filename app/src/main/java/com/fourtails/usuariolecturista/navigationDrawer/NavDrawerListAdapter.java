package com.fourtails.usuariolecturista.navigationDrawer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fourtails.usuariolecturista.R;

import java.util.ArrayList;

/**
 * This adapter is for displaying the navigation drawer, it contains the getView which does all the
 * "rendering" magic.
 */
public class NavDrawerListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems) {
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        assert convertView != null;
        LinearLayout container = (LinearLayout) convertView.findViewById(R.id.navDrawerContainer);

        // Normal Items
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imageButtonDrawerIcon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.textViewDrawer);
        TextView textCount = (TextView) convertView.findViewById(R.id.counter);

        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());

        imgIcon.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);

        // check whether it set visible or not
        if (navDrawerItems.get(position).getCounterVisibility()) {
            textCount.setText(navDrawerItems.get(position).getCount());
        }else{
            // hide the counter view
            textCount.setVisibility(View.GONE);
        }

        return convertView;
    }
}
