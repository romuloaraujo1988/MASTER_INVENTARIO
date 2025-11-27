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
public final class PatrimonioDao_InventarioDatabase_Impl implements PatrimonioDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PatrimonioEntity> __insertionAdapterOfPatrimonioEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarcarComoColetado;

  private final SharedSQLiteStatement __preparedStmtOfLimparTodos;

  public PatrimonioDao_InventarioDatabase_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPatrimonioEntity = new EntityInsertionAdapter<PatrimonioEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `patrimonio` (`id`,`numero`,`numeroPatrimonio`,`descricao`,`marca`,`modelo`,`numeroSerie`,`estado`,`valor`,`setorId`,`setorNome`,`idSala`,`nomeSala`,`salaId`,`salaNome`,`idResponsavel`,`nomeResponsavel`,`responsavelId`,`responsavelNome`,`status`,`coletado`,`dataColeta`,`coletadoPor`,`observacoesColeta`,`observacoes`,`dataUltimaAtualizacao`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PatrimonioEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNumero());
        statement.bindString(3, entity.getNumeroPatrimonio());
        statement.bindString(4, entity.getDescricao());
        if (entity.getMarca() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMarca());
        }
        if (entity.getModelo() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getModelo());
        }
        if (entity.getNumeroSerie() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getNumeroSerie());
        }
        if (entity.getEstado() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getEstado());
        }
        if (entity.getValor() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getValor());
        }
        if (entity.getSetorId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getSetorId());
        }
        if (entity.getSetorNome() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getSetorNome());
        }
        if (entity.getIdSala() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getIdSala());
        }
        if (entity.getNomeSala() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getNomeSala());
        }
        if (entity.getSalaId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getSalaId());
        }
        if (entity.getSalaNome() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getSalaNome());
        }
        if (entity.getIdResponsavel() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getIdResponsavel());
        }
        if (entity.getNomeResponsavel() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getNomeResponsavel());
        }
        if (entity.getResponsavelId() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getResponsavelId());
        }
        if (entity.getResponsavelNome() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getResponsavelNome());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getStatus());
        }
        final int _tmp = entity.getColetado() ? 1 : 0;
        statement.bindLong(21, _tmp);
        if (entity.getDataColeta() == null) {
          statement.bindNull(22);
        } else {
          statement.bindLong(22, entity.getDataColeta());
        }
        if (entity.getColetadoPor() == null) {
          statement.bindNull(23);
        } else {
          statement.bindString(23, entity.getColetadoPor());
        }
        if (entity.getObservacoesColeta() == null) {
          statement.bindNull(24);
        } else {
          statement.bindString(24, entity.getObservacoesColeta());
        }
        if (entity.getObservacoes() == null) {
          statement.bindNull(25);
        } else {
          statement.bindString(25, entity.getObservacoes());
        }
        statement.bindLong(26, entity.getDataUltimaAtualizacao());
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
  public Object marcarComoColetado(final long id, final Continuation<? super Unit> $completion) {
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
    final String _sql = "SELECT * FROM patrimonio WHERE numeroPatrimonio = ? LIMIT 1";
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
  public Object buscarPorNumeroComStatusColeta(final String numero, final int inventarioId,
      final Continuation<? super PatrimonioEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT p.*, \n"
            + "               s.nome as salaNome,\n"
            + "               s.nome as nomeSala,\n"
            + "               CASE WHEN c.id IS NOT NULL THEN 1 ELSE 0 END as coletado,\n"
            + "               c.nomeUsuario as coletadoPor,\n"
            + "               c.dataColeta as dataColeta\n"
            + "        FROM patrimonio p\n"
            + "        LEFT JOIN sala s ON s.id = p.idSala\n"
            + "        LEFT JOIN coleta c ON c.idPatrimonio = p.id \n"
            + "                           AND c.idInventario = ?\n"
            + "        WHERE p.numeroPatrimonio = ?\n"
            + "        LIMIT 1\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, inventarioId);
    _argIndex = 2;
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final PatrimonioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _result = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
          final int _cursorIndexOfNumeroPatrimonio = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPatrimonio");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfNumeroSerie = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroSerie");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfSetorId = CursorUtil.getColumnIndexOrThrow(_cursor, "setorId");
          final int _cursorIndexOfSetorNome = CursorUtil.getColumnIndexOrThrow(_cursor, "setorNome");
          final int _cursorIndexOfIdSala = CursorUtil.getColumnIndexOrThrow(_cursor, "idSala");
          final int _cursorIndexOfNomeSala = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeSala");
          final int _cursorIndexOfSalaId = CursorUtil.getColumnIndexOrThrow(_cursor, "salaId");
          final int _cursorIndexOfSalaNome = CursorUtil.getColumnIndexOrThrow(_cursor, "salaNome");
          final int _cursorIndexOfIdResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "idResponsavel");
          final int _cursorIndexOfNomeResponsavel = CursorUtil.getColumnIndexOrThrow(_cursor, "nomeResponsavel");
          final int _cursorIndexOfResponsavelId = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelId");
          final int _cursorIndexOfResponsavelNome = CursorUtil.getColumnIndexOrThrow(_cursor, "responsavelNome");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfColetado = CursorUtil.getColumnIndexOrThrow(_cursor, "coletado");
          final int _cursorIndexOfDataColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "dataColeta");
          final int _cursorIndexOfColetadoPor = CursorUtil.getColumnIndexOrThrow(_cursor, "coletadoPor");
          final int _cursorIndexOfObservacoesColeta = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoesColeta");
          final int _cursorIndexOfObservacoes = CursorUtil.getColumnIndexOrThrow(_cursor, "observacoes");
          final int _cursorIndexOfDataUltimaAtualizacao = CursorUtil.getColumnIndexOrThrow(_cursor, "dataUltimaAtualizacao");
          final List<PatrimonioEntity> _result = new ArrayList<PatrimonioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatrimonioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumero;
            _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
            final String _tmpNumeroPatrimonio;
            _tmpNumeroPatrimonio = _cursor.getString(_cursorIndexOfNumeroPatrimonio);
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
            final Integer _tmpSetorId;
            if (_cursor.isNull(_cursorIndexOfSetorId)) {
              _tmpSetorId = null;
            } else {
              _tmpSetorId = _cursor.getInt(_cursorIndexOfSetorId);
            }
            final String _tmpSetorNome;
            if (_cursor.isNull(_cursorIndexOfSetorNome)) {
              _tmpSetorNome = null;
            } else {
              _tmpSetorNome = _cursor.getString(_cursorIndexOfSetorNome);
            }
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
            final Integer _tmpSalaId;
            if (_cursor.isNull(_cursorIndexOfSalaId)) {
              _tmpSalaId = null;
            } else {
              _tmpSalaId = _cursor.getInt(_cursorIndexOfSalaId);
            }
            final String _tmpSalaNome;
            if (_cursor.isNull(_cursorIndexOfSalaNome)) {
              _tmpSalaNome = null;
            } else {
              _tmpSalaNome = _cursor.getString(_cursorIndexOfSalaNome);
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
            final Integer _tmpResponsavelId;
            if (_cursor.isNull(_cursorIndexOfResponsavelId)) {
              _tmpResponsavelId = null;
            } else {
              _tmpResponsavelId = _cursor.getInt(_cursorIndexOfResponsavelId);
            }
            final String _tmpResponsavelNome;
            if (_cursor.isNull(_cursorIndexOfResponsavelNome)) {
              _tmpResponsavelNome = null;
            } else {
              _tmpResponsavelNome = _cursor.getString(_cursorIndexOfResponsavelNome);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final boolean _tmpColetado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfColetado);
            _tmpColetado = _tmp != 0;
            final Long _tmpDataColeta;
            if (_cursor.isNull(_cursorIndexOfDataColeta)) {
              _tmpDataColeta = null;
            } else {
              _tmpDataColeta = _cursor.getLong(_cursorIndexOfDataColeta);
            }
            final String _tmpColetadoPor;
            if (_cursor.isNull(_cursorIndexOfColetadoPor)) {
              _tmpColetadoPor = null;
            } else {
              _tmpColetadoPor = _cursor.getString(_cursorIndexOfColetadoPor);
            }
            final String _tmpObservacoesColeta;
            if (_cursor.isNull(_cursorIndexOfObservacoesColeta)) {
              _tmpObservacoesColeta = null;
            } else {
              _tmpObservacoesColeta = _cursor.getString(_cursorIndexOfObservacoesColeta);
            }
            final String _tmpObservacoes;
            if (_cursor.isNull(_cursorIndexOfObservacoes)) {
              _tmpObservacoes = null;
            } else {
              _tmpObservacoes = _cursor.getString(_cursorIndexOfObservacoes);
            }
            final long _tmpDataUltimaAtualizacao;
            _tmpDataUltimaAtualizacao = _cursor.getLong(_cursorIndexOfDataUltimaAtualizacao);
            _item = new PatrimonioEntity(_tmpId,_tmpNumero,_tmpNumeroPatrimonio,_tmpDescricao,_tmpMarca,_tmpModelo,_tmpNumeroSerie,_tmpEstado,_tmpValor,_tmpSetorId,_tmpSetorNome,_tmpIdSala,_tmpNomeSala,_tmpSalaId,_tmpSalaNome,_tmpIdResponsavel,_tmpNomeResponsavel,_tmpResponsavelId,_tmpResponsavelNome,_tmpStatus,_tmpColetado,_tmpDataColeta,_tmpColetadoPor,_tmpObservacoesColeta,_tmpObservacoes,_tmpDataUltimaAtualizacao);
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
