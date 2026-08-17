package com.slpolice.reporting.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EvidenceDao_Impl implements EvidenceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EvidenceEntity> __insertionAdapterOfEvidenceEntity;

  public EvidenceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEvidenceEntity = new EntityInsertionAdapter<EvidenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `evidence` (`id`,`reportId`,`filePath`,`type`,`sizeBytes`,`integrityHash`,`capturedAt`,`sourceCapturedAt`,`capturedLatitude`,`capturedLongitude`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EvidenceEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getReportId());
        statement.bindString(3, entity.getFilePath());
        statement.bindString(4, entity.getType());
        statement.bindLong(5, entity.getSizeBytes());
        statement.bindString(6, entity.getIntegrityHash());
        statement.bindLong(7, entity.getCapturedAt());
        if (entity.getSourceCapturedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getSourceCapturedAt());
        }
        if (entity.getCapturedLatitude() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getCapturedLatitude());
        }
        if (entity.getCapturedLongitude() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getCapturedLongitude());
        }
      }
    };
  }

  @Override
  public Object insertAll(final List<EvidenceEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEvidenceEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object forReport(final String reportId,
      final Continuation<? super List<EvidenceEntity>> $completion) {
    final String _sql = "SELECT * FROM evidence WHERE reportId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, reportId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EvidenceEntity>>() {
      @Override
      @NonNull
      public List<EvidenceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfReportId = CursorUtil.getColumnIndexOrThrow(_cursor, "reportId");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfIntegrityHash = CursorUtil.getColumnIndexOrThrow(_cursor, "integrityHash");
          final int _cursorIndexOfCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "capturedAt");
          final int _cursorIndexOfSourceCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceCapturedAt");
          final int _cursorIndexOfCapturedLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "capturedLatitude");
          final int _cursorIndexOfCapturedLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "capturedLongitude");
          final List<EvidenceEntity> _result = new ArrayList<EvidenceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EvidenceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpReportId;
            _tmpReportId = _cursor.getString(_cursorIndexOfReportId);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpIntegrityHash;
            _tmpIntegrityHash = _cursor.getString(_cursorIndexOfIntegrityHash);
            final long _tmpCapturedAt;
            _tmpCapturedAt = _cursor.getLong(_cursorIndexOfCapturedAt);
            final Long _tmpSourceCapturedAt;
            if (_cursor.isNull(_cursorIndexOfSourceCapturedAt)) {
              _tmpSourceCapturedAt = null;
            } else {
              _tmpSourceCapturedAt = _cursor.getLong(_cursorIndexOfSourceCapturedAt);
            }
            final Double _tmpCapturedLatitude;
            if (_cursor.isNull(_cursorIndexOfCapturedLatitude)) {
              _tmpCapturedLatitude = null;
            } else {
              _tmpCapturedLatitude = _cursor.getDouble(_cursorIndexOfCapturedLatitude);
            }
            final Double _tmpCapturedLongitude;
            if (_cursor.isNull(_cursorIndexOfCapturedLongitude)) {
              _tmpCapturedLongitude = null;
            } else {
              _tmpCapturedLongitude = _cursor.getDouble(_cursorIndexOfCapturedLongitude);
            }
            _item = new EvidenceEntity(_tmpId,_tmpReportId,_tmpFilePath,_tmpType,_tmpSizeBytes,_tmpIntegrityHash,_tmpCapturedAt,_tmpSourceCapturedAt,_tmpCapturedLatitude,_tmpCapturedLongitude);
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
