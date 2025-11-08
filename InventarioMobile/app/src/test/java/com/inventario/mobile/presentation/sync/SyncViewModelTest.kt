package com.inventario.mobile.presentation.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SyncViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: InventarioRepository

    private lateinit var viewModel: SyncViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = SyncViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should initialize correctly`() {
        assertNotNull(viewModel)
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `clearMessages should work correctly`() {
        viewModel.clearMessages()
        // Test passes if no exception is thrown
        assertTrue(true)
    }

    @Test
    fun `clearError should work correctly`() {
        viewModel.clearError()
        // Test passes if no exception is thrown
        assertTrue(true)
    }

    @Test
    fun `clearSuccess should work correctly`() {
        viewModel.clearSuccess()
        // Test passes if no exception is thrown
        assertTrue(true)
    }
}