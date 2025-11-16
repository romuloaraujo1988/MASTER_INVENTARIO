package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.PatrimonioConsultaMapper;
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
public final class PatrimonioConsultaModule_ProvidePatrimonioConsultaMapperFactory implements Factory<PatrimonioConsultaMapper> {
  @Override
  public PatrimonioConsultaMapper get() {
    return providePatrimonioConsultaMapper();
  }

  public static PatrimonioConsultaModule_ProvidePatrimonioConsultaMapperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PatrimonioConsultaMapper providePatrimonioConsultaMapper() {
    return Preconditions.checkNotNullFromProvides(PatrimonioConsultaModule.INSTANCE.providePatrimonioConsultaMapper());
  }

  private static final class InstanceHolder {
    private static final PatrimonioConsultaModule_ProvidePatrimonioConsultaMapperFactory INSTANCE = new PatrimonioConsultaModule_ProvidePatrimonioConsultaMapperFactory();
  }
}
