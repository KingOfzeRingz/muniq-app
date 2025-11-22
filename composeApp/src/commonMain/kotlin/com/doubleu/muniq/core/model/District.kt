package com.doubleu.muniq.core.model

data class District(
    val id: String,
    val name: String,
    val overallScore: Int,
    val metrics: List<Metric>,
    val scores: com.doubleu.muniq.data.model.DistrictScoresDto  // Raw scores for dynamic calculation
)