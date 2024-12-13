package com.stevdza_san.demo

import com.russhwolf.settings.Settings
import com.stevdza_san.demo.domain.InitialPeriodState
import com.stevdza_san.demo.domain.Interval
import com.stevdza_san.demo.util.Platform
import com.stevdza_san.demo.util.getPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import openWebBrowser

internal class AppRatingManager(
    private val playStoreLink: String,
    private val appStoreLink: String,
) {
    private val timestampKey = "_appRatingManagerTimestamp"
    private val initialPeriodStateKey = "_appRatingManagerInitialPeriodState"
    private val appReviewTriggeredKey = "_appRatingManagerAppReviewCompleted"

    private val settings = Settings()
    private val _mTimestamp = MutableStateFlow(0L)
    private val _mInitialDelayInDays = MutableStateFlow(5)
    private val _mInitialPeriodState = MutableStateFlow(InitialPeriodState.Idle)
    private val _mInterval = MutableStateFlow(Interval.Monthly)
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    fun initialize(
        initialDelayInDays: Int,
        interval: Interval
    ) {
        if (initialDelayInDays < 5) throw IllegalArgumentException("Initial interval period cannot be less than 5 days.")
        readTimestamp()
        _mInitialDelayInDays.value = initialDelayInDays
        _mInterval.value = interval
        checkForInitialPeriod()
        shouldShowReviewDialog()
    }

    private fun checkForInitialPeriod() {
        _mInitialPeriodState.value = InitialPeriodState.valueOf(
            settings.getString(
                key = initialPeriodStateKey,
                defaultValue = InitialPeriodState.Idle.name
            )
        )
        when (_mInitialPeriodState.value) {
            InitialPeriodState.Idle -> {
                saveTimestamp()
                readTimestamp()
                settings.putString(
                    key = initialPeriodStateKey,
                    value = InitialPeriodState.Waiting.name
                )
                _mInitialPeriodState.value = InitialPeriodState.Waiting
            }

            else -> {}
        }
    }

    private fun shouldShowReviewDialog() {
        if (_mInitialPeriodState.value == InitialPeriodState.Waiting) {
            if (initialPeriodPassed()) {
                _showDialog.value = true
            }
        } else if (_mInitialPeriodState.value == InitialPeriodState.Completed) {
            if (!readAppReviewCompleted()) {
                println("AppRating-KMP: Not completed.")
                val lastShownTime = Instant.fromEpochMilliseconds(_mTimestamp.value)
                val timeDifference = Clock.System.now() - lastShownTime

                when (_mInterval.value) {
                    Interval.Monthly -> {
                        _showDialog.value = timeDifference.inWholeDays >= 30
                    }

                    Interval.Quarterly -> {
                        _showDialog.value = timeDifference.inWholeDays >= 90
                    }

                    Interval.SemiAnnually -> {
                        _showDialog.value = timeDifference.inWholeDays >= 180
                    }

                    Interval.Yearly -> {
                        _showDialog.value = timeDifference.inWholeDays >= 365
                    }
                }
            } else {
                println("AppRating-KMP: Completed!.")
            }
        }
    }

    private fun initialPeriodPassed(): Boolean {
        val lastSavedTime = Instant.fromEpochMilliseconds(_mTimestamp.value)
        val timeDifference = Clock.System.now() - lastSavedTime
        return timeDifference.inWholeDays >= _mInitialDelayInDays.value
    }

    private fun saveTimestamp() {
        settings.putLong(
            key = timestampKey,
            value = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun readTimestamp() {
        _mTimestamp.value = settings.getLong(
            key = timestampKey,
            defaultValue = 0L
        )
    }

    internal fun saveAppReviewCompleted() {
        settings.putBoolean(
            key = appReviewTriggeredKey,
            value = true
        )
    }

    private fun readAppReviewCompleted(): Boolean {
        return settings.getBoolean(
            key = appReviewTriggeredKey,
            defaultValue = false
        )
    }

    internal fun resetTimestamp() {
        saveTimestamp()
        hideDialog()
        if (_mInitialPeriodState.value != InitialPeriodState.Completed) {
            settings.putString(
                key = initialPeriodStateKey,
                value = InitialPeriodState.Completed.name
            )
        }
    }

    internal fun hideDialog() {
        _showDialog.value = false
    }

    internal fun openBrowser() {
        openWebBrowser(
            url = if (getPlatform() == Platform.ANDROID) playStoreLink
            else appStoreLink
        )
    }
}