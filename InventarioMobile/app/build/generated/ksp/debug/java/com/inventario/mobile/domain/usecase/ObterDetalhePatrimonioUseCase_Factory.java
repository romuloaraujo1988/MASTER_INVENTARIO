package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository;
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
public final class ObterDetalhePatrimonioUseCase_Factory implements Factory<ObterDetalhePatrimonioUseCase> {
  private final Provider<PatrimonioConsultaRepository> repositoryProvider;

  public ObterDetalhePatrimonioUseCase_Factory(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ObterDetalhePatrimonioUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ObterDetalhePatrimonioUseCase_Factory create(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    return new ObterDetalhePatrimonioUseCase_Factory(repositoryProvider);
  }

  public static ObterDetalhePatrimonioUseCase newInstance(PatrimonioConsultaRepository repository) {
    return new ObterDetalhePatrimonioUseCase(repository);
  }
}
