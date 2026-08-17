package com.slpolice.reporting.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class AuditDao_Impl implements AuditDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AuditLogEntity> __insertionAdapterOfAuditLogEntity;

  public AuditDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAuditLogEntity = new EntityInsertionAdapter<AuditLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `audit_logs` (`id`,`actorId`,`actorName`,`action`,`targetRef`,`details`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AuditLogEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getActorId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getActorId());
        }
        statement.bindString(3, entity.getActorName());
        statement.bindString(4, entity.getAction());
        statement.bindString(5, entity.getTargetRef());
        statement.bindString(6, entity.getDetails());
        statement.bindLong(7, entity.getTimestamp());
      }
    };
  }

  @Override
  public Object insert(final AuditLogEntity log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAuditLogEntity.insert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AuditLogEntity>> observeForTarget(final String ref) {
    final String _sql = "SELECT * FROM audit_logs WHERE targetRef = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, ref);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLogEntity>>() {
      @Override
      @NonNull
      public List<AuditLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfActorId = CursorUtil.getColumnIndexOrThrow(_cursor, "actorId");
          final int _cursorIndexOfActorName = CursorUtil.getColumnIndexOrThrow(_cursor, "actorName");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetRef = CursorUtil.getColumnIndexOrThrow(_cursor, "targetRef");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<AuditLogEntity> _result = new ArrayList<AuditLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpActorId;
            if (_cursor.isNull(_cursorIndexOfActorId)) {
              _tmpActorId = null;
            } else {
              _tmpActorId = _cursor.getLong(_cursorIndexOfActorId);
            }
            final String _tmpActorName;
            _tmpActorName = _cursor.getString(_cursorIndexOfActorName);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final String _tmpTargetRef;
            _tmpTargetRef = _cursor.getString(_cursorIndexOfTargetRef);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new AuditLogEntity(_tmpId,_tmpActorId,_tmpActorName,_tmpAction,_tmpTargetRef,_tmpDetails,_tmpTimestamp);
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
  public Flow<List<AuditLogEntity>> observeRecent() {
    final String _sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLogEntity>>() {
      @Override
      @NonNull
      public List<AuditLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfActorId = CursorUtil.getColumnIndexOrThrow(_cursor, "actorId");
          final int _cursorIndexOfActorName = CursorUtil.getColumnIndexOrThrow(_cursor, "actorName");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetRef = CursorUtil.getColumnIndexOrThrow(_cursor, "targetRef");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<AuditLogEntity> _result = new ArrayList<AuditLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpActorId;
            if (_cursor.isNull(_cursorIndexOfActorId)) {
              _tmpActorId = null;
            } else {
              _tmpActorId = _cursor.getLong(_cursorIndexOfActorId);
            }
            final String _tmpActorName;
            _tmpActorName = _cursor.getString(_cursorIndexOfActorName);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final String _tmpTargetRef;
            _tmpTargetRef = _cursor.getString(_cursorIndexOfTargetRef);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new AuditLogEntity(_tmpId,_tmpActorId,_tmpActorName,_tmpAction,_tmpTargetRef,_tmpDetails,_tmpTimestamp);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
