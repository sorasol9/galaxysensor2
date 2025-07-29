package com.example.myapplication.network

// 심박수 데이터를 담는 데이터 클래스
data class HeartRateData(
    val bpm: Double,  // 분당 심박수
    val time: String  // 측정 시간
)