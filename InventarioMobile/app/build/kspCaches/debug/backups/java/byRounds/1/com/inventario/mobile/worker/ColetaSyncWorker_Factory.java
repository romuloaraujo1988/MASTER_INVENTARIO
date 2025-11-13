package com.inventario.mobile.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase;
import com.inventario.mobile.sync.NotificationHelper;
import com.inventario.mobile.sync.SyncLogger;
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

  private final Provider<SyncLogger> syncLoggerProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public ColetaSyncWorker_Factory(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasUseCaseProvider,
      Provider<SyncLogger> syncLoggerProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.sincronizarColetasUseCaseProvider = sincronizarColetasUseCaseProvider;
    this.syncLoggerProvider = syncLoggerProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public ColetaSyncWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, sincronizarColetasUseCaseProvider.get(), syncLoggerProvider.get(), notificationHelperProvider.get());
  }

  public static ColetaSyncWorker_Factory create(
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasUseCaseProvider,
      Provider<SyncLogger> syncLoggerProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new ColetaSyncWorker_Factory(sincronizarColetasUseCaseProvider, syncLoggerProvider, notificationHelperProvider);
  }

  public static ColetaSyncWorker newInstance(Context appContext, WorkerParameters workerParams,
      SincronizarColetasPendentesUseCase sincronizarColetasUseCase, SyncLogger syncLogger,
      NotificationHelper notificationHelper) {
    return new ColetaSyncWorker(appContext, workerParams, sincronizarColetasUseCase, syncLogger, notificationHelper);
  }
}
