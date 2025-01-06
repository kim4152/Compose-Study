package com.example.composestudy.assignby

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ParentComposable(text: MutableState<String>, number: MutableState<Int>) {
    Column {
        Text(text = "Child 1: $text")
        Text(text = "Child 2: $number")
    }
}

@Composable
fun AssignExample() {
    val text = remember { mutableStateOf("Hello") }
    val number = remember { mutableStateOf(0) }

    Column {
        Button(onClick = { text.value += "!" }) {
            Text("Change Text")
        }
        Button(onClick = { number.value += 1 }) {
            Text("Increment Number")
        }

        ParentComposable(text = text, number = number)
    }
}
