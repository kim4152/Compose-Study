package com.example.composestudy

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable

@NonRestartableComposable
@Composable
fun NonRestartableChild(list: MutableState<List<Int>>){
    Text("${list.value.size}")

}

@Composable
fun NonRestartableParent(list: MutableState<List<Int>>){
    NonRestartableChild(list)
}


