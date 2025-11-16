package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.mapper.PatrimonioConsultaMapper;
import com.inventario.mobile.data.mapper.PatrimonioDetalheMapper;
import com.inventario.mobile.data.remote.api.PatrimonioConsultaApi;
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
public final class PatrimonioConsultaRepositoryImpl_Factory implements Factory<PatrimonioConsultaRepositoryImpl> {
  private final Provider<PatrimonioConsultaApi> apiProvider;

  private final Provider<PatrimonioConsultaMapper> consultaMapperProvider;

  private final Provider<PatrimonioDetalheMapper> detalheMapperProvider;

  public PatrimonioConsultaRepositoryImpl_Factory(Provider<PatrimonioConsultaApi> apiProvider,
      Provider<PatrimonioConsultaMapper> consultaMapperProvider,
      Provider<PatrimonioDetalheMapper> detalheMapperProvider) {
    this.apiProvider = apiProvider;
    this.consultaMapperProvider = consultaMapperProvider;
    this.detalheMapperProvider = detalheMapperProvider;
  }

  @Override
  public PatrimonioConsultaRepositoryImpl get() {
    return newInstance(apiProvider.get(), consultaMapperProvider.get(), detalheMapperProvider.get());
  }

  public static PatrimonioConsultaRepositoryImpl_Factory create(
      Provider<PatrimonioConsultaApi> apiProvider,
      Provider<PatrimonioConsultaMapper> consultaMapperProvider,
      Provider<PatrimonioDetalheMapper> detalheMapperProvider) {
    return new PatrimonioConsultaRepositoryImpl_Factory(apiProvider, consultaMapperProvider, detalheMapperProvider);
  }

  public static PatrimonioConsultaRepositoryImpl newInstance(PatrimonioConsultaApi api,
      PatrimonioConsultaMapper consultaMapper, PatrimonioDetalheMapper detalheMapper) {
    return new PatrimonioConsultaRepositoryImpl(api, consultaMapper, detalheMapper);
  }
}
