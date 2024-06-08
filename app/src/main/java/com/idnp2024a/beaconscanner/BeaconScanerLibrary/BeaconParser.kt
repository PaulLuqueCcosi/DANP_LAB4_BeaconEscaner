package com.idnp2024a.beaconscanner.BeaconScanerLibrary

class BeaconParser {

    companion object {
        private const val TAG: String = "BeaconParser"

        fun parseIBeacon(data: ByteArray, rssi: Int?): Beacon {
            val dataLen = Integer.parseInt(Utils.toHexString(data.copyOfRange(0, 1)), 16)
            val dataType = Integer.parseInt(Utils.toHexString(data.copyOfRange(1, 2)), 16)
            val leFlag = Integer.parseInt(Utils.toHexString(data.copyOfRange(2, 3)), 16)
            val len = Integer.parseInt(Utils.toHexString(data.copyOfRange(3, 4)), 16)
            val type = Integer.parseInt(Utils.toHexString(data.copyOfRange(4, 5)), 16)
            val company = Utils.toHexString(data.copyOfRange(5, 7))
            val subtype = Integer.parseInt(Utils.toHexString(data.copyOfRange(7, 8)), 16)
            val subtypeLen = Integer.parseInt(Utils.toHexString(data.copyOfRange(8, 9)), 16)
            val iBeaconUUID = Utils.toHexString(data.copyOfRange(9, 25))
            val major = Integer.parseInt(Utils.toHexString(data.copyOfRange(25, 27)), 16)
            val minor = Integer.parseInt(Utils.toHexString(data.copyOfRange(27, 29)), 16)
            val txPowerHex = Utils.toHexString(data.copyOfRange(29, 30))
            val txPower = Integer.parseInt(txPowerHex, 16).toByte().toInt()
            //val factor = (-1 * txPower - rssi!!) / (10 * 4.0)
            //val distance = Math.pow(10.0, factor)

            /*Log.d(
                TAG,
                "DECODE dataLen:$dataLen dataType:$dataType leFlag:$leFlag len:$len type:$type subtype:$subtype subtypeLen:$subtypeLen company:$company UUID:$iBeaconUUID major:$major minor:$minor txPower:$txPower"
            )*/

            return Beacon(
                macAddress = null,
                manufacturer = company,
                type = Beacon.BeaconType.IBEACON,
                uuid = iBeaconUUID,
                major = major,
                minor = minor,
                rssi = rssi,
                txPower = txPower
            )
        }
    }
}