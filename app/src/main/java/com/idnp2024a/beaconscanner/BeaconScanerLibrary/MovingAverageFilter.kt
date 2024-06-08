package com.idnp2024a.beaconscanner.BeaconScanerLibrary

import android.util.Log
import java.util.LinkedList

object MovingAverageFilter {
    private const val size = 5 // Tamaño de la ventana de promedio móvil
    private val distanceQueue = LinkedList<Double>()
    private const val TAG = "MovingAverageFilter"

    /**
     * Calcula la distancia utilizando txPower y RSSI aplicando un filtro de promedio móvil.
     *
     * @param txPower La potencia de transmisión (dBm).
     * @param rssi La intensidad de la señal recibida (dBm).
     * @param N El exponente de atenuación de la señal.
     * @return La distancia filtrada.
     */
    fun calculateDistance(txPower: Int, rssi: Int, N: Double): Double {
        // Calcula el factor basado en txPower y RSSI
        val factor = (txPower - rssi) / (10 * N)
        // Calcula la distancia usando la fórmula de atenuación de la señal
        val distance = Math.pow(10.0, factor)

        // Añade la nueva distancia calculada a la cola
        distanceQueue.add(distance)
        // Si la cola supera el tamaño definido, elimina el valor más antiguo
        if (distanceQueue.size > size) {
            distanceQueue.remove()
        }

        // Calcula la suma de todas las distancias en la cola
        val sum = distanceQueue.sum()
        // Calcula el promedio móvil
        val movingAverage = sum / distanceQueue.size

        // Imprime en el log la suma y el promedio móvil calculado
        Log.d(TAG, "Using moving filter: sum=$sum, movingAverage=$movingAverage, num=${distanceQueue.size}")

        return movingAverage
    }
}
