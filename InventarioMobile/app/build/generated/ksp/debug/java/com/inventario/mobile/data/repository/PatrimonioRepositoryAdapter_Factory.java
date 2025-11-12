package com.inventario.mobile.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PatrimonioRepositoryAdapter_Factory implements Factory<PatrimonioRepositoryAdapter> {
  private final Provider<PatrimonioRepositoryImpl> implProvider;

  public PatrimonioRepositoryAdapter_Factory(Provider<PatrimonioRepositoryImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public PatrimonioRepositoryAdapter get() {
    return newInstance(implProvider.get());
  }

  public static PatrimonioRepositoryAdapter_Factory create(
      Provider<PatrimonioRepositoryImpl> implProvider) {
    return new PatrimonioRepositoryAdapter_Factory(implProvider);
  }

  public static PatrimonioRepositoryAdapter newInstance(PatrimonioRepositoryImpl impl) {
    return new PatrimonioRepositoryAdapter(impl);
  }
}
