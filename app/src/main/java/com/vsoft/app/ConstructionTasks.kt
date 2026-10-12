package com.vsoft.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class ConstructionTask(
    val id: Long,
    val workplaceId: Long,
    val title: String,
    val floor: String,
    val progress: Int,
    val daysWorked: Int,
    val totalMinutes: Int,
    val workerMode: String,
    val peopleNames: String,
    val notes: String = ""
)

fun encodeConstructionTasks(tasks: List<ConstructionTask>): String = JSONArray().apply {
    tasks.forEach { task ->
        put(JSONObject().apply {
            put("id", task.id)
            put("workplaceId", task.workplaceId)
            put("title", task.title)
            put("floor", task.floor)
            put("progress", task.progress)
            put("daysWorked", task.daysWorked)
            put("totalMinutes", task.totalMinutes)
            put("workerMode", task.workerMode)
            put("peopleNames", task.peopleNames)
            put("notes", task.notes)
        })
    }
}.toString()

fun decodeConstructionTasks(value: String): MutableList<ConstructionTask> {
    val result = mutableListOf<ConstructionTask>()
    return try {
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(ConstructionTask(
                id = o.optLong("id"),
                workplaceId = o.optLong("workplaceId"),
                title = o.optString("title", ""),
                floor = o.optString("floor", ""),
                progress = o.optInt("progress", 0).coerceIn(0, 100),
                daysWorked = o.optInt("daysWorked", 0).coerceAtLeast(0),
                totalMinutes = o.optInt("totalMinutes", 0).coerceAtLeast(0),
                workerMode = o.optString("workerMode", "self"),
                peopleNames = o.optString("peopleNames", ""),
                notes = o.optString("notes", "")
            ))
        }
        result
    } catch (_: Exception) { mutableListOf() }
}

