package com.inventario.mobile.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.inventario.mobile.data.local.entity.SyncLogEntity;
import java.lang.Class;
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
public final class SyncLogDao_Impl implements SyncLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SyncLogEntity> __insertionAdapterOfSyncLogEntity;

  private final SharedSQLiteStatement __preparedStmtOfLimparAntigos;

  private final SharedSQLiteStatement __preparedStmtOfLimparTodos;

  public SyncLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSyncLogEntity = new EntityInsertionAdapter<SyncLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sync_log` (`id`,`tipo`,`dataHora`,`status`,`mensagem`,`coletasSincronizadas`,`coletasFalhadas`,`stackTrace`,`duracao`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SyncLogEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTipo());
        statement.bindLong(3, entity.getDataHora());
        statement.bindString(4, entity.getStatus());
        statement.bindString(5, entity.getMensagem());
        statement.bindLong(6, entity.getColetasSincronizadas());
        statement.bindLong(7, entity.getColetasFalhadas());
        if (entity.getStackTrace() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getStackTrace());
        }
        statement.bindLong(9, entity.getDuracao());
      }
    };
    this.__preparedStmtOfLimparAntigos = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM sync_log \n"
                + "        WHERE id NOT IN (\n"
                + "            SELECT id FROM sync_log \n"
                + "            ORDER BY dataHora DESC \n"
                + "            LIMIT ?\n"
                + "        )\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfLimparTodos = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sync_log";
        return _query;
      }
    };
  }

  @Override
  public Object inserir(final SyncLogEntity log, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSyncLogEntity.insertAndReturnId(log);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object limparAntigos(final int limit, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLimparAntigos.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, limit);
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
          __preparedStmtOfLimparAntigos.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object limparTodos(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLimparTodos.acquire();
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
          __preparedStmtOfLimparTodos.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SyncLogEntity>> observarTodos() {
    final String _sql = "SELECT * FROM sync_log ORDER BY dataHora DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sync_log"}, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTipo = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo");
          final int _cursorIndexOfDataHora = CursorUtil.getColumnIndexOrThrow(_cursor, "dataHora");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMensagem = CursorUtil.getColumnIndexOrThrow(_cursor, "mensagem");
          final int _cursorIndexOfColetasSincronizadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasSincronizadas");
          final int _cursorIndexOfColetasFalhadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasFalhadas");
          final int _cursorIndexOfStackTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "stackTrace");
          final int _cursorIndexOfDuracao = CursorUtil.getColumnIndexOrThrow(_cursor, "duracao");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTipo;
            _tmpTipo = _cursor.getString(_cursorIndexOfTipo);
            final long _tmpDataHora;
            _tmpDataHora = _cursor.getLong(_cursorIndexOfDataHora);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMensagem;
            _tmpMensagem = _cursor.getString(_cursorIndexOfMensagem);
            final int _tmpColetasSincronizadas;
            _tmpColetasSincronizadas = _cursor.getInt(_cursorIndexOfColetasSincronizadas);
            final int _tmpColetasFalhadas;
            _tmpColetasFalhadas = _cursor.getInt(_cursorIndexOfColetasFalhadas);
            final String _tmpStackTrace;
            if (_cursor.isNull(_cursorIndexOfStackTrace)) {
              _tmpStackTrace = null;
            } else {
              _tmpStackTrace = _cursor.getString(_cursorIndexOfStackTrace);
            }
            final long _tmpDuracao;
            _tmpDuracao = _cursor.getLong(_cursorIndexOfDuracao);
            _item = new SyncLogEntity(_tmpId,_tmpTipo,_tmpDataHora,_tmpStatus,_tmpMensagem,_tmpColetasSincronizadas,_tmpColetasFalhadas,_tmpStackTrace,_tmpDuracao);
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
  public Object buscarUltimos(final int limit,
      final Continuation<? super List<SyncLogEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_log ORDER BY dataHora DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTipo = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo");
          final int _cursorIndexOfDataHora = CursorUtil.getColumnIndexOrThrow(_cursor, "dataHora");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMensagem = CursorUtil.getColumnIndexOrThrow(_cursor, "mensagem");
          final int _cursorIndexOfColetasSincronizadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasSincronizadas");
          final int _cursorIndexOfColetasFalhadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasFalhadas");
          final int _cursorIndexOfStackTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "stackTrace");
          final int _cursorIndexOfDuracao = CursorUtil.getColumnIndexOrThrow(_cursor, "duracao");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTipo;
            _tmpTipo = _cursor.getString(_cursorIndexOfTipo);
            final long _tmpDataHora;
            _tmpDataHora = _cursor.getLong(_cursorIndexOfDataHora);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMensagem;
            _tmpMensagem = _cursor.getString(_cursorIndexOfMensagem);
            final int _tmpColetasSincronizadas;
            _tmpColetasSincronizadas = _cursor.getInt(_cursorIndexOfColetasSincronizadas);
            final int _tmpColetasFalhadas;
            _tmpColetasFalhadas = _cursor.getInt(_cursorIndexOfColetasFalhadas);
            final String _tmpStackTrace;
            if (_cursor.isNull(_cursorIndexOfStackTrace)) {
              _tmpStackTrace = null;
            } else {
              _tmpStackTrace = _cursor.getString(_cursorIndexOfStackTrace);
            }
            final long _tmpDuracao;
            _tmpDuracao = _cursor.getLong(_cursorIndexOfDuracao);
            _item = new SyncLogEntity(_tmpId,_tmpTipo,_tmpDataHora,_tmpStatus,_tmpMensagem,_tmpColetasSincronizadas,_tmpColetasFalhadas,_tmpStackTrace,_tmpDuracao);
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
  public Object buscarPorTipo(final String tipo,
      final Continuation<? super List<SyncLogEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_log WHERE tipo = ? ORDER BY dataHora DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, tipo);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTipo = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo");
          final int _cursorIndexOfDataHora = CursorUtil.getColumnIndexOrThrow(_cursor, "dataHora");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMensagem = CursorUtil.getColumnIndexOrThrow(_cursor, "mensagem");
          final int _cursorIndexOfColetasSincronizadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasSincronizadas");
          final int _cursorIndexOfColetasFalhadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasFalhadas");
          final int _cursorIndexOfStackTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "stackTrace");
          final int _cursorIndexOfDuracao = CursorUtil.getColumnIndexOrThrow(_cursor, "duracao");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTipo;
            _tmpTipo = _cursor.getString(_cursorIndexOfTipo);
            final long _tmpDataHora;
            _tmpDataHora = _cursor.getLong(_cursorIndexOfDataHora);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMensagem;
            _tmpMensagem = _cursor.getString(_cursorIndexOfMensagem);
            final int _tmpColetasSincronizadas;
            _tmpColetasSincronizadas = _cursor.getInt(_cursorIndexOfColetasSincronizadas);
            final int _tmpColetasFalhadas;
            _tmpColetasFalhadas = _cursor.getInt(_cursorIndexOfColetasFalhadas);
            final String _tmpStackTrace;
            if (_cursor.isNull(_cursorIndexOfStackTrace)) {
              _tmpStackTrace = null;
            } else {
              _tmpStackTrace = _cursor.getString(_cursorIndexOfStackTrace);
            }
            final long _tmpDuracao;
            _tmpDuracao = _cursor.getLong(_cursorIndexOfDuracao);
            _item = new SyncLogEntity(_tmpId,_tmpTipo,_tmpDataHora,_tmpStatus,_tmpMensagem,_tmpColetasSincronizadas,_tmpColetasFalhadas,_tmpStackTrace,_tmpDuracao);
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
  public Object buscarPorStatus(final String status,
      final Continuation<? super List<SyncLogEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_log WHERE status = ? ORDER BY dataHora DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, status);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTipo = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo");
          final int _cursorIndexOfDataHora = CursorUtil.getColumnIndexOrThrow(_cursor, "dataHora");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMensagem = CursorUtil.getColumnIndexOrThrow(_cursor, "mensagem");
          final int _cursorIndexOfColetasSincronizadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasSincronizadas");
          final int _cursorIndexOfColetasFalhadas = CursorUtil.getColumnIndexOrThrow(_cursor, "coletasFalhadas");
          final int _cursorIndexOfStackTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "stackTrace");
          final int _cursorIndexOfDuracao = CursorUtil.getColumnIndexOrThrow(_cursor, "duracao");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTipo;
            _tmpTipo = _cursor.getString(_cursorIndexOfTipo);
            final long _tmpDataHora;
            _tmpDataHora = _cursor.getLong(_cursorIndexOfDataHora);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMensagem;
            _tmpMensagem = _cursor.getString(_cursorIndexOfMensagem);
            final int _tmpColetasSincronizadas;
            _tmpColetasSincronizadas = _cursor.getInt(_cursorIndexOfColetasSincronizadas);
            final int _tmpColetasFalhadas;
            _tmpColetasFalhadas = _cursor.getInt(_cursorIndexOfColetasFalhadas);
            final String _tmpStackTrace;
            if (_cursor.isNull(_cursorIndexOfStackTrace)) {
              _tmpStackTrace = null;
            } else {
              _tmpStackTrace = _cursor.getString(_cursorIndexOfStackTrace);
            }
            final long _tmpDuracao;
            _tmpDuracao = _cursor.getLong(_cursorIndexOfDuracao);
            _item = new SyncLogEntity(_tmpId,_tmpTipo,_tmpDataHora,_tmpStatus,_tmpMensagem,_tmpColetasSincronizadas,_tmpColetasFalhadas,_tmpStackTrace,_tmpDuracao);
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
  public Object contar(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sync_log";
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
  public Object contarSucessos(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sync_log WHERE status = 'SUCCESS'";
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
  public Object contarErros(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sync_log WHERE status = 'ERROR'";
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
  public Object duracaoMedia(final Continuation<? super Long> $completion) {
    final String _sql = "\n"
            + "        SELECT AVG(duracao) FROM sync_log \n"
            + "        WHERE status = 'SUCCESS' AND duracao > 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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
