/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.liquid;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.liquid.QuickSettingsUtil.TileInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public class QuickSettingsTiles extends Fragment {

    private static final int MENU_RESET = Menu.FIRST;

    DraggableGridView mDragView;
    private ViewGroup mContainer;
    LayoutInflater mInflater;
    Resources mSystemUiResources;
    SharedPreferences prefs;
    Resources res;
    TileAdapter mTileAdapter;
    static ArrayList<String> curr;
    Context mContext;

    private int mTileTextSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDragView = new DraggableGridView(getActivity(), null);
        mContainer = container;
        mInflater = inflater;
        mContext = getActivity();
        PackageManager pm = mContext.getPackageManager();
        res = mContext.getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (pm != null) {
            try {
                mSystemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                mSystemUiResources = null;
            }
        }
        int colCount = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QUICK_TILES_PER_ROW, 3);
        updateTileTextSize(colCount);
        return mDragView;
    }

    void cleanTilesContent(ArrayList<String> tiles){
        Map<String, ?> allContacts = prefs.getAll();
        for (String tileID : allContacts.keySet()){
            if (!tiles.contains(tileID)){
                prefs.edit().remove(tileID).apply();
            }
        }
    }

    void genTiles() {
        mDragView.removeAllViews();
        String allTilesString = QuickSettingsUtil.getCurrentTiles(mContext);
        if (!allTilesString.equals("")){
            ArrayList<String> tiles = QuickSettingsUtil.getTileListFromString(allTilesString);
            cleanTilesContent(tiles);
            for (String tileindex : tiles) {
                StringTokenizer st = new StringTokenizer(tileindex,"+");
                QuickSettingsUtil.TileInfo tile = QuickSettingsUtil.TILES.get(st.nextToken());
                String tileID;
                String tileString = res.getString(tile.getTitleResId());
                if (st.hasMoreTokens()) {
                    tileID = st.nextToken();
                    if (tileindex.startsWith(QuickSettingsUtil.TILE_FAVCONTACT)){
                        String newTileString = prefs.getString(tileindex, null);
                        if (newTileString != null) tileString = newTileString;
                        else tileString += " "+tileID;
                    }
                }
                if (tile != null) addTile(tileString, tile.getIcon(), 0, false);
            }
        }
        addTile(res.getString(R.string.profiles_add), null, R.drawable.ic_menu_add, false);
    }

    /**
     * Adds a tile to the dragview
     * @param titleId - string id for tile text in systemui
     * @param iconSysId - resource id for icon in systemui
     * @param iconRegId - resource id for icon in local package
     * @param newTile - whether a new tile is being added by user
     */
    void addTile(String titleId, String iconSysId, int iconRegId, boolean newTile) {
        View v = (View) mInflater.inflate(R.layout.qs_tile, null, false);
        TextView name = (TextView) v.findViewById(R.id.qs_text);
        name.setText(titleId);
        name.setTextSize(1, mTileTextSize);
        if (mSystemUiResources != null && iconSysId != null) {
            int resId = mSystemUiResources.getIdentifier(iconSysId, null, null);
            if (resId > 0) {
                try {
                    Drawable d = mSystemUiResources.getDrawable(resId);
                    name.setCompoundDrawablesRelativeWithIntrinsicBounds(null, d, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, iconRegId, 0, 0);
        }
        mDragView.addView(v, newTile ? mDragView.getChildCount() - 1 : mDragView.getChildCount());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        genTiles();
        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();
        mDragView.setOnRearrangeListener(new OnRearrangeListener() {
            public void onRearrange(int oldIndex, int newIndex) {
                curr = QuickSettingsUtil.getTileListFromString(QuickSettingsUtil.getCurrentTiles(getActivity()));
                String oldTile = curr.get(oldIndex);
                curr.remove(oldIndex);
                curr.add(newIndex, oldTile);
                QuickSettingsUtil.saveCurrentTiles(getActivity(), QuickSettingsUtil.getTileStringFromList(curr));
            }
            @Override
            public void onDelete(int index) {
                curr = QuickSettingsUtil.getTileListFromString(QuickSettingsUtil.getCurrentTiles(getActivity()));
                curr.remove(index);
                QuickSettingsUtil.saveCurrentTiles(getActivity(), QuickSettingsUtil.getTileStringFromList(curr));
            }
        });
        mDragView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (arg2 != mDragView.getChildCount() - 1) return;
                curr = QuickSettingsUtil.getTileListFromString(QuickSettingsUtil.getCurrentTiles(getActivity()));
                mTileAdapter = null;
                mTileAdapter = new TileAdapter(getActivity(), 0);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.tile_choose_title)
                .setAdapter(mTileAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int position) {
                        TileInfo info = QuickSettingsUtil.TILES.get(mTileAdapter.getTileId(position));
                        int tileOccurencesCount=1;
                        for (int i=0; i<curr.size();i++)
                            if (curr.get(i).startsWith(info.getId())) tileOccurencesCount++;
                        info.setOccurences(tileOccurencesCount);
                        if (!info.isSingleton()) curr.add(info.getId()+"+"+tileOccurencesCount);
                        else curr.add(info.getId());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                QuickSettingsUtil.saveCurrentTiles(getActivity(), QuickSettingsUtil.getTileStringFromList(curr));
                            }
                        }).start();
                        String tileNameDisplay = res.getString(info.getTitleResId());
                        if (!info.isSingleton()) tileNameDisplay += " "+info.getOccurences();
                        addTile(tileNameDisplay, info.getIcon(), 0, true);
                    }
                });
                builder.create().show();
            }
        });
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isPhone(getActivity())) {
            mContainer.setPadding(20, 0, 0, 0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setAlphabeticShortcut('r')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetTiles();
                return true;
            default:
                return false;
        }
    }

    private void resetTiles() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(R.string.tiles_reset_title);
        alert.setMessage(R.string.tiles_reset_message);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                QuickSettingsUtil.resetTiles(getActivity());
                genTiles();
            }
        });
        alert.setNegativeButton(R.string.cancel, null);
        alert.create().show();
    }

    private void updateTileTextSize(int column) {
        // adjust the tile text size based on column count
        switch (column) {
            case 5:
                mTileTextSize = 7;
                break;
            case 4:
                mTileTextSize = 10;
                break;
            case 3:
            default:
                mTileTextSize = 12;
                break;
        }
    }

    @SuppressWarnings("rawtypes")
    static class TileAdapter extends ArrayAdapter {

        ArrayList<String> mTileKeys;
        Resources mResources;

        public TileAdapter(Context context, int textViewResourceId) {
            super(context, android.R.layout.simple_list_item_1);
            getItemsToDisplay();
            mResources = context.getResources();
        }

        private void getItemsToDisplay() {
            mTileKeys = new ArrayList(QuickSettingsUtil.TILES.keySet());
            for (int i=0; i<curr.size(); i++)
                if (mTileKeys.contains(curr.get(i)) && QuickSettingsUtil.TILES.get(curr.get(i)).isSingleton()) mTileKeys.remove(curr.get(i));
        }

        @Override
        public int getCount() {
            return mTileKeys.size();
        }

        @Override
        public Object getItem(int position) {
            int resid = QuickSettingsUtil.TILES.get(mTileKeys.get(position))
                    .getTitleResId();
            return mResources.getString(resid);
        }

        public String getTileId(int position) {
            return QuickSettingsUtil.TILES.get(mTileKeys.get(position))
                    .getId();
        }

    }

    public interface OnRearrangeListener {
        public abstract void onRearrange(int oldIndex, int newIndex);
        public abstract void onDelete(int index);
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QUICK_SETTINGS_TILE_CONTENT), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            String tileContent = Settings.System.getString(
                    mContext.getContentResolver(), Settings.System.QUICK_SETTINGS_TILE_CONTENT);
            StringTokenizer st = new StringTokenizer(tileContent,"|");
            String tile = st.nextToken();
            String name = st.nextToken();
            Log.e("QuickSettingsTiles","putting into prefs: "+tile+" , "+name);
            prefs.edit().putString(tile, name).apply();
            genTiles();
        }
    }
}
