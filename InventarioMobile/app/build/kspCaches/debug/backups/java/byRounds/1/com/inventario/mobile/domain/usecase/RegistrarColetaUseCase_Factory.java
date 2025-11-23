package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.local.LocalDataManager;
import com.inventario.mobile.domain.repository.ColetaRepository;
import com.inventario.mobile.domain.repository.PatrimonioRepository;
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
public final class RegistrarColetaUseCase_Factory implements Factory<RegistrarColetaUseCase> {
  private final Provider<ColetaRepository> coletaRepositoryProvider;

  private final Provider<PatrimonioRepository> patrimonioRepositoryProvider;

  private final Provider<LocalDataManager> localDataManagerProvider;

  public RegistrarColetaUseCase_Factory(Provider<ColetaRepository> coletaRepositoryProvider,
      Provider<PatrimonioRepository> patrimonioRepositoryProvider,
      Provider<LocalDataManager> localDataManagerProvider) {
    this.coletaRepositoryProvider = coletaRepositoryProvider;
    this.patrimonioRepositoryProvider = patrimonioRepositoryProvider;
    this.localDataManagerProvider = localDataManagerProvider;
  }

  @Override
  public RegistrarColetaUseCase get() {
    return newInstance(coletaRepositoryProvider.get(), patrimonioRepositoryProvider.get(), localDataManagerProvider.get());
  }

  public static RegistrarColetaUseCase_Factory create(
      Provider<ColetaRepository> coletaRepositoryProvider,
      Provider<PatrimonioRepository> patrimonioRepositoryProvider,
      Provider<LocalDataManager> localDataManagerProvider) {
    return new RegistrarColetaUseCase_Factory(coletaRepositoryProvider, patrimonioRepositoryProvider, localDataManagerProvider);
  }

  public static RegistrarColetaUseCase newInstance(ColetaRepository coletaRepository,
      PatrimonioRepository patrimonioRepository, LocalDataManager localDataManager) {
    return new RegistrarColetaUseCase(coletaRepository, patrimonioRepository, localDataManager);
  }
}
