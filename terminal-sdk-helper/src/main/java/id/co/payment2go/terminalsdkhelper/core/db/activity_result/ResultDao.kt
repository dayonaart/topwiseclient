package id.co.payment2go.terminalsdkhelper.core.db.activity_result

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ResultDao {
    @Query("SELECT * FROM result WHERE id = 1")
    suspend fun getResult(): ResultModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultModel): Long

    @Query("DELETE FROM result WHERE id = 1")
    suspend fun deleteResult()

}