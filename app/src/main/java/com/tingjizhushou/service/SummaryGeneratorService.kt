package com.tingjizhushou.service

import com.tingjizhushou.data.local.db.entity.SummaryEntity
import com.tingjizhushou.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for generating meeting summaries from transcription text.
 * Uses template-based extraction to identify key meeting elements.
 */
@Singleton
class SummaryGeneratorService @Inject constructor() {
    
    // Chinese patterns
    private val chinesePatterns = ChineseSummaryPatterns()
    
    // English patterns
    private val englishPatterns = EnglishSummaryPatterns()
    
    /**
     * Generate a summary from transcription text.
     * @param text The transcription text to summarize
     * @param language Language code (e.g., "zh-CN", "en-US")
     * @return Result with the generated summary or failure
     */
    suspend fun generateSummary(text: String, language: String): Result<SummaryEntity> = withContext(Dispatchers.Default) {
        try {
            if (text.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Text cannot be empty"))
            }
            
            val patterns = when {
                language.startsWith("zh") -> chinesePatterns
                language.startsWith("en") -> englishPatterns
                else -> chinesePatterns // Default to Chinese
            }
            
            val summary = generateFromTemplate(text, patterns, language)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate summary using pattern extraction.
     */
    private suspend fun generateFromTemplate(
        text: String,
        patterns: SummaryPatterns,
        language: String
    ): SummaryEntity = withContext(Dispatchers.Default) {
        val cleanText = cleanText(text)
        
        val title = extractTitle(cleanText, patterns)
        val location = extractLocation(cleanText, patterns)
        val participants = extractParticipants(cleanText, patterns)
        val agenda = extractAgenda(cleanText, patterns)
        val conclusion = extractConclusion(cleanText, patterns)
        
        SummaryEntity(
            id = 0,
            recordId = 0, // Will be set when associating with a record
            title = title,
            location = location,
            participants = participants,
            agenda = agenda,
            conclusion = conclusion,
            rawText = text,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Extract title from text.
     */
    private fun extractTitle(text: String, patterns: SummaryPatterns): String {
        // Try to find explicit title markers
        val titlePatterns = listOf(
            patterns.titlePattern,
            "会议主题[:：]\\s*(.+?)(?:\\n|$)",
            "会议名称[:：]\\s*(.+?)(?:\\n|$)",
            "^【(.+?)】",
            "^(.+?)(?:会议|讨论|交流)"
        )
        
        for (patternStr in titlePatterns) {
            try {
                val pattern = Pattern.compile(patternStr, Pattern.MULTILINE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    return matcher.group(1)?.trim() ?: continue
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        // Fallback: use first non-empty line
        val firstLine = text.lines()
            .firstOrNull { it.isNotBlank() && it.length > 5 }
            ?.trim()
            ?.take(100) ?: "未命名会议"
        
        return firstLine
    }
    
    /**
     * Extract location from text.
     */
    private fun extractLocation(text: String, patterns: SummaryPatterns): String? {
        val locationPatterns = listOf(
            patterns.locationPattern,
            "地点[:：]\\s*(.+?)(?:\\n|$)",
            "会议地点[:：]\\s*(.+?)(?:\\n|$)",
            "位置[:：]\\s*(.+?)(?:\\n|$)",
            "(?:在|于|到)(.+?)(?:举行|召开|进行|讨论|开会)"
        )
        
        for (patternStr in locationPatterns) {
            try {
                val pattern = Pattern.compile(patternStr, Pattern.MULTILINE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    val location = matcher.group(1)?.trim() ?: continue
                    if (location.length > 2 && location.length < 50) {
                        return location
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        return null
    }
    
    /**
     * Extract participants from text.
     */
    private fun extractParticipants(text: String, patterns: SummaryPatterns): String? {
        val participantPatterns = listOf(
            patterns.participantsPattern,
            "参会人员[:：]\\s*(.+?)(?:\\n|$)",
            "与会人员[:：]\\s*(.+?)(?:\\n|$)",
            "参加人员[:：]\\s*(.+?)(?:\\n|$)",
            "参会者[:：]\\s*(.+?)(?:\\n|$)",
            "出席人员[:：]\\s*(.+?)(?:\\n|$)"
        )
        
        for (patternStr in participantPatterns) {
            try {
                val pattern = Pattern.compile(patternStr, Pattern.MULTILINE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    val participants = matcher.group(1)?.trim() ?: continue
                    if (participants.isNotBlank()) {
                        return normalizeParticipants(participants)
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        // Try to extract names using common patterns
        val namePattern = if (patterns is ChineseSummaryPatterns) {
            "[A-Z\u4e00-\u9fa5]{2,4}(?:\\s*[A-Z\u4e00-\u9fa5]{1,4})*"
        } else {
            "[A-Z][a-z]+\\s+[A-Z][a-z]+"
        }
        
        val nameMatcher = Pattern.compile(namePattern).matcher(text)
        val names = mutableSetOf<String>()
        while (nameMatcher.find() && names.size < 20) {
            val name = nameMatcher.group().trim()
            if (name.length >= 2) {
                names.add(name)
            }
        }
        
        return if (names.isNotEmpty()) {
            names.joinToString("、")
        } else null
    }
    
    /**
     * Extract agenda items from text.
     */
    private fun extractAgenda(text: String, patterns: SummaryPatterns): String? {
        val agendaPatterns = listOf(
            patterns.agendaPattern,
            "议程[:：]\\s*(.+?)(?:\\n|$)",
            "会议议程[:：]\\s*(.+?)(?:\\n|$)",
            "议题[:：]\\s*(.+?)(?:\\n|$)",
            "讨论事项[:：]\\s*(.+?)(?:\\n|$)"
        )
        
        for (patternStr in agendaPatterns) {
            try {
                val pattern = Pattern.compile(patternStr, Pattern.MULTILINE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    val agenda = matcher.group(1)?.trim() ?: continue
                    if (agenda.isNotBlank()) {
                        return formatAgenda(agenda)
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        // Try to extract numbered items
        val numberedPattern = "(?:\\d+[.、]|(?:第\\d+[章节]))\\s*(.+?)(?=\\n|\\d+[.、]|$)"
        val numberMatcher = Pattern.compile(numberedPattern, Pattern.MULTILINE).matcher(text)
        val items = mutableListOf<String>()
        while (numberMatcher.find() && items.size < 10) {
            val item = numberMatcher.group(1)?.trim() ?: continue
            if (item.length > 3) {
                items.add(item)
            }
        }
        
        return if (items.isNotEmpty()) {
            items.mapIndexed { index, item -> "${index + 1}. $item" }
                .joinToString("\n")
        } else null
    }
    
    /**
     * Extract conclusion from text.
     */
    private fun extractConclusion(text: String, patterns: SummaryPatterns): String? {
        val conclusionPatterns = listOf(
            patterns.conclusionPattern,
            "结论[:：]\\s*(.+?)(?:\\n|$)",
            "总结[:：]\\s*(.+?)(?:\\n|$)",
            "会议总结[:：]\\s*(.+?)(?:\\n|$)",
            "决定事项[:：]\\s*(.+?)(?:\\n|$)",
            "下一步[:：]\\s*(.+?)(?:\\n|$)"
        )
        
        for (patternStr in conclusionPatterns) {
            try {
                val pattern = Pattern.compile(patternStr, Pattern.MULTILINE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    val conclusion = matcher.group(1)?.trim() ?: continue
                    if (conclusion.isNotBlank()) {
                        return conclusion
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        return null
    }
    
    /**
     * Clean and normalize text.
     */
    private fun cleanText(text: String): String {
        return text
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .replace(Regex("[\\x00-\\x1f]"), "") // Remove control characters
            .trim()
    }
    
    /**
     * Normalize participant names.
     */
    private fun normalizeParticipants(participants: String): String {
        return participants
            .split(Regex("[,，、\\n]"))
            .map { it.trim() }
            .filter { it.length >= 2 && it.length <= 10 }
            .distinct()
            .joinToString("、")
    }
    
    /**
     * Format agenda items.
     */
    private fun formatAgenda(agenda: String): String {
        val items = agenda.split(Regex("[,，;；\\n]"))
            .map { it.trim() }
            .filter { it.length > 3 }
            .distinct()
        
        return items.mapIndexed { index, item -> "${index + 1}. $item" }
            .joinToString("\n")
    }
    
    /**
     * Generate a formatted summary document.
     */
    fun generateFormattedSummary(summary: SummaryEntity, language: String): String {
        val isChinese = language.startsWith("zh")
        val sb = StringBuilder()
        
        // Title
        sb.appendLine("# ${summary.title ?: (if (isChinese) "会议纪要" else "Meeting Minutes")}")
        sb.appendLine()
        
        // Metadata
        sb.appendLine("**${if (isChinese) "时间" else "Date"}**: ${TimeUtils.formatDateTime(summary.createdAt)}")
        
        summary.location?.let {
            sb.appendLine("**${if (isChinese) "地点" else "Location"}**: $it")
        }
        
        summary.participants?.let {
            sb.appendLine("**${if (isChinese) "参会人员" else "Participants"}**: $it")
        }
        
        sb.appendLine()
        
        // Agenda
        summary.agenda?.let {
            sb.appendLine("## ${if (isChinese) "议程" else "Agenda"}")
            sb.appendLine(it)
            sb.appendLine()
        }
        
        // Conclusion
        summary.conclusion?.let {
            sb.appendLine("## ${if (isChinese) "结论" else "Conclusion"}")
            sb.appendLine(it)
            sb.appendLine()
        }
        
        // Raw transcript
        summary.rawText?.let {
            sb.appendLine("---")
            sb.appendLine("## ${if (isChinese) "原始记录" else "Raw Transcript"}")
            sb.appendLine(it)
        }
        
        return sb.toString()
    }
    
    /**
     * Abstract pattern holder for different languages.
     */
    private interface SummaryPatterns {
        val titlePattern: String
        val locationPattern: String
        val participantsPattern: String
        val agendaPattern: String
        val conclusionPattern: String
    }
    
    /**
     * Chinese language patterns.
     */
    private class ChineseSummaryPatterns : SummaryPatterns {
        override val titlePattern = "(?:会议主题|会议名称)[:：]\\s*(.+?)(?:\\n|$)"
        override val locationPattern = "(?:会议地点|地点)[:：]\\s*(.+?)(?:\\n|$)"
        override val participantsPattern = "(?:参会人员|与会人员|参加人员)[:：]\\s*(.+?)(?:\\n|$)"
        override val agendaPattern = "(?:议程|议题)[:：]\\s*(.+?)(?:\\n|$)"
        override val conclusionPattern = "(?:结论|总结|决定事项)[:：]\\s*(.+?)(?:\\n|$)"
    }
    
    /**
     * English language patterns.
     */
    private class EnglishSummaryPatterns : SummaryPatterns {
        override val titlePattern = "(?:Meeting Topic|Meeting Title)[:\\s]*(.+?)(?:\\n|$)"
        override val locationPattern = "(?:Location|Venue)[:\\s]*(.+?)(?:\\n|$)"
        override val participantsPattern = "(?:Attendees|Participants)[:\\s]*(.+?)(?:\\n|$)"
        override val agendaPattern = "(?:Agenda|Topics)[:\\s]*(.+?)(?:\\n|$)"
        override val conclusionPattern = "(?:Conclusion|Summary|Decisions)[:\\s]*(.+?)(?:\\n|$)"
    }
}
