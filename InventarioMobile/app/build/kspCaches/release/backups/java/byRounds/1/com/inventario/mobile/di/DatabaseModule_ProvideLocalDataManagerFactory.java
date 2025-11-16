package com.inventario.mobile.di;

import android.content.Context;
import com.inventario.mobile.data.local.LocalDataManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideLocalDataManagerFactory implements Factory<LocalDataManager> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideLocalDataManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LocalDataManager get() {
    return provideLocalDataManager(contextProvider.get());
  }

  public static DatabaseModule_ProvideLocalDataManagerFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideLocalDataManagerFactory(contextProvider);
  }

  public static LocalDataManager provideLocalDataManager(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLocalDataManager(context));
  }
}
