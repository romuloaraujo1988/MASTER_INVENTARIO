package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.ColetaMapper;
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
public final class MapperModule_ProvideColetaMapperFactory implements Factory<ColetaMapper> {
  @Override
  public ColetaMapper get() {
    return provideColetaMapper();
  }

  public static MapperModule_ProvideColetaMapperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ColetaMapper provideColetaMapper() {
    return Preconditions.checkNotNullFromProvides(MapperModule.INSTANCE.provideColetaMapper());
  }

  private static final class InstanceHolder {
    private static final MapperModule_ProvideColetaMapperFactory INSTANCE = new MapperModule_ProvideColetaMapperFactory();
  }
}
