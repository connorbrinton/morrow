package family.morrow

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale

private val Ink = Color(0xFF173E39)
private val Cream = Color(0xFFF6F1E7)
class MainActivity : ComponentActivity() {
    private var speech: TextToSpeech? = null
    private var voiceReady by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val offlineVoice = speech?.voices?.filter {
                    it.locale.language == Locale.ENGLISH.language && !it.isNetworkConnectionRequired
                }?.sortedBy { if (it.locale.country == Locale.US.country) 0 else 1 }?.firstOrNull()
                voiceReady = offlineVoice != null && speech?.setVoice(offlineVoice) == TextToSpeech.SUCCESS
            }
        }
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Ink, background = Cream, surface = Cream)) { App() } }
    }
    override fun onStop() { speech?.stop(); super.onStop() }
    override fun onDestroy() { speech?.shutdown(); super.onDestroy() }
    @Composable private fun App() {
        val prefs = remember { getSharedPreferences("settings", MODE_PRIVATE) }
        val pack = remember { Content.load(this) }
        var page by rememberSaveable { mutableStateOf("home") }
        var lessonDate by rememberSaveable { mutableStateOf(pack.today(LocalDate.now()).date.toString()) }
        val lesson = pack.weeks.flatMap { it.days }.find { it.date.toString() == lessonDate } ?: pack.today(LocalDate.now())
        var stage by rememberSaveable { mutableIntStateOf(0) }
        var translation by remember { mutableStateOf(prefs.getString("translation", "KJV")!!) }
        var completed by remember { mutableStateOf(prefs.getStringSet("completed", emptySet())!!.toSet()) }
        val readingScroll = rememberScrollState()
        val homeFocus = remember { FocusRequester() }
        val nextFocus = remember { FocusRequester() }
        LaunchedEffect(page, stage, lessonDate, translation) { speech?.stop(); readingScroll.scrollTo(0)
            withFrameNanos { }
            if (page == "home") homeFocus.requestFocus() else if (page == "study") nextFocus.requestFocus()
        }
        BackHandler(page != "home") { if (page == "study" && stage > 0) stage-- else page = "home" }
        Column(Modifier.fillMaxSize().background(Cream).padding(horizontal = 40.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("MORROW", color = Ink, fontSize = 18.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (page != "home") Action("Home") { page = "home" }
                Spacer(Modifier.width(12.dp)); Action("Settings") { page = "settings" }
            }
            Spacer(Modifier.height(12.dp))
            when (page) {
                "home" -> {
                    Text("A little time together", fontSize = 34.sp, color = Ink, fontWeight = FontWeight.Bold)
                    Text("Read • wonder • practice   /   About 5–8 minutes", fontSize = 18.sp, color = Ink)
                    Spacer(Modifier.height(10.dp))
                    val today = pack.today(LocalDate.now())
                    Action(if (today.date == LocalDate.now()) "Study today: ${today.title}" else "Study ${today.date}: ${today.title}", modifier = Modifier.focusRequester(homeFocus)) { lessonDate = today.date.toString(); stage = 0; page = "study" }
                    Spacer(Modifier.height(10.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pack.weeks.forEach { w ->
                            item { Text("${w.start}  ·  ${w.title}", fontSize = 22.sp, color = Ink, fontWeight = FontWeight.Bold); Text(w.assignment, fontSize = 16.sp) }
                            items(w.days, key = { it.date }) { d -> Action("${if (d.date.toString() in completed) "✓  " else ""}${d.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}  ·  ${d.title}  /  ${d.reference}") { lessonDate = d.date.toString(); stage = 0; page = "study" } }
                        }
                    }
                }
                "study" -> {
                    val labels = listOf("Discover", "Read", "Understand", "Talk & do", "Remember")
                    val passage = pack.translations[translation]?.get(lesson.reference)
                    val body = when(stage) {
                        0 -> lesson.discover
                        1 -> passage ?: "$translation text is not installed for this passage. Choose KJV below to read it here."
                        2 -> lesson.explain
                        3 -> "For little ones\n${lesson.younger}\n\nFor growing thinkers\n${lesson.older}\n\nTogether\n${lesson.activity}"
                        else -> "${lesson.remember}\n\nPrayer idea: Thank Heavenly Father for what you learned, and ask for help practicing it."
                    }
                    Text("${lesson.date}  ·  ${stage + 1} / 5  ·  ${labels[stage]}", fontSize = 16.sp, color = Ink)
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(lesson.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text(if(stage == 1) "${lesson.reference}  ·  $translation" else if(stage == 2) "Family explanation • not scripture text" else lesson.reference, fontSize = 16.sp, color = Ink)
                            Spacer(Modifier.height(10.dp))
                            Column(Modifier.weight(1f).verticalScroll(readingScroll).focusable()) { Text(body, fontSize = if(stage == 1) 25.sp else 22.sp, lineHeight = 32.sp, color = Ink) }
                        }
                        Illustration(lesson.scene, Modifier.width(150.dp).fillMaxHeight())
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if(stage > 0) Action("Previous") { stage-- }
                        Action(if(stage == 4) "Finish" else "Next", modifier = Modifier.focusRequester(nextFocus)) {
                            if(stage < 4) stage++ else {
                                completed = completed + lesson.date.toString()
                                prefs.edit().putStringSet("completed", completed).apply(); page = "home"
                            }
                        }
                        if(stage == 1 && passage == null) Action("Use KJV") { translation = "KJV"; prefs.edit().putString("translation", "KJV").apply() }
                        Action("Read aloud", voiceReady && (stage != 1 || passage != null)) { speech?.speak(body, TextToSpeech.QUEUE_FLUSH, null, "lesson") }
                        Action("Stop audio") { speech?.stop() }
                    }
                }
                else -> {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Make it yours", fontSize = 32.sp, color = Ink)
                        Text("Bible translation: $translation", fontSize = 23.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("KJV", "ICB").forEach { t -> Action(t) { translation = t; prefs.edit().putString("translation", t).apply() } }
                        }
                        Text("KJV is included. ICB needs a permitted text pack; unavailable passages are clearly labeled. Read aloud requires an installed offline English voice.", fontSize = 17.sp)
                        Text("All lessons and scripture text are included in the app. To change them, edit the weekly YAML files and install a new build.", fontSize = 17.sp)
                        Text("Independent family study app. Not an official Church application. Weekly assignments follow Come, Follow Me; activities and explanations are original family suggestions.", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
@Composable private fun Action(label: String, enabled: Boolean = true, modifier: Modifier = Modifier, click: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Button(onClick = click, enabled = enabled, modifier = modifier.onFocusChanged { focused = it.isFocused }.border(if(focused) 3.dp else 0.dp, Color(0xFFD39739), RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label, fontSize = 17.sp)
    }
}
@Composable private fun Illustration(scene: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Canvas(Modifier.size(140.dp)) {
            val w = size.width; val h = size.height
            drawCircle(Color(0xFFEBD8AE), w * .46f, Offset(w/2, h/2))
            when(scene) {
                "tree" -> { drawRect(Ink, Offset(w*.46f,h*.4f), Size(w*.08f,h*.4f)); drawCircle(Color(0xFF7A9E7E), w*.27f, Offset(w*.5f,h*.36f)); drawLine(Color(0xFF6D8F9B), Offset(w*.1f,h*.85f),Offset(w*.9f,h*.85f),8f) }
                "heart" -> { drawCircle(Color(0xFFB56C58),w*.17f,Offset(w*.38f,h*.42f)); drawCircle(Color(0xFFB56C58),w*.17f,Offset(w*.62f,h*.42f)); val p=androidx.compose.ui.graphics.Path().apply { moveTo(w*.22f,h*.46f); lineTo(w*.78f,h*.46f); lineTo(w*.5f,h*.8f); close() }; drawPath(p,Color(0xFFB56C58)) }
                else -> { drawRoundRect(Ink,Offset(w*.18f,h*.35f),Size(w*.64f,h*.4f),androidx.compose.ui.geometry.CornerRadius(12f)); drawLine(Cream,Offset(w*.5f,h*.38f),Offset(w*.5f,h*.72f),5f); drawCircle(Color(0xFFD39739),w*.12f,Offset(w*.7f,h*.2f)) }
            }
        }
        Text(when(scene) { "tree" -> "Grow together"; "heart" -> "Love & care"; else -> "Learn together" }, color = Ink, fontSize = 16.sp)
    }
}
