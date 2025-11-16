package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.data.remote.api.ApiService;
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
public final class ApiModule_ProvideApiServiceFactory implements Factory<ApiService> {
  private final Provider<Context> contextProvider;

  public ApiModule_ProvideApiServiceFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ApiService get() {
    return provideApiService(contextProvider.get());
  }

  public static ApiModule_ProvideApiServiceFactory create(Provider<Context> contextProvider) {
    return new ApiModule_ProvideApiServiceFactory(contextProvider);
  }

  public static ApiService provideApiService(Context context) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideApiService(context));
  }
}
