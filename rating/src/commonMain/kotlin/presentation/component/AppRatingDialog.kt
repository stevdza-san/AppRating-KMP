package com.stevdza_san.demo.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.stevdza_san.demo.AppRatingManager
import com.stevdza_san.demo.domain.Interval
import com.stevdza_san.demo.util.Platform
import com.stevdza_san.demo.util.getPlatform

/**
 * Displays an app rating dialog to the user, prompting them to leave a review on the app store.
 * This dialog can be configured to show at specified intervals with customizable text and styling.
 *
 * The placement of this dialog is recommended at the top level of the UI hierarchy for the following reasons:
 * - Global Visibility: Ensures the dialog is accessible and can be displayed
 *   regardless of the current screen or navigation state.
 * - Centralized Logic: Prevents duplication of dialog-related code across multiple screens
 *   and allows centralized control over when and how the dialog is shown.
 * - Non-Intrusive: The dialog state is managed independently of the navigation graph,
 *   making it less likely to interfere with other UI components or navigation transitions.
 *
 * It is recommended to place this dialog alongside other global components, such as `NavHost()`,
 * to maintain a clean and maintainable structure. Example usage:
 *
 * ```
 * @Composable
 * fun SetupNavGraph() {
 *     val navController = rememberNavController()
 *
 *     // Top-level AppRatingDialog
 *    AppRatingDialog(
 *        playStoreLink = "https://play.google.com/store/apps/details?id=PACKAGE_NAME",
 *        appStoreLink = "https://apps.apple.com/app/YOUR_APP/ID",
 *        interval = Interval.Monthly
 *    )
 *
 *     NavHost(
 *         navController = navController,
 *         startDestination = Screen.Home
 *     ) {
 *         composable<Screen.Home> { HomeScreen() }
 *         composable<Screen.Settings> { SettingsScreen() }
 *     }
 * }
 * ```
 *
 * This approach ensures that the app rating dialog is both unobtrusive and easily maintainable
 * in a Compose Multiplatform application.
 *
 * @param modifier A [Modifier] for applying styling or layout constraints to the dialog.
 * @param playStoreLink A full app link on Google Play Store (Android).
 * @param appStoreLink A full app link on App Store (iOS).
 * @param initialDelayInDays The number of days to wait before showing the dialog for the first time. Default is 5 days.
 * @param interval The interval at which the dialog should be displayed after the initial delay. Default is [Interval.Monthly].
 * @param title A composable lambda defining the title of the dialog.".
 * @param content A composable lambda defining the content of the dialog..
 * @param containerColor The background color of the dialog's container.
 * @param tonalElevation The elevation of the dialog to apply tonal shading..
 * @param dismissText The text for the dismiss button.
 * @param dismissButtonColors The colors to use for the dismiss button.
 * @param confirmText The text for the confirm button.
 * @param confirmButtonColors The colors to use for the confirm button.
 * @param onDismiss Callback triggered when the dismiss button is clicked.
 *
 */
@Composable
fun AppRatingDialog(
    modifier: Modifier = Modifier,
    playStoreLink: String,
    appStoreLink: String,
    initialDelayInDays: Int = 5,
    interval: Interval = Interval.Monthly,
    title: @Composable (() -> Unit)? = { Text(text = "Enjoying our App?") },
    content: @Composable (() -> Unit)? = { Text(text = "If you are satisfied, please take a moment to Rate us on ${if (getPlatform() == Platform.ANDROID) "Play Store" else "App Store"}.") },
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 1.dp,
    dismissText: String = "Cancel",
    dismissButtonColors: ButtonColors = ButtonDefaults.textButtonColors(),
    confirmText: String = "Leave a review",
    confirmButtonColors: ButtonColors = ButtonDefaults.textButtonColors(),
    onDismiss: (() -> Unit)? = null
) {
    val appRatingManager = remember {
        AppRatingManager(
            playStoreLink,
            appStoreLink
        )
    }
    val showDialog by appRatingManager.showDialog.collectAsState()

    LaunchedEffect(Unit) {
        appRatingManager.initialize(
            initialDelayInDays = initialDelayInDays,
            interval = interval
        )
    }

    if (showDialog) {
        AlertDialog(
            modifier = modifier.zIndex(99f),
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            title = title,
            text = content,
            onDismissRequest = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss?.invoke()
                        appRatingManager.hideDialog()
                        appRatingManager.resetTimestamp()
                    },
                    colors = dismissButtonColors
                ) {
                    Text(
                        text = dismissText,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        appRatingManager.saveAppReviewCompleted()
                        appRatingManager.resetTimestamp()
                        appRatingManager.openBrowser()
                    },
                    colors = confirmButtonColors
                ) {
                    Text(text = confirmText)
                }
            }
        )
    }
}