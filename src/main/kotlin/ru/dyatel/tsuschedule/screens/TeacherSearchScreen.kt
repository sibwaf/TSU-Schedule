package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.TeacherDao
import ru.dyatel.tsuschedule.data.database
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.layout.TeacherListAdapter
import ru.dyatel.tsuschedule.parsing.TeacherParser
import ru.dyatel.tsuschedule.utilities.ReplacingJobLauncher
import ru.dyatel.tsuschedule.utilities.ctx
import ru.dyatel.tsuschedule.utilities.hideKeyboard
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.lang.ref.WeakReference

class TeacherSearchView(context: Context) : BaseScreenView<TeacherSearchScreen>(context) {

    private val teacherRecycler = recyclerView {
        lparams(width = matchParent, height = matchParent)
        layoutManager = LinearLayoutManager(context)
    }

    fun setAdapter(adapter: TeacherListAdapter) {
        teacherRecycler.adapter = adapter
    }

}

class TeacherSearchScreen : Screen<TeacherSearchView>() {

    private lateinit var teacherDao: TeacherDao

    private val adapter = TeacherListAdapter()

    private var query = ""
    private val searchListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(query: String): Boolean {
            updateQuery(query)
            return true
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            if (query.length >= 3) {
                request(query)
            }
            return true
        }
    }

    private val task = ReplacingJobLauncher(UI)

    override fun createView(context: Context) = TeacherSearchView(context)

    override fun onShow(context: Context) {
        super.onShow(context)
        teacherDao = activity.database.teachers
        view.setAdapter(adapter)
    }

    override fun onHide(context: Context) {
        view.findFocus()?.hideKeyboard()
        super.onHide(context)
    }

    fun updateQuery(query: String) {
        this.query = query

        val name = query.trim().takeUnless { it.isEmpty() }
        if (name == null) {
            adapter.updateData(teacherDao.request())
            return
        }

        adapter.updateData(teacherDao.request(name))
    }

    // TODO: progress indicator
    private fun request(name: String) {
        val preferences = ctx!!.schedulePreferences

        val oldData = adapter.getData()
        val adapterReference = WeakReference(adapter)

        task.launch {
            try {
                val teachers = async {
                    val parser = TeacherParser().apply { setTimeout(preferences.connectionTimeout) }
                    val teachers = parser.getTeachers(name)

                    val removed = oldData - teachers

                    removed.forEach { teacherDao.remove(it.id) }
                    teacherDao.save(teachers)

                    teachers.sortedBy { it.name }
                }.await()

                adapterReference.get()?.updateData(teachers)
            } catch (e: Exception) {
                e.handle { message -> view?.let { longSnackbar(it, message) } }
            }
        }
    }

    override fun onUpdateMenu(menu: Menu) {
        val savedQuery = query
        menu.findItem(R.id.search).apply {
            (actionView as SearchView).apply {
                setOnQueryTextListener(searchListener)
                setQuery(savedQuery, false)
            }
            isVisible = true
        }
    }

}
