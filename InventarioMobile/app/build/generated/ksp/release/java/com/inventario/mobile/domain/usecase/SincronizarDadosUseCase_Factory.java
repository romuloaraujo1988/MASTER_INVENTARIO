package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.domain.repository.SincronizacaoRepository;
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
  private final Provider<SincronizacaoRepository> sincronizacaoRepositoryProvider;

  public SincronizarDadosUseCase_Factory(
      Provider<SincronizacaoRepository> sincronizacaoRepositoryProvider) {
    this.sincronizacaoRepositoryProvider = sincronizacaoRepositoryProvider;
  }

  @Override
  public SincronizarDadosUseCase get() {
    return newInstance(sincronizacaoRepositoryProvider.get());
  }

  public static SincronizarDadosUseCase_Factory create(
      Provider<SincronizacaoRepository> sincronizacaoRepositoryProvider) {
    return new SincronizarDadosUseCase_Factory(sincronizacaoRepositoryProvider);
  }

  public static SincronizarDadosUseCase newInstance(
      SincronizacaoRepository sincronizacaoRepository) {
    return new SincronizarDadosUseCase(sincronizacaoRepository);
  }
}
