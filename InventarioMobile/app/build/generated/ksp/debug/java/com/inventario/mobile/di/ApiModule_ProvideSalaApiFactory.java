package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.api.SalaApi;
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
public final class ApiModule_ProvideSalaApiFactory implements Factory<SalaApi> {
  private final Provider<Context> contextProvider;

  public ApiModule_ProvideSalaApiFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SalaApi get() {
    return provideSalaApi(contextProvider.get());
  }

  public static ApiModule_ProvideSalaApiFactory create(Provider<Context> contextProvider) {
    return new ApiModule_ProvideSalaApiFactory(contextProvider);
  }

  public static SalaApi provideSalaApi(Context context) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideSalaApi(context));
  }
}
