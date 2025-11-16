package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.SyncLogDao;
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
public final class DatabaseModule_ProvideSyncLogDaoFactory implements Factory<SyncLogDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideSyncLogDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SyncLogDao get() {
    return provideSyncLogDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSyncLogDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSyncLogDaoFactory(databaseProvider);
  }

  public static SyncLogDao provideSyncLogDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSyncLogDao(database));
  }
}
