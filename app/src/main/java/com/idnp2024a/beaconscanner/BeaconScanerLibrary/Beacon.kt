package com.idnp2024a.beaconscanner.BeaconScanerLibrary

class Beacon(
    val macAddress: String?,
    var manufacturer: String? = null,
    var type: BeaconType = BeaconType.ANY,
    var uuid: String? = null,
    var major: Int? = null,
    var minor: Int? = null,
    var namespace: String? = null,
    var instance: String? = null,
    var rssi: Int? = null,
    var txPower: Int? = null,
) {
    enum class BeaconType {
        IBEACON, EDDYSTONE_UID, ANY
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Beacon) return false
        if (uuid != other.uuid) return false
        return true
    }

//    private val movingAverageFilter = MovingAverageFilter(5)
    fun calculateDistanceAverageFilter(txPower: Int, rssi: Int, n: Double): Double? {
        return MovingAverageFilter.calculateDistance(txPower, rssi, n)
    }

    override fun hashCode(): Int {
        return uuid?.hashCode() ?: 0
    }
    override fun toString(): String {
        return "Beacon(macAddress=$macAddress, manufacturer=$manufacturer, type=$type, uuid=$uuid, major=$major, minor=$minor, rssi=$rssi, TxPower=$txPower)"
    }

    fun calculateDistance(txPower: Int, rssi: Int, N: Double): Any {
        val factor = (txPower - rssi) / (10 * N)
        val distance = Math.pow(10.0, factor)
        return distance;

    }
}
