package com.qcwireless.sdksample.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.qcwireless.sdksample.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class AiReportActivity : AppCompatActivity() {

    // ─────────────────────────────────────────────
    // Views
    // ─────────────────────────────────────────────
    private lateinit var btnBack: TextView
    private lateinit var tvRecoveryScore: TextView
    private lateinit var tvRecoveryLabel: TextView
    private lateinit var tvLastUpdated: TextView
    private lateinit var tvHrvReport: TextView
    private lateinit var tvSleepReport: TextView
    private lateinit var tvRhrReport: TextView
    private lateinit var tvSpo2Report: TextView
    private lateinit var tvStressReport: TextView
    private lateinit var tvAiAnalysis: TextView
    private lateinit var tvAiLoadingText: TextView
    private lateinit var layoutAiLoading: LinearLayout
    private lateinit var btnGenerateReport: LinearLayout
    private lateinit var tvGenerateLabel: TextView
    private lateinit var tvError: TextView

    // ─────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_report)
        bindViews()
        populateMetrics()
        setupListeners()
    }

    private fun bindViews() {
        btnBack          = findViewById(R.id.btn_back)
        tvRecoveryScore  = findViewById(R.id.tv_recovery_score)
        tvRecoveryLabel  = findViewById(R.id.tv_recovery_label)
        tvLastUpdated    = findViewById(R.id.tv_last_updated)
        tvHrvReport      = findViewById(R.id.tv_hrv_report)
        tvSleepReport    = findViewById(R.id.tv_sleep_report)
        tvRhrReport      = findViewById(R.id.tv_rhr_report)
        tvSpo2Report     = findViewById(R.id.tv_spo2_report)
        tvStressReport   = findViewById(R.id.tv_stress_report)
        tvAiAnalysis     = findViewById(R.id.tv_ai_analysis)
        tvAiLoadingText  = findViewById(R.id.tv_ai_loading_text)
        layoutAiLoading  = findViewById(R.id.layout_ai_loading)
        btnGenerateReport = findViewById(R.id.btn_generate_report)
        tvGenerateLabel  = findViewById(R.id.tv_generate_label)
        tvError          = findViewById(R.id.tv_error)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnGenerateReport.setOnClickListener { runAnalysis() }
    }

    // ─────────────────────────────────────────────
    // Biometric data
    // Pull from BLE sync cache; fall back to demo values when not connected.
    // In production: replace with your Room DB / SharedPreferences read.
    // ─────────────────────────────────────────────
    private fun loadBiometrics(): BiometricSnapshot {
        val connected = BleOperateManager.getInstance().isConnected
        return if (connected) {
            // TODO: replace with actual cached values from your local DB
            BiometricSnapshot(hrv = null, restingHr = null, sleepHours = null,
                              spo2 = null, stressLevel = null)
        } else {
            // Demo values so the report is meaningful before a device is paired
            BiometricSnapshot(hrv = 52, restingHr = 64, sleepHours = 6.8f,
                              spo2 = 97, stressLevel = 38)
        }
    }

    private fun populateMetrics() {
        val b = loadBiometrics()
        tvHrvReport.text    = b.hrv?.let { "$it ms" }                     ?: "-- ms"
        tvSleepReport.text  = b.sleepHours?.let { "%.1f hrs".format(it) } ?: "-- hrs"
        tvRhrReport.text    = b.restingHr?.let { "$it bpm" }              ?: "-- bpm"
        tvSpo2Report.text   = b.spo2?.let { "$it %" }                     ?: "-- %"
        tvStressReport.text = b.stressLevel?.let { stressLabel(it) }      ?: "--"
    }

    // ─────────────────────────────────────────────
    // Analysis entry point — brief animation then instant on-device result
    // ─────────────────────────────────────────────
    private fun runAnalysis() {
        setLoading(true)
        tvError.visibility = View.GONE

        // Give the UI one frame to show the spinner, then compute synchronously
        // (the scoring + narrative engine is O(1) — no perceptible delay)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val bio    = loadBiometrics()
                val scores = HealthScoreEngine.score(bio)
                val report = NarrativeEngine.generate(bio, scores)
                applyReport(scores, report)
            } catch (e: Exception) {
                tvError.text = "Analysis error: ${e.message}"
                tvError.visibility = View.VISIBLE
            } finally {
                setLoading(false)
            }
        }, 1_200)
    }

    private fun applyReport(scores: MetricScores, report: HealthReport) {
        // Recovery score
        tvRecoveryScore.text = scores.composite.toString()
        val (label, color) = when {
            scores.composite >= 67 -> "Peak Readiness"   to getColor(R.color.hftx_teal)
            scores.composite >= 34 -> "Moderate Recovery" to getColor(R.color.hftx_gold)
            else                   -> "Low Recovery"      to getColor(R.color.hftx_red)
        }
        tvRecoveryScore.setTextColor(color)
        tvRecoveryLabel.text = label

        val ts = SimpleDateFormat("HH:mm · d MMM", Locale.ENGLISH).format(Date())
        tvLastUpdated.text = "Updated at $ts · On-device analysis"

        // Narrative
        tvAiAnalysis.text = report.narrative
        tvAiAnalysis.setTextColor(getColor(R.color.hftx_text_primary))
    }

    private fun setLoading(loading: Boolean) {
        layoutAiLoading.visibility = if (loading) View.VISIBLE else View.GONE
        btnGenerateReport.isEnabled = !loading
        tvGenerateLabel.text = if (loading) "Analysing…" else "✦  Generate AI Report"
    }

    private fun stressLabel(v: Int) = when {
        v < 25 -> "Low ($v)"; v < 50 -> "Moderate ($v)"
        v < 75 -> "Elevated ($v)"; else -> "High ($v)"
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DATA MODELS
    // ═══════════════════════════════════════════════════════════════════

    data class BiometricSnapshot(
        val hrv: Int?,          // ms  — Heart Rate Variability
        val restingHr: Int?,    // bpm — Resting Heart Rate
        val sleepHours: Float?, // hrs — last night's total sleep
        val spo2: Int?,         // %   — Blood Oxygen Saturation
        val stressLevel: Int?   // 0-100 — higher = more stressed
    )

    data class MetricScores(
        val hrv: Int,         // 0-100
        val restingHr: Int,   // 0-100
        val sleep: Int,       // 0-100
        val spo2: Int,        // 0-100
        val stress: Int,      // 0-100 (inverted from raw stress)
        val composite: Int    // weighted recovery score
    )

    data class HealthReport(val narrative: String)

    // ═══════════════════════════════════════════════════════════════════
    //  ON-DEVICE SCORING ENGINE
    //  Clinical reference ranges from sports-medicine literature.
    //  All math runs in < 1 ms — no network, no model, no permissions.
    // ═══════════════════════════════════════════════════════════════════

    object HealthScoreEngine {

        // ── Individual metric scores ──────────────────────────────────

        /** HRV: higher = better. Scored relative to a normal adult range (20–100 ms). */
        fun scoreHrv(hrv: Int?): Int {
            hrv ?: return 50  // neutral when no data
            return when {
                hrv >= 80 -> 100
                hrv >= 65 -> lerp(80, 100, hrv, 65, 80)
                hrv >= 50 -> lerp(60, 80, hrv, 50, 65)
                hrv >= 35 -> lerp(35, 60, hrv, 35, 50)
                hrv >= 20 -> lerp(10, 35, hrv, 20, 35)
                else      -> 5
            }
        }

        /** Resting HR: lower = better (within safe range). Optimal 45–65 bpm. */
        fun scoreRhr(rhr: Int?): Int {
            rhr ?: return 50
            return when {
                rhr < 40  -> 40   // bradycardia — flag cautiously
                rhr <= 55 -> 100
                rhr <= 65 -> lerp(80, 100, rhr, 55, 65)
                rhr <= 75 -> lerp(55, 80,  rhr, 65, 75)
                rhr <= 85 -> lerp(25, 55,  rhr, 75, 85)
                rhr <= 95 -> lerp(5,  25,  rhr, 85, 95)
                else      -> 5
            }
        }

        /** Sleep: optimal 7–9 hrs. Penalties for both under and over-sleeping. */
        fun scoreSleep(hours: Float?): Int {
            hours ?: return 50
            return when {
                hours >= 7.0f && hours <= 9.0f -> 100
                hours >= 6.5f                  -> lerp(80, 100, hours, 6.5f, 7.0f)
                hours >= 6.0f                  -> lerp(60, 80,  hours, 6.0f, 6.5f)
                hours >= 5.0f                  -> lerp(30, 60,  hours, 5.0f, 6.0f)
                hours > 9.0f && hours <= 10.0f -> lerp(80, 100, hours, 9.0f, 10.0f)  // mild over
                hours > 10.0f                  -> 65  // over-sleeping, possible fatigue
                else                           -> 10
            }
        }

        /** SpO2: clinical thresholds. ≥97% = excellent, <93% = concerning. */
        fun scoreSpo2(spo2: Int?): Int {
            spo2 ?: return 50
            return when {
                spo2 >= 97 -> 100
                spo2 >= 95 -> lerp(75, 100, spo2, 95, 97)
                spo2 >= 93 -> lerp(40, 75,  spo2, 93, 95)
                spo2 >= 90 -> lerp(10, 40,  spo2, 90, 93)
                else       -> 5
            }
        }

        /** Stress: raw 0-100 inverted — lower stress = higher score. */
        fun scoreStress(stress: Int?): Int {
            stress ?: return 50
            return (100 - stress).coerceIn(0, 100)
        }

        /** Composite weighted recovery score. */
        fun score(b: BiometricSnapshot): MetricScores {
            val hScore  = scoreHrv(b.hrv)
            val rScore  = scoreRhr(b.restingHr)
            val slScore = scoreSleep(b.sleepHours)
            val o2Score = scoreSpo2(b.spo2)
            val stScore = scoreStress(b.stressLevel)

            // Weights: HRV 35%, Sleep 25%, RHR 20%, SpO2 10%, Stress 10%
            val composite = (hScore * 0.35 + slScore * 0.25 + rScore * 0.20 +
                             o2Score * 0.10 + stScore * 0.10).roundToInt()

            return MetricScores(hScore, rScore, slScore, o2Score, stScore, composite)
        }

        // ── Helpers ──────────────────────────────────────────────────

        /** Linear interpolation: map value from [inLow, inHigh] to [outLow, outHigh]. */
        private fun lerp(outLow: Int, outHigh: Int, v: Int, inLow: Int, inHigh: Int): Int {
            if (inHigh == inLow) return outLow
            val t = (v - inLow).toFloat() / (inHigh - inLow)
            return (outLow + t * (outHigh - outLow)).roundToInt().coerceIn(outLow, outHigh)
        }

        private fun lerp(outLow: Int, outHigh: Int, v: Float, inLow: Float, inHigh: Float): Int {
            if (inHigh == inLow) return outLow
            val t = (v - inLow) / (inHigh - inLow)
            return (outLow + t * (outHigh - outLow)).roundToInt().coerceIn(outLow, outHigh)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ON-DEVICE NARRATIVE ENGINE
    //  Selects and combines pre-written clinical paragraph templates
    //  based on metric score tiers. Produces varied, natural output
    //  with no network dependency and no LLM required.
    // ═══════════════════════════════════════════════════════════════════

    object NarrativeEngine {

        private enum class Tier { HIGH, MID, LOW }

        private fun tier(score: Int) = when {
            score >= 67 -> Tier.HIGH
            score >= 34 -> Tier.MID
            else        -> Tier.LOW
        }

        fun generate(b: BiometricSnapshot, s: MetricScores): HealthReport {
            val paragraphs = listOf(
                overviewParagraph(s),
                sleepParagraph(b.sleepHours, s.sleep),
                cardiovascularParagraph(b.hrv, b.restingHr, s.hrv, s.restingHr),
                recommendationParagraph(b, s)
            )
            return HealthReport(paragraphs.joinToString("\n\n"))
        }

        // ── Overview ─────────────────────────────────────────────────

        private fun overviewParagraph(s: MetricScores): String = when (tier(s.composite)) {
            Tier.HIGH -> pick(
                "Your biometrics are in excellent shape today. With a recovery score of ${s.composite}, your body has adapted well to recent training and daily demands. Your autonomic nervous system is well-balanced, and your body is primed to take on moderate to high intensity efforts.",
                "Today's data paints a strong picture of readiness. A composite score of ${s.composite} reflects thorough recovery overnight and stable physiological signals across all measured dimensions. You are entering the day in an optimal state.",
                "Outstanding recovery profile this session. Your ${s.composite}/100 score reflects low physiological strain and strong overnight regeneration. Your nervous system appears well-rested and your cardiovascular markers are trending positively."
            )
            Tier.MID -> pick(
                "Your recovery is moderate today, sitting at ${s.composite}/100. Some markers are performing well while others indicate your body is still processing recent stress. A measured approach to intensity is advisable — productive effort is possible, but listen for early fatigue signals.",
                "Today's score of ${s.composite} places you in the moderate recovery zone. Your body has partially recovered but has not yet reached peak readiness. Consider this a yellow-light day — manageable workloads are fine, but high-intensity output may compound accumulated fatigue.",
                "With a recovery score of ${s.composite}, your physiology is in a transitional state. Some systems are well-rested while others are still recovering. Prioritise quality over volume in any planned activity today."
            )
            Tier.LOW -> pick(
                "Your body is showing signs of elevated strain today, reflected in a recovery score of ${s.composite}/100. One or more key markers — HRV, sleep, or resting heart rate — are below your optimal range. This is a signal to prioritise rest, light movement, and recovery-focused habits.",
                "Today's data suggests your system needs support. A score of ${s.composite} indicates incomplete recovery and heightened physiological load. Pushing through high-intensity efforts today is not recommended — active recovery, hydration, and quality sleep tonight will return you to optimal sooner.",
                "Recovery score of ${s.composite} today. Your autonomic markers are depressed, which often follows sustained stress, disrupted sleep, or accumulated training load. Treat today as a genuine rest day — your long-term performance depends on respecting this signal."
            )
        }

        // ── Sleep ─────────────────────────────────────────────────────

        private fun sleepParagraph(hours: Float?, score: Int): String {
            val hoursStr = hours?.let { "%.1f hours".format(it) } ?: "an unrecorded duration"
            return when (tier(score)) {
                Tier.HIGH -> pick(
                    "Sleep quality looks strong. You logged $hoursStr last night, which falls within the optimal 7–9 hour window. Adequate sleep is the single biggest driver of recovery, and your data reflects this — deep sleep cycles have likely completed fully, supporting muscle repair, memory consolidation, and hormonal balance.",
                    "Your sleep duration of $hoursStr is excellent. Research consistently identifies 7–9 hours as the window in which HRV peaks, cortisol normalises, and cognitive performance reaches its daily high. Your recovery score is benefiting directly from this.",
                    "You got $hoursStr of sleep — a strong result. Sleep is when the body produces growth hormone, repairs cellular damage, and resets the autonomic nervous system. Your score reflects the downstream benefit of a full night's rest."
                )
                Tier.MID -> pick(
                    "You recorded $hoursStr of sleep. This is slightly below the 7–9 hour optimal range, which may be limiting your full recovery potential. You'll likely function well today, but a 30–45 minute increase in sleep duration over the coming nights would noticeably improve your readiness scores.",
                    "Sleep duration came in at $hoursStr — adequate, but with room to improve. Mild sleep restriction in the 6–7 hour range tends to reduce HRV, raise resting heart rate slightly, and reduce reaction time over multi-day periods. Aim to extend your sleep window by prioritising an earlier bedtime.",
                    "With $hoursStr logged, your sleep is in the moderate tier. You've covered most of your body's minimum needs, but optimal recovery is typically achieved above 7 hours. Consistency in your sleep schedule often matters as much as total duration."
                )
                Tier.LOW -> pick(
                    "Sleep is flagging as your weakest metric tonight — $hoursStr logged. Sleep deprivation suppresses HRV, elevates resting cortisol, and reduces glucose metabolism efficiency. No nutrition strategy or training protocol compensates for persistent short sleep. Tonight, prioritise a consistent sleep and wake time and minimise screen exposure in the hour before bed.",
                    "You recorded just $hoursStr last night. This is below the minimum threshold for complete physiological recovery. Your low recovery score is likely driven substantially by this shortfall. The body's immune repair, hormonal reset, and cardiovascular recovery all require adequate slow-wave and REM sleep stages that shorter durations often cut short.",
                    "Only $hoursStr of sleep recorded. This is the most actionable finding in today's report. Short sleep is associated with elevated inflammatory markers, reduced cognitive function, and impaired physical performance. Focus on sleep hygiene tonight: consistent timing, a cool dark environment, and limiting caffeine after midday."
                )
            }
        }

        // ── Cardiovascular ────────────────────────────────────────────

        private fun cardiovascularParagraph(
            hrv: Int?, rhr: Int?, hrvScore: Int, rhrScore: Int
        ): String {
            val hrvStr = hrv?.let { "$it ms" } ?: "not recorded"
            val rhrStr = rhr?.let { "$it bpm" } ?: "not recorded"
            val avgCv  = (hrvScore + rhrScore) / 2

            return when (tier(avgCv)) {
                Tier.HIGH -> pick(
                    "Cardiovascular markers are strong today. Your HRV of $hrvStr indicates a well-balanced autonomic nervous system with healthy parasympathetic dominance — the signature of genuine recovery. A resting heart rate of $rhrStr is efficient, meaning your heart is pumping effectively without undue load.",
                    "Your heart rate variability ($hrvStr) and resting heart rate ($rhrStr) are both performing well. These two metrics together are among the most reliable indicators of true physiological readiness. High HRV reflects low cumulative stress and good cardiac health; a low resting HR reflects cardiovascular efficiency built through consistent aerobic conditioning.",
                    "Excellent cardiovascular signature today: HRV $hrvStr, resting HR $rhrStr. HRV above 50 ms typically indicates the parasympathetic (rest and digest) branch of the nervous system is dominant — the optimal state for performance and recovery. Your heart is working efficiently."
                )
                Tier.MID -> pick(
                    "Your cardiovascular metrics are moderate. HRV of $hrvStr and resting heart rate of $rhrStr suggest partial recovery. HRV can fluctuate with alcohol, late meals, travel, emotional stress, and cumulative training load — if any of these apply, they likely explain the reading. Track trends over 7+ days for a more accurate baseline.",
                    "HRV ($hrvStr) and resting heart rate ($rhrStr) are within acceptable ranges but not at their best. This pattern is common mid-training block or after a period of elevated life stress. Your autonomic system is functional but showing signs of load. Moderate effort today is supported; maximal efforts are not.",
                    "Cardiovascular readiness is moderate today — HRV $hrvStr, RHR $rhrStr. A depressed HRV combined with a slightly elevated resting HR is a classic sign that the sympathetic (fight-or-flight) branch is more active than usual. This is normal in busy periods; a recovery day often resets this within 24–48 hours."
                )
                Tier.LOW -> pick(
                    "Your cardiovascular indicators are under strain. An HRV of $hrvStr and resting heart rate of $rhrStr together signal elevated sympathetic nervous system activity and insufficient recovery. This combination frequently follows inadequate sleep, high training volume, illness, or significant psychological stress. Avoid high-intensity exercise today.",
                    "HRV of $hrvStr is below optimal, and your resting heart rate of $rhrStr confirms the picture — your body is not fully recovered. Low HRV is one of the earliest detectable signs of overtraining, incoming illness, or excessive accumulated fatigue. Take this data seriously and prioritise recovery over output today.",
                    "Low cardiovascular readiness today: HRV $hrvStr, resting HR $rhrStr. When both metrics are suppressed simultaneously, the evidence for physiological stress is strong. Consider whether recent training load, sleep debt, nutritional deficits, or external stressors are contributing — and address the underlying cause rather than pushing through."
                )
            }
        }

        // ── Recommendation ────────────────────────────────────────────

        private fun recommendationParagraph(b: BiometricSnapshot, s: MetricScores): String {
            // Lead with the metric needing the most attention
            val weakest = mapOf(
                "hrv"   to s.hrv,
                "sleep" to s.sleep,
                "rhr"   to s.restingHr,
                "spo2"  to s.spo2,
                "stress" to s.stress
            ).minByOrNull { it.value }?.key ?: "hrv"

            return when {
                s.composite >= 67 -> pick(
                    "Today's recommendation: capitalise on your strong recovery. This is an ideal day for a challenging training session, an important meeting, or a demanding creative task. Your cognitive and physical resources are well-stocked. Stay hydrated, warm up properly, and trust your preparation.",
                    "You are in a green-light state. Use it intentionally — whether that means a quality training session, a focused work block, or a social commitment that requires energy. Protect your sleep tonight to carry this momentum into tomorrow.",
                    "Recommendation: lean into today's readiness. Your metrics support higher output across physical and cognitive domains. One watchpoint — avoid the temptation to over-train on high-recovery days. Quality and intention matter more than volume."
                )
                weakest == "sleep" -> pick(
                    "Priority recommendation: sleep is today's limiting factor. Set a bedtime alarm 30 minutes earlier than usual, reduce screen brightness after 9 PM, and avoid caffeine after 2 PM. A single night of quality sleep (7.5–8.5 hrs) will meaningfully shift tomorrow's recovery score.",
                    "The clearest action you can take today is to protect tonight's sleep. Avoid alcohol (which fragments sleep architecture), keep your sleep and wake time consistent, and consider a 10–20 minute nap this afternoon if you're feeling the deficit. Sleep is the highest-leverage recovery tool available.",
                    "Your data suggests sleep is the primary bottleneck. Focus today on low-intensity activity, stress reduction, and setting the conditions for a restorative night. Magnesium glycinate before bed, a cooler room (18–19°C), and a consistent wind-down routine are evidence-backed tools."
                )
                weakest == "stress" -> pick(
                    "Stress is elevated today. Physiological stress responses — elevated cortisol, suppressed HRV, disrupted sleep — are cumulative. Consider scheduling a deliberate recovery window: 10 minutes of slow diaphragmatic breathing (5 seconds in, 6 seconds out) measurably shifts the autonomic nervous system toward parasympathetic dominance.",
                    "Your stress marker is the primary concern. Non-sleep deep rest (NSDR) protocols, a brief walk in natural light, or simply reducing task-switching for a 90-minute focused block can lower cortisol and improve afternoon performance. Limit high-stakes decisions until you've had a recovery window.",
                    "Elevated stress is the most actionable finding today. Physical stress (exercise) and psychological stress use the same physiological resources. If your emotional or cognitive load is high, reduce physical training intensity proportionally. Recovery is not one-dimensional."
                )
                weakest == "hrv" -> pick(
                    "HRV is the primary area to address. Common drivers of suppressed HRV include alcohol, late-night eating, dehydration, and training load. Review the last 48 hours for these factors. For today, limit high-intensity training and focus on aerobic work at or below your aerobic threshold (conversational pace).",
                    "Focus on HRV restoration. Consistent sleep timing, adequate hydration (aim for 35 ml per kg bodyweight), and zone 2 aerobic activity (not high-intensity) are the three most evidence-supported HRV-raising interventions you can act on today and tonight.",
                    "Your HRV needs attention. This metric responds best to consistent habits over days, not single sessions. The most effective lever is sleep — both duration and timing. Secondary levers include hydration, reduced alcohol intake, cold exposure (brief cold shower), and managed training load."
                )
                weakest == "rhr" -> pick(
                    "Your resting heart rate is elevated relative to baseline. This is most often a sign of incomplete recovery, dehydration, or early illness. Drink water before anything else today (500 ml on waking is a useful protocol), keep today's activity light, and monitor for other illness symptoms.",
                    "Elevated resting HR is today's key signal. Your heart is working harder than usual at rest, which typically reflects elevated sympathetic tone. Avoid high-intensity exercise, prioritise hydration and whole foods, and aim for 8+ hours of sleep tonight to allow cardiac recovery.",
                    "Today's focus: support your cardiovascular system. An elevated resting heart rate often normalises within 24–48 hours when sleep, hydration, and low physical stress are prioritised. If it remains elevated for 3+ consecutive days, consult a healthcare professional."
                )
                else /* spo2 */ -> pick(
                    "Your blood oxygen reading warrants attention. SpO₂ below 95% at rest can reflect nasal congestion, shallow breathing patterns, altitude effects, or in some cases underlying respiratory issues. Practice slow deep breathing (box breathing: 4 seconds in, 4 hold, 4 out, 4 hold) and ensure your sleeping environment is well-ventilated.",
                    "Prioritise breathing quality today. Low SpO₂ can reduce cellular energy production and cognitive clarity. Avoid sleeping on your back if you snore (lateral position improves airway patency), keep indoor air circulating, and stay well-hydrated to support mucosal health.",
                    "Blood oxygen is today's watchpoint. Mild drops in SpO₂ are common with poor sleep position, congestion, or high altitude. If the reading persists below 94% across multiple sessions at rest, seek medical evaluation — persistent low SpO₂ is not a metric to optimise around without professional input."
                )
            }
        }

        // ── Utilities ─────────────────────────────────────────────────

        /** Pseudo-random pick from options — deterministic per hour so
         *  the report is stable within a session but varies day to day. */
        private fun pick(vararg options: String): String {
            val seed = System.currentTimeMillis() / 3_600_000  // changes hourly
            return options[(seed % options.size).toInt().absoluteValue]
        }
    }
}
