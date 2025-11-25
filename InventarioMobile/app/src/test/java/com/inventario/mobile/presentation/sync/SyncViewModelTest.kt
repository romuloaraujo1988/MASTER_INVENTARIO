package com.inventario.mobile.presentation.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.domain.usecase.SincronizarDadosUseCase
import com.inventario.mobile.sync.SyncManager
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
    private lateinit var sincronizarDadosUseCase: SincronizarDadosUseCase
    
    @Mock
    private lateinit var sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase
    
    @Mock
    private lateinit var syncManager: SyncManager

    private lateinit var viewModel: SyncViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = SyncViewModel(
            sincronizarDadosUseCase = sincronizarDadosUseCase,
            sincronizarColetasPendentesUseCase = sincronizarColetasPendentesUseCase,
            syncManager = syncManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should initialize correctly`() {
        assertNotNull(viewModel)
        assertNotNull(viewModel.state)
    }

    @Test
    fun `initial state should be Idle`() {
        val state = viewModel.state.value
        assertTrue(state is SyncState.Idle)
    }
}
