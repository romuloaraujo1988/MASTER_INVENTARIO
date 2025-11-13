package com.inventario.mobile.sync;

import com.inventario.mobile.data.local.dao.SyncLogDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SyncLogger_Factory implements Factory<SyncLogger> {
  private final Provider<SyncLogDao> syncLogDaoProvider;

  public SyncLogger_Factory(Provider<SyncLogDao> syncLogDaoProvider) {
    this.syncLogDaoProvider = syncLogDaoProvider;
  }

  @Override
  public SyncLogger get() {
    return newInstance(syncLogDaoProvider.get());
  }

  public static SyncLogger_Factory create(Provider<SyncLogDao> syncLogDaoProvider) {
    return new SyncLogger_Factory(syncLogDaoProvider);
  }

  public static SyncLogger newInstance(SyncLogDao syncLogDao) {
    return new SyncLogger(syncLogDao);
  }
}
