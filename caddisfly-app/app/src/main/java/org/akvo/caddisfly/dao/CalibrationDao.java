package org.akvo.caddisfly.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Query;

import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;

@Dao
public interface CalibrationDao {

    @Query("SELECT * FROM calibrationdetail WHERE uid = :uuid")
    CalibrationDetail getCalibrationDetails(String uuid);

    @Delete
    void delete(Calibration calibration);
}