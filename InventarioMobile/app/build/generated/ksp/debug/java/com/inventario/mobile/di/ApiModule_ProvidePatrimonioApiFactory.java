package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.data.remote.api.PatrimonioApi;
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
public final class ApiModule_ProvidePatrimonioApiFactory implements Factory<PatrimonioApi> {
  private final Provider<Context> contextProvider;

  public ApiModule_ProvidePatrimonioApiFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PatrimonioApi get() {
    return providePatrimonioApi(contextProvider.get());
  }

  public static ApiModule_ProvidePatrimonioApiFactory create(Provider<Context> contextProvider) {
    return new ApiModule_ProvidePatrimonioApiFactory(contextProvider);
  }

  public static PatrimonioApi providePatrimonioApi(Context context) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.providePatrimonioApi(context));
  }
}
