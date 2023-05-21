package com.tamboto.hospitalreservation.helper

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceHelper {
  private const val PREF_FILE = "PREF"

  const val IS_LOGGED_IN = "is_logged_in"
  const val ID = "id"
  const val NAME = "name"
  const val PHONE = "phone"
  const val ADDRESS = "address"
  const val EMAIL = "email"
  const val ROLE = "role"

  /**
   * Set a string shared preference
   *
   * @param key   - Key to set shared preference
   * @param value - Value for the key
   */
  fun setSharedPreferenceString(context: Context, key: String?, value: String?) {
    val settings: SharedPreferences = context.getSharedPreferences(PREF_FILE, 0)
    val editor = settings.edit()
    editor.putString(key, value)
    editor.apply()
  }

  /**
   * Set a integer shared preference
   *
   * @param key   - Key to set shared preference
   * @param value - Value for the key
   */
  fun setSharedPreferenceInt(context: Context, key: String?, value: Int) {
    val settings: SharedPreferences = context.getSharedPreferences(PREF_FILE, 0)
    val editor = settings.edit()
    editor.putInt(key, value)
    editor.apply()
  }

  /**
   * Set a Boolean shared preference
   *
   * @param key   - Key to set shared preference
   * @param value - Value for the key
   */
  fun setSharedPreferenceBoolean(context: Context, key: String?, value: Boolean) {
    val settings: SharedPreferences = context.getSharedPreferences(PREF_FILE, 0)
    val editor = settings.edit()
    editor.putBoolean(key, value)
    editor.apply()
  }

  /**
   * Get a string shared preference
   *
   * @param key      - Key to look up in shared preferences.
   * @param defValue - Default value to be returned if shared preference isn't found.
   * @return value - String containing value of the shared preference if found.
   */
  fun getSharedPreferenceString(context: Context, key: String?, defValue: String?): String {
    val settings: SharedPreferences = context.getSharedPreferences(PREF_FILE, 0)
    return settings.getString(key, defValue) ?: ""
  }

  /**
   * Get a integer shared preference
   *
   * @param key      - Key to look up in shared preferences.
   * @param defValue - Default value to be returned if shared preference isn't found.
   * @return value - String containing value of the shared preference if found.
   */
  fun getSharedPreferenceInt(context: Context, key: String?, defValue: Int): Int {
    val settings: SharedPreferences = context.getSharedPreferences(PREF_FILE, 0)
    return settings.getInt(key, defValue)
  }

  /**
   * Get a boolean shared preference
   *
   * @param key      - Key to look up in shared preferences.
   * @param defValue - Default value to be returned if shared preference isn't found.
   * @return value - String containing value of the shared preference if found.
   */
  fun getSharedPreferenceBoolean(context: Context, key: String?, defValue: Boolean): Boolean {
    val settings: SharedPreferences = context.getSharedPreferences(PREF_FILE, 0)
    return settings.getBoolean(key, defValue)
  }

  fun clearPreferences(context: Context) {
    context.getSharedPreferences(PREF_FILE, 0).edit().clear().apply()
  }
}
