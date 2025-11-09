package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.data.remote.api.ColetaApi;
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
public final class ApiModule_ProvideColetaApiFactory implements Factory<ColetaApi> {
  private final Provider<Context> contextProvider;

  public ApiModule_ProvideColetaApiFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ColetaApi get() {
    return provideColetaApi(contextProvider.get());
  }

  public static ApiModule_ProvideColetaApiFactory create(Provider<Context> contextProvider) {
    return new ApiModule_ProvideColetaApiFactory(contextProvider);
  }

  public static ColetaApi provideColetaApi(Context context) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideColetaApi(context));
  }
}
