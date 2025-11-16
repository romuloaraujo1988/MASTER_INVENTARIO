package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.api.PatrimonioApi;
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
public final class VerificarSePatrimonioFoiColetadoUseCase_Factory implements Factory<VerificarSePatrimonioFoiColetadoUseCase> {
  private final Provider<PatrimonioApi> patrimonioApiProvider;

  public VerificarSePatrimonioFoiColetadoUseCase_Factory(
      Provider<PatrimonioApi> patrimonioApiProvider) {
    this.patrimonioApiProvider = patrimonioApiProvider;
  }

  @Override
  public VerificarSePatrimonioFoiColetadoUseCase get() {
    return newInstance(patrimonioApiProvider.get());
  }

  public static VerificarSePatrimonioFoiColetadoUseCase_Factory create(
      Provider<PatrimonioApi> patrimonioApiProvider) {
    return new VerificarSePatrimonioFoiColetadoUseCase_Factory(patrimonioApiProvider);
  }

  public static VerificarSePatrimonioFoiColetadoUseCase newInstance(PatrimonioApi patrimonioApi) {
    return new VerificarSePatrimonioFoiColetadoUseCase(patrimonioApi);
  }
}
