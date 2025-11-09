package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.SalaMapper;
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
public final class MapperModule_ProvideSalaMapperFactory implements Factory<SalaMapper> {
  @Override
  public SalaMapper get() {
    return provideSalaMapper();
  }

  public static MapperModule_ProvideSalaMapperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SalaMapper provideSalaMapper() {
    return Preconditions.checkNotNullFromProvides(MapperModule.INSTANCE.provideSalaMapper());
  }

  private static final class InstanceHolder {
    private static final MapperModule_ProvideSalaMapperFactory INSTANCE = new MapperModule_ProvideSalaMapperFactory();
  }
}
