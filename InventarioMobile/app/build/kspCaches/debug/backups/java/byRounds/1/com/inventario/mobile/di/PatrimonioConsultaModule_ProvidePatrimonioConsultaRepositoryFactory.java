package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.PatrimonioConsultaMapper;
import com.inventario.mobile.data.mapper.PatrimonioDetalheMapper;
import com.inventario.mobile.data.remote.api.PatrimonioConsultaApi;
import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository;
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
public final class PatrimonioConsultaModule_ProvidePatrimonioConsultaRepositoryFactory implements Factory<PatrimonioConsultaRepository> {
  private final Provider<PatrimonioConsultaApi> apiProvider;

  private final Provider<PatrimonioConsultaMapper> consultaMapperProvider;

  private final Provider<PatrimonioDetalheMapper> detalheMapperProvider;

  public PatrimonioConsultaModule_ProvidePatrimonioConsultaRepositoryFactory(
      Provider<PatrimonioConsultaApi> apiProvider,
      Provider<PatrimonioConsultaMapper> consultaMapperProvider,
      Provider<PatrimonioDetalheMapper> detalheMapperProvider) {
    this.apiProvider = apiProvider;
    this.consultaMapperProvider = consultaMapperProvider;
    this.detalheMapperProvider = detalheMapperProvider;
  }

  @Override
  public PatrimonioConsultaRepository get() {
    return providePatrimonioConsultaRepository(apiProvider.get(), consultaMapperProvider.get(), detalheMapperProvider.get());
  }

  public static PatrimonioConsultaModule_ProvidePatrimonioConsultaRepositoryFactory create(
      Provider<PatrimonioConsultaApi> apiProvider,
      Provider<PatrimonioConsultaMapper> consultaMapperProvider,
      Provider<PatrimonioDetalheMapper> detalheMapperProvider) {
    return new PatrimonioConsultaModule_ProvidePatrimonioConsultaRepositoryFactory(apiProvider, consultaMapperProvider, detalheMapperProvider);
  }

  public static PatrimonioConsultaRepository providePatrimonioConsultaRepository(
      PatrimonioConsultaApi api, PatrimonioConsultaMapper consultaMapper,
      PatrimonioDetalheMapper detalheMapper) {
    return Preconditions.checkNotNullFromProvides(PatrimonioConsultaModule.INSTANCE.providePatrimonioConsultaRepository(api, consultaMapper, detalheMapper));
  }
}
