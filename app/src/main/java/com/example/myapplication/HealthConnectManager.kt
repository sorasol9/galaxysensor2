package com.example.myapplication

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import retrofit2.http.Body
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectManager(private val context: Context) {
    //    생성자에서 Context타입의 값을 받아서 클래스안에 context라는 이름으로 읽기 전용(private val)로 저장함(val은 변경할 수 없는 값)
//   Context는 안드로이드에서 현재 앱,화면,컴포넌트에 대한 정보
//    HealthConnectClient는 앱에서 헬스커넥트 기능을 사용하기 위한 객체
//    내 휴대폰에 있는 헬스커넥트 앱과 연결해서 클라이언트를 만드는 코드
    val healthConnectClient = HealthConnectClient.getOrCreate(context)

    //  setOf는 여러 개의 값을 가진 집합자료형 함수
//    HeartRateRecord::class 는 HeartRateRecord 클래스 자체를 가리킴
    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),

    )



    // suspend->코루틴(비동기처리)에서 사용할 수 있는 함수 , fun은 함수 선언 , :List<HeartRateRecord>는 반환 타입
    suspend fun readHeartRates(): List<HeartRateRecord> {
        val now = Instant.now() //현재시간
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant() // 오늘 자정 시간 기준

        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class, //어떤 종류의 기록을 읽을건지(HeartRateRecord)
            timeRangeFilter = TimeRangeFilter.between(todayStart, now) //어느 시간 범위를 읽을 건지->여기선 자정부터 현재
        )
        return healthConnectClient.readRecords(request).records//요청객체를 사용해서 위에서 만든request를 주고 records를 반환받음
    } //즉 이 앱이 readHeartRates함수를 호출하면, List<HeartRateRecord>에 데이터가 담기는 것\

    // 걸음수 데이터를 읽는 함수
    suspend fun readBodyTemperature(): List<BodyTemperatureRecord> {
        val now = Instant.now()
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant() // 오늘 자정 시간 기준

        val request = ReadRecordsRequest(
            recordType = BodyTemperatureRecord::class, // 걸음수 데이터를 읽음
            timeRangeFilter = TimeRangeFilter.between(todayStart, now) // 시간 범위 지정
        )
        return healthConnectClient.readRecords(request).records
    }


}