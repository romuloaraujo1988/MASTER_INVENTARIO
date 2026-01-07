package com.inventario.mobile.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase;
import com.inventario.mobile.domain.usecase.SincronizarFotosReferenciaUseCase;
import com.inventario.mobile.utils.PreferencesManager;
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

  private final Provider<SincronizarFotosReferenciaUseCase> sincronizarFotosReferenciaUseCaseProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public SyncWorker_Factory(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider,
      Provider<SincronizarFotosReferenciaUseCase> sincronizarFotosReferenciaUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.sincronizarColetasPendentesUseCaseProvider = sincronizarColetasPendentesUseCaseProvider;
    this.sincronizarFotosReferenciaUseCaseProvider = sincronizarFotosReferenciaUseCaseProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  public SyncWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, sincronizarColetasPendentesUseCaseProvider.get(), sincronizarFotosReferenciaUseCaseProvider.get(), preferencesManagerProvider.get());
  }

  public static SyncWorker_Factory create(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider,
      Provider<SincronizarFotosReferenciaUseCase> sincronizarFotosReferenciaUseCaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new SyncWorker_Factory(sincronizarColetasPendentesUseCaseProvider, sincronizarFotosReferenciaUseCaseProvider, preferencesManagerProvider);
  }

  public static SyncWorker newInstance(Context context, WorkerParameters workerParams,
      SincronizarColetasPendentesUseCase sincronizarColetasPendentesUseCase,
      SincronizarFotosReferenciaUseCase sincronizarFotosReferenciaUseCase,
      PreferencesManager preferencesManager) {
    return new SyncWorker(context, workerParams, sincronizarColetasPendentesUseCase, sincronizarFotosReferenciaUseCase, preferencesManager);
  }
}
