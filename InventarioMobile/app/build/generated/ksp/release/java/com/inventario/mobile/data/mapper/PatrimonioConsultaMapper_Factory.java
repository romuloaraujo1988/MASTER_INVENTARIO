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
public final class PatrimonioConsultaMapper_Factory implements Factory<PatrimonioConsultaMapper> {
  @Override
  public PatrimonioConsultaMapper get() {
    return newInstance();
  }

  public static PatrimonioConsultaMapper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PatrimonioConsultaMapper newInstance() {
    return new PatrimonioConsultaMapper();
  }

  private static final class InstanceHolder {
    private static final PatrimonioConsultaMapper_Factory INSTANCE = new PatrimonioConsultaMapper_Factory();
  }
}
