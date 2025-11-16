package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.data.remote.api.ApiService;
import com.inventario.mobile.data.repository.InventarioRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class RepositoryModule_Companion_ProvideInventarioRepositoryFactory implements Factory<InventarioRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<ApiService> apiServiceProvider;

  public RepositoryModule_Companion_ProvideInventarioRepositoryFactory(
      Provider<Context> contextProvider, Provider<ApiService> apiServiceProvider) {
    this.contextProvider = contextProvider;
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public InventarioRepository get() {
    return provideInventarioRepository(contextProvider.get(), apiServiceProvider.get());
  }

  public static RepositoryModule_Companion_ProvideInventarioRepositoryFactory create(
      Provider<Context> contextProvider, Provider<ApiService> apiServiceProvider) {
    return new RepositoryModule_Companion_ProvideInventarioRepositoryFactory(contextProvider, apiServiceProvider);
  }

  public static InventarioRepository provideInventarioRepository(Context context,
      ApiService apiService) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.Companion.provideInventarioRepository(context, apiService));
  }
}
