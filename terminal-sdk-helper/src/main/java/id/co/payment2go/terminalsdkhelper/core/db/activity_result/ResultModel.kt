package id.co.payment2go.terminalsdkhelper.core.db.activity_result

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "result")
data class ResultModel(
    val result: String,
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1
)
