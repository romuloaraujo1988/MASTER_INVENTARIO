package com.inventario.mobile.presentation.coleta;

import android.content.Context;
import androidx.activity.contextaware.OnContextAvailableListener;
import com.inventario.mobile.ui.base.BaseOfflineActivity;
import dagger.hilt.internal.GeneratedComponentManagerHolder;
import dagger.hilt.internal.UnsafeCasts;
import java.lang.Override;
import javax.annotation.processing.Generated;

/**
 * A generated base class to be extended by the @dagger.hilt.android.AndroidEntryPoint annotated class. If using the Gradle plugin, this is swapped as the base class via bytecode transformation.
 */
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.ActivityGenerator")
public abstract class Hilt_ManualCollectionActivity extends BaseOfflineActivity {
  private boolean injected = false;

  Hilt_ManualCollectionActivity() {
    super();
    _initHiltInternal();
  }

  private void _initHiltInternal() {
    addOnContextAvailableListener(new OnContextAvailableListener() {
      @Override
      public void onContextAvailable(Context context) {
        inject();
      }
    });
  }

  protected void inject() {
    if (!injected) {
      injected = true;
      ((ManualCollectionActivity_GeneratedInjector) UnsafeCasts.<GeneratedComponentManagerHolder>unsafeCast(this).generatedComponent()).injectManualCollectionActivity(UnsafeCasts.<ManualCollectionActivity>unsafeCast(this));
    }
  }
}
