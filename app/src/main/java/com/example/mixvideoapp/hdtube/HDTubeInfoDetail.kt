package com.example.mixvideoapp.hdtube

data class HDTubeInfoDetail(
    val views: String,
    val date: String,
    val desciption: String,
    val source: String,
    val listTags: List<String>,
    val listRelated: List<HDTubeVideo>,
    val listModels: List<HDTubeModel>
)
