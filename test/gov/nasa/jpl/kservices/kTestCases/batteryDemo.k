req Timepoint.setEpoch("2020-001T00:00:00.000")
req Timepoint.setUnits("seconds")
req Timepoint.setHorizonDuration(10)

var epoch : Time = 0

var battery : Battery

req battery.ahCapacityBattery = 183.0
req battery.maxVoltage = 32.8
req battery.rechargeEfficiency = 0.95

req battery.load =  TimeVaryingMap("", "src/kTestCases/BusPower.csv", null, Double.class)

// Loading solarPower from file instead of using setValue() since bae is solving for other time values in solarPower from the netPower constraint.
req battery.solarPower =  TimeVaryingMap("", "src/kTestCases/SolarPower.csv", null, Double.class)
//req battery.solarPower.setValue(epoch, 850)
//req battery.solarPower.setValue(5, 0)
//req battery.solarPower.setValue(8, 850)

class Battery {

  -- Attributes

  var ahCapacityBattery : Real  -- amp-hours
  var maxVoltage : Real  -- volts
  var whCapacityBattery : Real = ahCapacityBattery * maxVoltage  -- watt-hours
  var rechargeEfficiency : Real

  -- Input Power

  var load : TimeVaryingMap[Real]  -- watts
  var solarPower : TimeVaryingMap[Real]  -- watts

  -- Output Voltage and Energy

  var voltage : TimeVaryingMap[Real]  -- volts
  var SOC : Consumable  -- fraction of amp-hour capacity
  var SOC_Ah : Consumable = SOC * ahCapacityBattery  -- amp-hours

  -- Behavior constraints

  var netPower : TimeVaryingMap[Real] = solarPower - load  -- watts

  var netPowerIntegrated : TimeVaryingMap[Real] =   -- watt-hours
  netPower.integrate() / Timepoint.hours(1.0)

  var netDeltaWattHours : TimeVaryingMap[Real] =
    netPowerIntegrated.getDeltaMap()

  var netDeltaAmpHours : TimeVaryingMap[Real] =
    netDeltaWattHours / (if voltage = null then maxVoltage else voltage)

  var rechargeDeltaEnergy: TimeVaryingMap[Real] =  -- fraction of battery capacity amp-hours
  max(0.0, netDeltaAmpHours) * rechargeEfficiency / ahCapacityBattery

  var negativeDeltaEnergy : TimeVaryingMap[Real] =  -- fraction of battery capacity amp-hours
  min(0.0, netDeltaAmpHours) / ahCapacityBattery

  var deltaEnergy : TimeVaryingMap[Real] =
    rechargeDeltaEnergy + negativeDeltaEnergy

  req SOC = Consumable("stateOfCharge", 1.0, deltaEnergy,
    0.0, 1.0, TimeVaryingMap.LINEAR)  -- fraction of amp-hours

  req voltage = maxVoltage - (1.0 - if SOC = null then 1.0 else SOC ) * 6.8
  -- req voltage = maxVoltage
}


/*



req Timepoint.setEpoch("2020-001T00:00:00.000")
req Timepoint.setHorizonDuration(100)
req Timepoint.setUnits("seconds")

var netPower : TimeVaryingMap = TimeVaryingMap("", "src/kTestCases/BusPower.csv", null, Double.class)
var netPowerIntegrated : TimeVaryingMap = netPower.integrate()
var netDeltaWattHours : TimeVaryingMap = netPowerIntegrated.getDeltaMap()
var batteryVoltage : TimeVaryingMap = TimeVaryingMap("", "src/kTestCases/batteryVoltage.csv", null, Double.class)
var rechargeEfficiency : Real = 0.95
var ahCapacityBattery : Real = 183.0


var netDeltaAmpHours: TimeVaryingMap = netDeltaWattHours/ (if batteryVoltage = null then 32.8 else batteryVoltage)
var rechargeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours) * rechargeEfficiency/ahCapacityBattery

var negativeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours)

var deltaEnergy: TimeVaryingMap = rechargeDeltaEnergy + negativeDeltaEnergy

var stateOfCharge : TimeVaryingMap[Real] = if deltaEnergy < 0 then 0 else (if deltaEnergy > 1 then 1 else deltaEnergy)

*/
