package com.slpolice.reporting.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
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
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReportDao_Impl implements ReportDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReportEntity> __insertionAdapterOfReportEntity;

  private final EntityDeletionOrUpdateAdapter<ReportEntity> __updateAdapterOfReportEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public ReportDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReportEntity = new EntityInsertionAdapter<ReportEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reports` (`id`,`referenceNo`,`reporterId`,`category`,`title`,`description`,`locationName`,`latitude`,`longitude`,`vehicleNumber`,`incidentAt`,`anonymous`,`status`,`priority`,`officerNote`,`handledBy`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReportEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getReferenceNo());
        statement.bindLong(3, entity.getReporterId());
        statement.bindString(4, entity.getCategory());
        statement.bindString(5, entity.getTitle());
        statement.bindString(6, entity.getDescription());
        statement.bindString(7, entity.getLocationName());
        if (entity.getLatitude() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getLongitude());
        }
        if (entity.getVehicleNumber() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getVehicleNumber());
        }
        statement.bindLong(11, entity.getIncidentAt());
        final int _tmp = entity.getAnonymous() ? 1 : 0;
        statement.bindLong(12, _tmp);
        statement.bindString(13, entity.getStatus());
        statement.bindString(14, entity.getPriority());
        if (entity.getOfficerNote() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getOfficerNote());
        }
        if (entity.getHandledBy() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getHandledBy());
        }
        statement.bindLong(17, entity.getCreatedAt());
        statement.bindLong(18, entity.getUpdatedAt());
      }
    };
    this.__updateAdapterOfReportEntity = new EntityDeletionOrUpdateAdapter<ReportEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reports` SET `id` = ?,`referenceNo` = ?,`reporterId` = ?,`category` = ?,`title` = ?,`description` = ?,`locationName` = ?,`latitude` = ?,`longitude` = ?,`vehicleNumber` = ?,`incidentAt` = ?,`anonymous` = ?,`status` = ?,`priority` = ?,`officerNote` = ?,`handledBy` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReportEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getReferenceNo());
        statement.bindLong(3, entity.getReporterId());
        statement.bindString(4, entity.getCategory());
        statement.bindString(5, entity.getTitle());
        statement.bindString(6, entity.getDescription());
        statement.bindString(7, entity.getLocationName());
        if (entity.getLatitude() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getLongitude());
        }
        if (entity.getVehicleNumber() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getVehicleNumber());
        }
        statement.bindLong(11, entity.getIncidentAt());
        final int _tmp = entity.getAnonymous() ? 1 : 0;
        statement.bindLong(12, _tmp);
        statement.bindString(13, entity.getStatus());
        statement.bindString(14, entity.getPriority());
        if (entity.getOfficerNote() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getOfficerNote());
        }
        if (entity.getHandledBy() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getHandledBy());
        }
        statement.bindLong(17, entity.getCreatedAt());
        statement.bindLong(18, entity.getUpdatedAt());
        statement.bindString(19, entity.getId());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reports WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ReportEntity report, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReportEntity.insert(report);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ReportEntity report, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReportEntity.handle(report);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ReportWithEvidence> observeById(final String id) {
    final String _sql = "SELECT * FROM reports WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"evidence",
        "reports"}, new Callable<ReportWithEvidence>() {
      @Override
      @Nullable
      public ReportWithEvidence call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfReferenceNo = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceNo");
            final int _cursorIndexOfReporterId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporterId");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleNumber");
            final int _cursorIndexOfIncidentAt = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentAt");
            final int _cursorIndexOfAnonymous = CursorUtil.getColumnIndexOrThrow(_cursor, "anonymous");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
            final int _cursorIndexOfOfficerNote = CursorUtil.getColumnIndexOrThrow(_cursor, "officerNote");
            final int _cursorIndexOfHandledBy = CursorUtil.getColumnIndexOrThrow(_cursor, "handledBy");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<EvidenceEntity>> _collectionEvidence = new ArrayMap<String, ArrayList<EvidenceEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionEvidence.containsKey(_tmpKey)) {
                _collectionEvidence.put(_tmpKey, new ArrayList<EvidenceEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipevidenceAscomSlpoliceReportingDataLocalEvidenceEntity(_collectionEvidence);
            final ReportWithEvidence _result;
            if (_cursor.moveToFirst()) {
              final ReportEntity _tmpReport;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpReferenceNo;
              _tmpReferenceNo = _cursor.getString(_cursorIndexOfReferenceNo);
              final long _tmpReporterId;
              _tmpReporterId = _cursor.getLong(_cursorIndexOfReporterId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpDescription;
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              final String _tmpLocationName;
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
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
              final String _tmpVehicleNumber;
              if (_cursor.isNull(_cursorIndexOfVehicleNumber)) {
                _tmpVehicleNumber = null;
              } else {
                _tmpVehicleNumber = _cursor.getString(_cursorIndexOfVehicleNumber);
              }
              final long _tmpIncidentAt;
              _tmpIncidentAt = _cursor.getLong(_cursorIndexOfIncidentAt);
              final boolean _tmpAnonymous;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfAnonymous);
              _tmpAnonymous = _tmp != 0;
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpPriority;
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
              final String _tmpOfficerNote;
              if (_cursor.isNull(_cursorIndexOfOfficerNote)) {
                _tmpOfficerNote = null;
              } else {
                _tmpOfficerNote = _cursor.getString(_cursorIndexOfOfficerNote);
              }
              final Long _tmpHandledBy;
              if (_cursor.isNull(_cursorIndexOfHandledBy)) {
                _tmpHandledBy = null;
              } else {
                _tmpHandledBy = _cursor.getLong(_cursorIndexOfHandledBy);
              }
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpReport = new ReportEntity(_tmpId,_tmpReferenceNo,_tmpReporterId,_tmpCategory,_tmpTitle,_tmpDescription,_tmpLocationName,_tmpLatitude,_tmpLongitude,_tmpVehicleNumber,_tmpIncidentAt,_tmpAnonymous,_tmpStatus,_tmpPriority,_tmpOfficerNote,_tmpHandledBy,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<EvidenceEntity> _tmpEvidenceCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpEvidenceCollection = _collectionEvidence.get(_tmpKey_1);
              _result = new ReportWithEvidence(_tmpReport,_tmpEvidenceCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object findById(final String id,
      final Continuation<? super ReportWithEvidence> $completion) {
    final String _sql = "SELECT * FROM reports WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<ReportWithEvidence>() {
      @Override
      @Nullable
      public ReportWithEvidence call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfReferenceNo = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceNo");
            final int _cursorIndexOfReporterId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporterId");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleNumber");
            final int _cursorIndexOfIncidentAt = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentAt");
            final int _cursorIndexOfAnonymous = CursorUtil.getColumnIndexOrThrow(_cursor, "anonymous");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
            final int _cursorIndexOfOfficerNote = CursorUtil.getColumnIndexOrThrow(_cursor, "officerNote");
            final int _cursorIndexOfHandledBy = CursorUtil.getColumnIndexOrThrow(_cursor, "handledBy");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<EvidenceEntity>> _collectionEvidence = new ArrayMap<String, ArrayList<EvidenceEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionEvidence.containsKey(_tmpKey)) {
                _collectionEvidence.put(_tmpKey, new ArrayList<EvidenceEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipevidenceAscomSlpoliceReportingDataLocalEvidenceEntity(_collectionEvidence);
            final ReportWithEvidence _result;
            if (_cursor.moveToFirst()) {
              final ReportEntity _tmpReport;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpReferenceNo;
              _tmpReferenceNo = _cursor.getString(_cursorIndexOfReferenceNo);
              final long _tmpReporterId;
              _tmpReporterId = _cursor.getLong(_cursorIndexOfReporterId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpDescription;
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              final String _tmpLocationName;
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
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
              final String _tmpVehicleNumber;
              if (_cursor.isNull(_cursorIndexOfVehicleNumber)) {
                _tmpVehicleNumber = null;
              } else {
                _tmpVehicleNumber = _cursor.getString(_cursorIndexOfVehicleNumber);
              }
              final long _tmpIncidentAt;
              _tmpIncidentAt = _cursor.getLong(_cursorIndexOfIncidentAt);
              final boolean _tmpAnonymous;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfAnonymous);
              _tmpAnonymous = _tmp != 0;
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpPriority;
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
              final String _tmpOfficerNote;
              if (_cursor.isNull(_cursorIndexOfOfficerNote)) {
                _tmpOfficerNote = null;
              } else {
                _tmpOfficerNote = _cursor.getString(_cursorIndexOfOfficerNote);
              }
              final Long _tmpHandledBy;
              if (_cursor.isNull(_cursorIndexOfHandledBy)) {
                _tmpHandledBy = null;
              } else {
                _tmpHandledBy = _cursor.getLong(_cursorIndexOfHandledBy);
              }
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpReport = new ReportEntity(_tmpId,_tmpReferenceNo,_tmpReporterId,_tmpCategory,_tmpTitle,_tmpDescription,_tmpLocationName,_tmpLatitude,_tmpLongitude,_tmpVehicleNumber,_tmpIncidentAt,_tmpAnonymous,_tmpStatus,_tmpPriority,_tmpOfficerNote,_tmpHandledBy,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<EvidenceEntity> _tmpEvidenceCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpEvidenceCollection = _collectionEvidence.get(_tmpKey_1);
              _result = new ReportWithEvidence(_tmpReport,_tmpEvidenceCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReportWithEvidence>> observeByReporter(final long userId) {
    final String _sql = "SELECT * FROM reports WHERE reporterId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"evidence",
        "reports"}, new Callable<List<ReportWithEvidence>>() {
      @Override
      @NonNull
      public List<ReportWithEvidence> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfReferenceNo = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceNo");
            final int _cursorIndexOfReporterId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporterId");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleNumber");
            final int _cursorIndexOfIncidentAt = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentAt");
            final int _cursorIndexOfAnonymous = CursorUtil.getColumnIndexOrThrow(_cursor, "anonymous");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
            final int _cursorIndexOfOfficerNote = CursorUtil.getColumnIndexOrThrow(_cursor, "officerNote");
            final int _cursorIndexOfHandledBy = CursorUtil.getColumnIndexOrThrow(_cursor, "handledBy");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<EvidenceEntity>> _collectionEvidence = new ArrayMap<String, ArrayList<EvidenceEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionEvidence.containsKey(_tmpKey)) {
                _collectionEvidence.put(_tmpKey, new ArrayList<EvidenceEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipevidenceAscomSlpoliceReportingDataLocalEvidenceEntity(_collectionEvidence);
            final List<ReportWithEvidence> _result = new ArrayList<ReportWithEvidence>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final ReportWithEvidence _item;
              final ReportEntity _tmpReport;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpReferenceNo;
              _tmpReferenceNo = _cursor.getString(_cursorIndexOfReferenceNo);
              final long _tmpReporterId;
              _tmpReporterId = _cursor.getLong(_cursorIndexOfReporterId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpDescription;
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              final String _tmpLocationName;
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
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
              final String _tmpVehicleNumber;
              if (_cursor.isNull(_cursorIndexOfVehicleNumber)) {
                _tmpVehicleNumber = null;
              } else {
                _tmpVehicleNumber = _cursor.getString(_cursorIndexOfVehicleNumber);
              }
              final long _tmpIncidentAt;
              _tmpIncidentAt = _cursor.getLong(_cursorIndexOfIncidentAt);
              final boolean _tmpAnonymous;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfAnonymous);
              _tmpAnonymous = _tmp != 0;
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpPriority;
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
              final String _tmpOfficerNote;
              if (_cursor.isNull(_cursorIndexOfOfficerNote)) {
                _tmpOfficerNote = null;
              } else {
                _tmpOfficerNote = _cursor.getString(_cursorIndexOfOfficerNote);
              }
              final Long _tmpHandledBy;
              if (_cursor.isNull(_cursorIndexOfHandledBy)) {
                _tmpHandledBy = null;
              } else {
                _tmpHandledBy = _cursor.getLong(_cursorIndexOfHandledBy);
              }
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpReport = new ReportEntity(_tmpId,_tmpReferenceNo,_tmpReporterId,_tmpCategory,_tmpTitle,_tmpDescription,_tmpLocationName,_tmpLatitude,_tmpLongitude,_tmpVehicleNumber,_tmpIncidentAt,_tmpAnonymous,_tmpStatus,_tmpPriority,_tmpOfficerNote,_tmpHandledBy,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<EvidenceEntity> _tmpEvidenceCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpEvidenceCollection = _collectionEvidence.get(_tmpKey_1);
              _item = new ReportWithEvidence(_tmpReport,_tmpEvidenceCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ReportWithEvidence>> observeAll() {
    final String _sql = "SELECT * FROM reports ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"evidence",
        "reports"}, new Callable<List<ReportWithEvidence>>() {
      @Override
      @NonNull
      public List<ReportWithEvidence> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfReferenceNo = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceNo");
            final int _cursorIndexOfReporterId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporterId");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleNumber");
            final int _cursorIndexOfIncidentAt = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentAt");
            final int _cursorIndexOfAnonymous = CursorUtil.getColumnIndexOrThrow(_cursor, "anonymous");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
            final int _cursorIndexOfOfficerNote = CursorUtil.getColumnIndexOrThrow(_cursor, "officerNote");
            final int _cursorIndexOfHandledBy = CursorUtil.getColumnIndexOrThrow(_cursor, "handledBy");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<EvidenceEntity>> _collectionEvidence = new ArrayMap<String, ArrayList<EvidenceEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionEvidence.containsKey(_tmpKey)) {
                _collectionEvidence.put(_tmpKey, new ArrayList<EvidenceEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipevidenceAscomSlpoliceReportingDataLocalEvidenceEntity(_collectionEvidence);
            final List<ReportWithEvidence> _result = new ArrayList<ReportWithEvidence>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final ReportWithEvidence _item;
              final ReportEntity _tmpReport;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpReferenceNo;
              _tmpReferenceNo = _cursor.getString(_cursorIndexOfReferenceNo);
              final long _tmpReporterId;
              _tmpReporterId = _cursor.getLong(_cursorIndexOfReporterId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpDescription;
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              final String _tmpLocationName;
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
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
              final String _tmpVehicleNumber;
              if (_cursor.isNull(_cursorIndexOfVehicleNumber)) {
                _tmpVehicleNumber = null;
              } else {
                _tmpVehicleNumber = _cursor.getString(_cursorIndexOfVehicleNumber);
              }
              final long _tmpIncidentAt;
              _tmpIncidentAt = _cursor.getLong(_cursorIndexOfIncidentAt);
              final boolean _tmpAnonymous;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfAnonymous);
              _tmpAnonymous = _tmp != 0;
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpPriority;
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
              final String _tmpOfficerNote;
              if (_cursor.isNull(_cursorIndexOfOfficerNote)) {
                _tmpOfficerNote = null;
              } else {
                _tmpOfficerNote = _cursor.getString(_cursorIndexOfOfficerNote);
              }
              final Long _tmpHandledBy;
              if (_cursor.isNull(_cursorIndexOfHandledBy)) {
                _tmpHandledBy = null;
              } else {
                _tmpHandledBy = _cursor.getLong(_cursorIndexOfHandledBy);
              }
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpReport = new ReportEntity(_tmpId,_tmpReferenceNo,_tmpReporterId,_tmpCategory,_tmpTitle,_tmpDescription,_tmpLocationName,_tmpLatitude,_tmpLongitude,_tmpVehicleNumber,_tmpIncidentAt,_tmpAnonymous,_tmpStatus,_tmpPriority,_tmpOfficerNote,_tmpHandledBy,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<EvidenceEntity> _tmpEvidenceCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpEvidenceCollection = _collectionEvidence.get(_tmpKey_1);
              _item = new ReportWithEvidence(_tmpReport,_tmpEvidenceCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<StatusCount>> observeStatusCounts() {
    final String _sql = "SELECT status, COUNT(*) AS total FROM reports GROUP BY status";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reports"}, new Callable<List<StatusCount>>() {
      @Override
      @NonNull
      public List<StatusCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfStatus = 0;
          final int _cursorIndexOfTotal = 1;
          final List<StatusCount> _result = new ArrayList<StatusCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StatusCount _item;
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            _item = new StatusCount(_tmpStatus,_tmpTotal);
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
  public Flow<Integer> countForReporter(final long userId) {
    final String _sql = "SELECT COUNT(*) FROM reports WHERE reporterId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reports"}, new Callable<Integer>() {
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
  public Flow<List<CategoryCount>> observeCategoryCounts() {
    final String _sql = "SELECT category, COUNT(*) AS total FROM reports GROUP BY category ORDER BY total DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reports"}, new Callable<List<CategoryCount>>() {
      @Override
      @NonNull
      public List<CategoryCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategory = 0;
          final int _cursorIndexOfTotal = 1;
          final List<CategoryCount> _result = new ArrayList<CategoryCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryCount _item;
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            _item = new CategoryCount(_tmpCategory,_tmpTotal);
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

  private void __fetchRelationshipevidenceAscomSlpoliceReportingDataLocalEvidenceEntity(
      @NonNull final ArrayMap<String, ArrayList<EvidenceEntity>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchArrayMap(_map, true, (map) -> {
        __fetchRelationshipevidenceAscomSlpoliceReportingDataLocalEvidenceEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`reportId`,`filePath`,`type`,`sizeBytes`,`integrityHash`,`capturedAt`,`sourceCapturedAt`,`capturedLatitude`,`capturedLongitude` FROM `evidence` WHERE `reportId` IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      _stmt.bindString(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "reportId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfReportId = 1;
      final int _cursorIndexOfFilePath = 2;
      final int _cursorIndexOfType = 3;
      final int _cursorIndexOfSizeBytes = 4;
      final int _cursorIndexOfIntegrityHash = 5;
      final int _cursorIndexOfCapturedAt = 6;
      final int _cursorIndexOfSourceCapturedAt = 7;
      final int _cursorIndexOfCapturedLatitude = 8;
      final int _cursorIndexOfCapturedLongitude = 9;
      while (_cursor.moveToNext()) {
        final String _tmpKey;
        _tmpKey = _cursor.getString(_itemKeyIndex);
        final ArrayList<EvidenceEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final EvidenceEntity _item_1;
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
          _item_1 = new EvidenceEntity(_tmpId,_tmpReportId,_tmpFilePath,_tmpType,_tmpSizeBytes,_tmpIntegrityHash,_tmpCapturedAt,_tmpSourceCapturedAt,_tmpCapturedLatitude,_tmpCapturedLongitude);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
