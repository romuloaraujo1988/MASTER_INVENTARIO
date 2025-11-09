package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.PatrimonioMapper;
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
public final class MapperModule_ProvidePatrimonioMapperFactory implements Factory<PatrimonioMapper> {
  @Override
  public PatrimonioMapper get() {
    return providePatrimonioMapper();
  }

  public static MapperModule_ProvidePatrimonioMapperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PatrimonioMapper providePatrimonioMapper() {
    return Preconditions.checkNotNullFromProvides(MapperModule.INSTANCE.providePatrimonioMapper());
  }

  private static final class InstanceHolder {
    private static final MapperModule_ProvidePatrimonioMapperFactory INSTANCE = new MapperModule_ProvidePatrimonioMapperFactory();
  }
}
