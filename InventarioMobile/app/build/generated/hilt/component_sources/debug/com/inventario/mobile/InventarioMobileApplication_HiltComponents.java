package com.inventario.mobile;

import androidx.hilt.work.HiltWrapper_WorkerFactoryModule;
import com.inventario.mobile.di.ApiModule;
import com.inventario.mobile.di.AppModule;
import com.inventario.mobile.di.DashboardModule;
import com.inventario.mobile.di.DatabaseModule;
import com.inventario.mobile.di.MapperModule;
import com.inventario.mobile.di.NotificationModule;
import com.inventario.mobile.di.RepositoryModule;
import com.inventario.mobile.di.SyncModule;
import com.inventario.mobile.di.UseCaseModule;
import com.inventario.mobile.di.UtilModule;
import com.inventario.mobile.presentation.charts.ChartsFragment_GeneratedInjector;
import com.inventario.mobile.presentation.charts.ChartsViewModel_HiltModules;
import com.inventario.mobile.presentation.coleta.ColetaViewModelClean_HiltModules;
import com.inventario.mobile.presentation.coleta.CollectionViewActivity_GeneratedInjector;
import com.inventario.mobile.presentation.coleta.CollectionViewViewModelClean_HiltModules;
import com.inventario.mobile.presentation.coleta.ItemSemEtiquetaActivity_GeneratedInjector;
import com.inventario.mobile.presentation.coleta.ItemSemEtiquetaViewModel_HiltModules;
import com.inventario.mobile.presentation.coleta.ManualCollectionActivity_GeneratedInjector;
import com.inventario.mobile.presentation.coleta.ManualCollectionViewModel_HiltModules;
import com.inventario.mobile.presentation.coletas.ColetasActivityClean_GeneratedInjector;
import com.inventario.mobile.presentation.coletas.ColetasViewModelClean_HiltModules;
import com.inventario.mobile.presentation.dashboard.DashboardFragment_GeneratedInjector;
import com.inventario.mobile.presentation.dashboard.DashboardViewModelClean_HiltModules;
import com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity_GeneratedInjector;
import com.inventario.mobile.presentation.descricao.DescricaoSelectionViewModelClean_HiltModules;
import com.inventario.mobile.presentation.main.MainActivity_GeneratedInjector;
import com.inventario.mobile.presentation.sala.SalaSelectionActivity_GeneratedInjector;
import com.inventario.mobile.presentation.scanner.ScannerActivity_GeneratedInjector;
import com.inventario.mobile.presentation.statistics.ExportFragment_GeneratedInjector;
import com.inventario.mobile.presentation.statistics.OverviewFragment_GeneratedInjector;
import com.inventario.mobile.presentation.statistics.RankingsFragment_GeneratedInjector;
import com.inventario.mobile.presentation.statistics.StatisticsActivity_GeneratedInjector;
import com.inventario.mobile.presentation.sync.SyncActivity_GeneratedInjector;
import com.inventario.mobile.presentation.sync.SyncViewModel_HiltModules;
import com.inventario.mobile.presentation.validation.ValidationExampleFragment_GeneratedInjector;
import com.inventario.mobile.presentation.validation.ValidationViewModel_HiltModules;
import com.inventario.mobile.ui.base.BaseActivity_GeneratedInjector;
import com.inventario.mobile.ui.base.BaseOfflineActivity_GeneratedInjector;
import com.inventario.mobile.ui.coleta.ColetaActivity_GeneratedInjector;
import com.inventario.mobile.ui.splash.SplashActivity_GeneratedInjector;
import com.inventario.mobile.worker.BackupWorker_HiltModule;
import com.inventario.mobile.worker.ColetaSyncWorker_HiltModule;
import com.inventario.mobile.worker.DatabaseBackupWorker_HiltModule;
import com.inventario.mobile.worker.SyncWorker_HiltModule;
import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.android.components.ViewComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.components.ViewWithFragmentComponent;
import dagger.hilt.android.flags.FragmentGetContextFix;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_ActivityModule;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelModule;
import dagger.hilt.android.internal.managers.ActivityComponentManager;
import dagger.hilt.android.internal.managers.FragmentComponentManager;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_LifecycleModule;
import dagger.hilt.android.internal.managers.ServiceComponentManager;
import dagger.hilt.android.internal.managers.ViewComponentManager;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.HiltWrapper_ActivityModule;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.android.scopes.ActivityScoped;
import dagger.hilt.android.scopes.FragmentScoped;
import dagger.hilt.android.scopes.ServiceScoped;
import dagger.hilt.android.scopes.ViewModelScoped;
import dagger.hilt.android.scopes.ViewScoped;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedComponent;
import dagger.hilt.migration.DisableInstallInCheck;
import javax.inject.Singleton;

