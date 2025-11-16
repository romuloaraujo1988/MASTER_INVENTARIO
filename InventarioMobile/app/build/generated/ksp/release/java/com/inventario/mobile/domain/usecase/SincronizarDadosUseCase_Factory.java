package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.repository.SyncRepository;
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
public final class SincronizarDadosUseCase_Factory implements Factory<SincronizarDadosUseCase> {
  private final Provider<SyncRepository> syncRepositoryProvider;

  public SincronizarDadosUseCase_Factory(Provider<SyncRepository> syncRepositoryProvider) {
    this.syncRepositoryProvider = syncRepositoryProvider;
  }

  @Override
  public SincronizarDadosUseCase get() {
    return newInstance(syncRepositoryProvider.get());
  }

  public static SincronizarDadosUseCase_Factory create(
      Provider<SyncRepository> syncRepositoryProvider) {
    return new SincronizarDadosUseCase_Factory(syncRepositoryProvider);
  }

  public static SincronizarDadosUseCase newInstance(SyncRepository syncRepository) {
    return new SincronizarDadosUseCase(syncRepository);
  }
}
