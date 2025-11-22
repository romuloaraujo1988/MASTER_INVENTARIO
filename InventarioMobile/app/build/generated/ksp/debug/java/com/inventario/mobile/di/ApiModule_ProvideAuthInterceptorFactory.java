package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.utils.PreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.Interceptor;

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
public final class ApiModule_ProvideAuthInterceptorFactory implements Factory<Interceptor> {
  private final Provider<Context> contextProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public ApiModule_ProvideAuthInterceptorFactory(Provider<Context> contextProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.contextProvider = contextProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public Interceptor get() {
    return provideAuthInterceptor(contextProvider.get(), preferencesManagerProvider.get());
  }

  public static ApiModule_ProvideAuthInterceptorFactory create(Provider<Context> contextProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new ApiModule_ProvideAuthInterceptorFactory(contextProvider, preferencesManagerProvider);
  }

  public static Interceptor provideAuthInterceptor(Context context,
      PreferencesManager preferencesManager) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideAuthInterceptor(context, preferencesManager));
  }
}
