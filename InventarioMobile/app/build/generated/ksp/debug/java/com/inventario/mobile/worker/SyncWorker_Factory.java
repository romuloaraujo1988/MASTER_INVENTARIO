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
public final class SyncWorker_Factory {
  private final Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider;

  public SyncWorker_Factory(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider) {
    this.sincronizarColetasPendentesUseCaseProvider = sincronizarColetasPendentesUseCaseProvider;
  }

  public SyncWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, sincronizarColetasPendentesUseCaseProvider.get());
  }

  public static SyncWorker_Factory create(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider) {
    return new SyncWorker_Factory(sincronizarColetasPendentesUseCaseProvider);
  }

  public static SyncWorker newInstance(Context context, WorkerParameters workerParams,
      SincronizarColetasPendentesUseCase sincronizarColetasPendentesUseCase) {
    return new SyncWorker(context, workerParams, sincronizarColetasPendentesUseCase);
  }
}
