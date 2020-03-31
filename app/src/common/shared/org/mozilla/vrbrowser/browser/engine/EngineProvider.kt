/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.browser.engine

import android.content.Context
import org.mozilla.geckoview.*
import org.mozilla.vrbrowser.BuildConfig
import org.mozilla.vrbrowser.browser.SettingsStore
import org.mozilla.vrbrowser.browser.content.TrackingProtectionPolicy
import org.mozilla.vrbrowser.browser.content.TrackingProtectionStore
import org.mozilla.vrbrowser.crashreporting.CrashReporterService

object EngineProvider {

    private val WEB_EXTENSIONS = arrayOf("webcompat_vimeo", "webcompat_youtube")

    private var runtime: GeckoRuntime? = null
    private var executor: GeckoWebExecutor? = null
    private var client: GeckoViewFetchClient? = null

    @Synchronized
    fun getOrCreateRuntime(context: Context): GeckoRuntime {
        if (runtime == null) {
            val builder = GeckoRuntimeSettings.Builder()

            val policy : TrackingProtectionPolicy = TrackingProtectionStore.getTrackingProtectionPolicy(context);
            builder.crashHandler(CrashReporterService::class.java)
            builder.contentBlocking(ContentBlocking.Settings.Builder()
                    .antiTracking(policy.antiTrackingPolicy)
                    .enhancedTrackingProtectionLevel(SettingsStore.getInstance(context).trackingProtectionLevel)
                    .build())
            builder.consoleOutput(SettingsStore.getInstance(context).isConsoleLogsEnabled)
            builder.displayDensityOverride(SettingsStore.getInstance(context).displayDensity)
            builder.remoteDebuggingEnabled(SettingsStore.getInstance(context).isRemoteDebuggingEnabled)
            builder.displayDpiOverride(SettingsStore.getInstance(context).displayDpi)
            builder.screenSizeOverride(SettingsStore.getInstance(context).maxWindowWidth,
                    SettingsStore.getInstance(context).maxWindowHeight)
            builder.useMultiprocess(true)

            if (SettingsStore.getInstance(context).transparentBorderWidth > 0) {
                builder.useMaxScreenDepth(true)
            }

            if (BuildConfig.DEBUG) {
                builder.arguments(arrayOf("-purgecaches"))
                builder.debugLogging(true)
                builder.aboutConfigEnabled(true)
            } else {
                builder.debugLogging(SettingsStore.getInstance(context).isDebugLoggingEnabled)
            }

            runtime = GeckoRuntime.create(context, builder.build())
            for (extension in WEB_EXTENSIONS) {
                val path = "resource://android/assets/web_extensions/$extension/"
                runtime!!.registerWebExtension(WebExtension(path, runtime!!.webExtensionController))
            }
        }

        return runtime!!
    }

    fun createGeckoWebExecutor(context: Context): GeckoWebExecutor {
        return GeckoWebExecutor(getOrCreateRuntime(context))
    }

    fun getDefaultGeckoWebExecutor(context: Context): GeckoWebExecutor {
        if (executor == null) {
            executor = createGeckoWebExecutor(context)
        }

        return executor!!
    }

    fun createClient(context: Context): GeckoViewFetchClient {
        return GeckoViewFetchClient(context)
    }

    fun getDefaultClient(context: Context): GeckoViewFetchClient {
        if (client == null) {
            client = createClient(context)
        }

        return client!!
    }

}
