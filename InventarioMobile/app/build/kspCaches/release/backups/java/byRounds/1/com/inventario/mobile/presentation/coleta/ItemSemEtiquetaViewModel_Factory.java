package com.inventario.mobile.presentation.coleta;

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
public final class ItemSemEtiquetaViewModel_Factory implements Factory<ItemSemEtiquetaViewModel> {
  private final Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public ItemSemEtiquetaViewModel_Factory(
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.registrarColetaUseCaseProvider = registrarColetaUseCaseProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public ItemSemEtiquetaViewModel get() {
    return newInstance(registrarColetaUseCaseProvider.get(), preferencesManagerProvider.get());
  }

  public static ItemSemEtiquetaViewModel_Factory create(
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new ItemSemEtiquetaViewModel_Factory(registrarColetaUseCaseProvider, preferencesManagerProvider);
  }

  public static ItemSemEtiquetaViewModel newInstance(RegistrarColetaUseCase registrarColetaUseCase,
      PreferencesManager preferencesManager) {
    return new ItemSemEtiquetaViewModel(registrarColetaUseCase, preferencesManager);
  }
}
