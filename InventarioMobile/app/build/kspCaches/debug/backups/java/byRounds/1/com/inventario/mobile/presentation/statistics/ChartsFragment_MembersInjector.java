package com.inventario.mobile.presentation.statistics;

import com.inventario.mobile.presentation.charts.ChartDataProvider;
import com.inventario.mobile.utils.PreferencesManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ChartsFragment_MembersInjector implements MembersInjector<ChartsFragment> {
  private final Provider<ChartDataProvider> chartDataProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public ChartsFragment_MembersInjector(Provider<ChartDataProvider> chartDataProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.chartDataProvider = chartDataProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  public static MembersInjector<ChartsFragment> create(
      Provider<ChartDataProvider> chartDataProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new ChartsFragment_MembersInjector(chartDataProvider, preferencesManagerProvider);
  }

  @Override
  public void injectMembers(ChartsFragment instance) {
    injectChartDataProvider(instance, chartDataProvider.get());
    injectPreferencesManager(instance, preferencesManagerProvider.get());
  }

  @InjectedFieldSignature("com.inventario.mobile.presentation.statistics.ChartsFragment.chartDataProvider")
  public static void injectChartDataProvider(ChartsFragment instance,
      ChartDataProvider chartDataProvider) {
    instance.chartDataProvider = chartDataProvider;
  }

  @InjectedFieldSignature("com.inventario.mobile.presentation.statistics.ChartsFragment.preferencesManager")
  public static void injectPreferencesManager(ChartsFragment instance,
      PreferencesManager preferencesManager) {
    instance.preferencesManager = preferencesManager;
  }
}
