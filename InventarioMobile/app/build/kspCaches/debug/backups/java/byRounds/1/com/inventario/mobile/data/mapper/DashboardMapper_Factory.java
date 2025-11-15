package com.inventario.mobile.data.mapper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class DashboardMapper_Factory implements Factory<DashboardMapper> {
  @Override
  public DashboardMapper get() {
    return newInstance();
  }

  public static DashboardMapper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DashboardMapper newInstance() {
    return new DashboardMapper();
  }

  private static final class InstanceHolder {
    private static final DashboardMapper_Factory INSTANCE = new DashboardMapper_Factory();
  }
}
