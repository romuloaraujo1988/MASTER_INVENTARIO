package com.inventario.mobile.data.mapper;

import com.inventario.mobile.data.local.dao.PatrimonioDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ColetaMapper_Factory implements Factory<ColetaMapper> {
  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  public ColetaMapper_Factory(Provider<PatrimonioDao> patrimonioDaoProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
  }

  @Override
  public ColetaMapper get() {
    return newInstance(patrimonioDaoProvider.get());
  }

  public static ColetaMapper_Factory create(Provider<PatrimonioDao> patrimonioDaoProvider) {
    return new ColetaMapper_Factory(patrimonioDaoProvider);
  }

  public static ColetaMapper newInstance(PatrimonioDao patrimonioDao) {
    return new ColetaMapper(patrimonioDao);
  }
}
