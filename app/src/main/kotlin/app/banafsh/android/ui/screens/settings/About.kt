package app.banafsh.android.ui.screens.settings

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.banafsh.android.BuildConfig
import app.banafsh.android.R
import app.banafsh.android.data.utils.Version
import app.banafsh.android.data.utils.version
import app.banafsh.android.preferences.DataPreferences
import app.banafsh.android.providers.github.GitHub
import app.banafsh.android.providers.github.models.Release
import app.banafsh.android.providers.github.requests.releases
import app.banafsh.android.service.VERSION_NOTIFICATION_CHANNEL_ID
import app.banafsh.android.service.VERSION_NOTIFICATION_ID
import app.banafsh.android.ui.components.themed.CircularProgressIndicator
import app.banafsh.android.ui.components.themed.DefaultDialog
import app.banafsh.android.ui.components.themed.SecondaryTextButton
import app.banafsh.android.ui.screens.Route
import app.banafsh.android.ui.theme.LocalAppearance
import app.banafsh.android.ui.theme.utils.isAtLeastAndroid13
import app.banafsh.android.ui.theme.utils.isAtLeastAndroid8
import app.banafsh.android.ui.theme.utils.isCompositionLaunched
import app.banafsh.android.utils.bold
import app.banafsh.android.utils.center
import app.banafsh.android.utils.hasPermission
import app.banafsh.android.utils.pendingIntent
import app.banafsh.android.utils.semiBold
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val VERSION_NAME = BuildConfig.VERSION_NAME.substringBeforeLast("-")
const val REPO_OWNER = "IamRezaMousavi"
const val REPO_NAME = "Banafsh"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val permission = Manifest.permission.POST_NOTIFICATIONS

@get:RequiresApi(Build.VERSION_CODES.O)
private val Context.notificationChannel
    get() = NotificationChannel(
        /* id = */ VERSION_NOTIFICATION_CHANNEL_ID,
        /* name = */ getString(R.string.version_check),
        /* importance = */ NotificationManager.IMPORTANCE_HIGH
    ).apply {
        enableVibration(true)
        enableLights(true)
    }

class VersionCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        private const val WORK_TAG = "version_check_worker"

        fun upsert(context: Context, period: Duration?) = runCatching {
            val workManager = WorkManager.getInstance(context)

            if (period == null) {
                workManager.cancelAllWorkByTag(WORK_TAG)
                return@runCatching
            }

            val request = PeriodicWorkRequestBuilder<VersionCheckWorker>(period.toJavaDuration())
                .addTag(WORK_TAG)
                .setConstraints(
                    Constraints(
                        requiredNetworkType = NetworkType.CONNECTED,
                        requiresBatteryNotLow = true
                    )
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                /* uniqueWorkName = */ WORK_TAG,
                /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                /* periodicWork = */ request
            )

            Unit
        }.also { it.exceptionOrNull()?.printStackTrace() }
    }

    override suspend fun doWork(): Result = with(applicationContext) {
        if (isAtLeastAndroid13 && !hasPermission(permission)) return Result.retry()

        val notificationManager = getSystemService<NotificationManager>() ?: return Result.success()
        if (isAtLeastAndroid8) notificationManager.createNotificationChannel(notificationChannel)

        val result = withContext(Dispatchers.IO) {
            VERSION_NAME.version
                .getNewerVersion()
                .also { it?.exceptionOrNull()?.printStackTrace() }
        }

        result?.getOrNull()?.let { release ->
            notificationManager.notify(
                /* id = */ VERSION_NOTIFICATION_ID,
                /* notification = */ NotificationCompat
                    .Builder(this, VERSION_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.download)
                    .setContentTitle(getString(R.string.new_version_available))
                    .setContentText(getString(R.string.redirect_github))
                    .setContentIntent(
                        pendingIntent(
                            Intent(
                                /* action = */ Intent.ACTION_VIEW,
                                /* uri = */ Uri.parse(release.frontendUrl.toString())
                            )
                        )
                    )
                    .setAutoCancel(true)
                    .also {
                        it.setStyle(
                            NotificationCompat
                                .BigTextStyle(it)
                                .bigText(getString(R.string.new_version_available))
                        )
                    }
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            )
        }

        return when {
            result == null || result.isFailure -> Result.retry()
            result.isSuccess -> Result.success()
            else -> Result.failure() // Unreachable
        }
    }
}

