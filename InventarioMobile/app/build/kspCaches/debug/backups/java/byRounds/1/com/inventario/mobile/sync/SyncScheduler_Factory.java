package com.inventario.mobile.sync;

import android.content.Context;
import com.inventario.mobile.utils.PreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SyncScheduler_Factory implements Factory<SyncScheduler> {
  private final Provider<Context> contextProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public SyncScheduler_Factory(Provider<Context> contextProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.contextProvider = contextProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public SyncScheduler get() {
    return newInstance(contextProvider.get(), preferencesManagerProvider.get());
  }

  public static SyncScheduler_Factory create(Provider<Context> contextProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new SyncScheduler_Factory(contextProvider, preferencesManagerProvider);
  }

  public static SyncScheduler newInstance(Context context, PreferencesManager preferencesManager) {
    return new SyncScheduler(context, preferencesManager);
  }
}
