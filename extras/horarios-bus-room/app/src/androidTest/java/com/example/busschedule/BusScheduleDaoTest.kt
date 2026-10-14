package com.example.busschedule

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.busschedule.data.BusScheduleDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BusScheduleDaoTest {
    @Test
    fun cargaHorariosYFiltraUnaParada() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "bus_test.db"
        val database = Room.databaseBuilder(context, BusScheduleDatabase::class.java, name)
            .createFromAsset("database/bus_schedule.db")
            .build()

        try {
            val all = database.busScheduleDao().getAllSchedules().first()
            assertEquals(31, all.size)
            assertEquals(all.sortedBy { it.arrivalTimeInMillis }, all)

            val mainStreet = database.busScheduleDao().getSchedulesFor("Main Street").first()
            assertTrue(mainStreet.isNotEmpty())
            assertTrue(mainStreet.all { it.stopName == "Main Street" })
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
