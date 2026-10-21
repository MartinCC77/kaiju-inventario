package cl.duoc.kaiju.utils

fun String.esEnteroValido(): Boolean = this.toIntOrNull() != null

fun String.esDecimalValido(): Boolean = this.toDoubleOrNull() != null