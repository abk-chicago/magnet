package magnet.sample.app.main

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import android.view.Menu
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.message
import kotlinx.android.synthetic.main.activity_main.navigation
import magnet.DependencyScope
import magnet.sample.app.App

class MainActivity : AppCompatActivity() {

    private val pageBinders = SparseArray<PageBinder>()
    private lateinit var activityScope: DependencyScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create dependency scope for this activity
        activityScope = App.appScope.subscope()

        // registered Resources.class making it available to the pages down below
        activityScope.register(Resources::class.java, resources)

        // query registered pages
        val pages = App.implManager.get(
                // they are implementations of type Page.class
                Page::class.java,
                // each page receives own dependency scope that it cannot override values in activity scope
                activityScope.subscope()
        )

        // add queried pages to the menu and register listeners
        pages.forEach {
            PageBinder(it).register(navigation.menu, message, pageBinders)
        }

        // select initial message
        if (savedInstanceState == null) {
            pageBinders[navigation.selectedItemId].updateMessage(message)
        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // restore message according to selected page
        pageBinders[navigation.selectedItemId].updateMessage(message)
    }

}

class PageBinder(
        private val page: Page
) {

    fun register(menu: Menu, message: TextView, pageBinders: SparseArray<PageBinder>) {
        menu.add(0, page.id(), page.order(), page.menuTitleId())
                .setIcon(page.menuIconId())
                .setOnMenuItemClickListener {
                    updateMessage(message)
                    false
                }

        pageBinders.put(page.id(), this)
    }

    fun updateMessage(message: TextView) {
        message.text = page.message()
    }

}