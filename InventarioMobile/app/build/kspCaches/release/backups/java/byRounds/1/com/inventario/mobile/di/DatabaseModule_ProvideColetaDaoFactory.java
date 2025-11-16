package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.database.AppDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideColetaDaoFactory implements Factory<ColetaDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideColetaDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ColetaDao get() {
    return provideColetaDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideColetaDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideColetaDaoFactory(databaseProvider);
  }

  public static ColetaDao provideColetaDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideColetaDao(database));
  }
}
