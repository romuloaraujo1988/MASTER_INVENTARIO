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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.inventario.mobile.data.local.entity.SalaComEstatisticasEntity;
import com.inventario.mobile.data.local.entity.SalaEntity;
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
public final class SalaDao_InventarioDatabase_Impl implements SalaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SalaEntity> __insertionAdapterOfSalaEntity;

  private final SharedSQLiteStatement __preparedStmtOfLimparTodas;

  public SalaDao_InventarioDatabase_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSalaEntity = new EntityInsertionAdapter<SalaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sala` (`id`,`nome`,`idSetor`,`nomeSetor`,`ativa`,`dataUltimaAtualizacao`) VALUES (?,?,?,?,?,?)";
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
        final int _tmp = entity.getAtiva() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getDataUltimaAtualizacao());
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
  public Object upsert(final SalaEntity sala, final Continuation<? super Unit> $completion) {
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
  public Object upsertTodas(final List<SalaEntity> salas,
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
    final String _sql = "SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC";
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
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
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
    final String _sql = "SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC";
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
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorNome(final String query,
      final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "SELECT * FROM sala WHERE ativa = 1 AND nome LIKE ? ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
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
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
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
  public Object buscarPaginado(final int limit, final int offset,
      final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 2;
    _statement.bindLong(_argIndex, offset);
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
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorNomePaginado(final String query, final int limit, final int offset,
      final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "SELECT * FROM sala WHERE ativa = 1 AND nome LIKE ? ORDER BY nome ASC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 3;
    _statement.bindLong(_argIndex, offset);
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
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
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
    final String _sql = "SELECT COUNT(*) FROM sala WHERE ativa = 1";
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
  public Object contarPorNome(final String query, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sala WHERE ativa = 1 AND nome LIKE ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
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
  public Object buscarComEstatisticas(
      final Continuation<? super List<SalaComEstatisticasEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT s.*, \n"
            + "               COUNT(p.id) as total_patrimonios,\n"
            + "               SUM(CASE WHEN p.coletado = 1 THEN 1 ELSE 0 END) as coletados\n"
            + "        FROM sala s\n"
            + "        LEFT JOIN patrimonio p ON p.idSala = s.id\n"
            + "        WHERE s.ativa = 1\n"
            + "        GROUP BY s.id\n"
            + "        ORDER BY s.nome ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SalaComEstatisticasEntity>>() {
      @Override
      @NonNull
      public List<SalaComEstatisticasEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfIdSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "idSetor");
          final int _cursorIndexOfNomeSetor = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSetor");
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final int _cursorIndexOfTotalPatrimonios = CursorUtil.getColumnIndexOrThrow(_cursor, "total_patrimonios");
          final int _cursorIndexOfColetados = CursorUtil.getColumnIndexOrThrow(_cursor, "coletados");
          final List<SalaComEstatisticasEntity> _result = new ArrayList<SalaComEstatisticasEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SalaComEstatisticasEntity _item;
            final int _tmpTotalPatrimonios;
            _tmpTotalPatrimonios = _cursor.getInt(_cursorIndexOfTotalPatrimonios);
            final int _tmpColetados;
            _tmpColetados = _cursor.getInt(_cursorIndexOfColetados);
            final SalaEntity _tmpSala;
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _tmpSala = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
            _item = new SalaComEstatisticasEntity(_tmpSala,_tmpTotalPatrimonios,_tmpColetados);
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
  public Object buscarPorNomeOuNumero(final String query,
      final Continuation<? super List<SalaEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM sala \n"
            + "        WHERE ativa = 1 \n"
            + "        AND (nome LIKE '%' || ? || '%' OR id LIKE '%' || ? || '%')\n"
            + "        ORDER BY nome ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
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
          final int _cursorIndexOfAtiva = CursorUtil.getColumnIndexOrThrow(_cursor, "ativa");
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
            final boolean _tmpAtiva;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAtiva);
            _tmpAtiva = _tmp != 0;
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new SalaEntity(_tmpId,_tmpNome,_tmpIdSetor,_tmpNomeSetor,_tmpAtiva,_tmpDataUltimaAtualizacao);
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
  public Object buscarUltimaAtualizacao(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COALESCE(MAX(dataUltimaAtualizacao), 0) FROM sala";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
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
  public Object deletarPorIds(final List<Integer> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM sala WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (int _item : ids) {
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