public final class InventarioMobileApplication_HiltComponents {
  private InventarioMobileApplication_HiltComponents() {
  }

  @Module(
      subcomponents = ServiceC.class
  )
  @DisableInstallInCheck
  abstract interface ServiceCBuilderModule {
    @Binds
    ServiceComponentBuilder bind(ServiceC.Builder builder);
  }

  @Module(
      subcomponents = ActivityRetainedC.class
  )
  @DisableInstallInCheck
  abstract interface ActivityRetainedCBuilderModule {
    @Binds
    ActivityRetainedComponentBuilder bind(ActivityRetainedC.Builder builder);
  }

  @Module(
      subcomponents = ActivityC.class
  )
  @DisableInstallInCheck
  abstract interface ActivityCBuilderModule {
    @Binds
    ActivityComponentBuilder bind(ActivityC.Builder builder);
  }

  @Module(
      subcomponents = ViewModelC.class
  )
  @DisableInstallInCheck
  abstract interface ViewModelCBuilderModule {
    @Binds
    ViewModelComponentBuilder bind(ViewModelC.Builder builder);
  }

  @Module(
      subcomponents = ViewC.class
  )
  @DisableInstallInCheck
  abstract interface ViewCBuilderModule {
    @Binds
    ViewComponentBuilder bind(ViewC.Builder builder);
  }

  @Module(
      subcomponents = FragmentC.class
  )
  @DisableInstallInCheck
  abstract interface FragmentCBuilderModule {
    @Binds
    FragmentComponentBuilder bind(FragmentC.Builder builder);
  }

  @Module(
      subcomponents = ViewWithFragmentC.class
  )
  @DisableInstallInCheck
  abstract interface ViewWithFragmentCBuilderModule {
    @Binds
    ViewWithFragmentComponentBuilder bind(ViewWithFragmentC.Builder builder);
  }

  @Component(
      modules = {
          ApiModule.class,
          AppModule.class,
          ApplicationContextModule.class,
          BackupWorker_HiltModule.class,
          ColetaSyncWorker_HiltModule.class,
          DashboardModule.class,
          DatabaseBackupWorker_HiltModule.class,
          DatabaseModule.class,
          HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule.class,
          HiltWrapper_WorkerFactoryModule.class,
          ActivityRetainedCBuilderModule.class,
          ServiceCBuilderModule.class,
          MapperModule.class,
          NotificationModule.class,
          RepositoryModule.class,
          SyncModule.class,
          SyncWorker_HiltModule.class,
          UtilModule.class
      }
  )
  @Singleton
  public abstract static class SingletonC implements InventarioMobileApplication_GeneratedInjector,
      FragmentGetContextFix.FragmentGetContextFixEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint,
      ServiceComponentManager.ServiceComponentBuilderEntryPoint,
      SingletonComponent,
      GeneratedComponent {
  }

