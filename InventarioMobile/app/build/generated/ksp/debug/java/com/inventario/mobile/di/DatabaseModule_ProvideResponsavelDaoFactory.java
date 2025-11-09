package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.ResponsavelDao;
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
public final class DatabaseModule_ProvideResponsavelDaoFactory implements Factory<ResponsavelDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideResponsavelDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ResponsavelDao get() {
    return provideResponsavelDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideResponsavelDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideResponsavelDaoFactory(databaseProvider);
  }

  public static ResponsavelDao provideResponsavelDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideResponsavelDao(database));
  }
}
