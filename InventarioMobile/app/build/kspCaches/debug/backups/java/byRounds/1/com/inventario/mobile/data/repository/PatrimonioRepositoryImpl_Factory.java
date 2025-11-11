package com.inventario.mobile.data.repository;

import android.content.Context;
import com.inventario.mobile.data.strategy.DataSourceStrategyFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class PatrimonioRepositoryImpl_Factory implements Factory<PatrimonioRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<DataSourceStrategyFactory> strategyFactoryProvider;

  public PatrimonioRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<DataSourceStrategyFactory> strategyFactoryProvider) {
    this.contextProvider = contextProvider;
    this.strategyFactoryProvider = strategyFactoryProvider;
  }

  @Override
  public PatrimonioRepositoryImpl get() {
    return newInstance(contextProvider.get(), strategyFactoryProvider.get());
  }

  public static PatrimonioRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<DataSourceStrategyFactory> strategyFactoryProvider) {
    return new PatrimonioRepositoryImpl_Factory(contextProvider, strategyFactoryProvider);
  }

  public static PatrimonioRepositoryImpl newInstance(Context context,
      DataSourceStrategyFactory strategyFactory) {
    return new PatrimonioRepositoryImpl(context, strategyFactory);
  }
}
