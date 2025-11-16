package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.PatrimonioDao;
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
public final class DatabaseModule_ProvidePatrimonioDaoFactory implements Factory<PatrimonioDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvidePatrimonioDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PatrimonioDao get() {
    return providePatrimonioDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvidePatrimonioDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvidePatrimonioDaoFactory(databaseProvider);
  }

  public static PatrimonioDao providePatrimonioDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePatrimonioDao(database));
  }
}