  @Subcomponent
  @ServiceScoped
  public abstract static class ServiceC implements ServiceComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ServiceComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          ChartsViewModel_HiltModules.KeyModule.class,
          ColetaViewModelClean_HiltModules.KeyModule.class,
          ColetasViewModelClean_HiltModules.KeyModule.class,
          CollectionViewViewModelClean_HiltModules.KeyModule.class,
          DashboardViewModelClean_HiltModules.KeyModule.class,
          DescricaoSelectionViewModelClean_HiltModules.KeyModule.class,
          HiltWrapper_ActivityRetainedComponentManager_LifecycleModule.class,
          ActivityCBuilderModule.class,
          ViewModelCBuilderModule.class,
          ItemSemEtiquetaViewModel_HiltModules.KeyModule.class,
          ManualCollectionViewModel_HiltModules.KeyModule.class,
          SyncViewModel_HiltModules.KeyModule.class,
          ValidationViewModel_HiltModules.KeyModule.class
      }
  )
  @ActivityRetainedScoped
  public abstract static class ActivityRetainedC implements ActivityRetainedComponent,
      ActivityComponentManager.ActivityComponentBuilderEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityRetainedComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          HiltWrapper_ActivityModule.class,
          HiltWrapper_DefaultViewModelFactories_ActivityModule.class,
          FragmentCBuilderModule.class,
          ViewCBuilderModule.class,
          UseCaseModule.class
      }
  )
  @ActivityScoped
  public abstract static class ActivityC implements CollectionViewActivity_GeneratedInjector,
      ItemSemEtiquetaActivity_GeneratedInjector,
      ManualCollectionActivity_GeneratedInjector,
      ColetasActivityClean_GeneratedInjector,
      DescricaoSelectionActivity_GeneratedInjector,
      MainActivity_GeneratedInjector,
      SalaSelectionActivity_GeneratedInjector,
      ScannerActivity_GeneratedInjector,
      StatisticsActivity_GeneratedInjector,
      SyncActivity_GeneratedInjector,
      BaseActivity_GeneratedInjector,
      BaseOfflineActivity_GeneratedInjector,
      ColetaActivity_GeneratedInjector,
      SplashActivity_GeneratedInjector,
      ActivityComponent,
      DefaultViewModelFactories.ActivityEntryPoint,
      HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint,
      FragmentComponentManager.FragmentComponentBuilderEntryPoint,
      ViewComponentManager.ViewComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          ChartsViewModel_HiltModules.BindsModule.class,
          ColetaViewModelClean_HiltModules.BindsModule.class,
          ColetasViewModelClean_HiltModules.BindsModule.class,
          CollectionViewViewModelClean_HiltModules.BindsModule.class,
          DashboardViewModelClean_HiltModules.BindsModule.class,
          DescricaoSelectionViewModelClean_HiltModules.BindsModule.class,
          HiltWrapper_HiltViewModelFactory_ViewModelModule.class,
          ItemSemEtiquetaViewModel_HiltModules.BindsModule.class,
          ManualCollectionViewModel_HiltModules.BindsModule.class,
          SyncViewModel_HiltModules.BindsModule.class,
          ValidationViewModel_HiltModules.BindsModule.class
      }
  )
  @ViewModelScoped
  public abstract static class ViewModelC implements ViewModelComponent,
      HiltViewModelFactory.ViewModelFactoriesEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewModelComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewC implements ViewComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewComponentBuilder {
    }
  }

  @Subcomponent(
      modules = ViewWithFragmentCBuilderModule.class
  )
  @FragmentScoped
  public abstract static class FragmentC implements ChartsFragment_GeneratedInjector,
      DashboardFragment_GeneratedInjector,
      com.inventario.mobile.presentation.statistics.ChartsFragment_GeneratedInjector,
      ExportFragment_GeneratedInjector,
      OverviewFragment_GeneratedInjector,
      RankingsFragment_GeneratedInjector,
      ValidationExampleFragment_GeneratedInjector,
      FragmentComponent,
      DefaultViewModelFactories.FragmentEntryPoint,
      ViewComponentManager.ViewWithFragmentComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends FragmentComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewWithFragmentC implements ViewWithFragmentComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewWithFragmentComponentBuilder {
    }
  }
}
