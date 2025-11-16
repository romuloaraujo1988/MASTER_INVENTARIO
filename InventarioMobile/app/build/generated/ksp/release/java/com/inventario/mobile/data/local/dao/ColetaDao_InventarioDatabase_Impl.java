package com.inventario.mobile.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
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
public final class ColetaDao_InventarioDatabase_Impl implements ColetaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ColetaEntity> __insertionAdapterOfColetaEntity;

  private final SharedSQLiteStatement __preparedStmtOfAtualizarSincronizado;

  private final SharedSQLiteStatement __preparedStmtOfMarcarSincronizada;

  private final SharedSQLiteStatement __preparedStmtOfRegistrarErroSincronizacao;

  private final SharedSQLiteStatement __preparedStmtOfDeletar;

  private final SharedSQLiteStatement __preparedStmtOfLimparSincronizadas;

  private final SharedSQLiteStatement __preparedStmtOfLimparSincronizadasAntigas;

  public ColetaDao_InventarioDatabase_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfColetaEntity = new EntityInsertionAdapter<ColetaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `coleta` (`id`,`idPatrimonio`,`numeroPatrimonio`,`idInventario`,`idSala`,`nomeSala`,`idResponsavel`,`nomeResponsavel`,`observacao`,`estadoPatrimonio`,`latitude`,`longitude`,`dataColeta`,`idUsuario`,`nomeUsuario`,`sincronizado`,`tentativasSincronizacao`,`erroSincronizacao`,`servidorId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ColetaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getIdPatrimonio());
        statement.bindString(3, entity.getNumeroPatrimonio());
        statement.bindLong(4, entity.getIdInventario());
        if (entity.getIdSala() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getIdSala());
        }
        if (entity.getNomeSala() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getNomeSala());
        }
        if (entity.getIdResponsavel() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getIdResponsavel());
        }
        if (entity.getNomeResponsavel() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNomeResponsavel());
        }
        if (entity.getObservacao() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getObservacao());
        }
        if (entity.getEstadoPatrimonio() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getEstadoPatrimonio());
        }
        if (entity.getLatitude() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getLongitude());
        }
        statement.bindLong(13, entity.getDataColeta());
        statement.bindLong(14, entity.getIdUsuario());
        statement.bindString(15, entity.getNomeUsuario());
        final int _tmp = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(16, _tmp);
        statement.bindLong(17, entity.getTentativasSincronizacao());
        if (entity.getErroSincronizacao() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getErroSincronizacao());
        }
        if (entity.getServidorId() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getServidorId());
        }
      }
    };
    this.__preparedStmtOfAtualizarSincronizado = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE coleta SET sincronizado = ?, servidorId = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarcarSincronizada = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE coleta SET sincronizado = 1, servidorId = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRegistrarErroSincronizacao = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE coleta SET tentativasSincronizacao = tentativasSincronizacao + 1, erroSincronizacao = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletar = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM coleta WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfLimparSincronizadas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM coleta WHERE sincronizado = 1";
        return _query;
      }
    };
    this.__preparedStmtOfLimparSincronizadasAntigas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM coleta WHERE sincronizado = 1 AND dataColeta < ?";
        return _query;
      }
    };
  }

  @Override
  public Object inserir(final ColetaEntity coleta, final Continuation<? super Long> $completion) {
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
  public Object atualizarSincronizado(final long id, final boolean sincronizado,
      final int servidorId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAtualizarSincronizado.acquire();
        int _argIndex = 1;
        final int _tmp = sincronizado ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, servidorId);
        _argIndex = 3;
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
          __preparedStmtOfAtualizarSincronizado.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object marcarSincronizada(final long id, final Long servidorId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarcarSincronizada.acquire();
        int _argIndex = 1;
        if (servidorId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, servidorId);
        }
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
          __preparedStmtOfMarcarSincronizada.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object registrarErroSincronizacao(final long id, final String erro,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRegistrarErroSincronizacao.acquire();
        int _argIndex = 1;
        if (erro == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, erro);
        }
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
          __preparedStmtOfRegistrarErroSincronizacao.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletar(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletar.acquire();
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
          __preparedStmtOfDeletar.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object limparSincronizadas(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLimparSincronizadas.acquire();
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
          __preparedStmtOfLimparSincronizadas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object limparSincronizadasAntigas(final long timestamp,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLimparSincronizadasAntigas.acquire();
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
          __preparedStmtOfLimparSincronizadasAntigas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object buscarPendentes(final Continuation<? super List<ColetaEntity>> $completion) {
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
          final int _cursorIndexOfIdPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "idPatrimonio");
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfIdInventario = CursorUtil.getColumnIndexOrThrow(_cursor, "idInventario");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfObservacao = CursorUtil.getColumnIndexOrThrow(_cursor, "observacao");
          final int _cursorIndexOfEstadoPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoPatrimonio");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfIdUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "idUsuario");
          final int _cursorIndexOfNomeUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeUsuario");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final int _tmpIdPatrimonio;
            _tmpIdPatrimonio = _cursor.getInt(_cursorIndexOfIdPatrimonio);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
            final int _tmpIdInventario;
            _tmpIdInventario = _cursor.getInt(_cursorIndexOfIdInventario);
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
            final String _tmpObservacao;
            if (_cursor.isNull(_cursorIndexOfObservacao)) {
              _tmpObservacao = null;
            } else {
              _tmpObservacao = _cursor.getString(_cursorIndexOfObservacao);
            }
            final String _tmpEstadoPatrimonio;
            if (_cursor.isNull(_cursorIndexOfEstadoPatrimonio)) {
              _tmpEstadoPatrimonio = null;
            } else {
              _tmpEstadoPatrimonio = _cursor.getString(_cursorIndexOfEstadoPatrimonio);
            }
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
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final int _tmpIdUsuario;
            _tmpIdUsuario = _cursor.getInt(_cursorIndexOfIdUsuario);
            final String _tmpNomeUsuario;
            _tmpNomeUsuario = _cursor.getString(_cursorIndexOfNomeUsuario);
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            _item = new ColetaEntity(_tmpId,_tmpIdPatrimonio,_tmpNumeroPatrimonio,_tmpIdInventario,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpObservacao,_tmpEstadoPatrimonio,_tmpLatitude,_tmpLongitude,_tmpDataColeta,_tmpIdUsuario,_tmpNomeUsuario,_tmpSincronizado,_tmpTentativasSincronizacao,_tmpErroSincronizacao,_tmpServidorId);
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
  public Flow<List<ColetaEntity>> observarPendentes() {
    final String _sql = "SELECT * FROM coleta WHERE sincronizado = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"coleta"}, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "idPatrimonio");
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfIdInventario = CursorUtil.getColumnIndexOrThrow(_cursor, "idInventario");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfObservacao = CursorUtil.getColumnIndexOrThrow(_cursor, "observacao");
          final int _cursorIndexOfEstadoPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoPatrimonio");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfIdUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "idUsuario");
          final int _cursorIndexOfNomeUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeUsuario");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final int _tmpIdPatrimonio;
            _tmpIdPatrimonio = _cursor.getInt(_cursorIndexOfIdPatrimonio);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
            final int _tmpIdInventario;
            _tmpIdInventario = _cursor.getInt(_cursorIndexOfIdInventario);
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
            final String _tmpObservacao;
            if (_cursor.isNull(_cursorIndexOfObservacao)) {
              _tmpObservacao = null;
            } else {
              _tmpObservacao = _cursor.getString(_cursorIndexOfObservacao);
            }
            final String _tmpEstadoPatrimonio;
            if (_cursor.isNull(_cursorIndexOfEstadoPatrimonio)) {
              _tmpEstadoPatrimonio = null;
            } else {
              _tmpEstadoPatrimonio = _cursor.getString(_cursorIndexOfEstadoPatrimonio);
            }
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
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final int _tmpIdUsuario;
            _tmpIdUsuario = _cursor.getInt(_cursorIndexOfIdUsuario);
            final String _tmpNomeUsuario;
            _tmpNomeUsuario = _cursor.getString(_cursorIndexOfNomeUsuario);
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            _item = new ColetaEntity(_tmpId,_tmpIdPatrimonio,_tmpNumeroPatrimonio,_tmpIdInventario,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpObservacao,_tmpEstadoPatrimonio,_tmpLatitude,_tmpLongitude,_tmpDataColeta,_tmpIdUsuario,_tmpNomeUsuario,_tmpSincronizado,_tmpTentativasSincronizacao,_tmpErroSincronizacao,_tmpServidorId);
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
  public Object buscarTodas(final Continuation<? super List<ColetaEntity>> $completion) {
    final String _sql = "SELECT * FROM coleta ORDER BY dataColeta DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "idPatrimonio");
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfIdInventario = CursorUtil.getColumnIndexOrThrow(_cursor, "idInventario");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfObservacao = CursorUtil.getColumnIndexOrThrow(_cursor, "observacao");
          final int _cursorIndexOfEstadoPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoPatrimonio");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfIdUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "idUsuario");
          final int _cursorIndexOfNomeUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeUsuario");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final int _tmpIdPatrimonio;
            _tmpIdPatrimonio = _cursor.getInt(_cursorIndexOfIdPatrimonio);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
            final int _tmpIdInventario;
            _tmpIdInventario = _cursor.getInt(_cursorIndexOfIdInventario);
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
            final String _tmpObservacao;
            if (_cursor.isNull(_cursorIndexOfObservacao)) {
              _tmpObservacao = null;
            } else {
              _tmpObservacao = _cursor.getString(_cursorIndexOfObservacao);
            }
            final String _tmpEstadoPatrimonio;
            if (_cursor.isNull(_cursorIndexOfEstadoPatrimonio)) {
              _tmpEstadoPatrimonio = null;
            } else {
              _tmpEstadoPatrimonio = _cursor.getString(_cursorIndexOfEstadoPatrimonio);
            }
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
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final int _tmpIdUsuario;
            _tmpIdUsuario = _cursor.getInt(_cursorIndexOfIdUsuario);
            final String _tmpNomeUsuario;
            _tmpNomeUsuario = _cursor.getString(_cursorIndexOfNomeUsuario);
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            _item = new ColetaEntity(_tmpId,_tmpIdPatrimonio,_tmpNumeroPatrimonio,_tmpIdInventario,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpObservacao,_tmpEstadoPatrimonio,_tmpLatitude,_tmpLongitude,_tmpDataColeta,_tmpIdUsuario,_tmpNomeUsuario,_tmpSincronizado,_tmpTentativasSincronizacao,_tmpErroSincronizacao,_tmpServidorId);
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
  public Flow<Integer> observarQuantidadePendentes() {
    final String _sql = "SELECT COUNT(*) FROM coleta WHERE sincronizado = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"coleta"}, new Callable<Integer>() {
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
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object contarPendentes(final Continuation<? super Integer> $completion) {
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
  public Object buscarRecentes(final int limit,
      final Continuation<? super List<ColetaEntity>> $completion) {
    final String _sql = "SELECT * FROM coleta ORDER BY dataColeta DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ColetaEntity>>() {
      @Override
      @NonNull
      public List<ColetaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "idPatrimonio");
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfIdInventario = CursorUtil.getColumnIndexOrThrow(_cursor, "idInventario");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfObservacao = CursorUtil.getColumnIndexOrThrow(_cursor, "observacao");
          final int _cursorIndexOfEstadoPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoPatrimonio");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfIdUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "idUsuario");
          final int _cursorIndexOfNomeUsuario = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeUsuario");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfTentativasSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "tentativasSincronizacao");
          final int _cursorIndexOfErroSincronizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "erroSincronizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<ColetaEntity> _result = new ArrayList<ColetaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ColetaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final int _tmpIdPatrimonio;
            _tmpIdPatrimonio = _cursor.getInt(_cursorIndexOfIdPatrimonio);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
            final int _tmpIdInventario;
            _tmpIdInventario = _cursor.getInt(_cursorIndexOfIdInventario);
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
            final String _tmpObservacao;
            if (_cursor.isNull(_cursorIndexOfObservacao)) {
              _tmpObservacao = null;
            } else {
              _tmpObservacao = _cursor.getString(_cursorIndexOfObservacao);
            }
            final String _tmpEstadoPatrimonio;
            if (_cursor.isNull(_cursorIndexOfEstadoPatrimonio)) {
              _tmpEstadoPatrimonio = null;
            } else {
              _tmpEstadoPatrimonio = _cursor.getString(_cursorIndexOfEstadoPatrimonio);
            }
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
            final long _tmpDataColeta;
            _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            final int _tmpIdUsuario;
            _tmpIdUsuario = _cursor.getInt(_cursorIndexOfIdUsuario);
            final String _tmpNomeUsuario;
            _tmpNomeUsuario = _cursor.getString(_cursorIndexOfNomeUsuario);
            final boolean _tmpSincronizado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp != 0;
            final int _tmpTentativasSincronizacao;
            _tmpTentativasSincronizacao = _cursor.getInt(_cursorIndexOfTentativasSincronizacao);
            final String _tmpErroSincronizacao;
            if (_cursor.isNull(_cursorIndexOfErroSincronizacao)) {
              _tmpErroSincronizacao = null;
            } else {
              _tmpErroSincronizacao = _cursor.getString(_cursorIndexOfErroSincronizacao);
            }
            final Long _tmpServidorId;
            if (_cursor.isNull(_cursorIndexOfServidorId)) {
              _tmpServidorId = null;
            } else {
              _tmpServidorId = _cursor.getLong(_cursorIndexOfServidorId);
            }
            _item = new ColetaEntity(_tmpId,_tmpIdPatrimonio,_tmpNumeroPatrimonio,_tmpIdInventario,_tmpIdSala,_tmpNomeSala,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpObservacao,_tmpEstadoPatrimonio,_tmpLatitude,_tmpLongitude,_tmpDataColeta,_tmpIdUsuario,_tmpNomeUsuario,_tmpSincronizado,_tmpTentativasSincronizacao,_tmpErroSincronizacao,_tmpServidorId);
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
  public Object contarTodas(final Continuation<? super Integer> $completion) {
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
  public Object countByInventario(final int idInventario,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(DISTINCT idPatrimonio) FROM coleta WHERE idInventario = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, idInventario);
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
  public Object getEvolutionData(final int idInventario,
      final Continuation<? super List<EvolutionData>> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            strftime('%d/%m', dataColeta / 1000, 'unixepoch') as data,\n"
            + "            COUNT(*) as quantidade\n"
            + "        FROM coleta\n"
            + "        WHERE idInventario = ?\n"
            + "        GROUP BY date(dataColeta / 1000, 'unixepoch')\n"
            + "        ORDER BY date(dataColeta / 1000, 'unixepoch') ASC\n"
            + "        LIMIT 30\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, idInventario);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EvolutionData>>() {
      @Override
      @NonNull
      public List<EvolutionData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfData = 0;
          final int _cursorIndexOfQuantidade = 1;
          final List<EvolutionData> _result = new ArrayList<EvolutionData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EvolutionData _item;
            final String _tmpData;
            _tmpData = _cursor.getString(_cursorIndexOfData);
            final int _tmpQuantidade;
            _tmpQuantidade = _cursor.getInt(_cursorIndexOfQuantidade);
            _item = new EvolutionData(_tmpData,_tmpQuantidade);
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
  public Object getTopItems(final int idInventario,
      final Continuation<? super List<TopItemData>> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            p.descricao as descricao,\n"
            + "            COUNT(c.id) as quantidade\n"
            + "        FROM coleta c\n"
            + "        INNER JOIN patrimonio p ON c.idPatrimonio = p.id\n"
            + "        WHERE c.idInventario = ?\n"
            + "        GROUP BY p.descricao\n"
            + "        ORDER BY quantidade DESC\n"
            + "        LIMIT 10\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, idInventario);
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
