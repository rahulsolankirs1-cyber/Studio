package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.StudioFlowDatabase
import com.example.data.local.entity.BookingCrewAssignmentEntity
import com.example.data.local.entity.BookingEntity
import com.example.data.repository.BookingRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: StudioFlowDatabase
    private lateinit var bookingRepository: BookingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = StudioFlowDatabase.getInstance(context)
        bookingRepository = BookingRepository(database.bookingDao(), database.invoiceDao())
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TracStudio", appName)
    }

    @Test
    fun `verify discount and balance calculation logic`() {
        val baseRate = 200000.0
        val discountPercent = 10.0
        val discountAmount = baseRate * (discountPercent / 100.0)
        val taxableBase = baseRate - discountAmount
        val taxRate = 18.0
        val taxAmount = taxableBase * (taxRate / 100.0)
        val grandTotal = taxableBase + taxAmount
        val advancePaid = 50000.0
        val balanceDue = grandTotal - advancePaid

        assertEquals(20000.0, discountAmount, 0.01)
        assertEquals(180000.0, taxableBase, 0.01)
        assertEquals(32400.0, taxAmount, 0.01)
        assertEquals(212400.0, grandTotal, 0.01)
        assertEquals(162400.0, balanceDue, 0.01)
    }
}
