package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.SalaDao;
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
public final class DatabaseModule_ProvideSalaDaoFactory implements Factory<SalaDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideSalaDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SalaDao get() {
    return provideSalaDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSalaDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSalaDaoFactory(databaseProvider);
  }

  public static SalaDao provideSalaDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSalaDao(database));
  }
}
