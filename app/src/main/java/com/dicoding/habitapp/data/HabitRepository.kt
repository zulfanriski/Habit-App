package com.dicoding.habitapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dicoding.habitapp.utils.HabitSortType
import com.dicoding.habitapp.utils.SortUtils
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HabitRepository(private val habitDao: HabitDao, private val executor: ExecutorService) {

    companion object {

        @Volatile
        private var instance: HabitRepository? = null

        fun getInstance(context: Context): HabitRepository {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    val database = HabitDatabase.getInstance(context)
                    instance = HabitRepository(
                        database.habitDao(),
                        Executors.newSingleThreadExecutor()
                    )
                }
                return instance as HabitRepository
            }

        }
    }

    //TODO 4 : Use SortUtils.getSortedQuery to create sortable query and build paged list
    fun getHabits(sortType: HabitSortType): LiveData<PagedList<Habit>> {
        val q = SortUtils.getSortedQuery(sortType)
        val habit = habitDao.getHabits(q)

        val config = PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setEnablePlaceholders(true)
            .build()

        return LivePagedListBuilder(habit, config).build()
    }

    //TODO 5 : Complete other function inside repository
    fun getHabitById(habitId: Int): LiveData<Habit> = habitDao.getHabitById(habitId)

    fun insertHabit(newHabit: Habit): Long? {
        val hD = Callable{habitDao.insertHabit(newHabit)}
        val executeData = executor.submit(hD)
        return executeData.get()
    }

    fun deleteHabit(habit: Habit) {
        executor.execute {
            habitDao.deleteHabit(habit)
        }
    }

    fun getRandomHabitByPriorityLevel(level: String): LiveData<Habit> = habitDao.getRandomHabitByPriorityLevel(level)
}