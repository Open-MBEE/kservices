import gov.nasa.jpl.ae.util.distributions
import gov.nasa.jpl.ae.util.distributions.Normal

class BatteryClass {

    var netPower : TimeVaryingMap
    var z : Long = 0
    var setValObject : Object = netPower.setValue(z, 0.0)

    var netPowerIntegrated : TimeVaryingMap = netPower.integrate()
    var netDeltaWattHours : TimeVaryingMap = netPowerIntegrated.getDeltaMap()
    var batteryVoltage : TimeVaryingMap = TimeVaryingMap("", "/Users/dank/git/kservices/src/kTestCases/batteryVoltage.csv", null, Double.class)
    var rechargeEfficiency : Real = 0.95
    var ahCapacityBattery : Real = 183.0


    var netDeltaAmpHours: TimeVaryingMap = netDeltaWattHours/ (if batteryVoltage = null then 32.8 else batteryVoltage)
    var rechargeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours) * rechargeEfficiency/ahCapacityBattery

    var negativeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours)

    var deltaEnergy: TimeVaryingMap = rechargeDeltaEnergy + negativeDeltaEnergy

    var stateOfCharge : TimeVaryingMap = if deltaEnergy < 0 then 0 else (if deltaEnergy > 1 then 1 else deltaEnergy)

    class usePower extends DurativeEvent {
      var mu : Real
      var sigma : Real
      var randomPower : gov.nasa.jpl.ae.util.distributions.Normal = gov.nasa.jpl.ae.util.distributions.Normal(mu.getValue(false), sigma.getValue(false))

      -- Needs to return a boolean
      var negRandomDist : gov.nasa.jpl.ae.util.distributions.Normal = -randomPower
      var startRandomPower: Object = netPower.setValue(startTime, negRandomDist)
      var endRandomPower: Object = netPower.setValue(endTime, randomPower)
    }

    var up1 : usePower
    req up1.startTime = 1
    req up1.endTime = 4
    req up1.mu = 10
    req up1.sigma = 1

    var up2 : usePower
    req up2.startTime = 3
    req up2.endTime = 5
    req up2.mu = 20
    req up2.sigma = 4

}
