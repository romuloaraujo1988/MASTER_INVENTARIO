package com.inventario.mobile.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.ColetaDao_Impl;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao_Impl;
import com.inventario.mobile.data.local.dao.ResponsavelDao;
import com.inventario.mobile.data.local.dao.ResponsavelDao_Impl;
import com.inventario.mobile.data.local.dao.SalaDao;
import com.inventario.mobile.data.local.dao.SalaDao_Impl;
import com.inventario.mobile.data.local.dao.SincronizacaoDao;
import com.inventario.mobile.data.local.dao.SincronizacaoDao_Impl;
import com.inventario.mobile.data.local.dao.SyncLogDao;
import com.inventario.mobile.data.local.dao.SyncLogDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PatrimonioDao _patrimonioDao;

  private volatile SalaDao _salaDao;

  private volatile ResponsavelDao _responsavelDao;

  private volatile ColetaDao _coletaDao;

  private volatile SincronizacaoDao _sincronizacaoDao;

  private volatile SyncLogDao _syncLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `patrimonio` (`id` INTEGER NOT NULL, `numero` TEXT NOT NULL, `descricao` TEXT NOT NULL, `idSala` INTEGER, `nomeSala` TEXT, `idResponsavel` INTEGER, `nomeResponsavel` TEXT, `status` TEXT NOT NULL, `coletado` INTEGER NOT NULL, `dataUltimaAtualizacao` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_patrimonio_numero` ON `patrimonio` (`numero`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_descricao` ON `patrimonio` (`descricao`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_idSala` ON `patrimonio` (`idSala`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_coletado` ON `patrimonio` (`coletado`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sala` (`id` INTEGER NOT NULL, `nome` TEXT NOT NULL, `idSetor` INTEGER, `nomeSetor` TEXT, `dataUltimaAtualizacao` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sala_nome` ON `sala` (`nome`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `responsavel` (`id` INTEGER NOT NULL, `nome` TEXT NOT NULL, `cpf` TEXT, `email` TEXT, `dataUltimaAtualizacao` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_responsavel_nome` ON `responsavel` (`nome`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `coleta` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `idPatrimonio` INTEGER NOT NULL, `numeroPatrimonio` TEXT NOT NULL, `idInventario` INTEGER NOT NULL, `idSala` INTEGER, `nomeSala` TEXT, `idResponsavel` INTEGER, `nomeResponsavel` TEXT, `observacao` TEXT, `estadoPatrimonio` TEXT, `latitude` REAL, `longitude` REAL, `dataColeta` INTEGER NOT NULL, `idUsuario` INTEGER NOT NULL, `nomeUsuario` TEXT NOT NULL, `sincronizado` INTEGER NOT NULL, `tentativasSincronizacao` INTEGER NOT NULL, `erroSincronizacao` TEXT, `servidorId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_idPatrimonio` ON `coleta` (`idPatrimonio`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_idInventario` ON `coleta` (`idInventario`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_sincronizado` ON `coleta` (`sincronizado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_dataColeta` ON `coleta` (`dataColeta`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sincronizacao` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entidade` TEXT NOT NULL, `entidadeId` INTEGER NOT NULL, `operacao` TEXT NOT NULL, `sincronizado` INTEGER NOT NULL, `dataHora` INTEGER NOT NULL, `erro` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tipo` TEXT NOT NULL, `dataHora` INTEGER NOT NULL, `status` TEXT NOT NULL, `mensagem` TEXT NOT NULL, `coletasSincronizadas` INTEGER NOT NULL, `coletasFalhadas` INTEGER NOT NULL, `stackTrace` TEXT, `duracao` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8ce333907d7e0ce52ba2f9ac5aeed079')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `patrimonio`");
        db.execSQL("DROP TABLE IF EXISTS `sala`");
        db.execSQL("DROP TABLE IF EXISTS `responsavel`");
        db.execSQL("DROP TABLE IF EXISTS `coleta`");
        db.execSQL("DROP TABLE IF EXISTS `sincronizacao`");
        db.execSQL("DROP TABLE IF EXISTS `sync_log`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsPatrimonio = new HashMap<String, TableInfo.Column>(10);
        _columnsPatrimonio.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("numero", new TableInfo.Column("numero", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("descricao", new TableInfo.Column("descricao", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("idSala", new TableInfo.Column("idSala", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("nomeSala", new TableInfo.Column("nomeSala", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("idResponsavel", new TableInfo.Column("idResponsavel", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("nomeResponsavel", new TableInfo.Column("nomeResponsavel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("coletado", new TableInfo.Column("coletado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("dataUltimaAtualizacao", new TableInfo.Column("dataUltimaAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPatrimonio = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPatrimonio = new HashSet<TableInfo.Index>(4);
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_numero", true, Arrays.asList("numero"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_descricao", false, Arrays.asList("descricao"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_idSala", false, Arrays.asList("idSala"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_coletado", false, Arrays.asList("coletado"), Arrays.asList("ASC")));
        final TableInfo _infoPatrimonio = new TableInfo("patrimonio", _columnsPatrimonio, _foreignKeysPatrimonio, _indicesPatrimonio);
        final TableInfo _existingPatrimonio = TableInfo.read(db, "patrimonio");
        if (!_infoPatrimonio.equals(_existingPatrimonio)) {
          return new RoomOpenHelper.ValidationResult(false, "patrimonio(com.inventario.mobile.data.local.entity.PatrimonioEntity).\n"
                  + " Expected:\n" + _infoPatrimonio + "\n"
                  + " Found:\n" + _existingPatrimonio);
        }
        final HashMap<String, TableInfo.Column> _columnsSala = new HashMap<String, TableInfo.Column>(5);
        _columnsSala.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("nome", new TableInfo.Column("nome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("idSetor", new TableInfo.Column("idSetor", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("nomeSetor", new TableInfo.Column("nomeSetor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("dataUltimaAtualizacao", new TableInfo.Column("dataUltimaAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSala = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSala = new HashSet<TableInfo.Index>(1);
        _indicesSala.add(new TableInfo.Index("index_sala_nome", false, Arrays.asList("nome"), Arrays.asList("ASC")));
        final TableInfo _infoSala = new TableInfo("sala", _columnsSala, _foreignKeysSala, _indicesSala);
        final TableInfo _existingSala = TableInfo.read(db, "sala");
        if (!_infoSala.equals(_existingSala)) {
          return new RoomOpenHelper.ValidationResult(false, "sala(com.inventario.mobile.data.local.entity.SalaEntity).\n"
                  + " Expected:\n" + _infoSala + "\n"
                  + " Found:\n" + _existingSala);
        }
        final HashMap<String, TableInfo.Column> _columnsResponsavel = new HashMap<String, TableInfo.Column>(5);
        _columnsResponsavel.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResponsavel.put("nome", new TableInfo.Column("nome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResponsavel.put("cpf", new TableInfo.Column("cpf", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResponsavel.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResponsavel.put("dataUltimaAtualizacao", new TableInfo.Column("dataUltimaAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysResponsavel = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesResponsavel = new HashSet<TableInfo.Index>(1);
        _indicesResponsavel.add(new TableInfo.Index("index_responsavel_nome", false, Arrays.asList("nome"), Arrays.asList("ASC")));
        final TableInfo _infoResponsavel = new TableInfo("responsavel", _columnsResponsavel, _foreignKeysResponsavel, _indicesResponsavel);
        final TableInfo _existingResponsavel = TableInfo.read(db, "responsavel");
        if (!_infoResponsavel.equals(_existingResponsavel)) {
          return new RoomOpenHelper.ValidationResult(false, "responsavel(com.inventario.mobile.data.local.entity.ResponsavelEntity).\n"
                  + " Expected:\n" + _infoResponsavel + "\n"
                  + " Found:\n" + _existingResponsavel);
        }
        final HashMap<String, TableInfo.Column> _columnsColeta = new HashMap<String, TableInfo.Column>(19);
        _columnsColeta.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("idPatrimonio", new TableInfo.Column("idPatrimonio", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("numeroPatrimonio", new TableInfo.Column("numeroPatrimonio", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("idInventario", new TableInfo.Column("idInventario", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("idSala", new TableInfo.Column("idSala", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("nomeSala", new TableInfo.Column("nomeSala", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("idResponsavel", new TableInfo.Column("idResponsavel", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("nomeResponsavel", new TableInfo.Column("nomeResponsavel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("observacao", new TableInfo.Column("observacao", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("estadoPatrimonio", new TableInfo.Column("estadoPatrimonio", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("dataColeta", new TableInfo.Column("dataColeta", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("idUsuario", new TableInfo.Column("idUsuario", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("nomeUsuario", new TableInfo.Column("nomeUsuario", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("sincronizado", new TableInfo.Column("sincronizado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("tentativasSincronizacao", new TableInfo.Column("tentativasSincronizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("erroSincronizacao", new TableInfo.Column("erroSincronizacao", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("servidorId", new TableInfo.Column("servidorId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysColeta = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesColeta = new HashSet<TableInfo.Index>(4);
        _indicesColeta.add(new TableInfo.Index("index_coleta_idPatrimonio", false, Arrays.asList("idPatrimonio"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_idInventario", false, Arrays.asList("idInventario"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_sincronizado", false, Arrays.asList("sincronizado"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_dataColeta", false, Arrays.asList("dataColeta"), Arrays.asList("ASC")));
        final TableInfo _infoColeta = new TableInfo("coleta", _columnsColeta, _foreignKeysColeta, _indicesColeta);
        final TableInfo _existingColeta = TableInfo.read(db, "coleta");
        if (!_infoColeta.equals(_existingColeta)) {
          return new RoomOpenHelper.ValidationResult(false, "coleta(com.inventario.mobile.data.local.entity.ColetaEntity).\n"
                  + " Expected:\n" + _infoColeta + "\n"
                  + " Found:\n" + _existingColeta);
        }
        final HashMap<String, TableInfo.Column> _columnsSincronizacao = new HashMap<String, TableInfo.Column>(7);
        _columnsSincronizacao.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSincronizacao.put("entidade", new TableInfo.Column("entidade", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSincronizacao.put("entidadeId", new TableInfo.Column("entidadeId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSincronizacao.put("operacao", new TableInfo.Column("operacao", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSincronizacao.put("sincronizado", new TableInfo.Column("sincronizado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSincronizacao.put("dataHora", new TableInfo.Column("dataHora", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSincronizacao.put("erro", new TableInfo.Column("erro", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSincronizacao = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSincronizacao = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSincronizacao = new TableInfo("sincronizacao", _columnsSincronizacao, _foreignKeysSincronizacao, _indicesSincronizacao);
        final TableInfo _existingSincronizacao = TableInfo.read(db, "sincronizacao");
        if (!_infoSincronizacao.equals(_existingSincronizacao)) {
          return new RoomOpenHelper.ValidationResult(false, "sincronizacao(com.inventario.mobile.data.local.entity.SincronizacaoEntity).\n"
                  + " Expected:\n" + _infoSincronizacao + "\n"
                  + " Found:\n" + _existingSincronizacao);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncLog = new HashMap<String, TableInfo.Column>(9);
        _columnsSyncLog.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("tipo", new TableInfo.Column("tipo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("dataHora", new TableInfo.Column("dataHora", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("mensagem", new TableInfo.Column("mensagem", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("coletasSincronizadas", new TableInfo.Column("coletasSincronizadas", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("coletasFalhadas", new TableInfo.Column("coletasFalhadas", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("stackTrace", new TableInfo.Column("stackTrace", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("duracao", new TableInfo.Column("duracao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncLog = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncLog = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncLog = new TableInfo("sync_log", _columnsSyncLog, _foreignKeysSyncLog, _indicesSyncLog);
        final TableInfo _existingSyncLog = TableInfo.read(db, "sync_log");
        if (!_infoSyncLog.equals(_existingSyncLog)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_log(com.inventario.mobile.data.local.entity.SyncLogEntity).\n"
                  + " Expected:\n" + _infoSyncLog + "\n"
                  + " Found:\n" + _existingSyncLog);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "8ce333907d7e0ce52ba2f9ac5aeed079", "b4c27db6379206145d45be4104b83261");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "patrimonio","sala","responsavel","coleta","sincronizacao","sync_log");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `patrimonio`");
      _db.execSQL("DELETE FROM `sala`");
      _db.execSQL("DELETE FROM `responsavel`");
      _db.execSQL("DELETE FROM `coleta`");
      _db.execSQL("DELETE FROM `sincronizacao`");
      _db.execSQL("DELETE FROM `sync_log`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PatrimonioDao.class, PatrimonioDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SalaDao.class, SalaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ResponsavelDao.class, ResponsavelDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ColetaDao.class, ColetaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SincronizacaoDao.class, SincronizacaoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SyncLogDao.class, SyncLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PatrimonioDao patrimonioDao() {
    if (_patrimonioDao != null) {
      return _patrimonioDao;
    } else {
      synchronized(this) {
        if(_patrimonioDao == null) {
          _patrimonioDao = new PatrimonioDao_Impl(this);
        }
        return _patrimonioDao;
      }
    }
  }

  @Override
  public SalaDao salaDao() {
    if (_salaDao != null) {
      return _salaDao;
    } else {
      synchronized(this) {
        if(_salaDao == null) {
          _salaDao = new SalaDao_Impl(this);
        }
        return _salaDao;
      }
    }
  }

  @Override
  public ResponsavelDao responsavelDao() {
    if (_responsavelDao != null) {
      return _responsavelDao;
    } else {
      synchronized(this) {
        if(_responsavelDao == null) {
          _responsavelDao = new ResponsavelDao_Impl(this);
        }
        return _responsavelDao;
      }
    }
  }

  @Override
  public ColetaDao coletaDao() {
    if (_coletaDao != null) {
      return _coletaDao;
    } else {
      synchronized(this) {
        if(_coletaDao == null) {
          _coletaDao = new ColetaDao_Impl(this);
        }
        return _coletaDao;
      }
    }
  }

  @Override
  public SincronizacaoDao sincronizacaoDao() {
    if (_sincronizacaoDao != null) {
      return _sincronizacaoDao;
    } else {
      synchronized(this) {
        if(_sincronizacaoDao == null) {
          _sincronizacaoDao = new SincronizacaoDao_Impl(this);
        }
        return _sincronizacaoDao;
      }
    }
  }

  @Override
  public SyncLogDao syncLogDao() {
    if (_syncLogDao != null) {
      return _syncLogDao;
    } else {
      synchronized(this) {
        if(_syncLogDao == null) {
          _syncLogDao = new SyncLogDao_Impl(this);
        }
        return _syncLogDao;
      }
    }
  }
}
