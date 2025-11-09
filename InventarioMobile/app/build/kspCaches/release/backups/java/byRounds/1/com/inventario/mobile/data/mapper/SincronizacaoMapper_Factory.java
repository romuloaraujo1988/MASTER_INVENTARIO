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
public final class SincronizacaoMapper_Factory implements Factory<SincronizacaoMapper> {
  @Override
  public SincronizacaoMapper get() {
    return newInstance();
  }

  public static SincronizacaoMapper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SincronizacaoMapper newInstance() {
    return new SincronizacaoMapper();
  }

  private static final class InstanceHolder {
    private static final SincronizacaoMapper_Factory INSTANCE = new SincronizacaoMapper_Factory();
  }
}
