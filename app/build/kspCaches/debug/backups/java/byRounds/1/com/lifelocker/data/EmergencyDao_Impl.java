package com.lifelocker.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
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
public final class EmergencyDao_Impl implements EmergencyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EmergencyContact> __insertionAdapterOfEmergencyContact;

  private final EntityDeletionOrUpdateAdapter<EmergencyContact> __deletionAdapterOfEmergencyContact;

  private final EntityDeletionOrUpdateAdapter<EmergencyContact> __updateAdapterOfEmergencyContact;

  public EmergencyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmergencyContact = new EntityInsertionAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `emergency_contacts` (`id`,`name`,`relationship`,`phone`,`bloodGroup`,`allergies`,`conditions`,`medicines`,`doctor`,`hospital`,`insurance`,`medicalNotes`,`isPrimary`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContact entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getRelationship());
        statement.bindString(4, entity.getPhone());
        statement.bindString(5, entity.getBloodGroup());
        statement.bindString(6, entity.getAllergies());
        statement.bindString(7, entity.getConditions());
        statement.bindString(8, entity.getMedicines());
        statement.bindString(9, entity.getDoctor());
        statement.bindString(10, entity.getHospital());
        statement.bindString(11, entity.getInsurance());
        statement.bindString(12, entity.getMedicalNotes());
        final int _tmp = entity.isPrimary() ? 1 : 0;
        statement.bindLong(13, _tmp);
      }
    };
    this.__deletionAdapterOfEmergencyContact = new EntityDeletionOrUpdateAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `emergency_contacts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContact entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfEmergencyContact = new EntityDeletionOrUpdateAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `emergency_contacts` SET `id` = ?,`name` = ?,`relationship` = ?,`phone` = ?,`bloodGroup` = ?,`allergies` = ?,`conditions` = ?,`medicines` = ?,`doctor` = ?,`hospital` = ?,`insurance` = ?,`medicalNotes` = ?,`isPrimary` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContact entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getRelationship());
        statement.bindString(4, entity.getPhone());
        statement.bindString(5, entity.getBloodGroup());
        statement.bindString(6, entity.getAllergies());
        statement.bindString(7, entity.getConditions());
        statement.bindString(8, entity.getMedicines());
        statement.bindString(9, entity.getDoctor());
        statement.bindString(10, entity.getHospital());
        statement.bindString(11, entity.getInsurance());
        statement.bindString(12, entity.getMedicalNotes());
        final int _tmp = entity.isPrimary() ? 1 : 0;
        statement.bindLong(13, _tmp);
        statement.bindLong(14, entity.getId());
      }
    };
  }

  @Override
  public Object insertContact(final EmergencyContact contact,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfEmergencyContact.insertAndReturnId(contact);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteContact(final EmergencyContact contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfEmergencyContact.handle(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateContact(final EmergencyContact contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEmergencyContact.handle(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getContactById(final int id,
      final Continuation<? super EmergencyContact> $completion) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EmergencyContact>() {
      @Override
      @Nullable
      public EmergencyContact call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfBloodGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodGroup");
          final int _cursorIndexOfAllergies = CursorUtil.getColumnIndexOrThrow(_cursor, "allergies");
          final int _cursorIndexOfConditions = CursorUtil.getColumnIndexOrThrow(_cursor, "conditions");
          final int _cursorIndexOfMedicines = CursorUtil.getColumnIndexOrThrow(_cursor, "medicines");
          final int _cursorIndexOfDoctor = CursorUtil.getColumnIndexOrThrow(_cursor, "doctor");
          final int _cursorIndexOfHospital = CursorUtil.getColumnIndexOrThrow(_cursor, "hospital");
          final int _cursorIndexOfInsurance = CursorUtil.getColumnIndexOrThrow(_cursor, "insurance");
          final int _cursorIndexOfMedicalNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "medicalNotes");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final EmergencyContact _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpBloodGroup;
            _tmpBloodGroup = _cursor.getString(_cursorIndexOfBloodGroup);
            final String _tmpAllergies;
            _tmpAllergies = _cursor.getString(_cursorIndexOfAllergies);
            final String _tmpConditions;
            _tmpConditions = _cursor.getString(_cursorIndexOfConditions);
            final String _tmpMedicines;
            _tmpMedicines = _cursor.getString(_cursorIndexOfMedicines);
            final String _tmpDoctor;
            _tmpDoctor = _cursor.getString(_cursorIndexOfDoctor);
            final String _tmpHospital;
            _tmpHospital = _cursor.getString(_cursorIndexOfHospital);
            final String _tmpInsurance;
            _tmpInsurance = _cursor.getString(_cursorIndexOfInsurance);
            final String _tmpMedicalNotes;
            _tmpMedicalNotes = _cursor.getString(_cursorIndexOfMedicalNotes);
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            _result = new EmergencyContact(_tmpId,_tmpName,_tmpRelationship,_tmpPhone,_tmpBloodGroup,_tmpAllergies,_tmpConditions,_tmpMedicines,_tmpDoctor,_tmpHospital,_tmpInsurance,_tmpMedicalNotes,_tmpIsPrimary);
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
  public Flow<List<EmergencyContact>> getAllContacts() {
    final String _sql = "SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"emergency_contacts"}, new Callable<List<EmergencyContact>>() {
      @Override
      @NonNull
      public List<EmergencyContact> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfBloodGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodGroup");
          final int _cursorIndexOfAllergies = CursorUtil.getColumnIndexOrThrow(_cursor, "allergies");
          final int _cursorIndexOfConditions = CursorUtil.getColumnIndexOrThrow(_cursor, "conditions");
          final int _cursorIndexOfMedicines = CursorUtil.getColumnIndexOrThrow(_cursor, "medicines");
          final int _cursorIndexOfDoctor = CursorUtil.getColumnIndexOrThrow(_cursor, "doctor");
          final int _cursorIndexOfHospital = CursorUtil.getColumnIndexOrThrow(_cursor, "hospital");
          final int _cursorIndexOfInsurance = CursorUtil.getColumnIndexOrThrow(_cursor, "insurance");
          final int _cursorIndexOfMedicalNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "medicalNotes");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final List<EmergencyContact> _result = new ArrayList<EmergencyContact>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContact _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpBloodGroup;
            _tmpBloodGroup = _cursor.getString(_cursorIndexOfBloodGroup);
            final String _tmpAllergies;
            _tmpAllergies = _cursor.getString(_cursorIndexOfAllergies);
            final String _tmpConditions;
            _tmpConditions = _cursor.getString(_cursorIndexOfConditions);
            final String _tmpMedicines;
            _tmpMedicines = _cursor.getString(_cursorIndexOfMedicines);
            final String _tmpDoctor;
            _tmpDoctor = _cursor.getString(_cursorIndexOfDoctor);
            final String _tmpHospital;
            _tmpHospital = _cursor.getString(_cursorIndexOfHospital);
            final String _tmpInsurance;
            _tmpInsurance = _cursor.getString(_cursorIndexOfInsurance);
            final String _tmpMedicalNotes;
            _tmpMedicalNotes = _cursor.getString(_cursorIndexOfMedicalNotes);
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            _item = new EmergencyContact(_tmpId,_tmpName,_tmpRelationship,_tmpPhone,_tmpBloodGroup,_tmpAllergies,_tmpConditions,_tmpMedicines,_tmpDoctor,_tmpHospital,_tmpInsurance,_tmpMedicalNotes,_tmpIsPrimary);
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
  public Flow<List<EmergencyContact>> searchContacts(final String query) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE name LIKE '%' || ? || '%' OR relationship LIKE '%' || ? || '%' OR phone LIKE '%' || ? || '%' ORDER BY isPrimary DESC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"emergency_contacts"}, new Callable<List<EmergencyContact>>() {
      @Override
      @NonNull
      public List<EmergencyContact> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfBloodGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodGroup");
          final int _cursorIndexOfAllergies = CursorUtil.getColumnIndexOrThrow(_cursor, "allergies");
          final int _cursorIndexOfConditions = CursorUtil.getColumnIndexOrThrow(_cursor, "conditions");
          final int _cursorIndexOfMedicines = CursorUtil.getColumnIndexOrThrow(_cursor, "medicines");
          final int _cursorIndexOfDoctor = CursorUtil.getColumnIndexOrThrow(_cursor, "doctor");
          final int _cursorIndexOfHospital = CursorUtil.getColumnIndexOrThrow(_cursor, "hospital");
          final int _cursorIndexOfInsurance = CursorUtil.getColumnIndexOrThrow(_cursor, "insurance");
          final int _cursorIndexOfMedicalNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "medicalNotes");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final List<EmergencyContact> _result = new ArrayList<EmergencyContact>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContact _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpBloodGroup;
            _tmpBloodGroup = _cursor.getString(_cursorIndexOfBloodGroup);
            final String _tmpAllergies;
            _tmpAllergies = _cursor.getString(_cursorIndexOfAllergies);
            final String _tmpConditions;
            _tmpConditions = _cursor.getString(_cursorIndexOfConditions);
            final String _tmpMedicines;
            _tmpMedicines = _cursor.getString(_cursorIndexOfMedicines);
            final String _tmpDoctor;
            _tmpDoctor = _cursor.getString(_cursorIndexOfDoctor);
            final String _tmpHospital;
            _tmpHospital = _cursor.getString(_cursorIndexOfHospital);
            final String _tmpInsurance;
            _tmpInsurance = _cursor.getString(_cursorIndexOfInsurance);
            final String _tmpMedicalNotes;
            _tmpMedicalNotes = _cursor.getString(_cursorIndexOfMedicalNotes);
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            _item = new EmergencyContact(_tmpId,_tmpName,_tmpRelationship,_tmpPhone,_tmpBloodGroup,_tmpAllergies,_tmpConditions,_tmpMedicines,_tmpDoctor,_tmpHospital,_tmpInsurance,_tmpMedicalNotes,_tmpIsPrimary);
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
  public Object getAllContactsSyncForBackup(
      final Continuation<? super List<EmergencyContact>> $completion) {
    final String _sql = "SELECT * FROM emergency_contacts";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EmergencyContact>>() {
      @Override
      @NonNull
      public List<EmergencyContact> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfBloodGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodGroup");
          final int _cursorIndexOfAllergies = CursorUtil.getColumnIndexOrThrow(_cursor, "allergies");
          final int _cursorIndexOfConditions = CursorUtil.getColumnIndexOrThrow(_cursor, "conditions");
          final int _cursorIndexOfMedicines = CursorUtil.getColumnIndexOrThrow(_cursor, "medicines");
          final int _cursorIndexOfDoctor = CursorUtil.getColumnIndexOrThrow(_cursor, "doctor");
          final int _cursorIndexOfHospital = CursorUtil.getColumnIndexOrThrow(_cursor, "hospital");
          final int _cursorIndexOfInsurance = CursorUtil.getColumnIndexOrThrow(_cursor, "insurance");
          final int _cursorIndexOfMedicalNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "medicalNotes");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final List<EmergencyContact> _result = new ArrayList<EmergencyContact>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContact _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpBloodGroup;
            _tmpBloodGroup = _cursor.getString(_cursorIndexOfBloodGroup);
            final String _tmpAllergies;
            _tmpAllergies = _cursor.getString(_cursorIndexOfAllergies);
            final String _tmpConditions;
            _tmpConditions = _cursor.getString(_cursorIndexOfConditions);
            final String _tmpMedicines;
            _tmpMedicines = _cursor.getString(_cursorIndexOfMedicines);
            final String _tmpDoctor;
            _tmpDoctor = _cursor.getString(_cursorIndexOfDoctor);
            final String _tmpHospital;
            _tmpHospital = _cursor.getString(_cursorIndexOfHospital);
            final String _tmpInsurance;
            _tmpInsurance = _cursor.getString(_cursorIndexOfInsurance);
            final String _tmpMedicalNotes;
            _tmpMedicalNotes = _cursor.getString(_cursorIndexOfMedicalNotes);
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            _item = new EmergencyContact(_tmpId,_tmpName,_tmpRelationship,_tmpPhone,_tmpBloodGroup,_tmpAllergies,_tmpConditions,_tmpMedicines,_tmpDoctor,_tmpHospital,_tmpInsurance,_tmpMedicalNotes,_tmpIsPrimary);
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
