package com.inventario.mobile.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.inventario.mobile.data.local.entity.SincronizacaoEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class SincronizacaoDao_Impl implements SincronizacaoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SincronizacaoEntity> __insertionAdapterOfSincronizacaoEntity;

  private final EntityDeletionOrUpdateAdapter<SincronizacaoEntity> __updateAdapterOfSincronizacaoEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarcarComoSincronizado;

  private final SharedSQLiteStatement __preparedStmtOfRegistrarErro;

  private final SharedSQLiteStatement __preparedStmtOfLimparSincronizados;

  private final SharedSQLiteStatement __preparedStmtOfLimparTodas;

  public SincronizacaoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSincronizacaoEntity = new EntityInsertionAdapter<SincronizacaoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sincronizacao` (`id`,`entidade`,`entidadeId`,`operacao`,`sincronizado`,`dataHora`,`erro`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SincronizacaoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEntidade());
        statement.bindLong(3, entity.getEntidadeId());
        statement.bindString(4, entity.getOperacao());
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getDataHora());
        if (entity.getErro() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getErro());
        }
      }
    };
    this.__updateAdapterOfSincronizacaoEntity = new EntityDeletionOrUpdateAdapter<SincronizacaoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sincronizacao` SET `id` = ?,`entidade` = ?,`entidadeId` = ?,`operacao` = ?,`sincronizado` = ?,`dataHora` = ?,`erro` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SincronizacaoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEntidade());
        statement.bindLong(3, entity.getEntidadeId());
        statement.bindString(4, entity.getOperacao());
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getDataHora());
        if (entity.getErro() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getErro());
        }
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfMarcarComoSincronizado = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sincronizacao SET sincronizado = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRegistrarErro = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sincronizacao SET erro = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfLimparSincronizados = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sincronizacao WHERE sincronizado = 1 AND dataHora < ?";
        return _query;
      }
    };
    this.__preparedStmtOfLimparTodas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sincronizacao";
        return _query;
      }
    };
  }

  @Override
  public Object inserir(final SincronizacaoEntity sincronizacao,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSincronizacaoEntity.insertAndReturnId(sincronizacao);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object inserirTodas(final List<SincronizacaoEntity> sincronizacoes,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSincronizacaoEntity.insert(sincronizacoes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object atualizar(final SincronizacaoEntity sincronizacao,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSincronizacaoEntity.handle(sincronizacao);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object marcarComoSincronizado(final long id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarcarComoSincronizado.acquire();
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
          __preparedStmtOfMarcarComoSincronizado.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object registrarErro(final long id, final String erro,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRegistrarErro.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, erro);
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
          __preparedStmtOfRegistrarErro.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object limparSincronizados(final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLimparSincronizados.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
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
          __preparedStmtOfLimparSincronizados.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object limparTodas(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLimparTodas.acquire();
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
          __preparedStmtOfLimparTodas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object buscarPendentes(final Continuation<? super List<SincronizacaoEntity>> $completion) {
    final String _sql = "SELECT * FROM sincronizacao WHERE sincronizado = 0 ORDER BY dataHora ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SincronizacaoEntity>>() {
      @Override
      @NonNull
      public List<SincronizacaoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEntidade = CursorUtil.getColumnIndexOrThrow(_cursor, "entidade");
          final int _cursorIndexOfEntidadeId = CursorUtil.getColumnIndexOrThrow(_cursor, "entidadeId");
          final int _cursorIndexOfOperacao = CursorUtil.getColumnIndexOrThrow(_cursor, "operacao");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataHora = CursorUtil.getColumnIndexOrThrow(_cursor, "dataHora");
          final int _cursorIndexOfErro = CursorUtil.getColumnIndexOrThrow(_cursor, "erro");
          final List<SincronizacaoEntity> _result = new ArrayList<SincronizacaoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SincronizacaoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEntidade;
            _tmpEntidade = _cursor.getString(_cursorIndexOfEntidade);
            final long _tmpEntidadeId;
            _tmpEntidadeId = _cursor.getLong(_cursorIndexOfEntidadeId);
            final String _tmpOperacao;
            _tmpOperacao = _cursor.getString(_cursorIndexOfOperacao);
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final long _tmpDataHora;
            _tmpDataHora = _cursor.getLong(_cursorIndexOfDataHora);
            final String _tmpErro;
            if (_cursor.isNull(_cursorIndexOfErro)) {
              _tmpErro = null;
            } else {
              _tmpErro = _cursor.getString(_cursorIndexOfErro);
            }
            _item = new SincronizacaoEntity(_tmpId,_tmpEntidade,_tmpEntidadeId,_tmpOperacao,_tmpSincronizado,_tmpDataHora,_tmpErro);
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
  public Flow<List<SincronizacaoEntity>> observarPendentes() {
    final String _sql = "SELECT * FROM sincronizacao WHERE sincronizado = 0 ORDER BY dataHora ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sincronizacao"}, new Callable<List<SincronizacaoEntity>>() {
      @Override
      @NonNull
      public List<SincronizacaoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEntidade = CursorUtil.getColumnIndexOrThrow(_cursor, "entidade");
          final int _cursorIndexOfEntidadeId = CursorUtil.getColumnIndexOrThrow(_cursor, "entidadeId");
          final int _cursorIndexOfOperacao = CursorUtil.getColumnIndexOrThrow(_cursor, "operacao");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataHora = CursorUtil.getColumnIndexOrThrow(_cursor, "dataHora");
          final int _cursorIndexOfErro = CursorUtil.getColumnIndexOrThrow(_cursor, "erro");
          final List<SincronizacaoEntity> _result = new ArrayList<SincronizacaoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SincronizacaoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEntidade;
            _tmpEntidade = _cursor.getString(_cursorIndexOfEntidade);
            final long _tmpEntidadeId;
            _tmpEntidadeId = _cursor.getLong(_cursorIndexOfEntidadeId);
            final String _tmpOperacao;
            _tmpOperacao = _cursor.getString(_cursorIndexOfOperacao);
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final long _tmpDataHora;
            _tmpDataHora = _cursor.getLong(_cursorIndexOfDataHora);
            final String _tmpErro;
            if (_cursor.isNull(_cursorIndexOfErro)) {
              _tmpErro = null;
            } else {
              _tmpErro = _cursor.getString(_cursorIndexOfErro);
            }
            _item = new SincronizacaoEntity(_tmpId,_tmpEntidade,_tmpEntidadeId,_tmpOperacao,_tmpSincronizado,_tmpDataHora,_tmpErro);
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
  public Object getUltimaSincronizacao(
      final Continuation<? super SincronizacaoEntity> $completion) {
    final String _sql = "SELECT * FROM sincronizacao ORDER BY dataHora DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SincronizacaoEntity>() {
      @Override
      @Nullable
      public SincronizacaoEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEntidade = CursorUtil.getColumnIndexOrThrow(_cursor, "entidade");
          final int _cursorIndexOfEntidadeId = CursorUtil.getColumnIndexOrThrow(_cursor, "entidadeId");
          final int _cursorIndexOfOperacao = CursorUtil.getColumnIndexOrThrow(_cursor, "operacao");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataHora = CursorUtil.getColumnIndexOrThrow(_cursor, "dataHora");
          final int _cursorIndexOfErro = CursorUtil.getColumnIndexOrThrow(_cursor, "erro");
          final SincronizacaoEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEntidade;
            _tmpEntidade = _cursor.getString(_cursorIndexOfEntidade);
            final long _tmpEntidadeId;
            _tmpEntidadeId = _cursor.getLong(_cursorIndexOfEntidadeId);
            final String _tmpOperacao;
            _tmpOperacao = _cursor.getString(_cursorIndexOfOperacao);
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final long _tmpDataHora;
            _tmpDataHora = _cursor.getLong(_cursorIndexOfDataHora);
            final String _tmpErro;
            if (_cursor.isNull(_cursorIndexOfErro)) {
              _tmpErro = null;
            } else {
              _tmpErro = _cursor.getString(_cursorIndexOfErro);
            }
            _result = new SincronizacaoEntity(_tmpId,_tmpEntidade,_tmpEntidadeId,_tmpOperacao,_tmpSincronizado,_tmpDataHora,_tmpErro);
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
  public Object contarPendentes(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sincronizacao WHERE sincronizado = 0";
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
  public Object marcarComoSincronizados(final List<Long> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE sincronizacao SET sincronizado = 1, erro = NULL WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
