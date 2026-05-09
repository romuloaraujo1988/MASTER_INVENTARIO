package com.inventario.mobile.presentation.descricao;

import com.inventario.mobile.domain.usecase.BuscarDescricoesNaoColetadasUseCase;
import com.inventario.mobile.domain.usecase.BuscarPatrimoniosPorDescricaoUseCase;
import com.inventario.mobile.domain.usecase.RegistrarColetaPorDescricaoUseCase;
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase;
import com.inventario.mobile.utils.PreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DescricaoSelectionViewModelClean_Factory implements Factory<DescricaoSelectionViewModelClean> {
  private final Provider<BuscarDescricoesNaoColetadasUseCase> buscarDescricoesNaoColetadasUseCaseProvider;

  private final Provider<BuscarPatrimoniosPorDescricaoUseCase> buscarPatrimoniosPorDescricaoUseCaseProvider;

  private final Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider;

  private final Provider<RegistrarColetaPorDescricaoUseCase> registrarColetaPorDescricaoUseCaseProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public DescricaoSelectionViewModelClean_Factory(
      Provider<BuscarDescricoesNaoColetadasUseCase> buscarDescricoesNaoColetadasUseCaseProvider,
      Provider<BuscarPatrimoniosPorDescricaoUseCase> buscarPatrimoniosPorDescricaoUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<RegistrarColetaPorDescricaoUseCase> registrarColetaPorDescricaoUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.buscarDescricoesNaoColetadasUseCaseProvider = buscarDescricoesNaoColetadasUseCaseProvider;
    this.buscarPatrimoniosPorDescricaoUseCaseProvider = buscarPatrimoniosPorDescricaoUseCaseProvider;
    this.registrarColetaUseCaseProvider = registrarColetaUseCaseProvider;
    this.registrarColetaPorDescricaoUseCaseProvider = registrarColetaPorDescricaoUseCaseProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public DescricaoSelectionViewModelClean get() {
    return newInstance(buscarDescricoesNaoColetadasUseCaseProvider.get(), buscarPatrimoniosPorDescricaoUseCaseProvider.get(), registrarColetaUseCaseProvider.get(), registrarColetaPorDescricaoUseCaseProvider.get(), preferencesManagerProvider.get());
  }

  public static DescricaoSelectionViewModelClean_Factory create(
      Provider<BuscarDescricoesNaoColetadasUseCase> buscarDescricoesNaoColetadasUseCaseProvider,
      Provider<BuscarPatrimoniosPorDescricaoUseCase> buscarPatrimoniosPorDescricaoUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<RegistrarColetaPorDescricaoUseCase> registrarColetaPorDescricaoUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new DescricaoSelectionViewModelClean_Factory(buscarDescricoesNaoColetadasUseCaseProvider, buscarPatrimoniosPorDescricaoUseCaseProvider, registrarColetaUseCaseProvider, registrarColetaPorDescricaoUseCaseProvider, preferencesManagerProvider);
  }

  public static DescricaoSelectionViewModelClean newInstance(
      BuscarDescricoesNaoColetadasUseCase buscarDescricoesNaoColetadasUseCase,
      BuscarPatrimoniosPorDescricaoUseCase buscarPatrimoniosPorDescricaoUseCase,
      RegistrarColetaUseCase registrarColetaUseCase,
      RegistrarColetaPorDescricaoUseCase registrarColetaPorDescricaoUseCase,
      PreferencesManager preferencesManager) {
    return new DescricaoSelectionViewModelClean(buscarDescricoesNaoColetadasUseCase, buscarPatrimoniosPorDescricaoUseCase, registrarColetaUseCase, registrarColetaPorDescricaoUseCase, preferencesManager);
  }
}