@Composable
fun ConstructionTasksPage(
    workplace: Workplace,
    tasks: List<ConstructionTask>,
    people: List<Person>,
    onTasksChange: (MutableList<ConstructionTask>) -> Unit
) {
    var editing by remember(workplace.id) { mutableStateOf<ConstructionTask?>(null) }
    var showAdd by remember(workplace.id) { mutableStateOf(false) }
    val progress = if (tasks.isEmpty()) 0 else tasks.map { it.progress }.average().toInt()
    val totalMinutes = tasks.sumOf { it.totalMinutes }
    val totalDays = tasks.sumOf { it.daysWorked }
    val completeCount = tasks.count { it.progress >= 100 }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Column(
                        Modifier.fillMaxWidth().background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(Color(0xFF125C55), Color(0xFF16877B), Color(0xFF51A69B))
                            )
                        ).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (LocalVsoftLanguage.current == "en") "CONSTRUCTION PROJECT" else "پروژه ساختمانی", color = Color.White.copy(alpha = .75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(workplace.name, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                                Text(if (LocalVsoftLanguage.current == "en") "$completeCount of ${tasks.size} tasks completed" else "${tasks.size} فعالیت • $completeCount تکمیل‌شده", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                            }
                            Icon(Icons.Default.Apartment, null, tint = Color.White, modifier = Modifier.size(34.dp))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProjectMetric(if (LocalVsoftLanguage.current == "en") "Overall progress" else "پیشرفت کلی", "$progress%", Modifier.weight(1f))
                            ProjectMetric(if (LocalVsoftLanguage.current == "en") "Work days" else "روز کاری", "$totalDays", Modifier.weight(1f))
                            ProjectMetric(if (LocalVsoftLanguage.current == "en") "Total hours" else "مجموع ساعت", String.format(Locale.US, "%.1f", totalMinutes / 60.0), Modifier.weight(1f))
                        }
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = .22f)
                        )
                        Text(
                            if (LocalVsoftLanguage.current == "en") "Project progress is the average of its activities." else "پیشرفت کلی، میانگین درصد پیشرفت فعالیت‌های این پروژه است.",
                            color = Color.White.copy(alpha = .78f), fontSize = 11.sp
                        )
                    }
                }
            }
            item {
                Text(if (LocalVsoftLanguage.current == "en") "Construction activities" else "فعالیت‌های ساختمانی", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            if (tasks.isEmpty()) {
                item {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.WorkHistory, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                            Text(if (LocalVsoftLanguage.current == "en") "No construction activities yet" else "هنوز فعالیت ساختمانی ثبت نشده", fontWeight = FontWeight.Bold)
                            Text(if (LocalVsoftLanguage.current == "en") "Add the first task for this workplace." else "اولین فعالیت این مکان کاری را اضافه کن.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(tasks.sortedByDescending { it.id }, key = { it.id }) { task ->
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .08f))
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(task.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        (if (LocalVsoftLanguage.current == "en") "Floor: " else "طبقه: ") + task.floor.ifBlank { if (LocalVsoftLanguage.current == "en") "Not specified" else "مشخص نشده" },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                                    )
                                }
                                Text("${task.progress}%", color = if (task.progress == 100) Color(0xFF16845B) else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                                IconButton(onClick = { editing = task }) { Icon(Icons.Default.Edit, contentDescription = if (LocalVsoftLanguage.current == "en") "Edit task" else "ویرایش فعالیت") }
                            }
                            LinearProgressIndicator(progress = { task.progress / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(8.dp)))
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                LabelValue(Icons.Default.Schedule, "${task.daysWorked} ${if (LocalVsoftLanguage.current == "en") "days" else "روز"}")
                                LabelValue(Icons.Default.Schedule, String.format(Locale.US, "%.1f", task.totalMinutes / 60.0) + " " + if (LocalVsoftLanguage.current == "en") "hours" else "ساعت")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(workerDescription(task, LocalVsoftLanguage.current == "en"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (task.notes.isNotBlank()) Text(task.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (task.progress == 100) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16845B), modifier = Modifier.size(17.dp))
                                Text(if (LocalVsoftLanguage.current == "en") "Completed" else "تکمیل شده", color = Color(0xFF16845B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            shape = RoundedCornerShape(18.dp)
        ) { Icon(Icons.Default.Add, contentDescription = if (LocalVsoftLanguage.current == "en") "Add activity" else "افزودن فعالیت") }
    }

    if (showAdd) ConstructionTaskDialog(
        workplace = workplace, initial = null, people = people,
        onDismiss = { showAdd = false },
        onSave = { task -> onTasksChange((tasks + task).toMutableList()); showAdd = false }
    )
    editing?.let { task ->
        ConstructionTaskDialog(
            workplace = workplace, initial = task, people = people,
            onDismiss = { editing = null },
            onSave = { updated -> onTasksChange(tasks.map { if (it.id == updated.id) updated else it }.toMutableList()); editing = null },
            onDelete = { onTasksChange(tasks.filterNot { it.id == task.id }.toMutableList()); editing = null }
        )
    }
}

@Composable
private fun ProjectMetric(title: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = .13f)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Color.White.copy(alpha = .75f), fontSize = 10.sp, maxLines = 1)
        Text(value, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun LabelValue(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun workerDescription(task: ConstructionTask, english: Boolean): String = when (task.workerMode) {
    "self" -> if (english) "I did this myself" else "خودم به‌تنهایی انجام دادم"
    "team" -> (if (english) "Me with others" else "من و دیگران") + if (task.peopleNames.isBlank()) "" else ": ${task.peopleNames}"
    else -> (if (english) "Done by someone else" else "فرد دیگری انجام داد") + if (task.peopleNames.isBlank()) "" else ": ${task.peopleNames}"
}

@Composable
private fun ConstructionTaskDialog(
    workplace: Workplace,
    initial: ConstructionTask?,
    people: List<Person>,
    onDismiss: () -> Unit,
    onSave: (ConstructionTask) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var floor by remember(initial?.id) { mutableStateOf(initial?.floor.orEmpty()) }
    var days by remember(initial?.id) { mutableStateOf((initial?.daysWorked ?: 0).toString()) }
    var hours by remember(initial?.id) { mutableStateOf(String.format(Locale.US, "%.1f", (initial?.totalMinutes ?: 0) / 60.0)) }
    var progress by remember(initial?.id) { mutableFloatStateOf((initial?.progress ?: 0).toFloat()) }
    var workerMode by remember(initial?.id) { mutableStateOf(initial?.workerMode ?: "self") }
    var peopleNames by remember(initial?.id) { mutableStateOf(initial?.peopleNames.orEmpty()) }
    var notes by remember(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) (if (LocalVsoftLanguage.current == "en") "New construction activity" else "فعالیت ساختمانی جدید") else (if (LocalVsoftLanguage.current == "en") "Edit activity" else "ویرایش فعالیت"), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).then(Modifier), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (LocalVsoftLanguage.current == "en") "Activity name" else "نام فعالیت") })
                OutlinedTextField(floor, { floor = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (LocalVsoftLanguage.current == "en") "Floor / level" else "طبقه / سطح") }, placeholder = { Text(if (LocalVsoftLanguage.current == "en") "e.g. Ground floor" else "مثلاً طبقه همکف") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(days, { days = it.filter(Char::isDigit).take(4) }, modifier = Modifier.weight(1f), singleLine = true, label = { Text(if (LocalVsoftLanguage.current == "en") "Work days" else "روزهای کاری") })
                    OutlinedTextField(hours, { hours = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.').take(7) }, modifier = Modifier.weight(1f), singleLine = true, label = { Text(if (LocalVsoftLanguage.current == "en") "Total hours" else "مجموع ساعت") })
                }
                Text((if (LocalVsoftLanguage.current == "en") "Progress" else "درصد پیشرفت") + ": ${progress.toInt()}%", fontWeight = FontWeight.Bold)
                Slider(value = progress, onValueChange = { progress = it }, valueRange = 0f..100f, steps = 99)
                Text(if (LocalVsoftLanguage.current == "en") "Who did the work?" else "چه کسی کار را انجام داد؟", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = workerMode == "self", onClick = { workerMode = "self" }, label = { Text(if (LocalVsoftLanguage.current == "en") "Me alone" else "فقط خودم") })
                    FilterChip(selected = workerMode == "team", onClick = { workerMode = "team" }, label = { Text(if (LocalVsoftLanguage.current == "en") "With others" else "با دیگران") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = workerMode == "other", onClick = { workerMode = "other" }, label = { Text(if (LocalVsoftLanguage.current == "en") "Someone else" else "فرد دیگر") })
                }
                if (workerMode != "self") {
                    OutlinedTextField(
                        peopleNames,
                        { peopleNames = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (LocalVsoftLanguage.current == "en") "Person(s) / collaborator" else "نام فرد یا همکاران") },
                        placeholder = { Text(people.take(3).joinToString("، ") { it.name }) }
                    )
                }
                OutlinedTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text(if (LocalVsoftLanguage.current == "en") "Notes (optional)" else "یادداشت (اختیاری)") })
                Text(if (LocalVsoftLanguage.current == "en") "Days count days you actually worked; hours are the total time spent." else "روزها فقط روزهایی هستند که واقعاً کار کردی؛ ساعت، مجموع زمان صرف‌شده است.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val totalMinutes = ((hours.replace(',', '.').toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100000.0) * 60.0).toInt()
                    onSave(ConstructionTask(
                        id = initial?.id ?: System.currentTimeMillis(),
                        workplaceId = workplace.id,
                        title = title.trim(),
                        floor = floor.trim(),
                        progress = progress.toInt().coerceIn(0, 100),
                        daysWorked = (days.toIntOrNull() ?: 0).coerceAtLeast(0),
                        totalMinutes = totalMinutes,
                        workerMode = workerMode,
                        peopleNames = peopleNames.trim(),
                        notes = notes.trim()
                    ))
                },
                enabled = title.isNotBlank()
            ) { Text(if (LocalVsoftLanguage.current == "en") "Save" else "ذخیره") }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) TextButton(onClick = onDelete) { Text(if (LocalVsoftLanguage.current == "en") "Delete" else "حذف", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text(if (LocalVsoftLanguage.current == "en") "Cancel" else "لغو") }
            }
        }
    )
}
