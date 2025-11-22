package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.mapper.ColetaMapper;
import com.inventario.mobile.utils.PreferencesManager;
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

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public MapperModule_ProvideColetaMapperFactory(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public ColetaMapper get() {
    return provideColetaMapper(patrimonioDaoProvider.get(), preferencesManagerProvider.get());
  }

  public static MapperModule_ProvideColetaMapperFactory create(
      Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new MapperModule_ProvideColetaMapperFactory(patrimonioDaoProvider, preferencesManagerProvider);
  }

  public static ColetaMapper provideColetaMapper(PatrimonioDao patrimonioDao,
      PreferencesManager preferencesManager) {
    return Preconditions.checkNotNullFromProvides(MapperModule.INSTANCE.provideColetaMapper(patrimonioDao, preferencesManager));
  }
}
