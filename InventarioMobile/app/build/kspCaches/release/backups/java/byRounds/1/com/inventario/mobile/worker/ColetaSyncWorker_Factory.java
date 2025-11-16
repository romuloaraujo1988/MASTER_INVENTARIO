package com.inventario.mobile.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase;
import dagger.internal.DaggerGenerated;
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
public final class ColetaSyncWorker_Factory {
  private final Provider<SincronizarColetasPendentesUseCase> sincronizarColetasUseCaseProvider;

  public ColetaSyncWorker_Factory(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasUseCaseProvider) {
    this.sincronizarColetasUseCaseProvider = sincronizarColetasUseCaseProvider;
  }

  public ColetaSyncWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, sincronizarColetasUseCaseProvider.get());
  }

  public static ColetaSyncWorker_Factory create(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasUseCaseProvider) {
    return new ColetaSyncWorker_Factory(sincronizarColetasUseCaseProvider);
  }

  public static ColetaSyncWorker newInstance(Context appContext, WorkerParameters workerParams,
      SincronizarColetasPendentesUseCase sincronizarColetasUseCase) {
    return new ColetaSyncWorker(appContext, workerParams, sincronizarColetasUseCase);
  }
}
