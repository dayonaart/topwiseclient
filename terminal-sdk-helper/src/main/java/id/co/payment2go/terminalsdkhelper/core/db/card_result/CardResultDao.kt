package id.co.payment2go.terminalsdkhelper.core.db.card_result

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardResultDao {
    @Query("SELECT * FROM card_result WHERE id = 1")
    suspend fun getResult(): CardResultModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: CardResultModel): Long

    @Query("DELETE FROM card_result WHERE id = 1")
    suspend fun deleteResult()

}