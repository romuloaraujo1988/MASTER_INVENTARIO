package com.inventario.mobile.presentation.consulta;

import com.inventario.mobile.domain.usecase.BuscarPatrimonioAvancadaUseCase;
import com.inventario.mobile.domain.usecase.BuscarPatrimonioPorCodigoUseCase;
import com.inventario.mobile.domain.usecase.BuscarPatrimonioPorDescricaoUseCase;
import com.inventario.mobile.domain.usecase.ObterDetalhePatrimonioUseCase;
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
public final class ConsultaPatrimonioViewModel_Factory implements Factory<ConsultaPatrimonioViewModel> {
  private final Provider<BuscarPatrimonioPorCodigoUseCase> buscarPorCodigoUseCaseProvider;

  private final Provider<BuscarPatrimonioPorDescricaoUseCase> buscarPorDescricaoUseCaseProvider;

  private final Provider<BuscarPatrimonioAvancadaUseCase> buscarAvancadaUseCaseProvider;

  private final Provider<ObterDetalhePatrimonioUseCase> obterDetalheUseCaseProvider;

  public ConsultaPatrimonioViewModel_Factory(
      Provider<BuscarPatrimonioPorCodigoUseCase> buscarPorCodigoUseCaseProvider,
      Provider<BuscarPatrimonioPorDescricaoUseCase> buscarPorDescricaoUseCaseProvider,
      Provider<BuscarPatrimonioAvancadaUseCase> buscarAvancadaUseCaseProvider,
      Provider<ObterDetalhePatrimonioUseCase> obterDetalheUseCaseProvider) {
    this.buscarPorCodigoUseCaseProvider = buscarPorCodigoUseCaseProvider;
    this.buscarPorDescricaoUseCaseProvider = buscarPorDescricaoUseCaseProvider;
    this.buscarAvancadaUseCaseProvider = buscarAvancadaUseCaseProvider;
    this.obterDetalheUseCaseProvider = obterDetalheUseCaseProvider;
  }

  @Override
  public ConsultaPatrimonioViewModel get() {
    return newInstance(buscarPorCodigoUseCaseProvider.get(), buscarPorDescricaoUseCaseProvider.get(), buscarAvancadaUseCaseProvider.get(), obterDetalheUseCaseProvider.get());
  }

  public static ConsultaPatrimonioViewModel_Factory create(
      Provider<BuscarPatrimonioPorCodigoUseCase> buscarPorCodigoUseCaseProvider,
      Provider<BuscarPatrimonioPorDescricaoUseCase> buscarPorDescricaoUseCaseProvider,
      Provider<BuscarPatrimonioAvancadaUseCase> buscarAvancadaUseCaseProvider,
      Provider<ObterDetalhePatrimonioUseCase> obterDetalheUseCaseProvider) {
    return new ConsultaPatrimonioViewModel_Factory(buscarPorCodigoUseCaseProvider, buscarPorDescricaoUseCaseProvider, buscarAvancadaUseCaseProvider, obterDetalheUseCaseProvider);
  }

  public static ConsultaPatrimonioViewModel newInstance(
      BuscarPatrimonioPorCodigoUseCase buscarPorCodigoUseCase,
      BuscarPatrimonioPorDescricaoUseCase buscarPorDescricaoUseCase,
      BuscarPatrimonioAvancadaUseCase buscarAvancadaUseCase,
      ObterDetalhePatrimonioUseCase obterDetalheUseCase) {
    return new ConsultaPatrimonioViewModel(buscarPorCodigoUseCase, buscarPorDescricaoUseCase, buscarAvancadaUseCase, obterDetalheUseCase);
  }
}
