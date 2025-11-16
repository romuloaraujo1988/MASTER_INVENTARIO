package com.inventario.mobile.presentation.dashboard;

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
public final class DashboardFragment_MembersInjector implements MembersInjector<DashboardFragment> {
  private final Provider<PreferencesManager> preferencesManagerProvider;

  public DashboardFragment_MembersInjector(
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  public static MembersInjector<DashboardFragment> create(
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new DashboardFragment_MembersInjector(preferencesManagerProvider);
  }

  @Override
  public void injectMembers(DashboardFragment instance) {
    injectPreferencesManager(instance, preferencesManagerProvider.get());
  }

  @InjectedFieldSignature("com.inventario.mobile.presentation.dashboard.DashboardFragment.preferencesManager")
  public static void injectPreferencesManager(DashboardFragment instance,
      PreferencesManager preferencesManager) {
    instance.preferencesManager = preferencesManager;
  }
}
