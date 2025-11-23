package com.inventario.mobile.presentation.dashboard;

import com.inventario.mobile.network.ConnectionStateManager;
import com.inventario.mobile.ui.base.BaseOfflineFragment_MembersInjector;
import com.inventario.mobile.utils.NetworkMonitor;
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
  private final Provider<ConnectionStateManager> connectionStateManagerProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public DashboardFragment_MembersInjector(
      Provider<ConnectionStateManager> connectionStateManagerProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.connectionStateManagerProvider = connectionStateManagerProvider;
    this.networkMonitorProvider = networkMonitorProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  public static MembersInjector<DashboardFragment> create(
      Provider<ConnectionStateManager> connectionStateManagerProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new DashboardFragment_MembersInjector(connectionStateManagerProvider, networkMonitorProvider, preferencesManagerProvider);
  }

  @Override
  public void injectMembers(DashboardFragment instance) {
    BaseOfflineFragment_MembersInjector.injectConnectionStateManager(instance, connectionStateManagerProvider.get());
    BaseOfflineFragment_MembersInjector.injectNetworkMonitor(instance, networkMonitorProvider.get());
    injectPreferencesManager(instance, preferencesManagerProvider.get());
  }

  @InjectedFieldSignature("com.inventario.mobile.presentation.dashboard.DashboardFragment.preferencesManager")
  public static void injectPreferencesManager(DashboardFragment instance,
      PreferencesManager preferencesManager) {
    instance.preferencesManager = preferencesManager;
  }
}
