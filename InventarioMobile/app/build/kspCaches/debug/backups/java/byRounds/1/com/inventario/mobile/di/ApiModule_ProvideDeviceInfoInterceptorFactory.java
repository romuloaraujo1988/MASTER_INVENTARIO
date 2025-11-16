package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.network.DeviceInfoInterceptor;
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
public final class ApiModule_ProvideDeviceInfoInterceptorFactory implements Factory<DeviceInfoInterceptor> {
  private final Provider<Context> contextProvider;

  public ApiModule_ProvideDeviceInfoInterceptorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DeviceInfoInterceptor get() {
    return provideDeviceInfoInterceptor(contextProvider.get());
  }

  public static ApiModule_ProvideDeviceInfoInterceptorFactory create(
      Provider<Context> contextProvider) {
    return new ApiModule_ProvideDeviceInfoInterceptorFactory(contextProvider);
  }

  public static DeviceInfoInterceptor provideDeviceInfoInterceptor(Context context) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideDeviceInfoInterceptor(context));
  }
}
