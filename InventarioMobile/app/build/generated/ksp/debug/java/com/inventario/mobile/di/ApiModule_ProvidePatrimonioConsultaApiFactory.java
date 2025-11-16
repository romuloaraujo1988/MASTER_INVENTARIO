package com.inventario.mobile.di;

import com.inventario.mobile.data.remote.api.PatrimonioConsultaApi;
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
public final class ApiModule_ProvidePatrimonioConsultaApiFactory implements Factory<PatrimonioConsultaApi> {
  private final Provider<Retrofit> retrofitProvider;

  public ApiModule_ProvidePatrimonioConsultaApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public PatrimonioConsultaApi get() {
    return providePatrimonioConsultaApi(retrofitProvider.get());
  }

  public static ApiModule_ProvidePatrimonioConsultaApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new ApiModule_ProvidePatrimonioConsultaApiFactory(retrofitProvider);
  }

  public static PatrimonioConsultaApi providePatrimonioConsultaApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.providePatrimonioConsultaApi(retrofit));
  }
}
