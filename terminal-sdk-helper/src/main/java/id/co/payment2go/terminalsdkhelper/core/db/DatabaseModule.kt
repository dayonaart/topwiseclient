package id.co.payment2go.terminalsdkhelper.core.db

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.core.db.activity_result.ResultDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideResultDatabase(app: Application): ResultDatabase {
        return Room.databaseBuilder(
            app,
            ResultDatabase::class.java,
            "db_result"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideResultDao(db: ResultDatabase) = db.resultDao()

    @Provides
    @Singleton
    fun provideCardResultDao(db: ResultDatabase) = db.cardResultDao()
}