package com.example.composestudy.assignby

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun AssignChild1(text: String) {
    Text(text = "Child 1: ${text}")
}

@Composable
fun AssignChild2(number: ImmutableList<Int>) {
    Text(text = "Child 2: ${number.size}")
}

@Composable
fun ParentComposable(text: String, number: List<Int>) {
    val mutable = mutableListOf(1, 2, 3)
    val list: PersistentList<Int> = mutable.toImmutableList().toPersistentList().mutate { }


    Column {
        AssignChild1(text = text)
        AssignChild2(number = list)
    }
}

@Composable
fun AssignExample() {
    var text by remember { mutableStateOf("Hello") }
    val number = remember { mutableListOf(1, 2, 3) }

    Column {
        Button(onClick = { text += "!" }) {
            Text("Change Text")
        }
        Button(onClick = { }) {
            Text("Increment Number")
        }

        ParentComposable(text = text, number = number)
    }
}
