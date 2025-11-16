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
import com.inventario.mobile.data.local.entity.ResponsavelEntity;
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
public final class ResponsavelDao_Impl implements ResponsavelDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ResponsavelEntity> __insertionAdapterOfResponsavelEntity;

  private final SharedSQLiteStatement __preparedStmtOfLimparTodos;

  public ResponsavelDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfResponsavelEntity = new EntityInsertionAdapter<ResponsavelEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `responsavel` (`id`,`nome`,`cpf`,`email`,`dataUltimaAtualizacao`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ResponsavelEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        if (entity.getCpf() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCpf());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEmail());
        }
        statement.bindLong(5, entity.getDataUltimaAtualizacao());
      }
    };
    this.__preparedStmtOfLimparTodos = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM responsavel";
        return _query;
      }
    };
  }

  @Override
  public Object inserir(final ResponsavelEntity responsavel,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfResponsavelEntity.insert(responsavel);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object inserirTodos(final List<ResponsavelEntity> responsaveis,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfResponsavelEntity.insert(responsaveis);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Object buscarTodos(final Continuation<? super List<ResponsavelEntity>> $completion) {
    final String _sql = "SELECT * FROM responsavel ORDER BY nome";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ResponsavelEntity>>() {
      @Override
      @NonNull
      public List<ResponsavelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfCpf = CursorUtil.getColumnIndexOrThrow(_cursor, "cpf");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<ResponsavelEntity> _result = new ArrayList<ResponsavelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ResponsavelEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpCpf;
            if (_cursor.isNull(_cursorIndexOfCpf)) {
              _tmpCpf = null;
            } else {
              _tmpCpf = _cursor.getString(_cursorIndexOfCpf);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new ResponsavelEntity(_tmpId,_tmpNome,_tmpCpf,_tmpEmail,_tmpDataUltimaAtualizacao);
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
  public Flow<List<ResponsavelEntity>> observarTodos() {
    final String _sql = "SELECT * FROM responsavel ORDER BY nome";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"responsavel"}, new Callable<List<ResponsavelEntity>>() {
      @Override
      @NonNull
      public List<ResponsavelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfCpf = CursorUtil.getColumnIndexOrThrow(_cursor, "cpf");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<ResponsavelEntity> _result = new ArrayList<ResponsavelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ResponsavelEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpCpf;
            if (_cursor.isNull(_cursorIndexOfCpf)) {
              _tmpCpf = null;
            } else {
              _tmpCpf = _cursor.getString(_cursorIndexOfCpf);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new ResponsavelEntity(_tmpId,_tmpNome,_tmpCpf,_tmpEmail,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorId(final int id,
      final Continuation<? super ResponsavelEntity> $completion) {
    final String _sql = "SELECT * FROM responsavel WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ResponsavelEntity>() {
      @Override
      @Nullable
      public ResponsavelEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfCpf = CursorUtil.getColumnIndexOrThrow(_cursor, "cpf");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final ResponsavelEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpCpf;
            if (_cursor.isNull(_cursorIndexOfCpf)) {
              _tmpCpf = null;
            } else {
              _tmpCpf = _cursor.getString(_cursorIndexOfCpf);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new ResponsavelEntity(_tmpId,_tmpNome,_tmpCpf,_tmpEmail,_tmpDataUltimaAtualizacao);
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
      final Continuation<? super List<ResponsavelEntity>> $completion) {
    final String _sql = "SELECT * FROM responsavel WHERE nome LIKE '%' || ? || '%' ORDER BY nome";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, termo);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ResponsavelEntity>>() {
      @Override
      @NonNull
      public List<ResponsavelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfCpf = CursorUtil.getColumnIndexOrThrow(_cursor, "cpf");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<ResponsavelEntity> _result = new ArrayList<ResponsavelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ResponsavelEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpCpf;
            if (_cursor.isNull(_cursorIndexOfCpf)) {
              _tmpCpf = null;
            } else {
              _tmpCpf = _cursor.getString(_cursorIndexOfCpf);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new ResponsavelEntity(_tmpId,_tmpNome,_tmpCpf,_tmpEmail,_tmpDataUltimaAtualizacao);
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
    final String _sql = "SELECT COUNT(*) FROM responsavel";
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
