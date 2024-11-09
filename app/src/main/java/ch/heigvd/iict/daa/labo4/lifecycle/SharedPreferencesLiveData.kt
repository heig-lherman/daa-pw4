package ch.heigvd.iict.daa.labo4.lifecycle

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * A mutable LiveData implementation that observes changes in a SharedPreferences object.
 * It is used to observe changes in the SharedPreferences object and update the UI accordingly.
 *
 * @param T the type of the value to observe
 * @param sharedPreferences the SharedPreferences object to observe
 * @param key the key of the value to observe
 * @param defaultValue the default value of the value to observe
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
abstract class SharedPreferencesLiveData<T>(
    protected val sharedPreferences: SharedPreferences,
    protected val key: String,
    protected val defaultValue: T
) : LiveData<T>() {

    /**
     * Gets the value from the SharedPreferences object.
     *
     * @param key the key of the value to get
     * @param defaultValue the default value of the value to get
     *
     * @return the value from the SharedPreferences object
     */
    protected abstract fun getValueFromPreferences(key: String, defaultValue: T): T

    /**
     * Sets the value to the SharedPreferences object.
     *
     * @param key the key of the value to set
     * @param value the value to set
     */
    protected abstract fun setValueToPreferences(key: String, value: T)

    final override fun onActive() {
        super.onActive()
        value = getValueFromPreferences(key, defaultValue)
        sharedPreferences.registerOnSharedPreferenceChangeListener(changeListener)
    }

    final override fun onInactive() {
        super.onInactive()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(changeListener)
    }

    public final override fun setValue(value: T?) {
        setValueToPreferences(key, value!!)
    }

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == this.key) {
            super.value = getValueFromPreferences(key, defaultValue)
        }
    }
}

/**
 * [SharedPreferencesLiveData] implementation for Enum values.
 *
 * @param T the Enum type of the value to observe
 * @param sharedPreferences the SharedPreferences object to observe
 * @param key the key of the value to observe
 * @param defaultValue the default enum value of the value to observe
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class SharedPreferencesEnumeratedLiveData<T : Enum<T>>(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: T
) : SharedPreferencesLiveData<T>(sharedPreferences, key, defaultValue) {

    private val enumClass = defaultValue::class.java

    override fun getValueFromPreferences(key: String, defaultValue: T): T = sharedPreferences
        .getString(key, defaultValue.name)!!
        .let { enumClass.enumConstants!!.first { enum -> enum.name == it } }

    override fun setValueToPreferences(key: String, value: T) = with(sharedPreferences.edit()) {
        putString(key, value.name)
        apply()
    }
}
