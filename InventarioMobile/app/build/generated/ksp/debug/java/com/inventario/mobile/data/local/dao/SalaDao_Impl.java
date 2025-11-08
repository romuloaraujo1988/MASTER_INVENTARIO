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
import androidx.sqlite.db.SupportSQLiteStatement;
import com.inventario.mobile.data.local.entity.SalaEntity;
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
public final class SalaDao_Impl implements SalaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SalaEntity> __insertionAdapterOfSalaEntity;

  private final EntityDeletionOrUpdateAdapter<SalaEntity> __deletionAdapterOfSalaEntity;

  private final EntityDeletionOrUpdateAdapter<SalaEntity> __updateAdapterOfSalaEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSalaById;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public SalaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSalaEntity = new EntityInsertionAdapter<SalaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sala` (`id`,`nome`,`descricao`,`setorId`,`setorNome`,`sincronizado`,`dataCriacao`,`dataAtualizacao`,`servidorId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SalaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        if (entity.getDescricao() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescricao());
        }
        statement.bindLong(4, entity.getSetorId());
        if (entity.getSetorNome() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getSetorNome());
        }
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getDataCriacao());
        statement.bindLong(8, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getServidorId());
        }
      }
    };
    this.__deletionAdapterOfSalaEntity = new EntityDeletionOrUpdateAdapter<SalaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sala` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SalaEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSalaEntity = new EntityDeletionOrUpdateAdapter<SalaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sala` SET `id` = ?,`nome` = ?,`descricao` = ?,`setorId` = ?,`setorNome` = ?,`sincronizado` = ?,`dataCriacao` = ?,`dataAtualizacao` = ?,`servidorId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SalaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        if (entity.getDescricao() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescricao());
        }
        statement.bindLong(4, entity.getSetorId());
        if (entity.getSetorNome() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getSetorNome());
        }
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getDataCriacao());
        statement.bindLong(8, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getServidorId());
        }
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSalaById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sala WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sala";
        return _query;
      }
    };
  }

  @Override
  public Object insertSala(final SalaEntity sala, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSalaEntity.insertAndReturnId(sala);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSalas(final List<SalaEntity> salas,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfSalaEntity.insertAndReturnIdsList(salas);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSala(final SalaEntity sala, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSalaEntity.handle(sala);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSala(final SalaEntity sala, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSalaEntity.handle(sala);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSalaById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSalaById.acquire();
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
          __preparedStmtOfDeleteSalaById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SalaEntity>> getAllSalas() {
    final String _sql = "SELECT * FROM sala ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sala"}, new Callable<List<SalaEntity>>() {
      @Override
      @NonNull
      public List<SalaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<SalaEntity> _result = new ArrayList<SalaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SalaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpDescricao;
            if (_cursor.isNull(_cursorIndexOfDescricao)) {
              _tmpDescricao = null;
            } else {
              _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            }
            final long _tmpSetorId;
            _tmpSetorId = _cursor.getLong(_cursorIndexOfSetorId);
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
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
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpDescricao,_tmpSetorId,_tmpSetorNome,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getSalaById(final long id, final Continuation<? super SalaEntity> $completion) {
    final String _sql = "SELECT * FROM sala WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SalaEntity>() {
      @Override
      @Nullable
      public SalaEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final SalaEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpDescricao;
            if (_cursor.isNull(_cursorIndexOfDescricao)) {
              _tmpDescricao = null;
            } else {
              _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            }
            final long _tmpSetorId;
            _tmpSetorId = _cursor.getLong(_cursorIndexOfSetorId);
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
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
            _result = new SalaEntity(_tmpId,_tmpNome,_tmpDescricao,_tmpSetorId,_tmpSetorNome,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getSalasBySetor(final long setorId,
      final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "SELECT * FROM sala WHERE setorId = ? ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, setorId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SalaEntity>>() {
      @Override
      @NonNull
      public List<SalaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<SalaEntity> _result = new ArrayList<SalaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SalaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpDescricao;
            if (_cursor.isNull(_cursorIndexOfDescricao)) {
              _tmpDescricao = null;
            } else {
              _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            }
            final long _tmpSetorId;
            _tmpSetorId = _cursor.getLong(_cursorIndexOfSetorId);
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
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
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpDescricao,_tmpSetorId,_tmpSetorNome,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getSalasByNome(final String nome,
      final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "SELECT * FROM sala WHERE nome LIKE '%' || ? || '%' ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nome);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SalaEntity>>() {
      @Override
      @NonNull
      public List<SalaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<SalaEntity> _result = new ArrayList<SalaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SalaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpDescricao;
            if (_cursor.isNull(_cursorIndexOfDescricao)) {
              _tmpDescricao = null;
            } else {
              _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            }
            final long _tmpSetorId;
            _tmpSetorId = _cursor.getLong(_cursorIndexOfSetorId);
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
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
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpDescricao,_tmpSetorId,_tmpSetorNome,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getCountBySetor(final long setorId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sala WHERE setorId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, setorId);
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
    final String _sql = "SELECT COUNT(*) FROM sala";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
