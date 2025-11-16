package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.mapper.ColetaMapper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  public MapperModule_ProvideColetaMapperFactory(Provider<PatrimonioDao> patrimonioDaoProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
  }

  @Override
  public ColetaMapper get() {
    return provideColetaMapper(patrimonioDaoProvider.get());
  }

  public static MapperModule_ProvideColetaMapperFactory create(
      Provider<PatrimonioDao> patrimonioDaoProvider) {
    return new MapperModule_ProvideColetaMapperFactory(patrimonioDaoProvider);
  }

  public static ColetaMapper provideColetaMapper(PatrimonioDao patrimonioDao) {
    return Preconditions.checkNotNullFromProvides(MapperModule.INSTANCE.provideColetaMapper(patrimonioDao));
  }
}
