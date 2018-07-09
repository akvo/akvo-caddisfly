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

    @Query("SELECT * FROM calibrationdetail WHERE uid = :uuid")
    CalibrationDetail getCalibrationDetails(String uuid);

    @Delete
    void delete(Calibration calibration);
}