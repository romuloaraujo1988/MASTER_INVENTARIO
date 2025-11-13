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
import com.inventario.mobile.data.local.entity.SalaEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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

  private final SharedSQLiteStatement __preparedStmtOfLimparTodas;

  public SalaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSalaEntity = new EntityInsertionAdapter<SalaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sala` (`id`,`nome`,`idSetor`,`nomeSetor`,`dataUltimaAtualizacao`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SalaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        if (entity.getIdSetor() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getIdSetor());
        }
        if (entity.getNomeSetor() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getNomeSetor());
        }
        statement.bindLong(5, entity.getDataUltimaAtualizacao());
      }
    };
    this.__preparedStmtOfLimparTodas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sala";
        return _query;
      }
    };
  }

  @Override
  public Object inserir(final SalaEntity sala, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSalaEntity.insert(sala);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object inserirTodas(final List<SalaEntity> salas,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSalaEntity.insert(salas);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Object buscarTodas(final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "SELECT * FROM sala ORDER BY nome";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SalaEntity>>() {
      @Override
      @NonNull
      public List<SalaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfIdSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "idSetor");
          final int _cursorIndexOfNomeSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSetor");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<SalaEntity> _result = new ArrayList<SalaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SalaEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final Integer _tmpIdSetor;
            if (_cursor.isNull(_cursorIndexOfIdSetor)) {
              _tmpIdSetor = null;
            } else {
              _tmpIdSetor = _cursor.getInt(_cursorIndexOfIdSetor);
            }
            final String _tmpNomeSetor;
            if (_cursor.isNull(_cursorIndexOfNomeSetor)) {
              _tmpNomeSetor = null;
            } else {
              _tmpNomeSetor = _cursor.getString(_cursorIndexOfNomeSetor);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpDataUltimaAtualizacao);
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
  public Flow<List<SalaEntity>> observarTodas() {
    final String _sql = "SELECT * FROM sala ORDER BY nome";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sala"}, new Callable<List<SalaEntity>>() {
      @Override
      @NonNull
      public List<SalaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfIdSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "idSetor");
          final int _cursorIndexOfNomeSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSetor");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<SalaEntity> _result = new ArrayList<SalaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SalaEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final Integer _tmpIdSetor;
            if (_cursor.isNull(_cursorIndexOfIdSetor)) {
              _tmpIdSetor = null;
            } else {
              _tmpIdSetor = _cursor.getInt(_cursorIndexOfIdSetor);
            }
            final String _tmpNomeSetor;
            if (_cursor.isNull(_cursorIndexOfNomeSetor)) {
              _tmpNomeSetor = null;
            } else {
              _tmpNomeSetor = _cursor.getString(_cursorIndexOfNomeSetor);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorId(final int id, final Continuation<? super SalaEntity> $completion) {
    final String _sql = "SELECT * FROM sala WHERE id = ? LIMIT 1";
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
          final int _cursorIndexOfIdSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "idSetor");
          final int _cursorIndexOfNomeSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSetor");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final SalaEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final Integer _tmpIdSetor;
            if (_cursor.isNull(_cursorIndexOfIdSetor)) {
              _tmpIdSetor = null;
            } else {
              _tmpIdSetor = _cursor.getInt(_cursorIndexOfIdSetor);
            }
            final String _tmpNomeSetor;
            if (_cursor.isNull(_cursorIndexOfNomeSetor)) {
              _tmpNomeSetor = null;
            } else {
              _tmpNomeSetor = _cursor.getString(_cursorIndexOfNomeSetor);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorNome(final String termo,
      final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "SELECT * FROM sala WHERE nome LIKE '%' || ? || '%' ORDER BY nome";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, termo);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SalaEntity>>() {
      @Override
      @NonNull
      public List<SalaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfIdSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "idSetor");
          final int _cursorIndexOfNomeSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSetor");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<SalaEntity> _result = new ArrayList<SalaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SalaEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final Integer _tmpIdSetor;
            if (_cursor.isNull(_cursorIndexOfIdSetor)) {
              _tmpIdSetor = null;
            } else {
              _tmpIdSetor = _cursor.getInt(_cursorIndexOfIdSetor);
            }
            final String _tmpNomeSetor;
            if (_cursor.isNull(_cursorIndexOfNomeSetor)) {
              _tmpNomeSetor = null;
            } else {
              _tmpNomeSetor = _cursor.getString(_cursorIndexOfNomeSetor);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpDataUltimaAtualizacao);
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
