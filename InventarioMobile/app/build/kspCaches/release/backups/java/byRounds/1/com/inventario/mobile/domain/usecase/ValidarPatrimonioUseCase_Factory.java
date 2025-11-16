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
public final class ValidarPatrimonioUseCase_Factory implements Factory<ValidarPatrimonioUseCase> {
  private final Provider<PatrimonioApi> patrimonioApiProvider;

  public ValidarPatrimonioUseCase_Factory(Provider<PatrimonioApi> patrimonioApiProvider) {
    this.patrimonioApiProvider = patrimonioApiProvider;
  }

  @Override
  public ValidarPatrimonioUseCase get() {
    return newInstance(patrimonioApiProvider.get());
  }

  public static ValidarPatrimonioUseCase_Factory create(
      Provider<PatrimonioApi> patrimonioApiProvider) {
    return new ValidarPatrimonioUseCase_Factory(patrimonioApiProvider);
  }

  public static ValidarPatrimonioUseCase newInstance(PatrimonioApi patrimonioApi) {
    return new ValidarPatrimonioUseCase(patrimonioApi);
  }
}
