package com.inventario.mobile.di;

import com.inventario.mobile.data.remote.api.PatrimonioApi;
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
public final class ApiModule_ProvidePatrimonioApiFactory implements Factory<PatrimonioApi> {
  private final Provider<Retrofit> retrofitProvider;

  public ApiModule_ProvidePatrimonioApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public PatrimonioApi get() {
    return providePatrimonioApi(retrofitProvider.get());
  }

  public static ApiModule_ProvidePatrimonioApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new ApiModule_ProvidePatrimonioApiFactory(retrofitProvider);
  }

  public static PatrimonioApi providePatrimonioApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.providePatrimonioApi(retrofit));
  }
}
