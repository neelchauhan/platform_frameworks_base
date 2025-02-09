/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.systemui.communal.smartspace

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.android.systemui.animation.ActivityTransitionAnimator
import com.android.systemui.communal.domain.interactor.CommunalSceneInteractor
import com.android.systemui.communal.util.InteractionHandlerDelegate
import com.android.systemui.communal.widgets.SmartspaceAppWidgetHostView
import com.android.systemui.log.LogBuffer
import com.android.systemui.log.core.Logger
import com.android.systemui.log.dagger.CommunalLog
import com.android.systemui.plugins.ActivityStarter
import javax.inject.Inject

/** Handles interactions on smartspace elements on the hub. */
class SmartspaceInteractionHandler
@Inject
constructor(
    private val activityStarter: ActivityStarter,
    communalSceneInteractor: CommunalSceneInteractor,
    @CommunalLog val logBuffer: LogBuffer,
) : RemoteViews.InteractionHandler {

    private companion object {
        const val TAG = "SmartspaceInteractionHandler"
    }

    private val delegate =
        InteractionHandlerDelegate(
            communalSceneInteractor,
            findViewToAnimate = { view -> view is SmartspaceAppWidgetHostView },
            intentStarter =
                object : InteractionHandlerDelegate.IntentStarter {
                    override fun startActivity(
                        intent: PendingIntent,
                        fillInIntent: Intent,
                        activityOptions: ActivityOptions,
                        controller: ActivityTransitionAnimator.Controller?
                    ): Boolean {
                        return startIntent(intent, fillInIntent, activityOptions, controller)
                    }
                },
            logger = Logger(logBuffer, TAG),
        )

    override fun onInteraction(
        view: View,
        pendingIntent: PendingIntent,
        response: RemoteViews.RemoteResponse
    ): Boolean = delegate.onInteraction(view, pendingIntent, response)

    private fun startIntent(
        pendingIntent: PendingIntent,
        fillInIntent: Intent,
        extraOptions: ActivityOptions,
        animationController: ActivityTransitionAnimator.Controller?
    ): Boolean {
        activityStarter.startPendingIntentWithoutDismissing(
            pendingIntent,
            /* dismissShade = */ false,
            /* intentSentUiThreadCallback = */ null,
            animationController,
            fillInIntent,
            extraOptions.toBundle()
        )
        return true
    }
}
