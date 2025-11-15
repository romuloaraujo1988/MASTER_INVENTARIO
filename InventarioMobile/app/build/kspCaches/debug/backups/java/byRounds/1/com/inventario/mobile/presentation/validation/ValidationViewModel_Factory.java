package com.inventario.mobile.presentation.validation;

import com.inventario.mobile.domain.usecase.ValidarPatrimonioUseCase;
import com.inventario.mobile.domain.usecase.VerificarDuplicataColetaUseCase;
import com.inventario.mobile.domain.usecase.VerificarSePatrimonioFoiColetadoUseCase;
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
public final class ValidationViewModel_Factory implements Factory<ValidationViewModel> {
  private final Provider<ValidarPatrimonioUseCase> validarPatrimonioUseCaseProvider;

  private final Provider<VerificarDuplicataColetaUseCase> verificarDuplicataColetaUseCaseProvider;

  private final Provider<VerificarSePatrimonioFoiColetadoUseCase> verificarSePatrimonioFoiColetadoUseCaseProvider;

  public ValidationViewModel_Factory(
      Provider<ValidarPatrimonioUseCase> validarPatrimonioUseCaseProvider,
      Provider<VerificarDuplicataColetaUseCase> verificarDuplicataColetaUseCaseProvider,
      Provider<VerificarSePatrimonioFoiColetadoUseCase> verificarSePatrimonioFoiColetadoUseCaseProvider) {
    this.validarPatrimonioUseCaseProvider = validarPatrimonioUseCaseProvider;
    this.verificarDuplicataColetaUseCaseProvider = verificarDuplicataColetaUseCaseProvider;
    this.verificarSePatrimonioFoiColetadoUseCaseProvider = verificarSePatrimonioFoiColetadoUseCaseProvider;
  }

  @Override
  public ValidationViewModel get() {
    return newInstance(validarPatrimonioUseCaseProvider.get(), verificarDuplicataColetaUseCaseProvider.get(), verificarSePatrimonioFoiColetadoUseCaseProvider.get());
  }

  public static ValidationViewModel_Factory create(
      Provider<ValidarPatrimonioUseCase> validarPatrimonioUseCaseProvider,
      Provider<VerificarDuplicataColetaUseCase> verificarDuplicataColetaUseCaseProvider,
      Provider<VerificarSePatrimonioFoiColetadoUseCase> verificarSePatrimonioFoiColetadoUseCaseProvider) {
    return new ValidationViewModel_Factory(validarPatrimonioUseCaseProvider, verificarDuplicataColetaUseCaseProvider, verificarSePatrimonioFoiColetadoUseCaseProvider);
  }

  public static ValidationViewModel newInstance(ValidarPatrimonioUseCase validarPatrimonioUseCase,
      VerificarDuplicataColetaUseCase verificarDuplicataColetaUseCase,
      VerificarSePatrimonioFoiColetadoUseCase verificarSePatrimonioFoiColetadoUseCase) {
    return new ValidationViewModel(validarPatrimonioUseCase, verificarDuplicataColetaUseCase, verificarSePatrimonioFoiColetadoUseCase);
  }
}
