package com.fourtails.usuariolecturista.navigationDrawer;

/**
 * Custom NavDrawer item that gets drawn into the drawer
 * Has an item with an icon and can have a counter, we most
 * likely wont use the counter
 */
public class NavDrawerItem {
    private String title;
    private int icon;
    private String count = "0";
    // boolean to set visiblity of the counter
    private boolean isCounterVisible = false;

    public NavDrawerItem() {
    }

    /**
     * Constructor for default kind of item
     *
     * @param title
     * @param icon
     */
    public NavDrawerItem(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    /**
     * Constructor with counter
     * @param title
     * @param icon
     * @param isCounterVisible
     * @param count
     */
    public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count) {
        this.title = title;
        this.icon = icon;
        this.isCounterVisible = isCounterVisible;
        this.count = count;
    }

    public String getTitle() {
        return this.title;
    }

    public int getIcon() {
        return this.icon;
    }

    public String getCount() {
        return this.count;
    }

    public boolean getCounterVisibility() {
        return this.isCounterVisible;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setCounterVisibility(boolean isCounterVisible) {
        this.isCounterVisible = isCounterVisible;
    }
}
