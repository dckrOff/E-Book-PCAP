package uz.dckroff.pcap

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import uz.dckroff.pcap.databinding.ActivityMainBinding

/**
 * Главная активность приложения, содержащая Navigation Component
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка Toolbar
        setSupportActionBar(binding.toolbar)

        // Получение NavController
        navController = findNavController(R.id.nav_host_fragment)

        // Настройка верхних уровней навигации (не показывать стрелку "Назад")
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.contentListFragment,
                R.id.glossaryFragment,
                R.id.bookmarksFragment,
                R.id.notesFragment,
                R.id.quizListFragment
            ),
            binding.drawerLayout
        )

        // Настройка ActionBar с NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Настройка боковой панели навигации
        binding.navView.setupWithNavController(navController)

        // Настройка нижней панели навигации
        binding.bottomNavView.setupWithNavController(navController)

        // Обработка нажатий элементов в боковой панели
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home, R.id.nav_contents, R.id.nav_glossary, R.id.nav_bookmarks -> {
                    // Используем стандартную навигацию для фрагментов
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    binding.navView.setupWithNavController(navController)
                    binding.navView.setCheckedItem(menuItem.itemId)
                    true
                }
                R.id.nav_tests -> {
                    Timber.d("Переход к тестам")
                    // Переход к списку тестов
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.quizListFragment)
                    true
                }
                R.id.nav_settings -> {
                    Timber.d("Переход к настройкам")
                    // Здесь можно реализовать переход к настройкам
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_about -> {
                    Timber.d("Показать информацию о приложении")
                    // Здесь можно реализовать показ информации о приложении
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        // Настройка переключателя для выдвижной панели
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Timber.d("Переход к настройкам из меню")
                // Здесь можно реализовать переход к настройкам
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}