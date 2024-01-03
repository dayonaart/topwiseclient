package id.co.payment2go.terminalsdkhelper.core.db.card_result

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_result")
data class CardResultModel(
    val result: String,
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1
)
