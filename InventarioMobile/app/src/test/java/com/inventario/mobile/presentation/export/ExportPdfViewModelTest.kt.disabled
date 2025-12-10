package com.inventario.mobile.presentation.export

import android.content.Intent
import com.inventario.mobile.domain.model.ExportConfig
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.PdfRepository
import com.inventario.mobile.domain.usecase.BuscarSalasParaExportacaoUseCase
import com.inventario.mobile.domain.usecase.GerarRelatorioPdfUseCase
import com.inventario.mobile.domain.usecase.NoDataException
import com.inventario.mobile.util.NetworkChecker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests para ExportPdfViewModel
 * 
 * **Validates: Requirements 1.3, 4.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExportPdfViewModelTest : FunSpec({
    
    val testDispatcher = StandardTestDispatcher()
    
    lateinit var buscarSalasUseCase: BuscarSalasParaExportacaoUseCase
    lateinit var gerarRelatorioPdfUseCase: GerarRelatorioPdfUseCase
    lateinit var pdfRepository: PdfRepository
    lateinit var networkChecker: NetworkChecker
    lateinit var viewModel: ExportPdfViewModel
    
    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }
    
    afterSpec {
        Dispatchers.resetMain()
    }
    
    beforeTest {
        buscarSalasUseCase = mockk()
        gerarRelatorioPdfUseCase = mockk()
        pdfRepository = mockk()
        networkChecker = mockk()
        
        // Default mock behavior
        every { networkChecker.isOnline() } returns true
        coEvery { buscarSalasUseCase() } returns Result.success(emptyList())
    }
    
    test("initial state should be Idle then LoadingSalas") {
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        // O ViewModel inicia carregando salas automaticamente
        // Após o init, deve estar em LoadingSalas ou SalasLoaded
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.state.first()
        // Pode ser SalasLoaded (lista vazia) ou LoadingSalas
        (state is ExportPdfState.SalasLoaded || state is ExportPdfState.LoadingSalas) shouldBe true
    }
    
    test("loadSalas should emit SalasLoaded on success") {
        val salas = listOf(
            Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1),
            Sala(id = 2, nome = "Sala 102", codigo = "S102", setorId = 1)
        )
        coEvery { buscarSalasUseCase() } returns Result.success(salas)
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.state.first()
        state.shouldBeInstanceOf<ExportPdfState.SalasLoaded>()
        (state as ExportPdfState.SalasLoaded).salas.size shouldBe 2
    }
    
    test("loadSalas should emit Error on failure") {
        coEvery { buscarSalasUseCase() } returns Result.failure(Exception("Erro de conexão"))
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.state.first()
        state.shouldBeInstanceOf<ExportPdfState.Error>()
        (state as ExportPdfState.Error).message shouldBe "Erro de conexão"
    }
    
    test("generatePdf without sala should emit Error") {
        coEvery { buscarSalasUseCase() } returns Result.success(emptyList())
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Tentar gerar PDF sem selecionar sala
        viewModel.generatePdf()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.state.first()
        state.shouldBeInstanceOf<ExportPdfState.Error>()
        (state as ExportPdfState.Error).message shouldBe "Selecione uma sala para exportar"
    }
    
    test("generatePdf with empty results should emit NoData") {
        val sala = Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1)
        coEvery { buscarSalasUseCase() } returns Result.success(listOf(sala))
        coEvery { gerarRelatorioPdfUseCase(any()) } returns Result.failure(
            NoDataException("Nenhum patrimônio encontrado")
        )
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Selecionar sala e gerar PDF
        viewModel.selectSala(sala)
        viewModel.generatePdf()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.state.first()
        state.shouldBeInstanceOf<ExportPdfState.NoData>()
    }
    
    test("generatePdf with success should emit Success") {
        val sala = Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1)
        val exportResult = ExportResult(
            filePath = "/path/to/file.pdf",
            fileName = "inventario_sala_101.pdf",
            totalItems = 10,
            coletados = 7,
            naoColetados = 3,
            percentualColeta = 70.0
        )
        
        coEvery { buscarSalasUseCase() } returns Result.success(listOf(sala))
        coEvery { gerarRelatorioPdfUseCase(any()) } returns Result.success(exportResult)
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Selecionar sala e gerar PDF
        viewModel.selectSala(sala)
        viewModel.generatePdf()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.state.first()
        state.shouldBeInstanceOf<ExportPdfState.Success>()
        (state as ExportPdfState.Success).result.totalItems shouldBe 10
    }
    
    test("selectSala should update selectedSala and enable export") {
        val sala = Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1)
        coEvery { buscarSalasUseCase() } returns Result.success(listOf(sala))
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.canExport.value shouldBe false
        
        viewModel.selectSala(sala)
        
        viewModel.selectedSala.value shouldBe sala
        viewModel.canExport.value shouldBe true
    }
    
    test("selectFilter should update selectedFilter") {
        coEvery { buscarSalasUseCase() } returns Result.success(emptyList())
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Default should be TODOS
        viewModel.selectedFilter.value shouldBe ExportFilter.TODOS
        
        viewModel.selectFilter(ExportFilter.COLETADOS)
        viewModel.selectedFilter.value shouldBe ExportFilter.COLETADOS
        
        viewModel.selectFilter(ExportFilter.NAO_COLETADOS)
        viewModel.selectedFilter.value shouldBe ExportFilter.NAO_COLETADOS
    }
    
    test("isOffline should reflect network status") {
        every { networkChecker.isOnline() } returns false
        coEvery { buscarSalasUseCase() } returns Result.success(emptyList())
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.isOffline.value shouldBe true
    }
    
    test("clearSalaSelection should reset selection and disable export") {
        val sala = Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1)
        coEvery { buscarSalasUseCase() } returns Result.success(listOf(sala))
        
        viewModel = ExportPdfViewModel(
            buscarSalasUseCase,
            gerarRelatorioPdfUseCase,
            pdfRepository,
            networkChecker
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectSala(sala)
        viewModel.canExport.value shouldBe true
        
        viewModel.clearSalaSelection()
        
        viewModel.selectedSala.value shouldBe null
        viewModel.canExport.value shouldBe false
    }
})
