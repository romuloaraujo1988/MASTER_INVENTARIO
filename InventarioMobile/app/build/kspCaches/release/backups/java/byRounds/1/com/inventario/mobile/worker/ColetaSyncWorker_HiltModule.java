package com.inventario.mobile.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = ColetaSyncWorker.class
)
public interface ColetaSyncWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.inventario.mobile.worker.ColetaSyncWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(ColetaSyncWorker_AssistedFactory factory);
}
