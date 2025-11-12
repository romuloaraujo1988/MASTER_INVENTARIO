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
import com.inventario.mobile.data.local.entity.PatrimonioEntity;
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
public final class PatrimonioDao_AppDatabase_Impl implements PatrimonioDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PatrimonioEntity> __insertionAdapterOfPatrimonioEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarcarComoColetado;

  private final SharedSQLiteStatement __preparedStmtOfLimparTodos;

  public PatrimonioDao_AppDatabase_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPatrimonioEntity = new EntityInsertionAdapter<PatrimonioEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `patrimonio` (`id`,`numero`,`descricao`,`idSala`,`nomeSala`,`idResponsavel`,`nomeResponsavel`,`status`,`coletado`,`dataUltimaAtualizacao`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PatrimonioEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNumero());
        statement.bindString(3, entity.getDescricao());
        if (entity.getIdSala() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getIdSala());
        }
        if (entity.getNomeSala() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getNomeSala());
        }
        if (entity.getIdResponsavel() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getIdResponsavel());
        }
        if (entity.getNomeResponsavel() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getNomeResponsavel());
        }
        statement.bindString(8, entity.getStatus());
        final int _tmp = entity.getColetado() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindLong(10, entity.getDataUltimaAtualizacao());
      }
    };
    this.__preparedStmtOfMarcarComoColetado = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patrimonio SET coletado = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfLimparTodos = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM patrimonio";
        return _query;
      }
    };
  }

  @Override
  public Object inserir(final PatrimonioEntity patrimonio,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPatrimonioEntity.insert(patrimonio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object inserirTodos(final List<PatrimonioEntity> patrimonios,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPatrimonioEntity.insert(patrimonios);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object marcarComoColetado(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarcarComoColetado.acquire();
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
          __preparedStmtOfMarcarComoColetado.release(_stmt);
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
  public Object buscarPorNumero(final String numero,
      final Continuation<? super PatrimonioEntity> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE numero = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, numero);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PatrimonioEntity>() {
      @Override
      @Nullable
      public PatrimonioEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorId(final int id,
      final Continuation<? super PatrimonioEntity> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PatrimonioEntity>() {
      @Override
      @Nullable
      public PatrimonioEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Flow<List<PatrimonioEntity>> observarNaoColetados() {
    final String _sql = "SELECT * FROM patrimonio WHERE coletado = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"patrimonio"}, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Object buscarNaoColetadosPaginado(final int limit, final int offset,
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE coletado = 0 LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 2;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Object buscarDescricoesNaoColetadas(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT DISTINCT descricao FROM patrimonio WHERE coletado = 0 ORDER BY descricao";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
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
  public Object buscarPorDescricaoNaoColetados(final String descricao,
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE descricao = ? AND coletado = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, descricao);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorSalaNaoColetados(final int idSala,
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE idSala = ? AND coletado = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, idSala);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Object contarTodos(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM patrimonio";
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
  public Object contarNaoColetados(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM patrimonio WHERE coletado = 0";
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
  public Object contarColetados(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM patrimonio WHERE coletado = 1";
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
  public Object getAllPatrimoniosList(
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Object syncData(final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE coletado = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final Integer _tmpIdSala;
            if (_cursor.isNull(_cursorIndexOfIdSala)) {
              _tmpIdSala = null;
            } else {
              _tmpIdSala = _cursor.getInt(_cursorIndexOfIdSala);
            }
            final String _tmpNomeSala;
            if (_cursor.isNull(_cursorIndexOfNomeSala)) {
              _tmpNomeSala = null;
            } else {
              _tmpNomeSala = _cursor.getString(_cursorIndexOfNomeSala);
            }
            final Integer _tmpIdResponsavel;
            if (_cursor.isNull(_cursorIndexOfIdResponsavel)) {
              _tmpIdResponsavel = null;
            } else {
              _tmpIdResponsavel = _cursor.getInt(_cursorIndexOfIdResponsavel);
            }
            final String _tmpNomeResponsavel;
            if (_cursor.isNull(_cursorIndexOfNomeResponsavel)) {
              _tmpNomeResponsavel = null;
            } else {
              _tmpNomeResponsavel = _cursor.getString(_cursorIndexOfNomeResponsavel);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpDescricao,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpStatus,_tmpColetado,_tmpDataUltimaAtualizacao);
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
  public Object countByStatus(final String status,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM patrimonio WHERE UPPER(status) = UPPER(?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, status);
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
  public Object getStatusDistribution(final Continuation<? super List<StatusData>> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            COALESCE(status, 'SEM STATUS') as status,\n"
            + "            COUNT(*) as quantidade\n"
            + "        FROM patrimonio\n"
            + "        GROUP BY status\n"
            + "        ORDER BY quantidade DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StatusData>>() {
      @Override
      @NonNull
      public List<StatusData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfStatus = 0;
          final int _cursorIndexOfQuantidade = 1;
          final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StatusData _item;
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final int _tmpQuantidade;
            _tmpQuantidade = _cursor.getInt(_cursorIndexOfQuantidade);
            _item = new StatusData(_tmpStatus,_tmpQuantidade);
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
  public Object getPatrimoniosPorSetor(final Continuation<? super List<SetorData>> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            COALESCE(sa.nomeSetor, 'Sem Setor') as setor,\n"
            + "            COUNT(p.id) as quantidade\n"
            + "        FROM patrimonio p\n"
            + "        LEFT JOIN sala sa ON p.idSala = sa.id\n"
            + "        GROUP BY sa.nomeSetor\n"
            + "        ORDER BY quantidade DESC\n"
            + "        LIMIT 10\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SetorData>>() {
      @Override
      @NonNull
      public List<SetorData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSetor = 0;
          final int _cursorIndexOfQuantidade = 1;
          final List<SetorData> _result = new ArrayList<SetorData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SetorData _item;
            final String _tmpSetor;
            _tmpSetor = _cursor.getString(_cursorIndexOfSetor);
            final int _tmpQuantidade;
            _tmpQuantidade = _cursor.getInt(_cursorIndexOfQuantidade);
            _item = new SetorData(_tmpSetor,_tmpQuantidade);
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
  public Object countAll(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM patrimonio";
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
  public Object getDescricoesFrequentes(final Continuation<? super List<TopItemData>> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            descricao,\n"
            + "            COUNT(*) as quantidade\n"
            + "        FROM patrimonio\n"
            + "        GROUP BY descricao\n"
            + "        ORDER BY quantidade DESC\n"
            + "        LIMIT 10\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TopItemData>>() {
      @Override
      @NonNull
      public List<TopItemData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDescricao = 0;
          final int _cursorIndexOfQuantidade = 1;
          final List<TopItemData> _result = new ArrayList<TopItemData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TopItemData _item;
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final int _tmpQuantidade;
            _tmpQuantidade = _cursor.getInt(_cursorIndexOfQuantidade);
            _item = new TopItemData(_tmpDescricao,_tmpQuantidade);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
