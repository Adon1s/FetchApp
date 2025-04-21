package com.example.fetchapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fetchapp.ListItem

@Composable
fun ItemsListScreen(items: List<ListItem>) {

    if (items.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No items to display",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        val groupedItems = items.groupBy { it.listId }

        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            groupedItems.keys.sorted().forEach { listId ->
                item {
                    ListHeader(listId = listId)
                }

                val sortedItems = groupedItems[listId]?.sortedBy { it.id } ?: emptyList()
                items(sortedItems) { item ->
                    ItemRow(item = item)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ListHeader(listId: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "List $listId",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Divider(
            color = MaterialTheme.colors.primary,
            thickness = 2.dp
        )
    }
}

@Composable
fun ItemRow(item: ListItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ID: ${item.id}",
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Name: ${item.name}",
                style = MaterialTheme.typography.body2
            )
        }
    }
}