private suspend fun Version.getNewerVersion(
    repoOwner: String = REPO_OWNER,
    repoName: String = REPO_NAME,
    contentType: String = "application/vnd.android.package-archive"
) = GitHub.releases(
    owner = repoOwner,
    repo = repoName
)?.mapCatching { releases ->
    releases
        .sortedByDescending { it.publishedAt }
        .firstOrNull { release ->
            !release.draft &&
                !release.preRelease &&
                release.tag.version > this &&
                release.assets.any {
                    it.contentType == contentType && it.state == Release.Asset.State.Uploaded
                }
        }
}

@Route
@Composable
fun About() = SettingsCategoryScreen(
    title = stringResource(R.string.about),
    description = stringResource(
        R.string.format_version_credits,
        VERSION_NAME
    )
) {
    val (_, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(
            if (isAtLeastAndroid13) context.applicationContext.hasPermission(permission)
            else true
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )

    SettingsGroup(title = stringResource(R.string.social)) {
        SettingsEntry(
            title = stringResource(R.string.github),
            text = stringResource(R.string.view_source),
            onClick = {
                uriHandler.openUri("https://github.com/$REPO_OWNER/$REPO_NAME")
            }
        )
    }

    SettingsGroup(title = stringResource(R.string.contact)) {
        SettingsEntry(
            title = stringResource(R.string.report_bug),
            text = stringResource(R.string.report_bug_description),
            onClick = {
                uriHandler.openUri(
                    @Suppress("ktlint:standard:max-line-length")
                    "https://github.com/$REPO_OWNER/$REPO_NAME/issues/new?assignees=&labels=bug&template=bug_report.yaml"
                )
            }
        )

        SettingsEntry(
            title = stringResource(R.string.request_feature),
            text = stringResource(R.string.redirect_github),
            onClick = {
                uriHandler.openUri(
                    @Suppress("ktlint:standard:max-line-length")
                    "https://github.com/$REPO_OWNER/$REPO_NAME/issues/new?assignees=&labels=enhancement&template=feature_request.md"
                )
            }
        )
    }

    var newVersionDialogOpened by rememberSaveable { mutableStateOf(false) }

    SettingsGroup(title = stringResource(R.string.version)) {
        SettingsEntry(
            title = stringResource(R.string.check_new_version),
            text = stringResource(R.string.current_version, VERSION_NAME),
            onClick = { newVersionDialogOpened = true }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.version_check),
            selectedValue = DataPreferences.versionCheckPeriod,
            onValueSelect = onSelect@{
                DataPreferences.versionCheckPeriod = it
                if (isAtLeastAndroid13 && it.period != null && !hasPermission)
                    launcher.launch(permission)

                VersionCheckWorker.upsert(context.applicationContext, it.period)
            },
            valueText = { it.displayName() }
        )
    }

    if (newVersionDialogOpened)
        DefaultDialog(
            onDismiss = { newVersionDialogOpened = false }
        ) {
            var newerVersion: Result<Release?>? by remember { mutableStateOf(null) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    newerVersion = VERSION_NAME.version
                        .getNewerVersion()
                        ?.onFailure(Throwable::printStackTrace)
                }
            }

            newerVersion?.getOrNull()?.let {
                BasicText(
                    text = stringResource(R.string.new_version_available),
                    style = typography.xs.semiBold.center
                )

                Spacer(modifier = Modifier.height(12.dp))

                BasicText(
                    text = it.name ?: it.tag,
                    style = typography.m.bold.center
                )

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryTextButton(
                    text = stringResource(R.string.more_information),
                    onClick = { uriHandler.openUri(it.frontendUrl.toString()) }
                )
            } ?: newerVersion?.exceptionOrNull()?.let {
                BasicText(
                    text = stringResource(R.string.error_github),
                    style = typography.xs.semiBold.center,
                    modifier = Modifier.padding(all = 24.dp)
                )
            } ?: if (newerVersion?.isSuccess == true) BasicText(
                text = stringResource(R.string.up_to_date),
                style = typography.xs.semiBold.center
            ) else CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
}
