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
import com.inventario.mobile.data.local.entity.SetorEntity;
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
public final class SetorDao_Impl implements SetorDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SetorEntity> __insertionAdapterOfSetorEntity;

  private final EntityDeletionOrUpdateAdapter<SetorEntity> __deletionAdapterOfSetorEntity;

  private final EntityDeletionOrUpdateAdapter<SetorEntity> __updateAdapterOfSetorEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSetorById;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public SetorDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSetorEntity = new EntityInsertionAdapter<SetorEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `setor` (`id`,`nome`,`descricao`,`sincronizado`,`dataCriacao`,`dataAtualizacao`,`servidorId`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SetorEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        if (entity.getDescricao() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescricao());
        }
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getDataCriacao());
        statement.bindLong(6, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getServidorId());
        }
      }
    };
    this.__deletionAdapterOfSetorEntity = new EntityDeletionOrUpdateAdapter<SetorEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `setor` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SetorEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSetorEntity = new EntityDeletionOrUpdateAdapter<SetorEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `setor` SET `id` = ?,`nome` = ?,`descricao` = ?,`sincronizado` = ?,`dataCriacao` = ?,`dataAtualizacao` = ?,`servidorId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SetorEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        if (entity.getDescricao() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescricao());
        }
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getDataCriacao());
        statement.bindLong(6, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getServidorId());
        }
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSetorById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM setor WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM setor";
        return _query;
      }
    };
  }

  @Override
  public Object insertSetor(final SetorEntity setor, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSetorEntity.insertAndReturnId(setor);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSetores(final List<SetorEntity> setores,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfSetorEntity.insertAndReturnIdsList(setores);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSetor(final SetorEntity setor, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSetorEntity.handle(setor);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSetor(final SetorEntity setor, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSetorEntity.handle(setor);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSetorById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSetorById.acquire();
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
          __preparedStmtOfDeleteSetorById.release(_stmt);
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
  public Flow<List<SetorEntity>> getAllSetores() {
    final String _sql = "SELECT * FROM setor ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"setor"}, new Callable<List<SetorEntity>>() {
      @Override
      @NonNull
      public List<SetorEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<SetorEntity> _result = new ArrayList<SetorEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SetorEntity _item;
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
            _item = new SetorEntity(_tmpId,_tmpNome,_tmpDescricao,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getSetorById(final long id, final Continuation<? super SetorEntity> $completion) {
    final String _sql = "SELECT * FROM setor WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SetorEntity>() {
      @Override
      @Nullable
      public SetorEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final SetorEntity _result;
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
            _result = new SetorEntity(_tmpId,_tmpNome,_tmpDescricao,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getSetoresByNome(final String nome,
      final Continuation<? super List<SetorEntity>> $completion) {
    final String _sql = "SELECT * FROM setor WHERE nome LIKE '%' || ? || '%' ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nome);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SetorEntity>>() {
      @Override
      @NonNull
      public List<SetorEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<SetorEntity> _result = new ArrayList<SetorEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SetorEntity _item;
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
            _item = new SetorEntity(_tmpId,_tmpNome,_tmpDescricao,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getTotalCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM setor";
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
