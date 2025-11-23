package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.network.DeviceInfoInterceptor;
import com.inventario.mobile.network.OfflineFallbackInterceptor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

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
public final class ApiModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<Context> contextProvider;

  private final Provider<HttpLoggingInterceptor> loggingInterceptorProvider;

  private final Provider<Interceptor> authInterceptorProvider;

  private final Provider<DeviceInfoInterceptor> deviceInfoInterceptorProvider;

  private final Provider<OfflineFallbackInterceptor> offlineFallbackInterceptorProvider;

  public ApiModule_ProvideOkHttpClientFactory(Provider<Context> contextProvider,
      Provider<HttpLoggingInterceptor> loggingInterceptorProvider,
      Provider<Interceptor> authInterceptorProvider,
      Provider<DeviceInfoInterceptor> deviceInfoInterceptorProvider,
      Provider<OfflineFallbackInterceptor> offlineFallbackInterceptorProvider) {
    this.contextProvider = contextProvider;
    this.loggingInterceptorProvider = loggingInterceptorProvider;
    this.authInterceptorProvider = authInterceptorProvider;
    this.deviceInfoInterceptorProvider = deviceInfoInterceptorProvider;
    this.offlineFallbackInterceptorProvider = offlineFallbackInterceptorProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(contextProvider.get(), loggingInterceptorProvider.get(), authInterceptorProvider.get(), deviceInfoInterceptorProvider.get(), offlineFallbackInterceptorProvider.get());
  }

  public static ApiModule_ProvideOkHttpClientFactory create(Provider<Context> contextProvider,
      Provider<HttpLoggingInterceptor> loggingInterceptorProvider,
      Provider<Interceptor> authInterceptorProvider,
      Provider<DeviceInfoInterceptor> deviceInfoInterceptorProvider,
      Provider<OfflineFallbackInterceptor> offlineFallbackInterceptorProvider) {
    return new ApiModule_ProvideOkHttpClientFactory(contextProvider, loggingInterceptorProvider, authInterceptorProvider, deviceInfoInterceptorProvider, offlineFallbackInterceptorProvider);
  }

  public static OkHttpClient provideOkHttpClient(Context context,
      HttpLoggingInterceptor loggingInterceptor, Interceptor authInterceptor,
      DeviceInfoInterceptor deviceInfoInterceptor,
      OfflineFallbackInterceptor offlineFallbackInterceptor) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideOkHttpClient(context, loggingInterceptor, authInterceptor, deviceInfoInterceptor, offlineFallbackInterceptor));
  }
}
