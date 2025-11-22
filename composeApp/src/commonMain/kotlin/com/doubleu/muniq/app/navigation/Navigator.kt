import com.doubleu.muniq.app.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Navigator {
    private val _current = MutableStateFlow<Screen>(Screen.Splash)
    val current: StateFlow<Screen> = _current

    fun navigate(screen: Screen) {
        _current.value = screen
    }

    fun backToMap() {
        _current.value = Screen.Map
    }
}