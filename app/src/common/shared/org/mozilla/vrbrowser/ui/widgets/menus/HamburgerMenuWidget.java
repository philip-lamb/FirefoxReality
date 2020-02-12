package org.mozilla.vrbrowser.ui.widgets.menus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.IntDef;

import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.ui.widgets.WidgetPlacement;
import org.mozilla.vrbrowser.utils.AnimationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class HamburgerMenuWidget extends MenuWidget {

    public interface MenuDelegate {
        void onSendTab();
        void onResize();
        void onSwitchMode();
    }

    @IntDef(value = { SWITCH_MODE, WINDOW_RESIZE, SEND_TAB})
    public @interface Action {}
    public static final int SEND_TAB = 0;
    public static final int WINDOW_RESIZE = 1;
    public static final int SWITCH_MODE = 2;

    HashMap<Integer, MenuItem> mItems;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<MenuDelegate> mMenuDelegate;
    boolean mSendTabEnabled = true;

    public HamburgerMenuWidget(Context aContext) {
        super(aContext, R.layout.hamburger_menu);
        initialize();
    }

    private void initialize() {
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        mAdapter.updateBackgrounds(R.drawable.context_menu_item_background_first,
                R.drawable.context_menu_item_background_last,
                R.drawable.context_menu_item_background,
                R.drawable.context_menu_item_background_single);
        mAdapter.updateLayoutId(R.layout.hamburger_menu_item);

        updateMenuItems();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateUI();
    }

    @Override
    public void show(int aShowFlags) {
        super.show(aShowFlags);

        AnimationHelper.scaleIn(findViewById(R.id.menuContainer), 100, 0, null);
    }

    @Override
    public void hide(int aHideFlags) {
        AnimationHelper.scaleOut(findViewById(R.id.menuContainer), 100, 0, () -> HamburgerMenuWidget.super.hide(aHideFlags));
    }

    @Override
    protected void initializeWidgetPlacement(WidgetPlacement aPlacement) {
        aPlacement.visible = false;
        aPlacement.width =  WidgetPlacement.dpDimension(getContext(), R.dimen.hamburger_menu_width);
        aPlacement.parentAnchorX = 1.0f;
        aPlacement.parentAnchorY = 1.0f;
        aPlacement.anchorX = 1.0f;
        aPlacement.anchorY = 0.0f;
        aPlacement.translationX = 20;
        aPlacement.translationY = 10;
        aPlacement.translationZ = WidgetPlacement.unitFromMeters(getContext(), R.dimen.context_menu_z_distance);
    }

    public void setUAMode(int uaMode) {
        switch (uaMode) {
            case GeckoSessionSettings.USER_AGENT_MODE_DESKTOP:
                    mItems.get(SWITCH_MODE).mImageId = R.drawable.ic_icon_ua_desktop;
                    break;

            case GeckoSessionSettings.USER_AGENT_MODE_MOBILE:
            case GeckoSessionSettings.USER_AGENT_MODE_VR:
                mItems.get(SWITCH_MODE).mImageId = R.drawable.ic_icon_ua_default;
                break;

        }

        mListView.invalidateViews();
    }

    public void setMenuDelegate(MenuDelegate delegate) {
        mMenuDelegate = Optional.ofNullable(delegate);
    }

    @SuppressLint("UseSparseArrays")
    private void updateMenuItems() {
        mItems = new HashMap<>();

        if (mSendTabEnabled) {
            mItems.put(SEND_TAB,
                    new MenuItem(getContext().getString(R.string.hamburger_menu_send_tab),
                            R.drawable.ic_icon_tabs_sendtodevice,
                            () -> mMenuDelegate.ifPresent(MenuDelegate::onSendTab)));
        }

        mItems.put(WINDOW_RESIZE,
                new MenuItem(getContext().getString(R.string.hamburger_menu_resize),
                        R.drawable.ic_icon_resize,
                        () -> mMenuDelegate.ifPresent(MenuDelegate::onResize)));

        mItems.put(SWITCH_MODE,
                new MenuItem(getContext().getString(R.string.hamburger_menu_switch_to_desktop),
                        R.drawable.ic_icon_ua_default,
                        () -> mMenuDelegate.ifPresent(MenuDelegate::onSwitchMode)));

        super.updateMenuItems(new ArrayList<>(mItems.values()));

        mWidgetPlacement.height = mItems.size() * WidgetPlacement.dpDimension(getContext(), R.dimen.hamburger_menu_item_height);
        mWidgetPlacement.height += mBorderWidth * 2;
        mWidgetPlacement.height += WidgetPlacement.dpDimension(getContext(), R.dimen.hamburger_menu_triangle_height);
    }

    public void setSendTabEnabled(boolean value) {
        mSendTabEnabled = value;
        updateMenuItems();
    }

}
