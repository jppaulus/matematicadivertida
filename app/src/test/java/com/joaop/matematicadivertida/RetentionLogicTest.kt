package com.joaop.matematicadivertida

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class RetentionLogicTest {

    private fun prefs(lastPlayed: String?, streak: Int = 0): SharedPreferences = FakePrefs().apply {
        edit().apply {
            if (lastPlayed != null) putString("streak_last_date", lastPlayed)
            putInt("streak_count", streak)
        }.apply()
    }

    @Test
    fun reminder_isSkippedWhenAlreadyPlayedTodayOrNeverPlayed() {
        assertNull(DailyReminder.messageFor(prefs("2026-09-15", streak = 4), today = "2026-09-15"))
        assertNull(DailyReminder.messageFor(prefs(null), today = "2026-09-15"))
    }

    @Test
    fun reminder_warnsWhenStreakIsAtRisk() {
        val message = DailyReminder.messageFor(prefs("2026-09-14", streak = 5), today = "2026-09-15")
        assertNotNull(message)
        assertTrue(message!!.title.contains("sequência"))
        assertTrue(message.body.contains("5"))
    }

    @Test
    fun reminder_backsOffForUsersAwayForLong() {
        assertNotNull(DailyReminder.messageFor(prefs("2026-09-12"), today = "2026-09-15"))
        assertNull(DailyReminder.messageFor(prefs("2026-09-10"), today = "2026-09-15"))
        assertNotNull(DailyReminder.messageFor(prefs("2026-09-08"), today = "2026-09-15"))
        assertNull(DailyReminder.messageFor(prefs("2026-08-01"), today = "2026-09-15"))
    }

    @Test
    fun reminder_isScheduledForTheNextSixPm() {
        fun at(hour: Int, minute: Int) = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 15, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals(TimeUnit.HOURS.toMillis(8), DailyReminder.millisUntilNextReminder(at(10, 0)))
        assertEquals(TimeUnit.HOURS.toMillis(24), DailyReminder.millisUntilNextReminder(at(18, 0)))
        assertEquals(TimeUnit.MINUTES.toMillis(21 * 60 + 30), DailyReminder.millisUntilNextReminder(at(20, 30)))
    }

    @Test
    fun review_isAskedOnlyAfterRealUseAndAtMostEvery60Days() {
        val now = TimeUnit.DAYS.toMillis(400)
        assertFalse(ReviewPrompter.shouldAsk(ReviewPrompter.MIN_TOTAL_CORRECT - 1, 0L, now))
        assertTrue(ReviewPrompter.shouldAsk(ReviewPrompter.MIN_TOTAL_CORRECT, 0L, now))
        assertFalse(ReviewPrompter.shouldAsk(100, now - TimeUnit.DAYS.toMillis(59), now))
        assertTrue(ReviewPrompter.shouldAsk(100, now - TimeUnit.DAYS.toMillis(60), now))
    }
}

/** SharedPreferences em memória para testes na JVM. */
private class FakePrefs : SharedPreferences {
    private val values = HashMap<String, Any?>()

    override fun getAll(): MutableMap<String, *> = HashMap(values)
    override fun getString(key: String, defValue: String?) = values[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: MutableSet<String>?) =
        values[key] as? MutableSet<String> ?: defValues
    override fun getInt(key: String, defValue: Int) = values[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long) = values[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float) = values[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = values[key] as? Boolean ?: defValue
    override fun contains(key: String) = values.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = HashMap<String, Any?>()
        private var clear = false
        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun remove(key: String) = apply { pending[key] = null }
        override fun clear() = apply { clear = true }
        override fun commit(): Boolean {
            if (clear) values.clear()
            pending.forEach { (key, value) -> if (value == null) values.remove(key) else values[key] = value }
            return true
        }
        override fun apply() {
            commit()
        }
    }
}
