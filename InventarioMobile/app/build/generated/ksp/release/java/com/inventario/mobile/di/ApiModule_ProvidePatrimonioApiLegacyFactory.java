package com.inventario.mobile.di;

import com.inventario.mobile.api.PatrimonioApi;
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
public final class ApiModule_ProvidePatrimonioApiLegacyFactory implements Factory<PatrimonioApi> {
  private final Provider<Retrofit> retrofitProvider;

  public ApiModule_ProvidePatrimonioApiLegacyFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public PatrimonioApi get() {
    return providePatrimonioApiLegacy(retrofitProvider.get());
  }

  public static ApiModule_ProvidePatrimonioApiLegacyFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new ApiModule_ProvidePatrimonioApiLegacyFactory(retrofitProvider);
  }

  public static PatrimonioApi providePatrimonioApiLegacy(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.providePatrimonioApiLegacy(retrofit));
  }
}
