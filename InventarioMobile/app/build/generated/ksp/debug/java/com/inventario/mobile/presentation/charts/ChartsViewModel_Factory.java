package com.inventario.mobile.presentation.charts;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ChartsViewModel_Factory implements Factory<ChartsViewModel> {
  private final Provider<ChartDataProvider> chartDataProvider;

  public ChartsViewModel_Factory(Provider<ChartDataProvider> chartDataProvider) {
    this.chartDataProvider = chartDataProvider;
  }

  @Override
  public ChartsViewModel get() {
    return newInstance(chartDataProvider.get());
  }

  public static ChartsViewModel_Factory create(Provider<ChartDataProvider> chartDataProvider) {
    return new ChartsViewModel_Factory(chartDataProvider);
  }

  public static ChartsViewModel newInstance(ChartDataProvider chartDataProvider) {
    return new ChartsViewModel(chartDataProvider);
  }
}
