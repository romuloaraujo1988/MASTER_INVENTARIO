package com.inventario.mobile.di;

import com.inventario.mobile.api.SalaApi;
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
public final class ApiModule_ProvideSalaApiFactory implements Factory<SalaApi> {
  private final Provider<Retrofit> retrofitProvider;

  public ApiModule_ProvideSalaApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public SalaApi get() {
    return provideSalaApi(retrofitProvider.get());
  }

  public static ApiModule_ProvideSalaApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new ApiModule_ProvideSalaApiFactory(retrofitProvider);
  }

  public static SalaApi provideSalaApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideSalaApi(retrofit));
  }
}
