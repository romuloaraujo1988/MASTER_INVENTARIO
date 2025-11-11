package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.api.PatrimonioApi;
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
public final class ApiModule_ProvidePatrimonioApiLegacyFactory implements Factory<PatrimonioApi> {
  private final Provider<Context> contextProvider;

  public ApiModule_ProvidePatrimonioApiLegacyFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PatrimonioApi get() {
    return providePatrimonioApiLegacy(contextProvider.get());
  }

  public static ApiModule_ProvidePatrimonioApiLegacyFactory create(
      Provider<Context> contextProvider) {
    return new ApiModule_ProvidePatrimonioApiLegacyFactory(contextProvider);
  }

  public static PatrimonioApi providePatrimonioApiLegacy(Context context) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.providePatrimonioApiLegacy(context));
  }
}
