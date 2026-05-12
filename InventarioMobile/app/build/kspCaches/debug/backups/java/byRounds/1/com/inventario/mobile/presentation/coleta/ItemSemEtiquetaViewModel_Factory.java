package com.inventario.mobile.presentation.coleta;

import com.inventario.mobile.domain.usecase.BuscarSugestoesDescricaoUseCase;
import com.inventario.mobile.domain.usecase.LimparCacheDeOutrosInventariosUseCase;
import com.inventario.mobile.domain.usecase.MarcarPatrimonioColetadoLocalmenteUseCase;
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase;
import com.inventario.mobile.utils.PreferencesManager;
import com.inventario.mobile.utils.VibrationHelper;
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
public final class ItemSemEtiquetaViewModel_Factory implements Factory<ItemSemEtiquetaViewModel> {
  private final Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider;

  private final Provider<BuscarSugestoesDescricaoUseCase> buscarSugestoesDescricaoUseCaseProvider;

  private final Provider<MarcarPatrimonioColetadoLocalmenteUseCase> marcarPatrimonioColetadoLocalmenteUseCaseProvider;

  private final Provider<LimparCacheDeOutrosInventariosUseCase> limparCacheDeOutrosInventariosUseCaseProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  private final Provider<VibrationHelper> vibrationHelperProvider;

  public ItemSemEtiquetaViewModel_Factory(
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<BuscarSugestoesDescricaoUseCase> buscarSugestoesDescricaoUseCaseProvider,
      Provider<MarcarPatrimonioColetadoLocalmenteUseCase> marcarPatrimonioColetadoLocalmenteUseCaseProvider,
      Provider<LimparCacheDeOutrosInventariosUseCase> limparCacheDeOutrosInventariosUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<VibrationHelper> vibrationHelperProvider) {
    this.registrarColetaUseCaseProvider = registrarColetaUseCaseProvider;
    this.buscarSugestoesDescricaoUseCaseProvider = buscarSugestoesDescricaoUseCaseProvider;
    this.marcarPatrimonioColetadoLocalmenteUseCaseProvider = marcarPatrimonioColetadoLocalmenteUseCaseProvider;
    this.limparCacheDeOutrosInventariosUseCaseProvider = limparCacheDeOutrosInventariosUseCaseProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
    this.vibrationHelperProvider = vibrationHelperProvider;
  }

  @Override
  public ItemSemEtiquetaViewModel get() {
    return newInstance(registrarColetaUseCaseProvider.get(), buscarSugestoesDescricaoUseCaseProvider.get(), marcarPatrimonioColetadoLocalmenteUseCaseProvider.get(), limparCacheDeOutrosInventariosUseCaseProvider.get(), preferencesManagerProvider.get(), vibrationHelperProvider.get());
  }

  public static ItemSemEtiquetaViewModel_Factory create(
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<BuscarSugestoesDescricaoUseCase> buscarSugestoesDescricaoUseCaseProvider,
      Provider<MarcarPatrimonioColetadoLocalmenteUseCase> marcarPatrimonioColetadoLocalmenteUseCaseProvider,
      Provider<LimparCacheDeOutrosInventariosUseCase> limparCacheDeOutrosInventariosUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<VibrationHelper> vibrationHelperProvider) {
    return new ItemSemEtiquetaViewModel_Factory(registrarColetaUseCaseProvider, buscarSugestoesDescricaoUseCaseProvider, marcarPatrimonioColetadoLocalmenteUseCaseProvider, limparCacheDeOutrosInventariosUseCaseProvider, preferencesManagerProvider, vibrationHelperProvider);
  }

  public static ItemSemEtiquetaViewModel newInstance(RegistrarColetaUseCase registrarColetaUseCase,
      BuscarSugestoesDescricaoUseCase buscarSugestoesDescricaoUseCase,
      MarcarPatrimonioColetadoLocalmenteUseCase marcarPatrimonioColetadoLocalmenteUseCase,
      LimparCacheDeOutrosInventariosUseCase limparCacheDeOutrosInventariosUseCase,
      PreferencesManager preferencesManager, VibrationHelper vibrationHelper) {
    return new ItemSemEtiquetaViewModel(registrarColetaUseCase, buscarSugestoesDescricaoUseCase, marcarPatrimonioColetadoLocalmenteUseCase, limparCacheDeOutrosInventariosUseCase, preferencesManager, vibrationHelper);
  }
}
