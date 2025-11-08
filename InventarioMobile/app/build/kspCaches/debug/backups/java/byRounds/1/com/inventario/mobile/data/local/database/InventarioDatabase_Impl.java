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
import com.inventario.mobile.data.local.dao.SalaDao;
import com.inventario.mobile.data.local.dao.SalaDao_Impl;
import com.inventario.mobile.data.local.dao.SetorDao;
import com.inventario.mobile.data.local.dao.SetorDao_Impl;
import com.inventario.mobile.data.local.dao.UsuarioDao;
import com.inventario.mobile.data.local.dao.UsuarioDao_Impl;
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
public final class InventarioDatabase_Impl extends InventarioDatabase {
  private volatile UsuarioDao _usuarioDao;

  private volatile PatrimonioDao _patrimonioDao;

  private volatile ColetaDao _coletaDao;

  private volatile SalaDao _salaDao;

  private volatile SetorDao _setorDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `usuario` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `email` TEXT NOT NULL, `senha` TEXT, `ativo` INTEGER NOT NULL, `sincronizado` INTEGER NOT NULL, `dataCriacao` INTEGER NOT NULL, `dataAtualizacao` INTEGER NOT NULL, `servidorId` INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usuario_email` ON `usuario` (`email`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `patrimonio` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `codigo` TEXT NOT NULL, `descricao` TEXT NOT NULL, `marca` TEXT, `modelo` TEXT, `numeroSerie` TEXT, `estado` TEXT, `valor` REAL, `salaId` INTEGER, `salaNome` TEXT, `qrCode` TEXT, `coletado` INTEGER NOT NULL, `sincronizado` INTEGER NOT NULL, `dataCriacao` INTEGER NOT NULL, `dataAtualizacao` INTEGER NOT NULL, `servidorId` INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_patrimonio_codigo` ON `patrimonio` (`codigo`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_salaId` ON `patrimonio` (`salaId`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_patrimonio_qrCode` ON `patrimonio` (`qrCode`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_descricao` ON `patrimonio` (`descricao`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_coletado` ON `patrimonio` (`coletado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_sincronizado` ON `patrimonio` (`sincronizado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_servidorId` ON `patrimonio` (`servidorId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_salaId_coletado` ON `patrimonio` (`salaId`, `coletado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_coletado_sincronizado` ON `patrimonio` (`coletado`, `sincronizado`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `coleta` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `patrimonioId` INTEGER NOT NULL, `usuarioId` INTEGER NOT NULL, `dataColeta` INTEGER NOT NULL, `localizacaoAtual` TEXT, `observacoes` TEXT, `fotoPath` TEXT, `status` TEXT NOT NULL, `latitude` REAL, `longitude` REAL, `sincronizado` INTEGER NOT NULL, `sincronizada` INTEGER NOT NULL, `dataCriacao` INTEGER NOT NULL, `dataAtualizacao` INTEGER NOT NULL, `servidorId` INTEGER, `tentativasSincronizacao` INTEGER NOT NULL, `ultimaTentativaSincronizacao` INTEGER, `erroSincronizacao` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_patrimonioId` ON `coleta` (`patrimonioId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_usuarioId` ON `coleta` (`usuarioId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_dataColeta` ON `coleta` (`dataColeta`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_sincronizado` ON `coleta` (`sincronizado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_status` ON `coleta` (`status`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_servidorId` ON `coleta` (`servidorId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_usuarioId_sincronizado` ON `coleta` (`usuarioId`, `sincronizado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_usuarioId_dataColeta` ON `coleta` (`usuarioId`, `dataColeta`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_sincronizado_dataColeta` ON `coleta` (`sincronizado`, `dataColeta`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sala` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `descricao` TEXT, `setorId` INTEGER NOT NULL, `setorNome` TEXT, `sincronizado` INTEGER NOT NULL, `dataCriacao` INTEGER NOT NULL, `dataAtualizacao` INTEGER NOT NULL, `servidorId` INTEGER, FOREIGN KEY(`setorId`) REFERENCES `setor`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sala_nome` ON `sala` (`nome`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sala_setorId` ON `sala` (`setorId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `setor` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `descricao` TEXT, `sincronizado` INTEGER NOT NULL, `dataCriacao` INTEGER NOT NULL, `dataAtualizacao` INTEGER NOT NULL, `servidorId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_setor_nome` ON `setor` (`nome`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '72cda4b070414c4ede3d5346cbfde086')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `usuario`");
        db.execSQL("DROP TABLE IF EXISTS `patrimonio`");
        db.execSQL("DROP TABLE IF EXISTS `coleta`");
        db.execSQL("DROP TABLE IF EXISTS `sala`");
        db.execSQL("DROP TABLE IF EXISTS `setor`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsUsuario = new HashMap<String, TableInfo.Column>(9);
        _columnsUsuario.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("nome", new TableInfo.Column("nome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("senha", new TableInfo.Column("senha", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("ativo", new TableInfo.Column("ativo", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("sincronizado", new TableInfo.Column("sincronizado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("dataCriacao", new TableInfo.Column("dataCriacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("dataAtualizacao", new TableInfo.Column("dataAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsuario.put("servidorId", new TableInfo.Column("servidorId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsuario = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsuario = new HashSet<TableInfo.Index>(1);
        _indicesUsuario.add(new TableInfo.Index("index_usuario_email", true, Arrays.asList("email"), Arrays.asList("ASC")));
        final TableInfo _infoUsuario = new TableInfo("usuario", _columnsUsuario, _foreignKeysUsuario, _indicesUsuario);
        final TableInfo _existingUsuario = TableInfo.read(db, "usuario");
        if (!_infoUsuario.equals(_existingUsuario)) {
          return new RoomOpenHelper.ValidationResult(false, "usuario(com.inventario.mobile.data.local.entity.UsuarioEntity).\n"
                  + " Expected:\n" + _infoUsuario + "\n"
                  + " Found:\n" + _existingUsuario);
        }
        final HashMap<String, TableInfo.Column> _columnsPatrimonio = new HashMap<String, TableInfo.Column>(16);
        _columnsPatrimonio.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("codigo", new TableInfo.Column("codigo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("descricao", new TableInfo.Column("descricao", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("marca", new TableInfo.Column("marca", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("modelo", new TableInfo.Column("modelo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("numeroSerie", new TableInfo.Column("numeroSerie", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("estado", new TableInfo.Column("estado", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("valor", new TableInfo.Column("valor", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("salaId", new TableInfo.Column("salaId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("salaNome", new TableInfo.Column("salaNome", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("qrCode", new TableInfo.Column("qrCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("coletado", new TableInfo.Column("coletado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("sincronizado", new TableInfo.Column("sincronizado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("dataCriacao", new TableInfo.Column("dataCriacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("dataAtualizacao", new TableInfo.Column("dataAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("servidorId", new TableInfo.Column("servidorId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPatrimonio = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPatrimonio = new HashSet<TableInfo.Index>(9);
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_codigo", true, Arrays.asList("codigo"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_salaId", false, Arrays.asList("salaId"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_qrCode", true, Arrays.asList("qrCode"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_descricao", false, Arrays.asList("descricao"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_coletado", false, Arrays.asList("coletado"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_sincronizado", false, Arrays.asList("sincronizado"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_servidorId", false, Arrays.asList("servidorId"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_salaId_coletado", false, Arrays.asList("salaId", "coletado"), Arrays.asList("ASC", "ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_coletado_sincronizado", false, Arrays.asList("coletado", "sincronizado"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoPatrimonio = new TableInfo("patrimonio", _columnsPatrimonio, _foreignKeysPatrimonio, _indicesPatrimonio);
        final TableInfo _existingPatrimonio = TableInfo.read(db, "patrimonio");
        if (!_infoPatrimonio.equals(_existingPatrimonio)) {
          return new RoomOpenHelper.ValidationResult(false, "patrimonio(com.inventario.mobile.data.local.entity.PatrimonioEntity).\n"
                  + " Expected:\n" + _infoPatrimonio + "\n"
                  + " Found:\n" + _existingPatrimonio);
        }
        final HashMap<String, TableInfo.Column> _columnsColeta = new HashMap<String, TableInfo.Column>(18);
        _columnsColeta.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("patrimonioId", new TableInfo.Column("patrimonioId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("usuarioId", new TableInfo.Column("usuarioId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("dataColeta", new TableInfo.Column("dataColeta", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("localizacaoAtual", new TableInfo.Column("localizacaoAtual", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("observacoes", new TableInfo.Column("observacoes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("fotoPath", new TableInfo.Column("fotoPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("sincronizado", new TableInfo.Column("sincronizado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("sincronizada", new TableInfo.Column("sincronizada", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("dataCriacao", new TableInfo.Column("dataCriacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("dataAtualizacao", new TableInfo.Column("dataAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("servidorId", new TableInfo.Column("servidorId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("tentativasSincronizacao", new TableInfo.Column("tentativasSincronizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("ultimaTentativaSincronizacao", new TableInfo.Column("ultimaTentativaSincronizacao", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("erroSincronizacao", new TableInfo.Column("erroSincronizacao", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysColeta = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesColeta = new HashSet<TableInfo.Index>(9);
        _indicesColeta.add(new TableInfo.Index("index_coleta_patrimonioId", false, Arrays.asList("patrimonioId"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_usuarioId", false, Arrays.asList("usuarioId"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_dataColeta", false, Arrays.asList("dataColeta"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_sincronizado", false, Arrays.asList("sincronizado"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_status", false, Arrays.asList("status"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_servidorId", false, Arrays.asList("servidorId"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_usuarioId_sincronizado", false, Arrays.asList("usuarioId", "sincronizado"), Arrays.asList("ASC", "ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_usuarioId_dataColeta", false, Arrays.asList("usuarioId", "dataColeta"), Arrays.asList("ASC", "ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_sincronizado_dataColeta", false, Arrays.asList("sincronizado", "dataColeta"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoColeta = new TableInfo("coleta", _columnsColeta, _foreignKeysColeta, _indicesColeta);
        final TableInfo _existingColeta = TableInfo.read(db, "coleta");
        if (!_infoColeta.equals(_existingColeta)) {
          return new RoomOpenHelper.ValidationResult(false, "coleta(com.inventario.mobile.data.local.entity.ColetaEntity).\n"
                  + " Expected:\n" + _infoColeta + "\n"
                  + " Found:\n" + _existingColeta);
        }
        final HashMap<String, TableInfo.Column> _columnsSala = new HashMap<String, TableInfo.Column>(9);
        _columnsSala.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("nome", new TableInfo.Column("nome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("descricao", new TableInfo.Column("descricao", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("setorId", new TableInfo.Column("setorId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("setorNome", new TableInfo.Column("setorNome", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("sincronizado", new TableInfo.Column("sincronizado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("dataCriacao", new TableInfo.Column("dataCriacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("dataAtualizacao", new TableInfo.Column("dataAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("servidorId", new TableInfo.Column("servidorId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSala = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSala.add(new TableInfo.ForeignKey("setor", "CASCADE", "NO ACTION", Arrays.asList("setorId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSala = new HashSet<TableInfo.Index>(2);
        _indicesSala.add(new TableInfo.Index("index_sala_nome", false, Arrays.asList("nome"), Arrays.asList("ASC")));
        _indicesSala.add(new TableInfo.Index("index_sala_setorId", false, Arrays.asList("setorId"), Arrays.asList("ASC")));
        final TableInfo _infoSala = new TableInfo("sala", _columnsSala, _foreignKeysSala, _indicesSala);
        final TableInfo _existingSala = TableInfo.read(db, "sala");
        if (!_infoSala.equals(_existingSala)) {
          return new RoomOpenHelper.ValidationResult(false, "sala(com.inventario.mobile.data.local.entity.SalaEntity).\n"
                  + " Expected:\n" + _infoSala + "\n"
                  + " Found:\n" + _existingSala);
        }
        final HashMap<String, TableInfo.Column> _columnsSetor = new HashMap<String, TableInfo.Column>(7);
        _columnsSetor.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetor.put("nome", new TableInfo.Column("nome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetor.put("descricao", new TableInfo.Column("descricao", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetor.put("sincronizado", new TableInfo.Column("sincronizado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetor.put("dataCriacao", new TableInfo.Column("dataCriacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetor.put("dataAtualizacao", new TableInfo.Column("dataAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetor.put("servidorId", new TableInfo.Column("servidorId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSetor = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSetor = new HashSet<TableInfo.Index>(1);
        _indicesSetor.add(new TableInfo.Index("index_setor_nome", false, Arrays.asList("nome"), Arrays.asList("ASC")));
        final TableInfo _infoSetor = new TableInfo("setor", _columnsSetor, _foreignKeysSetor, _indicesSetor);
        final TableInfo _existingSetor = TableInfo.read(db, "setor");
        if (!_infoSetor.equals(_existingSetor)) {
          return new RoomOpenHelper.ValidationResult(false, "setor(com.inventario.mobile.data.local.entity.SetorEntity).\n"
                  + " Expected:\n" + _infoSetor + "\n"
                  + " Found:\n" + _existingSetor);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "72cda4b070414c4ede3d5346cbfde086", "84ab55da04240cabd37896b6f60ab339");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "usuario","patrimonio","coleta","sala","setor");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `usuario`");
      _db.execSQL("DELETE FROM `patrimonio`");
      _db.execSQL("DELETE FROM `coleta`");
      _db.execSQL("DELETE FROM `sala`");
      _db.execSQL("DELETE FROM `setor`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(UsuarioDao.class, UsuarioDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PatrimonioDao.class, PatrimonioDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ColetaDao.class, ColetaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SalaDao.class, SalaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SetorDao.class, SetorDao_Impl.getRequiredConverters());
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
  public UsuarioDao usuarioDao() {
    if (_usuarioDao != null) {
      return _usuarioDao;
    } else {
      synchronized(this) {
        if(_usuarioDao == null) {
          _usuarioDao = new UsuarioDao_Impl(this);
        }
        return _usuarioDao;
      }
    }
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
  public SetorDao setorDao() {
    if (_setorDao != null) {
      return _setorDao;
    } else {
      synchronized(this) {
        if(_setorDao == null) {
          _setorDao = new SetorDao_Impl(this);
        }
        return _setorDao;
      }
    }
  }
}
