package com.inventario.mobile.presentation.statistics;

import com.inventario.mobile.data.local.database.AppDatabase;
import com.inventario.mobile.utils.PreferencesManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ChartsFragment_MembersInjector implements MembersInjector<ChartsFragment> {
  private final Provider<AppDatabase> databaseProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public ChartsFragment_MembersInjector(Provider<AppDatabase> databaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.databaseProvider = databaseProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  public static MembersInjector<ChartsFragment> create(Provider<AppDatabase> databaseProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new ChartsFragment_MembersInjector(databaseProvider, preferencesManagerProvider);
  }

  @Override
  public void injectMembers(ChartsFragment instance) {
    injectDatabase(instance, databaseProvider.get());
    injectPreferencesManager(instance, preferencesManagerProvider.get());
  }

  @InjectedFieldSignature("com.inventario.mobile.presentation.statistics.ChartsFragment.database")
  public static void injectDatabase(ChartsFragment instance, AppDatabase database) {
    instance.database = database;
  }

  @InjectedFieldSignature("com.inventario.mobile.presentation.statistics.ChartsFragment.preferencesManager")
  public static void injectPreferencesManager(ChartsFragment instance,
      PreferencesManager preferencesManager) {
    instance.preferencesManager = preferencesManager;
  }
}
