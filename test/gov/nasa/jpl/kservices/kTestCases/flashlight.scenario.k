ParameterListenerImpl.setMaxLoopsWithNoProgress(10000)

class Controller {
    var Cstate: TimeVaryingMap[ControllerState]
    class activity_325 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = Off
    }
    class activity_326 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = Standby
    }
    class activity_327 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = On
    }
    class activity_328 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = Standby
    }
    class ControllerState
    val Standby: ControllerState
    val On: ControllerState
    val Off: ControllerState
    
    
}
class Battery {
    var V: TimeVaryingMap[Real]
    var deltaV: TimeVaryingMap[Real]
    var i: TimeVaryingMap[Real]
    req V = 10
    req V = deltaV
}
class Switch {
    var deltaV: TimeVaryingMap[Real]
    var i: TimeVaryingMap[Real]
    var Ps: TimeVaryingMap[SwitchPosition]
    class SwitchPosition_trigger
    val OPEN: SwitchPosition_trigger
    val CLOSE: SwitchPosition_trigger
    
    var SwitchPosition_cmd: TimeVaryingMap[SwitchPosition_trigger]
    class activity_312 extends DurativeEvent {
        req time >= startTime && time < endTime => Ps = Open
    }
    class activity_313 extends DurativeEvent {
        req time >= startTime && time < endTime => Ps = Closed
    }
    class activity_314 extends DurativeEvent {
        req time >= startTime && time < endTime => Ps = Open
    }
    class activity_329 extends DurativeEvent {
        req time >= startTime && time < endTime => i > 0
    }
    class SwitchPosition
    val Open: SwitchPosition
    val Closed: SwitchPosition
    
    req Ps != Closed || deltaV = 0
    req Ps != Open || i = 0
}
class Lamp {
    var R: TimeVaryingMap[Real]
    var eta: TimeVaryingMap[Real]
    var L: TimeVaryingMap[Real]
    var deltaV: TimeVaryingMap[Real]
    var i: TimeVaryingMap[Real]
    req R = 50
    req eta = 4
    req L = deltaV * i * eta
}
class SystemClass {
    var instance_Battery: Battery
    var instance_Lamp: Lamp
    var instance_Switch: Switch
    var instance_Controller: Controller
    req instance_Switch.i = instance_Battery.i
}

class LampON {
    
    
    
    var tp_224: Time
    var tp_232: Time
    var tp_238: Time
    var tp_246: Time
    var tp_248: Time
    var tp_240: Time
    var tp_254: Time
    var tp_242: Time
    var tp_260: Time
    var tp_266: Time
    var tp_226: Time
    
    req tp_232 + seconds(5.0) <= tp_246
    req tp_232 + seconds(5.0) >= tp_246
    req tp_248 + seconds(10.0) <= tp_240
    req tp_248 + seconds(15.0) >= tp_240
    req tp_238 <= tp_240
    req tp_266 <= tp_242
    req tp_254 + seconds(30.0) <= tp_242
    req tp_254 + seconds(30.0) >= tp_242
    req tp_260 + seconds(150.0) <= tp_226
    req tp_260 + seconds(200.0) >= tp_226
    var instance_SystemClass: SystemClass
    
    var goal00000: activity_312 = instance_SystemClass.instance_Switch.activity_312(startTime :: tp_224, endTime :: tp_238)
    var goal00001: activity_313 = instance_SystemClass.instance_Switch.activity_313(startTime :: tp_240, endTime :: tp_266)
    var goal00002: activity_314 = instance_SystemClass.instance_Switch.activity_314(startTime :: tp_242, endTime :: tp_226)
    var goal00003: activity_325 = instance_SystemClass.instance_Controller.activity_325(startTime :: tp_224, endTime :: tp_232)
    var goal00004: activity_326 = instance_SystemClass.instance_Controller.activity_326(startTime :: tp_246, endTime :: tp_248)
    var goal00005: activity_327 = instance_SystemClass.instance_Controller.activity_327(startTime :: tp_240, endTime :: tp_254)
    var goal00006: activity_328 = instance_SystemClass.instance_Controller.activity_328(startTime :: tp_242, endTime :: tp_260)
    var goal00007: activity_329 = instance_SystemClass.instance_Switch.activity_329(startTime :: tp_240, endTime :: tp_266)
}
var instance_LampON : LampON

