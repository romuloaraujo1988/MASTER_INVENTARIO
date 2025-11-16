package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.local.LocalDataManager;
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
public final class ObterUsuarioAtualUseCase_Factory implements Factory<ObterUsuarioAtualUseCase> {
  private final Provider<LocalDataManager> localDataManagerProvider;

  public ObterUsuarioAtualUseCase_Factory(Provider<LocalDataManager> localDataManagerProvider) {
    this.localDataManagerProvider = localDataManagerProvider;
  }

  @Override
  public ObterUsuarioAtualUseCase get() {
    return newInstance(localDataManagerProvider.get());
  }

  public static ObterUsuarioAtualUseCase_Factory create(
      Provider<LocalDataManager> localDataManagerProvider) {
    return new ObterUsuarioAtualUseCase_Factory(localDataManagerProvider);
  }

  public static ObterUsuarioAtualUseCase newInstance(LocalDataManager localDataManager) {
    return new ObterUsuarioAtualUseCase(localDataManager);
  }
}
