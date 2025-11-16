package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.PatrimonioDetalheMapper;
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
public final class PatrimonioConsultaModule_ProvidePatrimonioDetalheMapperFactory implements Factory<PatrimonioDetalheMapper> {
  @Override
  public PatrimonioDetalheMapper get() {
    return providePatrimonioDetalheMapper();
  }

  public static PatrimonioConsultaModule_ProvidePatrimonioDetalheMapperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PatrimonioDetalheMapper providePatrimonioDetalheMapper() {
    return Preconditions.checkNotNullFromProvides(PatrimonioConsultaModule.INSTANCE.providePatrimonioDetalheMapper());
  }

  private static final class InstanceHolder {
    private static final PatrimonioConsultaModule_ProvidePatrimonioDetalheMapperFactory INSTANCE = new PatrimonioConsultaModule_ProvidePatrimonioDetalheMapperFactory();
  }
}
