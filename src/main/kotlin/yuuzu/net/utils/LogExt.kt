package yuuzu.net.utils

private const val ANSI_RESET = "\u001B[0m"
private const val ANSI_BLUE = "\u001B[34m"
private const val ANSI_GREEN = "\u001B[32m"
private const val ANSI_YELLOW = "\u001B[33m"
private const val ANSI_RED = "\u001B[31m"

fun String.logi() { // color: blue
    println("$ANSI_BLUE$this$ANSI_RESET")
}

fun String.logd() { // color: green
    println("$ANSI_GREEN$this$ANSI_RESET")
}

fun String.logw() { // color: yellow
    println("$ANSI_YELLOW$this$ANSI_RESET")
}

fun String.loge() { // color: red
    println("$ANSI_RED$this$ANSI_RESET")
}