/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui.widgets.settings;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.browser.SettingsStore;
import org.mozilla.vrbrowser.browser.engine.SessionStore;
import org.mozilla.vrbrowser.databinding.OptionsLanguageVoiceBinding;
import org.mozilla.vrbrowser.ui.views.settings.RadioGroupSetting;
import org.mozilla.vrbrowser.ui.widgets.WidgetManagerDelegate;
import org.mozilla.vrbrowser.ui.widgets.WidgetPlacement;
import org.mozilla.vrbrowser.utils.LocaleUtils;

class VoiceSearchLanguageOptionsView extends SettingsView {

    private OptionsLanguageVoiceBinding mBinding;

    public VoiceSearchLanguageOptionsView(Context aContext, WidgetManagerDelegate aWidgetManager) {
        super(aContext, aWidgetManager);
        initialize(aContext);
    }

    private void initialize(Context aContext) {
        updateUI();
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.options_language_voice, this, true);

        mScrollbar = mBinding.scrollbar;

        // Header
        mBinding.headerLayout.setBackClickListener(view -> {
            mDelegate.showView(SettingViewType.LANGUAGE);
        });
        mBinding.headerLayout.setHelpClickListener(view -> {
            SessionStore.get().getActiveSession().loadUri(getResources().getString(R.string.sumo_language_voice_url));
            mDelegate.exitWholeSettings();
        });

        // Footer
        mBinding.footerLayout.setFooterButtonClickListener(mResetListener);

        mBinding.languageRadio.setOptions(LocaleUtils.getSupportedLocalizedLanguages(getContext()));

        String locale = LocaleUtils.getVoiceSearchLocale(getContext());
        mBinding.languageRadio.setOnCheckedChangeListener(mLanguageListener);
        setLanguage(LocaleUtils.getIndexForSupportedLocale(locale), false);
    }

    @Override
    protected boolean reset() {
        String systemLocale = LocaleUtils.getClosestSupportedLocale(getContext(), LocaleUtils.getDeviceLanguage().getId());
        String currentLocale = LocaleUtils.getVoiceSearchLanguage(getContext()).getId();
        if (currentLocale.equalsIgnoreCase(systemLocale)) {
            setLanguage(LocaleUtils.getIndexForSupportedLocale(systemLocale), false);

        } else {
            setLanguage(LocaleUtils.getIndexForSupportedLocale(systemLocale), true);
            SettingsStore.getInstance(getContext()).setVoiceSearchLocale(null);
        }

        return false;
    }

    private RadioGroupSetting.OnCheckedChangeListener mLanguageListener = (radioGroup, checkedId, doApply) -> {
        String currentLocale = LocaleUtils.getVoiceSearchLocale(getContext());
        String locale = LocaleUtils.getSupportedLocaleForIndex(mBinding.languageRadio.getCheckedRadioButtonId());

        if (!locale.equalsIgnoreCase(currentLocale)) {
            setLanguage(checkedId, true);
        }
    };

    private OnClickListener mResetListener = (view) -> {
        reset();
    };

    private void setLanguage(int checkedId, boolean doApply) {
        mBinding.languageRadio.setOnCheckedChangeListener(null);
        mBinding.languageRadio.setChecked(checkedId, doApply);
        mBinding.languageRadio.setOnCheckedChangeListener(mLanguageListener);

        if (doApply) {
            String locale = LocaleUtils.getSupportedLocaleForIndex(checkedId);
            LocaleUtils.setVoiceSearchLocale(getContext(), locale);
        }
    }

    @Override
    public Point getDimensions() {
        return new Point( WidgetPlacement.dpDimension(getContext(), R.dimen.settings_dialog_width),
                WidgetPlacement.dpDimension(getContext(), R.dimen.settings_dialog_height));
    }

    @Override
    protected SettingViewType getType() {
        return SettingViewType.LANGUAGE_VOICE;
    }
    
}
