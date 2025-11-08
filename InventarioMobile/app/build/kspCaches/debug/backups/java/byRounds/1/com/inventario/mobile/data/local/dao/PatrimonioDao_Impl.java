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
import com.inventario.mobile.data.local.entity.PatrimonioEntity;
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
public final class PatrimonioDao_Impl implements PatrimonioDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PatrimonioEntity> __insertionAdapterOfPatrimonioEntity;

  private final EntityDeletionOrUpdateAdapter<PatrimonioEntity> __deletionAdapterOfPatrimonioEntity;

  private final EntityDeletionOrUpdateAdapter<PatrimonioEntity> __updateAdapterOfPatrimonioEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeletePatrimonioById;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsColetado;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsNaoColetado;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public PatrimonioDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPatrimonioEntity = new EntityInsertionAdapter<PatrimonioEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `patrimonio` (`id`,`codigo`,`descricao`,`marca`,`modelo`,`numeroSerie`,`estado`,`valor`,`salaId`,`salaNome`,`qrCode`,`coletado`,`sincronizado`,`dataCriacao`,`dataAtualizacao`,`servidorId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PatrimonioEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCodigo());
        statement.bindString(3, entity.getDescricao());
        if (entity.getMarca() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getMarca());
        }
        if (entity.getModelo() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getModelo());
        }
        if (entity.getNumeroSerie() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getNumeroSerie());
        }
        if (entity.getEstado() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEstado());
        }
        if (entity.getValor() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getValor());
        }
        if (entity.getSalaId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getSalaId());
        }
        if (entity.getSalaNome() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getSalaNome());
        }
        if (entity.getQrCode() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getQrCode());
        }
        final int _tmp = entity.getColetado() ? 1 : 0;
        statement.bindLong(12, _tmp);
        final int _tmp_1 = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        statement.bindLong(14, entity.getDataCriacao());
        statement.bindLong(15, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getServidorId());
        }
      }
    };
    this.__deletionAdapterOfPatrimonioEntity = new EntityDeletionOrUpdateAdapter<PatrimonioEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `patrimonio` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PatrimonioEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPatrimonioEntity = new EntityDeletionOrUpdateAdapter<PatrimonioEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `patrimonio` SET `id` = ?,`codigo` = ?,`descricao` = ?,`marca` = ?,`modelo` = ?,`numeroSerie` = ?,`estado` = ?,`valor` = ?,`salaId` = ?,`salaNome` = ?,`qrCode` = ?,`coletado` = ?,`sincronizado` = ?,`dataCriacao` = ?,`dataAtualizacao` = ?,`servidorId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PatrimonioEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCodigo());
        statement.bindString(3, entity.getDescricao());
        if (entity.getMarca() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getMarca());
        }
        if (entity.getModelo() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getModelo());
        }
        if (entity.getNumeroSerie() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getNumeroSerie());
        }
        if (entity.getEstado() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEstado());
        }
        if (entity.getValor() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getValor());
        }
        if (entity.getSalaId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getSalaId());
        }
        if (entity.getSalaNome() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getSalaNome());
        }
        if (entity.getQrCode() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getQrCode());
        }
        final int _tmp = entity.getColetado() ? 1 : 0;
        statement.bindLong(12, _tmp);
        final int _tmp_1 = entity.getSincronizado() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        statement.bindLong(14, entity.getDataCriacao());
        statement.bindLong(15, entity.getDataAtualizacao());
        if (entity.getServidorId() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getServidorId());
        }
        statement.bindLong(17, entity.getId());
      }
    };
    this.__preparedStmtOfDeletePatrimonioById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM patrimonio WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsColetado = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patrimonio SET coletado = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsNaoColetado = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patrimonio SET coletado = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM patrimonio";
        return _query;
      }
    };
  }

  @Override
  public Object insertPatrimonio(final PatrimonioEntity patrimonio,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPatrimonioEntity.insertAndReturnId(patrimonio);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPatrimonios(final List<PatrimonioEntity> patrimonios,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfPatrimonioEntity.insertAndReturnIdsList(patrimonios);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePatrimonio(final PatrimonioEntity patrimonio,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPatrimonioEntity.handle(patrimonio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePatrimonio(final PatrimonioEntity patrimonio,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPatrimonioEntity.handle(patrimonio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePatrimonioById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePatrimonioById.acquire();
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
          __preparedStmtOfDeletePatrimonioById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsColetado(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsColetado.acquire();
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
          __preparedStmtOfMarkAsColetado.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsNaoColetado(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsNaoColetado.acquire();
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
          __preparedStmtOfMarkAsNaoColetado.release(_stmt);
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
  public Flow<List<PatrimonioEntity>> getAllPatrimonios() {
    final String _sql = "SELECT * FROM patrimonio ORDER BY codigo ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"patrimonio"}, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _item = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getAllPatrimoniosList(
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio ORDER BY codigo ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _item = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getPatrimonioById(final long id,
      final Continuation<? super PatrimonioEntity> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE id = ?";
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
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _result = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getPatrimonioByCodigo(final String codigo,
      final Continuation<? super PatrimonioEntity> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE codigo = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, codigo);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PatrimonioEntity>() {
      @Override
      @Nullable
      public PatrimonioEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _result = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getPatrimonioByQrCode(final String qrCode,
      final Continuation<? super PatrimonioEntity> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE qrCode = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, qrCode);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PatrimonioEntity>() {
      @Override
      @Nullable
      public PatrimonioEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _result = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getPatrimoniosBySala(final long salaId,
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE salaId = ? ORDER BY codigo ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, salaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _item = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getPatrimoniosNaoColetados(
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE coletado = 0 ORDER BY codigo ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _item = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getPatrimoniosColetados(
      final Continuation<? super List<PatrimonioEntity>> $completion) {
    final String _sql = "SELECT * FROM patrimonio WHERE coletado = 1 ORDER BY codigo ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PatrimonioEntity>>() {
      @Override
      @NonNull
      public List<PatrimonioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCodigo = CursorUtil.getColumnIndexOrThrow(_cursor, "codigo");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfQrCode = CursorUtil.getColumnIndexOrThrow(_cursor, "qrCode");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfSincronizado = CursorUtil.getColumnIndexOrThrow(_cursor, "sincronizado");
          final int _cursorIndexOfDataCriacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataCriacao");
          final int _cursorIndexOfDataAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataAtualizacao");
          final int _cursorIndexOfServidorId = CursorUtil.getColumnIndexOrThrow(_cursor, "servidorId");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCodigo;
            _tmpCodigo = _cursor.getString(_cursorIndexOfCodigo);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final String _tmpMarca;
            if (_cursor.isNull(_cursorIndexOfMarca)) {
              _tmpMarca = null;
            } else {
              _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            }
            final String _tmpModelo;
            if (_cursor.isNull(_cursorIndexOfModelo)) {
              _tmpModelo = null;
            } else {
              _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            }
            final String _tmpNumeroSerie;
            if (_cursor.isNull(_cursorIndexOfNumeroSerie)) {
              _tmpNumeroSerie = null;
            } else {
              _tmpNumeroSerie = _cursor.getString(_cursorIndexOfNumeroSerie);
            }
            final String _tmpEstado;
            if (_cursor.isNull(_cursorIndexOfEstado)) {
              _tmpEstado = null;
            } else {
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            }
            final Double _tmpValor;
            if (_cursor.isNull(_cursorIndexOfValor)) {
              _tmpValor = null;
            } else {
              _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            }
            final Long _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getLong(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
            }
            final String _tmpQrCode;
            if (_cursor.isNull(_cursorIndexOfQrCode)) {
              _tmpQrCode = null;
            } else {
              _tmpQrCode = _cursor.getString(_cursorIndexOfQrCode);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final boolean _tmpSincronizado;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSincronizado);
            _tmpSincronizado = _tmp_1 != 0;
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
            _item = new PatrimonioEntity(_tmpId,_tmpCodigo,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSalaId,_tmpSalaNome,_tmpQrCode,_tmpColetado,_tmpSincronizado,_tmpDataCriacao,_tmpDataAtualizacao,_tmpServidorId);
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
  public Object getCountNaoColetados(final Continuation<? super Integer> $completion) {
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
  public Object getCountColetados(final Continuation<? super Integer> $completion) {
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
  public Object getTotalCount(final Continuation<? super Integer> $completion) {
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
