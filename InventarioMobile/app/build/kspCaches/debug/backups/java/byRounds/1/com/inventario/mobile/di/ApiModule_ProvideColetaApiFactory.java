package com.inventario.mobile.di;

import com.inventario.mobile.data.remote.api.ColetaApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class ApiModule_ProvideColetaApiFactory implements Factory<ColetaApi> {
  private final Provider<Retrofit> retrofitProvider;

  public ApiModule_ProvideColetaApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ColetaApi get() {
    return provideColetaApi(retrofitProvider.get());
  }

  public static ApiModule_ProvideColetaApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new ApiModule_ProvideColetaApiFactory(retrofitProvider);
  }

  public static ColetaApi provideColetaApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideColetaApi(retrofit));
  }
}
