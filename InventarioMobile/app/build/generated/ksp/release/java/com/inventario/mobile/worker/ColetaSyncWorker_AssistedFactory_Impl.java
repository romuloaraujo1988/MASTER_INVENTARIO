package com.inventario.mobile.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ColetaSyncWorker_AssistedFactory_Impl implements ColetaSyncWorker_AssistedFactory {
  private final ColetaSyncWorker_Factory delegateFactory;

  ColetaSyncWorker_AssistedFactory_Impl(ColetaSyncWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public ColetaSyncWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<ColetaSyncWorker_AssistedFactory> create(
      ColetaSyncWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ColetaSyncWorker_AssistedFactory_Impl(delegateFactory));
  }
}
