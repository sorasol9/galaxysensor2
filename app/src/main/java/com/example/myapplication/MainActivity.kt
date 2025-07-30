package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.HeartRateData
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectManager: HealthConnectManager

    // UI에 표시할 데이터를 저장할 변수들
    private var heartRateDataList = mutableStateOf<List<HeartRateData>>(emptyList())
    private var bodyTemperatureData = mutableStateOf<Double>(0.0)

    private val permissionLauncher =
        registerForActivityResult<Set<String>, Set<String>>(
            PermissionController.createRequestPermissionResultContract()
        ) { granted: Set<String> ->
            timber.log.Timber.tag("HEALTH_SYNC").d("권한 요청 결과: $granted")
            if (granted.containsAll(healthConnectManager.permissions)) {
                fetchAndSend { heartRates, bodyTemp ->
                    heartRateDataList.value = heartRates
                    bodyTemperatureData.value = bodyTemp
                }
            } else {
                Log.e("HEALTH_SYNC", "권한 요청 실패")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HEALTH_SYNC", "앱 실행됨")
        healthConnectManager = HealthConnectManager(this)

        setContent {
            val heartRates by heartRateDataList
            val bodyTemp by bodyTemperatureData

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "실버포션",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // 심박수 데이터 표시
                Text(
                    text = "심박수 데이터: ${
                        if (heartRates.isNotEmpty()) {
                            heartRates.joinToString(", ") { "${it.bpm} bpm" }
                        } else {
                            "데이터 없음"
                        }
                    }",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 체온 데이터 표시
                Text(
                    text = "체온: ${if (bodyTemp > 0) "%.1f°C".format(bodyTemp) else "데이터 없음"}",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(onClick = {
                    permissionLauncher.launch(healthConnectManager.permissions)
                }) {
                    Text(text = "데이터 가져오기 및 서버 전송")
                }
            }
        }
    }

    private fun fetchAndSend(onDataFetched: (List<HeartRateData>, Double) -> Unit) {
        Log.d("HEALTH_SYNC", "fetchAndSend 실행됨")
        lifecycleScope.launch {
            try {
                // 1️⃣ 심박수 데이터 읽기
                val heartRateRecords = healthConnectManager.readHeartRates()

                // ✅ 가장 최신 샘플만 추출 (모든 samples 중 최신 시간 기준)
                val latestSample = heartRateRecords
                    .flatMap { it.samples }
                    .maxByOrNull { it.time }

                val heartRateData = listOf(
                    HeartRateData(
                        bpm = latestSample?.beatsPerMinute?.toDouble() ?: 0.0,
                        time = latestSample?.time.toString()
                    )
                )

                // 2️⃣ 체온 데이터 읽기 (이 부분은 그대로)
                val bodyTempRecords = healthConnectManager.readBodyTemperature()
                val latestBodyTemp = bodyTempRecords.lastOrNull()?.temperature?.inCelsius ?: 0.0

                Log.d("HEALTH_SYNC", "심박수 데이터: ${heartRateData.size}개")
                Log.d("HEALTH_SYNC", "체온 데이터: $latestBodyTemp°C")

                // 3️⃣ UI에 최신 데이터 전달
                onDataFetched(heartRateData, latestBodyTemp)

                // 4️⃣ 서버로 전송
                val healthData = HealthData(
                    heartRateData = heartRateData,
                    bodyTemperature = latestBodyTemp
                )
                RetrofitClient.apiService.sendHealthData(healthData)

            } catch (e: Exception) {
                Log.e("HEALTH_SYNC", "에러 발생", e)
            }
        }
    }
}

// HealthData 클래스를 심박수와 체온만 포함하도록 수정
data class HealthData(
    val heartRateData: List<HeartRateData>,
    val bodyTemperature: Double
)