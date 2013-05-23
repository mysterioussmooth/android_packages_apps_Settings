/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.liquid;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

public class LockscreenWidgets extends SettingsPreferenceFragment {

    private static final String TAG = "LockscreenWidgets";

    private static final String KEY_WIDGET_OPTIONS = "lockscreen_widgets_group";
    private static final String KEY_LOCKSCREEN_ALL_WIDGETS = "lockscreen_all_widgets";
    private static final String KEY_LOCKSCREEN_CAMERA_WIDGET = "lockscreen_camera_widget";
    private static final String KEY_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS = "lockscreen_hide_initial_page_hints";
    private static final String KEY_LOCKSCREEN_LONGPRESS_CHALLENGE = "lockscreen_longpress_challenge";
    private static final String KEY_LOCKSCREEN_MAXIMIZE_WIDGETS = "lockscreen_maximize_widgets";
    private static final String KEY_LOCKSCREEN_UNLIMITED_WIDGETS = "lockscreen_unlimited_widgets";
    private static final String KEY_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";

    private CheckBoxPreference mAllWidgets;
    private CheckBoxPreference mCameraWidget;
    private CheckBoxPreference mHideInitialPageHints;
    private CheckBoxPreference mLongpressChallenge;
    private CheckBoxPreference mMaximizeWidgets;
    private CheckBoxPreference mUnlimitedWidgets;
    private CheckBoxPreference mUsabilityHints;
    private CheckBoxPreference mUseCarousel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_widgets);

        mAllWidgets = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_ALL_WIDGETS);
        mAllWidgets.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_ALL_WIDGETS, 0) == 1);

        mCameraWidget = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_CAMERA_WIDGET);
        mCameraWidget.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_CAMERA_WIDGET, 0) == 1);

        mHideInitialPageHints = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS);
        mHideInitialPageHints.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, 0) == 1);

        mLongpressChallenge = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_LONGPRESS_CHALLENGE);
        mMaximizeWidgets = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_MAXIMIZE_WIDGETS);
        if (!Utils.isPhone(getActivity())) {
            PreferenceCategory widgetCategory = (PreferenceCategory) findPreference(KEY_WIDGET_OPTIONS);
            if (mLongpressChallenge != null)
                widgetCategory.removePreference(mLongpressChallenge);
            mLongpressChallenge = null;
            if (mMaximizeWidgets != null)
                widgetCategory.removePreference(mMaximizeWidgets);
            mMaximizeWidgets = null;
        } else {
            mLongpressChallenge.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE, 0) == 1);
            mMaximizeWidgets.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, 0) == 1);
        }

        mUnlimitedWidgets = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_UNLIMITED_WIDGETS);
        mUnlimitedWidgets.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, 0) == 1);

        mUseCarousel = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_USE_CAROUSEL);
        mUseCarousel.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, 0) == 1);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAllWidgets) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALL_WIDGETS, mAllWidgets.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mCameraWidget) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_CAMERA_WIDGET, mCameraWidget.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mHideInitialPageHints) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, mHideInitialPageHints.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLongpressChallenge) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE, mLongpressChallenge.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mMaximizeWidgets) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, mMaximizeWidgets.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mUnlimitedWidgets) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, mUnlimitedWidgets.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mUseCarousel) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, mUseCarousel.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
