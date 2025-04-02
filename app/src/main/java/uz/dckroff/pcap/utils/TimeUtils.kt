package uz.dckroff.pcap.utils

import java.util.concurrent.TimeUnit

/**
 * Форматирует миллисекунды в строку формата "MM:SS"
 */
fun formatTime(milliseconds: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - 
            TimeUnit.MINUTES.toSeconds(minutes)
    
    return String.format("%02d:%02d", minutes, seconds)
} 