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

        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());

        /** This logic is used in case we want the first item to be the app icon **/
        // Bigger item just for the mascot
//        ImageView imgIconLogo = (ImageView) convertView.findViewById(R.id.imageButtonDrawerIconLogo);
//        TextView txtTitleLogo = (TextView) convertView.findViewById(R.id.textViewDrawerLogo);
//
//        imgIconLogo.setImageResource(navDrawerItems.get(position).getIcon());
//        txtTitleLogo.setText(navDrawerItems.get(position).getTitle());
//
//        // position zero means that is the mascot, and that is why we make the height bigger, and
//        // "render" the different icon and text, and hide the others
//        if (position == 0) {
//            container.setMinimumHeight(100);
//            imgIcon.setVisibility(View.GONE);
//            txtTitle.setVisibility(View.GONE);
//            imgIconLogo.setVisibility(View.VISIBLE);
//            txtTitleLogo.setVisibility(View.VISIBLE);
//        } else {
//            imgIcon.setVisibility(View.VISIBLE);
//            txtTitle.setVisibility(View.VISIBLE);
//            imgIconLogo.setVisibility(View.GONE);
//            txtTitleLogo.setVisibility(View.GONE);
//
//        }

        imgIcon.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);


        /** Future Proof for displaying count**/
        // check whether it set visible or not
/*        if(navDrawerItems.get(position).getCounterVisibility()){
            txtCount.setText(navDrawerItems.get(position).getCount());
        }else{
            // hide the counter view
            txtCount.setVisibility(View.GONE);
        }*/

        return convertView;
    }
}
