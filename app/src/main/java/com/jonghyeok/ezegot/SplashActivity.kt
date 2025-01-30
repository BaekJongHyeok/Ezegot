package com.jonghyeok.ezegot

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.api.StationInfoDTO
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Egegot_mkTheme {
                SplashScreen()
            }
        }

        // 데이터를 가져온 후 MainActivity로 이동
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getStations()

                // stationList가 비어 있지 않으면 SharedPreferences에 저장
                if (response.stationList.isNotEmpty()) {
                    saveStationListToPreferences(response.stationList)
                    navigateToMainActivity()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveStationListToPreferences(stationList: List<StationInfoDTO>) {
        val sharedPreferences = getSharedPreferences("default", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(stationList) // List<Station>을 JSON 문자열로 변환
        editor.putString("stationList", json)
        editor.apply() // 저장
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = App_Background_Color
    ) { }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    Egegot_mkTheme {
        SplashScreen()
    }
}
