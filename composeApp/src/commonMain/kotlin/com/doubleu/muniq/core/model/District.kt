package com.doubleu.muniq.core.model

data class District(
    val id: String,
    val name: String,
    val overallScore: Int,
    val metrics: List<Metric>
)