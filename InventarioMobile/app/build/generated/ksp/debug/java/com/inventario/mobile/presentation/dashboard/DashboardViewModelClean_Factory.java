package com.inventario.mobile.presentation.dashboard;

import com.inventario.mobile.data.repository.InventarioRepository;
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
public final class DashboardViewModelClean_Factory implements Factory<DashboardViewModelClean> {
  private final Provider<InventarioRepository> inventarioRepositoryProvider;

  public DashboardViewModelClean_Factory(
      Provider<InventarioRepository> inventarioRepositoryProvider) {
    this.inventarioRepositoryProvider = inventarioRepositoryProvider;
  }

  @Override
  public DashboardViewModelClean get() {
    return newInstance(inventarioRepositoryProvider.get());
  }

  public static DashboardViewModelClean_Factory create(
      Provider<InventarioRepository> inventarioRepositoryProvider) {
    return new DashboardViewModelClean_Factory(inventarioRepositoryProvider);
  }

  public static DashboardViewModelClean newInstance(InventarioRepository inventarioRepository) {
    return new DashboardViewModelClean(inventarioRepository);
  }
}
