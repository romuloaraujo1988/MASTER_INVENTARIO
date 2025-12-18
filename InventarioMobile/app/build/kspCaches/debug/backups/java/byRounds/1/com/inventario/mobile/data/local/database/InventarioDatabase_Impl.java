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
import com.inventario.mobile.data.local.dao.ColetaDao_InventarioDatabase_Impl;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao_InventarioDatabase_Impl;
import com.inventario.mobile.data.local.dao.SalaDao;
import com.inventario.mobile.data.local.dao.SalaDao_InventarioDatabase_Impl;
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
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `usuario` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `email` TEXT NOT NULL, `senha` TEXT, `ativo` INTEGER NOT NULL, `sincronizado` INTEGER NOT NULL, `dataCriacao` INTEGER NOT NULL, `dataAtualizacao` INTEGER NOT NULL, `servidorId` INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usuario_email` ON `usuario` (`email`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `patrimonio` (`id` INTEGER NOT NULL, `numero` TEXT NOT NULL, `numeroPatrimonio` TEXT NOT NULL, `descricao` TEXT NOT NULL, `marca` TEXT, `modelo` TEXT, `numeroSerie` TEXT, `estado` TEXT, `valor` REAL, `setorId` INTEGER, `setorNome` TEXT, `idSala` INTEGER, `nomeSala` TEXT, `salaId` INTEGER, `salaNome` TEXT, `idResponsavel` INTEGER, `nomeResponsavel` TEXT, `responsavelId` INTEGER, `responsavelNome` TEXT, `status` TEXT, `coletado` INTEGER NOT NULL, `dataColeta` INTEGER, `coletadoPor` TEXT, `observacoesColeta` TEXT, `observacoes` TEXT, `dataUltimaAtualizacao` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_patrimonio_numero` ON `patrimonio` (`numero`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_numeroPatrimonio` ON `patrimonio` (`numeroPatrimonio`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_descricao` ON `patrimonio` (`descricao`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_idSala` ON `patrimonio` (`idSala`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_coletado` ON `patrimonio` (`coletado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_nomeSala` ON `patrimonio` (`nomeSala`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_responsavelNome` ON `patrimonio` (`responsavelNome`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_coletado_numeroPatrimonio` ON `patrimonio` (`coletado`, `numeroPatrimonio`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_coletado_descricao` ON `patrimonio` (`coletado`, `descricao`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_coletado_nomeSala` ON `patrimonio` (`coletado`, `nomeSala`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patrimonio_idSala_coletado` ON `patrimonio` (`idSala`, `coletado`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `coleta` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `idPatrimonio` INTEGER NOT NULL, `numeroPatrimonio` TEXT NOT NULL, `idInventario` INTEGER NOT NULL, `idSala` INTEGER, `nomeSala` TEXT, `idResponsavel` INTEGER, `nomeResponsavel` TEXT, `observacao` TEXT, `estadoPatrimonio` TEXT, `latitude` REAL, `longitude` REAL, `dataColeta` INTEGER NOT NULL, `idUsuario` INTEGER NOT NULL, `nomeUsuario` TEXT NOT NULL, `sincronizado` INTEGER NOT NULL, `tentativasSincronizacao` INTEGER NOT NULL, `erroSincronizacao` TEXT, `servidorId` INTEGER, `tempoColetaSegundos` INTEGER, `tempoScanSegundos` INTEGER, `tempoPreenchimentoSegundos` INTEGER, `metodoColeta` TEXT, `horaColeta` INTEGER, `diaSemana` INTEGER, `periodoColeta` TEXT, `tipoScan` TEXT, `tentativasScan` INTEGER NOT NULL, `errosScan` INTEGER NOT NULL, `qualidadeEtiqueta` TEXT, `semEtiqueta` INTEGER NOT NULL, `descricaoItemSemEtiqueta` TEXT, `categoriaItemSemEtiqueta` TEXT, `fotoPatrimonio` TEXT, `fotoPath` TEXT, `fotoThumbnailPath` TEXT, `fotoSincronizada` INTEGER NOT NULL, `motivoFoto` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_idPatrimonio` ON `coleta` (`idPatrimonio`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_idInventario` ON `coleta` (`idInventario`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_sincronizado` ON `coleta` (`sincronizado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_dataColeta` ON `coleta` (`dataColeta`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_metodoColeta` ON `coleta` (`metodoColeta`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_tipoScan` ON `coleta` (`tipoScan`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_idPatrimonio_idInventario` ON `coleta` (`idPatrimonio`, `idInventario`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_idInventario_sincronizado` ON `coleta` (`idInventario`, `sincronizado`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coleta_idInventario_dataColeta` ON `coleta` (`idInventario`, `dataColeta`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sala` (`id` INTEGER NOT NULL, `nome` TEXT NOT NULL, `idSetor` INTEGER, `nomeSetor` TEXT, `ativa` INTEGER NOT NULL, `dataUltimaAtualizacao` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sala_nome` ON `sala` (`nome`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sala_ativa` ON `sala` (`ativa`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `setor` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `descricao` TEXT, `sincronizado` INTEGER NOT NULL, `dataCriacao` INTEGER NOT NULL, `dataAtualizacao` INTEGER NOT NULL, `servidorId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_setor_nome` ON `setor` (`nome`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2fb0161000d8512664137815738fc637')");
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
        final HashMap<String, TableInfo.Column> _columnsPatrimonio = new HashMap<String, TableInfo.Column>(26);
        _columnsPatrimonio.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("numero", new TableInfo.Column("numero", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("numeroPatrimonio", new TableInfo.Column("numeroPatrimonio", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("descricao", new TableInfo.Column("descricao", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("marca", new TableInfo.Column("marca", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("modelo", new TableInfo.Column("modelo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("numeroSerie", new TableInfo.Column("numeroSerie", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("estado", new TableInfo.Column("estado", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("valor", new TableInfo.Column("valor", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("setorId", new TableInfo.Column("setorId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("setorNome", new TableInfo.Column("setorNome", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("idSala", new TableInfo.Column("idSala", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("nomeSala", new TableInfo.Column("nomeSala", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("salaId", new TableInfo.Column("salaId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("salaNome", new TableInfo.Column("salaNome", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("idResponsavel", new TableInfo.Column("idResponsavel", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("nomeResponsavel", new TableInfo.Column("nomeResponsavel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("responsavelId", new TableInfo.Column("responsavelId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("responsavelNome", new TableInfo.Column("responsavelNome", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("coletado", new TableInfo.Column("coletado", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("dataColeta", new TableInfo.Column("dataColeta", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("coletadoPor", new TableInfo.Column("coletadoPor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("observacoesColeta", new TableInfo.Column("observacoesColeta", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("observacoes", new TableInfo.Column("observacoes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatrimonio.put("dataUltimaAtualizacao", new TableInfo.Column("dataUltimaAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPatrimonio = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPatrimonio = new HashSet<TableInfo.Index>(11);
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_numero", true, Arrays.asList("numero"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_numeroPatrimonio", false, Arrays.asList("numeroPatrimonio"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_descricao", false, Arrays.asList("descricao"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_idSala", false, Arrays.asList("idSala"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_coletado", false, Arrays.asList("coletado"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_nomeSala", false, Arrays.asList("nomeSala"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_responsavelNome", false, Arrays.asList("responsavelNome"), Arrays.asList("ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_coletado_numeroPatrimonio", false, Arrays.asList("coletado", "numeroPatrimonio"), Arrays.asList("ASC", "ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_coletado_descricao", false, Arrays.asList("coletado", "descricao"), Arrays.asList("ASC", "ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_coletado_nomeSala", false, Arrays.asList("coletado", "nomeSala"), Arrays.asList("ASC", "ASC")));
        _indicesPatrimonio.add(new TableInfo.Index("index_patrimonio_idSala_coletado", false, Arrays.asList("idSala", "coletado"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoPatrimonio = new TableInfo("patrimonio", _columnsPatrimonio, _foreignKeysPatrimonio, _indicesPatrimonio);
        final TableInfo _existingPatrimonio = TableInfo.read(db, "patrimonio");
        if (!_infoPatrimonio.equals(_existingPatrimonio)) {
          return new RoomOpenHelper.ValidationResult(false, "patrimonio(com.inventario.mobile.data.local.entity.PatrimonioEntity).\n"
                  + " Expected:\n" + _infoPatrimonio + "\n"
                  + " Found:\n" + _existingPatrimonio);
        }
        final HashMap<String, TableInfo.Column> _columnsColeta = new HashMap<String, TableInfo.Column>(38);
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
        _columnsColeta.put("tempoColetaSegundos", new TableInfo.Column("tempoColetaSegundos", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("tempoScanSegundos", new TableInfo.Column("tempoScanSegundos", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("tempoPreenchimentoSegundos", new TableInfo.Column("tempoPreenchimentoSegundos", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("metodoColeta", new TableInfo.Column("metodoColeta", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("horaColeta", new TableInfo.Column("horaColeta", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("diaSemana", new TableInfo.Column("diaSemana", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("periodoColeta", new TableInfo.Column("periodoColeta", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("tipoScan", new TableInfo.Column("tipoScan", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("tentativasScan", new TableInfo.Column("tentativasScan", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("errosScan", new TableInfo.Column("errosScan", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("qualidadeEtiqueta", new TableInfo.Column("qualidadeEtiqueta", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("semEtiqueta", new TableInfo.Column("semEtiqueta", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("descricaoItemSemEtiqueta", new TableInfo.Column("descricaoItemSemEtiqueta", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("categoriaItemSemEtiqueta", new TableInfo.Column("categoriaItemSemEtiqueta", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("fotoPatrimonio", new TableInfo.Column("fotoPatrimonio", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("fotoPath", new TableInfo.Column("fotoPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("fotoThumbnailPath", new TableInfo.Column("fotoThumbnailPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("fotoSincronizada", new TableInfo.Column("fotoSincronizada", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsColeta.put("motivoFoto", new TableInfo.Column("motivoFoto", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysColeta = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesColeta = new HashSet<TableInfo.Index>(9);
        _indicesColeta.add(new TableInfo.Index("index_coleta_idPatrimonio", false, Arrays.asList("idPatrimonio"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_idInventario", false, Arrays.asList("idInventario"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_sincronizado", false, Arrays.asList("sincronizado"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_dataColeta", false, Arrays.asList("dataColeta"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_metodoColeta", false, Arrays.asList("metodoColeta"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_tipoScan", false, Arrays.asList("tipoScan"), Arrays.asList("ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_idPatrimonio_idInventario", false, Arrays.asList("idPatrimonio", "idInventario"), Arrays.asList("ASC", "ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_idInventario_sincronizado", false, Arrays.asList("idInventario", "sincronizado"), Arrays.asList("ASC", "ASC")));
        _indicesColeta.add(new TableInfo.Index("index_coleta_idInventario_dataColeta", false, Arrays.asList("idInventario", "dataColeta"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoColeta = new TableInfo("coleta", _columnsColeta, _foreignKeysColeta, _indicesColeta);
        final TableInfo _existingColeta = TableInfo.read(db, "coleta");
        if (!_infoColeta.equals(_existingColeta)) {
          return new RoomOpenHelper.ValidationResult(false, "coleta(com.inventario.mobile.data.local.entity.ColetaEntity).\n"
                  + " Expected:\n" + _infoColeta + "\n"
                  + " Found:\n" + _existingColeta);
        }
        final HashMap<String, TableInfo.Column> _columnsSala = new HashMap<String, TableInfo.Column>(6);
        _columnsSala.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("nome", new TableInfo.Column("nome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("idSetor", new TableInfo.Column("idSetor", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("nomeSetor", new TableInfo.Column("nomeSetor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("ativa", new TableInfo.Column("ativa", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSala.put("dataUltimaAtualizacao", new TableInfo.Column("dataUltimaAtualizacao", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSala = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSala = new HashSet<TableInfo.Index>(2);
        _indicesSala.add(new TableInfo.Index("index_sala_nome", false, Arrays.asList("nome"), Arrays.asList("ASC")));
        _indicesSala.add(new TableInfo.Index("index_sala_ativa", false, Arrays.asList("ativa"), Arrays.asList("ASC")));
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
    }, "2fb0161000d8512664137815738fc637", "89b7794568dad6e7c1fa274f968780a2");
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
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `usuario`");
      _db.execSQL("DELETE FROM `patrimonio`");
      _db.execSQL("DELETE FROM `coleta`");
      _db.execSQL("DELETE FROM `sala`");
      _db.execSQL("DELETE FROM `setor`");
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
    _typeConvertersMap.put(UsuarioDao.class, UsuarioDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PatrimonioDao.class, PatrimonioDao_InventarioDatabase_Impl.getRequiredConverters());
    _typeConvertersMap.put(ColetaDao.class, ColetaDao_InventarioDatabase_Impl.getRequiredConverters());
    _typeConvertersMap.put(SalaDao.class, SalaDao_InventarioDatabase_Impl.getRequiredConverters());
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
          _patrimonioDao = new PatrimonioDao_InventarioDatabase_Impl(this);
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
          _coletaDao = new ColetaDao_InventarioDatabase_Impl(this);
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
          _salaDao = new SalaDao_InventarioDatabase_Impl(this);
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
