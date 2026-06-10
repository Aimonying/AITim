package com.tingjizhushou.ui.state

/**
 * UI state for meeting summary generation.
 * Represents the state of AI-powered meeting minutes generation.
 */
data class SummaryState(
    val isGenerating: Boolean = false,
    val title: String? = null,
    val location: String? = null,
    val participants: String? = null,
    val agenda: String? = null,
    val conclusion: String? = null,
    val rawText: String? = null,
    val error: String? = null
) {
    /**
     * Check if summary has been generated.
     */
    val hasSummary: Boolean
        get() = title != null || participants != null || agenda != null || conclusion != null
    
    /**
     * Check if in error state.
     */
    val isError: Boolean
        get() = error != null
    
    /**
     * Get a display title or default.
     */
    fun getDisplayTitle(): String {
        return title ?: "会议纪要"
    }
    
    /**
     * Get participant count.
     */
    fun getParticipantCount(): Int {
        return participants?.split(Regex("[,，、]"))?.size ?: 0
    }
    
    /**
     * Check if agenda is available.
     */
    fun hasAgenda(): Boolean {
        return !agenda.isNullOrBlank()
    }
    
    /**
     * Check if conclusion is available.
     */
    fun hasConclusion(): Boolean {
        return !conclusion.isNullOrBlank()
    }
    
    /**
     * Generate formatted summary text.
     */
    fun toFormattedText(): String {
        return buildString {
            appendLine("# ${getDisplayTitle()}")
            appendLine()
            
            location?.let { 
                appendLine("**地点**: $it")
            }
            
            participants?.let {
                appendLine("**参会人员**: $it")
            }
            
            appendLine()
            
            agenda?.let {
                appendLine("## 议程")
                appendLine(it)
                appendLine()
            }
            
            conclusion?.let {
                appendLine("## 结论")
                appendLine(it)
                appendLine()
            }
            
            rawText?.let {
                appendLine("---")
                appendLine("## 原始记录")
                appendLine(it)
            }
        }
    }
}
