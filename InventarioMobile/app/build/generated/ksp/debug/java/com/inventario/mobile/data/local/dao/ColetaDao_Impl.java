package com.inventario.mobile.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.paging.LimitOffsetPagingSource;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.inventario.mobile.data.local.entity.ColetaEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ColetaDao_Impl implements ColetaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ColetaEntity> __insertionAdapterOfColetaEntity;

  private final EntityDeletionOrUpdateAdapter<ColetaEntity> __deletionAdapterOfColetaEntity;

  private final EntityDeletionOrUpdateAdapter<ColetaEntity> __updateAdapterOfColetaEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteColetaById;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsSynchronized;

  private final SharedSQLiteStatement __preparedStmtOfClearSynchronizedColetas;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldSyncedColetas;

  public ColetaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfColetaEntity = new EntityInsertionAdapter<ColetaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `coleta` (`id`,`patrimonioId`,`usuarioId`,`dataColeta`,`localizacaoAtual`,`observacoes`,`fotoPath`,`status`,`latitude`,`longitude`,`sincronizado`,`sincronizada`,`dataCriacao`,`dataAtualizacao`,`servidorId`,`tentativasSincronizacao`,`ultimaTentativaSincronizacao`,`erroSincronizacao`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ColetaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPatrimonioId());
        statement.bindLong(3, entity.getUsuarioId());
        statement.bindLong(4, entity.getDataColeta());
        if (entity.getLocalizacaoAtual() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLocalizacaoAtual());
        }
        if (entity.getObservacoes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getObservacoes());
        }
        if (entity.getFotoPath() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getFotoPath());
        }
        statement.bindString(8, entity.getStatus());
        if (entity.getLatitude() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getLongitude());
        }
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final int _tmp_1 = entity.getSincronizada() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getDataCriacao());
        statement.bindLong(14, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getServidorId());
        }
        statement.bindLong(16, entity.getTentativasSincronizacao());
        if (entity.getUltimaTentativaSincronizacao() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getUltimaTentativaSincronizacao());
        }
        if (entity.getErroSincronizacao() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getErroSincronizacao());
        }
      }
    };
    this.__deletionAdapterOfColetaEntity = new EntityDeletionOrUpdateAdapter<ColetaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `coleta` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ColetaEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfColetaEntity = new EntityDeletionOrUpdateAdapter<ColetaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `coleta` SET `id` = ?,`patrimonioId` = ?,`usuarioId` = ?,`dataColeta` = ?,`localizacaoAtual` = ?,`observacoes` = ?,`fotoPath` = ?,`status` = ?,`latitude` = ?,`longitude` = ?,`sincronizado` = ?,`sincronizada` = ?,`dataCriacao` = ?,`dataAtualizacao` = ?,`servidorId` = ?,`tentativasSincronizacao` = ?,`ultimaTentativaSincronizacao` = ?,`erroSincronizacao` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ColetaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPatrimonioId());
        statement.bindLong(3, entity.getUsuarioId());
        statement.bindLong(4, entity.getDataColeta());
        if (entity.getLocalizacaoAtual() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLocalizacaoAtual());
        }
        if (entity.getObservacoes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getObservacoes());
        }
        if (entity.getFotoPath() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getFotoPath());
        }
        statement.bindString(8, entity.getStatus());
        if (entity.getLatitude() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getLongitude());
        }
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final int _tmp_1 = entity.getSincronizada() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getDataCriacao());
        statement.bindLong(14, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getServidorId());
        }
        statement.bindLong(16, entity.getTentativasSincronizacao());
        if (entity.getUltimaTentativaSincronizacao() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getUltimaTentativaSincronizacao());
        }
        if (entity.getErroSincronizacao() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getErroSincronizacao());
        }
        statement.bindLong(19, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteColetaById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM coleta WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsSynchronized = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE coleta SET sincronizado = 1, servidorId = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearSynchronizedColetas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM coleta WHERE sincronizado = 1";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldSyncedColetas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM coleta \n"
                + "        WHERE sincronizado = 1 \n"
                + "        AND dataColeta < ?\n"
                + "    ";
        return _query;
      }
    };
  }

  @Override
  public Object insertColeta(final ColetaEntity coleta,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfColetaEntity.insertAndReturnId(coleta);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertColetas(final List<ColetaEntity> coletas,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfColetaEntity.insertAndReturnIdsList(coletas);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteColeta(final ColetaEntity coleta,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfColetaEntity.handle(coleta);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateColeta(final ColetaEntity coleta,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfColetaEntity.handle(coleta);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ColetaEntity coleta, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfColetaEntity.handle(coleta);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteColetaById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteColetaById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteColetaById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsSynchronized(final long id, final long servidorId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSynchronized.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, servidorId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAsSynchronized.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearSynchronizedColetas(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearSynchronizedColetas.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearSynchronizedColetas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldSyncedColetas(final long timestamp,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldSyncedColetas.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldSyncedColetas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ColetaEntity>> getAllColetas() {
    final String _sql = "SELECT * FROM coleta ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"coleta"}, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public PagingSource<Integer, ColetaEntity> getAllColetasPaged() {
    final String _sql = "SELECT * FROM coleta ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return new LimitOffsetPagingSource<ColetaEntity>(_statement, __db, "coleta") {
      @Override
      @NonNull
      protected List<ColetaEntity> convertRows(@NonNull final Cursor cursor) {
        final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(cursor, "id");
        final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(cursor, "patrimonioId");
        final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(cursor, "usuarioId");
        final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(cursor, "dataColeta");
        final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(cursor, "localizacaoAtual");
        final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(cursor, "observacoes");
        final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(cursor, "fotoPath");
        final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(cursor, "status");
        final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(cursor, "latitude");
        final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(cursor, "longitude");
        final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizado");
        final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizada");
        final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataCriacao");
        final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataAtualizacao");
        final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(cursor, "servidorId");
        final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "tentativasSincronizacao");
        final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "ultimaTentativaSincronizacao");
        final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "erroSincronizacao");
        final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(cursor.getCount());
        while (cursor.moveToNext()) {
          final ColetaEntity _item;
          final long _tmpId;
          _tmpId = cursor.getLong(_cursorIndexOfId);
          final long _tmpPatrimonioId;
          _tmpPatrimonioId = cursor.getLong(_cursorIndexOfPatrimonioId);
          final long _tmpUsuarioId;
          _tmpUsuarioId = cursor.getLong(_cursorIndexOfUsuarioId);
          final long _tmpDataColeta;
          _tmpDataColeta = cursor.getLong(_cursorIndexOfDataColeta);
          final String _tmpLocalizacaoAtual;
          if (cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
            _tmpLocalizacaoAtual = null;
          } else {
            _tmpLocalizacaoAtual = cursor.getString(_cursorIndexOfLocalizacaoAtual);
          }
          final String _tmpObservacoes;
          if (cursor.isNull(_cursorIndexOfObservacoes)) {
            _tmpObservacoes = null;
          } else {
            _tmpObservacoes = cursor.getString(_cursorIndexOfObservacoes);
          }
          final String _tmpFotoPath;
          if (cursor.isNull(_cursorIndexOfFotoPath)) {
            _tmpFotoPath = null;
          } else {
            _tmpFotoPath = cursor.getString(_cursorIndexOfFotoPath);
          }
          final String _tmpStatus;
          _tmpStatus = cursor.getString(_cursorIndexOfStatus);
          final Double _tmpLatitude;
          if (cursor.isNull(_cursorIndexOfLatitude)) {
            _tmpLatitude = null;
          } else {
            _tmpLatitude = cursor.getDouble(_cursorIndexOfLatitude);
          }
          final Double _tmpLongitude;
          if (cursor.isNull(_cursorIndexOfLongitude)) {
            _tmpLongitude = null;
          } else {
            _tmpLongitude = cursor.getDouble(_cursorIndexOfLongitude);
          }
          final boolean _tmpSincronizado;
          final int _tmp;
          _tmp = cursor.getInt(_cursorIndexOfSincronizado);
          _tmpSincronizado = _tmp != 0;
          final boolean _tmpSincronizada;
          final int _tmp_1;
          _tmp_1 = cursor.getInt(_cursorIndexOfSincronizada);
          _tmpSincronizada = _tmp_1 != 0;
          final long _tmpDataCriacao;
          _tmpDataCriacao = cursor.getLong(_cursorIndexOfDataCriacao);
          final long _tmpDataAtualizacao;
          _tmpDataAtualizacao = cursor.getLong(_cursorIndexOfDataAtualizacao);
          final Long _tmpServidorId;
          if (cursor.isNull(_cursorIndexOfServidorId)) {
            _tmpServidorId = null;
          } else {
            _tmpServidorId = cursor.getLong(_cursorIndexOfServidorId);
          }
          final int _tmpTentativasSincronizacao;
          _tmpTentativasSincronizacao = cursor.getInt(_cursorIndexOfTentativasSincronizacao);
          final Long _tmpUltimaTentativaSincronizacao;
          if (cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
            _tmpUltimaTentativaSincronizacao = null;
          } else {
            _tmpUltimaTentativaSincronizacao = cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
          }
          final String _tmpErroSincronizacao;
          if (cursor.isNull(_cursorIndexOfErroSincronizacao)) {
            _tmpErroSincronizacao = null;
          } else {
            _tmpErroSincronizacao = cursor.getString(_cursorIndexOfErroSincronizacao);
          }
          _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
          _result.add(_item);
        }
        return _result;
      }
    };
  }

  @Override
  public PagingSource<Integer, ColetaEntity> getColetasByUsuarioPaged(final long usuarioId) {
    final String _sql = "SELECT * FROM coleta WHERE usuarioId = ? ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, usuarioId);
    return new LimitOffsetPagingSource<ColetaEntity>(_statement, __db, "coleta") {
      @Override
      @NonNull
      protected List<ColetaEntity> convertRows(@NonNull final Cursor cursor) {
        final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(cursor, "id");
        final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(cursor, "patrimonioId");
        final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(cursor, "usuarioId");
        final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(cursor, "dataColeta");
        final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(cursor, "localizacaoAtual");
        final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(cursor, "observacoes");
        final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(cursor, "fotoPath");
        final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(cursor, "status");
        final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(cursor, "latitude");
        final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(cursor, "longitude");
        final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizado");
        final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizada");
        final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataCriacao");
        final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataAtualizacao");
        final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(cursor, "servidorId");
        final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "tentativasSincronizacao");
        final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "ultimaTentativaSincronizacao");
        final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "erroSincronizacao");
        final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(cursor.getCount());
        while (cursor.moveToNext()) {
          final ColetaEntity _item;
          final long _tmpId;
          _tmpId = cursor.getLong(_cursorIndexOfId);
          final long _tmpPatrimonioId;
          _tmpPatrimonioId = cursor.getLong(_cursorIndexOfPatrimonioId);
          final long _tmpUsuarioId;
          _tmpUsuarioId = cursor.getLong(_cursorIndexOfUsuarioId);
          final long _tmpDataColeta;
          _tmpDataColeta = cursor.getLong(_cursorIndexOfDataColeta);
          final String _tmpLocalizacaoAtual;
          if (cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
            _tmpLocalizacaoAtual = null;
          } else {
            _tmpLocalizacaoAtual = cursor.getString(_cursorIndexOfLocalizacaoAtual);
          }
          final String _tmpObservacoes;
          if (cursor.isNull(_cursorIndexOfObservacoes)) {
            _tmpObservacoes = null;
          } else {
            _tmpObservacoes = cursor.getString(_cursorIndexOfObservacoes);
          }
          final String _tmpFotoPath;
          if (cursor.isNull(_cursorIndexOfFotoPath)) {
            _tmpFotoPath = null;
          } else {
            _tmpFotoPath = cursor.getString(_cursorIndexOfFotoPath);
          }
          final String _tmpStatus;
          _tmpStatus = cursor.getString(_cursorIndexOfStatus);
          final Double _tmpLatitude;
          if (cursor.isNull(_cursorIndexOfLatitude)) {
            _tmpLatitude = null;
          } else {
            _tmpLatitude = cursor.getDouble(_cursorIndexOfLatitude);
          }
          final Double _tmpLongitude;
          if (cursor.isNull(_cursorIndexOfLongitude)) {
            _tmpLongitude = null;
          } else {
            _tmpLongitude = cursor.getDouble(_cursorIndexOfLongitude);
          }
          final boolean _tmpSincronizado;
          final int _tmp;
          _tmp = cursor.getInt(_cursorIndexOfSincronizado);
          _tmpSincronizado = _tmp != 0;
          final boolean _tmpSincronizada;
          final int _tmp_1;
          _tmp_1 = cursor.getInt(_cursorIndexOfSincronizada);
          _tmpSincronizada = _tmp_1 != 0;
          final long _tmpDataCriacao;
          _tmpDataCriacao = cursor.getLong(_cursorIndexOfDataCriacao);
          final long _tmpDataAtualizacao;
          _tmpDataAtualizacao = cursor.getLong(_cursorIndexOfDataAtualizacao);
          final Long _tmpServidorId;
          if (cursor.isNull(_cursorIndexOfServidorId)) {
            _tmpServidorId = null;
          } else {
            _tmpServidorId = cursor.getLong(_cursorIndexOfServidorId);
          }
          final int _tmpTentativasSincronizacao;
          _tmpTentativasSincronizacao = cursor.getInt(_cursorIndexOfTentativasSincronizacao);
          final Long _tmpUltimaTentativaSincronizacao;
          if (cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
            _tmpUltimaTentativaSincronizacao = null;
          } else {
            _tmpUltimaTentativaSincronizacao = cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
          }
          final String _tmpErroSincronizacao;
          if (cursor.isNull(_cursorIndexOfErroSincronizacao)) {
            _tmpErroSincronizacao = null;
          } else {
            _tmpErroSincronizacao = cursor.getString(_cursorIndexOfErroSincronizacao);
          }
          _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
          _result.add(_item);
        }
        return _result;
      }
    };
  }

  @Override
  public PagingSource<Integer, ColetaEntity> getColetasPendentesPaged() {
    final String _sql = "SELECT * FROM coleta WHERE sincronizado = 0 ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return new LimitOffsetPagingSource<ColetaEntity>(_statement, __db, "coleta") {
      @Override
      @NonNull
      protected List<ColetaEntity> convertRows(@NonNull final Cursor cursor) {
        final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(cursor, "id");
        final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(cursor, "patrimonioId");
        final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(cursor, "usuarioId");
        final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(cursor, "dataColeta");
        final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(cursor, "localizacaoAtual");
        final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(cursor, "observacoes");
        final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(cursor, "fotoPath");
        final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(cursor, "status");
        final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(cursor, "latitude");
        final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(cursor, "longitude");
        final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizado");
        final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizada");
        final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataCriacao");
        final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataAtualizacao");
        final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(cursor, "servidorId");
        final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "tentativasSincronizacao");
        final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "ultimaTentativaSincronizacao");
        final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "erroSincronizacao");
        final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(cursor.getCount());
        while (cursor.moveToNext()) {
          final ColetaEntity _item;
          final long _tmpId;
          _tmpId = cursor.getLong(_cursorIndexOfId);
          final long _tmpPatrimonioId;
          _tmpPatrimonioId = cursor.getLong(_cursorIndexOfPatrimonioId);
          final long _tmpUsuarioId;
          _tmpUsuarioId = cursor.getLong(_cursorIndexOfUsuarioId);
          final long _tmpDataColeta;
          _tmpDataColeta = cursor.getLong(_cursorIndexOfDataColeta);
          final String _tmpLocalizacaoAtual;
          if (cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
            _tmpLocalizacaoAtual = null;
          } else {
            _tmpLocalizacaoAtual = cursor.getString(_cursorIndexOfLocalizacaoAtual);
          }
          final String _tmpObservacoes;
          if (cursor.isNull(_cursorIndexOfObservacoes)) {
            _tmpObservacoes = null;
          } else {
            _tmpObservacoes = cursor.getString(_cursorIndexOfObservacoes);
          }
          final String _tmpFotoPath;
          if (cursor.isNull(_cursorIndexOfFotoPath)) {
            _tmpFotoPath = null;
          } else {
            _tmpFotoPath = cursor.getString(_cursorIndexOfFotoPath);
          }
          final String _tmpStatus;
          _tmpStatus = cursor.getString(_cursorIndexOfStatus);
          final Double _tmpLatitude;
          if (cursor.isNull(_cursorIndexOfLatitude)) {
            _tmpLatitude = null;
          } else {
            _tmpLatitude = cursor.getDouble(_cursorIndexOfLatitude);
          }
          final Double _tmpLongitude;
          if (cursor.isNull(_cursorIndexOfLongitude)) {
            _tmpLongitude = null;
          } else {
            _tmpLongitude = cursor.getDouble(_cursorIndexOfLongitude);
          }
          final boolean _tmpSincronizado;
          final int _tmp;
          _tmp = cursor.getInt(_cursorIndexOfSincronizado);
          _tmpSincronizado = _tmp != 0;
          final boolean _tmpSincronizada;
          final int _tmp_1;
          _tmp_1 = cursor.getInt(_cursorIndexOfSincronizada);
          _tmpSincronizada = _tmp_1 != 0;
          final long _tmpDataCriacao;
          _tmpDataCriacao = cursor.getLong(_cursorIndexOfDataCriacao);
          final long _tmpDataAtualizacao;
          _tmpDataAtualizacao = cursor.getLong(_cursorIndexOfDataAtualizacao);
          final Long _tmpServidorId;
          if (cursor.isNull(_cursorIndexOfServidorId)) {
            _tmpServidorId = null;
          } else {
            _tmpServidorId = cursor.getLong(_cursorIndexOfServidorId);
          }
          final int _tmpTentativasSincronizacao;
          _tmpTentativasSincronizacao = cursor.getInt(_cursorIndexOfTentativasSincronizacao);
          final Long _tmpUltimaTentativaSincronizacao;
          if (cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
            _tmpUltimaTentativaSincronizacao = null;
          } else {
            _tmpUltimaTentativaSincronizacao = cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
          }
          final String _tmpErroSincronizacao;
          if (cursor.isNull(_cursorIndexOfErroSincronizacao)) {
            _tmpErroSincronizacao = null;
          } else {
            _tmpErroSincronizacao = cursor.getString(_cursorIndexOfErroSincronizacao);
          }
          _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
          _result.add(_item);
        }
        return _result;
      }
    };
  }

  @Override
  public PagingSource<Integer, ColetaEntity> getColetasByPatrimonioPaged(final long patrimonioId) {
    final String _sql = "SELECT * FROM coleta WHERE patrimonioId = ? ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, patrimonioId);
    return new LimitOffsetPagingSource<ColetaEntity>(_statement, __db, "coleta") {
      @Override
      @NonNull
      protected List<ColetaEntity> convertRows(@NonNull final Cursor cursor) {
        final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(cursor, "id");
        final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(cursor, "patrimonioId");
        final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(cursor, "usuarioId");
        final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(cursor, "dataColeta");
        final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(cursor, "localizacaoAtual");
        final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(cursor, "observacoes");
        final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(cursor, "fotoPath");
        final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(cursor, "status");
        final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(cursor, "latitude");
        final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(cursor, "longitude");
        final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizado");
        final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(cursor, "sincronizada");
        final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataCriacao");
        final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(cursor, "dataAtualizacao");
        final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(cursor, "servidorId");
        final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "tentativasSincronizacao");
        final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "ultimaTentativaSincronizacao");
        final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(cursor, "erroSincronizacao");
        final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(cursor.getCount());
        while (cursor.moveToNext()) {
          final ColetaEntity _item;
          final long _tmpId;
          _tmpId = cursor.getLong(_cursorIndexOfId);
          final long _tmpPatrimonioId;
          _tmpPatrimonioId = cursor.getLong(_cursorIndexOfPatrimonioId);
          final long _tmpUsuarioId;
          _tmpUsuarioId = cursor.getLong(_cursorIndexOfUsuarioId);
          final long _tmpDataColeta;
          _tmpDataColeta = cursor.getLong(_cursorIndexOfDataColeta);
          final String _tmpLocalizacaoAtual;
          if (cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
            _tmpLocalizacaoAtual = null;
          } else {
            _tmpLocalizacaoAtual = cursor.getString(_cursorIndexOfLocalizacaoAtual);
          }
          final String _tmpObservacoes;
          if (cursor.isNull(_cursorIndexOfObservacoes)) {
            _tmpObservacoes = null;
          } else {
            _tmpObservacoes = cursor.getString(_cursorIndexOfObservacoes);
          }
          final String _tmpFotoPath;
          if (cursor.isNull(_cursorIndexOfFotoPath)) {
            _tmpFotoPath = null;
          } else {
            _tmpFotoPath = cursor.getString(_cursorIndexOfFotoPath);
          }
          final String _tmpStatus;
          _tmpStatus = cursor.getString(_cursorIndexOfStatus);
          final Double _tmpLatitude;
          if (cursor.isNull(_cursorIndexOfLatitude)) {
            _tmpLatitude = null;
          } else {
            _tmpLatitude = cursor.getDouble(_cursorIndexOfLatitude);
          }
          final Double _tmpLongitude;
          if (cursor.isNull(_cursorIndexOfLongitude)) {
            _tmpLongitude = null;
          } else {
            _tmpLongitude = cursor.getDouble(_cursorIndexOfLongitude);
          }
          final boolean _tmpSincronizado;
          final int _tmp;
          _tmp = cursor.getInt(_cursorIndexOfSincronizado);
          _tmpSincronizado = _tmp != 0;
          final boolean _tmpSincronizada;
          final int _tmp_1;
          _tmp_1 = cursor.getInt(_cursorIndexOfSincronizada);
          _tmpSincronizada = _tmp_1 != 0;
          final long _tmpDataCriacao;
          _tmpDataCriacao = cursor.getLong(_cursorIndexOfDataCriacao);
          final long _tmpDataAtualizacao;
          _tmpDataAtualizacao = cursor.getLong(_cursorIndexOfDataAtualizacao);
          final Long _tmpServidorId;
          if (cursor.isNull(_cursorIndexOfServidorId)) {
            _tmpServidorId = null;
          } else {
            _tmpServidorId = cursor.getLong(_cursorIndexOfServidorId);
          }
          final int _tmpTentativasSincronizacao;
          _tmpTentativasSincronizacao = cursor.getInt(_cursorIndexOfTentativasSincronizacao);
          final Long _tmpUltimaTentativaSincronizacao;
          if (cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
            _tmpUltimaTentativaSincronizacao = null;
          } else {
            _tmpUltimaTentativaSincronizacao = cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
          }
          final String _tmpErroSincronizacao;
          if (cursor.isNull(_cursorIndexOfErroSincronizacao)) {
            _tmpErroSincronizacao = null;
          } else {
            _tmpErroSincronizacao = cursor.getString(_cursorIndexOfErroSincronizacao);
          }
          _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
          _result.add(_item);
        }
        return _result;
      }
    };
  }

  @Override
  public Object getColetaById(final long id, final Continuation<? super ColetaEntity> $completion) {
    final String _sql = "SELECT * FROM coleta WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ColetaEntity>() {
      @Override
      @Nullable
      public ColetaEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final ColetaEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _result = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getColetasPendentes(final Continuation<? super List<ColetaEntity>> $completion) {
    final String _sql = "SELECT * FROM coleta WHERE sincronizado = 0 ORDER BY dataColeta ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getColetasByPatrimonio(final long patrimonioId,
      final Continuation<? super List<ColetaEntity>> $completion) {
    final String _sql = "SELECT * FROM coleta WHERE patrimonioId = ? ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, patrimonioId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getColetasByUsuario(final long usuarioId,
      final Continuation<? super List<ColetaEntity>> $completion) {
    final String _sql = "SELECT * FROM coleta WHERE usuarioId = ? ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, usuarioId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM coleta WHERE sincronizado = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM coleta";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingSync(final Continuation<? super List<ColetaEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM coleta \n"
            + "        WHERE sincronizada = 0 \n"
            + "        AND (tentativasSincronizacao < 3 OR tentativasSincronizacao IS NULL)\n"
            + "        ORDER BY dataColeta ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getFailedSync(final Continuation<? super List<ColetaEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM coleta \n"
            + "        WHERE sincronizada = 0 \n"
            + "        AND tentativasSincronizacao >= 3\n"
            + "        ORDER BY ultimaTentativaSincronizacao DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _item = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long id, final Continuation<? super ColetaEntity> $completion) {
    final String _sql = "SELECT * FROM coleta WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ColetaEntity>() {
      @Override
      @Nullable
      public ColetaEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPatrimonioId = CursorUtil.getColumnIndexOrThrow(_cursor, "patrimonioId");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfLocalizacaoAtual = CursorUtil.getColumnIndexOrThrow(_cursor, "localizacaoAtual");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfFotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "fotoPath");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfSincronizada = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizada");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfUltimaTentativaSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "ultimaTentativaSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final ColetaEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPatrimonioId;
            _tmpPatrimonioId = _cursor.getLong(_cursorIndexOfPatrimonioId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final String _tmpLocalizacaoAtual;
            if (_cursor.isNull(_cursorIndexOfLocalizacaoAtual)) {
              _tmpLocalizacaoAtual = null;
            } else {
              _tmpLocalizacaoAtual = _cursor.getString(_cursorIndexOfLocalizacaoAtual);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final String _tmpFotoPath;
            if (_cursor.isNull(_cursorIndexOfFotoPath)) {
              _tmpFotoPath = null;
            } else {
              _tmpFotoPath = _cursor.getString(_cursorIndexOfFotoPath);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final boolean _tmpSincronizada;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizada);
            _tmpSincronizada = _tmp_1 != 0;
            final long _tmpDataCriacao;
            _tmpDataCriacao = _cursor.getLong(_cursorIndexOfDataCriacao);
            final long _tmpDataAtualizacao;
            _tmpDataAtualizacao = _cursor.getLong(_cursorIndexOfDataAtualizacao);
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final Long _tmpUltimaTentativaSincronizacao;
            if (_cursor.isNull(_cursorIndexOfUltimaTentativaSincronizacao)) {
              _tmpUltimaTentativaSincronizacao = null;
            } else {
              _tmpUltimaTentativaSincronizacao = _cursor.getLong(_cursorIndexOfUltimaTentativaSincronizacao);
            }
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            _result = new ColetaEntity(_tmpId,_tmpPatrimonioId,_tmpUsuarioId,_tmpDataColeta,_tmpLocalizacaoAtual,_tmpObservacoes,_tmpFotoPath,_tmpStatus,_tmpLatitude,_tmpLongitude,_tmpSincronizado,_tmpSincronizada,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId,_tmpTentativasSincronizacao,_tmpUltimaTentativaSincronizacao,_tmpErroSincronizacao);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
