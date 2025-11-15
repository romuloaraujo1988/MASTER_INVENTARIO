package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.api.InventarioApi;
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
public final class BuscarInventarioAtivoUseCase_Factory implements Factory<BuscarInventarioAtivoUseCase> {
  private final Provider<InventarioApi> inventarioApiProvider;

  public BuscarInventarioAtivoUseCase_Factory(Provider<InventarioApi> inventarioApiProvider) {
    this.inventarioApiProvider = inventarioApiProvider;
  }

  @Override
  public BuscarInventarioAtivoUseCase get() {
    return newInstance(inventarioApiProvider.get());
  }

  public static BuscarInventarioAtivoUseCase_Factory create(
      Provider<InventarioApi> inventarioApiProvider) {
    return new BuscarInventarioAtivoUseCase_Factory(inventarioApiProvider);
  }

  public static BuscarInventarioAtivoUseCase newInstance(InventarioApi inventarioApi) {
    return new BuscarInventarioAtivoUseCase(inventarioApi);
  }
}
