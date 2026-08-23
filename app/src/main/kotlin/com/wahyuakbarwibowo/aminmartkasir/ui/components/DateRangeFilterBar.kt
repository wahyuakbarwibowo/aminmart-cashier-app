package com.wahyuakbarwibowo.aminmartkasir.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Baris filter periode: Semua / Hari Ini / 7 Hari / Bulan Ini / Kustom.
 * Tanggal dikirim balik dalam format "yyyy-MM-dd".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterBar(
    startDate: String,
    endDate: String,
    onRangeChange: (String, String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apiFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val labelFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    var showRangePicker by remember { mutableStateOf(false) }

    fun applyLastDays(daysAgo: Int) {
        val end = Calendar.getInstance()
        val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
        onRangeChange(apiFormat.format(start.time), apiFormat.format(end.time))
    }

    fun applyThisMonth() {
        val end = Calendar.getInstance()
        val start = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        onRangeChange(apiFormat.format(start.time), apiFormat.format(end.time))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = startDate.isBlank(),
            onClick = onClear,
            label = { Text("Semua") }
        )
        FilterChip(
            selected = false,
            onClick = { applyLastDays(0) },
            label = { Text("Hari Ini") }
        )
        FilterChip(
            selected = false,
            onClick = { applyLastDays(7) },
            label = { Text("7 Hari") }
        )
        FilterChip(
            selected = false,
            onClick = { applyThisMonth() },
            label = { Text("Bulan Ini") }
        )
        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            InputChip(
                selected = true,
                onClick = { showRangePicker = true },
                label = {
                    Text(
                        "${labelFormat.format(apiFormat.parse(startDate)!!)} - " +
                            labelFormat.format(apiFormat.parse(endDate)!!)
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Hapus filter",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onClear() }
                    )
                }
            )
        } else {
            FilterChip(
                selected = false,
                onClick = { showRangePicker = true },
                label = { Text("Kustom") },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
        }
    }

    if (showRangePicker) {
        val rangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = rangePickerState.selectedStartDateMillis
                        val endMillis = rangePickerState.selectedEndDateMillis
                        if (startMillis != null && endMillis != null) {
                            // DateRangePicker mengembalikan millis UTC tengah malam.
                            val utcFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onRangeChange(utcFormat.format(Date(startMillis)), utcFormat.format(Date(endMillis)))
                        }
                        showRangePicker = false
                    },
                    enabled = rangePickerState.selectedStartDateMillis != null &&
                        rangePickerState.selectedEndDateMillis != null
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) { Text("Batal") }
            }
        ) {
            DateRangePicker(state = rangePickerState, modifier = Modifier.weight(1f))
        }
    }
}
