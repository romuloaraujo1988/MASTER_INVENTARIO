package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.DashboardMapper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DashboardModule_ProvideDashboardMapperFactory implements Factory<DashboardMapper> {
  @Override
  public DashboardMapper get() {
    return provideDashboardMapper();
  }

  public static DashboardModule_ProvideDashboardMapperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DashboardMapper provideDashboardMapper() {
    return Preconditions.checkNotNullFromProvides(DashboardModule.INSTANCE.provideDashboardMapper());
  }

  private static final class InstanceHolder {
    private static final DashboardModule_ProvideDashboardMapperFactory INSTANCE = new DashboardModule_ProvideDashboardMapperFactory();
  }
}
