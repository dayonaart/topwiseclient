package id.co.payment2go.terminalsdkhelper.core.db.activity_result

import androidx.room.Database
import androidx.room.RoomDatabase
import id.co.payment2go.terminalsdkhelper.core.db.card_result.CardResultDao
import id.co.payment2go.terminalsdkhelper.core.db.card_result.CardResultModel

@Database(entities = [ResultModel::class, CardResultModel::class], version = 1)
abstract class ResultDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao
    abstract fun cardResultDao(): CardResultDao
}