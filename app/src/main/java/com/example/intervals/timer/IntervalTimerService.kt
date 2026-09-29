package com.example.intervals.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.intervals.IntervalsApplication
import com.example.intervals.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntervalTimerService : Service()
{
    private val scope = CoroutineScope(SupervisorJob())
    private var runJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate()
    {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        when (intent?.action)
        {
            ACTION_STOP ->
            {
                stopRun()
            }
            else ->
            {
                val presetId = intent?.getLongExtra(EXTRA_PRESET_ID, -1L) ?: -1L
                if (presetId != -1L) startRun(presetId)
            }
        }
        return START_NOT_STICKY
    }

    private fun startRun(presetId: Long)
    {
        runJob?.cancel()
        runJob = scope.launch {
            val repository = (application as IntervalsApplication).repository
            val preset = repository.getById(presetId) ?: run { stopSelf(); return@launch }
            repository.markUsed(presetId)
            val timeline = TimelineBuilder.build(preset)
            val totalSeconds = timeline.sumOf { it.durationSeconds }
            var elapsedBefore = 0

            startForeground(NOTIFICATION_ID, buildNotification(preset.name, Phase.WARMUP, 0))

            for (step in timeline)
            {
                for (remaining in step.durationSeconds downTo 1)
                {
                    val state = TimerState(
                        isActive = true,
                        presetId = presetId,
                        presetName = preset.name,
                        phase = step.phase,
                        secondsRemainingInPhase = remaining,
                        setIndex = step.setIndex,
                        totalSets = step.totalSets,
                        elapsedSeconds = elapsedBefore + (step.durationSeconds - remaining),
                        totalSeconds = totalSeconds
                    )
                    TimerStateHolder.update(state)
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(preset.name, step.phase, remaining))
                    delay(1000)
                }
                elapsedBefore += step.durationSeconds
            }

            TimerStateHolder.update(
                TimerState(
                    isActive = false,
                    presetId = presetId,
                    presetName = preset.name,
                    phase = Phase.DONE,
                    elapsedSeconds = totalSeconds,
                    totalSeconds = totalSeconds
                )
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopRun()
    {
        runJob?.cancel()
        TimerStateHolder.reset()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(presetName: String, phase: Phase, secondsRemaining: Int): Notification
    {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, IntervalTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val timeText = String.format("%d:%02d", minutes, seconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(presetName)
            .setContentText("${phase.name} · $timeText")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun createNotificationChannel()
    {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Interval timer",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        runJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object
    {
        private const val CHANNEL_ID = "interval_timer"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_PRESET_ID = "preset_id"
        private const val ACTION_STOP = "com.example.intervals.action.STOP"

        fun start(context: Context, presetId: Long)
        {
            val intent = Intent(context, IntervalTimerService::class.java)
                .putExtra(EXTRA_PRESET_ID, presetId)
            context.startForegroundService(intent)
        }

        fun stop(context: Context)
        {
            val intent = Intent(context, IntervalTimerService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }
}
