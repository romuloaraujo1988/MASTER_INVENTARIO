package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.util.NetworkChecker;
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
public final class UtilModule_ProvideNetworkCheckerFactory implements Factory<NetworkChecker> {
  private final Provider<Context> contextProvider;

  public UtilModule_ProvideNetworkCheckerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NetworkChecker get() {
    return provideNetworkChecker(contextProvider.get());
  }

  public static UtilModule_ProvideNetworkCheckerFactory create(Provider<Context> contextProvider) {
    return new UtilModule_ProvideNetworkCheckerFactory(contextProvider);
  }

  public static NetworkChecker provideNetworkChecker(Context context) {
    return Preconditions.checkNotNullFromProvides(UtilModule.INSTANCE.provideNetworkChecker(context));
  }
}
