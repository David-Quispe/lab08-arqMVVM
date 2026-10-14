package com.example.inventory

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.inventory.data.InventoryDatabase
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {
    private lateinit var database: InventoryDatabase
    private lateinit var dao: ItemDao

    @Before
    fun crearBaseTemporal() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            InventoryDatabase::class.java
        ).build()
        dao = database.itemDao()
    }

    @After
    fun cerrarBase() {
        database.close()
    }

    @Test
    fun insertaYLeeProductos() = runBlocking {
        val lapiz = Item(1, "Lápiz", 2.50, 10)
        val cuaderno = Item(2, "Cuaderno", 8.00, 5)
        dao.insert(lapiz)
        dao.insert(cuaderno)

        assertEquals(listOf(cuaderno, lapiz), dao.getAllItems().first())
        assertEquals(lapiz, dao.getItem(1).first())
    }

    @Test
    fun actualizaYEliminaProducto() = runBlocking {
        val original = Item(1, "Lápiz", 2.50, 10)
        dao.insert(original)
        val vendido = original.copy(quantity = 9)
        dao.update(vendido)
        assertEquals(vendido, dao.getItem(1).first())

        dao.delete(vendido)
        assertTrue(dao.getAllItems().first().isEmpty())
    }
}
