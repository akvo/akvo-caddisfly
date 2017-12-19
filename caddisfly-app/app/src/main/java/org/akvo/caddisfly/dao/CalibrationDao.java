package org.akvo.caddisfly.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;

import java.util.List;

@Dao
public interface CalibrationDao {

    @Query("SELECT * FROM calibration WHERE uid = :uuid ORDER BY value")
    List<Calibration> getAll(String uuid);

    @Query("SELECT * FROM calibrationdetail WHERE uid = :uuid")
    CalibrationDetail getCalibrationDetails(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Calibration calibration);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalibrationDetail calibrationDetail);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Calibration> calibrations);

    @Update
    void update(Calibration calibration);

    @Delete
    void delete(Calibration calibration);

    @Query("DELETE FROM calibration WHERE uid = :uuid")
    void deleteCalibrations(String uuid);
}