package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SubjectGradeItem(
    val id: String,
    val name: String,
    val grade: Double, // -1.0 means "Sem nota registrada"
    val topicsToReview: String
)

object StudentScheduleManager {
    private const val PREFS_NAME = "crux_student_schedule"
    private const val KEY_SHIFT = "student_shift" // "Manhã", "Tarde", "Integral", "Noite"
    private const val KEY_SUBJECTS_JSON = "subjects_json"

    val AVAILABLE_SHIFTS = listOf("Manhã", "Tarde", "Integral", "Noite")

    private var prefs: SharedPreferences? = null

    private val _shiftFlow = MutableStateFlow("Manhã")
    val shiftFlow: StateFlow<String> = _shiftFlow.asStateFlow()

    private val _subjectsFlow = MutableStateFlow<List<SubjectGradeItem>>(emptyList())
    val subjectsFlow: StateFlow<List<SubjectGradeItem>> = _subjectsFlow.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadData()
        }
    }

    private fun loadData() {
        val savedShift = prefs?.getString(KEY_SHIFT, "Manhã") ?: "Manhã"
        _shiftFlow.value = savedShift

        val jsonStr = prefs?.getString(KEY_SUBJECTS_JSON, null)
        if (jsonStr.isNull_or_blank()) {
            val defaultList = getDefaultSubjects()
            _subjectsFlow.value = defaultList
            saveSubjectsToPrefs(defaultList)
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<SubjectGradeItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SubjectGradeItem(
                            id = obj.optString("id", "sub_$i"),
                            name = obj.optString("name", "Matéria"),
                            grade = obj.optDouble("grade", -1.0),
                            topicsToReview = obj.optString("topics", "")
                        )
                    )
                }
                _subjectsFlow.value = list
            } catch (e: Exception) {
                val defaultList = getDefaultSubjects()
                _subjectsFlow.value = defaultList
                saveSubjectsToPrefs(defaultList)
            }
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

    private fun getDefaultSubjects(): List<SubjectGradeItem> {
        return listOf(
            SubjectGradeItem("sub_1", "Matemática", -1.0, "Álgebra, Funções e Geometria"),
            SubjectGradeItem("sub_2", "Língua Portuguesa", -1.0, "Interpretação de Texto e Gramática"),
            SubjectGradeItem("sub_3", "Física", -1.0, "Mecânica, Cinemática e Termodinâmica"),
            SubjectGradeItem("sub_4", "Química", -1.0, "Estequiometria e Tabela Periódica"),
            SubjectGradeItem("sub_5", "Biologia", -1.0, "Citologia, Genética e Ecologia"),
            SubjectGradeItem("sub_6", "História", -1.0, "História do Brasil e Era Contemporânea"),
            SubjectGradeItem("sub_7", "Geografia", -1.0, "Geopolítica e Cartografia"),
            SubjectGradeItem("sub_8", "Inglês", -1.0, "Grammar and Reading Comprehension")
        )
    }

    fun setShift(shift: String) {
        prefs?.edit()?.putString(KEY_SHIFT, shift)?.apply()
        _shiftFlow.value = shift
    }

    fun updateSubjectGrade(id: String, newGrade: Double, newTopics: String? = null) {
        val currentList = _subjectsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = currentList[index]
            currentList[index] = item.copy(
                grade = newGrade,
                topicsToReview = newTopics ?: item.topicsToReview
            )
            _subjectsFlow.value = currentList
            saveSubjectsToPrefs(currentList)
        }
    }

    fun addSubject(name: String, grade: Double, topics: String) {
        val currentList = _subjectsFlow.value.toMutableList()
        val newId = "sub_${System.currentTimeMillis()}"
        currentList.add(SubjectGradeItem(newId, name, grade, topics))
        _subjectsFlow.value = currentList
        saveSubjectsToPrefs(currentList)
    }

    fun removeSubject(id: String) {
        val currentList = _subjectsFlow.value.filterNot { it.id == id }
        _subjectsFlow.value = currentList
        saveSubjectsToPrefs(currentList)
    }

    private fun saveSubjectsToPrefs(list: List<SubjectGradeItem>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("grade", item.grade)
            obj.put("topics", item.topicsToReview)
            array.put(obj)
        }
        prefs?.edit()?.putString(KEY_SUBJECTS_JSON, array.toString())?.apply()
    }

    fun buildStudyPlanText(): String {
        val shift = _shiftFlow.value
        val subjects = _subjectsFlow.value

        // Prioritize subjects with lowest grade (>= 0) or unassessed (-1)
        val unassessed = subjects.filter { it.grade == -1.0 }
        val lowGrades = subjects.filter { it.grade in 0.0..6.9 }.sortedBy { it.grade }
        val goodGrades = subjects.filter { it.grade >= 7.0 }

        val focusSubjects = (lowGrades + unassessed).take(3).ifEmpty { subjects.take(3) }

        val sb = StringBuilder()
        sb.append("📚 Plano de Estudos - Turno da ").append(shift).append(":\n\n")

        if (focusSubjects.isNotEmpty()) {
            sb.append("🎯 Principais Matérias para Revisar Hoje:\n")
            focusSubjects.forEach { sub ->
                val gradeStr = if (sub.grade == -1.0) "Sem nota registrada (-1)" else "Nota: ${sub.grade}"
                sb.append("• ").append(sub.name).append(" (").append(gradeStr).append(")\n")
                if (sub.topicsToReview.isNotBlank()) {
                    sb.append("  ➜ Foco: ").append(sub.topicsToReview).append("\n")
                }
            }
        }

        if (unassessed.isNotEmpty()) {
            sb.append("\n⚠️ Matérias pendentes de avaliação: ")
                .append(unassessed.joinToString(", ") { it.name })
        }

        return sb.toString()
    }

    fun triggerDailyNotification(context: Context): String {
        val shift = _shiftFlow.value
        val planText = buildStudyPlanText()

        val channelId = "crux_study_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes de Estudo & Revisão",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações diárias do seu plano de estudo e matérias no Crux Tutor"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title = "🔔 Crux Tutor: Suas aulas e o que revisar (${shift})"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_crux_logo_1786280288578)
            .setContentTitle(title)
            .setContentText("Confira seu plano de revisão para o turno da $shift!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(planText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(1001, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return planText
    }
}
