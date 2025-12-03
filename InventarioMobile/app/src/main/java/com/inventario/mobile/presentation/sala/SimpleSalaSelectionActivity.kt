package com.inventario.mobile.presentation.sala

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.databinding.ActivitySimpleSalaSelectionBinding
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.presentation.adapter.SalaAdapter
import com.inventario.mobile.ui.coleta.ColetaActivity
import java.util.Date

/**
 * Versão simplificada da SalaSelectionActivity para teste sem dependências complexas
 */
class SimpleSalaSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimpleSalaSelectionBinding
    private lateinit var salaAdapter: SalaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleSalaSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadMockData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Selecionar Sala"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        salaAdapter = SalaAdapter(
            onSalaClick = { sala ->
                onSalaSelected(sala)
            }
        )
        
        binding.recyclerViewSalas.apply {
            layoutManager = LinearLayoutManager(this@SimpleSalaSelectionActivity)
            adapter = salaAdapter
        }
    }

    private fun loadMockData() {
        // Criar dados mock para teste
        val mockSalas = listOf(
            Sala(
                id = 1,
                nome = "Sala 101",
                codigo = "S101",
                descricao = "Sala de aula 101",
                ativo = true,
                setorId = 1,
                sincronizado = true,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                id = 2,
                nome = "Sala 102",
                codigo = "S102",
                descricao = "Sala de aula 102",
                ativo = true,
                setorId = 1,
                sincronizado = true,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                id = 3,
                nome = "Laboratório 201",
                codigo = "L201",
                descricao = "Laboratório de informática",
                ativo = true,
                setorId = 2,
                sincronizado = true,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            )
        )
        
        salaAdapter.submitList(mockSalas)
    }

    private fun onSalaSelected(sala: Sala) {
        // Navegar para a tela de coleta
        val intent = Intent(this, ColetaActivity::class.java).apply {
            putExtra(ColetaActivity.EXTRA_SALA_ID, sala.id)
            putExtra(ColetaActivity.EXTRA_SALA_NOME, sala.nome)
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
